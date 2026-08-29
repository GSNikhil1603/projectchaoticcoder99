import React, { useState } from 'react';
import {
  Trophy,
  CheckCircle2,
  Clock,
  Sparkles,
  Award,
  Users,
  Flame,
  ChevronRight,
  TrendingUp,
} from 'lucide-react';
import { useApp } from '../context/AppContext';
import { HOSTEL_LEADERBOARD, TOP_ARTISTS_LEADERBOARD } from '../data/sampleCampusData';
import confetti from 'canvas-confetti';

export const ChallengesLeaderboard: React.FC = () => {
  const { challenges, badges, claimChallengeReward, userProfile } = useApp();
  const [activeSubTab, setActiveSubTab] = useState<'quests' | 'badges' | 'leaderboard'>('quests');
  const [leaderboardType, setLeaderboardType] = useState<'hostels' | 'individuals'>('hostels');

  const handleClaim = (id: string) => {
    claimChallengeReward(id);
    confetti({ particleCount: 70, spread: 80, origin: { y: 0.6 } });
  };

  return (
    <div className="space-y-6 pb-24 max-w-4xl mx-auto px-4 sm:px-6 pt-4">
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-[#16211B] font-display">
            Campus Quests & Leaderboard
          </h2>
          <p className="text-xs sm:text-sm text-[#75857C]">
            Complete daily walking milestones, unlock collector badges, and lead your hostel wing.
          </p>
        </div>

        {/* Level badge */}
        <div className="flex items-center space-x-2 bg-[#D7F5E4] border border-[#56B386]/40 px-3.5 py-1.5 rounded-2xl shadow-xs">
          <Award size={16} className="text-[#56B386]" />
          <span className="text-xs font-bold text-[#1E4833]">
            Level {userProfile.currentLevel} • {userProfile.explorerRank}
          </span>
        </div>
      </div>

      {/* Segmented Sub Tabs */}
      <div className="flex bg-[#F7F5EE] p-1.5 rounded-2xl border border-[#D8DFD5] max-w-md">
        <button
          onClick={() => setActiveSubTab('quests')}
          className={`flex-1 py-2 rounded-xl text-xs font-bold transition-all flex items-center justify-center space-x-1.5 ${
            activeSubTab === 'quests'
              ? 'bg-white text-[#16211B] shadow-xs'
              : 'text-[#75857C] hover:text-[#16211B]'
          }`}
        >
          <Sparkles size={14} className="text-[#56B386]" />
          <span>Campus Quests</span>
        </button>

        <button
          onClick={() => setActiveSubTab('badges')}
          className={`flex-1 py-2 rounded-xl text-xs font-bold transition-all flex items-center justify-center space-x-1.5 ${
            activeSubTab === 'badges'
              ? 'bg-white text-[#16211B] shadow-xs'
              : 'text-[#75857C] hover:text-[#16211B]'
          }`}
        >
          <Award size={14} className="text-[#866FB3]" />
          <span>Badges ({badges.filter((b) => b.isUnlocked).length}/{badges.length})</span>
        </button>

        <button
          onClick={() => setActiveSubTab('leaderboard')}
          className={`flex-1 py-2 rounded-xl text-xs font-bold transition-all flex items-center justify-center space-x-1.5 ${
            activeSubTab === 'leaderboard'
              ? 'bg-white text-[#16211B] shadow-xs'
              : 'text-[#75857C] hover:text-[#16211B]'
          }`}
        >
          <Trophy size={14} className="text-[#FFB74D]" />
          <span>Leaderboard</span>
        </button>
      </div>

      {/* 1. QUESTS TAB */}
      {activeSubTab === 'quests' && (
        <div className="space-y-3.5">
          {challenges.map((chal) => {
            const pct = Math.min(100, Math.round((chal.currentValue / chal.targetValue) * 100));
            return (
              <div
                key={chal.id}
                className="bg-white rounded-[22px] border border-[#D8DFD5] p-4 sm:p-5 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4 transition-all hover:shadow-sm"
              >
                <div className="flex items-start space-x-3.5">
                  <div className="w-12 h-12 rounded-2xl bg-[#F7F5EE] border border-[#D8DFD5] flex items-center justify-center text-2xl flex-shrink-0">
                    {chal.iconEmoji}
                  </div>
                  <div className="space-y-1">
                    <div className="flex items-center space-x-2">
                      <span className="text-[10px] font-bold uppercase tracking-wider text-[#56B386] bg-[#D7F5E4] px-2 py-0.5 rounded-full">
                        {chal.category}
                      </span>
                      {chal.isCompleted && (
                        <span className="text-[10px] font-bold text-[#56B386] flex items-center space-x-0.5">
                          <CheckCircle2 size={11} />
                          <span>Complete</span>
                        </span>
                      )}
                    </div>
                    <h4 className="text-base font-bold text-[#16211B] font-display">
                      {chal.title}
                    </h4>
                    <p className="text-xs text-[#75857C] max-w-md">{chal.description}</p>

                    {/* Progress bar */}
                    <div className="pt-1.5 flex items-center space-x-3 max-w-xs">
                      <div className="flex-1 h-2 rounded-full bg-neutral-100 overflow-hidden">
                        <div
                          style={{ width: `${pct}%` }}
                          className={`h-full rounded-full transition-all duration-500 ${
                            chal.isCompleted ? 'bg-[#56B386]' : 'bg-[#866FB3]'
                          }`}
                        />
                      </div>
                      <span className="text-[11px] font-bold text-[#75857C] whitespace-nowrap">
                        {chal.currentValue} / {chal.targetValue}
                      </span>
                    </div>
                  </div>
                </div>

                {/* Reward Button */}
                <div className="sm:text-right flex-shrink-0">
                  {chal.isClaimed ? (
                    <span className="inline-flex items-center space-x-1 text-xs font-bold text-[#75857C] bg-neutral-100 px-3.5 py-2 rounded-2xl">
                      <CheckCircle2 size={14} className="text-[#56B386]" />
                      <span>Reward Claimed</span>
                    </span>
                  ) : chal.isCompleted ? (
                    <button
                      onClick={() => handleClaim(chal.id)}
                      className="px-4 py-2.5 rounded-2xl bg-[#56B386] text-white font-bold text-xs shadow-xs hover:bg-[#48A176] transition-all flex items-center space-x-1.5"
                    >
                      <Sparkles size={14} />
                      <span>Claim +{chal.rewardCoins} Coins 🪙</span>
                    </button>
                  ) : (
                    <span className="inline-flex items-center space-x-1 text-xs font-bold text-[#FFB74D] bg-[#FFECC7] px-3 py-1.5 rounded-2xl">
                      <span>+{chal.rewardCoins} 🪙</span>
                    </span>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* 2. BADGES TAB */}
      {activeSubTab === 'badges' && (
        <div className="grid grid-cols-2 sm:grid-cols-3 gap-3.5">
          {badges.map((badge) => (
            <div
              key={badge.id}
              className={`rounded-[24px] border p-4 sm:p-5 flex flex-col items-center text-center space-y-2.5 transition-all ${
                badge.isUnlocked
                  ? 'bg-white border-[#D8DFD5] shadow-xs hover:shadow-md'
                  : 'bg-neutral-50/70 border-dashed border-neutral-300 opacity-60'
              }`}
            >
              <div
                className={`w-16 h-16 rounded-full flex items-center justify-center text-3xl shadow-inner ${
                  badge.isUnlocked
                    ? 'bg-gradient-to-br from-[#D7F5E4] to-[#E2E6FF]'
                    : 'bg-neutral-200'
                }`}
              >
                {badge.isUnlocked ? badge.iconEmoji : '🔒'}
              </div>

              <div>
                <span className="text-[10px] font-bold uppercase tracking-wider text-[#75857C] block">
                  {badge.category}
                </span>
                <h4 className="text-sm font-bold text-[#16211B] mt-0.5">{badge.title}</h4>
                <p className="text-[11px] text-[#75857C] mt-1 line-clamp-2">
                  {badge.description}
                </p>
              </div>

              {badge.isUnlocked && badge.unlockedDate && (
                <span className="text-[10px] font-semibold text-[#56B386] bg-[#D7F5E4] px-2.5 py-0.5 rounded-full">
                  Unlocked {badge.unlockedDate}
                </span>
              )}
            </div>
          ))}
        </div>
      )}

      {/* 3. LEADERBOARD TAB */}
      {activeSubTab === 'leaderboard' && (
        <div className="space-y-4">
          {/* Sub Toggle: Hostels vs Top Walking Artists */}
          <div className="flex space-x-2">
            <button
              onClick={() => setLeaderboardType('hostels')}
              className={`px-4 py-2 rounded-2xl text-xs font-bold transition-all ${
                leaderboardType === 'hostels'
                  ? 'bg-[#16211B] text-white'
                  : 'bg-white border border-[#D8DFD5] text-[#16211B] hover:bg-neutral-50'
              }`}
            >
              🏢 Campus Hostel Blocks
            </button>
            <button
              onClick={() => setLeaderboardType('individuals')}
              className={`px-4 py-2 rounded-2xl text-xs font-bold transition-all ${
                leaderboardType === 'individuals'
                  ? 'bg-[#16211B] text-white'
                  : 'bg-white border border-[#D8DFD5] text-[#16211B] hover:bg-neutral-50'
              }`}
            >
              🏆 Top Walking Artists
            </button>
          </div>

          <div className="bg-white rounded-[24px] border border-[#D8DFD5] divide-y divide-[#D8DFD5] shadow-xs overflow-hidden">
            {(leaderboardType === 'hostels' ? HOSTEL_LEADERBOARD : TOP_ARTISTS_LEADERBOARD).map(
              (item) => (
                <div
                  key={item.id}
                  className={`p-4 flex items-center justify-between transition-colors ${
                    item.isCurrentUser ? 'bg-[#D7F5E4]/40 font-semibold' : 'hover:bg-neutral-50/60'
                  }`}
                >
                  <div className="flex items-center space-x-3.5">
                    {/* Rank Badge */}
                    <div
                      className={`w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold ${
                        item.rank === 1
                          ? 'bg-[#FFD166] text-[#16211B] shadow-xs'
                          : item.rank === 2
                          ? 'bg-[#D8DFD5] text-[#16211B]'
                          : item.rank === 3
                          ? 'bg-[#FFB74D] text-[#16211B]'
                          : 'bg-neutral-100 text-[#75857C]'
                      }`}
                    >
                      {item.rank}
                    </div>

                    <div className="w-10 h-10 rounded-2xl bg-[#F7F5EE] border border-[#D8DFD5] flex items-center justify-center text-xl flex-shrink-0">
                      {item.avatarEmoji}
                    </div>

                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="text-sm font-bold text-[#16211B]">{item.name}</span>
                        {item.isCurrentUser && (
                          <span className="text-[10px] font-bold bg-[#56B386] text-white px-2 py-0.2 rounded-full">
                            You
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-[#75857C]">{item.subtitle}</p>
                    </div>
                  </div>

                  <div className="text-right">
                    <span className="text-sm font-extrabold text-[#16211B] block">
                      {item.scoreText}
                    </span>
                  </div>
                </div>
              )
            )}
          </div>
        </div>
      )}
    </div>
  );
};
