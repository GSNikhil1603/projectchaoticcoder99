import React, { useState } from 'react';
import {
  Palette,
  Sparkles,
  Download,
  Share2,
  Check,
  RotateCcw,
  Stamp,
  Brush,
  Layers,
} from 'lucide-react';
import { useApp } from '../context/AppContext';
import { ArtCanvasView } from './ArtCanvasView';
import { ART_PALETTES, RouteArtEngine } from '../engine/routeArtEngine';
import { ColorBlob, LandmarkSticker } from '../types';
import confetti from 'canvas-confetti';

export const ColoringStudio: React.FC = () => {
  const {
    routes,
    selectedArtworkId,
    setSelectedArtworkId,
    updateRouteArt,
    customPigments,
    showToast,
  } = useApp();

  const currentArtwork = routes.find((r) => r.id === selectedArtworkId) || routes[0];

  // Editable local state
  const [activePaletteKey, setActivePaletteKey] = useState<string>(
    currentArtwork?.artStyle || 'Pastel Bloom'
  );
  const [activeBrushKey, setActiveBrushKey] = useState<'INK' | 'WATERCOLOR' | 'NEON' | 'CHALK'>(
    currentArtwork?.brushStyleKey || 'INK'
  );
  const [blobs, setBlobs] = useState<ColorBlob[]>(() =>
    currentArtwork ? RouteArtEngine.jsonToBlobs(currentArtwork.blobsJson) : []
  );
  const [stickers, setStickers] = useState<LandmarkSticker[]>(() =>
    currentArtwork ? RouteArtEngine.jsonToStickers(currentArtwork.landmarkStickersJson) : []
  );

  const [selectedBlobIndex, setSelectedBlobIndex] = useState<number | null>(null);
  const [activeColorChoice, setActiveColorChoice] = useState<string>(
    ART_PALETTES['Pastel Bloom'][0]
  );
  const [activeStickerToPlace, setActiveStickerToPlace] = useState<string | null>(null);

  // Synchronize when active artwork selection changes
  React.useEffect(() => {
    if (currentArtwork) {
      setActivePaletteKey(currentArtwork.artStyle);
      setActiveBrushKey(currentArtwork.brushStyleKey);
      setBlobs(RouteArtEngine.jsonToBlobs(currentArtwork.blobsJson));
      setStickers(RouteArtEngine.jsonToStickers(currentArtwork.landmarkStickersJson));
      setSelectedBlobIndex(null);
    }
  }, [currentArtwork?.id]);

  if (!currentArtwork) {
    return (
      <div className="max-w-4xl mx-auto p-10 text-center space-y-3">
        <Palette size={40} className="mx-auto text-[#75857C]" />
        <h3 className="text-lg font-bold text-[#16211B]">No artwork selected</h3>
        <p className="text-xs text-[#75857C]">Complete a campus walk or select an artwork from the home gallery.</p>
      </div>
    );
  }

  // Handle Palette Switch: harmonic recoloring of all blobs
  const applyPaletteToAllBlobs = (paletteName: string) => {
    setActivePaletteKey(paletteName);
    const colors = ART_PALETTES[paletteName] || ART_PALETTES['Pastel Bloom'];
    setBlobs((prev) =>
      prev.map((b, i) => ({
        ...b,
        colorHex: colors[i % colors.length],
      }))
    );
    showToast(`Applied ${paletteName} palette to artwork 🎨`);
  };

  // Handle Tap-to-Recolor on Canvas
  const handleBlobTapped = (_blob: ColorBlob, index: number) => {
    setSelectedBlobIndex(index);
    if (activeColorChoice) {
      setBlobs((prev) =>
        prev.map((b, i) => (i === index ? { ...b, colorHex: activeColorChoice } : b))
      );
      showToast('Recolored watercolor blob! ✨');
    }
  };

  // Handle Canvas Tap for Sticker Placement
  const handleCanvasTapped = (normX: number, normY: number) => {
    if (activeStickerToPlace) {
      const newSticker: LandmarkSticker = {
        id: `st_${Date.now()}`,
        name: 'Landmark Tag',
        iconEmoji: activeStickerToPlace,
        x: Math.round(normX),
        y: Math.round(normY),
      };
      setStickers((prev) => [...prev, newSticker]);
      setActiveStickerToPlace(null);
      showToast(`Placed sticker ${newSticker.iconEmoji} on canvas! ✨`);
    }
  };

  // Save changes to Artwork Entity
  const handleSaveChanges = () => {
    const updatedSvg = RouteArtEngine.generateFullSvg(
      RouteArtEngine.jsonToPoints(currentArtwork.pointsJson),
      blobs,
      activePaletteKey,
      activeBrushKey,
      currentArtwork.shapeCategory,
      stickers
    );

    updateRouteArt(currentArtwork.id, {
      artStyle: activePaletteKey,
      brushStyleKey: activeBrushKey,
      blobsJson: JSON.stringify(blobs),
      landmarkStickersJson: JSON.stringify(stickers),
      previewSvg: updatedSvg,
    });

    confetti({ particleCount: 60, spread: 70, origin: { y: 0.7 } });
  };

  // Export high-res SVG
  const handleExportSvg = () => {
    const fullSvg = RouteArtEngine.generateFullSvg(
      RouteArtEngine.jsonToPoints(currentArtwork.pointsJson),
      blobs,
      activePaletteKey,
      activeBrushKey,
      currentArtwork.shapeCategory,
      stickers
    );
    const blob = new Blob([fullSvg], { type: 'image/svg+xml;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${currentArtwork.title.replace(/\s+/g, '_')}_customized.svg`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    showToast('Downloaded custom SVG! 🎨');
  };

  const currentPaletteColors = ART_PALETTES[activePaletteKey] || ART_PALETTES['Pastel Bloom'];

  return (
    <div className="space-y-6 pb-24 max-w-4xl mx-auto px-4 sm:px-6 pt-4">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-[#16211B] font-display">
            Coloring & Art Customization Studio
          </h2>
          <p className="text-xs sm:text-sm text-[#75857C]">
            Tap blobs to recolor, switch palettes, place landmark stickers, and adjust strokes.
          </p>
        </div>

        {/* Artwork selector dropdown */}
        <select
          value={currentArtwork.id}
          onChange={(e) => setSelectedArtworkId(e.target.value)}
          className="px-3.5 py-2 rounded-2xl bg-white border border-[#D8DFD5] text-xs font-bold text-[#16211B] focus:outline-none focus:border-[#56B386]"
        >
          {routes.map((r) => (
            <option key={r.id} value={r.id}>
              {r.title} ({r.distanceKm} km)
            </option>
          ))}
        </select>
      </div>

      {/* Main Studio Viewport */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-start">
        {/* Interactive Art Canvas */}
        <div className="md:col-span-6 bg-white rounded-[28px] border border-[#D8DFD5] p-5 shadow-sm space-y-3 flex flex-col items-center">
          <div className="w-full aspect-square rounded-[22px] bg-[#F7F5EE] border border-[#D8DFD5] relative overflow-hidden p-3 shadow-inner">
            <ArtCanvasView
              pointsJson={currentArtwork.pointsJson}
              blobsJson={JSON.stringify(blobs)}
              artStyle={activePaletteKey}
              brushStyleKey={activeBrushKey}
              stickersJson={JSON.stringify(stickers)}
              isInteractive={true}
              onBlobTapped={handleBlobTapped}
              onCanvasTapped={handleCanvasTapped}
            />

            {activeStickerToPlace && (
              <div className="absolute top-3 left-3 bg-[#16211B] text-white px-3 py-1 rounded-full text-xs font-bold shadow-md animate-pulse">
                Tap canvas to place {activeStickerToPlace}
              </div>
            )}
          </div>

          <div className="flex items-center justify-between w-full text-xs text-[#75857C] px-1">
            <span>💡 Tap any watercolor blob to paint it with selected pigment</span>
            {stickers.length > 0 && (
              <button
                onClick={() => setStickers([])}
                className="text-red-500 hover:underline font-semibold"
              >
                Clear stickers ({stickers.length})
              </button>
            )}
          </div>
        </div>

        {/* Studio Tool Palette & Pigment Controls */}
        <div className="md:col-span-6 space-y-4">
          {/* Active Swatch Picker */}
          <div className="bg-white rounded-[24px] border border-[#D8DFD5] p-5 shadow-sm space-y-3">
            <label className="text-xs font-bold text-[#16211B] block">
              Active Painting Pigment (Tap to select color)
            </label>

            {/* Current Palette Swatches */}
            <div className="flex items-center space-x-2.5 overflow-x-auto pb-1">
              {currentPaletteColors.map((color) => {
                const isSelected = activeColorChoice === color;
                return (
                  <button
                    key={color}
                    onClick={() => setActiveColorChoice(color)}
                    style={{ backgroundColor: color }}
                    className={`w-9 h-9 rounded-full shadow-xs transition-transform flex items-center justify-center flex-shrink-0 ${
                      isSelected ? 'scale-115 ring-3 ring-[#16211B]' : 'hover:scale-105'
                    }`}
                  >
                    {isSelected && <Check size={14} className="text-white drop-shadow-xs" />}
                  </button>
                );
              })}
            </div>

            {/* User's Custom Mixed Pigments from Color Lab */}
            {customPigments.length > 0 && (
              <div className="pt-2 border-t border-[#D8DFD5]">
                <span className="text-[11px] font-bold text-[#75857C] block mb-1.5">
                  Your Custom Laboratory Pigments:
                </span>
                <div className="flex items-center space-x-2 overflow-x-auto pb-1">
                  {customPigments.map((pig) => {
                    const isSelected = activeColorChoice === pig.hexCode;
                    return (
                      <button
                        key={pig.id}
                        onClick={() => setActiveColorChoice(pig.hexCode)}
                        style={{ backgroundColor: pig.hexCode }}
                        title={pig.name}
                        className={`w-8 h-8 rounded-full shadow-xs transition-transform flex items-center justify-center flex-shrink-0 ${
                          isSelected ? 'scale-115 ring-3 ring-[#16211B]' : 'hover:scale-105'
                        }`}
                      >
                        {isSelected && <Check size={12} className="text-white drop-shadow-xs" />}
                      </button>
                    );
                  })}
                </div>
              </div>
            )}
          </div>

          {/* Harmonic Palettes Switcher */}
          <div className="bg-white rounded-[24px] border border-[#D8DFD5] p-5 shadow-sm space-y-3">
            <label className="text-xs font-bold text-[#16211B] block">
              Harmonic Color Themes
            </label>
            <div className="grid grid-cols-2 gap-2 text-xs">
              {Object.keys(ART_PALETTES).map((palName) => {
                const isSelected = activePaletteKey === palName;
                return (
                  <button
                    key={palName}
                    onClick={() => applyPaletteToAllBlobs(palName)}
                    className={`p-2.5 rounded-xl border text-left font-semibold transition-all flex items-center justify-between ${
                      isSelected
                        ? 'border-[#56B386] bg-[#D7F5E4] text-[#1E4833]'
                        : 'border-[#D8DFD5] bg-[#F7F5EE] text-[#16211B] hover:bg-neutral-100'
                    }`}
                  >
                    <span>{palName}</span>
                    <div className="flex -space-x-1">
                      {ART_PALETTES[palName].slice(0, 3).map((c, i) => (
                        <span
                          key={i}
                          style={{ backgroundColor: c }}
                          className="w-3 h-3 rounded-full border border-white"
                        />
                      ))}
                    </div>
                  </button>
                );
              })}
            </div>
          </div>

          {/* Brush & Stroke Style */}
          <div className="bg-white rounded-[24px] border border-[#D8DFD5] p-5 shadow-sm space-y-3">
            <label className="text-xs font-bold text-[#16211B] block">
              Brush Stroke Technique
            </label>
            <div className="grid grid-cols-4 gap-2 text-xs">
              {(['INK', 'WATERCOLOR', 'NEON', 'CHALK'] as const).map((b) => (
                <button
                  key={b}
                  onClick={() => setActiveBrushKey(b)}
                  className={`py-2 px-1 rounded-xl font-bold transition-all text-center ${
                    activeBrushKey === b
                      ? 'bg-[#16211B] text-white shadow-xs'
                      : 'bg-[#F7F5EE] border border-[#D8DFD5] text-[#16211B] hover:bg-neutral-100'
                  }`}
                >
                  {b}
                </button>
              ))}
            </div>
          </div>

          {/* Landmark Stickers Placement */}
          <div className="bg-white rounded-[24px] border border-[#D8DFD5] p-5 shadow-sm space-y-3">
            <label className="text-xs font-bold text-[#16211B] block">
              Place Campus Landmark Stickers
            </label>
            <div className="flex flex-wrap gap-2">
              {['🏛️', '🌿', '☕', '🎓', '⚽', '📚', '🎨', '🧪', '🌸', '⚡', '🌙', '🌟'].map(
                (emoji) => {
                  const isSelected = activeStickerToPlace === emoji;
                  return (
                    <button
                      key={emoji}
                      onClick={() =>
                        setActiveStickerToPlace(isSelected ? null : emoji)
                      }
                      className={`w-10 h-10 rounded-2xl text-xl flex items-center justify-center transition-transform ${
                        isSelected
                          ? 'bg-[#16211B] scale-115 ring-2 ring-[#56B386]'
                          : 'bg-[#F7F5EE] border border-[#D8DFD5] hover:scale-105'
                      }`}
                    >
                      {emoji}
                    </button>
                  );
                }
              )}
            </div>
          </div>

          {/* Action Buttons: Save & Export */}
          <div className="grid grid-cols-2 gap-3 pt-2">
            <button
              onClick={handleSaveChanges}
              className="py-3.5 px-4 rounded-2xl bg-[#56B386] text-white font-bold text-xs sm:text-sm shadow-md hover:bg-[#48A176] transition-all flex items-center justify-center space-x-2"
            >
              <Sparkles size={16} />
              <span>Save Changes ✨</span>
            </button>

            <button
              onClick={handleExportSvg}
              className="py-3.5 px-4 rounded-2xl bg-white border border-[#D8DFD5] text-[#16211B] font-bold text-xs sm:text-sm shadow-xs hover:bg-neutral-50 transition-all flex items-center justify-center space-x-2"
            >
              <Download size={16} />
              <span>Export SVG</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
