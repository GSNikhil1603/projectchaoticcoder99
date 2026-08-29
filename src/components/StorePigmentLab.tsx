import React, { useState } from 'react';
import {
  ShoppingBag,
  Sparkles,
  Lock,
  Check,
  FlaskConical,
  Plus,
  Trash2,
  Brush,
  Palette,
} from 'lucide-react';
import { useApp } from '../context/AppContext';
import confetti from 'canvas-confetti';

export const StorePigmentLab: React.FC = () => {
  const {
    storeItems,
    buyStoreItem,
    userProfile,
    customPigments,
    saveCustomPigment,
    deleteCustomPigment,
    showToast,
  } = useApp();

  const [activeTab, setActiveTab] = useState<'store' | 'lab'>('store');

  // Pigment Lab interactive mixer sliders
  const [red, setRed] = useState(134);
  const [green, setGreen] = useState(111);
  const [blue, setBlue] = useState(179);
  const [pigmentName, setPigmentName] = useState('');

  const currentHex = `#${[red, green, blue]
    .map((x) => x.toString(16).padStart(2, '0'))
    .join('')}`.toUpperCase();

  const handleBuy = (id: string) => {
    const success = buyStoreItem(id);
    if (success) {
      confetti({ particleCount: 50, spread: 60, origin: { y: 0.6 } });
    }
  };

  const handleSavePigment = () => {
    saveCustomPigment(pigmentName.trim(), currentHex);
    setPigmentName('');
    confetti({ particleCount: 40, spread: 50, origin: { y: 0.6 } });
  };

  return (
    <div className="space-y-6 pb-24 max-w-4xl mx-auto px-4 sm:px-6 pt-4">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-[#16211B] font-display">
            Artisan Store & Color Alchemy Lab
          </h2>
          <p className="text-xs sm:text-sm text-[#75857C]">
            Unlock premium brushes and harmonic palettes with coins, or synthesize custom campus pigments.
          </p>
        </div>

        {/* Total Coins Pill */}
        <div className="flex items-center space-x-2 bg-[#FFECC7] border border-[#FFB74D]/50 px-4 py-2 rounded-2xl shadow-xs">
          <span className="text-base">🪙</span>
          <span className="text-sm font-bold text-[#16211B]">
            {userProfile.totalCoins.toLocaleString()} Coins
          </span>
        </div>
      </div>

      {/* Tab Switcher */}
      <div className="flex bg-[#F7F5EE] p-1.5 rounded-2xl border border-[#D8DFD5] max-w-xs">
        <button
          onClick={() => setActiveTab('store')}
          className={`flex-1 py-2 rounded-xl text-xs font-bold transition-all flex items-center justify-center space-x-1.5 ${
            activeTab === 'store'
              ? 'bg-white text-[#16211B] shadow-xs'
              : 'text-[#75857C] hover:text-[#16211B]'
          }`}
        >
          <ShoppingBag size={14} className="text-[#56B386]" />
          <span>Art Store</span>
        </button>

        <button
          onClick={() => setActiveTab('lab')}
          className={`flex-1 py-2 rounded-xl text-xs font-bold transition-all flex items-center justify-center space-x-1.5 ${
            activeTab === 'lab'
              ? 'bg-white text-[#16211B] shadow-xs'
              : 'text-[#75857C] hover:text-[#16211B]'
          }`}
        >
          <FlaskConical size={14} className="text-[#866FB3]" />
          <span>Pigment Lab 🧪</span>
        </button>
      </div>

      {/* 1. ART STORE VIEW */}
      {activeTab === 'store' && (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {storeItems.map((item) => (
            <div
              key={item.id}
              className="bg-white rounded-[24px] border border-[#D8DFD5] p-5 shadow-xs flex flex-col justify-between space-y-4 hover:shadow-sm transition-all"
            >
              <div className="flex items-start space-x-3.5">
                <div
                  style={{ backgroundColor: item.previewHex }}
                  className="w-12 h-12 rounded-2xl flex items-center justify-center shadow-xs flex-shrink-0 text-white"
                >
                  {item.itemType === 'OUTLINE' ? <Brush size={20} /> : <Palette size={20} />}
                </div>

                <div className="space-y-1">
                  <span className="text-[10px] font-bold uppercase tracking-wider text-[#56B386] bg-[#D7F5E4] px-2 py-0.5 rounded-full">
                    {item.itemType === 'OUTLINE' ? 'Brush Stroke' : 'Harmonic Palette'}
                  </span>
                  <h4 className="text-base font-bold text-[#16211B] font-display">
                    {item.title}
                  </h4>
                  <p className="text-xs text-[#75857C]">{item.description}</p>

                  {item.paletteColors && (
                    <div className="flex items-center space-x-1.5 pt-1.5">
                      {item.paletteColors.map((c, i) => (
                        <span
                          key={i}
                          style={{ backgroundColor: c }}
                          className="w-4 h-4 rounded-full border border-[#D8DFD5]"
                        />
                      ))}
                    </div>
                  )}
                </div>
              </div>

              {/* Purchase / Unlocked status */}
              <div className="pt-2 border-t border-[#D8DFD5] flex items-center justify-between">
                <div className="flex items-center space-x-1 font-bold text-sm text-[#16211B]">
                  <span>{item.costCoins}</span>
                  <span>🪙 Coins</span>
                </div>

                {item.isUnlocked ? (
                  <span className="inline-flex items-center space-x-1 text-xs font-bold text-[#56B386] bg-[#D7F5E4] px-3 py-1.5 rounded-2xl">
                    <Check size={14} />
                    <span>Unlocked</span>
                  </span>
                ) : (
                  <button
                    onClick={() => handleBuy(item.id)}
                    className="px-4 py-2 rounded-2xl bg-[#16211B] hover:bg-[#2B3B30] text-white font-bold text-xs shadow-xs transition-colors flex items-center space-x-1.5"
                  >
                    <Lock size={12} />
                    <span>Unlock</span>
                  </button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* 2. PIGMENT LAB (ALCHEMY WORKSHOP) */}
      {activeTab === 'lab' && (
        <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-start">
          {/* Interactive Synthesizer */}
          <div className="md:col-span-6 bg-white rounded-[28px] border border-[#D8DFD5] p-5 sm:p-6 shadow-sm space-y-5">
            <div className="flex items-center space-x-2">
              <FlaskConical size={20} className="text-[#866FB3]" />
              <h3 className="text-lg font-bold text-[#16211B] font-display">
                Pigment Synthesis Crucible
              </h3>
            </div>

            {/* Live Color Swatch Bubble */}
            <div className="w-full aspect-video rounded-2xl border border-[#D8DFD5] shadow-inner relative overflow-hidden flex flex-col items-center justify-center space-y-2" style={{ backgroundColor: currentHex }}>
              <div className="bg-white/90 backdrop-blur-xs px-3.5 py-1.5 rounded-full text-xs font-bold text-[#16211B] shadow-xs">
                HEX: {currentHex}
              </div>
              <span className="text-[11px] font-bold text-white drop-shadow-md">
                RGB({red}, {green}, {blue})
              </span>
            </div>

            {/* Sliders for Red, Green, Blue */}
            <div className="space-y-4 text-xs font-bold text-[#16211B]">
              <div>
                <div className="flex justify-between mb-1">
                  <span className="text-red-600">Red Channel</span>
                  <span>{red}</span>
                </div>
                <input
                  type="range"
                  min="0"
                  max="255"
                  value={red}
                  onChange={(e) => setRed(Number(e.target.value))}
                  className="w-full accent-red-500 cursor-pointer"
                />
              </div>

              <div>
                <div className="flex justify-between mb-1">
                  <span className="text-green-600">Green Channel</span>
                  <span>{green}</span>
                </div>
                <input
                  type="range"
                  min="0"
                  max="255"
                  value={green}
                  onChange={(e) => setGreen(Number(e.target.value))}
                  className="w-full accent-green-600 cursor-pointer"
                />
              </div>

              <div>
                <div className="flex justify-between mb-1">
                  <span className="text-blue-600">Blue Channel</span>
                  <span>{blue}</span>
                </div>
                <input
                  type="range"
                  min="0"
                  max="255"
                  value={blue}
                  onChange={(e) => setBlue(Number(e.target.value))}
                  className="w-full accent-blue-500 cursor-pointer"
                />
              </div>
            </div>

            {/* Name and Save */}
            <div className="pt-2 space-y-3">
              <input
                type="text"
                placeholder="Give your pigment a name (e.g. Science Hill Moss)"
                value={pigmentName}
                onChange={(e) => setPigmentName(e.target.value)}
                className="w-full px-4 py-2.5 rounded-2xl bg-[#F7F5EE] border border-[#D8DFD5] text-xs text-[#16211B] focus:outline-none focus:border-[#56B386]"
              />

              <button
                onClick={handleSavePigment}
                className="w-full py-3.5 rounded-2xl bg-[#866FB3] text-white font-bold text-xs sm:text-sm shadow-md hover:bg-[#765EA3] transition-all flex items-center justify-center space-x-2"
              >
                <Plus size={16} />
                <span>Save Pigment to Studio Palette (+50 XP) ✨</span>
              </button>
            </div>
          </div>

          {/* Saved Laboratory Pigments Collection */}
          <div className="md:col-span-6 space-y-4">
            <div className="bg-white rounded-[28px] border border-[#D8DFD5] p-5 shadow-sm space-y-3">
              <h3 className="text-base font-bold text-[#16211B] font-display">
                Your Laboratory Pigment Vault ({customPigments.length})
              </h3>
              <p className="text-xs text-[#75857C]">
                These custom pigments are readily available across the Coloring Studio!
              </p>

              {customPigments.length === 0 ? (
                <div className="bg-[#F7F5EE] rounded-2xl p-6 text-center text-xs text-[#75857C]">
                  No custom pigments synthesized yet. Mix colors above!
                </div>
              ) : (
                <div className="space-y-2.5 pt-2">
                  {customPigments.map((pig) => (
                    <div
                      key={pig.id}
                      className="p-3 rounded-2xl border border-[#D8DFD5] bg-[#F7F5EE] flex items-center justify-between"
                    >
                      <div className="flex items-center space-x-3">
                        <div
                          style={{ backgroundColor: pig.hexCode }}
                          className="w-9 h-9 rounded-full border border-white shadow-xs"
                        />
                        <div>
                          <h4 className="text-xs font-bold text-[#16211B]">{pig.name}</h4>
                          <span className="text-[10px] text-[#75857C]">{pig.hexCode} • {pig.createdAt}</span>
                        </div>
                      </div>

                      <button
                        onClick={() => deleteCustomPigment(pig.id)}
                        className="p-2 rounded-xl text-neutral-400 hover:text-red-500 hover:bg-white transition-colors"
                        title="Delete Pigment"
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
