/**
 * Campus Route-to-Art — Core Client Application Engine
 * Implements Catmull-Rom to Cubic Bézier Splines, RDP Simplification,
 * Organic Watercolor Blobs, Live GPS Tracker & Simulator, Coloring Studio,
 * Campus Map, Quests, Store, and Local Storage Persistence.
 */

// ==========================================
// 1. ALGORITHMIC ROUTE-TO-ART ENGINE
// ==========================================
const RouteArtEngine = {
  // Curated Color Palettes
  PASTEL_PALETTES: [
    ["#A7D7C5", "#C8B6E2", "#FFD3B6", "#A8D8EA", "#FFAAA6"], // Spring Bloom
    ["#BCEAD5", "#D6C6EE", "#FFE3AA", "#B2DFDB", "#F8BBD0"], // Lavender Mint
    ["#C5E1A5", "#E1BEE7", "#FFE082", "#80DEEA", "#FFCCBC"], // Sunshine Meadow
    ["#80CBC4", "#B39DDB", "#FFAB91", "#90CAF9", "#FFF59D"], // Campus Pastel
    ["#264653", "#2A9D8F", "#E9C46A", "#F4A261", "#E76F51"], // Earthy Vibrant
    ["#00F5D4", "#7B2CBF", "#FF007F", "#FEE440", "#00BBF9"]  // Cyber Neon Grid
  ],

  // Ramer-Douglas-Peucker (RDP) Polyline Simplification
  simplifyPoints(points, epsilon = 4.0) {
    if (!points || points.length < 3) return points || [];
    let maxDistance = 0;
    let index = 0;
    const start = points[0];
    const end = points[points.length - 1];

    for (let i = 1; i < points.length - 1; i++) {
      const dist = this._perpendicularDistance(points[i], start, end);
      if (dist > maxDistance) {
        maxDistance = dist;
        index = i;
      }
    }

    if (maxDistance > epsilon) {
      const left = this.simplifyPoints(points.slice(0, index + 1), epsilon);
      const right = this.simplifyPoints(points.slice(index), epsilon);
      return left.slice(0, -1).concat(right);
    } else {
      return [start, end];
    }
  },

  _perpendicularDistance(pt, lineStart, lineEnd) {
    const dx = lineEnd.x - lineStart.x;
    const dy = lineEnd.y - lineStart.y;
    const mag = Math.hypot(dx, dy);
    if (mag === 0) return Math.hypot(pt.x - lineStart.x, pt.y - lineStart.y);
    const u = Math.max(0, Math.min(1, ((pt.x - lineStart.x) * dx + (pt.y - lineStart.y) * dy) / (mag * mag)));
    const ix = lineStart.x + u * dx;
    const iy = lineStart.y + u * dy;
    return Math.hypot(pt.x - ix, pt.y - iy);
  },

  // Normalize points into target coordinate box maintaining aspect ratio
  normalizePoints(rawPoints, targetSize = 800, padding = 100) {
    if (!rawPoints || rawPoints.length === 0) return [];
    const minX = Math.min(...rawPoints.map(p => p.x));
    const maxX = Math.max(...rawPoints.map(p => p.x));
    const minY = Math.min(...rawPoints.map(p => p.y));
    const maxY = Math.max(...rawPoints.map(p => p.y));

    const width = Math.max(1, maxX - minX);
    const height = Math.max(1, maxY - minY);
    const maxDim = Math.max(width, height);
    const scale = (targetSize - padding * 2) / maxDim;

    const centerX = (minX + maxX) / 2;
    const centerY = (minY + maxY) / 2;
    const targetCenterX = targetSize / 2;
    const targetCenterY = targetSize / 2;

    return rawPoints.map(pt => ({
      x: targetCenterX + (pt.x - centerX) * scale,
      y: targetCenterY + (pt.y - centerY) * scale,
      alt: pt.alt || 184.0,
      vDisp: pt.vDisp || 0.0,
      thick: pt.thick || 1.0,
      grade: pt.grade || 0.0
    }));
  },

  // Generate Organic Watercolor Blobs along route turning clusters
  generateColorBlobs(points, palette = null) {
    if (!points || points.length === 0) return [];
    const pal = palette || this.PASTEL_PALETTES[0];
    const blobs = [];
    const count = Math.min(6, Math.max(3, Math.floor(points.length / 4)));
    const step = Math.floor(points.length / count) || 1;

    for (let i = 0; i < count; i++) {
      const ptIndex = Math.min(points.length - 1, i * step + Math.floor(step / 2));
      const pt = points[ptIndex];
      const radiusX = 85 + ((i * 37) % 50);
      const radiusY = 70 + ((i * 53) % 60);
      const rotation = (i * 45) % 180;
      const colorHex = pal[i % pal.length];

      blobs.push({
        id: i,
        x: pt.x + (i % 2 === 0 ? 25 : -25),
        y: pt.y + (i % 3 === 0 ? -20 : 30),
        radiusX: radiusX,
        radiusY: radiusY,
        rotation: rotation,
        colorHex: colorHex,
        label: `Zone ${i + 1}`
      });
    }
    return blobs;
  },

  // Convert points to smooth Catmull-Rom spline cubic Bézier SVG path
  generateSvgPathString(points) {
    if (!points || points.length === 0) return "";
    if (points.length === 1) return `M ${points[0].x.toFixed(2)} ${points[0].y.toFixed(2)}`;

    let path = `M ${points[0].x.toFixed(2)} ${points[0].y.toFixed(2)}`;
    for (let i = 0; i < points.length - 1; i++) {
      const p0 = i > 0 ? points[i - 1] : points[i];
      const p1 = points[i];
      const p2 = points[i + 1];
      const p3 = i + 2 < points.length ? points[i + 2] : p2;

      // Catmull-Rom to Cubic Bézier control points
      const cp1x = p1.x + (p2.x - p0.x) / 6;
      const cp1y = p1.y + (p2.y - p0.y) / 6;
      const cp2x = p2.x - (p3.x - p1.x) / 6;
      const cp2y = p2.y - (p3.y - p1.y) / 6;

      path += ` C ${cp1x.toFixed(2)} ${cp1y.toFixed(2)}, ${cp2x.toFixed(2)} ${cp2y.toFixed(2)}, ${p2.x.toFixed(2)} ${p2.y.toFixed(2)}`;
    }
    return path;
  },

  // Classify shape geometry into creative motif titles
  classifyShape(points, distanceKm = 5.0) {
    if (!points || points.length < 5) return { title: "Simple Stride", category: "Minimal" };
    const minX = Math.min(...points.map(p => p.x));
    const maxX = Math.max(...points.map(p => p.x));
    const minY = Math.min(...points.map(p => p.y));
    const maxY = Math.max(...points.map(p => p.y));

    const ratio = (maxX - minX) / Math.max(1, maxY - minY);
    let loops = 0;
    for (let i = 0; i < points.length - 6; i += 3) {
      const p1 = points[i];
      for (let j = i + 5; j < points.length; j += 2) {
        const p2 = points[j];
        if (Math.hypot(p1.x - p2.x, p1.y - p2.y) < 60) {
          loops++;
          break;
        }
      }
    }

    if (loops >= 3) return { title: "Campus Flora & Bloom", category: "Floral" };
    if (loops === 2) return { title: "Cosmic Butterfly", category: "Fauna" };
    if (ratio > 1.8) return { title: "Infinity Horizon", category: "Ribbon" };
    if (ratio < 0.6) return { title: "Botanical Stem", category: "Floral" };
    if (distanceKm > 5.0) return { title: "Grand Campus Odyssey", category: "Abstract" };
    return { title: "Whimsical Melody", category: "Abstract" };
  },

  // Generates math-based organic sample walks
  generateSampleWalk(seedType) {
    const points = [];
    const count = 28;
    const center = 400;

    switch (seedType % 6) {
      case 0: // Fluid organic bloom loop
        for (let i = 0; i <= count; i++) {
          const t = (i / count) * 2 * Math.PI;
          const r = 180 + 70 * Math.sin(3 * t) + 40 * Math.cos(2 * t);
          const x = center + r * Math.cos(t) + 30 * Math.sin(5 * t);
          const y = center + r * Math.sin(t) + 20 * Math.cos(4 * t);
          points.push({ x, y });
        }
        break;
      case 1: // Butterfly motif
        for (let i = 0; i <= count; i++) {
          const t = (i / count) * 2 * Math.PI;
          const r = 160 * (Math.pow(Math.sin(t), 2) + Math.abs(Math.cos(2 * t)));
          const x = center + r * Math.cos(t) * 1.2;
          const y = center + r * Math.sin(t);
          points.push({ x, y });
        }
        break;
      case 2: // Wavy serpentine ribbon
        for (let i = 0; i <= count; i++) {
          const progress = i / count;
          const x = 200 + progress * 400 + 40 * Math.sin(progress * 4 * Math.PI);
          const y = 250 + 300 * Math.sin(progress * 2.5 * Math.PI) + 40 * Math.cos(progress * 5 * Math.PI);
          points.push({ x, y });
        }
        break;
      case 3: // Botanical Leaf
        for (let i = 0; i <= count; i++) {
          const t = (i / count) * 2 * Math.PI;
          const r = 200 * Math.abs(Math.sin(t));
          const x = center + r * Math.cos(t) * 0.8 + 20 * Math.sin(3 * t);
          const y = center + r * Math.sin(t) * 1.3;
          points.push({ x, y });
        }
        break;
      case 4: // Abstract Quad Loop
        for (let i = 0; i <= count; i++) {
          const t = (i / count) * 2 * Math.PI;
          const r = 150 + 80 * Math.cos(4 * t);
          const x = center + r * Math.cos(t);
          const y = center + r * Math.sin(t);
          points.push({ x, y });
        }
        break;
      default: // Campus Stride
        for (let i = 0; i <= count; i++) {
          const t = (i / count) * 2 * Math.PI;
          const r = 170 + 50 * Math.sin(5 * t);
          const x = center + r * Math.cos(t);
          const y = center + r * Math.sin(t);
          points.push({ x, y });
        }
        break;
    }

    const normalized = this.normalizePoints(points, 800, 100);
    const palette = this.PASTEL_PALETTES[seedType % this.PASTEL_PALETTES.length];
    const blobs = this.generateColorBlobs(normalized, palette);
    return { points: normalized, blobs };
  }
};

