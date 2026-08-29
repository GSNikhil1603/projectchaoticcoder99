import React, { useState } from 'react';
import { Sparkles, Plus, Search, Filter, Compass } from 'lucide-react';
import { useApp } from '../context/AppContext';
import { CreationCardItem, ArtworkThumbnailCard } from './ArtworkCard';
import { ArtworkDetailModal } from './ArtworkDetailModal';
import { WalkRouteEntity } from '../types';

export const HomeView: React.FC = () => {
  const {
    routes,
    userProfile,
    setActiveTab,
    toggleFavoriteRoute,
    setSelectedArtworkId,
    showToast,
  } = useApp();

  const [searchQuery, setSearchQuery] = useState('');
  const [filterFavoriteOnly, setFilterFavoriteOnly] = useState(false);
  const [selectedStyleFilter, setSelectedStyleFilter] = useState('ALL');
  const [activeModalArtwork, setActiveModalArtwork] = useState<WalkRouteEntity | null>(null);

  const filteredRoutes = routes.filter((r) => {
    if (filterFavoriteOnly && !r.isFavorite) return false;
    if (selectedStyleFilter !== 'ALL' && r.artStyle !== selectedStyleFilter) return false;
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      return (
        r.title.toLowerCase().includes(q) ||
        r.shapeCategory.toLowerCase().includes(q) ||
        r.artStyle.toLowerCase().includes(q) ||
        r.dateString.toLowerCase().includes(q)
      );
    }
    return true;
  });

  const uniqueStyles = Array.from(new Set(routes.map((r) => r.artStyle)));

  return (
    <div className="space-y-6 pb-20 max-w-4xl mx-auto px-4 sm:px-6 pt-4">
      {/* Top Banner Greeting */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <div className="flex items-center space-x-2">
            <span className="text-2xl font-extrabold text-[#16211B] font-display">
              Hi {userProfile.username || 'Neeraj'}
            </span>
            <span className="text-xl">✨</span>
          </div>
          <p className="text-sm text-[#75857C]">Live. Move. Create. Transform your campus walks into generative art.</p>
        </div>

        {/* Total Coins Pill */}
        <div className="flex items-center space-x-2 bg-[#FFECC7] border border-[#FFB74D]/50 px-4 py-2 rounded-2xl shadow-xs">
          <span className="text-base">🪙</span>
          <span className="text-sm font-bold text-[#16211B]">
            {userProfile.totalCoins.toLocaleString()} Coins
          </span>
        </div>
      </div>

      {/* Hero Quick-Start Walking Action Card */}
      <div
        style={{
          background: 'linear-gradient(135deg, #D7F5E4 0%, #E2E6FF 100%)',
        }}
        className="rounded-[28px] border border-[#D8DFD5] p-5 sm:p-6 shadow-xs relative overflow-hidden flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4"
      >
        <div className="space-y-1.5 max-w-md">
          <div className="inline-flex items-center space-x-1.5 bg-white/80 px-2.5 py-0.5 rounded-full text-xs font-bold text-[#1E4833]">
            <Sparkles size={13} className="text-[#56B386]" />
            <span>Generative GPS Engine Ready</span>
          </div>
          <h3 className="text-xl font-bold text-[#16211B] font-display">
            Start a New Walk & Paint Artwork
          </h3>
          <p className="text-xs sm:text-sm text-[#3A4D42]">
            Track your steps, conquer elevation inclines, and generate collectible watercolor splines in real time.
          </p>
        </div>

        <button
          onClick={() => setActiveTab('tracker')}
          className="flex items-center space-x-2 px-5 py-3.5 rounded-2xl bg-[#16211B] text-white font-bold text-sm shadow-md hover:bg-[#2C3B32] transition-all hover:scale-102 flex-shrink-0"
        >
          <Plus size={18} className="text-[#56B386]" />
          <span>Start Campus Walk</span>
        </button>
      </div>

      {/* Section 1: Recent Creations Carousel */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="text-lg font-bold text-[#16211B] font-display">
            Your Recent Creations
          </h3>
          <span className="text-xs text-[#75857C]">
            {routes.length} Artworks Total
          </span>
        </div>

        {routes.length === 0 ? (
          <div className="bg-white rounded-2xl border border-[#D8DFD5] p-6 text-center text-sm text-[#75857C]">
            No artworks yet. Take a campus walk to mint your first piece!
          </div>
        ) : (
          <div className="flex items-center space-x-4 overflow-x-auto py-2 px-1 no-scrollbar">
            {routes.slice(0, 10).map((route, idx) => (
              <CreationCardItem
                key={route.id}
                route={route}
                index={idx}
                onClick={() => setActiveModalArtwork(route)}
                onFavoriteToggle={() => toggleFavoriteRoute(route.id)}
              />
            ))}
          </div>
        )}
      </div>

      {/* Section 2: Explore & Filter Artworks Collection */}
      <div className="space-y-4 pt-2">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <h3 className="text-lg font-bold text-[#16211B] font-display">
            Artworks Collection
          </h3>

          {/* Search Bar */}
          <div className="relative flex-1 max-w-xs">
            <Search
              size={15}
              className="absolute left-3.5 top-1/2 -translate-y-1/2 text-[#75857C]"
            />
            <input
              type="text"
              placeholder="Search by title, shape, style..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-9 pr-4 py-2 rounded-2xl bg-white border border-[#D8DFD5] text-xs focus:outline-none focus:border-[#56B386] transition-colors"
            />
          </div>
        </div>

        {/* Filter Chips */}
        <div className="flex items-center space-x-2 overflow-x-auto pb-1 text-xs">
          <button
            onClick={() => setFilterFavoriteOnly(!filterFavoriteOnly)}
            className={`px-3.5 py-1.5 rounded-full font-semibold transition-colors flex items-center space-x-1.5 flex-shrink-0 ${
              filterFavoriteOnly
                ? 'bg-[#866FB3] text-white'
                : 'bg-white border border-[#D8DFD5] text-[#16211B] hover:bg-neutral-50'
            }`}
          >
            <span>Favorites</span>
            <span>❤️</span>
          </button>

          <button
            onClick={() => setSelectedStyleFilter('ALL')}
            className={`px-3.5 py-1.5 rounded-full font-semibold transition-colors flex-shrink-0 ${
              selectedStyleFilter === 'ALL'
                ? 'bg-[#16211B] text-white'
                : 'bg-white border border-[#D8DFD5] text-[#16211B] hover:bg-neutral-50'
            }`}
          >
            All Styles
          </button>

          {uniqueStyles.map((style) => (
            <button
              key={style}
              onClick={() => setSelectedStyleFilter(style)}
              className={`px-3.5 py-1.5 rounded-full font-semibold transition-colors flex-shrink-0 ${
                selectedStyleFilter === style
                  ? 'bg-[#56B386] text-white'
                  : 'bg-white border border-[#D8DFD5] text-[#16211B] hover:bg-neutral-50'
              }`}
            >
              {style}
            </button>
          ))}
        </div>

        {/* Gallery Grid */}
        {filteredRoutes.length === 0 ? (
          <div className="bg-white rounded-[24px] border border-[#D8DFD5] p-10 text-center space-y-3">
            <Compass size={36} className="mx-auto text-[#75857C]" />
            <h4 className="text-base font-bold text-[#16211B]">No matching artworks found</h4>
            <p className="text-xs text-[#75857C] max-w-xs mx-auto">
              Try adjusting your search query or style filter, or start a new walk.
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3.5">
            {filteredRoutes.map((route) => (
              <ArtworkThumbnailCard
                key={route.id}
                route={route}
                onClick={() => setActiveModalArtwork(route)}
                onFavoriteToggle={() => toggleFavoriteRoute(route.id)}
                onShareClick={() => {
                  navigator.clipboard.writeText(
                    `Check out my Campus Art "${route.title}" (${route.distanceKm} km, ${route.shapeCategory} style) minted on PathCanvas! 🎨`
                  );
                  showToast('Share summary copied to clipboard! 📋');
                }}
              />
            ))}
          </div>
        )}
      </div>

      {/* Modal */}
      {activeModalArtwork && (
        <ArtworkDetailModal
          artwork={activeModalArtwork}
          onClose={() => setActiveModalArtwork(null)}
          onOpenInStudio={(id) => {
            setSelectedArtworkId(id);
            setActiveTab('studio');
          }}
        />
      )}
    </div>
  );
};
