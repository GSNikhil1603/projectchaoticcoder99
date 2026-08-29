import React, { createContext, useContext, useState, useEffect } from 'react';
import {
  WalkRouteEntity,
  ChallengeEntity,
  BadgeEntity,
  StoreItemEntity,
  UserProfileEntity,
  CampusLandmark,
  CustomPigment,
  GpsCoordinate,
} from '../types';
import {
  INITIAL_ROUTES,
  INITIAL_CHALLENGES,
  INITIAL_BADGES,
  INITIAL_STORE_ITEMS,
  INITIAL_LANDMARKS,
  INITIAL_CUSTOM_PIGMENTS,
  INITIAL_USER_PROFILE,
} from '../data/sampleCampusData';
import { RouteArtEngine } from '../engine/routeArtEngine';

export type ScreenTab = 'home' | 'tracker' | 'map' | 'studio' | 'challenges' | 'store' | 'profile';

interface ActiveWalkState {
  isTracking: boolean;
  isPaused: boolean;
  startTime: number | null;
  elapsedSeconds: number;
  coordinates: GpsCoordinate[];
  distanceKm: number;
  currentElevationGain: number;
  stepCount: number;
  currentSpeedKmh: number;
  currentGradePercentage: number;
  caloriesBurned: number;
  selectedArtStyle: string;
  selectedBrushStyle: 'INK' | 'WATERCOLOR' | 'NEON' | 'CHALK';
}

interface AppContextType {
  // Navigation
  activeTab: ScreenTab;
  setActiveTab: (tab: ScreenTab) => void;

  // Data
  routes: WalkRouteEntity[];
  challenges: ChallengeEntity[];
  badges: BadgeEntity[];
  storeItems: StoreItemEntity[];
  landmarks: CampusLandmark[];
  customPigments: CustomPigment[];
  userProfile: UserProfileEntity;

  // Selected artwork for Studio
  selectedArtworkId: string | null;
  setSelectedArtworkId: (id: string | null) => void;

  // Actions
  toggleFavoriteRoute: (routeId: string) => void;
  deleteRoute: (routeId: string) => void;
  updateRouteArt: (routeId: string, updates: Partial<WalkRouteEntity>) => void;
  claimChallengeReward: (challengeId: string) => void;
  buyStoreItem: (itemId: string) => boolean;
  saveCustomPigment: (name: string, hexCode: string) => void;
  deleteCustomPigment: (pigmentId: string) => void;
  discoverLandmark: (landmarkId: string) => void;
  updateProfile: (name: string, studentId: string, hostel: string, department: string) => void;
  setTheme: (themeName: string) => void;
  addXp: (amount: number) => void;

  // Active Walk Engine
  activeWalk: ActiveWalkState;
  startWalk: (artStyle?: string, brushStyle?: 'INK' | 'WATERCOLOR' | 'NEON' | 'CHALK') => void;
  pauseWalk: () => void;
  resumeWalk: () => void;
  stopWalk: () => WalkRouteEntity | null;
  cancelWalk: () => void;
  addWalkCoordinate: (coord: GpsCoordinate) => void;
  simulateStep: () => void;

  // Toast / Status Message
  toastMessage: string | null;
  showToast: (msg: string) => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const AppProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [activeTab, setActiveTab] = useState<ScreenTab>('home');

  // Persistence Initializers
  const [routes, setRoutes] = useState<WalkRouteEntity[]>(() => {
    const saved = localStorage.getItem('pathcanvas_routes');
    return saved ? JSON.parse(saved) : INITIAL_ROUTES;
  });

  const [challenges, setChallenges] = useState<ChallengeEntity[]>(() => {
    const saved = localStorage.getItem('pathcanvas_challenges');
    return saved ? JSON.parse(saved) : INITIAL_CHALLENGES;
  });

  const [badges, setBadges] = useState<BadgeEntity[]>(() => {
    const saved = localStorage.getItem('pathcanvas_badges');
    return saved ? JSON.parse(saved) : INITIAL_BADGES;
  });

  const [storeItems, setStoreItems] = useState<StoreItemEntity[]>(() => {
    const saved = localStorage.getItem('pathcanvas_store_items');
    return saved ? JSON.parse(saved) : INITIAL_STORE_ITEMS;
  });

