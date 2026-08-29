import React from 'react';
import { Heart, Share2 } from 'lucide-react';
import { WalkRouteEntity } from '../types';
import { ArtCanvasView } from './ArtCanvasView';

interface CreationCardProps {
  route: WalkRouteEntity;
  index?: number;
  onClick: () => void;
  onFavoriteToggle: (e: React.MouseEvent) => void;
}

export const CreationCardItem: React.FC<CreationCardProps> = ({
  route,
  index = 0,
  onClick,
  onFavoriteToggle,
}) => {
  const glowColors = [
    'rgba(227, 248, 238, 0.95)', // Mint
    'rgba(236, 231, 255, 0.95)', // Lavender
    'rgba(227, 240, 253, 0.95)', // Soft Blue
    'rgba(241, 252, 228, 0.95)', // Lime
  ];
  const glowColor = glowColors[index % glowColors.length];

  const formatDate = (dateStr: string) => {
    const parts = dateStr.split(' ');
    if (parts.length >= 2) return `${parts[0]} ${parts[1].slice(0, 3)}`;
    return dateStr;
  };

  return (
    <div
      onClick={onClick}
      className="flex flex-col items-center cursor-pointer group flex-shrink-0"
    >
      <div
        style={{ backgroundColor: '#FFFFFF', borderColor: '#D8DFD5' }}
        className="w-20 h-20 rounded-[20px] border shadow-sm relative overflow-hidden transition-all duration-300 group-hover:scale-105 group-hover:shadow-md p-1.5"
      >
        {/* Glow backdrop */}
        <div
          style={{ background: `radial-gradient(circle, ${glowColor} 0%, #FFFFFF 100%)` }}
          className="w-full h-full rounded-[16px] overflow-hidden relative"
        >
          <ArtCanvasView
            pointsJson={route.pointsJson}
            blobsJson={route.blobsJson}
            artStyle={route.artStyle}
            brushStyleKey={route.brushStyleKey}
            stickersJson={route.landmarkStickersJson}
          />

          {/* Favorite heart */}
          <button
            onClick={(e) => {
              e.stopPropagation();
              onFavoriteToggle(e);
            }}
            className="absolute top-1 right-1 p-1 rounded-full bg-white/70 backdrop-blur-xs hover:bg-white transition-colors"
          >
            <Heart
              size={12}
              className={`${
                route.isFavorite ? 'fill-[#866FB3] text-[#866FB3]' : 'text-[#75857C]'
              }`}
            />
          </button>
        </div>
      </div>

      <span className="text-[12px] font-semibold text-[#16211B] mt-1.5">
        {formatDate(route.dateString)}
      </span>
    </div>
  );
};

interface ArtworkThumbnailCardProps {
  route: WalkRouteEntity;
  onClick: () => void;
  onFavoriteToggle: (e: React.MouseEvent) => void;
  onShareClick?: (e: React.MouseEvent) => void;
}

export const ArtworkThumbnailCard: React.FC<ArtworkThumbnailCardProps> = ({
  route,
  onClick,
  onFavoriteToggle,
  onShareClick,
}) => {
  return (
    <div
      onClick={onClick}
      className="bg-white rounded-[22px] border border-[#D8DFD5] p-3 cursor-pointer transition-all duration-300 hover:shadow-md hover:-translate-y-0.5 group flex flex-col justify-between"
    >
      {/* Artwork Canvas Aspect Box */}
      <div className="w-full aspect-[1/1] rounded-[18px] bg-[#F7F5EE] relative overflow-hidden p-2">
        <ArtCanvasView
          pointsJson={route.pointsJson}
          blobsJson={route.blobsJson}
          artStyle={route.artStyle}
          brushStyleKey={route.brushStyleKey}
          stickersJson={route.landmarkStickersJson}
        />

        {/* Top Floating Controls */}
        <div className="absolute top-2 left-2 right-2 flex justify-between items-center pointer-events-none">
          {onShareClick && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                onShareClick(e);
              }}
              className="pointer-events-auto p-1.5 rounded-full bg-white/90 shadow-xs hover:bg-white text-[#3A4D42] transition-colors"
              title="Share"
            >
              <Share2 size={13} />
            </button>
          )}

          <button
            onClick={(e) => {
              e.stopPropagation();
              onFavoriteToggle(e);
            }}
            className="pointer-events-auto p-1.5 rounded-full bg-white/90 shadow-xs hover:bg-white transition-colors ml-auto"
            title="Favorite"
          >
            <Heart
              size={14}
              className={`${
                route.isFavorite ? 'fill-[#866FB3] text-[#866FB3]' : 'text-[#75857C]'
              }`}
            />
          </button>
        </div>

        {/* Shape Badge bottom left */}
        <div className="absolute bottom-2 left-2 bg-white/85 backdrop-blur-xs px-2 py-0.5 rounded-full text-[10px] font-bold text-[#16211B] shadow-xs">
          {route.shapeCategory}
        </div>
      </div>

      {/* Info Row */}
      <div className="mt-3 flex items-center justify-between">
        <div>
          <h4 className="text-[14px] font-bold text-[#16211B] truncate max-w-[140px]">
            {route.title}
          </h4>
          <p className="text-[11px] text-[#75857C]">{route.dateString}</p>
        </div>
        <div className="text-right">
          <span className="text-[13px] font-bold text-[#56B386]">
            {route.distanceKm} km
          </span>
          <p className="text-[10px] text-[#75857C]">{route.durationMinutes} min</p>
        </div>
      </div>
    </div>
  );
};
