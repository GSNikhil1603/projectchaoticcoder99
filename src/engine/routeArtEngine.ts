import { GpsCoordinate, PointF, ColorBlob, LandmarkSticker } from '../types';

export const ART_PALETTES: Record<string, string[]> = {
  'Pastel Bloom': ['#56B386', '#866FB3', '#FF8A65', '#FFB74D', '#4FC3F7', '#A5D6A7'],
  'Watercolor Splash': ['#3D5A80', '#98C1D9', '#E0FBFC', '#EE6C4D', '#293241', '#84A98C'],
  'Neon Cyber': ['#00F5D4', '#7B2CBF', '#FF007F', '#F72585', '#4CC9F0', '#7209B7'],
  'Botanical Herbarium': ['#2D6A4F', '#52B788', '#74C69D', '#D8F3DC', '#B7E4C7', '#40916C'],
  'Monochrome Ink': ['#16211B', '#3A4D42', '#75857C', '#A4B0A7', '#D8DFD5', '#24332A'],
  'Campus Sunset': ['#FF6B6B', '#FFA07A', '#FFD166', '#4ECDC4', '#1A535C', '#F78C6B'],
  'Dorm Violet': ['#7B2CBF', '#9D4EDD', '#C77DFF', '#E0AAFF', '#3C096C', '#5A189A'],
};

export class RouteArtEngine {
  /**
   * Main entry point: converts GPS track into normalized generative art data
   */
  static processRoute(
    coords: GpsCoordinate[],
    artStyle = 'Pastel Bloom',
    canvasSize = 800,
    padding = 80
  ): {
    points: PointF[];
    blobs: ColorBlob[];
    shapeCategory: string;
    distanceKm: number;
    elevationGainMeters: number;
    svgPreview: string;
  } {
    if (coords.length < 2) {
      const fallbackPoints = this.generateFallbackPath(canvasSize);
      const fallbackBlobs = this.generateWatercolorBlobs(fallbackPoints, artStyle, canvasSize);
      return {
        points: fallbackPoints,
        blobs: fallbackBlobs,
        shapeCategory: 'Loop',
        distanceKm: 0.5,
        elevationGainMeters: 5,
        svgPreview: this.generateFullSvg(fallbackPoints, fallbackBlobs, artStyle, 'INK', 'Loop', []),
      };
    }

    // 1. Calculate distance and elevation
    let totalDist = 0;
    let elevationGain = 0;
    for (let i = 0; i < coords.length - 1; i++) {
      totalDist += this.haversineDistance(coords[i], coords[i + 1]);
      const elev1 = coords[i].altitude ?? 0;
      const elev2 = coords[i + 1].altitude ?? 0;
      if (elev2 > elev1) {
        elevationGain += (elev2 - elev1);
      }
    }

    // 2. Simplify GPS path with Ramer-Douglas-Peucker (epsilon tuned for aesthetic curves)
    const simplified = this.simplifyCoordinatesRDP(coords, 0.00003);

    // 3. Normalize into canvas dimensions [800 x 800] with slope and thickness multipliers
    const points = this.normalizeToCanvasSpace(simplified, canvasSize, padding);

    // 4. Classify shape archetype
    const shapeCategory = this.classifyShape(points);

    // 5. Generate organic watercolor color blobs
    const blobs = this.generateWatercolorBlobs(points, artStyle, canvasSize);

    // 6. Generate SVG preview string
    const svgPreview = this.generateFullSvg(points, blobs, artStyle, 'INK', shapeCategory, []);

    return {
      points,
      blobs,
      shapeCategory,
      distanceKm: Math.max(0.1, parseFloat(totalDist.toFixed(2))),
      elevationGainMeters: Math.round(elevationGain),
      svgPreview,
    };
  }

  /**
   * Ramer-Douglas-Peucker algorithm for path simplification
   */
  static simplifyCoordinatesRDP(coords: GpsCoordinate[], epsilon: number): GpsCoordinate[] {
    if (coords.length < 3) return coords;

    let maxDist = 0;
    let index = 0;
    const end = coords.length - 1;

    for (let i = 1; i < end; i++) {
      const dist = this.perpendicularDistance(coords[i], coords[0], coords[end]);
      if (dist > maxDist) {
        maxDist = dist;
        index = i;
      }
    }

    if (maxDist > epsilon) {
      const left = this.simplifyCoordinatesRDP(coords.slice(0, index + 1), epsilon);
      const right = this.simplifyCoordinatesRDP(coords.slice(index), epsilon);
      return [...left.slice(0, -1), ...right];
    } else {
      return [coords[0], coords[end]];
    }
  }