  const [landmarks, setLandmarks] = useState<CampusLandmark[]>(() => {
    const saved = localStorage.getItem('pathcanvas_landmarks');
    return saved ? JSON.parse(saved) : INITIAL_LANDMARKS;
  });

  const [customPigments, setCustomPigments] = useState<CustomPigment[]>(() => {
    const saved = localStorage.getItem('pathcanvas_custom_pigments');
    return saved ? JSON.parse(saved) : INITIAL_CUSTOM_PIGMENTS;
  });

  const [userProfile, setUserProfile] = useState<UserProfileEntity>(() => {
    const saved = localStorage.getItem('pathcanvas_user_profile');
    return saved ? JSON.parse(saved) : INITIAL_USER_PROFILE;
  });

  const [selectedArtworkId, setSelectedArtworkId] = useState<string | null>(routes[0]?.id || null);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  // Active Walk State
  const [activeWalk, setActiveWalk] = useState<ActiveWalkState>({
    isTracking: false,
    isPaused: false,
    startTime: null,
    elapsedSeconds: 0,
    coordinates: [],
    distanceKm: 0,
    currentElevationGain: 0,
    stepCount: 0,
    currentSpeedKmh: 0,
    currentGradePercentage: 0,
    caloriesBurned: 0,
    selectedArtStyle: 'Pastel Bloom',
    selectedBrushStyle: 'INK',
  });

  // Sync to localStorage
  useEffect(() => {
    localStorage.setItem('pathcanvas_routes', JSON.stringify(routes));
  }, [routes]);

  useEffect(() => {
    localStorage.setItem('pathcanvas_challenges', JSON.stringify(challenges));
  }, [challenges]);

  useEffect(() => {
    localStorage.setItem('pathcanvas_badges', JSON.stringify(badges));
  }, [badges]);

  useEffect(() => {
    localStorage.setItem('pathcanvas_store_items', JSON.stringify(storeItems));
  }, [storeItems]);

  useEffect(() => {
    localStorage.setItem('pathcanvas_landmarks', JSON.stringify(landmarks));
  }, [landmarks]);

  useEffect(() => {
    localStorage.setItem('pathcanvas_custom_pigments', JSON.stringify(customPigments));
  }, [customPigments]);

  useEffect(() => {
    localStorage.setItem('pathcanvas_user_profile', JSON.stringify(userProfile));
  }, [userProfile]);

  // Active Walk Timer interval
  useEffect(() => {
    let interval: any;
    if (activeWalk.isTracking && !activeWalk.isPaused) {
      interval = setInterval(() => {
        setActiveWalk(prev => ({
          ...prev,
          elapsedSeconds: prev.elapsedSeconds + 1,
        }));
      }, 1000);
    }
    return () => clearInterval(interval);
  }, [activeWalk.isTracking, activeWalk.isPaused]);

