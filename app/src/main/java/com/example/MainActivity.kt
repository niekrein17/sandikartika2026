package com.example

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.TacticalNavTab
import com.example.ui.components.AppDisguiseDialog
import com.example.ui.components.TacticalHeader
import com.example.ui.components.WebSuitePreviewDialog
import com.example.ui.screens.CalculatorDisguiseScreen
import com.example.ui.screens.EmbedScreen
import com.example.ui.screens.ExtractScreen
import com.example.ui.screens.InspectorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TacticalAlertRed
import com.example.ui.theme.TacticalAlertRedDim
import com.example.ui.theme.TacticalBlack
import com.example.ui.theme.TacticalCardBorder
import com.example.ui.theme.TacticalDarkSurface
import com.example.ui.theme.TacticalEmerald
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary
import com.example.util.AppDisguiseManager

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainContent(
                    viewModel = viewModel,
                    onVibrate = { triggerHapticFeedback() }
                )
            }
        }
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val v = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                v?.vibrate(80)
            }
        } catch (_: Exception) {}
    }
}

@Composable
fun MainContent(
    viewModel: MainViewModel,
    onVibrate: () -> Unit
) {
    val isCamouflageActive by viewModel.isCamouflageDisguiseActive.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val panicNotice by viewModel.panicWipeNotice.collectAsState()
    val showDisguiseDialog by viewModel.showDisguiseDialog.collectAsState()
    val activeDisguise by viewModel.activeDisguiseProfile.collectAsState()
    val showWebSuitePreview by viewModel.showWebSuitePreview.collectAsState()
    var showPanicDialog by remember { mutableStateOf(false) }

    // If camouflage disguise mode is on, render authentic working calculator
    if (isCamouflageActive) {
        CalculatorDisguiseScreen(
            onUnlockTacticalSuite = {
                viewModel.setCamouflageDisguiseActive(false)
                onVibrate()
            }
        )
        return
    }

    // Handle back button: if not on EMBED tab, back returns to EMBED
    BackHandler(enabled = currentTab != TacticalNavTab.EMBED) {
        viewModel.selectTab(TacticalNavTab.EMBED)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TacticalHeader(
                onPanicZeroize = { showPanicDialog = true },
                onLockCamouflage = {
                    viewModel.setCamouflageDisguiseActive(true)
                    onVibrate()
                },
                onOpenDisguise = {
                    viewModel.setDisguiseDialogVisible(true)
                    onVibrate()
                },
                onOpenWebSuite = {
                    viewModel.setWebSuitePreviewVisible(true)
                    onVibrate()
                },
                activeDisguiseName = activeDisguise.displayName
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = TacticalDarkSurface,
                tonalElevation = 8.dp
            ) {
                TacticalNavTab.values().forEach { tab ->
                    val isSelected = currentTab == tab
                    val (icon, label) = when (tab) {
                        TacticalNavTab.EMBED -> Pair(Icons.Default.Lock, "SISIP")
                        TacticalNavTab.EXTRACT -> Pair(Icons.Default.LockOpen, "URAI")
                        TacticalNavTab.INSPECTOR -> Pair(Icons.Default.Analytics, "INSPEKTUR")
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            viewModel.selectTab(tab)
                            onVibrate()
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontFamily = FontFamily.Monospace
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = TacticalBlack,
                            selectedTextColor = TacticalEmerald,
                            indicatorColor = TacticalEmerald,
                            unselectedIconColor = TacticalTextSecondary,
                            unselectedTextColor = TacticalTextSecondary
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TacticalBlack)
                .padding(innerPadding)
        ) {
            when (currentTab) {
                TacticalNavTab.EMBED -> EmbedScreen(viewModel = viewModel)
                TacticalNavTab.EXTRACT -> ExtractScreen(viewModel = viewModel)
                TacticalNavTab.INSPECTOR -> InspectorScreen(viewModel = viewModel)
            }

            // Panic notice overlay banner
            AnimatedVisibility(
                visible = panicNotice != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                if (panicNotice != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TacticalAlertRedDim)
                            .border(1.dp, TacticalAlertRed, RoundedCornerShape(8.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = null,
                                    tint = TacticalAlertRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PANIC ZEROIZE AKTIF",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = TacticalAlertRed,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = panicNotice!!,
                                fontSize = 10.5.sp,
                                color = TacticalTextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.dismissPanicNotice() },
                                colors = ButtonDefaults.buttonColors(containerColor = TacticalAlertRed, contentColor = TacticalBlack),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("TUTUP PEMBERITAHUAN", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Emergency Panic Zeroize Confirmation Dialog
    if (showPanicDialog) {
        AlertDialog(
            onDismissRequest = { showPanicDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = TacticalAlertRed,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "EMERGENCY ZEROIZE (PANIK)",
                        color = TacticalAlertRed,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Apakah Anda yakin ingin segera menghapus dan menolkan seluruh memori taktis?",
                        fontSize = 12.5.sp,
                        color = TacticalTextPrimary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tindakan ini akan menghapus seketika:\n• Semua kata sandi dalam memori RAM\n• Teks instruksi rahasia dan koordinat\n• Media stego aktif dari cache memori",
                        fontSize = 11.sp,
                        color = TacticalTextSecondary,
                        lineHeight = 16.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPanicDialog = false
                        viewModel.triggerEmergencyZeroize()
                        onVibrate()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalAlertRed,
                        contentColor = TacticalBlack
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("confirm_panic_button")
                ) {
                    Text(
                        text = "MUSNAHKAN MEMORI",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showPanicDialog = false },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("BATAL", fontSize = 11.sp, color = TacticalTextSecondary)
                }
            },
            containerColor = TacticalDarkSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }

    // App Icon & Launcher Identity Disguise Dialog
    if (showDisguiseDialog) {
        AppDisguiseDialog(
            currentProfile = activeDisguise,
            onSelectProfile = { profile ->
                viewModel.selectDisguiseProfile(profile)
                onVibrate()
            },
            onDismiss = { viewModel.setDisguiseDialogVisible(false) }
        )
    }

    // 100% Offline Single-File HTML Web Suite Preview Dialog
    if (showWebSuitePreview) {
        val (htmlFile, htmlUri) = viewModel.getStandaloneHtmlFile()
        WebSuitePreviewDialog(
            htmlFile = htmlFile,
            htmlUri = htmlUri,
            onDismiss = { viewModel.setWebSuitePreviewVisible(false) }
        )
    }
}
