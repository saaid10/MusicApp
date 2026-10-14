package com.saeed.musicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.saeed.musicapp.ui.theme.MusicAppTheme
import org.schabi.newpipe.extractor.NewPipe

class MainActivity : ComponentActivity() {
    private val viewModel: ImportViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NewPipe.init(DownloaderImpl())

        viewModel.importAudio("https://www.youtube.com/watch?v=Y4HWvsGs0rY", "Love-Sic", "Nujabes", null)

        setContent {
            MusicAppTheme {
                Surface(modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp)) {
                    ImportScreen()
                }
            }
        }
    }
}
