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

## Spike status (2026-09-28)

Steps 1-3 confirmed working end-to-end on emulator (Logcat: `Transcode complete`).
- Steps 1-2 run on a background `Thread` (blocking network I/O).
- Step 3 hops to the main thread via `runOnUiThread { }` -- `Transformer.start()` needs a Looper thread and returns immediately.

Open questions:
- YouTube's `audioStreams[0]` was itag 139 (`audio/mp4`, already AAC, ~48 kbps). Is step 3 needed when the source is already AAC? (Import must support any URL, not only YouTube.)
- `audioStreams[0]` picks whatever is listed first, not the best quality -- stream selection needs a rule.

## Decisions (2026-09-29)

**Skip step 3 when the source is already AAC.** Saves battery/time and avoids lossy-to-lossy quality loss ("photocopy of a photocopy"). Accepted cost: the format check must be reliable, or the file won't play. `getDirectAudioUrl` was refactored to `getAudioStream`, which returns the whole `AudioStream`, so the format (`AudioStream.format`, e.g. `MediaFormat.M4A`) isn't lost.

**Stream selection rule (step 1):**
1. Prefer M4A (AAC) streams. If none exist, consider all streams (step 3 converts the result).
2. Ignore streams with unknown bitrate (NewPipe reports `-1`), unless every candidate is unknown — some audio beats none. (Added 2026-09-30.)
3. Among the candidates, pick the highest bitrate at or below 192 kbps.
4. If every candidate is above 192 kbps, pick the lowest (closest to 192).

Implemented in `getAudioStream` (2026-09-30); confirmed on-device: M4A 128 kbps. No audio streams at all (live stream, blocked video): `getAudioStream` throws a clear error (`error("No audio streams found")`) at the top, instead of `minBy` throwing a vague `NoSuchElementException`. Chosen over a nullable return so every failure (invalid URL, network, no audio) is handled the same way: one `try/catch` in the caller. Failure path confirmed on-device: an invalid URL logs `The import failed` + stack trace, and the app stays open. (2026-09-30)

Reasoning: ~128 kbps AAC is good, and ~192-256 kbps sounds identical to the original for almost everyone; Bluetooth headphones re-compress to ~256 kbps anyway. On YouTube the result is M4A 128 kbps (itag 140) rather than WEBM/Opus 160: the difference is barely audible, and it avoids a conversion. The 192 cap protects against oversized streams from other sites.

**Skip step 3 implemented (2026-09-30):** `MainActivity` only calls `transcodeToAac` when `audioStream.format != MediaFormat.M4A`. Confirmed on-device: M4A stream -> no `Transcode complete` line. Consequence for step 4: the file to keep is `cache/raw_audio` when step 3 is skipped, `cache/converted_audio` otherwise.

Observed once: `SocketTimeoutException` during step 2 (`copyTo` in `downloadAudioToCache`), i.e. no data for OkHttp's default 10 s read timeout. Rerun succeeded. Treated as a one-off network hiccup for now; revisit if it recurs (e.g. timeout config or retry).
