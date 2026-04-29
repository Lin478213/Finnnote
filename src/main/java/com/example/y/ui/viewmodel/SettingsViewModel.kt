package com.example.y.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.y.data.repository.BackgroundSettings
import com.example.y.data.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: UserPreferencesRepository,
) : ViewModel() {

    /** 背景设置状态 */
    val backgroundSettings: StateFlow<BackgroundSettings> =
        prefs.backgroundSettings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BackgroundSettings(),
        )

    fun setBackgroundEnabled(enabled: Boolean) {
        viewModelScope.launch { prefs.updateBackgroundEnabled(enabled) }
    }

    fun setBackgroundImage(uri: Uri?) {
        viewModelScope.launch { prefs.updateBackgroundImage(uri) }
    }

    fun setBlurRadius(radius: Float) {
        viewModelScope.launch { prefs.updateBlurRadius(radius) }
    }

    fun setDimAlpha(alpha: Float) {
        viewModelScope.launch { prefs.updateDimAlpha(alpha) }
    }
}
