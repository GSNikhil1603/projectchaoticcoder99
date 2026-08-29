import React, { useState } from 'react';
import {
  User,
  Award,
  Footprints,
  Sparkles,
  MapPin,
  Flame,
  Palette,
  CloudCheck,
  Edit3,
  Check,
  RefreshCw,
} from 'lucide-react';
import { useApp } from '../context/AppContext';
import confetti from 'canvas-confetti';

export const ProfileView: React.FC = () => {
  const { userProfile, updateProfile, setTheme, showToast, routes } = useApp();

  const [isEditing, setIsEditing] = useState(false);
  const [nameInput, setNameInput] = useState(userProfile.username);
  const [studentIdInput, setStudentIdInput] = useState(userProfile.studentId);
  const [hostelInput, setHostelInput] = useState(userProfile.hostelBlock);
  const [deptInput, setDeptInput] = useState(userProfile.department);
  const [isSyncing, setIsSyncing] = useState(false);

  const xpProgress = Math.min(100, Math.round(((userProfile.currentXp % 1000) / 1000) * 100));

  const themes = [
    { name: 'Classic Mint', color: '#56B386' },
    { name: 'Lavender Twilight', color: '#866FB3' },
    { name: 'Cyber Emerald', color: '#00F5D4' },
    { name: 'Sunset Ochre', color: '#FF8A65' },
  ];

  const handleSaveProfile = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfile(nameInput, studentIdInput, hostelInput, deptInput);
    setIsEditing(false);
  };

  const handleCloudSync = () => {
    setIsSyncing(true);
    setTimeout(() => {
      setIsSyncing(false);
      showToast('All artworks & profile synced to Campus Cloud! ☁️✨');
      confetti({ particleCount: 40, spread: 60, origin: { y: 0.7 } });
    }, 1200);
  };

  return (
    <div className="space-y-6 pb-24 max-w-4xl mx-auto px-4 sm:px-6 pt-4">
      {/* Profile Header Card */}
      <div className="bg-white rounded-[28px] border border-[#D8DFD5] p-5 sm:p-6 shadow-sm flex flex-col sm:flex-row items-start sm:items-center justify-between gap-5">
        <div className="flex items-center space-x-4">
          <div className="w-16 h-16 rounded-full bg-gradient-to-tr from-[#56B386] to-[#866FB3] flex items-center justify-center text-white text-2xl font-bold shadow-md">
            {userProfile.username ? userProfile.username.charAt(0).toUpperCase() : 'N'}
          </div>

          <div className="space-y-1">
            <div className="flex items-center space-x-2">
              <h2 className="text-xl sm:text-2xl font-bold text-[#16211B] font-display">
                {userProfile.username}
              </h2>
              <span className="text-[11px] font-bold text-[#56B386] bg-[#D7F5E4] px-2.5 py-0.5 rounded-full">
                ID: {userProfile.studentId}
              </span>
            </div>
            <p className="text-xs text-[#75857C]">
              {userProfile.hostelBlock} • {userProfile.department}
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-2 w-full sm:w-auto">
          <button
            onClick={() => setIsEditing(true)}
            className="flex-1 sm:flex-initial flex items-center justify-center space-x-1.5 px-4 py-2.5 rounded-2xl bg-[#F7F5EE] border border-[#D8DFD5] text-xs font-bold text-[#16211B] hover:bg-neutral-100 transition-colors"
          >
            <Edit3 size={14} />
            <span>Edit Info</span>
          </button>

          <button
            onClick={handleCloudSync}
            disabled={isSyncing}
            className="flex items-center justify-center space-x-1.5 px-4 py-2.5 rounded-2xl bg-[#16211B] text-white text-xs font-bold shadow-xs hover:bg-[#2B3B30] transition-colors"
          >
            <RefreshCw size={14} className={isSyncing ? 'animate-spin' : ''} />
            <span>{isSyncing ? 'Syncing...' : 'Sync Cloud'}</span>
          </button>
        </div>
      </div>

      {/* Explorer Rank & Level XP Card */}
      <div
        style={{
          background: 'linear-gradient(135deg, #FFECC7 0%, #D7F5E4 100%)',
        }}
        className="rounded-[28px] border border-[#D8DFD5] p-5 sm:p-6 shadow-xs space-y-3"
      >
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-2.5">
            <Award size={22} className="text-[#16211B]" />
            <div>
              <span className="text-xs font-bold uppercase tracking-wider text-[#75857C]">
                Level {userProfile.currentLevel} Rank
              </span>
              <h3 className="text-lg sm:text-xl font-bold text-[#16211B] font-display">
                {userProfile.explorerRank}
              </h3>
            </div>
          </div>

          <div className="text-right">
            <span className="text-base font-extrabold text-[#16211B]">
              {userProfile.currentXp} XP
            </span>
            <span className="text-[10px] text-[#75857C] block">Lifetime Score</span>
          </div>
        </div>

        {/* Level XP Progress Bar */}
        <div className="space-y-1 pt-1">
          <div className="h-3 rounded-full bg-white/70 p-0.5 overflow-hidden">
            <div
              style={{ width: `${xpProgress}%` }}
              className="h-full rounded-full bg-[#56B386] transition-all duration-500"
            />
          </div>
          <div className="flex justify-between text-[10px] font-bold text-[#3A4D42]">
            <span>Next Level: {1000 - (userProfile.currentXp % 1000)} XP remaining</span>
            <span>{xpProgress}%</span>
          </div>
        </div>
      </div>

      {/* Lifetime Stats Matrix */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-3.5">
        <div className="bg-white rounded-2xl p-4 border border-[#D8DFD5] text-center shadow-xs">
          <MapPin size={20} className="mx-auto text-[#56B386] mb-1.5" />
          <span className="text-xl font-bold text-[#16211B] block">
            {userProfile.distanceWalkedKm} km
          </span>
          <span className="text-xs text-[#75857C]">Distance Walked</span>
        </div>

        <div className="bg-white rounded-2xl p-4 border border-[#D8DFD5] text-center shadow-xs">
          <Palette size={20} className="mx-auto text-[#866FB3] mb-1.5" />
          <span className="text-xl font-bold text-[#16211B] block">
            {routes.length}
          </span>
          <span className="text-xs text-[#75857C]">Artworks Minted</span>
        </div>

        <div className="bg-white rounded-2xl p-4 border border-[#D8DFD5] text-center shadow-xs">
          <Footprints size={20} className="mx-auto text-[#319795] mb-1.5" />
          <span className="text-xl font-bold text-[#16211B] block">
            {userProfile.totalSteps.toLocaleString()}
          </span>
          <span className="text-xs text-[#75857C]">Campus Steps</span>
        </div>

        <div className="bg-white rounded-2xl p-4 border border-[#D8DFD5] text-center shadow-xs">
          <span className="text-2xl block mb-1">🪙</span>
          <span className="text-xl font-bold text-[#16211B] block">
            {userProfile.totalCoins.toLocaleString()}
          </span>
          <span className="text-xs text-[#75857C]">Coins Balance</span>
        </div>
      </div>

      {/* Aesthetic Theme Switcher */}
      <div className="bg-white rounded-[24px] border border-[#D8DFD5] p-5 shadow-xs space-y-3">
        <h3 className="text-base font-bold text-[#16211B] font-display">
          Campus App Theme & Interface Tint
        </h3>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5">
          {themes.map((th) => {
            const isSelected = userProfile.selectedTheme === th.name;
            return (
              <button
                key={th.name}
                onClick={() => setTheme(th.name)}
                className={`p-3 rounded-2xl border text-xs font-bold transition-all flex items-center justify-between ${
                  isSelected
                    ? 'border-[#16211B] bg-[#F7F5EE] text-[#16211B] ring-2 ring-[#56B386]'
                    : 'border-[#D8DFD5] bg-white text-[#75857C] hover:bg-neutral-50'
                }`}
              >
                <div className="flex items-center space-x-2">
                  <span
                    style={{ backgroundColor: th.color }}
                    className="w-3.5 h-3.5 rounded-full shadow-xs"
                  />
                  <span>{th.name}</span>
                </div>
                {isSelected && <Check size={14} className="text-[#56B386]" />}
              </button>
            );
          })}
        </div>
      </div>

      {/* Edit Profile Modal */}
      {isEditing && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <form
            onSubmit={handleSaveProfile}
            className="bg-[#F7F5EE] max-w-md w-full rounded-[28px] border border-[#D8DFD5] p-6 space-y-4 shadow-2xl"
          >
            <h3 className="text-xl font-bold text-[#16211B] font-display">
              Edit Student Profile
            </h3>

            <div className="space-y-3 text-xs font-bold text-[#16211B]">
              <div>
                <label className="block mb-1 text-[#75857C]">Display Name</label>
                <input
                  type="text"
                  value={nameInput}
                  onChange={(e) => setNameInput(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-2xl bg-white border border-[#D8DFD5] focus:outline-none focus:border-[#56B386]"
                  required
                />
              </div>

              <div>
                <label className="block mb-1 text-[#75857C]">Student ID / Roll No</label>
                <input
                  type="text"
                  value={studentIdInput}
                  onChange={(e) => setStudentIdInput(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-2xl bg-white border border-[#D8DFD5] focus:outline-none focus:border-[#56B386]"
                />
              </div>

              <div>
                <label className="block mb-1 text-[#75857C]">Hostel Block & Wing</label>
                <input
                  type="text"
                  value={hostelInput}
                  onChange={(e) => setHostelInput(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-2xl bg-white border border-[#D8DFD5] focus:outline-none focus:border-[#56B386]"
                />
              </div>

              <div>
                <label className="block mb-1 text-[#75857C]">Academic Department</label>
                <input
                  type="text"
                  value={deptInput}
                  onChange={(e) => setDeptInput(e.target.value)}
                  className="w-full px-3.5 py-2.5 rounded-2xl bg-white border border-[#D8DFD5] focus:outline-none focus:border-[#56B386]"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 pt-3">
              <button
                type="button"
                onClick={() => setIsEditing(false)}
                className="py-3 rounded-2xl bg-white border border-[#D8DFD5] font-bold text-xs text-[#16211B] hover:bg-neutral-50"
              >
                Cancel
              </button>

              <button
                type="submit"
                className="py-3 rounded-2xl bg-[#56B386] font-bold text-xs text-white shadow-xs hover:bg-[#48A176]"
              >
                Save Profile
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};
