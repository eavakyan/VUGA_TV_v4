# Splash Screen Feature

## Overview

The Android TV app now includes a motion splash screen that plays a video at startup, providing a more engaging and professional launch experience.

## Features

### Video Splash Screen
- **Local Video**: Uses a custom splash video stored in `app/src/main/assets/splash_video.mp4`
- **Branded Content**: Features "VUGA TV" branding with "Entertainment Redefined" tagline
- **Automatic Transition**: Automatically transitions to the main app after video completion
- **Fallback Support**: Shows animated fallback content if video fails to load

### Visual Effects
- **Smooth Animations**: Fade-in/fade-out transitions between splash and main app
- **Background Animation**: Subtle animated background color
- **Progress Indicator**: Animated loading progress bar
- **Skip Button**: Optional skip button for users who want to bypass the splash

### Technical Implementation
- **ExoPlayer Integration**: Uses Media3 ExoPlayer for video playback
- **Compose Animations**: Leverages Jetpack Compose animation APIs
- **Memory Management**: Proper cleanup of video player resources
- **Error Handling**: Graceful fallback when video fails to load

## File Structure

```
app/src/main/
├── assets/
│   └── splash_video.mp4          # Custom splash video
├── java/com/vugaenterprises/androidtv/
│   ├── MainActivity.kt           # Updated with splash screen logic
│   └── ui/screens/
│       └── SplashScreen.kt       # Splash screen component
```

## Customization

### Video Content
To customize the splash video:
1. Replace `app/src/main/assets/splash_video.mp4` with your own video
2. Ensure the video is in MP4 format with H.264 encoding
3. Recommended resolution: 1920x1080 (16:9 aspect ratio)
4. Recommended duration: 3-5 seconds

### Visual Branding
To modify the fallback content:
- Edit the text content in `SplashScreen.kt`
- Adjust colors, fonts, and animations
- Modify the background animation parameters

### Timing
To adjust timing:
- Minimum splash duration: Modify the `delay(3000)` value in `LaunchedEffect`
- Video end delay: Modify the `delay(500)` value after video ends
- Animation durations: Adjust `tween` parameters in animation specs

## Video Generation

The current splash video was generated using FFmpeg:

```bash
ffmpeg -f lavfi -i "color=c=black:size=1920x1080:duration=5" \
  -vf "drawbox=y=0:color=#1a1a1a:width=iw:height=ih:t=fill,\
       drawtext=text='VUGA TV':fontcolor=white:fontsize=120:x=(w-text_w)/2:y=(h-text_h)/2-50:font=Arial-Bold,\
       drawtext=text='Entertainment Redefined':fontcolor=#cccccc:fontsize=40:x=(w-text_w)/2:y=(h-text_h)/2+100:font=Arial" \
  -c:v libx264 -preset fast -crf 23 splash_video.mp4
```

## Performance Considerations

- **Video Size**: Keep the splash video file size small (< 1MB) for fast loading
- **Memory Usage**: Video player is properly disposed to prevent memory leaks
- **Loading Time**: Fallback content ensures app doesn't hang if video fails to load
- **Battery Impact**: Minimal impact as splash screen is short-lived

## Troubleshooting

### Video Not Playing
- Check that `splash_video.mp4` exists in the assets folder
- Verify video format is compatible with ExoPlayer
- Check logcat for video loading errors

### App Hangs on Splash
- The app has a 3-second minimum timeout to prevent hanging
- Check for video player initialization errors
- Verify ExoPlayer dependencies are properly included

### Performance Issues
- Reduce video resolution or duration if needed
- Check for memory leaks in video player cleanup
- Monitor CPU usage during splash screen playback 