// ==========================================
// 2. SAMPLE INITIAL DATA & PERSISTENCE
// ==========================================
const SampleCampusData = {
  getInitialRoutes() {
    const list = [];
    const titles = [
      { date: "23 Feb 2024", title: "Today's Campus Bloom", shape: "Whimsical Bloom", cat: "Floral", km: 5.6, steps: 7842, cal: 312, seed: 0 },
      { date: "22 Feb 2024", title: "Hostel to Library Stride", shape: "Cosmic Butterfly", cat: "Fauna", km: 6.4, steps: 9120, cal: 360, seed: 1 },
      { date: "21 Feb 2024", title: "Sports Complex Loop", shape: "Infinity Ribbon", cat: "Ribbon", km: 4.8, steps: 6450, cal: 270, seed: 2 },
      { date: "20 Feb 2024", title: "Botanical Green Path", shape: "Leaf of Knowledge", cat: "Floral", km: 5.9, steps: 8300, cal: 330, seed: 3 },
      { date: "19 Feb 2024", title: "Evening Canteen Run", shape: "Amber Loop", cat: "Abstract", km: 3.9, steps: 5200, cal: 210, seed: 4 },
      { date: "18 Feb 2024", title: "Hostel Ring Road Trek", shape: "Emerald Tri-Star", cat: "Geometric", km: 5.1, steps: 7100, cal: 285, seed: 5 },
      { date: "17 Feb 2024", title: "Classroom Switch Stride", shape: "Pink Coral Blossom", cat: "Floral", km: 4.9, steps: 6800, cal: 265, seed: 0 },
      { date: "16 Feb 2024", title: "Perimeter Campus Trek", shape: "Azure Cloud Ribbon", cat: "Ribbon", km: 6.2, steps: 8900, cal: 345, seed: 2 }
    ];

    titles.forEach((item, idx) => {
      const { points, blobs } = RouteArtEngine.generateSampleWalk(item.seed);
      list.push({
        id: idx + 1,
        dateString: item.date,
        title: item.title,
        shapeName: item.shape,
        shapeCategory: item.cat,
        distanceKm: item.km,
        steps: item.steps,
        calories: item.cal,
        points: points,
        blobs: blobs,
        brushStyle: "INK",
        paletteIndex: item.seed % RouteArtEngine.PASTEL_PALETTES.length,
        isFavorite: idx === 1 || idx === 3 || idx === 5
      });
    });
    return list;
  },

  getInitialQuests() {
    return [
      { id: "q1", title: "Campus 8,000 Step Stride", desc: "Walk at least 8,000 steps today across campus lecture halls.", cat: "Daily", target: 8000, current: 7842, reward: 100, icon: "👟", completed: false, claimed: false },
      { id: "q2", title: "Closed Loop Artist", desc: "Walk a full loop that connects back to create a closed artwork zone.", cat: "Daily", target: 1, current: 1, reward: 150, icon: "🎨", completed: true, claimed: false },
      { id: "q3", title: "Hostel Derby: 25 km", desc: "Accumulate 25 km of campus walking to boost your block's ranking.", cat: "Weekly", target: 25, current: 18.5, reward: 350, icon: "🏆", completed: false, claimed: false },
      { id: "q4", title: "Campus Pioneer", desc: "Visit 4 distinct campus zones (Library, SJT, Food Court, Lake).", cat: "Special", target: 4, current: 4, reward: 250, icon: "🗺️", completed: true, claimed: true }
    ];
  },

  getInitialLeaderboard() {
    return [
      { rank: 1, name: "Aarav Sharma", avatar: "🥇", km: "64.2 km", crystals: "2,450 💎", isUser: false },
      { rank: 2, name: "Priya Patel", avatar: "🥈", km: "58.7 km", crystals: "2,100 💎", isUser: false },
      { rank: 3, name: "Rohan Varma", avatar: "🥉", km: "52.4 km", crystals: "1,890 💎", isUser: false },
      { rank: 4, name: "Neeraj (You)", avatar: "👨‍🎓", km: "48.2 km", crystals: "850 💎", isUser: true },
      { rank: 5, name: "Sneha Reddy", avatar: "👟", km: "45.0 km", crystals: "720 💎", isUser: false }
    ];
  },

  getInitialBadges() {
    return [
      { id: "b1", name: "First Masterpiece", rarity: "Common", emoji: "🖼️", desc: "Minted your first daily walk into artwork.", unlocked: true },
      { id: "b2", name: "Color Maestro", rarity: "Rare", emoji: "🎨", desc: "Customized and saved 5 walk artworks in Studio.", unlocked: true },
      { id: "b3", name: "100k Campus Titan", rarity: "Epic", emoji: "⚡", desc: "Surpassed 100,000 lifetime campus steps.", unlocked: true },
      { id: "b4", name: "Nocturnal Stargazer", rarity: "Rare", emoji: "🌙", desc: "Completed an art walk after 9:00 PM.", unlocked: false },
      { id: "b5", name: "Infinity Loop Master", rarity: "Legendary", emoji: "🌸", desc: "Generated a multi-petal floral geometry.", unlocked: false },
      { id: "b6", name: "Speed Strider", rarity: "Epic", emoji: "🚀", desc: "Maintained a pace under 5:30 min/km for 5 km.", unlocked: false }
    ];
  },

  getInitialStoreItems() {
    return [
      { id: "s1", name: "Fine Ink Pen", cat: "BRUSH", desc: "Crisp architectural line with smooth tapering.", cost: 0, preview: "✒️", style: "INK", unlocked: true },
      { id: "s2", name: "Cyber Neon Pulse", cat: "BRUSH", desc: "Glowing cyan-violet stroke for night routes.", cost: 0, preview: "⚡", style: "NEON", unlocked: true },
      { id: "s3", name: "Aquarelle Flow", cat: "BRUSH", desc: "Soft watercolor stroke that bleeds gently.", cost: 300, preview: "🎨", style: "WATERCOLOR", unlocked: false },
      { id: "s4", name: "Campus Chalk", cat: "BRUSH", desc: "Textured graffiti chalk outline.", cost: 200, preview: "🖍️", style: "CHALK", unlocked: false },
      { id: "s5", name: "Pastel Bloom Palette", cat: "PALETTE", desc: "Mints, soft lavenders, and honey peach.", cost: 0, preview: "🌸", style: "0", unlocked: true },
      { id: "s6", name: "Sunset Bloom Palette", cat: "PALETTE", desc: "Warm oranges into rosy sunset pink.", cost: 320, preview: "🌅", style: "4", unlocked: false },
      { id: "s7", name: "Cyberpunk Neon Grid", cat: "PALETTE", desc: "Electric cyan, magenta, and solar yellow.", cost: 450, preview: "👾", style: "5", unlocked: false },
      { id: "s8", name: "Watercolor Bleed Filter", cat: "EFFECT", desc: "Organic Gaussian diffusion on pastel zones.", cost: 250, preview: "💧", style: "BLUR", unlocked: false }
    ];
  },

  getCampusLandmarks() {
    return [
      { id: "lm1", name: "Silver Jubilee Tower (SJT)", icon: "🏫", x: 220, y: 160, desc: "Main academic complex with wide lecture halls.", steps: "3,420 steps today", walks: "4 walks" },
      { id: "lm2", name: "Technology Tower (TT)", icon: "🏢", x: 500, y: 180, desc: "High-tech computing laboratories & faculty blocks.", steps: "2,190 steps today", walks: "3 walks" },
      { id: "lm3", name: "Central Library", icon: "📚", x: 360, y: 240, desc: "Quiet study zones and sprawling botanical lawn.", steps: "4,100 steps today", walks: "6 walks" },
      { id: "lm4", name: "Campus Food Court", icon: "☕", x: 220, y: 380, desc: "Student hub for coffee, snacks, and evening hangouts.", steps: "5,800 steps today", walks: "8 walks" },
      { id: "lm5", name: "Central Lake & Gazebo", icon: "🏞️", x: 610, y: 470, desc: "Scenic paved jogging track surrounding the water.", steps: "1,850 steps today", walks: "5 walks" },
      { id: "lm6", name: "Outdoor Sports Arena", icon: "⚽", x: 380, y: 460, desc: "Synthetic running track and football grounds.", steps: "2,940 steps today", walks: "2 walks" }
    ];
  }
};

