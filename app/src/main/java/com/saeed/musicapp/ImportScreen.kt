package com.saeed.musicapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable fun ImportScreen() {
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }

    Column (modifier = Modifier.safeDrawingPadding().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Import", style = MaterialTheme.typography.headlineLarge)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value =  url, onValueChange = {url = it}, label = { Text("URL") })
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = title, onValueChange = {title = it}, label = {Text("Song title")} )
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = artist, onValueChange = {artist = it}, label = {Text("Artist (optional)")})
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = album, onValueChange = {album = it}, label = {Text("Album (optional)")})
        Button(onClick = {}, modifier = Modifier.fillMaxWidth()) { Text("Import") }
    }
}