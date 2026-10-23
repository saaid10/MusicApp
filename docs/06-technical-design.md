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

## Import UI design (2026-10-14)

Mockup made in Google Stitch, used as a visual reference only. Compose can't import HTML/CSS, so only the design tokens carry over.

- **Design tokens ("Nocturne Audio"):** Primary `#10B981`, Secondary `#2DD4BF`, Tertiary `#A7F3D0`, Neutral/background `#121212`. Font: Plus Jakarta Sans. Pill-shaped buttons and chips, rounded cards.
- **Fields:** URL (required, with a paste button), Song title (required), Artist (optional), Album (optional). The Import button stays disabled until the URL is valid.
- **Filename vs title:** the file stays `videoId.m4a`, and the title, artist and album are stored separately as metadata. Duplicate detection keeps working, and two songs with the same title can't collide.
- **Metadata storage:** title, artist and album are stored in Room (see "Library database (Room)" below).
- **States:** idle, importing, success ("Saved to library"), already in library (Skip highlighted, or Replace), failed (message + Try again).
- **Progress:** an indeterminate bar for the MVP. A real 0–100% (with optional download speed) is deferred: it means replacing `copyTo` with a byte-counting loop plus `Content-Length`, it only covers the download step, and for files under 10 MB it's barely visible.
- **Failure messages:** plain hints such as "No internet connection" or "Invalid link" are wanted. This needs the `catch` to tell the causes apart (resolved, see "Import screen implemented" below).
- **Dropped from the mockup:** "Keep Both Copies" (it would collide on `videoId.m4a`), quality upgrade / 320 kbps / lossless (contradicts the ≤192 kbps M4A rule), ID3 tagging (that's mp3, not m4a), the bottom nav bar (no other screens yet), the "Supported: YouTube, SoundCloud, Bandcamp" claim (untested), the waveform, and the "Engine ready" chip (it would show a fake status).
- **Navigation (decided 2026-10-14, built later):** no home screen, because it only adds a tap. A bottom bar with 3 tabs, ordered by use: **Library · Playlists · Import**. Library is the start screen, with a search field at its top that filters your own songs (cross-catalog search stays out of scope). No settings icon until a setting exists. The bottom bar gets built once Library exists (after Room).

## Import screen implemented (2026-10-18 to 2026-10-21)

The Import screen (`ImportScreen.kt`) now drives the pipeline. The `onCreate` test trigger is gone, so rotation no longer starts a second import.

- **State flow:** `ImportViewModel` exposes `uiState: StateFlow<ImportUiState>` (private `MutableStateFlow` inside). The screen reads it with `collectAsStateWithLifecycle()` (dependency `lifecycle-runtime-compose:2.8.7`) and copies it into a plain `val currentState` so Kotlin can smart-cast it in `if`/`when`.
- **Import button:** enabled only when the URL starts with `http`, the title is not blank, and no import is running. Optional fields go in as `null` when blank (`ifBlank { null }`).
- **Importing:** a progress card at the top (indeterminate bar) and a spinner inside the button. The 4 fields, the clear button and the Paste chip are disabled (`enabled = !isImporting`), because `importAudio` already copied their values at the tap. Editing them mid-import would make the screen disagree with what gets saved.
- **Success:** a full screen ("Saved to Library!", title/artist, **Import Another Song** = reset state + clear the fields). It replaces the form via an early `return` in `ImportScreen`. The mockup's "View in Library" / "Play Now" are left out until Library and playback exist.
- **Already in Library:** a bottom sheet. **Skip** = reset + clear the fields. Swipe-dismiss = reset only, fields kept. **Replace** calls `importAudio(..., skipDuplicateCheck = true)`, which bypasses `isAlreadyImported` and overwrites the file (`copyTo(..., overwrite = true)`). Without the flag, Replace would hit the duplicate check again and loop back to the sheet. Replace is also meant for fixing a mistyped title, but that only has an effect once metadata is stored (Room).
- **Failed:** `Failed(failedMsg, failedHint, isLinkError)`. The ViewModel's `catch` maps the exception type to a `Triple(message, hint, isLinkError)`, specific types before general ones (`UnknownHostException` before `IOException`, `ContentNotAvailableException` before `ExtractionException`). **Try Again** = reset state, fields kept. The URL field turns red only when `isLinkError` is true (unsupported link, i.e. `ExtractionException`). A blocked video, no internet or a refused download is not the link's fault. A flag is used instead of comparing the message text, so rewording a message can't break the behaviour. The transcode `onFailure` uses `isLinkError = false`.
- **Download failures:** step 2 now throws `DownloadFailedException(responseCode)` (extends `Exception`, not `IOException`: a 403 is the server refusing, not a connection problem). It shows as "Download failed <code>" instead of falling into "Something went wrong.". Verified by temporarily inverting the status check (showed 206).

**Still open:** `error("No audio streams found")` in `getAudioStream` still lands in "Something went wrong."; the HTTP response is not closed when `DownloadFailedException` is thrown; the `"${videoId}.m4a"` filename is still duplicated in two functions.

## Library database (Room) (2026-10-23 to 2026-10-24)

Each imported song gets a row in a local Room (SQLite) database, `music.db`, stored at `/data/user/0/com.saeed.musicapp/databases/`. The audio stays a separate file in `filesDir` (`<videoId>.m4a`). The row holds the metadata and links to the file through `videoId`.

**`Song` entity (`Song.kt`, table `songs`):**

| Field | Type | Required | Source |
|---|---|---|---|
| `videoId` (primary key) | `String` | yes | NewPipe (`info.id`); also the filename |
| `title` | `String` | yes | user |
| `artist` | `String?` | no | user |
| `album` | `String?` | no | user |
| `dateAdded` | `Long` (epoch ms) | yes | app (`System.currentTimeMillis()`), used to sort newest first |
| `duration` | `Long?` (seconds) | no | NewPipe (`info.duration`). Values ≤ 0 mean unknown and are stored as `null` in `getAudioStream`, the only place `info` is available, and carried out via `ImportedAudio.duration` |

- **Thumbnail:** deferred until after the MVP. A URL alone would not show offline, unlike the songs themselves.
- **`SongDao`:** `insert` (`OnConflictStrategy.REPLACE`, so **Replace** overwrites the row just as it overwrites the file), `getAllSongs(): Flow<List<Song>>` (`ORDER BY dateAdded DESC`; the Flow re-emits on every table change, so the Library updates by itself), `delete` (for storage management).
- **`MusicDatabase`:** `@Database(entities = [Song::class], version = 1)`. Bump the version (with a migration) whenever the table changes.
- **Single instance:** a custom `MusicApplication` (registered with `android:name` in the manifest) holds `val database by lazy { Room.databaseBuilder(...).build() }`. One object for the whole app avoids two writers on the same file, and `lazy` is thread-safe. This was chosen over a `companion object` `getInstance()` (same result, but that needs hand-written `@Volatile`/`synchronized`). The ViewModel reaches it via `getApplication<MusicApplication>().database`.
- **When the row is written:** after the file move and before `Success`. Inserting at the tap would leave a row with no file when the import fails (the Library would show an unplayable song). Inserting before `Success` means the screen only claims success once the row is saved.
- **Transcode path:** the insert runs inside `onSuccess`, which Media3 calls later on the main thread, after the import coroutine has already finished. So it starts its own `viewModelScope.launch(Dispatchers.IO)` (needed for the `suspend` insert) with its own `try/catch` (the outer `catch` can no longer see it).
- **File and row stay together:** if the insert fails, the just-moved file is deleted (skip path: `movedToStorage.delete()`; transcode path: rebuilt from `filesDir` + `"${videoId}.m4a"`) and Failed shows "Couldn't save the song." Without that, the duplicate check (file-based) would report "Already in Library" for a song the Library never shows.
- Dependencies: Room 2.7.2 (`room-runtime`, `room-ktx`, `room-compiler` via KSP `2.2.10-2.0.2`, matching Kotlin 2.2.10).
- Verified on the emulator: skip path and transcode path (format check temporarily flipped) both write the row, and Replace overwrites it.

**Still open:** on a Replace where the insert fails, the file is deleted but the old row remains; the duplicate check still looks at the file, not the database.