// ==========================================
// 3. MAIN APPLICATION CONTROLLER
// ==========================================
class CampusRouteArtApp {
  constructor() {
    this.routes = this.loadStoredData("cra_routes", SampleCampusData.getInitialRoutes());
    this.quests = this.loadStoredData("cra_quests", SampleCampusData.getInitialQuests());
    this.storeItems = this.loadStoredData("cra_store", SampleCampusData.getInitialStoreItems());
    this.badges = this.loadStoredData("cra_badges", SampleCampusData.getInitialBadges());
    this.leaderboard = SampleCampusData.getInitialLeaderboard();
    this.landmarks = SampleCampusData.getCampusLandmarks();
    this.crystals = parseInt(localStorage.getItem("cra_crystals") || "850", 10);
    this.streak = parseInt(localStorage.getItem("cra_streak") || "7", 10);

    // Active Studio State
    this.activeStudioRoute = this.routes[0];
    this.selectedBrushStyle = this.activeStudioRoute.brushStyle || "INK";
    this.selectedPaletteIndex = this.activeStudioRoute.paletteIndex || 0;
    this.selectedColorHex = RouteArtEngine.PASTEL_PALETTES[this.selectedPaletteIndex][0];

    // Tracker & Simulation State
    this.isTracking = false;
    this.trackingTimer = null;
    this.activePreset = "lake-jog";
    this.simulatedPath = [];
    this.liveDistance = 0.0;
    this.liveSteps = 0;
    this.liveSeconds = 0;
    this.liveCalories = 0;
    this.watchGpsId = null;

    // Cheer quotes
    this.cheerQuotes = [
      ["You turned every step into a", "small work of art."],
      ["Every stride is a stroke of", "pure campus creativity."],
      ["The campus is your canvas,", "and your feet are the brush."],
      ["Walking turns daily motion into", "timeless living art."],
      ["You mapped your morning in", "vibrant pastel shades."]
    ];
    this.cheerIndex = 0;

    this.initDOM();
    this.bindEvents();
    this.renderAll();
  }

  loadStoredData(key, fallback) {
    try {
      const stored = localStorage.getItem(key);
      return stored ? JSON.parse(stored) : fallback;
    } catch {
      return fallback;
    }
  }

  saveData(key, value) {
    try {
      localStorage.setItem(key, JSON.stringify(value));
    } catch (e) {
      console.warn("Storage write failed", e);
    }
  }

  initDOM() {
    this.views = document.querySelectorAll(".app-view");
    this.navButtons = document.querySelectorAll(".nav-item");
    this.crystalDisplay = document.getElementById("crystal-display");
    this.streakDisplay = document.getElementById("streak-display");
    this.storeWalletVal = document.getElementById("store-wallet-val");

    // Modal
    this.modal = document.getElementById("general-modal");
    this.modalTitle = document.getElementById("modal-title");
    this.modalBody = document.getElementById("modal-body");
    this.modalCloseBtn = document.getElementById("modal-close-btn");
    this.modalActionBtn = document.getElementById("modal-action-btn");
    this.toastContainer = document.getElementById("toast-container");
  }

