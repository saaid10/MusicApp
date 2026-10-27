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
import com.saeed.musicapp.ui.theme.MusicAppTheme
import org.schabi.newpipe.extractor.NewPipe

class MainActivity : ComponentActivity() {
    private val viewModel: ImportViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NewPipe.init(DownloaderImpl())

        setContent {
            MusicAppTheme {
                var selectedTab by rememberSaveable { mutableStateOf(AppTab.Library) }
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
                                LibraryScreen(
                                    // TEMPORARY sample songs: replaced by the database songs (step 2)
                                    songs = listOf(
                                        Song("a1", "Luv(sic)", "Nujabes", null, 0L, 280L),
                                        Song("a2", "Resonance", "HOME", "Odyssey", 0L, 212L),
                                        Song("a3", "Untitled voice memo", null, null, 0L, null),
                                    ),
                                    query = "",
                                    onQueryChange = { },
                                    storageUsed = "1.2 GB",
                                    onStorageClick = { },
                                    onShuffleClick = { },
                                    onSongClick = { },
                                    onSongMoreClick = { },
                                )
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
