package com.saeed.musicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saeed.musicapp.ui.theme.MusicAppTheme
import org.schabi.newpipe.extractor.NewPipe

class MainActivity : ComponentActivity() {
    private val viewModel: ImportViewModel by viewModels()
    private val libraryViewModel: LibraryViewModel by viewModels()
    private val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NewPipe.init(DownloaderImpl())

        setContent {
            MusicAppTheme {
                var selectedTab by rememberSaveable { mutableStateOf(AppTab.Library) }
                var query by rememberSaveable { mutableStateOf("") }
                val songs by libraryViewModel.songs.collectAsStateWithLifecycle()
                val filteredSongs = songs.orEmpty().filter {
                    it.title.contains(query,ignoreCase = true) ||
                            it.artist?.contains(query, ignoreCase = true) == true ||
                            it.album?.contains(query, ignoreCase = true) == true
                }
                Scaffold(
                    bottomBar = { AppBottomBar(selected = selectedTab, onTabClick = { selectedTab = it }) }
                ) { innerPadding ->
                    // innerPadding = status bar on top + bottom bar height; consumed so the
                    // screens' own safeDrawingPadding doesn't add the same space again
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                    ) {
                        when(selectedTab) {
                            AppTab.Library -> {
                                if (songs == null) {
                                    // Room has not answered yet: draw nothing instead of flashing the empty state
                                } else if (songs?.isEmpty() == true) {
                                    LibraryEmptyState( onImportClick = {selectedTab = AppTab.Import} )
                                } else {
                                    LibraryScreen(
                                        filteredSongs,
                                        query = query,
                                        onQueryChange = { query = it },
                                        storageUsed = "1.2 GB",
                                        onStorageClick = { },
                                        onShuffleClick = { },
                                        onSongClick = { playerViewModel.playSong(it) },
                                        onSongMoreClick = { },
                                    )
                                }
                            }
                            AppTab.Playlists -> Text("Playlists coming soon")
                            AppTab.Import -> ImportScreen(viewModel)
                        }
                    }
                }
            }
        }
    }
}
