package com.saeed.musicapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import org.schabi.newpipe.extractor.NewPipe

class MainActivity : ComponentActivity() {
    private val viewModel: ImportViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NewPipe.init(DownloaderImpl())

        viewModel.importAudio("https://www.youtube.com/watch?v=Y4HWvsGs0rY")

        setContent {
            MaterialTheme {
                Surface {
                    Text("MusicApp")
                }
            }
        }
    }
}
