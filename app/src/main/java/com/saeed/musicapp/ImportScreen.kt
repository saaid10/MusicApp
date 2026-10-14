package com.saeed.musicapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun ImportScreen() {
    var url by remember { mutableStateOf("") }

    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Import", style = MaterialTheme.typography.headlineLarge)
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = url,
            onValueChange = { url = it }, label = { Text("URL") },
            shape = RectangleShape,
            colors = fieldColors(MaterialTheme.colorScheme.surfaceVariant)
        )

        // Metadata card: groups title, artist and album in one box
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RectangleShape
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                MetaDataField(label = "Song title", value = title, onValueChange = { title = it }, placeholder = "Song goes here", icon = Icons.Rounded.MusicNote, required = true)
                MetaDataField(label = "Artist", value = artist, onValueChange = {artist = it}, placeholder = "Artist (optional)", icon = Icons.Rounded.Mic, required = false)
                MetaDataField(label = "Album", value = album, onValueChange = {album = it}, placeholder = "Album (optional)", Icons.Rounded.Album, required = false)
                
            }
        }

        // Taller pill with a soft emerald glow
        Button(
            onClick = {},
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

@Composable private fun MetaDataField (label: String, value: String, onValueChange: (String) -> Unit, placeholder: String, icon: ImageVector, required: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row() {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
            if (required)
            {
                Text(
                    " *",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        OutlinedTextField(
            leadingIcon = { Icon(icon, contentDescription = null)},
            placeholder = {Text(placeholder)},
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            shape = RectangleShape,
            colors = fieldColors(MaterialTheme.colorScheme.surfaceContainerHigh)
        )
    }
}

// Filled box, no border unless focused (focused border stays emerald)
@Composable
private fun fieldColors(fill: Color): TextFieldColors = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = fill,
    focusedContainerColor = fill,
    unfocusedBorderColor = Color.Transparent
)
