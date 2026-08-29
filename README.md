# Campus Route-to-Art (Android)

Gamifies daily campus walking by algorithmically converting GPS routes into collectible digital generative artwork. Built with modern Android architecture using Kotlin and Jetpack Compose.

## Key Features

- **Algorithmic Route-to-Art Engine**: Converts walking routes into smooth generative vector paths with organic background color blobs and campus milestone markers.
- **Continuous Live Tracker**: Real-time walking session recorder with GPS path tracing, step counting, distance, pace, and instant artwork generation upon finishing.
- **Interactive Coloring Studio**: Customize generated route artworks with unlockable color palettes, stroke styles, background pigment effects, and watermark captions.
- **Interactive Campus Map**: Interactive map with real campus landmarks (SJT, TT, PRP, Food Street, Lake, etc.) and preset art walk challenges.
- **Campus Quests & Leaderboard**: Complete walking challenges, earn pigment crystals, climb the student walker leaderboard, and unlock achievements.
- **Pigment Lab & Store**: Unlock new artist color schemes (Neon Pulse, Matcha Tea, Sunset Glow, Cherry Blossom, Minimalist Noir) using earned walk crystals.
- **Offline-First Persistence**: Powered by Room database for storing walked routes, personal stats, and custom colorings.

## Tech Stack

- **UI**: Jetpack Compose & Material 3
- **Architecture**: MVVM with Kotlin Coroutines & Flow
- **Persistence**: Room Database with TypeConverters
- **Design System**: M3 Dynamic styling with custom palettes
