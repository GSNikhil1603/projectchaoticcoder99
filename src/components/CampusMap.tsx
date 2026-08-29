import React, { useState } from 'react';
import {
  MapPin,
  Sparkles,
  CheckCircle2,
  Layers,
  Compass,
  Info,
  Navigation,
  ExternalLink,
} from 'lucide-react';
import { useApp } from '../context/AppContext';
import { CampusLandmark } from '../types';
import confetti from 'canvas-confetti';

export const CampusMap: React.FC = () => {
  const { landmarks, routes, discoverLandmark, showToast, setActiveTab } = useApp();
  const [selectedLandmark, setSelectedLandmark] = useState<CampusLandmark | null>(landmarks[0] || null);
  const [selectedCategory, setSelectedCategory] = useState<string>('ALL');
  const [showRouteOverlays, setShowRouteOverlays] = useState(true);

  const categories = ['ALL', 'Academic', 'Historic', 'Nature', 'Research', 'Athletics', 'Residential'];

  const filteredLandmarks = landmarks.filter((lm) => {
    if (selectedCategory !== 'ALL' && lm.category !== selectedCategory) return false;
    return true;
  });

  const handleCheckIn = (lm: CampusLandmark) => {
    discoverLandmark(lm.id);
    setSelectedLandmark({ ...lm, isDiscovered: true });
    confetti({ particleCount: 50, spread: 60, origin: { y: 0.6 } });
  };

  return (
    <div className="space-y-6 pb-24 max-w-4xl mx-auto px-4 sm:px-6 pt-4">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-[#16211B] font-display">
            Interactive Campus Map
          </h2>
          <p className="text-xs sm:text-sm text-[#75857C]">
            Explore student landmarks, check in to claim coin rewards, and view route art trails.
          </p>
        </div>

        {/* Route Overlay Toggle */}
        <button
          onClick={() => setShowRouteOverlays(!showRouteOverlays)}
          className={`flex items-center space-x-2 px-3.5 py-2 rounded-2xl text-xs font-bold transition-all ${
            showRouteOverlays
              ? 'bg-[#56B386] text-white shadow-xs'
              : 'bg-white border border-[#D8DFD5] text-[#16211B] hover:bg-neutral-50'
          }`}
        >
          <Layers size={15} />
          <span>{showRouteOverlays ? 'Route Trails Visible' : 'Show Art Trails'}</span>
        </button>
      </div>

      {/* Category Filter Chips */}
      <div className="flex items-center space-x-2 overflow-x-auto pb-1 text-xs no-scrollbar">
        {categories.map((cat) => (
          <button
            key={cat}
            onClick={() => setSelectedCategory(cat)}
            className={`px-3.5 py-1.5 rounded-full font-semibold transition-colors flex-shrink-0 ${
              selectedCategory === cat
                ? 'bg-[#16211B] text-white'
                : 'bg-white border border-[#D8DFD5] text-[#16211B] hover:bg-neutral-50'
            }`}
          >
            {cat === 'ALL' ? 'All Landmarks' : cat}
          </button>
        ))}
      </div>

      {/* Interactive Visual Map Viewport */}
      <div className="bg-white rounded-[28px] border border-[#D8DFD5] p-4 shadow-sm relative overflow-hidden">
        <div
          style={{
            background: 'radial-gradient(ellipse at center, #EBF5EE 0%, #DCECE0 100%)',
            height: '420px',
          }}
          className="w-full rounded-[22px] border border-[#D8DFD5] relative overflow-hidden select-none"
        >
          {/* Subtle Campus Grid & Blueprint lines */}
          <div className="absolute inset-0 opacity-20 pointer-events-none bg-[radial-gradient(#56B386_1px,transparent_1px)] [background-size:24px_24px]" />

          {/* Campus Map Zones & Path Walkways */}
          <svg className="absolute inset-0 w-full h-full pointer-events-none" xmlns="http://www.w3.org/2000/svg">
            {/* Campus Central Ring Walkway */}
            <circle cx="50%" cy="50%" r="35%" fill="none" stroke="#C5DACB" strokeWidth="18" strokeDasharray="6,6" opacity="0.6" />
            <path d="M 20% 75% Q 45% 45% 75% 30%" fill="none" stroke="#C5DACB" strokeWidth="14" opacity="0.5" />
            <path d="M 30% 25% Q 55% 65% 85% 60%" fill="none" stroke="#C5DACB" strokeWidth="12" opacity="0.5" />

            {/* Overlaid Route Trails if enabled */}
            {showRouteOverlays &&
              routes.map((r, i) => (
                <path
                  key={r.id}
                  d={`M ${30 + (i * 12)}% ${35 + (i * 10)}% Q ${50 + (i * 5)}% ${40 + (i * 8)}% ${65 - (i * 8)}% ${60 + (i * 5)}%`}
                  fill="none"
                  stroke={i % 2 === 0 ? '#56B386' : '#866FB3'}
                  strokeWidth="3.5"
                  strokeLinecap="round"
                  opacity="0.85"
                />
              ))}
          </svg>

          {/* Landmark Interactive Pins */}
          {filteredLandmarks.map((lm) => {
            const isSelected = selectedLandmark?.id === lm.id;
            return (
              <button
                key={lm.id}
                onClick={() => setSelectedLandmark(lm)}
                style={{
                  left: `${lm.xPercent}%`,
                  top: `${lm.yPercent}%`,
                  transform: 'translate(-50%, -50%)',
                }}
                className={`absolute transition-all duration-300 z-10 flex flex-col items-center group cursor-pointer ${
                  isSelected ? 'scale-125 z-20' : 'hover:scale-110'
                }`}
              >
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center shadow-md border-2 transition-all ${
                    isSelected
                      ? 'bg-[#16211B] text-white border-[#56B386] shadow-lg'
                      : lm.isDiscovered
                      ? 'bg-white text-[#16211B] border-[#56B386]'
                      : 'bg-neutral-100 text-neutral-400 border-neutral-300 opacity-80'
                  }`}
                >
                  <span className="text-lg">{lm.iconEmoji}</span>
                </div>

                <span
                  className={`text-[10px] font-bold px-2 py-0.5 rounded-full mt-1 whitespace-nowrap shadow-xs ${
                    isSelected
                      ? 'bg-[#16211B] text-white'
                      : 'bg-white/90 backdrop-blur-xs text-[#16211B] border border-[#D8DFD5]'
                  }`}
                >
                  {lm.name.split(' ')[0]}
                </span>
              </button>
            );
          })}

          {/* Map Compass Rose */}
          <div className="absolute top-3 right-3 bg-white/85 backdrop-blur-xs p-2 rounded-2xl border border-[#D8DFD5] shadow-xs flex items-center space-x-1 text-[11px] font-bold text-[#16211B]">
            <Compass size={14} className="text-[#56B386]" />
            <span>Campus North</span>
          </div>
        </div>
      </div>

      {/* Selected Landmark Details Card */}
      {selectedLandmark && (
        <div className="bg-white rounded-[24px] border border-[#D8DFD5] p-5 shadow-sm space-y-4">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div className="flex items-center space-x-3">
              <div className="w-12 h-12 rounded-2xl bg-[#D7F5E4] flex items-center justify-center text-2xl flex-shrink-0">
                {selectedLandmark.iconEmoji}
              </div>
              <div>
                <div className="flex items-center space-x-2">
                  <span className="text-xs font-bold uppercase tracking-wider text-[#56B386]">
                    {selectedLandmark.category} Zone
                  </span>
                  {selectedLandmark.isDiscovered && (
                    <span className="inline-flex items-center space-x-1 text-[10px] font-bold bg-[#D7F5E4] text-[#1E4833] px-2 py-0.5 rounded-full">
                      <CheckCircle2 size={11} />
                      <span>Discovered</span>
                    </span>
                  )}
                </div>
                <h3 className="text-lg font-bold text-[#16211B] font-display mt-0.5">
                  {selectedLandmark.name}
                </h3>
              </div>
            </div>

            {/* Check-In / Claim Button */}
            {!selectedLandmark.isDiscovered ? (
              <button
                onClick={() => handleCheckIn(selectedLandmark)}
                className="px-5 py-3 rounded-2xl bg-[#56B386] text-white font-bold text-xs sm:text-sm shadow-sm hover:bg-[#48A176] transition-all flex items-center justify-center space-x-2"
              >
                <Sparkles size={16} />
                <span>Check In (+{selectedLandmark.pointsReward} 🪙 & 60 XP)</span>
              </button>
            ) : (
              <div className="flex items-center space-x-2">
                <button
                  onClick={() => {
                    setActiveTab('tracker');
                    showToast(`Starting walk towards ${selectedLandmark.name}! 🚶`);
                  }}
                  className="px-4 py-2.5 rounded-2xl bg-[#E2E6FF] text-[#383387] font-bold text-xs hover:bg-[#D0D7FF] transition-colors flex items-center space-x-1.5"
                >
                  <Navigation size={14} />
                  <span>Walk Here</span>
                </button>
              </div>
            )}
          </div>

          <p className="text-xs sm:text-sm text-[#3A4D42] bg-[#F7F5EE] p-3.5 rounded-2xl border border-[#D8DFD5]">
            {selectedLandmark.description}
          </p>

          <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 text-xs">
            <div className="p-2.5 rounded-xl bg-neutral-50 border border-[#D8DFD5] text-center">
              <span className="text-[#75857C] block text-[10px]">Location</span>
              <span className="font-bold text-[#16211B]">
                {selectedLandmark.lat.toFixed(4)}, {selectedLandmark.lng.toFixed(4)}
              </span>
            </div>
            <div className="p-2.5 rounded-xl bg-neutral-50 border border-[#D8DFD5] text-center">
              <span className="text-[#75857C] block text-[10px]">Reward</span>
              <span className="font-bold text-[#FFB74D]">+{selectedLandmark.pointsReward} Coins 🪙</span>
            </div>
            <div className="p-2.5 rounded-xl bg-neutral-50 border border-[#D8DFD5] text-center">
              <span className="text-[#75857C] block text-[10px]">Sticker Reward</span>
              <span className="font-bold text-[#16211B]">Sticker Unlocked ✨</span>
            </div>
            <div className="p-2.5 rounded-xl bg-neutral-50 border border-[#D8DFD5] text-center">
              <span className="text-[#75857C] block text-[10px]">Status</span>
              <span className={`font-bold ${selectedLandmark.isDiscovered ? 'text-[#56B386]' : 'text-[#75857C]'}`}>
                {selectedLandmark.isDiscovered ? 'Visited ✓' : 'Undiscovered 🔒'}
              </span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
