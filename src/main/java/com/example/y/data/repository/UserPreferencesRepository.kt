package com.example.y.data.repository

import android.content.Context
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** 背景设置数据类 */
data class BackgroundSettings(
    /** 是否启用自定义背景 */
    val enabled: Boolean = false,
    /** 背景图片 URI（来自相册） */
    val imageUri: String = "",
    /** 模糊程度 0f ~ 25f */
    val blurRadius: Float = 0f,
    /** 背景暗度覆盖层 0f ~ 0.8f，用于保证前景内容可读 */
    val dimAlpha: Float = 0.3f,
)

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val BG_ENABLED = booleanPreferencesKey("bg_enabled")
        val BG_IMAGE_URI = stringPreferencesKey("bg_image_uri")
        val BG_BLUR_RADIUS = floatPreferencesKey("bg_blur_radius")
        val BG_DIM_ALPHA = floatPreferencesKey("bg_dim_alpha")
    }

    // ---- 背景设置 ----

    val backgroundSettings: Flow<BackgroundSettings> = context.dataStore.data.map { prefs ->
        BackgroundSettings(
            enabled = prefs[Keys.BG_ENABLED] ?: false,
            imageUri = prefs[Keys.BG_IMAGE_URI] ?: "",
            blurRadius = prefs[Keys.BG_BLUR_RADIUS] ?: 0f,
            dimAlpha = prefs[Keys.BG_DIM_ALPHA] ?: 0.3f,
        )
    }

    suspend fun updateBackgroundEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.BG_ENABLED] = enabled }
    }

    suspend fun updateBackgroundImage(uri: Uri?) {
        context.dataStore.edit { it[Keys.BG_IMAGE_URI] = uri?.toString() ?: "" }
    }

    suspend fun updateBlurRadius(radius: Float) {
        context.dataStore.edit { it[Keys.BG_BLUR_RADIUS] = radius.coerceIn(0f, 25f) }
    }

    suspend fun updateDimAlpha(alpha: Float) {
        context.dataStore.edit { it[Keys.BG_DIM_ALPHA] = alpha.coerceIn(0f, 0.8f) }
    }
}
