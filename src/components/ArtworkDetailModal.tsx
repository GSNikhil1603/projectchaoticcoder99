import React from 'react';
import {
  X,
  Heart,
  Download,
  Share2,
  Palette,
  Trash2,
  Sparkles,
  MapPin,
  TrendingUp,
  Flame,
  Clock,
  Footprints,
  Gauge,
} from 'lucide-react';
import { WalkRouteEntity } from '../types';
import { ArtCanvasView } from './ArtCanvasView';
import { useApp } from '../context/AppContext';
import confetti from 'canvas-confetti';

interface ArtworkDetailModalProps {
  artwork: WalkRouteEntity | null;
  onClose: () => void;
  onOpenInStudio: (artworkId: string) => void;
}

export const ArtworkDetailModal: React.FC<ArtworkDetailModalProps> = ({
  artwork,
  onClose,
  onOpenInStudio,
}) => {
  const { toggleFavoriteRoute, deleteRoute, showToast } = useApp();

  if (!artwork) return null;

  const handleDownloadSvg = () => {
    const blob = new Blob([artwork.previewSvg], { type: 'image/svg+xml;charset=utf-8' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `${artwork.title.replace(/\s+/g, '_')}.svg`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
    showToast('Downloaded high-res SVG artwork! 🎨');
    confetti({ particleCount: 40, spread: 60, origin: { y: 0.7 } });
  };

  const handleShare = async () => {
    if (navigator.share) {
      try {
        await navigator.share({
          title: `Campus Art: ${artwork.title}`,
          text: `I walked ${artwork.distanceKm} km and minted a ${artwork.shapeCategory} generative artwork on campus! 🌸✨`,
          url: window.location.href,
        });
      } catch {
        // Fallback to clipboard
        navigator.clipboard.writeText(
          `Campus Art: ${artwork.title} | ${artwork.distanceKm} km, ${artwork.stepCount} steps. Minted with PathCanvas! 🎨`
        );
        showToast('Artwork summary copied to clipboard! 📋');
      }
    } else {
      navigator.clipboard.writeText(
        `Campus Art: ${artwork.title} | ${artwork.distanceKm} km, ${artwork.stepCount} steps. Minted with PathCanvas! 🎨`
      );
      showToast('Artwork summary copied to clipboard! 📋');
    }
  };

  const handleDelete = () => {
    if (confirm('Are you sure you want to delete this artwork?')) {
      deleteRoute(artwork.id);
      onClose();
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4 overflow-y-auto">
      <div
        style={{ backgroundColor: '#F7F5EE' }}
        className="w-full max-w-xl rounded-[28px] border border-[#D8DFD5] shadow-2xl overflow-hidden flex flex-col max-h-[92vh]"
      >
        {/* Header */}
        <div className="p-4 sm:p-5 flex items-center justify-between border-b border-[#D8DFD5] bg-white/60">
          <div>
            <span className="text-[11px] font-bold uppercase tracking-wider text-[#56B386] bg-[#D7F5E4] px-2.5 py-0.5 rounded-full">
              {artwork.shapeCategory} Archetype
            </span>
            <h2 className="text-xl sm:text-2xl font-bold text-[#16211B] mt-1 font-display">
              {artwork.title}
            </h2>
            <p className="text-xs text-[#75857C]">{artwork.dateString}</p>
          </div>

          <div className="flex items-center space-x-2">
            <button
              onClick={() => toggleFavoriteRoute(artwork.id)}
              className="p-2 rounded-full bg-white border border-[#D8DFD5] hover:bg-neutral-50 transition-colors"
            >
              <Heart
                size={18}
                className={`${
                  artwork.isFavorite ? 'fill-[#866FB3] text-[#866FB3]' : 'text-[#75857C]'
                }`}
              />
            </button>
            <button
              onClick={onClose}
              className="p-2 rounded-full bg-white border border-[#D8DFD5] hover:bg-neutral-50 transition-colors"
            >
              <X size={18} className="text-[#16211B]" />
            </button>
          </div>
        </div>

        {/* Scrollable Body */}
        <div className="p-4 sm:p-6 overflow-y-auto space-y-5">
          {/* High-res Art Canvas Frame */}
          <div className="w-full aspect-square max-w-[420px] mx-auto rounded-[24px] bg-white border border-[#D8DFD5] p-4 shadow-sm relative overflow-hidden">
            <ArtCanvasView
              pointsJson={artwork.pointsJson}
              blobsJson={artwork.blobsJson}
              artStyle={artwork.artStyle}
              brushStyleKey={artwork.brushStyleKey}
              stickersJson={artwork.landmarkStickersJson}
            />

            {/* Overlay brush tag */}
            <div className="absolute bottom-3 right-3 bg-white/90 backdrop-blur-xs px-2.5 py-1 rounded-full text-[11px] font-bold text-[#16211B] shadow-xs border border-[#D8DFD5]">
              {artwork.brushStyleKey} Stroke
            </div>
          </div>

          {/* Stats Grid */}
          <div className="grid grid-cols-3 sm:grid-cols-6 gap-2.5">
            <div className="bg-white rounded-2xl p-3 border border-[#D8DFD5] text-center flex flex-col items-center">
              <MapPin size={16} className="text-[#56B386] mb-1" />
              <span className="text-base font-bold text-[#16211B]">{artwork.distanceKm}</span>
              <span className="text-[10px] text-[#75857C]">Kilometers</span>
            </div>

            <div className="bg-white rounded-2xl p-3 border border-[#D8DFD5] text-center flex flex-col items-center">
              <Footprints size={16} className="text-[#866FB3] mb-1" />
              <span className="text-base font-bold text-[#16211B]">{artwork.stepCount}</span>
              <span className="text-[10px] text-[#75857C]">Steps</span>
            </div>

            <div className="bg-white rounded-2xl p-3 border border-[#D8DFD5] text-center flex flex-col items-center">
              <Clock size={16} className="text-[#FFB74D] mb-1" />
              <span className="text-base font-bold text-[#16211B]">{artwork.durationMinutes}m</span>
              <span className="text-[10px] text-[#75857C]">Duration</span>
            </div>

            <div className="bg-white rounded-2xl p-3 border border-[#D8DFD5] text-center flex flex-col items-center">
              <Gauge size={16} className="text-[#319795] mb-1" />
              <span className="text-base font-bold text-[#16211B]">{artwork.avgSpeedKmh}</span>
              <span className="text-[10px] text-[#75857C]">km/h</span>
            </div>

            <div className="bg-white rounded-2xl p-3 border border-[#D8DFD5] text-center flex flex-col items-center">
              <TrendingUp size={16} className="text-[#FF8A65] mb-1" />
              <span className="text-base font-bold text-[#16211B]">+{artwork.elevationGainMeters}m</span>
              <span className="text-[10px] text-[#75857C]">Elevation</span>
            </div>

            <div className="bg-white rounded-2xl p-3 border border-[#D8DFD5] text-center flex flex-col items-center">
              <Flame size={16} className="text-[#E53E3E] mb-1" />
              <span className="text-base font-bold text-[#16211B]">{artwork.caloriesBurned}</span>
              <span className="text-[10px] text-[#75857C]">Calories</span>
            </div>
          </div>

          {/* Art Properties Banner */}
          <div className="bg-[#E2E6FF] rounded-2xl p-3.5 flex items-center justify-between text-xs text-[#16211B]">
            <div className="flex items-center space-x-2">
              <Sparkles size={16} className="text-[#866FB3]" />
              <span className="font-semibold">Palette: {artwork.artStyle}</span>
            </div>
            <span className="bg-white/80 px-2 py-0.5 rounded-full font-medium text-[#383387]">
              {artwork.syncStatus}
            </span>
          </div>
        </div>

        {/* Footer Actions */}
        <div className="p-4 sm:p-5 border-t border-[#D8DFD5] bg-white/70 flex flex-wrap gap-2 justify-between items-center">
          <div className="flex items-center space-x-2">
            <button
              onClick={() => {
                onOpenInStudio(artwork.id);
                onClose();
              }}
              className="flex items-center space-x-1.5 px-4 py-2.5 rounded-2xl bg-[#56B386] text-white text-xs sm:text-sm font-bold shadow-xs hover:bg-[#48A176] transition-colors"
            >
              <Palette size={16} />
              <span>Coloring Studio</span>
            </button>

            <button
              onClick={handleDownloadSvg}
              className="flex items-center space-x-1.5 px-3.5 py-2.5 rounded-2xl bg-white border border-[#D8DFD5] text-xs sm:text-sm font-semibold text-[#16211B] hover:bg-neutral-50 transition-colors"
            >
              <Download size={15} />
              <span>Export SVG</span>
            </button>

            <button
              onClick={handleShare}
              className="flex items-center space-x-1.5 px-3.5 py-2.5 rounded-2xl bg-white border border-[#D8DFD5] text-xs sm:text-sm font-semibold text-[#16211B] hover:bg-neutral-50 transition-colors"
            >
              <Share2 size={15} />
              <span>Share</span>
            </button>
          </div>

          <button
            onClick={handleDelete}
            className="p-2.5 rounded-2xl text-red-600 hover:bg-red-50 transition-colors"
            title="Delete Artwork"
          >
            <Trash2 size={16} />
          </button>
        </div>
      </div>
    </div>
  );
};
