# Lumify

Lumify is a modern Android photo gallery application built with Jetpack Compose, following the latest Android development best practices. It enables users to browse their photos, view them in detail, apply professional-looking filters, and capture new images.

<p align="center">
  <img src="/screenshots/homescreen.png" width="30%" />
  <img src="/screenshots/editscreen.png" width="30%" /> 
  <img src="/screenshots/camerascreen.png" width="30%" />
</p>

## Features

### Photo Gallery
- Browse device photos in an elegant staggered grid layout
- Efficient image loading with caching using Coil
- Responsive UI with smooth scrolling and transitions

### Detail View
- Full-screen photo viewing experience
- Share functionality for easy photo distribution
- Edge-to-edge UI with immersive design

### Photo Filtering
- 14 professionally designed filters inspired by popular camera styles
- Real-time filter preview directly on the full image
- Save options for preserving original photos or creating new copies
- Filter styles include:
    - Silver: Classic high-contrast black & white
    - Nostalgia: Warm vintage film tones
    - Nordic: Clean, cool color palette
    - Golden: Rich, warm tones with enhanced depth
    - Impact: Bold contrast with vibrant colors
    - Cinema: Subtle filmic look with reduced contrast
    - Street: Dramatic black & white with deep shadows
    - Pop: Bright, vibrant colors with extra punch
    - Dreamy: Soft pastel colors with lifted shadows
    - Analog: Creative cross-processed look
    - Verde: Lush greens with teal shadows
    - Portrait: Flattering skin tones
    - Airy: Bright, high-key look

### Camera Integration
- Built-in camera for capturing new photos using CameraX
- Flash control, front/back camera switching
- Tap-to-focus functionality
- Captured photos are immediately available in the gallery

## Technical Stack

### Architecture & Patterns
- **Architecture Pattern**: MVVM (Model-View-ViewModel)
- **UI Framework**: Jetpack Compose
- **Programming Language**: Kotlin
- **Dependency Injection**: Hilt
- **Coroutines & Flow**: For reactive programming
- **Material 3**: For modern design components

### Key Libraries
- **Navigation**: Jetpack Navigation Compose
- **Image Loading**: Coil
- **Camera**: CameraX
- **Permissions**: Accompanist Permissions
- **Build System**: Gradle with Kotlin DSL and Version Catalog

## Project Structure

### Data Layer
- `MediaItem`: Data class for photo information
- `MediaRepository`: Handles access to device media store
- `PhotoFilter`: Represents an image filter with its properties and transformations

### UI Layer
- `GalleryScreen`: Displays the photo grid
- `DetailScreen`: Full-screen photo viewing with filtering capabilities
- `CameraScreen`: In-app camera interface
- `FilterStrip`: Horizontal strip of filter thumbnails

### Utils
- `ImageFilterUtils`: Functions for applying filters and saving images
- `ColorMatrixUtils`: Color transformation utilities for photo filters

## Getting Started

### Prerequisites
- Android Studio Arctic Fox (2021.3.1) or newer
- JDK 11 or newer
- Android SDK 21+

### Setup
1. Clone the repository:
```
git clone https://github.com/yourusername/lumify.git
```

2. Open the project in Android Studio.
3. Sync the project with Gradle files.
4. Run the app on an emulator or physical device.

## Permissions
Lumify requires the following permissions:
- `READ_EXTERNAL_STORAGE` or `READ_MEDIA_IMAGES` (Android 13+): For accessing device photos
- `CAMERA`: For taking new photos
- `WRITE_EXTERNAL_STORAGE`: For saving filtered images (on Android < 10)

The app handles permission requests at runtime and provides clear explanations for why permissions are needed.

## Future Enhancements
- Photo organization with albums and tags
- Advanced editing tools (crop, rotate, etc.)
- Sharing photos to social media
- Cloud backup integration
- Video support
- Search functionality

## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

## License
This project is licensed under Apache 2.0 license - see the LICENSE file for details.

---

Developed with ❤️ using modern Android development practices by Adhithya ✨
