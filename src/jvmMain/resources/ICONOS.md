# Iconos de KodeForge

Para añadir iconos personalizados a la aplicación:

## 1. Guarda tu imagen base como `icon.png` (1024x1024 px recomendado)

## 2. Convertir a .icns (macOS)

```bash
# Crear iconset
mkdir icon.iconset
sips -z 16 16 icon.png --out icon.iconset/icon_16x16.png
sips -z 32 32 icon.png --out icon.iconset/icon_16x16@2x.png
sips -z 32 32 icon.png --out icon.iconset/icon_32x32.png
sips -z 64 64 icon.png --out icon.iconset/icon_32x32@2x.png
sips -z 128 128 icon.png --out icon.iconset/icon_128x128.png
sips -z 256 256 icon.png --out icon.iconset/icon_128x128@2x.png
sips -z 256 256 icon.png --out icon.iconset/icon_256x256.png
sips -z 512 512 icon.png --out icon.iconset/icon_256x256@2x.png
sips -z 512 512 icon.png --out icon.iconset/icon_512x512.png
sips -z 1024 1024 icon.png --out icon.iconset/icon_512x512@2x.png

# Generar .icns
iconutil -c icns icon.iconset
rm -rf icon.iconset
```

## 3. Convertir a .ico (Windows)

Usa una herramienta online como https://cloudconvert.com/png-to-ico
o instala ImageMagick:

```bash
convert icon.png -define icon:auto-resize=256,128,64,48,32,16 icon.ico
```

## 4. Descomentar en build.gradle.kts

```kotlin
windows {
    iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
}

macOS {
    iconFile.set(project.file("src/jvmMain/resources/icon.icns"))
}
```

## 5. Regenerar el ejecutable

```bash
./gradlew packageDmg  # macOS
./gradlew packageExe  # Windows
```