  bindEvents() {
    // Navigation Tabs
    this.navButtons.forEach(btn => {
      btn.addEventListener("click", () => {
        const targetViewId = btn.getAttribute("data-target-view");
        this.switchView(targetViewId);
      });
    });

    // Logo & Header clicks
    document.getElementById("btn-logo-home").addEventListener("click", () => this.switchView("view-home"));
    document.getElementById("btn-open-profile").addEventListener("click", () => this.switchView("view-profile"));
    document.getElementById("btn-crystal-balance").addEventListener("click", () => this.switchView("view-store"));
    document.getElementById("btn-quick-walk").addEventListener("click", () => this.switchView("view-tracker"));
    document.getElementById("card-hero-today").addEventListener("click", () => {
      this.openInStudio(this.routes[0]);
    });

    // Cheer Card
    document.getElementById("btn-new-cheer").addEventListener("click", () => {
      this.cheerIndex = (this.cheerIndex + 1) % this.cheerQuotes.length;
      this.renderCheerCard();
    });

    document.getElementById("btn-inspire-me").addEventListener("click", () => {
      const tips = [
        "✨ You've walked 7,842 steps today — creative energy flows through every path!",
        "🎨 Tip: Try taking the Lake loop to unlock the Cosmic Butterfly geometry!",
        "🌿 2,158 steps remaining to reach your 10,000 daily campus goal.",
        "🔥 You're on a 7-day streak! Your campus map is glowing with color."
      ];
      const randomTip = tips[Math.floor(Math.random() * tips.length)];
      this.showModal("✨ Campus Daily Inspiration", `<p style="font-size:15px;line-height:1.6;color:#374151;">${randomTip}</p>`);
    });

    // Gallery button on home
    document.getElementById("btn-view-all-gallery").addEventListener("click", () => this.switchView("view-profile"));

    // Modal close
    this.modalCloseBtn.addEventListener("click", () => this.closeModal());
    this.modalActionBtn.addEventListener("click", () => this.closeModal());
    this.modal.addEventListener("click", (e) => {
      if (e.target === this.modal) this.closeModal();
    });

    // Coloring Studio Events
    this.bindStudioEvents();

    // Tracker & Simulation Events
    this.bindTrackerEvents();

    // Map Events
    this.bindMapEvents();

    // Quests Events
    this.bindQuestEvents();

    // Store Events
    this.bindStoreEvents();

    // Profile & Gallery Filter Events
    this.bindProfileEvents();
  }

