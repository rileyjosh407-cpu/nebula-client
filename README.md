# Nebula Client (Fabric, Minecraft 1.21.11)

A client-side utility mod with a dark module menu, themes, saved configs, keybinds and toast
notifications. Everything it does is local rendering/audio; it sends nothing extra to servers.

## Features

- **Menu** (Right Shift by default): sidebar categories, search, expandable module cards with
  toggles, sliders and dropdowns, per-module keybinds.
- **Farm FPS Optimizer**: hide XP orbs, falling blocks, TNT and explosion particles; mute sounds
  that repeat too often (threshold slider).
- **Region Map**: coordinate-grid HUD showing which large region cell you are in, with neighbours.
- **Coordinates**: XYZ and facing panel with corner selection.
- **Theme / Configs / Keybinds / Settings** pages. Configs are JSON in `.minecraft/config/nebula/`.
  The `default` config auto-saves when you close the menu or quit the game.

Pistons and redstone-dust hiding from the original screenshots are not included.

## Easiest: let GitHub build it (nothing to install)

1. Create a free account at github.com and make a new **private** repository.
2. Click "uploading an existing file", drag in the *contents* of this folder (including the
   hidden `.github` folder), and commit.
3. Open the **Actions** tab, open the latest "Build mod jar" run, and download the
   `nebula-client-mod` artifact at the bottom. Unzip it to get the jar.

## Build the jar locally instead

Requirements: JDK 21 and Gradle 8.14+ (or open the folder in IntelliJ IDEA, which sets this up).

```
gradle wrapper --gradle-version 8.14     # one-time, generates ./gradlew
./gradlew build
```

The mod jar is `build/libs/nebula-client-1.0.0.jar` (ignore the `-sources` jar).

## Install

1. Install Fabric Loader 0.19.x for Minecraft 1.21.11.
2. Put **Fabric API 0.141.3+1.21.11** (or newer for 1.21.11) and `nebula-client-1.0.0.jar` in
   `.minecraft/mods/`.

## Versions used (checked 2026-10-15)

Yarn `1.21.11+build.6`, Fabric Loader `0.19.5`, Fabric API `0.141.3+1.21.11`, Loom `1.14-SNAPSHOT`
(set in `gradle.properties`).

## If the build fails

This project was written against the 1.21.11 Yarn mappings but could not be compiled in the
environment it was authored in, so a small mapping mismatch is possible. Most likely spots:

- `fabric-loom` version: if Gradle can't resolve `1.14-SNAPSHOT`, change `loom_version` to the
  latest from https://fabricmc.net/develop.
- `SoundSystemMixin`: uses `SoundSystem.PlayResult.NOT_STARTED`. If that constant name differs,
  check the enum in your IDE.
- Any other error will point at a single line; the mixin descriptors are in `src/main/java/dev/nebula/mixin/`.
