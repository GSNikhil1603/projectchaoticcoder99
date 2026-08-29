import React from 'react';
import {
  Compass,
  Footprints,
  MapPin,
  Palette,
  Trophy,
  ShoppingBag,
  User,
  Sparkles,
} from 'lucide-react';
import { useApp, ScreenTab } from '../context/AppContext';

export const Navbar: React.FC = () => {
  const { activeTab, setActiveTab, userProfile } = useApp();

  const navItems: Array<{ id: ScreenTab; label: string; icon: React.ReactNode }> = [
    { id: 'home', label: 'Gallery', icon: <Compass size={19} /> },
    { id: 'tracker', label: 'Walk Track', icon: <Footprints size={19} /> },
    { id: 'map', label: 'Campus Map', icon: <MapPin size={19} /> },
    { id: 'studio', label: 'Studio', icon: <Palette size={19} /> },
    { id: 'challenges', label: 'Quests', icon: <Trophy size={19} /> },
    { id: 'store', label: 'Store', icon: <ShoppingBag size={19} /> },
    { id: 'profile', label: 'Profile', icon: <User size={19} /> },
  ];

  return (
    <>
      {/* Top Header App Bar */}
      <header className="sticky top-0 z-40 bg-[#F7F5EE]/90 backdrop-blur-md border-b border-[#D8DFD5] px-4 sm:px-6 py-3">
        <div className="max-w-5xl mx-auto flex items-center justify-between">
          {/* Logo & Brand */}
          <div
            onClick={() => setActiveTab('home')}
            className="flex items-center space-x-2.5 cursor-pointer group"
          >
            <div className="w-8 h-8 rounded-xl bg-gradient-to-tr from-[#56B386] to-[#866FB3] flex items-center justify-center text-white shadow-xs group-hover:scale-105 transition-transform">
              <Sparkles size={16} />
            </div>
            <div>
              <h1 className="text-base font-extrabold text-[#16211B] font-display leading-tight tracking-tight">
                PathCanvas
              </h1>
              <p className="text-[10px] text-[#75857C] font-semibold">Campus Route-to-Art</p>
            </div>
          </div>

          {/* Desktop Navigation Links */}
          <nav className="hidden md:flex items-center space-x-1 bg-white/70 p-1 rounded-2xl border border-[#D8DFD5]">
            {navItems.map((item) => {
              const isActive = activeTab === item.id;
              return (
                <button
                  key={item.id}
                  onClick={() => setActiveTab(item.id)}
                  className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-xl text-xs font-bold transition-all ${
                    isActive
                      ? 'bg-[#16211B] text-white shadow-xs'
                      : 'text-[#75857C] hover:text-[#16211B] hover:bg-neutral-100/60'
                  }`}
                >
                  {item.icon}
                  <span>{item.label}</span>
                </button>
              );
            })}
          </nav>

          {/* Header Right Coin Pill */}
          <div
            onClick={() => setActiveTab('store')}
            className="flex items-center space-x-1.5 bg-white border border-[#D8DFD5] hover:border-[#FFB74D] px-3 py-1.5 rounded-full text-xs font-bold text-[#16211B] cursor-pointer shadow-xs transition-colors"
          >
            <span>🪙</span>
            <span>{userProfile.totalCoins.toLocaleString()}</span>
          </div>
        </div>
      </header>

      {/* Bottom Floating Mobile Navigation Bar */}
      <div className="md:hidden fixed bottom-3 left-3 right-3 z-40">
        <div className="bg-white/95 backdrop-blur-md rounded-[24px] border border-[#D8DFD5] shadow-lg p-2 flex items-center justify-around">
          {navItems.map((item) => {
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                className={`flex flex-col items-center justify-center p-1.5 rounded-xl transition-all ${
                  isActive ? 'text-[#56B386] scale-110 font-bold' : 'text-[#75857C] hover:text-[#16211B]'
                }`}
              >
                {item.icon}
                <span className="text-[9px] mt-0.5">{item.label}</span>
              </button>
            );
          })}
        </div>
      </div>
    </>
  );
};