  private static perpendicularDistance(pt: GpsCoordinate, lineStart: GpsCoordinate, lineEnd: GpsCoordinate): number {
    const dx = lineEnd.lng - lineStart.lng;
    const dy = lineEnd.lat - lineStart.lat;
    const mag = Math.hypot(dx, dy);
    if (mag === 0) {
      return Math.hypot(pt.lng - lineStart.lng, pt.lat - lineStart.lat);
    }
    const num = Math.abs(dy * pt.lng - dx * pt.lat + lineEnd.lng * lineStart.lat - lineEnd.lat * lineStart.lng);
    return num / mag;
  }

  /**
   * Maps geographic coordinates to 800x800 canvas coordinate space
   */
  static normalizeToCanvasSpace(coords: GpsCoordinate[], canvasSize = 800, padding = 80): PointF[] {
    if (coords.length === 0) return [];

    let minLat = coords[0].lat;
    let maxLat = coords[0].lat;
    let minLng = coords[0].lng;
    let maxLng = coords[0].lng;

    coords.forEach(c => {
      if (c.lat < minLat) minLat = c.lat;
      if (c.lat > maxLat) maxLat = c.lat;
      if (c.lng < minLng) minLng = c.lng;
      if (c.lng > maxLng) maxLng = c.lng;
    });

    const latSpan = Math.max(0.0001, maxLat - minLat);
    const lngSpan = Math.max(0.0001, maxLng - minLng);
    const usableSize = canvasSize - padding * 2;

    // Aspect ratio preservation
    const scale = Math.min(usableSize / lngSpan, usableSize / latSpan);
    const offsetX = padding + (usableSize - lngSpan * scale) / 2;
    const offsetY = padding + (usableSize - latSpan * scale) / 2;

    const points: PointF[] = [];

    for (let i = 0; i < coords.length; i++) {
      const c = coords[i];
      const x = offsetX + (c.lng - minLng) * scale;
      // Invert Y because latitude increases northward while canvas Y increases downward
      const y = offsetY + (maxLat - c.lat) * scale;

      // Calculate grade and elevation slope
      let grade = 0;
      let thicknessMultiplier = 1.0;
      const elev = c.altitude ?? 0;
      const speed = c.speedMps ?? 1.2;

      if (i > 0) {
        const prevC = coords[i - 1];
        const distMeters = this.haversineDistance(prevC, c) * 1000;
        const elevDiff = (c.altitude ?? 0) - (prevC.altitude ?? 0);
        if (distMeters > 1) {
          grade = (elevDiff / distMeters) * 100;
          if (grade > 2) {
            // Uphill step: line swells organically with physical effort
            thicknessMultiplier = 1.0 + Math.min(2.0, grade * 0.25);
          } else if (grade < -2) {
            // Downhill step: swift lighter glide
            thicknessMultiplier = Math.max(0.7, 1.0 + grade * 0.05);
          }
        }
      }

      points.push({
        x: Math.round(x * 10) / 10,
        y: Math.round(y * 10) / 10,
        elevation: elev,
        gradePercentage: Math.round(grade * 10) / 10,
        strokeThicknessMultiplier: Math.round(thicknessMultiplier * 100) / 100,
        speedMps: speed,
      });
    }

    return points;
  }

  /**
   * Generates organic watercolor clusters around the walk's geometry
   */
  static generateWatercolorBlobs(points: PointF[], artStyle = 'Pastel Bloom', canvasSize = 800): ColorBlob[] {
    if (points.length === 0) return [];
    const palette = ART_PALETTES[artStyle] || ART_PALETTES['Pastel Bloom'];
    const blobs: ColorBlob[] = [];

    // Spatial clustering: sample points along the route
    const numBlobs = Math.min(10, Math.max(4, Math.floor(points.length / 2) + 2));
    const step = Math.max(1, Math.floor(points.length / numBlobs));

    let paletteIdx = 0;
    for (let i = 0; i < points.length; i += step) {
      const pt = points[i];
      const color = palette[paletteIdx % palette.length];
      paletteIdx++;

      // Controlled organic randomness around path nodes
      const angle = (i * 137.5) * (Math.PI / 180); // Golden ratio angle distribution
      const spread = 25 + (i % 3) * 15;
      const blobX = Math.min(canvasSize - 60, Math.max(60, pt.x + Math.cos(angle) * spread));
      const blobY = Math.min(canvasSize - 60, Math.max(60, pt.y + Math.sin(angle) * spread));

      const rx = 65 + (i % 4) * 20;
      const ry = 55 + ((i + 2) % 3) * 18;
      const rotation = (i * 45) % 360;

      blobs.push({
        id: `blob_${i}`,
        x: Math.round(blobX),
        y: Math.round(blobY),
        radiusX: Math.round(rx),
        radiusY: Math.round(ry),
        colorHex: color,
        opacity: 0.65,
        rotation: rotation,
        layer: i % 2,
      });
    }

    return blobs;
  }

