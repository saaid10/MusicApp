package com.saeed.musicapp

sealed interface ImportUiState {
    data object Idle : ImportUiState
    data object  Importing : ImportUiState
    data class Success(val title: String, val artist: String?) : ImportUiState
    data class AlreadyInLibrary(val importedAudio: ImportedAudio) : ImportUiState
    data class Failed(val failedMsg: String, val failedHint: String) : ImportUiState
}