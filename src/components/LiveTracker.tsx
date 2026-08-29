import React, { useState, useEffect } from 'react';
import {
  Play,
  Pause,
  Square,
  Sparkles,
  Footprints,
  Clock,
  TrendingUp,
  Flame,
  Gauge,
  Compass,
  Layers,
  CheckCircle2,
  RefreshCw,
} from 'lucide-react';
import { useApp } from '../context/AppContext';
import { ArtCanvasView } from './ArtCanvasView';
import { ART_PALETTES, RouteArtEngine } from '../engine/routeArtEngine';
import confetti from 'canvas-confetti';

export const LiveTracker: React.FC = () => {
  const {
    activeWalk,
    startWalk,
    pauseWalk,
    resumeWalk,
    stopWalk,
    cancelWalk,
    addWalkCoordinate,
    simulateStep,
    showToast,
    setActiveTab,
  } = useApp();

  const [selectedArtStyle, setSelectedArtStyle] = useState('Pastel Bloom');
  const [selectedBrush, setSelectedBrush] = useState<'INK' | 'WATERCOLOR' | 'NEON' | 'CHALK'>('INK');
  const [gpsActive, setGpsActive] = useState(false);
  const [showFinishSuccessModal, setShowFinishSuccessModal] = useState(false);
  const [mintedResult, setMintedResult] = useState<any>(null);

  // Browser Geolocation integration
  useEffect(() => {
    let watchId: number | null = null;
    if (activeWalk.isTracking && !activeWalk.isPaused && navigator.geolocation) {
      setGpsActive(true);
      watchId = navigator.geolocation.watchPosition(
        (position) => {
          addWalkCoordinate({
            lat: position.coords.latitude,
            lng: position.coords.longitude,
            altitude: position.coords.altitude ?? 920,
            timestamp: position.timestamp,
            speedMps: position.coords.speed ?? 1.3,
          });
        },
        (err) => {
          console.log('Browser geolocation status / fallback:', err.message);
          setGpsActive(false);
        },
        { enableHighAccuracy: true, timeout: 10000, maximumAge: 2000 }
      );
    } else {
      setGpsActive(false);
    }

    return () => {
      if (watchId !== null) navigator.geolocation.clearWatch(watchId);
    };
  }, [activeWalk.isTracking, activeWalk.isPaused]);

  // Live art points and blobs calculation for active preview
  const liveArt = React.useMemo(() => {
    if (activeWalk.coordinates.length < 2) {
      const fallbackPoints = RouteArtEngine.generateFallbackPath(800);
      const fallbackBlobs = RouteArtEngine.generateWatercolorBlobs(fallbackPoints, selectedArtStyle, 800);
      return {
        pointsJson: JSON.stringify(fallbackPoints),
        blobsJson: JSON.stringify(fallbackBlobs),
        shapeCategory: 'Loop',
      };
    }

    const { points, blobs, shapeCategory } = RouteArtEngine.processRoute(
      activeWalk.coordinates,
      selectedArtStyle
    );

    return {
      pointsJson: JSON.stringify(points),
      blobsJson: JSON.stringify(blobs),
      shapeCategory,
    };
  }, [activeWalk.coordinates, selectedArtStyle]);

  const handleStart = () => {
    startWalk(selectedArtStyle, selectedBrush);
  };

  const handleFinish = () => {
    const route = stopWalk();
    if (route) {
      setMintedResult(route);
      setShowFinishSuccessModal(true);
      confetti({
        particleCount: 100,
        spread: 80,
        origin: { y: 0.6 },
      });
    }
  };

  const formatTimer = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  // Preset Campus Walk Simulators for quick testing/exploration
  const loadPresetRoute = (presetName: string) => {
    if (!activeWalk.isTracking) {
      startWalk(selectedArtStyle, selectedBrush);
    }

    const presets: Record<string, Array<{ lat: number; lng: number; altitude: number }>> = {
      spiral: [
        { lat: 12.9716, lng: 77.5946, altitude: 920 },
        { lat: 12.9722, lng: 77.5955, altitude: 924 },
        { lat: 12.9730, lng: 77.5950, altitude: 928 },
        { lat: 12.9735, lng: 77.5938, altitude: 932 },
        { lat: 12.9728, lng: 77.5925, altitude: 925 },
        { lat: 12.9719, lng: 77.5932, altitude: 921 },
        { lat: 12.9716, lng: 77.5946, altitude: 920 },
      ],
      hill: [
        { lat: 12.9750, lng: 77.5900, altitude: 915 },
        { lat: 12.9765, lng: 77.5915, altitude: 925 },
        { lat: 12.9780, lng: 77.5920, altitude: 936 },
        { lat: 12.9795, lng: 77.5910, altitude: 948 },
        { lat: 12.9810, lng: 77.5935, altitude: 955 },
      ],
      loop: [
        { lat: 12.9700, lng: 77.6000, altitude: 900 },
        { lat: 12.9715, lng: 77.6015, altitude: 905 },
        { lat: 12.9725, lng: 77.6005, altitude: 910 },
        { lat: 12.9720, lng: 77.5990, altitude: 908 },
        { lat: 12.9705, lng: 77.5985, altitude: 902 },
        { lat: 12.9695, lng: 77.6005, altitude: 900 },
      ],
    };

    const targetCoords = presets[presetName] || presets.spiral;
    targetCoords.forEach((c) => {
      addWalkCoordinate({ ...c, timestamp: Date.now(), speedMps: 1.4 });
    });

    showToast(`Loaded ${presetName.toUpperCase()} campus walk pattern! 🚶‍♂️`);
  };

  return (
    <div className="space-y-6 pb-24 max-w-4xl mx-auto px-4 sm:px-6 pt-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-[#16211B] font-display">
            Live Walking GPS Tracker
          </h2>
          <p className="text-xs sm:text-sm text-[#75857C]">
            Walk across campus to generate real-time generative spline art.
          </p>
        </div>

        {/* GPS Live Status Indicator */}
        <div
          className={`flex items-center space-x-1.5 px-3 py-1.5 rounded-full text-xs font-semibold ${
            activeWalk.isTracking
              ? 'bg-[#D7F5E4] text-[#1E4833]'
              : 'bg-white border border-[#D8DFD5] text-[#75857C]'
          }`}
        >
          <span
            className={`w-2 h-2 rounded-full ${
              activeWalk.isTracking ? 'bg-[#56B386] animate-pulse' : 'bg-[#75857C]'
            }`}
          />
          <span>{activeWalk.isTracking ? (gpsActive ? 'GPS Live' : 'Simulating Walk') : 'GPS Standby'}</span>
        </div>
      </div>

      {/* Main Split Layout: Live Art Canvas (Left) & Controls/Stats HUD (Right) */}
      <div className="grid grid-cols-1 md:grid-cols-12 gap-6 items-start">
        {/* Live Art Canvas Screen */}
        <div className="md:col-span-6 bg-white rounded-[28px] border border-[#D8DFD5] p-5 shadow-sm relative overflow-hidden flex flex-col items-center justify-center">
          <div className="w-full aspect-square rounded-[20px] bg-[#F7F5EE] border border-[#D8DFD5] relative overflow-hidden p-3 shadow-inner">
            <ArtCanvasView
              pointsJson={liveArt.pointsJson}
              blobsJson={liveArt.blobsJson}
              artStyle={selectedArtStyle}
              brushStyleKey={selectedBrush}
            />

            {/* Shape Category Live Label */}
            <div className="absolute top-3 left-3 bg-white/90 backdrop-blur-xs px-3 py-1 rounded-full text-xs font-bold text-[#16211B] shadow-xs border border-[#D8DFD5] flex items-center space-x-1.5">
              <Sparkles size={12} className="text-[#56B386]" />
              <span>Shape: {liveArt.shapeCategory}</span>
            </div>

            {/* Incline Indicator */}
            {activeWalk.isTracking && (
              <div className="absolute bottom-3 left-3 bg-white/90 backdrop-blur-xs px-3 py-1 rounded-full text-xs font-bold text-[#16211B] shadow-xs border border-[#D8DFD5]">
                Grade: {activeWalk.currentGradePercentage}%{' '}
                {activeWalk.currentGradePercentage > 2
                  ? '🧗 Uphill'
                  : activeWalk.currentGradePercentage < -2
                  ? '🏃 Downhill'
                  : '🌿 Flat'}
              </div>
            )}
          </div>

          <p className="text-[11px] text-[#75857C] text-center mt-3">
            {activeWalk.isTracking
              ? 'Splines dynamically swell with physical elevation incline'
              : 'Start tracking or simulate walk steps to generate geometry'}
          </p>
        </div>

        {/* Controls & Realtime HUD */}
        <div className="md:col-span-6 space-y-4">
          {/* Main Primary Action Buttons */}
          <div className="bg-white rounded-[24px] border border-[#D8DFD5] p-5 shadow-sm space-y-4">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-[#75857C] uppercase tracking-wider">
                Session Controls
              </span>
              <span className="text-xl font-bold font-mono text-[#16211B]">
                {formatTimer(activeWalk.elapsedSeconds)}
              </span>
            </div>

            {!activeWalk.isTracking ? (
              <button
                onClick={handleStart}
                className="w-full py-4 rounded-2xl bg-[#56B386] text-white font-bold text-base shadow-md hover:bg-[#48A176] transition-all flex items-center justify-center space-x-2"
              >
                <Play size={20} fill="white" />
                <span>Start Walking & Record Art</span>
              </button>
            ) : (
              <div className="grid grid-cols-2 gap-3">
                {activeWalk.isPaused ? (
                  <button
                    onClick={resumeWalk}
                    className="py-3.5 rounded-2xl bg-[#56B386] text-white font-bold text-sm shadow-xs hover:bg-[#48A176] transition-all flex items-center justify-center space-x-1.5"
                  >
                    <Play size={17} fill="white" />
                    <span>Resume</span>
                  </button>
                ) : (
                  <button
                    onClick={pauseWalk}
                    className="py-3.5 rounded-2xl bg-[#FFB74D] text-[#16211B] font-bold text-sm shadow-xs hover:bg-[#FFA726] transition-all flex items-center justify-center space-x-1.5"
                  >
                    <Pause size={17} />
                    <span>Pause</span>
                  </button>
                )}

                <button
                  onClick={handleFinish}
                  className="py-3.5 rounded-2xl bg-[#16211B] text-white font-bold text-sm shadow-xs hover:bg-[#2A3B30] transition-all flex items-center justify-center space-x-1.5"
                >
                  <Square size={16} fill="white" />
                  <span>Finish & Mint</span>
                </button>

                <button
                  onClick={cancelWalk}
                  className="col-span-2 py-2 rounded-xl text-xs text-red-600 hover:bg-red-50 transition-colors font-semibold text-center"
                >
                  Cancel Walk
                </button>
              </div>
            )}

            {/* Quick Step Simulator Button (for testing and prototyping) */}
            {activeWalk.isTracking && (
              <div className="pt-2 border-t border-[#D8DFD5] flex items-center justify-between gap-2">
                <button
                  onClick={simulateStep}
                  className="flex-1 py-2.5 px-3 rounded-xl bg-[#E2E6FF] hover:bg-[#D0D7FF] text-[#383387] text-xs font-bold transition-colors flex items-center justify-center space-x-1.5"
                >
                  <Footprints size={15} />
                  <span>Take Step (+GPS Point)</span>
                </button>

                <button
                  onClick={() => {
                    for (let i = 0; i < 5; i++) simulateStep();
                  }}
                  className="py-2.5 px-3 rounded-xl bg-neutral-100 hover:bg-neutral-200 text-[#16211B] text-xs font-bold transition-colors"
                >
                  +5 Steps
                </button>
              </div>
            )}
          </div>

          {/* Stats HUD Matrix */}
          <div className="grid grid-cols-3 gap-3">
            <div className="bg-white rounded-2xl p-3.5 border border-[#D8DFD5] text-center">
              <span className="text-[10px] uppercase font-bold text-[#75857C] block mb-1">
                Distance
              </span>
              <span className="text-lg font-bold text-[#56B386]">
                {activeWalk.distanceKm} km
              </span>
            </div>

            <div className="bg-white rounded-2xl p-3.5 border border-[#D8DFD5] text-center">
              <span className="text-[10px] uppercase font-bold text-[#75857C] block mb-1">
                Steps
              </span>
              <span className="text-lg font-bold text-[#866FB3]">
                {activeWalk.stepCount}
              </span>
            </div>

            <div className="bg-white rounded-2xl p-3.5 border border-[#D8DFD5] text-center">
              <span className="text-[10px] uppercase font-bold text-[#75857C] block mb-1">
                Speed
              </span>
              <span className="text-lg font-bold text-[#319795]">
                {activeWalk.currentSpeedKmh} km/h
              </span>
            </div>

            <div className="bg-white rounded-2xl p-3.5 border border-[#D8DFD5] text-center">
              <span className="text-[10px] uppercase font-bold text-[#75857C] block mb-1">
                Elevation
              </span>
              <span className="text-lg font-bold text-[#FF8A65]">
                +{activeWalk.currentElevationGain} m
              </span>
            </div>

            <div className="bg-white rounded-2xl p-3.5 border border-[#D8DFD5] text-center">
              <span className="text-[10px] uppercase font-bold text-[#75857C] block mb-1">
                Calories
              </span>
              <span className="text-lg font-bold text-[#E53E3E]">
                {activeWalk.caloriesBurned} kcal
              </span>
            </div>

            <div className="bg-white rounded-2xl p-3.5 border border-[#D8DFD5] text-center">
              <span className="text-[10px] uppercase font-bold text-[#75857C] block mb-1">
                Points
              </span>
              <span className="text-lg font-bold text-[#16211B]">
                {activeWalk.coordinates.length}
              </span>
            </div>
          </div>

          {/* Palette & Brush Selection */}
          <div className="bg-white rounded-[24px] border border-[#D8DFD5] p-5 shadow-sm space-y-4">
            <div>
              <label className="text-xs font-bold text-[#16211B] block mb-2">
                Active Color Palette
              </label>
              <div className="flex flex-wrap gap-2">
                {Object.keys(ART_PALETTES).map((paletteName) => (
                  <button
                    key={paletteName}
                    onClick={() => setSelectedArtStyle(paletteName)}
                    className={`px-3 py-1.5 rounded-full text-xs font-semibold transition-all flex items-center space-x-1.5 ${
                      selectedArtStyle === paletteName
                        ? 'bg-[#16211B] text-white shadow-xs'
                        : 'bg-[#F7F5EE] border border-[#D8DFD5] text-[#16211B] hover:bg-neutral-100'
                    }`}
                  >
                    <span
                      className="w-2.5 h-2.5 rounded-full"
                      style={{ backgroundColor: ART_PALETTES[paletteName][0] }}
                    />
                    <span>{paletteName}</span>
                  </button>
                ))}
              </div>
            </div>

            <div>
              <label className="text-xs font-bold text-[#16211B] block mb-2">
                Brush & Stroke Style
              </label>
              <div className="grid grid-cols-4 gap-2 text-xs">
                {(['INK', 'WATERCOLOR', 'NEON', 'CHALK'] as const).map((b) => (
                  <button
                    key={b}
                    onClick={() => setSelectedBrush(b)}
                    className={`py-2 px-1 rounded-xl font-bold transition-all text-center ${
                      selectedBrush === b
                        ? 'bg-[#56B386] text-white shadow-xs'
                        : 'bg-[#F7F5EE] border border-[#D8DFD5] text-[#16211B] hover:bg-neutral-100'
                    }`}
                  >
                    {b}
                  </button>
                ))}
              </div>
            </div>

            {/* Preset Walk Simulations */}
            <div className="pt-2 border-t border-[#D8DFD5]">
              <label className="text-xs font-bold text-[#75857C] block mb-2">
                Simulate Campus Route Presets:
              </label>
              <div className="grid grid-cols-3 gap-2">
                <button
                  onClick={() => loadPresetRoute('spiral')}
                  className="py-2 px-2 bg-[#F7F5EE] hover:bg-[#E8EFE5] rounded-xl text-xs font-semibold text-[#16211B] border border-[#D8DFD5]"
                >
                  🌸 Quad Spiral
                </button>
                <button
                  onClick={() => loadPresetRoute('hill')}
                  className="py-2 px-2 bg-[#F7F5EE] hover:bg-[#E8EFE5] rounded-xl text-xs font-semibold text-[#16211B] border border-[#D8DFD5]"
                >
                  ⛰️ Science Hill
                </button>
                <button
                  onClick={() => loadPresetRoute('loop')}
                  className="py-2 px-2 bg-[#F7F5EE] hover:bg-[#E8EFE5] rounded-xl text-xs font-semibold text-[#16211B] border border-[#D8DFD5]"
                >
                  🔄 Garden Loop
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Mint Success Modal */}
      {showFinishSuccessModal && mintedResult && (
        <div className="fixed inset-0 z-50 bg-black/60 backdrop-blur-sm flex items-center justify-center p-4">
          <div className="bg-[#F7F5EE] max-w-md w-full rounded-[28px] border border-[#D8DFD5] p-6 text-center space-y-4 shadow-2xl animate-in fade-in zoom-in duration-200">
            <div className="w-14 h-14 mx-auto rounded-full bg-[#D7F5E4] flex items-center justify-center text-[#56B386]">
              <CheckCircle2 size={32} />
            </div>

            <div>
              <span className="text-xs font-bold uppercase tracking-wider text-[#56B386]">
                Artwork Minted Successfully!
              </span>
              <h3 className="text-2xl font-extrabold text-[#16211B] font-display mt-1">
                {mintedResult.title}
              </h3>
              <p className="text-xs text-[#75857C]">
                Classified as a {mintedResult.shapeCategory} geometric spline archetype.
              </p>
            </div>

            <div className="w-48 h-48 mx-auto rounded-2xl bg-white border border-[#D8DFD5] p-2 shadow-sm">
              <ArtCanvasView
                pointsJson={mintedResult.pointsJson}
                blobsJson={mintedResult.blobsJson}
                artStyle={mintedResult.artStyle}
                brushStyleKey={mintedResult.brushStyleKey}
              />
            </div>

            <div className="bg-[#FFECC7] rounded-2xl p-3 text-xs font-bold text-[#16211B] flex items-center justify-around">
              <span>+150 XP Earned ⭐</span>
              <span>+{Math.round(mintedResult.distanceKm * 50) + 50} Coins 🪙</span>
            </div>

            <div className="grid grid-cols-2 gap-3 pt-2">
              <button
                onClick={() => {
                  setShowFinishSuccessModal(false);
                  setActiveTab('home');
                }}
                className="py-3 rounded-2xl bg-white border border-[#D8DFD5] font-bold text-xs text-[#16211B] hover:bg-neutral-50"
              >
                View in Gallery
              </button>

              <button
                onClick={() => {
                  setShowFinishSuccessModal(false);
                  setActiveTab('studio');
                }}
                className="py-3 rounded-2xl bg-[#56B386] font-bold text-xs text-white shadow-xs hover:bg-[#48A176]"
              >
                Customize in Studio 🎨
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
