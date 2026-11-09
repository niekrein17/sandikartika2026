package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TacticalBlack
import com.example.ui.theme.TacticalCardBorder
import com.example.ui.theme.TacticalCardSurface
import com.example.ui.theme.TacticalCyan
import com.example.ui.theme.TacticalDarkSurface
import com.example.ui.theme.TacticalEmerald
import com.example.ui.theme.TacticalGold
import com.example.ui.theme.TacticalTextMuted
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary
import com.example.util.AppDisguiseManager

@Composable
fun AppDisguiseDialog(
    currentProfile: AppDisguiseManager.AppDisguiseProfile,
    onSelectProfile: (AppDisguiseManager.AppDisguiseProfile) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = TacticalEmerald,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SAMARKAN IKON & NAMA APLIKASI",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TacticalEmerald,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Pilih kedok identitas aplikasi di layar beranda (Home Screen) dan daftar aplikasi HP agar tidak memicu kecurigaan saat pemeriksaan fisik:",
                    fontSize = 11.sp,
                    color = TacticalTextPrimary,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                AppDisguiseManager.AppDisguiseProfile.values().forEach { profile ->
                    val isSelected = profile == currentProfile
                    val borderCol = if (isSelected) TacticalEmerald else TacticalCardBorder
                    val bgCol = if (isSelected) TacticalEmerald.copy(alpha = 0.12f) else TacticalDarkSurface

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(bgCol)
                            .border(1.dp, borderCol, RoundedCornerShape(8.dp))
                            .clickable { onSelectProfile(profile) }
                            .padding(10.dp)
                            .testTag("disguise_option_${profile.name.lowercase()}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Image(
                                    painter = painterResource(id = profile.iconRes),
                                    contentDescription = profile.displayName,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = profile.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) TacticalEmerald else TacticalTextPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = profile.subtitle,
                                        fontSize = 9.5.sp,
                                        color = TacticalTextSecondary
                                    )
                                }
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = TacticalEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "*Catatan Taktis: Peluncur Android (Launcher) memerlukan beberapa detik untuk memperbarui gambar ikon dan label di layar utama.",
                    fontSize = 8.5.sp,
                    color = TacticalTextMuted,
                    lineHeight = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TacticalEmerald,
                    contentColor = TacticalBlack
                ),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "SELESAI",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        },
        containerColor = TacticalCardSurface,
        shape = RoundedCornerShape(12.dp)
    )
}
