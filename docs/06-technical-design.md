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

## Step 4 design (2026-09-30)

- New function in `ImportConvert.kt`: input file + `Context`, output the moved `File`.
- Called in two places: the skip path (`else` branch in `MainActivity`, moves `raw_audio`) and the transcode path (inside `onCompleted`, moves `converted_audio`). The move can't happen after `transformer.start()`: that call returns before the file exists.
- Target folder: `filesDir` (kept until the app deletes it or is uninstalled), not `cacheDir` (Android may wipe it when storage is low).
- File name = the video's ID plus `.m4a` (e.g. `Y4HWvsGs0rY.m4a`), not the title: titles aren't unique and can contain characters that aren't allowed in file names. The display title will be stored separately (later, in Room). Same video twice -> same ID -> detectable duplicate (ties to the duplicate-detection user stories). The `.m4a` extension is correct on both paths, since both produce AAC in an MP4 container.
- The video ID comes from `StreamInfo.id`, but `getAudioStream` discarded `info`. Decision: return both values together in a `data class ImportedAudio(videoId: String, audioStream: AudioStream)` (chosen over `Pair` for readable names).

## Step 4 implemented (2026-10-07)

`moveToPermanentStorage(file, context, videoId): File` copies the file to `filesDir/<videoId>.m4a` (`copyTo(..., overwrite = true)`), deletes the cache original, and returns the copy. `transcodeToAac` takes `videoId` as an extra parameter so `onCompleted` can call it. Both paths confirmed on-device.

**Cache cleanup:** on the transcode path, `raw_audio` (the input to step 3) was never deleted. Now `transcodeToAac` deletes it in both `onCompleted` and `onError`. After either outcome it has no further use: there is no retry feature, and a retry would need a fresh download anyway (stream URLs expire). In `onCompleted` the delete runs *before* the move, so it still happens if the move throws.

**Move failure inside `onCompleted`:** `onCompleted` runs later on the main thread, so `MainActivity`'s `try/catch` (on the background `Thread`) can't catch an exception from it. An uncaught exception there would crash the app. So `onCompleted` wraps the move and its log in its own `try/catch`. The `catch` logs the error and deletes `converted_audio`. Confirmed on-device: the cache folder is empty after the transcode path.

## Duplicate detection (2026-10-07)

- `isAlreadyImported(videoId, context): Boolean` checks whether `filesDir/<videoId>.m4a` exists. It compares by video ID, not URL: one video can have many URLs (`youtu.be/...`, `&t=30`, ...).
- Checked right after step 1, which is the first point where the ID is known. That way a duplicate costs no download or transcode.
- Current behavior (no UI yet): log `Already in the library` and stop.
- Planned UX: a dialog with **Skip** (highlighted, since the user most likely forgot they have it) or **Replace**. Replace needs no extra code: the normal pipeline already overwrites (`overwrite = true`). There is no "add again" option for the library, because the result would be the same file under the same name.
- Playlists will allow adding a song twice (a playlist entry only points to a library file). Deferred to the Playlists/Room phase.
- Both paths confirmed on-device.
- Known risk: the filename pattern `"${videoId}.m4a"` is written in two functions (`moveToPermanentStorage`, `isAlreadyImported`). If they drift apart, the check stops finding anything without any error. A candidate for a small shared helper later.

**Logging:** the step 1 log (URL / format / bitrate / ID) now runs right after `getAudioStream`, so it prints on every path, including duplicates and failed downloads. The `Download file:` log runs right after the download, while `raw_audio` still exists.

## Pipeline moved into a ViewModel (2026-10-09)

The temporary `Thread {}` in `MainActivity` had three problems: the import ran on every app start, the logic lived in the screen, and rotation (Android destroys and recreates the Activity) left the old `Thread` running while holding the dead Activity, and `onCreate` started a second import.

- **`ImportViewModel`** (`ImportViewModel.kt`) owns the pipeline. One public method: `importAudio(url: String)`. It returns nothing: the work finishes later, so the result will be exposed as state the screen observes (once there is a UI).
- **`AndroidViewModel`, not `ViewModel`:** the pipeline needs a `Context` (`cacheDir`, `filesDir`, `Transformer.Builder`). Passing the Activity in would leak it on rotation, since the coroutine outlives the screen. `AndroidViewModel` provides the Application context via `getApplication<Application>()`. It lives as long as the app, so there is nothing to leak.
- **Threads:** `viewModelScope.launch(Dispatchers.IO)` replaces `Thread {}` (blocking network/file work). `withContext(Dispatchers.Main)` replaces `runOnUiThread` for `transcodeToAac` (Transformer needs a Looper thread). `viewModelScope` cancels the coroutine when the ViewModel is cleared.
- **Getting the ViewModel:** `MainActivity` uses `by viewModels()`, which returns the same instance across rotations. Constructing it with `ImportViewModel(...)` would create a new one on every `onCreate`, and the old one would never be cleared.
- **Temporary trigger:** `onCreate` still calls `importAudio(<test URL>)`, so rotation calls it again. This is harmless for an already-imported video, but rotating mid-download on a new URL would start a second, racing import (both pass the duplicate check before either saves). Accepted for now: the real trigger will be a button.
- Dependency added: `androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7`. This matches the lifecycle 2.8.x that `activity-compose` 1.9.3 already pulls in, and needs compileSdk 34 or higher.
- All three paths confirmed on-device: duplicate, skip (M4A), and transcode (tested with the format check temporarily flipped).

**HTTP status check (step 2):** `downloadAudioToCache` saved the response body whatever the status, so an error reply (e.g. 403) was written to `raw_audio` and only failed later, as a confusing Transformer `UnrecognizedInputFormatException`. This was observed once, after a 0.6 s "download". The cause was not proven, but it is most likely a server error reply. It now throws right after `execute()` when `!response.isSuccessful`, including `response.code` in the message, so the failure lands in the ViewModel's `try/catch` with the real status code.
