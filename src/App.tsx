import React from 'react';
import { AppProvider, useApp } from './context/AppContext';
import { Navbar } from './components/Navbar';
import { HomeView } from './components/HomeView';
import { LiveTracker } from './components/LiveTracker';
import { CampusMap } from './components/CampusMap';
import { ColoringStudio } from './components/ColoringStudio';
import { ChallengesLeaderboard } from './components/ChallengesLeaderboard';
import { StorePigmentLab } from './components/StorePigmentLab';
import { ProfileView } from './components/ProfileView';

const MainContent: React.FC = () => {
  const { activeTab, toastMessage } = useApp();

  return (
    <div className="min-h-screen bg-[#F7F5EE] text-[#16211B] flex flex-col font-sans">
      <Navbar />

      {/* Main View Transition Area */}
      <main className="flex-1 w-full max-w-5xl mx-auto">
        {activeTab === 'home' && <HomeView />}
        {activeTab === 'tracker' && <LiveTracker />}
        {activeTab === 'map' && <CampusMap />}
        {activeTab === 'studio' && <ColoringStudio />}
        {activeTab === 'challenges' && <ChallengesLeaderboard />}
        {activeTab === 'store' && <StorePigmentLab />}
        {activeTab === 'profile' && <ProfileView />}
      </main>

      {/* Global Toast Notification */}
      {toastMessage && (
        <div className="fixed bottom-20 md:bottom-6 left-1/2 -translate-x-1/2 z-50 bg-[#16211B] text-white px-5 py-2.5 rounded-2xl shadow-xl border border-[#3A4D42] text-xs sm:text-sm font-semibold flex items-center space-x-2 animate-in fade-in slide-in-from-bottom-2">
          <span>{toastMessage}</span>
        </div>
      )}
    </div>
  );
};

export const App: React.FC = () => {
  return (
    <AppProvider>
      <MainContent />
    </AppProvider>
  );
};

export default App;
