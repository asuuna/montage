# Montage — Specification

## Vision
Montage is a cross-platform JavaFX desktop editor that combines timeline-based video editing with on-device AI assistance so small teams can cut, enhance, and publish social videos in minutes without relying on cloud services.

## Primary Personas & Goals
- **Solo Creator**: Import footage, obtain smart scene cuts and highlights, publish quickly to multiple aspect ratios.
- **SMB Social Manager**: Batch subtitle and export branded clips with consistent presets.
- **Technical Editor**: Needs precise timeline manipulation, shortcuts, and reliable renders.

## Top Use Cases
1. Import camera/phone footage, probe metadata, and generate proxy thumbnails via FFmpeg.
2. Run scene + silence detection, propose an auto-rough-cut, and present edits on the timeline.
3. Produce multilingual subtitles locally (Vosk by default, optional Whisper ONNX) and export SRT/VTT.
4. Build highlight reels by ranking segments (motion/audio/face/keywords) and export social variants (16:9, 9:16, 1:1).
5. Apply stabilization, color tweaks, and text templates; manage autosave, undo/redo, shortcuts, and theme switching.
6. Render H.264/H.265 exports with presets, monitor progress, resume jobs, and package installers for Win/macOS/Linux via jpackage.
7. Provide diagnostics, telemetry toggle (off by default), fallback instructions when codecs/GPU are missing, and optional local GPU acceleration toggle.

## Functional Requirements
- **Project & Storage**: Autosave every 60s with crash recovery, JSON project store under `~/MontageProjects`, configurable caches/models.
- **Media Engine**: JavaCV/FFmpeg probing, proxy/thumbnail generation, disk+memory frame cache, encoders with presets, fallback messaging if FFmpeg unavailable.
- **Timeline & Editing**: Multi-track video/audio timeline, drag/drop, snap, trim, ripple, transitions, time-stretch, audio gain, undo/redo, autosave resume, keyboard shortcuts, dark/light themes.
- **AI Pipelines**: Scene detection (OpenCV hist diff), silence trim, subtitle ASR (Vosk/Whisper ONNX import), scene clustering for chapters, highlight scoring, object tracking (KCF/CSRT), GPU toggle when supported, all on-device.
- **Effects**: Stabilization, color adjustment (brightness/contrast/saturation, 3D LUT), text titles, audio crossfades and ducking.
- **Render Queue**: DAG task graph, thread pools, progress UI, cancel, retry/resume, background job serialization.
- **Integration & Diagnostics**: SLF4J/Logback with rotating logs, Diagnostics screen (versions, paths, GPU/FFmpeg status), telemetry toggle (disabled default).
- **Packaging**: Maven + wrappers, jpackage installers (.exe /.dmg /.deb) with icons, runtime image (javafx:jlink option), installers emitted to `dist/` with README.
- **Extensibility**: Plugin SPI via `ServiceLoader` for effects/exporters; optional cloud integrations disabled by default and clearly labeled.

## Non-Functional Requirements
- **Performance**: Responsive UI <200 ms interactions, real-time proxy playback 1080p30, render throughput tracked with perf metrics and optional JFR profiling flag.
- **Reliability**: Autosave/recovery, deterministic renders, restartable jobs, local model integrity checks.
- **Security & Privacy**: No network calls without explicit opt-in; telemetry stored locally; model downloads manual.
- **Compatibility**: Java 17+, Windows 10+, macOS 12+, Ubuntu 22.04+, default CPU processing with optional GPU toggle.
- **Maintainability**: Modular Maven build, ADR log, Test suites (JUnit5, TestFX), CI script `mvn -B verify` headless friendly.
- **Localization & Accessibility**: FR/EN bundles, documented shortcuts, theme switching, high-contrast mode via CSS.

## Acceptance Criteria
1. Media import displays metadata, proxies, and thumbnails without errors for sample clips.
2. Timeline editing (trim/split/ripple/undo) updates preview in real time and autosaves state.
3. AI features (scene detect, silence trim, subtitles, highlight reel, tracking) run fully offline with adjustable parameters and GPU toggle default off.
4. Export pipeline renders MP4 (H.264/H.265) with progress/cancel/resume; fallback message appears if codec missing.
5. Diagnostics view lists environment info, log location, FFmpeg/GPU availability, telemetry status.
6. Build produces passing tests (unit + integration + TestFX smoke), generates installers in `dist/`, and README/NOTICE/Licenses describe dependencies and model sources.

## Traceability
| Feature | Acceptance Tests |
| --- | --- |
| Media I/O | 1 |
| Timeline Editing | 2 |
| AI Assistance | 3 |
| Render Queue | 4 |
| Diagnostics & Observability | 5 |
| Packaging & Compliance | 6 |

## Open Questions
- Branding assets (icons, splash) pending.
- Default GPU providers per OS (CUDA vs DirectML vs Metal) to auto-detect.
- Shipping default LUTs/templates licensing.
