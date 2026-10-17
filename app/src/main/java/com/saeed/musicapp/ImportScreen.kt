package com.saeed.musicapp

import android.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.saeed.musicapp.ui.theme.Amber
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ImportScreen(viewModel: ImportViewModel) {
    val clipboard = LocalClipboardManager.current
    var url by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var album by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        )
        {
            Text("Import", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Add music to your offline library from web links",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        MetaDataField(
            label = "Source URL",
            value = url,
            onValueChange = { url = it },
            placeholder = "Paste a link",
            icon = Icons.Rounded.Link,
            required = true,
            trailing = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (url.isNotEmpty()) {
                        IconButton(onClick = { url = "" }) {
                            Icon(Icons.Rounded.Cancel, contentDescription = "Clear link")
                        }
                    }
                    Surface(
                        onClick = {url = clipboard.getText()?.text ?: url},
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        )
                        {
                            Icon(Icons.Rounded.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Text("Paste", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                }
            }
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                )
                {
                    Icon(
                        Icons.Rounded.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Track Metadata",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                MetaDataField(
                    label = "Song title",
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Song goes here",
                    icon = Icons.Rounded.MusicNote,
                    required = true
                )
                MetaDataField(
                    label = "Artist",
                    value = artist,
                    onValueChange = { artist = it },
                    placeholder = "Artist",
                    icon = Icons.Rounded.Mic,
                    required = false
                )
                MetaDataField(
                    label = "Album",
                    value = album,
                    onValueChange = { album = it },
                    placeholder = "Album",
                    icon = Icons.Rounded.Album,
                    required = false
                )

            }
        }
        val canImport = url.startsWith("http") && title.isNotBlank() && state !is ImportUiState.Importing
        // Taller pill with a soft emerald glow
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { viewModel.importAudio(url, title, artist.ifBlank { null }, album.ifBlank { null })},
                enabled = canImport,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        ambientColor = MaterialTheme.colorScheme.primary,
                        spotColor = MaterialTheme.colorScheme.primary
                    )
            ) { Text("Import", style = MaterialTheme.typography.titleMedium) }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                Text("Imported files are stored locally on your device.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        when (state) {
            is ImportUiState.Idle -> {}
            is ImportUiState.Importing -> {Text("Importing...")}
            is ImportUiState.Success -> {Text("Saved to library")}
            is ImportUiState.AlreadyInLibrary -> {AlreadyInLibrarySheet(
                title = title,
                artist = artist.ifBlank { null },
                onSkip = {
                    viewModel.resetState()
                    url = ""
                    title = ""
                    artist = ""
                    album = ""
                },
                onReplace = {},
                onDismiss = {viewModel.resetState()},)}
            is ImportUiState.Failed -> {Text("Import failed")}
        }
    }
}

@Composable
private fun MetaDataField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    required: Boolean = false,
    trailing: (@Composable () -> Unit)? = null
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {

            Text(
                label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (required) {
                Text(
                    " *",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.weight(1f))
            if (!required) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                )
                {
                    Text(
                        "Optional",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        OutlinedTextField(
            leadingIcon = { Icon(icon, contentDescription = null) },
            trailingIcon = trailing,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            value = value,
            onValueChange = onValueChange,
            shape = RectangleShape,
            colors = fieldColors(MaterialTheme.colorScheme.surfaceContainerHigh)
        )
    }
}

// Bottom sheet over the dimmed form, shown when the song's file already exists.
// Title/artist are what the user typed (no stored metadata until Room)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlreadyInLibrarySheet(
    title: String,
    artist: String?,
    onSkip: () -> Unit,
    onReplace: () -> Unit,
    onDismiss: () -> Unit // swipe down / tap outside
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        // Floating card: gap at sides/bottom, all corners rounded
        modifier = Modifier
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        shape = RoundedCornerShape(32.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        scrimColor = Color.Black.copy(alpha = 0.7f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amber icon in a soft amber circle (warning, not error)
            Surface(shape = CircleShape, color = Amber.copy(alpha = 0.15f)) {
                Icon(
                    Icons.Rounded.LibraryMusic,
                    contentDescription = null,
                    modifier = Modifier.padding(14.dp).size(28.dp),
                    tint = Amber
                )
            }
            Text(
                "Already in Library",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            // Title in bold, rest normal
            Text(
                buildAnnotatedString {
                    append("This song is already in your library as ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                        append("‘$title’")
                    }
                    if (artist != null) append(" by $artist")
                    append(". Would you like to skip it or replace the existing file?")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            // Skip = main (green, glowing) action, Replace = dark secondary with green icon
            Button(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = CircleShape,
                        ambientColor = MaterialTheme.colorScheme.primary,
                        spotColor = MaterialTheme.colorScheme.primary
                    )
            ) {
                Icon(Icons.Rounded.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Skip", style = MaterialTheme.typography.titleSmall)
            }
            FilledTonalButton(
                onClick = onReplace,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    Icons.Rounded.Autorenew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text("Replace Existing", style = MaterialTheme.typography.titleSmall)
            }
        }
    }
}

// Filled box, no border unless focused (focused border stays emerald)
@Composable
private fun fieldColors(fill: Color): TextFieldColors = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = fill,
    focusedContainerColor = fill,
    unfocusedBorderColor = Color.Transparent
)