  const showToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage(prev => (prev === msg ? null : prev));
    }, 3200);
  };

  const addXp = (amount: number) => {
    setUserProfile(prev => {
      const newXp = prev.currentXp + amount;
      let nextLvl = prev.currentLevel;
      let rank = prev.explorerRank;

      if (newXp >= 2500) {
        nextLvl = 4;
        rank = 'Campus Cartographer';
      } else if (newXp >= 1500) {
        nextLvl = 3;
        rank = 'Trail Artisan';
      } else if (newXp >= 800) {
        nextLvl = 2;
        rank = 'Path Pioneer';
      }

      return {
        ...prev,
        currentXp: newXp,
        currentLevel: nextLvl,
        explorerRank: rank,
      };
    });
  };

  const toggleFavoriteRoute = (routeId: string) => {
    setRoutes(prev =>
      prev.map(r => (r.id === routeId ? { ...r, isFavorite: !r.isFavorite } : r))
    );
  };

  const deleteRoute = (routeId: string) => {
    setRoutes(prev => prev.filter(r => r.id !== routeId));
    setUserProfile(prev => ({
      ...prev,
      totalArtworksCount: Math.max(0, prev.totalArtworksCount - 1),
    }));
    showToast('Artwork deleted.');
  };

  const updateRouteArt = (routeId: string, updates: Partial<WalkRouteEntity>) => {
    setRoutes(prev =>
      prev.map(r => (r.id === routeId ? { ...r, ...updates } : r))
    );
    showToast('Artwork updated successfully ✨');
  };

  const claimChallengeReward = (challengeId: string) => {
    const ch = challenges.find(c => c.id === challengeId);
    if (!ch || !ch.isCompleted || ch.isClaimed) return;

    setChallenges(prev =>
      prev.map(c => (c.id === challengeId ? { ...c, isClaimed: true } : c))
    );

    setUserProfile(prev => ({
      ...prev,
      totalCoins: prev.totalCoins + ch.rewardCoins,
    }));

    addXp(100);
    showToast(`Claimed +${ch.rewardCoins} 🪙 coins & +100 XP! 🎉`);
  };

  const buyStoreItem = (itemId: string): boolean => {
    const item = storeItems.find(i => i.id === itemId);
    if (!item || item.isUnlocked) return false;

    if (userProfile.totalCoins < item.costCoins) {
      showToast(`Not enough coins! You need ${item.costCoins} 🪙`);
      return false;
    }

    setUserProfile(prev => ({
      ...prev,
      totalCoins: prev.totalCoins - item.costCoins,
    }));

    setStoreItems(prev =>
      prev.map(i => (i.id === itemId ? { ...i, isUnlocked: true } : i))
    );

    addXp(80);
    showToast(`Unlocked ${item.title}! ✨ (+80 XP)`);
    return true;
  };

  const saveCustomPigment = (name: string, hexCode: string) => {
    const newPigment: CustomPigment = {
      id: `pig_${Date.now()}`,
      name: name || `Custom Blend #${Math.floor(Math.random() * 900 + 100)}`,
      hexCode,
      category: 'Custom Pigment',
      createdAt: new Date().toLocaleDateString('en-GB', { day: 'numeric', month: 'short', year: 'numeric' }),
    };

    setCustomPigments(prev => [newPigment, ...prev]);
    addXp(50);

    // Check Master Colorist Badge
    if (customPigments.length + 1 >= 3) {
      setBadges(prev =>
        prev.map(b =>
          b.id === 'badge_03'
            ? { ...b, isUnlocked: true, unlockedDate: new Date().toLocaleDateString('en-GB', { day: 'numeric', month: 'short' }) }
            : b
        )
      );
    }

    showToast(`Saved pigment "${newPigment.name}"! (+50 XP) ✨`);
  };

  const deleteCustomPigment = (pigmentId: string) => {
    setCustomPigments(prev => prev.filter(p => p.id !== pigmentId));
    showToast('Pigment removed.');
  };

  const discoverLandmark = (landmarkId: string) => {
    const lm = landmarks.find(l => l.id === landmarkId);
    if (!lm || lm.isDiscovered) return;

    setLandmarks(prev =>
      prev.map(l => (l.id === landmarkId ? { ...l, isDiscovered: true } : l))
    );

    setUserProfile(prev => ({
      ...prev,
      totalCoins: prev.totalCoins + lm.pointsReward,
    }));

    // Update quest progress
    setChallenges(prev =>
      prev.map(c => {
        if (c.id === 'chal_04') {
          const nextVal = c.currentValue + 1;
          return {
            ...c,
            currentValue: nextVal,
            isCompleted: nextVal >= c.targetValue,
          };
        }
        return c;
      })
    );

    addXp(60);
    showToast(`Discovered ${lm.name}! (+${lm.pointsReward} 🪙 & +60 XP) 🏛️`);
  };

  const updateProfile = (name: string, studentId: string, hostel: string, department: string) => {
    setUserProfile(prev => ({
      ...prev,
      username: name,
      studentId,
      hostelBlock: hostel,
      department,
    }));
    showToast('Profile updated.');
  };

  const setTheme = (themeName: string) => {
    setUserProfile(prev => ({ ...prev, selectedTheme: themeName }));
    showToast(`Theme changed to ${themeName}`);
  };

  // Walk Tracker Methods
  const startWalk = (
    artStyle = 'Pastel Bloom',
    brushStyle: 'INK' | 'WATERCOLOR' | 'NEON' | 'CHALK' = 'INK'
  ) => {
    // Initial campus coordinate near Quad
    const initialCoord: GpsCoordinate = {
      lat: 12.9716 + (Math.random() - 0.5) * 0.0005,
      lng: 77.5946 + (Math.random() - 0.5) * 0.0005,
      altitude: 920,
      timestamp: Date.now(),
      speedMps: 1.3,
    };

    setActiveWalk({
      isTracking: true,
      isPaused: false,
      startTime: Date.now(),
      elapsedSeconds: 0,
      coordinates: [initialCoord],
      distanceKm: 0,
      currentElevationGain: 0,
      stepCount: 0,
      currentSpeedKmh: 4.8,
      currentGradePercentage: 0,
      caloriesBurned: 0,
      selectedArtStyle: artStyle,
      selectedBrushStyle: brushStyle,
    });
    showToast('Walking GPS tracker started. Move around to paint artwork! 🚶');
  };

  const pauseWalk = () => {
    setActiveWalk(prev => ({ ...prev, isPaused: true }));
    showToast('Tracking paused.');
  };

  const resumeWalk = () => {
    setActiveWalk(prev => ({ ...prev, isPaused: false }));
    showToast('Tracking resumed.');
  };

  const addWalkCoordinate = (coord: GpsCoordinate) => {
    setActiveWalk(prev => {
      if (!prev.isTracking || prev.isPaused) return prev;
      const lastCoord = prev.coordinates[prev.coordinates.length - 1];
      let addDist = 0;
      let elevGain = 0;
      let grade = 0;

      if (lastCoord) {
        addDist = RouteArtEngine.haversineDistance(lastCoord, coord);
        const diffElev = (coord.altitude ?? 0) - (lastCoord.altitude ?? 0);
        if (diffElev > 0) elevGain = diffElev;
        if (addDist > 0.001) {
          grade = (diffElev / (addDist * 1000)) * 100;
        }
      }

      const newDist = prev.distanceKm + addDist;
      const newSteps = prev.stepCount + Math.round(addDist * 1400);
      const newSpeed = (coord.speedMps ?? 1.3) * 3.6;
      const newCalories = Math.round(newDist * 65);

      return {
        ...prev,
        coordinates: [...prev.coordinates, coord],
        distanceKm: parseFloat(newDist.toFixed(2)),
        currentElevationGain: prev.currentElevationGain + elevGain,
        stepCount: newSteps,
        currentSpeedKmh: parseFloat(newSpeed.toFixed(1)),
        currentGradePercentage: parseFloat(grade.toFixed(1)),
        caloriesBurned: newCalories,
      };
    });
  };

  const simulateStep = () => {
    if (!activeWalk.isTracking || activeWalk.isPaused) return;

    const last = activeWalk.coordinates[activeWalk.coordinates.length - 1] || {
      lat: 12.9716,
      lng: 77.5946,
      altitude: 920,
    };

    // Realistic campus walking step vector with natural organic curvature
    const stepCount = activeWalk.coordinates.length;
    const angle = (stepCount * 0.35) + Math.sin(stepCount * 0.2) * 0.8;
    const dLat = Math.sin(angle) * 0.00035;
    const dLng = Math.cos(angle) * 0.00038;
    const elevDelta = (Math.sin(stepCount * 0.5) * 1.5) + (Math.random() > 0.5 ? 0.6 : -0.2);

    const nextCoord: GpsCoordinate = {
      lat: last.lat + dLat,
      lng: last.lng + dLng,
      altitude: (last.altitude ?? 920) + elevDelta,
      timestamp: Date.now(),
      speedMps: 1.2 + Math.random() * 0.4,
    };

    addWalkCoordinate(nextCoord);
  };

  const stopWalk = (): WalkRouteEntity | null => {
    if (!activeWalk.isTracking || activeWalk.coordinates.length < 2) {
      showToast('Walk too short to mint artwork. Walk a bit more!');
      return null;
    }

    const { points, blobs, shapeCategory, distanceKm, elevationGainMeters, svgPreview } =
      RouteArtEngine.processRoute(activeWalk.coordinates, activeWalk.selectedArtStyle);

    const durationMin = Math.max(1, Math.round(activeWalk.elapsedSeconds / 60));
    const now = new Date();
    const dateStr = now.toLocaleDateString('en-GB', { day: 'numeric', month: 'long', year: 'numeric' });

    const newRoute: WalkRouteEntity = {
      id: `route_${Date.now()}`,
      title: `${shapeCategory} Campus Walk (${now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })})`,
      dateString: dateStr,
      timestamp: Date.now(),
      distanceKm: Math.max(distanceKm, activeWalk.distanceKm),
      durationMinutes: durationMin,
      pointsJson: JSON.stringify(points),
      blobsJson: JSON.stringify(blobs),
      artStyle: activeWalk.selectedArtStyle,
      shapeCategory,
      isFavorite: false,
      syncStatus: 'SYNCED',
      previewSvg: svgPreview,
      stepCount: Math.max(activeWalk.stepCount, Math.round(distanceKm * 1400)),
      avgSpeedKmh: activeWalk.currentSpeedKmh || 4.8,
      elevationGainMeters: Math.max(elevationGainMeters, Math.round(activeWalk.currentElevationGain)),
      caloriesBurned: Math.max(activeWalk.caloriesBurned, Math.round(distanceKm * 65)),
      brushStyleKey: activeWalk.selectedBrushStyle,
      landmarkStickersJson: '[]',
    };

    setRoutes(prev => [newRoute, ...prev]);
    setSelectedArtworkId(newRoute.id);

    // Update Profile totals
    setUserProfile(prev => ({
      ...prev,
      totalArtworksCount: prev.totalArtworksCount + 1,
      distanceWalkedKm: parseFloat((prev.distanceWalkedKm + newRoute.distanceKm).toFixed(1)),
      totalSteps: prev.totalSteps + newRoute.stepCount,
      totalCoins: prev.totalCoins + Math.round(newRoute.distanceKm * 50) + 50,
    }));

    addXp(Math.round(newRoute.distanceKm * 100) + 120);

    // Reset Active Walk State
    setActiveWalk({
      isTracking: false,
      isPaused: false,
      startTime: null,
      elapsedSeconds: 0,
      coordinates: [],
      distanceKm: 0,
      currentElevationGain: 0,
      stepCount: 0,
      currentSpeedKmh: 0,
      currentGradePercentage: 0,
      caloriesBurned: 0,
      selectedArtStyle: 'Pastel Bloom',
      selectedBrushStyle: 'INK',
    });

    showToast(`Minted "${newRoute.shapeCategory}" artwork! (+${Math.round(newRoute.distanceKm * 50) + 50} 🪙 & +150 XP) 🎉`);
    return newRoute;
  };

  const cancelWalk = () => {
    setActiveWalk({
      isTracking: false,
      isPaused: false,
      startTime: null,
      elapsedSeconds: 0,
      coordinates: [],
      distanceKm: 0,
      currentElevationGain: 0,
      stepCount: 0,
      currentSpeedKmh: 0,
      currentGradePercentage: 0,
      caloriesBurned: 0,
      selectedArtStyle: 'Pastel Bloom',
      selectedBrushStyle: 'INK',
    });
    showToast('Walk cancelled.');
  };

  return (
    <AppContext.Provider
      value={{
        activeTab,
        setActiveTab,
        routes,
        challenges,
        badges,
        storeItems,
        landmarks,
        customPigments,
        userProfile,
        selectedArtworkId,
        setSelectedArtworkId,
        toggleFavoriteRoute,
        deleteRoute,
        updateRouteArt,
        claimChallengeReward,
        buyStoreItem,
        saveCustomPigment,
        deleteCustomPigment,
        discoverLandmark,
        updateProfile,
        setTheme,
        addXp,
        activeWalk,
        startWalk,
        pauseWalk,
        resumeWalk,
        stopWalk,
        cancelWalk,
        addWalkCoordinate,
        simulateStep,
        toastMessage,
        showToast,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) throw new Error('useApp must be used within an AppProvider');
  return context;
};
