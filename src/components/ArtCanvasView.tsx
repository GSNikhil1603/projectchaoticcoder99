import React, { useRef, useEffect } from 'react';
import { RouteArtEngine } from '../engine/routeArtEngine';
import { ColorBlob, PointF, LandmarkSticker } from '../types';

interface ArtCanvasViewProps {
  pointsJson: string;
  blobsJson: string;
  artStyle?: string;
  brushStyleKey?: 'INK' | 'WATERCOLOR' | 'NEON' | 'CHALK';
  stickersJson?: string;
  className?: string;
  isInteractive?: boolean;
  onBlobTapped?: (blob: ColorBlob, index: number) => void;
  onCanvasTapped?: (x: number, y: number) => void;
  customPaletteHex?: string;
}

export const ArtCanvasView: React.FC<ArtCanvasViewProps> = ({
  pointsJson,
  blobsJson,
  brushStyleKey = 'INK',
  stickersJson = '[]',
  className = 'w-full h-full',
  isInteractive = false,
  onBlobTapped,
  onCanvasTapped,
}) => {
  const canvasRef = useRef<HTMLCanvasElement | null>(null);

  const points: PointF[] = React.useMemo(() => RouteArtEngine.jsonToPoints(pointsJson), [pointsJson]);
  const blobs: ColorBlob[] = React.useMemo(() => RouteArtEngine.jsonToBlobs(blobsJson), [blobsJson]);
  const stickers: LandmarkSticker[] = React.useMemo(() => RouteArtEngine.jsonToStickers(stickersJson), [stickersJson]);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    const width = canvas.width;
    const height = canvas.height;
    const scaleX = width / 800;
    const scaleY = height / 800;

    // Clear canvas
    ctx.clearRect(0, 0, width, height);

    // 1. Draw Organic Watercolor Blobs
    blobs.forEach((blob) => {
      const cx = blob.x * scaleX;
      const cy = blob.y * scaleY;
      const rx = blob.radiusX * scaleX;
      const ry = blob.radiusY * scaleY;

      ctx.save();
      ctx.translate(cx, cy);
      ctx.rotate((blob.rotation * Math.PI) / 180);

      // Outer radial watercolor dispersion
      const maxR = Math.max(rx, ry) * 1.15;
      const grad = ctx.createRadialGradient(0, 0, maxR * 0.1, 0, 0, maxR);
      
      const alpha = brushStyleKey === 'NEON' ? 0.45 : brushStyleKey === 'WATERCOLOR' ? 0.75 : 0.65;
      
      // Parse hex to rgba
      grad.addColorStop(0, hexToRgba(blob.colorHex, alpha));
      grad.addColorStop(0.65, hexToRgba(blob.colorHex, alpha * 0.4));
      grad.addColorStop(1, hexToRgba(blob.colorHex, 0));

      ctx.fillStyle = grad;
      ctx.beginPath();
      ctx.ellipse(0, 0, rx, ry, 0, 0, Math.PI * 2);
      ctx.fill();

      // Inner concentrated pigment nucleus
      const coreGrad = ctx.createRadialGradient(0, 0, 0, 0, 0, maxR * 0.6);
      coreGrad.addColorStop(0, hexToRgba(blob.colorHex, alpha * 0.45));
      coreGrad.addColorStop(1, hexToRgba(blob.colorHex, 0));
      ctx.fillStyle = coreGrad;
      ctx.beginPath();
      ctx.ellipse(0, 0, rx * 0.65, ry * 0.65, 0, 0, Math.PI * 2);
      ctx.fill();

      ctx.restore();
    });

    // 2. Draw Smoothed Walking Art Path (Catmull-Rom to Cubic Bezier)
    if (points.length >= 2) {
      const baseStrokeWidth = (brushStyleKey === 'CHALK' ? 4.5 : brushStyleKey === 'NEON' ? 3.8 : brushStyleKey === 'WATERCOLOR' ? 3.6 : 3.2) * scaleX;

      for (let i = 0; i < points.length - 1; i++) {
        const p0 = i > 0 ? points[i - 1] : points[i];
        const p1 = points[i];
        const p2 = points[i + 1];
        const p3 = i + 2 < points.length ? points[i + 2] : p2;

        const sp0 = { x: p0.x * scaleX, y: p0.y * scaleY };
        const sp1 = { x: p1.x * scaleX, y: p1.y * scaleY };
        const sp2 = { x: p2.x * scaleX, y: p2.y * scaleY };
        const sp3 = { x: p3.x * scaleX, y: p3.y * scaleY };

        const localMultiplier = Math.max(0.75, Math.min(3.2, (p1.strokeThicknessMultiplier + p2.strokeThicknessMultiplier) / 2));
        const segStrokeWidth = baseStrokeWidth * localMultiplier;
        const grade = (p1.gradePercentage + p2.gradePercentage) / 2;

        const cp1x = sp1.x + (sp2.x - sp0.x) / 6;
        const cp1y = sp1.y + (sp2.y - sp0.y) / 6;
        const cp2x = sp2.x + (sp3.x - sp1.x) / 6;
        const cp2y = sp2.y + (sp3.y - sp1.y) / 6;

        // Incline Elevation Bloom (Uphill Glow)
        if (localMultiplier > 1.2 || grade >= 2.0) {
          ctx.save();
          ctx.beginPath();
          ctx.moveTo(sp1.x, sp1.y);
          ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, sp2.x, sp2.y);
          ctx.lineWidth = segStrokeWidth * 2.4;
          ctx.lineCap = 'round';
          ctx.lineJoin = 'round';
          ctx.strokeStyle = grade >= 5.0 ? 'rgba(255, 138, 101, 0.35)' : 'rgba(255, 213, 79, 0.30)';
          ctx.stroke();
          ctx.restore();
        }

        // Main Stroke by Brush Style
        ctx.save();
        ctx.beginPath();
        ctx.moveTo(sp1.x, sp1.y);
        ctx.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, sp2.x, sp2.y);
        ctx.lineCap = 'round';
        ctx.lineJoin = 'round';

        if (brushStyleKey === 'NEON') {
          // Glow pass
          ctx.shadowBlur = 12 * scaleX;
          ctx.shadowColor = '#00F5D4';
          ctx.lineWidth = segStrokeWidth * 1.5;
          ctx.strokeStyle = 'rgba(0, 245, 212, 0.6)';
          ctx.stroke();

          // Core pass
          ctx.lineWidth = segStrokeWidth;
          ctx.strokeStyle = grade >= 4.0 ? '#FF4081' : '#00F5D4';
          ctx.stroke();
        } else if (brushStyleKey === 'WATERCOLOR') {
          ctx.lineWidth = segStrokeWidth * 1.2;
          ctx.strokeStyle = grade >= 3.0 ? 'rgba(93, 64, 55, 0.85)' : 'rgba(61, 49, 74, 0.85)';
          ctx.stroke();
        } else if (brushStyleKey === 'CHALK') {
          ctx.setLineDash([16 * scaleX, 5 * scaleX]);
          ctx.lineWidth = segStrokeWidth;
          ctx.strokeStyle = grade >= 3.0 ? '#5D4037' : '#435048';
          ctx.stroke();
        } else {
          // Classic Ink Line
          ctx.lineWidth = segStrokeWidth;
          ctx.strokeStyle = grade >= 5.0 ? '#BF360C' : grade >= 2.0 ? '#1B382B' : '#1E2822';
          ctx.stroke();
        }
        ctx.restore();
      }
    }

    // 3. Start / End Landmarks & Sparkles
    if (points.length > 0) {
      const startPt = points[0];
      const endPt = points[points.length - 1];

      // Start Dot (Mint)
      ctx.save();
      ctx.fillStyle = '#56B386';
      ctx.beginPath();
      ctx.arc(startPt.x * scaleX, startPt.y * scaleY, 7 * scaleX, 0, Math.PI * 2);
      ctx.fill();
      ctx.fillStyle = '#FFFFFF';
      ctx.beginPath();
      ctx.arc(startPt.x * scaleX, startPt.y * scaleY, 3.5 * scaleX, 0, Math.PI * 2);
      ctx.fill();
      ctx.restore();

      // End Dot (Lavender)
      ctx.save();
      ctx.fillStyle = '#866FB3';
      ctx.beginPath();
      ctx.arc(endPt.x * scaleX, endPt.y * scaleY, 6 * scaleX, 0, Math.PI * 2);
      ctx.fill();
      ctx.fillStyle = '#FFFFFF';
      ctx.beginPath();
      ctx.arc(endPt.x * scaleX, endPt.y * scaleY, 2.5 * scaleX, 0, Math.PI * 2);
      ctx.fill();
      ctx.restore();

      // Sparkles
      if (points.length > 4) {
        const mid1 = points[Math.floor(points.length / 3)];
        drawSparkle(ctx, (mid1.x + 28) * scaleX, (mid1.y - 35) * scaleY, 7 * scaleX, '#76C893');
      }
      if (points.length > 8) {
        const mid2 = points[Math.floor((points.length * 2) / 3)];
        drawSparkle(ctx, (mid2.x - 32) * scaleX, (mid2.y + 26) * scaleY, 6 * scaleX, '#9F86C0');
      }
    }

    // 4. Draw Landmark Stickers
    stickers.forEach((sticker) => {
      ctx.save();
      ctx.font = `${26 * scaleX}px sans-serif`;
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(sticker.iconEmoji, sticker.x * scaleX, sticker.y * scaleY);
      ctx.restore();
    });
  }, [points, blobs, stickers, brushStyleKey]);

  const handleCanvasClick = (e: React.MouseEvent<HTMLCanvasElement>) => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    const rect = canvas.getBoundingClientRect();
    const clickX = e.clientX - rect.left;
    const clickY = e.clientY - rect.top;

    const scaleX = canvas.width / 800;
    const scaleY = canvas.height / 800;

    const normX = clickX / (rect.width / 800);
    const normY = clickY / (rect.height / 800);

    onCanvasTapped?.(normX, normY);

    if (onBlobTapped) {
      // Find tapped blob
      for (let i = blobs.length - 1; i >= 0; i--) {
        const b = blobs[i];
        const bx = b.x * scaleX;
        const by = b.y * scaleY;
        const rx = b.radiusX * scaleX;
        const ry = b.radiusY * scaleY;

        const normalizedDist = Math.hypot((clickX - bx) / rx, (clickY - by) / ry);
        if (normalizedDist <= 1.25) {
          onBlobTapped(b, i);
          return;
        }
      }
    }
  };

  return (
    <canvas
      ref={canvasRef}
      width={800}
      height={800}
      onClick={handleCanvasClick}
      className={`${className} ${isInteractive ? 'cursor-pointer' : ''}`}
    />
  );
};

function hexToRgba(hex: string, alpha = 1): string {
  try {
    let clean = hex.replace('#', '');
    if (clean.length === 3) {
      clean = clean.split('').map(c => c + c).join('');
    }
    const num = parseInt(clean, 16);
    const r = (num >> 16) & 255;
    const g = (num >> 8) & 255;
    const b = num & 255;
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
  } catch {
    return `rgba(86, 179, 134, ${alpha})`;
  }
}

function drawSparkle(ctx: CanvasRenderingContext2D, cx: number, cy: number, size: number, color: string) {
  ctx.save();
  ctx.fillStyle = color;
  ctx.beginPath();
  ctx.moveTo(cx, cy - size);
  ctx.lineTo(cx + size * 0.35, cy - size * 0.35);
  ctx.lineTo(cx + size, cy);
  ctx.lineTo(cx + size * 0.35, cy + size * 0.35);
  ctx.lineTo(cx, cy + size);
  ctx.lineTo(cx - size * 0.35, cy + size * 0.35);
  ctx.lineTo(cx - size, cy);
  ctx.lineTo(cx - size * 0.35, cy - size * 0.35);
  ctx.closePath();
  ctx.fill();
  ctx.restore();
}
