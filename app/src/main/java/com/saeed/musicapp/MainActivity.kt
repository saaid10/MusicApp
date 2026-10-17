package com.saeed.musicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
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
                Surface(modifier = Modifier.fillMaxSize()) {
                    ImportScreen(viewModel)
                }
            }
        }
    }
}
