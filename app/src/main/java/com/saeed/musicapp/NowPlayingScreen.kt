package com.saeed.musicapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.saeed.musicapp.ui.theme.MusicAppTheme

// Now Playing screen LOOK only (Stitch "Now Playing - Playing/Paused" mockups).
// Every hook is empty: the playback behaviour (ExoPlayer, position, toggles) is wired up separately.
// Dropped from the mockup: profile avatar (no accounts), "Lossless" badge.

@Composable
fun NowPlayingScreen(
    title: String,
    artist: String?,
    album: String?,
    playingFrom: String,
    isPlaying: Boolean,
    progress: Float,            // 0f..1f, how far into the song
    positionText: String,       // e.g. "1:12"
    durationText: String,       // e.g. "4:40"
    shuffleOn: Boolean,
    repeatOn: Boolean,
    onCollapseClick: () -> Unit,
    onMoreClick: () -> Unit,
    onSeek: (Float) -> Unit,
    onShuffleClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
                )
            )
            .safeDrawingPadding()
            .padding(horizontal = 24.dp)
    ) {
        NowPlayingTopBar(playingFrom = playingFrom, onCollapseClick = onCollapseClick, onMoreClick = onMoreClick)
        Spacer(Modifier.height(24.dp))
        ArtworkPlaceholder()
        Spacer(Modifier.weight(1f))
        SongInfo(title = title, artist = artist, album = album)
        Spacer(Modifier.height(24.dp))
        SeekBar(progress = progress, positionText = positionText, durationText = durationText, onSeek = onSeek)
        Spacer(Modifier.height(16.dp))
        PlayerControls(
            isPlaying = isPlaying,
            shuffleOn = shuffleOn,
            repeatOn = repeatOn,
            onShuffleClick = onShuffleClick,
            onPreviousClick = onPreviousClick,
            onPlayPauseClick = onPlayPauseClick,
            onNextClick = onNextClick,
            onRepeatClick = onRepeatClick
        )
        Spacer(Modifier.height(32.dp))
    }
}

// Collapse arrow · "Playing from" label · options
@Composable
private fun NowPlayingTopBar(playingFrom: String, onCollapseClick: () -> Unit, onMoreClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onCollapseClick) {
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = "Close player",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "PLAYING FROM",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                playingFrom,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(onClick = onMoreClick) {
            Icon(
                Icons.Rounded.MoreVert,
                contentDescription = "Song options",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// Stand-in for album art (thumbnails are post-MVP): rounded card with rings and a note
@Composable
private fun ArtworkPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(32.dp))
            .background(
                Brush.linearGradient(
                    listOf(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Rings from outside in, each a little darker
        listOf(0.75f, 0.55f, 0.35f).forEach { fraction ->
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .aspectRatio(1f)
                    .background(Color.Black.copy(alpha = 0.15f), CircleShape)
            )
        }
        Icon(
            Icons.Rounded.MusicNote,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun SongInfo(title: String, artist: String?, album: String?) {
    // "Artist • Album", leaving out whichever is missing
    val subtitle = listOfNotNull(artist, album).joinToString(" • ")
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (subtitle.isNotEmpty()) {
            Text(
                subtitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // custom thumb/track slots
@Composable
private fun SeekBar(progress: Float, positionText: String, durationText: String, onSeek: (Float) -> Unit) {
    val colors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    )
    Column(Modifier.fillMaxWidth()) {
        // Thin track + round thumb like the mockup (the Material default is a thick track with a bar thumb)
        Slider(
            value = progress,
            onValueChange = onSeek,
            colors = colors,
            thumb = {
                Box(
                    Modifier
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    sliderState = sliderState,
                    modifier = Modifier.height(4.dp),
                    colors = colors,
                    drawStopIndicator = null,
                    thumbTrackGapSize = 0.dp
                )
            }
        )
        Row(Modifier.fillMaxWidth()) {
            Text(
                positionText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Text(
                durationText,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Shuffle · previous · play/pause · next · repeat
@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    shuffleOn: Boolean,
    repeatOn: Boolean,
    onShuffleClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onRepeatClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ToggleControl(Icons.Rounded.Shuffle, "Shuffle", isOn = shuffleOn, onClick = onShuffleClick)
        IconButton(onClick = onPreviousClick, modifier = Modifier.size(56.dp)) {
            Icon(
                Icons.Rounded.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Surface(
            onClick = onPlayPauseClick,
            modifier = Modifier
                .size(76.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary,
                    spotColor = MaterialTheme.colorScheme.primary
                ),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        IconButton(onClick = onNextClick, modifier = Modifier.size(56.dp)) {
            Icon(
                Icons.Rounded.SkipNext,
                contentDescription = "Next",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        ToggleControl(Icons.Rounded.Repeat, "Repeat", isOn = repeatOn, onClick = onRepeatClick)
    }
}

// Shuffle/Repeat: emerald + a small dot underneath when on, grey when off
@Composable
private fun ToggleControl(icon: ImageVector, label: String, isOn: Boolean, onClick: () -> Unit) {
    val tint = if (isOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = label, tint = tint)
        }
        Box(
            Modifier
                .size(4.dp)
                .background(if (isOn) tint else Color.Transparent, CircleShape)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, heightDp = 800)
@Composable
private fun NowPlayingPlayingPreview() {
    MusicAppTheme {
        NowPlayingScreen(
            title = "Resonance", artist = "HOME", album = "Odyssey", playingFrom = "Library",
            isPlaying = true, progress = 0.26f, positionText = "1:12", durationText = "4:40",
            shuffleOn = true, repeatOn = false,
            onCollapseClick = { }, onMoreClick = { }, onSeek = { }, onShuffleClick = { },
            onPreviousClick = { }, onPlayPauseClick = { }, onNextClick = { }, onRepeatClick = { },
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212, heightDp = 800)
@Composable
private fun NowPlayingPausedPreview() {
    MusicAppTheme {
        NowPlayingScreen(
            title = "Midnight City", artist = "M83", album = null, playingFrom = "Late Night",
            isPlaying = false, progress = 0.26f, positionText = "1:12", durationText = "4:03",
            shuffleOn = false, repeatOn = true,
            onCollapseClick = { }, onMoreClick = { }, onSeek = { }, onShuffleClick = { },
            onPreviousClick = { }, onPlayPauseClick = { }, onNextClick = { }, onRepeatClick = { },
        )
    }
}
