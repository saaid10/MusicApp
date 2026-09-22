# Technical Design

## Import & Convert pipeline

Decision: extraction and transcoding happen entirely on-device (no external server/API). Reason: avoids a dependency on third-party "youtube to mp3" services, which are unreliable and frequently shut down; also consistent with the existing Scope & Delimitation decision to not run a personal server for now.

Pipeline steps:

1. URL in -> direct audio stream URL out (via extractor library)
2. Direct audio stream URL in -> raw audio file downloaded to device out
3. Raw audio file in -> transcoded AAC file out (via on-device transcoder)
4. AAC file in -> saved to app's folder out

Steps 2 and 4 are standard Android file I/O, no research needed. Steps 1 and 3 each need a library and are the open unknowns for the spike:

- Step 1: a library that can turn a YouTube URL into a direct audio stream URL
- Step 3: a library that can transcode raw audio to AAC on-device

## Output format: AAC, not mp3

Original assumption was mp3. Changed to AAC after research found Android's MediaCodec only supports mp3 *decoding* natively, not *encoding* -- producing an mp3 file would require bundling a third-party encoder (e.g. LAME via FFmpeg), adding a dependency and app size for no real benefit. AAC is Android's native encode format (no extra library needed for step 3's output), and is equal-or-better than mp3 on both quality and file size at the same bitrate. Since the app both creates and plays back the file itself, mp3's universal-compatibility advantage doesn't apply here.

## Library candidates

**Step 1 (URL -> direct audio stream URL):** NewPipeExtractor (TeamNewPipe/NewPipeExtractor) -- chosen. Actively maintained (last release March 2026), is the core library NewPipe itself is built on, large ecosystem of apps/forks depending on it. License: GPLv3 -- fine for a personal/private app, but would require this app's source to also be GPL if ever published for others to use. Considered and rejected: kotlin-youtubeExtractor (archived/abandoned since May 2024).

**Step 3 (raw audio -> AAC):** Media3 Transformer (Jetpack Media3, Google's official Android media library) -- chosen. Directly supports AAC output via `setAudioMimeType(MimeTypes.AUDIO_AAC)`. First-party Google library, actively maintained (docs updated April 2026), compatible back to Android 6.0 (API 23). No competing candidate came close on maintenance guarantees.