  /**
   * Classifies geometric shape category from route topology
   */
  static classifyShape(points: PointF[]): string {
    if (points.length < 3) return 'Linear Path';

    const start = points[0];
    const end = points[points.length - 1];
    const closureDist = Math.hypot(end.x - start.x, end.y - start.y);

    // Calculate bounding box and centroid
    let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
    let sumX = 0, sumY = 0;
    points.forEach(p => {
      if (p.x < minX) minX = p.x;
      if (p.x > maxX) maxX = p.x;
      if (p.y < minY) minY = p.y;
      if (p.y > maxY) maxY = p.y;
      sumX += p.x;
      sumY += p.y;
    });

    const centroidX = sumX / points.length;
    const centroidY = sumY / points.length;
    const width = maxX - minX;
    const height = maxY - minY;
    const boundingDiag = Math.hypot(width, height);

    // Closure ratio
    const isClosedLoop = (closureDist / Math.max(1, boundingDiag)) < 0.28;

    // Radius variance from centroid
    let radiusSum = 0;
    const radii = points.map(p => {
      const r = Math.hypot(p.x - centroidX, p.y - centroidY);
      radiusSum += r;
      return r;
    });
    const avgRadius = radiusSum / points.length;
    const variance = radii.reduce((acc, r) => acc + Math.pow(r - avgRadius, 2), 0) / points.length;
    const stdDev = Math.sqrt(variance);
    const circularity = avgRadius > 0 ? (stdDev / avgRadius) : 1;

    if (isClosedLoop) {
      if (circularity < 0.32) return 'Orbital';
      if (circularity < 0.55) return 'Floral';
      return 'Loop';
    } else {
      const aspectRatio = width / Math.max(1, height);
      if (aspectRatio > 2.5 || aspectRatio < 0.4) return 'Linear Path';
      if (points.length > 8) return 'Ribbon';
      return 'Meander';
    }
  }

  /**
   * Catmull-Rom spline conversion to SVG path data (Cubic Bezier curves)
   */
  static generateSvgPathString(points: PointF[]): string {
    if (points.length === 0) return '';
    if (points.length === 1) return `M ${points[0].x} ${points[0].y}`;
    if (points.length === 2) return `M ${points[0].x} ${points[0].y} L ${points[1].x} ${points[1].y}`;

    let d = `M ${points[0].x.toFixed(1)} ${points[0].y.toFixed(1)}`;

    for (let i = 0; i < points.length - 1; i++) {
      const p0 = i > 0 ? points[i - 1] : points[i];
      const p1 = points[i];
      const p2 = points[i + 1];
      const p3 = i + 2 < points.length ? points[i + 2] : p2;

      const cp1x = p1.x + (p2.x - p0.x) / 6;
      const cp1y = p1.y + (p2.y - p0.y) / 6;
      const cp2x = p2.x - (p3.x - p1.x) / 6;
      const cp2y = p2.y - (p3.y - p1.y) / 6;

      d += ` C ${cp1x.toFixed(1)} ${cp1y.toFixed(1)}, ${cp2x.toFixed(1)} ${cp2y.toFixed(1)}, ${p2.x.toFixed(1)} ${p2.y.toFixed(1)}`;
    }

    return d;
  }

