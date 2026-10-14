package com.saeed.musicapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

@Composable fun ImportScreen() {
    var url by remember { mutableStateOf("") }

    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }

    Column (modifier = Modifier.safeDrawingPadding().padding(horizontal = 16.dp, vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Import", style = MaterialTheme.typography.headlineLarge)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(),
            value =  url,
            onValueChange = {url = it}, label = { Text("URL") },
            shape = RectangleShape,
            colors = fieldColors(MaterialTheme.colorScheme.surfaceVariant))

        // Metadata card: groups title, artist and album in one box
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RectangleShape
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(modifier = Modifier.fillMaxWidth(),
                    value = title,
                    onValueChange = {title = it}, label = {Text("Song title")},
                    shape = RectangleShape,
                    colors = fieldColors(MaterialTheme.colorScheme.surfaceContainerHigh))

                OutlinedTextField(modifier = Modifier.fillMaxWidth(),
                    value = artist,
                    onValueChange = {artist = it}, label = {Text("Artist (optional)")},
                    shape = RectangleShape,
                    colors = fieldColors(MaterialTheme.colorScheme.surfaceContainerHigh))

                OutlinedTextField(modifier = Modifier.fillMaxWidth(),
                    value = album,
                    onValueChange = {album = it}, label = {Text("Album (optional)")},
                    shape = RectangleShape,
                    colors = fieldColors(MaterialTheme.colorScheme.surfaceContainerHigh))
            }
        }

        // Taller pill with a soft emerald glow
        Button(onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.primary,
                    spotColor = MaterialTheme.colorScheme.primary
                )
        ) { Text("Import") }
    }
}

// Filled box, no border unless focused (focused border stays emerald)
@Composable
private fun fieldColors(fill: Color): TextFieldColors = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = fill,
    focusedContainerColor = fill,
    unfocusedBorderColor = Color.Transparent
)
