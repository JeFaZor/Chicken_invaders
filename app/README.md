# Chicken Invaders - Android Game

## Overview
Android game where players control a spaceship to avoid falling chickens while collecting coins.

## Features

### Game Mechanics
- 5-lane road with 8 rows
- Spaceship avoids falling chickens
- Collect coins for bonus points
- Distance counter (Odometer)
- 3 lives system with heart display
- Crash sound effects

### Control Options
- **Button Mode - Slow**: Left/right buttons, slower speed
- **Button Mode - Fast**: Left/right buttons, faster speed
- **Sensor Mode**: Tilt device left/right to move
    - Bonus: Tilt forward/backward for speed control

### Menus & Screens
- Main menu with control mode selection
- Game over screen with score and name input
- High scores table with two tabs:
    - Scores list (top 10)
    - Map showing score locations

### Technical Features
- Score calculation: Distance + (Coins × 10)
- GPS location saving with each score
- Google Maps integration showing score markers
- Click on score to view location on map
- Data persistence using SharedPreferences

## Technologies
- Kotlin
- Android SDK (API 26-35)
- Google Maps SDK
- Location Services
- Accelerometer sensors

## Setup
1. Clone repository
2. Add Google Maps API key to AndroidManifest.xml
3. Build and run
4. Grant location permissions

**Developer**: Lior Toledano