  switchView(viewId) {
    this.views.forEach(v => v.classList.remove("active"));
    const targetView = document.getElementById(viewId);
    if (targetView) targetView.classList.add("active");

    this.navButtons.forEach(btn => {
      if (btn.getAttribute("data-target-view") === viewId) {
        btn.classList.add("active");
      } else {
        btn.classList.remove("active");
      }
    });

    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  showToast(message, emoji = "✨") {
    const toast = document.createElement("div");
    toast.className = "toast-message";
    toast.innerHTML = `<span>${emoji}</span><span>${message}</span>`;
    this.toastContainer.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = "0";
      setTimeout(() => toast.remove(), 300);
    }, 2500);
  }

  showModal(title, bodyHtml) {
    this.modalTitle.innerHTML = title;
    this.modalBody.innerHTML = bodyHtml;
    this.modal.classList.add("active");
  }

  closeModal() {
    this.modal.classList.remove("active");
  }

  renderAll() {
    this.updateCrystalDisplays();
    this.renderCheerCard();
    this.renderHomeHero();
    this.renderHomeCarousel();
    this.renderStudioView();
    this.renderCampusMap();
    this.renderQuests("ALL");
    this.renderLeaderboard();
    this.renderStore("BRUSH");
    this.renderProfile();
    this.initTrackerCanvases();
  }

  updateCrystalDisplays() {
    this.crystalDisplay.textContent = this.crystals;
    this.streakDisplay.textContent = `${this.streak}d`;
    if (this.storeWalletVal) this.storeWalletVal.textContent = this.crystals;
    localStorage.setItem("cra_crystals", this.crystals.toString());
  }

  renderCheerCard() {
    const quote = this.cheerQuotes[this.cheerIndex % this.cheerQuotes.length];
    document.getElementById("cheer-text-1").textContent = quote[0];
    document.getElementById("cheer-text-2").textContent = quote[1];
  }

  renderHomeHero() {
    const today = this.routes[0];
    if (!today) return;
    document.getElementById("hero-step-count").textContent = today.steps.toLocaleString();
    document.getElementById("hero-distance").textContent = today.distanceKm.toFixed(1);
    document.getElementById("hero-calories").textContent = today.calories;
    const goalPct = Math.min(100, Math.round((today.steps / 10000) * 100));
    document.getElementById("hero-goal-pct").textContent = `${goalPct}% of daily goal`;
    document.getElementById("hero-goal-fill").style.width = `${goalPct}%`;
  }

  renderHomeCarousel() {
    const container = document.getElementById("home-artworks-carousel");
    container.innerHTML = "";

    this.routes.forEach(route => {
      const card = document.createElement("div");
      card.className = "artwork-card-mini";
      card.innerHTML = `
        <div class="mini-canvas-thumb">
          <svg viewBox="0 0 500 500">${this.buildSvgInner(route.points, route.blobs, route.brushStyle)}</svg>
        </div>
        <div class="mini-card-details">
          <span class="mini-card-title">${route.shapeName}</span>
          <span class="mini-card-date">${route.dateString}</span>
          <div class="mini-card-stats">
            <span>${route.distanceKm} km</span>
            <span>${route.steps.toLocaleString()} steps</span>
          </div>
        </div>
      `;
      card.addEventListener("click", () => this.openInStudio(route));
      container.appendChild(card);
    });
  }

  // ==========================================
  // 4. COLORING STUDIO LOGIC
  // ==========================================
  bindStudioEvents() {
    // Palette theme tabs
    const themeTabs = document.querySelectorAll("#palette-theme-tabs .tab-btn");
    themeTabs.forEach(tab => {
      tab.addEventListener("click", () => {
        themeTabs.forEach(t => t.classList.remove("active"));
        tab.classList.add("active");
        this.selectedPaletteIndex = parseInt(tab.getAttribute("data-palette-index"), 10);
        this.renderActiveSwatches();
      });
    });

    // Brush selectors
    const brushCards = document.querySelectorAll(".brush-card");
    brushCards.forEach(card => {
      card.addEventListener("click", () => {
        brushCards.forEach(c => c.classList.remove("active"));
        card.classList.add("active");
        this.selectedBrushStyle = card.getAttribute("data-brush");
        this.activeStudioRoute.brushStyle = this.selectedBrushStyle;
        this.renderStudioCanvas();
      });
    });

    // Save changes button
    document.getElementById("btn-studio-save").addEventListener("click", () => {
      this.saveData("cra_routes", this.routes);
      this.showToast("Artwork customized & saved to collection!", "🎨");
      this.renderHomeCarousel();
      this.renderProfile();
    });

    // Export Story Card
    document.getElementById("btn-studio-share").addEventListener("click", () => this.exportStoryCard());
    document.getElementById("btn-export-story-card").addEventListener("click", () => this.exportStoryCard());
    document.getElementById("btn-export-png").addEventListener("click", () => this.exportHighResPng());
    document.getElementById("btn-export-svg").addEventListener("click", () => this.exportSvgFile());
  }

  openInStudio(route) {
    this.activeStudioRoute = route;
    this.selectedBrushStyle = route.brushStyle || "INK";
    this.selectedPaletteIndex = route.paletteIndex || 0;
    this.switchView("view-studio");
    this.renderStudioView();
  }

  renderStudioView() {
    const route = this.activeStudioRoute;
    if (!route) return;

    document.getElementById("studio-artwork-title").textContent = route.shapeName;
    document.getElementById("studio-artwork-meta").textContent = `${route.dateString} • ${route.distanceKm} km • ${route.steps.toLocaleString()} steps`;

    // Highlight active brush
    document.querySelectorAll(".brush-card").forEach(c => {
      c.classList.toggle("active", c.getAttribute("data-brush") === this.selectedBrushStyle);
    });

    // Highlight palette tab
    document.querySelectorAll("#palette-theme-tabs .tab-btn").forEach(t => {
      t.classList.toggle("active", parseInt(t.getAttribute("data-palette-index"), 10) === this.selectedPaletteIndex);
    });

    this.renderActiveSwatches();
    this.renderStudioCanvas();
  }

  renderActiveSwatches() {
    const container = document.getElementById("active-palette-swatches");
    container.innerHTML = "";
    const palette = RouteArtEngine.PASTEL_PALETTES[this.selectedPaletteIndex % RouteArtEngine.PASTEL_PALETTES.length];

    palette.forEach((hex, idx) => {
      const swatch = document.createElement("button");
      swatch.className = `color-swatch-btn ${idx === 0 ? "active" : ""}`;
      swatch.style.backgroundColor = hex;
      swatch.setAttribute("data-color", hex);

      swatch.addEventListener("click", () => {
        document.querySelectorAll(".color-swatch-btn").forEach(s => s.classList.remove("active"));
        swatch.classList.add("active");
        this.selectedColorHex = hex;
      });

      container.appendChild(swatch);
    });

    this.selectedColorHex = palette[0];
  }

  renderStudioCanvas() {
    const svgElement = document.getElementById("studio-interactive-svg");
    const route = this.activeStudioRoute;
    if (!svgElement || !route) return;

    svgElement.innerHTML = this.buildSvgInner(route.points, route.blobs, this.selectedBrushStyle, true);

    // Attach click listeners to blob zones for interactive coloring
    const blobElements = svgElement.querySelectorAll(".interactive-blob");
    blobElements.forEach(blobEl => {
      blobEl.addEventListener("click", (e) => {
        e.stopPropagation();
        const blobId = parseInt(blobEl.getAttribute("data-blob-id"), 10);
        const targetBlob = route.blobs.find(b => b.id === blobId);
        if (targetBlob) {
          targetBlob.colorHex = this.selectedColorHex;
          blobEl.setAttribute("fill", this.selectedColorHex);
          this.showToast(`Zone colored with ${this.selectedColorHex}!`, "🎨");
        }
      });
    });
  }

  buildSvgInner(points, blobs, brushStyle = "INK", isInteractive = false) {
    const pathString = RouteArtEngine.generateSvgPathString(points);

    // Blobs
    const blobSvg = (blobs || []).map(b => {
      const scale = 500 / 800;
      const bx = (b.x * scale).toFixed(1);
      const by = (b.y * scale).toFixed(1);
      const brx = (b.radiusX * scale).toFixed(1);
      const bry = (b.radiusY * scale).toFixed(1);
      return `<ellipse class="${isInteractive ? 'interactive-blob' : ''}" data-blob-id="${b.id}" cx="${bx}" cy="${by}" rx="${brx}" ry="${bry}" fill="${b.colorHex}" fill-opacity="0.65" transform="rotate(${b.rotation} ${bx} ${by})" style="cursor:${isInteractive ? 'pointer' : 'default'};transition:fill 0.3s ease;" />`;
    }).join("");

    // Brush Stroke definitions
    let strokeColor = "#334155";
    let strokeWidth = 5.0;
    let extraFilter = "";
    let filterDef = "";

    if (brushStyle === "NEON") {
      strokeColor = "#00F5D4";
      strokeWidth = 4.5;
      extraFilter = 'filter="url(#neonGlow)"';
      filterDef = `
        <filter id="neonGlow" x="-20%" y="-20%" width="140%" height="140%">
          <feGaussianBlur stdDeviation="4" result="blur" />
          <feComposite in="SourceGraphic" in2="blur" operator="over" />
        </filter>
      `;
    } else if (brushStyle === "WATERCOLOR") {
      strokeColor = "#A78BFA";
      strokeWidth = 7.0;
    } else if (brushStyle === "CHALK") {
      strokeColor = "#F59E0B";
      strokeWidth = 6.0;
    }

    return `
      <defs>
        ${filterDef}
        <linearGradient id="neonGradient" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stop-color="#00F5D4" />
          <stop offset="100%" stop-color="#7B2CBF" />
        </linearGradient>
      </defs>
      <rect width="100%" height="100%" fill="#0D1117" rx="20" />
      <g id="watercolor-blobs-layer">${blobSvg}</g>
      <g id="route-line-layer">
        ${brushStyle === "NEON" ? `<path d="${pathString}" fill="none" stroke="#7B2CBF" stroke-width="8" stroke-linecap="round" opacity="0.6" ${extraFilter} />` : ""}
        <path d="${pathString}" fill="none" stroke="${brushStyle === 'NEON' ? 'url(#neonGradient)' : strokeColor}" stroke-width="${strokeWidth}" stroke-linecap="round" stroke-linejoin="round" />
      </g>
    `;
  }

  exportStoryCard() {
    const route = this.activeStudioRoute;
    const modalHtml = `
      <div style="background:#0D1117;border-radius:18px;padding:16px;color:#fff;display:flex;flex-direction:column;gap:12px;text-align:center;">
        <div style="width:100%;aspect-ratio:1;border-radius:12px;overflow:hidden;background:#111827;">
          <svg viewBox="0 0 500 500" style="width:100%;height:100%;">${this.buildSvgInner(route.points, route.blobs, this.selectedBrushStyle)}</svg>
        </div>
        <div>
          <h3 style="font-family:'Outfit';font-size:20px;font-weight:800;color:#52B788;">${route.shapeName}</h3>
          <p style="font-size:12px;color:#94A3B8;">${route.dateString} • VIT Main Campus</p>
        </div>
        <div style="display:flex;justify-content:space-around;background:rgba(255,255,255,0.08);padding:10px;border-radius:12px;">
          <div><div style="font-size:16px;font-weight:800;">${route.distanceKm} km</div><div style="font-size:10px;color:#94A3B8;">Distance</div></div>
          <div><div style="font-size:16px;font-weight:800;">${route.steps.toLocaleString()}</div><div style="font-size:10px;color:#94A3B8;">Steps</div></div>
          <div><div style="font-size:16px;font-weight:800;">${route.calories} kcal</div><div style="font-size:10px;color:#94A3B8;">Burned</div></div>
        </div>
        <button class="btn-modal-primary" id="btn-download-story-png" style="width:100%;margin-top:6px;">Download Story Image</button>
      </div>
    `;
    this.showModal("📲 Instagram Story Card", modalHtml);
    document.getElementById("btn-download-story-png").addEventListener("click", () => this.exportHighResPng());
  }

  exportHighResPng() {
    const route = this.activeStudioRoute;
    const svgXml = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 500 500" width="1080" height="1080">${this.buildSvgInner(route.points, route.blobs, this.selectedBrushStyle)}</svg>`;
    const blob = new Blob([svgXml], { type: "image/svg+xml;charset=utf-8" });
    const URL = window.URL || window.webkitURL || window;
    const blobURL = URL.createObjectURL(blob);
    const image = new Image();

    image.onload = () => {
      const canvas = document.createElement("canvas");
      canvas.width = 1080;
      canvas.height = 1080;
      const context = canvas.getContext("2d");
      context.drawImage(image, 0, 0);

      // Watermark Text
      context.font = "bold 32px 'Plus Jakarta Sans', sans-serif";
      context.fillStyle = "#52B788";
      context.fillText("WalkArt • " + route.shapeName, 50, 1000);
      context.font = "24px 'Plus Jakarta Sans', sans-serif";
      context.fillStyle = "#94A3B8";
      context.fillText(`${route.dateString} • ${route.distanceKm} km • ${route.steps.toLocaleString()} steps`, 50, 1035);

      const pngUrl = canvas.toDataURL("image/png");
      const downloadLink = document.createElement("a");
      downloadLink.href = pngUrl;
      downloadLink.download = `WalkArt-${route.shapeName.replace(/\s+/g, "_")}.png`;
      document.body.appendChild(downloadLink);
      downloadLink.click();
      downloadLink.remove();
      this.showToast("High-Res PNG downloaded successfully!", "📥");
    };
    image.src = blobURL;
  }

  exportSvgFile() {
    const route = this.activeStudioRoute;
    const svgXml = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 500 500" width="500" height="500">${this.buildSvgInner(route.points, route.blobs, this.selectedBrushStyle)}</svg>`;
    const blob = new Blob([svgXml], { type: "image/svg+xml;charset=utf-8" });
    const downloadLink = document.createElement("a");
    downloadLink.href = URL.createObjectURL(blob);
    downloadLink.download = `WalkArt-${route.shapeName.replace(/\s+/g, "_")}.svg`;
    document.body.appendChild(downloadLink);
    downloadLink.click();
    downloadLink.remove();
    this.showToast("Vector SVG downloaded!", "📥");
  }

  // ==========================================
  // 5. LIVE GPS TRACKER & SIMULATOR LOGIC
  // ==========================================
  initTrackerCanvases() {
    this.mapCanvas = document.getElementById("tracker-map-canvas");
    this.artCanvas = document.getElementById("tracker-art-canvas");
    if (!this.mapCanvas || !this.artCanvas) return;
    this.mapCtx = this.mapCanvas.getContext("2d");
    this.artCtx = this.artCanvas.getContext("2d");
    this.drawTrackerMapBackground();
  }

  drawTrackerMapBackground() {
    const ctx = this.mapCtx;
    const w = this.mapCanvas.width;
    const h = this.mapCanvas.height;

    // Dark Map Background
    ctx.fillStyle = "#0F172A";
    ctx.fillRect(0, 0, w, h);

    // Subtle grid lines
    ctx.strokeStyle = "#1E293B";
    ctx.lineWidth = 1;
    for (let x = 0; x < w; x += 40) {
      ctx.beginPath(); ctx.moveTo(x, 0); ctx.lineTo(x, h); ctx.stroke();
    }
    for (let y = 0; y < h; y += 40) {
      ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(w, y); ctx.stroke();
    }

    // Campus pathways
    ctx.strokeStyle = "#334155";
    ctx.lineWidth = 6;
    ctx.lineCap = "round";
    ctx.beginPath(); ctx.moveTo(50, 200); ctx.lineTo(550, 200); ctx.stroke();
    ctx.beginPath(); ctx.moveTo(200, 50); ctx.lineTo(200, 380); ctx.stroke();
    ctx.beginPath(); ctx.moveTo(420, 50); ctx.lineTo(420, 380); ctx.stroke();

    // Lake water body
    ctx.fillStyle = "#1E3A8A";
    ctx.beginPath();
    ctx.ellipse(460, 310, 80, 55, 0, 0, Math.PI * 2);
    ctx.fill();

    ctx.fillStyle = "#60A5FA";
    ctx.font = "bold 11px sans-serif";
    ctx.fillText("Central Lake", 430, 315);
  }

  bindTrackerEvents() {
    // Preset buttons
    const presetBtns = document.querySelectorAll(".preset-btn");
    presetBtns.forEach(btn => {
      btn.addEventListener("click", () => {
        if (this.isTracking) return;
        presetBtns.forEach(b => b.classList.remove("active"));
        btn.classList.add("active");
        this.activePreset = btn.getAttribute("data-preset");
        this.resetTrackerState();
      });
    });

    // Start / Pause Walk Button
    const toggleBtn = document.getElementById("btn-toggle-walk");
    const finishBtn = document.getElementById("btn-finish-walk");

    toggleBtn.addEventListener("click", () => {
      if (!this.isTracking) {
        this.startWalkSession();
      } else {
        this.pauseWalkSession();
      }
    });

    finishBtn.addEventListener("click", () => {
      this.finishAndMintWalk();
    });

    // Custom Canvas Draw Mode
    let isDrawing = false;
    this.artCanvas.addEventListener("mousedown", (e) => {
      if (this.activePreset !== "custom-draw") return;
      isDrawing = true;
      this.isTracking = true;
      finishBtn.disabled = false;
      const rect = this.artCanvas.getBoundingClientRect();
      const x = (e.clientX - rect.left) * (this.artCanvas.width / rect.width);
      const y = (e.clientY - rect.top) * (this.artCanvas.height / rect.height);
      this.addTrackingPoint(x, y);
    });

    this.artCanvas.addEventListener("mousemove", (e) => {
      if (!isDrawing || this.activePreset !== "custom-draw") return;
      const rect = this.artCanvas.getBoundingClientRect();
      const x = (e.clientX - rect.left) * (this.artCanvas.width / rect.width);
      const y = (e.clientY - rect.top) * (this.artCanvas.height / rect.height);
      this.addTrackingPoint(x, y);
    });

    window.addEventListener("mouseup", () => { isDrawing = false; });
  }

  startWalkSession() {
    this.isTracking = true;
    const toggleBtn = document.getElementById("btn-toggle-walk");
    const finishBtn = document.getElementById("btn-finish-walk");
    toggleBtn.classList.add("is-paused");
    document.getElementById("tracker-btn-text").textContent = "Pause Walk";
    finishBtn.disabled = false;

    if (this.activePreset === "real-gps") {
      this.startRealGpsTracking();
    } else {
      this.startSimulationTimer();
    }
    this.showToast("Walk session started! Keep moving.", "👟");
  }

  pauseWalkSession() {
    this.isTracking = false;
    const toggleBtn = document.getElementById("btn-toggle-walk");
    toggleBtn.classList.remove("is-paused");
    document.getElementById("tracker-btn-text").textContent = "Resume Walk";
    if (this.trackingTimer) clearInterval(this.trackingTimer);
    if (this.watchGpsId) navigator.geolocation.clearWatch(this.watchGpsId);
  }

  startSimulationTimer() {
    if (this.trackingTimer) clearInterval(this.trackingTimer);
    let stepIndex = 0;
    const totalSimSteps = 120;
    const w = this.artCanvas.width;
    const h = this.artCanvas.height;

    this.trackingTimer = setInterval(() => {
      stepIndex++;
      this.liveSeconds += 2;
      this.liveSteps += 18 + Math.floor(Math.random() * 8);
      this.liveDistance += 0.015;
      this.liveCalories += 1;

      // Calculate path point based on preset
      const progress = stepIndex / totalSimSteps;
      const t = progress * 2 * Math.PI;
      let x = w / 2;
      let y = h / 2;

      if (this.activePreset === "lake-jog") {
        x = w / 2 + 140 * Math.cos(t) + 20 * Math.sin(3 * t);
        y = h / 2 + 100 * Math.sin(t) + 15 * Math.cos(2 * t);
      } else if (this.activePreset === "sjt-canteen") {
        x = 100 + progress * 400 + 30 * Math.sin(progress * 4 * Math.PI);
        y = 120 + 200 * Math.sin(progress * Math.PI);
      } else { // perimeter trek
        x = w / 2 + 180 * Math.cos(t) * (1 + 0.2 * Math.sin(4 * t));
        y = h / 2 + 130 * Math.sin(t) * (1 + 0.2 * Math.cos(3 * t));
      }

      this.addTrackingPoint(x, y);

      if (stepIndex >= totalSimSteps) {
        clearInterval(this.trackingTimer);
        this.showToast("Campus route completed! Ready to mint.", "🎉");
      }
    }, 200);
  }

  startRealGpsTracking() {
    if (!navigator.geolocation) {
      this.showToast("Geolocation not supported by this browser.", "⚠️");
      return;
    }
    this.watchGpsId = navigator.geolocation.watchPosition(
      (pos) => {
        this.liveDistance += 0.01;
        this.liveSteps += 14;
        this.liveCalories += 1;
        const x = (pos.coords.longitude % 1) * this.artCanvas.width;
        const y = (pos.coords.latitude % 1) * this.artCanvas.height;
        this.addTrackingPoint(x, y);
      },
      (err) => console.warn(err),
      { enableHighAccuracy: true }
    );
  }

  addTrackingPoint(x, y) {
    this.simulatedPath.push({ x, y });
    this.updateTrackerHUD();
    this.drawTrackerArtPath();
  }

  updateTrackerHUD() {
    document.getElementById("live-stat-distance").innerHTML = `${this.liveDistance.toFixed(2)} <small>km</small>`;
    document.getElementById("live-stat-steps").textContent = this.liveSteps.toLocaleString();
    const paceMin = this.liveDistance > 0 ? (this.liveSeconds / 60 / this.liveDistance) : 0;
    const pM = Math.floor(paceMin);
    const pS = Math.floor((paceMin - pM) * 60);
    document.getElementById("live-stat-pace").innerHTML = `${pM}:${pS.toString().padStart(2, '0')} <small>/km</small>`;
    document.getElementById("live-stat-calories").innerHTML = `${this.liveCalories} <small>kcal</small>`;

    if (this.simulatedPath.length > 6) {
      const shape = RouteArtEngine.classifyShape(this.simulatedPath, this.liveDistance);
      document.getElementById("live-detected-shape").textContent = shape.title;
    }
  }

  drawTrackerArtPath() {
    const ctx = this.artCtx;
    ctx.clearRect(0, 0, this.artCanvas.width, this.artCanvas.height);
    if (this.simulatedPath.length < 2) return;

    // Glowing stroke
    ctx.strokeStyle = "#00F5D4";
    ctx.lineWidth = 4;
    ctx.lineCap = "round";
    ctx.lineJoin = "round";
    ctx.shadowColor = "#00F5D4";
    ctx.shadowBlur = 10;

    ctx.beginPath();
    ctx.moveTo(this.simulatedPath[0].x, this.simulatedPath[0].y);
    for (let i = 1; i < this.simulatedPath.length; i++) {
      ctx.lineTo(this.simulatedPath[i].x, this.simulatedPath[i].y);
    }
    ctx.stroke();
    ctx.shadowBlur = 0; // reset
  }

  finishAndMintWalk() {
    this.pauseWalkSession();
    if (this.simulatedPath.length < 5) {
      this.showToast("Walk a bit more before minting!", "⚠️");
      return;
    }

    const simplified = RouteArtEngine.simplifyPoints(this.simulatedPath, 3.0);
    const normalized = RouteArtEngine.normalizePoints(simplified, 800, 100);
    const blobs = RouteArtEngine.generateColorBlobs(normalized);
    const shape = RouteArtEngine.classifyShape(normalized, this.liveDistance);
    const earnedCrystals = Math.max(50, Math.floor(this.liveDistance * 40));

    const newRoute = {
      id: Date.now(),
      dateString: "Today (Minted)",
      title: shape.title,
      shapeName: shape.title,
      shapeCategory: shape.category,
      distanceKm: parseFloat(this.liveDistance.toFixed(2)) || 3.5,
      steps: this.liveSteps || 4200,
      calories: this.liveCalories || 180,
      points: normalized,
      blobs: blobs,
      brushStyle: "NEON",
      paletteIndex: 0,
      isFavorite: true
    };

    this.routes.unshift(newRoute);
    this.saveData("cra_routes", this.routes);
    this.crystals += earnedCrystals;
    this.updateCrystalDisplays();

    // Check Quests progress
    this.quests.forEach(q => {
      if (q.id === "q1") q.current = Math.min(q.target, q.current + newRoute.steps);
      if (q.id === "q3") q.current = Math.min(q.target, q.current + newRoute.distanceKm);
      if (q.current >= q.target) q.completed = true;
    });
    this.saveData("cra_quests", this.quests);

    this.showToast(`🎉 Masterpiece minted! +${earnedCrystals} 💎 crystals earned!`, "💎");
    this.resetTrackerState();
    this.openInStudio(newRoute);
  }

  resetTrackerState() {
    if (this.trackingTimer) clearInterval(this.trackingTimer);
    if (this.watchGpsId) navigator.geolocation.clearWatch(this.watchGpsId);
    this.isTracking = false;
    this.simulatedPath = [];
    this.liveDistance = 0.0;
    this.liveSteps = 0;
    this.liveSeconds = 0;
    this.liveCalories = 0;
    document.getElementById("btn-toggle-walk").classList.remove("is-paused");
    document.getElementById("tracker-btn-text").textContent = "Start Walking";
    document.getElementById("btn-finish-walk").disabled = true;
    this.updateTrackerHUD();
    if (this.artCtx) this.artCtx.clearRect(0, 0, this.artCanvas.width, this.artCanvas.height);
  }

  // ==========================================
  // 6. CAMPUS MAP & LANDMARKS LOGIC
  // ==========================================
  bindMapEvents() {
    document.getElementById("btn-walk-landmark").addEventListener("click", () => {
      this.switchView("view-tracker");
      this.startWalkSession();
    });
  }

  renderCampusMap() {
    const pinsGroup = document.getElementById("map-landmark-pins");
    pinsGroup.innerHTML = "";

    this.landmarks.forEach(lm => {
      const g = document.createElementNS("http://www.w3.org/2000/svg", "g");
      g.setAttribute("class", "landmark-pin-group");
      g.setAttribute("transform", `translate(${lm.x}, ${lm.y})`);

      g.innerHTML = `
        <circle cx="0" cy="0" r="18" fill="#FFFFFF" stroke="#52B788" stroke-width="3" filter="drop-shadow(0 2px 4px rgba(0,0,0,0.15))" />
        <text x="0" y="5" font-size="14" text-anchor="middle">${lm.icon}</text>
      `;

      g.addEventListener("click", () => {
        document.getElementById("landmark-icon").textContent = lm.icon;
        document.getElementById("landmark-title").textContent = lm.name;
        document.getElementById("landmark-desc").textContent = lm.desc;
        document.getElementById("landmark-stats").innerHTML = `<span>👣 ${lm.steps}</span><span>✨ ${lm.walks}</span>`;
        this.showToast(`Selected ${lm.name}`, lm.icon);
      });

      pinsGroup.appendChild(g);
    });

    // Render Preset Walks List
    const presetList = document.getElementById("campus-presets-list");
    presetList.innerHTML = "";

    const walkChallenges = [
      { name: "SJT to Food Court Stride", icon: "🏫", desc: "2.4 km • Academic Quad crossing", reward: "+80 💎" },
      { name: "Central Lake Sunset Jog", icon: "🏞️", desc: "3.2 km • Scenic water loop", reward: "+120 💎" },
      { name: "Perimeter Campus Marathon", icon: "🌲", desc: "6.8 km • Outer campus boundary", reward: "+250 💎" }
    ];

    walkChallenges.forEach(wc => {
      const item = document.createElement("div");
      item.className = "preset-walk-card";
      item.innerHTML = `
        <div class="preset-walk-left">
          <span class="preset-walk-icon">${wc.icon}</span>
          <div>
            <div class="preset-walk-name">${wc.name}</div>
            <div class="preset-walk-sub">${wc.desc}</div>
          </div>
        </div>
        <span class="quest-reward-chip">${wc.reward}</span>
      `;
      item.addEventListener("click", () => {
        this.switchView("view-tracker");
        this.startWalkSession();
      });
      presetList.appendChild(item);
    });
  }

  // ==========================================
  // 7. QUESTS & LEADERBOARD LOGIC
  // ==========================================
  bindQuestEvents() {
    const questTabs = document.querySelectorAll(".quest-tab-btn");
    questTabs.forEach(tab => {
      tab.addEventListener("click", () => {
        questTabs.forEach(t => t.classList.remove("active"));
        tab.classList.add("active");
        this.renderQuests(tab.getAttribute("data-quest-cat"));
      });
    });
  }

  renderQuests(filterCategory = "ALL") {
    const listContainer = document.getElementById("quests-list-container");
    listContainer.innerHTML = "";

    const filtered = filterCategory === "ALL" ? this.quests : this.quests.filter(q => q.cat === filterCategory);

    filtered.forEach(q => {
      const card = document.createElement("div");
      card.className = "quest-card";
      const progressFraction = Math.min(1, q.current / q.target);
      const progressPercent = Math.round(progressFraction * 100);

      card.innerHTML = `
        <div class="quest-card-top">
          <div class="quest-info-group">
            <span class="quest-icon">${q.icon}</span>
            <div>
              <div class="quest-title">${q.title}</div>
              <div class="quest-desc">${q.desc}</div>
            </div>
          </div>
          <span class="quest-reward-chip">+${q.reward} 💎</span>
        </div>
        <div class="quest-progress-bar-wrap">
          <div class="quest-progress-track">
            <div class="quest-progress-fill" style="width:${progressPercent}%"></div>
          </div>
        </div>
        <div class="quest-card-footer">
          <span class="quest-progress-text">${q.current} / ${q.target} (${progressPercent}%)</span>
          <button class="btn-claim-reward ${q.claimed ? 'claimed' : ''}" ${q.completed && !q.claimed ? '' : 'disabled'}>
            ${q.claimed ? 'Claimed ✓' : (q.completed ? 'Claim Reward 🎁' : 'In Progress')}
          </button>
        </div>
      `;

      const claimBtn = card.querySelector(".btn-claim-reward");
      if (claimBtn && q.completed && !q.claimed) {
        claimBtn.addEventListener("click", () => {
          q.claimed = true;
          this.crystals += q.reward;
          this.saveData("cra_quests", this.quests);
          this.updateCrystalDisplays();
          this.renderQuests(filterCategory);
          this.showToast(`Claimed +${q.reward} Crystals!`, "🎁");
        });
      }

      listContainer.appendChild(card);
    });
  }

  renderLeaderboard() {
    const rowsContainer = document.getElementById("leaderboard-rows-container");
    rowsContainer.innerHTML = "";

    this.leaderboard.forEach(item => {
      const row = document.createElement("div");
      row.className = `leaderboard-row ${item.isUser ? 'is-user' : ''}`;
      row.innerHTML = `
        <span class="rank-badge rank-${item.rank}">#${item.rank}</span>
        <div class="student-col">
          <span class="student-avatar">${item.avatar}</span>
          <span>${item.name}</span>
        </div>
        <span>${item.km}</span>
        <span>${item.crystals}</span>
      `;
      rowsContainer.appendChild(row);
    });
  }

  // ==========================================
  // 8. PIGMENT LAB & STORE LOGIC
  // ==========================================
  bindStoreEvents() {
    const storeTabs = document.querySelectorAll(".store-tab-btn");
    storeTabs.forEach(tab => {
      tab.addEventListener("click", () => {
        storeTabs.forEach(t => t.classList.remove("active"));
        tab.classList.add("active");
        this.renderStore(tab.getAttribute("data-store-tab"));
      });
    });
  }

  renderStore(category = "BRUSH") {
    const container = document.getElementById("store-items-grid-container");
    container.innerHTML = "";

    const items = this.storeItems.filter(i => i.cat === category);

    items.forEach(item => {
      const card = document.createElement("div");
      card.className = "store-item-card";
      card.innerHTML = `
        <div class="store-item-preview" style="background:#F1F5F9;">
          <span>${item.preview}</span>
        </div>
        <div class="store-item-name">${item.name}</div>
        <div class="store-item-desc">${item.desc}</div>
        <div class="store-item-footer">
          <span class="item-cost">${item.cost === 0 ? 'Free' : item.cost + ' 💎'}</span>
          <button class="btn-buy-item ${item.unlocked ? 'unlocked' : ''}">
            ${item.unlocked ? 'Unlocked ✓' : 'Unlock'}
          </button>
        </div>
      `;

      const buyBtn = card.querySelector(".btn-buy-item");
      if (buyBtn && !item.unlocked) {
        buyBtn.addEventListener("click", () => {
          if (this.crystals < item.cost) {
            this.showToast("Not enough crystals! Complete walks to earn more.", "⚠️");
            return;
          }
          this.crystals -= item.cost;
          item.unlocked = true;
          this.saveData("cra_store", this.storeItems);
          this.updateCrystalDisplays();
          this.renderStore(category);
          this.showToast(`Unlocked ${item.name}!`, "🎉");
        });
      }

      container.appendChild(card);
    });
  }

  // ==========================================
  // 9. PROFILE & ARTWORKS GALLERY LOGIC
  // ==========================================
  bindProfileEvents() {
    const filterChips = document.querySelectorAll(".filter-chip");
    filterChips.forEach(chip => {
      chip.addEventListener("click", () => {
        filterChips.forEach(c => c.classList.remove("active"));
        chip.classList.add("active");
        this.renderProfileGallery(chip.getAttribute("data-filter"));
      });
    });
  }

  renderProfile() {
    // Badges
    const badgesContainer = document.getElementById("profile-badges-container");
    badgesContainer.innerHTML = "";

    this.badges.forEach(b => {
      const card = document.createElement("div");
      card.className = `badge-item-card ${b.unlocked ? '' : 'locked'}`;
      card.innerHTML = `
        <span class="badge-emoji">${b.emoji}</span>
        <span class="badge-name">${b.name}</span>
        <span class="badge-rarity">${b.rarity}</span>
      `;
      card.addEventListener("click", () => {
        this.showModal(`${b.emoji} ${b.name} (${b.rarity})`, `<p style="font-size:14px;color:#374151;">${b.desc}</p><p style="margin-top:8px;font-weight:700;color:${b.unlocked ? '#059669' : '#DC2626'}">${b.unlocked ? '✓ Unlocked' : '🔒 Locked'}</p>`);
      });
      badgesContainer.appendChild(card);
    });

    this.renderProfileGallery("ALL");
  }

  renderProfileGallery(filter = "ALL") {
    const grid = document.getElementById("profile-artworks-grid");
    grid.innerHTML = "";

    let list = this.routes;
    if (filter === "FAVORITE") list = list.filter(r => r.isFavorite);
    else if (filter !== "ALL") list = list.filter(r => r.shapeCategory === filter);

    list.forEach(route => {
      const card = document.createElement("div");
      card.className = "artwork-grid-card";
      card.innerHTML = `
        <div class="mini-canvas-thumb">
          <svg viewBox="0 0 500 500">${this.buildSvgInner(route.points, route.blobs, route.brushStyle)}</svg>
        </div>
        <div style="display:flex;justify-content:space-between;align-items:center;">
          <span style="font-size:13px;font-weight:700;color:#111827;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">${route.shapeName}</span>
          <span style="font-size:12px;cursor:pointer;" class="btn-fav-star">${route.isFavorite ? '★' : '☆'}</span>
        </div>
        <div style="font-size:10px;color:#6B7280;">${route.dateString} • ${route.distanceKm} km</div>
      `;

      card.querySelector(".mini-canvas-thumb").addEventListener("click", () => this.openInStudio(route));
      const star = card.querySelector(".btn-fav-star");
      star.addEventListener("click", (e) => {
        e.stopPropagation();
        route.isFavorite = !route.isFavorite;
        this.saveData("cra_routes", this.routes);
        this.renderProfileGallery(filter);
      });

      grid.appendChild(card);
    });
  }
}

// Bootstrap Application on DOM Ready
document.addEventListener("DOMContentLoaded", () => {
  window.app = new CampusRouteArtApp();
});
