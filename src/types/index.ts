export interface GpsCoordinate {
  lat: number;
  lng: number;
  altitude?: number;
  timestamp?: number;
  speedMps?: number;
}

export interface PointF {
  x: number;
  y: number;
  elevation: number;
  gradePercentage: number;
  strokeThicknessMultiplier: number;
  speedMps: number;
}

export interface ColorBlob {
  id?: string;
  x: number;
  y: number;
  radiusX: number;
  radiusY: number;
  colorHex: string;
  opacity: number;
  rotation: number;
  layer: number;
}

export interface LandmarkSticker {
  id: string;
  name: string;
  iconEmoji: string;
  x: number;
  y: number;
}

export interface WalkRouteEntity {
  id: string;
  title: string;
  dateString: string;
  timestamp: number;
  distanceKm: number;
  durationMinutes: number;
  pointsJson: string;
  blobsJson: string;
  artStyle: string;
  shapeCategory: string;
  isFavorite: boolean;
  syncStatus: string;
  previewSvg: string;
  stepCount: number;
  avgSpeedKmh: number;
  elevationGainMeters: number;
  caloriesBurned: number;
  brushStyleKey: 'INK' | 'WATERCOLOR' | 'NEON' | 'CHALK';
  landmarkStickersJson: string;
  customPaletteHex?: string;
}

export interface ChallengeEntity {
  id: string;
  title: string;
  description: string;
  rewardCoins: number;
  targetValue: number;
  currentValue: number;
  isCompleted: boolean;
  isClaimed: boolean;
  iconEmoji: string;
  category: string;
}

export interface BadgeEntity {
  id: string;
  title: string;
  description: string;
  iconEmoji: string;
  isUnlocked: boolean;
  unlockedDate: string | null;
  category: string;
}

export interface StoreItemEntity {
  id: string;
  title: string;
  description: string;
  costCoins: number;
  itemType: 'PALETTE' | 'OUTLINE' | 'SPECIAL';
  isUnlocked: boolean;
  previewHex: string;
  paletteColors?: string[];
}

export interface CustomPigment {
  id: string;
  name: string;
  hexCode: string;
  category: string;
  createdAt: string;
}

export interface UserProfileEntity {
  id: string;
  username: string;
  totalCoins: number;
  explorerRank: string;
  currentLevel: number;
  currentXp: number;
  distanceWalkedKm: number;
  totalArtworksCount: number;
  totalSteps: number;
  studentId: string;
  hostelBlock: string;
  department: string;
  selectedTheme: string;
}

export interface CampusLandmark {
  id: string;
  name: string;
  category: string;
  description: string;
  xPercent: number; // For interactive visual map
  yPercent: number;
  lat: number;
  lng: number;
  iconEmoji: string;
  isDiscovered: boolean;
  pointsReward: number;
}

export interface LeaderboardItem {
  id: string;
  rank: number;
  name: string;
  subtitle: string;
  scoreText: string;
  avatarEmoji: string;
  isCurrentUser: boolean;
}
