#!/data/data/com.termux/files/usr/bin/bash
# ==============================================================================
# Pulse — Ultimate Sensor Toolbox
# Automated Build Script for Termux on Android
# Package: com.cartoniq.pulse
# ==============================================================================

set -e

echo "=================================================="
echo "⚡ PULSE — ULTIMATE SENSOR TOOLBOX"
echo "🛠️  Termux Android Build Pipeline"
echo "=================================================="

# Check Java 17
if ! command -v java &> /dev/null; then
    echo "📦 Java not detected. Installing OpenJDK 17..."
    pkg update -y
    pkg install -y openjdk-17
fi

JAVA_VER=$(java -version 2>&1 | head -n 1)
echo "☕ Active Java: $JAVA_VER"

# Ensure gradlew has executable permission
chmod +x ./gradlew

echo "🧹 Running Gradle clean..."
./gradlew clean

echo "🚀 Building Debug APK (assembleDebug)..."
./gradlew assembleDebug --stacktrace

APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

if [ -f "$APK_PATH" ]; then
    echo "=================================================="
    echo "✅ SUCCESS: Debug APK successfully compiled!"
    echo "📍 Location: $APK_PATH"
    echo "📏 Size: $(du -h $APK_PATH | cut -f1)"
    echo "=================================================="
    echo ""
    echo "📲 To install directly on your Android phone, run:"
    echo "   termux-open $APK_PATH"
    echo "=================================================="
else
    echo "❌ Build finished but APK was not found at expected path: $APK_PATH"
    exit 1
fi
