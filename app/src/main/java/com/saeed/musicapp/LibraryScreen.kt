package com.saeed.musicapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.SdStorage
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.saeed.musicapp.ui.theme.MusicAppTheme

// Library screen LOOK only (Stitch "Library" mockups). Every hook is empty: the
// behaviour (ViewModel, filtering, which state to show) is wired up separately.

@Composable
fun LibraryScreen(
    songs: List<Song>,
    query: String,
    onQueryChange: (String) -> Unit,
    storageUsed: String,
    onStorageClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onSongClick: (Song) -> Unit,
    onSongMoreClick: (Song) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        LibraryHeader(storageUsed = storageUsed, onStorageClick = onStorageClick)
        Spacer(Modifier.height(16.dp))
        LibrarySearchField(query = query, onQueryChange = onQueryChange)
        Spacer(Modifier.height(16.dp))
        ShuffleRow(songCount = songs.size, onShuffleClick = onShuffleClick)
        Spacer(Modifier.height(8.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(songs, key = { it.videoId }) { song ->
                SongRow(
                    title = song.title,
                    artist = song.artist,
                    durationText = null, // TODO: format song.duration as "m:ss"
                    isPlaying = false,
                    onClick = { onSongClick(song) },
                    onMoreClick = { onSongMoreClick(song) }
                )
            }
            item { OfflineFooter() }
        }
    }
}

@Composable
private fun LibraryHeader(storageUsed: String, onStorageClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Library", style = MaterialTheme.typography.headlineLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Imported Tracks",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
                    Text(
                        "Offline",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Spacer(Modifier.weight(1f))
        Surface(
            onClick = onStorageClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Rounded.SdStorage,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "$storageUsed used",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun LibrarySearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search your songs") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Clear search")
                }
            }
        },
        singleLine = true,
        shape = CircleShape,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    )
}

@Composable
private fun ShuffleRow(songCount: Int, onShuffleClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onShuffleClick, contentPadding = PaddingValues(horizontal = 20.dp)) {
            Icon(Icons.Rounded.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Shuffle play", style = MaterialTheme.typography.titleSmall)
        }
        Spacer(Modifier.weight(1f))
        Text(
            "$songCount songs",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// One song in the list. Emerald highlight + equalizer icon when it is the playing song.
@Composable
fun SongRow(
    title: String,
    artist: String?,
    durationText: String?,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
) {
    val accent = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = if (isPlaying) accent.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SongPlaceholderIcon(icon = if (isPlaying) Icons.Rounded.Equalizer else Icons.Rounded.MusicNote)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isPlaying) accent else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (artist != null) {
                    Text(
                        artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (durationText != null) {
                Text(
                    durationText,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isPlaying) accent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onMoreClick) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = "Song options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// Stand-in for album art (thumbnails are post-MVP)
@Composable
private fun SongPlaceholderIcon(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun OfflineFooter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.CloudOff,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.width(6.dp))
        Text(
            "All tracks saved for offline playback",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Shown instead of the list when the library has no songs at all
@Composable
fun LibraryEmptyState(onImportClick: () -> Unit) {
    CenteredMessage(
        icon = Icons.Rounded.LibraryMusic,
        title = "Your library is empty",
        body = "Import a song from a link to get started"
    ) {
        Button(onClick = onImportClick, contentPadding = PaddingValues(horizontal = 24.dp)) {
            Icon(Icons.Rounded.Download, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Import a song", style = MaterialTheme.typography.titleSmall)
        }
    }
}

// Shown instead of the list when the search matches nothing
@Composable
fun NoSearchResults(query: String) {
    CenteredMessage(
        icon = Icons.Rounded.SearchOff,
        title = "No songs match “$query”",
        body = "Check the spelling, or import the song from a link"
    )
}

@Composable
private fun CenteredMessage(
    icon: ImageVector,
    title: String,
    body: String,
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier
                    .padding(20.dp)
                    .size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (action != null) {
            Spacer(Modifier.height(4.dp))
            action()
        }
    }
}

// The 3 tabs, in the order decided in docs/06
enum class AppTab(val label: String, val icon: ImageVector) {
    Library("Library", Icons.AutoMirrored.Rounded.LibraryBooks),
    Playlists("Playlists", Icons.AutoMirrored.Rounded.QueueMusic),
    Import("Import", Icons.Rounded.Download),
}

@Composable
fun AppBottomBar(selected: AppTab, onTabClick: (AppTab) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        AppTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onTabClick(tab) },
                icon = { Icon(tab.icon, contentDescription = null) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            )
        }
    }
}

// Sample data so the screen can be seen in Android Studio's preview pane
private val previewSongs = listOf(
    Song("a1", "Luv(sic)", "Nujabes", null, 0L, 280L),
    Song("a2", "Resonance", "HOME", "Odyssey", 0L, 212L),
    Song("a3", "Midnight City", "M83", null, 0L, 243L),
    Song("a4", "Untitled voice memo", null, null, 0L, null),
)

@Preview(showBackground = true, backgroundColor = 0xFF121212, heightDp = 800)
@Composable
private fun LibraryScreenPreview() {
    MusicAppTheme {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                LibraryScreen(
                    songs = previewSongs,
                    query = "",
                    onQueryChange = { },
                    storageUsed = "1.2 GB",
                    onStorageClick = { },
                    onShuffleClick = { },
                    onSongClick = { },
                    onSongMoreClick = { },
                )
            }
            AppBottomBar(selected = AppTab.Library, onTabClick = { })
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun LibraryEmptyPreview() {
    MusicAppTheme { LibraryEmptyState(onImportClick = { }) }
}

@Preview(showBackground = true, backgroundColor = 0xFF121212)
@Composable
private fun NoSearchResultsPreview() {
    MusicAppTheme { NoSearchResults(query = "nujabes") }
}
