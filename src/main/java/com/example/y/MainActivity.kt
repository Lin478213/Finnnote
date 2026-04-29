package com.example.y

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.y.ui.component.glass.LiquidGlassBottomBar
import com.example.y.ui.navigation.AppNavHost
import com.example.y.ui.navigation.TopDestination
import com.example.y.ui.screen.add.AddTransactionSheet
import com.example.y.ui.theme.LocalLiquidGlassBackdrop
import com.example.y.ui.theme.YTheme
import com.example.y.ui.viewmodel.SettingsViewModel
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsVm: SettingsViewModel = hiltViewModel()
            val bgSettings by settingsVm.backgroundSettings.collectAsStateWithLifecycle()

            YTheme {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar =
                    currentRoute == null || TopDestination.entries.any { it.route == currentRoute }
                val snackbarHostState = remember { SnackbarHostState() }
                var showAddSheet by rememberSaveable { mutableStateOf(false) }

                val hasBg = bgSettings.enabled && bgSettings.imageUri.isNotEmpty()
                val backdrop = rememberLayerBackdrop()

                Box(modifier = Modifier.fillMaxSize()) {
                    // ===== 第1层：全局背景（被 Backdrop 捕获用于玻璃效果） =====
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(backdrop),
                    ) {
                        if (hasBg) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(Uri.parse(bgSettings.imageUri))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (bgSettings.blurRadius > 0f)
                                            Modifier.blur(bgSettings.blurRadius.dp, BlurredEdgeTreatment.Rectangle)
                                        else Modifier,
                                    ),
                            )
                            if (bgSettings.dimAlpha > 0f) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = bgSettings.dimAlpha)),
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background),
                            )
                        }
                    }

                    CompositionLocalProvider(LocalLiquidGlassBackdrop provides backdrop) {
                        // ===== 第2层：主内容 Scaffold（最上层） =====
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = Color.Transparent,
                            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                            bottomBar = {
                                AnimatedVisibility(
                                    visible = showBottomBar,
                                    enter = fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 40)) +
                                            slideInVertically(
                                                animationSpec = tween(
                                                    durationMillis = 360,
                                                    easing = FastOutSlowInEasing,
                                                ),
                                                initialOffsetY = { fullHeight -> fullHeight / 2 },
                                            ),
                                    exit = fadeOut(animationSpec = tween(durationMillis = 160)) +
                                            slideOutVertically(
                                                animationSpec = tween(
                                                    durationMillis = 240,
                                                    easing = FastOutSlowInEasing,
                                                ),
                                                targetOffsetY = { fullHeight -> fullHeight / 2 },
                                            ),
                                ) {
                                    LiquidGlassBottomBar(
                                        destinations = TopDestination.entries,
                                        currentRoute = currentRoute,
                                        backdrop = backdrop,
                                        onDestinationSelected = { destination ->
                                            navController.navigate(destination.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                    )
                                }
                            },
                        ) { innerPadding ->
                            AppNavHost(
                                navController = navController,
                                onShowAddSheet = { showAddSheet = true },
                                modifier = Modifier.padding(innerPadding),
                            )
                        }

                        // ===== 第3层：BottomSheet 模态框（浮在内容上方） =====
                        if (showAddSheet) {
                            AddTransactionSheet(onDismiss = { showAddSheet = false })
                        }
                    }
                }
            }
        }
    }
}