  /**
   * Generates a complete standalone SVG document with background blobs, path layers, and stickers
   */
  static generateFullSvg(
    points: PointF[],
    blobs: ColorBlob[],
    artStyle = 'Pastel Bloom',
    brushStyleKey: 'INK' | 'WATERCOLOR' | 'NEON' | 'CHALK' = 'INK',
    shapeCategory = 'Floral',
    stickers: LandmarkSticker[] = []
  ): string {
    const pathD = this.generateSvgPathString(points);

    // Blobs SVG definitions
    const gradientDefs = blobs.map((b, idx) => `
      <radialGradient id="blobGrad_${idx}" cx="50%" cy="50%" r="50%">
        <stop offset="0%" stop-color="${b.colorHex}" stop-opacity="${b.opacity}"/>
        <stop offset="70%" stop-color="${b.colorHex}" stop-opacity="${b.opacity * 0.4}"/>
        <stop offset="100%" stop-color="${b.colorHex}" stop-opacity="0"/>
      </radialGradient>
    `).join('\n');

    const blobsElements = blobs.map((b, idx) => `
      <ellipse cx="${b.x}" cy="${b.y}" rx="${b.radiusX}" ry="${b.radiusY}"
        fill="url(#blobGrad_${idx})"
        transform="rotate(${b.rotation} ${b.x} ${b.y})" />
    `).join('\n');

    // Stroke styling
    let strokeColor = '#16211B';
    let strokeWidth = 3.5;
    let filterDef = '';
    let filterAttr = '';
    let strokeDash = '';

    if (brushStyleKey === 'NEON') {
      strokeColor = '#00F5D4';
      strokeWidth = 4.0;
      filterDef = `
        <filter id="neonGlow" x="-20%" y="-20%" width="140%" height="140%">
          <feGaussianBlur stdDeviation="4" result="coloredBlur"/>
          <feMerge>
            <feMergeNode in="coloredBlur"/>
            <feMergeNode in="SourceGraphic"/>
          </feMerge>
        </filter>
      `;
      filterAttr = 'filter="url(#neonGlow)"';
    } else if (brushStyleKey === 'WATERCOLOR') {
      strokeColor = '#3D314A';
      strokeWidth = 4.2;
    } else if (brushStyleKey === 'CHALK') {
      strokeColor = '#435048';
      strokeWidth = 4.0;
      strokeDash = 'stroke-dasharray="16, 5"';
    }

    // Start/End indicators
    let endpointsSvg = '';
    if (points.length > 0) {
      const start = points[0];
      const end = points[points.length - 1];
      endpointsSvg = `
        <circle cx="${start.x}" cy="${start.y}" r="7" fill="#56B386" stroke="#ffffff" stroke-width="2"/>
        <circle cx="${end.x}" cy="${end.y}" r="6" fill="#866FB3" stroke="#ffffff" stroke-width="1.5"/>
      `;
    }

    // Stickers
    const stickersSvg = stickers.map(st => `
      <text x="${st.x}" y="${st.y}" font-size="28" text-anchor="middle" dominant-baseline="central">${st.iconEmoji}</text>
    `).join('\n');

    return `
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="100%" height="100%">
  <defs>
    ${gradientDefs}
    ${filterDef}
  </defs>
  <rect width="800" height="800" fill="#F7F5EE" rx="24"/>
  <g id="blobs_layer">
    ${blobsElements}
  </g>
  <g id="path_layer">
    <path d="${pathD}" fill="none" stroke="${strokeColor}" stroke-width="${strokeWidth}" stroke-linecap="round" stroke-linejoin="round" ${strokeDash} ${filterAttr}/>
  </g>
  <g id="endpoints_layer">
    ${endpointsSvg}
  </g>
  <g id="stickers_layer">
    ${stickersSvg}
  </g>
</svg>
    `.trim();
  }

  /**
   * Helper: Haversine distance in kilometers
   */
  static haversineDistance(c1: GpsCoordinate, c2: GpsCoordinate): number {
    const R = 6371; // Earth radius in km
    const dLat = ((c2.lat - c1.lat) * Math.PI) / 180;
    const dLng = ((c2.lng - c1.lng) * Math.PI) / 180;
    const lat1 = (c1.lat * Math.PI) / 180;
    const lat2 = (c2.lat * Math.PI) / 180;

    const a =
      Math.sin(dLat / 2) * Math.sin(dLat / 2) +
      Math.sin(dLng / 2) * Math.sin(dLng / 2) * Math.cos(lat1) * Math.cos(lat2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }

  /**
   * Generates a sample floral/loop path for initial artworks or empty states
   */
  static generateFallbackPath(canvasSize = 800): PointF[] {
    const points: PointF[] = [];
    const center = canvasSize / 2;
    const radius = 240;
    const totalPoints = 36;

    for (let i = 0; i <= totalPoints; i++) {
      const theta = (i / totalPoints) * Math.PI * 2;
      // Petal formula (r = a + b * sin(k * theta))
      const r = radius * (0.8 + 0.25 * Math.sin(3 * theta));
      const x = center + r * Math.cos(theta);
      const y = center + r * Math.sin(theta);
      const elev = 50 + 15 * Math.sin(2 * theta);
      const grade = Math.sin(3 * theta) * 4;

      points.push({
        x: Math.round(x),
        y: Math.round(y),
        elevation: elev,
        gradePercentage: grade,
        strokeThicknessMultiplier: grade > 1 ? 1.4 : 1.0,
        speedMps: 1.3,
      });
    }

    return points;
  }

  static jsonToPoints(jsonStr: string): PointF[] {
    try {
      if (!jsonStr) return [];
      return JSON.parse(jsonStr);
    } catch {
      return [];
    }
  }

  static jsonToBlobs(jsonStr: string): ColorBlob[] {
    try {
      if (!jsonStr) return [];
      return JSON.parse(jsonStr);
    } catch {
      return [];
    }
  }

  static jsonToStickers(jsonStr: string): LandmarkSticker[] {
    try {
      if (!jsonStr) return [];
      return JSON.parse(jsonStr);
    } catch {
      return [];
    }
  }
}
