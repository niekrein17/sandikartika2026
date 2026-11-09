package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExtractionStatus
import com.example.data.model.StegoTransmissionMode
import com.example.ui.MainViewModel
import com.example.ui.components.TacticalCard
import com.example.ui.components.TacticalClassificationBadge
import com.example.ui.components.TacticalTextField
import com.example.ui.theme.TacticalAlertRed
import com.example.ui.theme.TacticalAlertRedDim
import com.example.ui.theme.TacticalBlack
import com.example.ui.theme.TacticalCardBorder
import com.example.ui.theme.TacticalCardBorderActive
import com.example.ui.theme.TacticalCyan
import com.example.ui.theme.TacticalDarkSurface
import com.example.ui.theme.TacticalDecoyBlue
import com.example.ui.theme.TacticalDecoyBorder
import com.example.ui.theme.TacticalDecoySurface
import com.example.ui.theme.TacticalEmerald
import com.example.ui.theme.TacticalGold
import com.example.ui.theme.TacticalTextMuted
import com.example.ui.theme.TacticalTextPrimary
import com.example.ui.theme.TacticalTextSecondary

@Composable
fun ExtractScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val extractState by viewModel.extractState.collectAsState()
    val embedState by viewModel.embedState.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.setExtractBitmapFromUri(uri)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(TacticalBlack),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Stego Image Input
        item {
            TacticalCard(
                title = "1. Citra Stego yang Diselidiki",
                badgeText = if (extractState.stegoBitmap != null) "MEDIA SIAP" else "BELUM ADA MEDIA",
                badgeColor = if (extractState.stegoBitmap != null) TacticalEmerald else TacticalTextMuted
            ) {
                if (extractState.stegoBitmap != null) {
                    val bmp = extractState.stegoBitmap!!
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TacticalDarkSurface)
                            .border(1.dp, TacticalCardBorder, RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = "Stego Image",
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(1.dp, TacticalEmerald, RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (extractState.simulatedJpegQuality != null) {
                                    "CITRA STEGO (TERKOMPRESI JPEG Q=${extractState.simulatedJpegQuality})"
                                } else {
                                    "CITRA TAKTIS BERMUATAN"
                                },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (extractState.simulatedJpegQuality != null) TacticalGold else TacticalEmerald,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Resolusi: ${bmp.width} x ${bmp.height} px",
                                fontSize = 10.sp,
                                color = TacticalTextSecondary,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.height(30.dp),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Ganti Foto...",
                                        fontSize = 9.5.sp,
                                        color = TacticalCyan,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }

                                OutlinedButton(
                                    onClick = { viewModel.simulateJpegCompressionOnExtract(80) },
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("simulate_jpeg_button"),
                                    shape = RoundedCornerShape(4.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Simulasi JPEG (Q=80)",
                                        fontSize = 9.5.sp,
                                        color = TacticalGold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TacticalDarkSurface)
                            .border(1.dp, TacticalCardBorder, RoundedCornerShape(8.dp))
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = TacticalEmerald,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "PILIH CITRA STEGO DARI PENYIMPANAN",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TacticalTextPrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Pilih file citra PNG yang diterima dari rekan operasi",
                                fontSize = 10.sp,
                                color = TacticalTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Password Input
        item {
            TacticalCard(
                title = "2. Kata Sandi Taktis",
                badgeText = "AUTENTIKASI",
                badgeColor = TacticalGold
            ) {
                Text(
                    text = "Ketikkan kata sandi operasional (Sandi Utama atau Sandi Umpan Darurat):",
                    fontSize = 11.sp,
                    color = TacticalTextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))

                TacticalTextField(
                    value = extractState.passwordInput,
                    onValueChange = { viewModel.setExtractPasswordInput(it) },
                    label = "Kata Sandi Taktis",
                    isPassword = true,
                    leadingIcon = Icons.Default.Key,
                    isMonospace = true,
                    placeholder = "Ketikkan sandi...",
                    testTag = "input_extract_password"
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Fast test presets row if embed state has passwords
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (embedState.masterPassword.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(TacticalAlertRedDim)
                                .border(1.dp, TacticalAlertRed.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .clickable { viewModel.setExtractPasswordInput(embedState.masterPassword) }
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sandi Master (${embedState.masterPassword})",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TacticalAlertRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    if (embedState.isDeniableMode && embedState.decoyPassword.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(TacticalDecoySurface)
                                .border(1.dp, TacticalDecoyBorder.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .clickable { viewModel.setExtractPasswordInput(embedState.decoyPassword) }
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sandi Umpan (${embedState.decoyPassword})",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = TacticalDecoyBlue,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = { viewModel.executeExtraction() },
                    enabled = !extractState.isProcessing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("extract_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalEmerald,
                        contentColor = TacticalBlack
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (extractState.isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = TacticalBlack,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MENGEKSTRAKSI & MENCOCOKKAN GCM...",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "URAI INSTRUKSI TAKTIS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Section 3: Error Message if any
        if (extractState.error != null && extractState.decryptedResult?.payload == null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TacticalAlertRedDim)
                        .border(1.dp, TacticalAlertRed, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = TacticalAlertRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "EKSTRAKSI GAGAL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TacticalAlertRed,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = extractState.error!!,
                                fontSize = 11.sp,
                                color = TacticalTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Section 4: Revealed Decrypted Result
        val res = extractState.decryptedResult
        val payload = res?.payload
        if (payload != null) {
            item {
                if (!res.isDecoy) {
                    // MASTER (OPERATIONAL TOP SECRET)
                    TacticalCard(
                        title = "INSTRUKSI TERURAI // KARTIKA OPERASIONAL",
                        badgeText = "TERBUKA: MASTER",
                        badgeColor = TacticalAlertRed,
                        borderColor = TacticalAlertRed
                    ) {
                        TacticalClassificationBadge(classification = payload.classification)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = payload.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = TacticalTextPrimary,
                            letterSpacing = 0.5.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "UNIT OTORITAS: ${payload.authorUnit}",
                            fontSize = 10.sp,
                            color = TacticalGold,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Time-Lock Warning if time locked
                        if (res.isTimeLocked && !extractState.activeTimeLockBypass) {
                            val timeLockService = com.example.service.TacticalTimeLockService.instance
                            val remainingFormatted = timeLockService.formatCountdown(res.timeLockRemainingMs)
                            val targetFormatted = timeLockService.formatTimestamp(payload.timeLockEpochMs)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TacticalGold.copy(alpha = 0.15f))
                                    .border(1.dp, TacticalGold, RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = TacticalGold, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "TACTICAL TIME-LOCK AKTIF: $remainingFormatted",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TacticalGold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Waktu Buka Operasi: $targetFormatted",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TacticalTextPrimary,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Doktrin Operasi: Pesan dijadwalkan untuk dibuka tepat saat operasi dimulai. Akses dini harus diawasi komandan lapangan.",
                                        fontSize = 9.5.sp,
                                        color = TacticalTextSecondary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { viewModel.toggleTimeLockBypass() },
                                        shape = RoundedCornerShape(4.dp),
                                        modifier = Modifier.height(32.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TacticalGold)
                                    ) {
                                        Text("Buka Paksa (Otoritas Perwira)", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Message Plaintext Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TacticalDarkSurface)
                                .border(1.dp, TacticalCardBorder, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = payload.message,
                                fontSize = 13.sp,
                                color = TacticalTextPrimary,
                                lineHeight = 19.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Coordinates if present
                        if (!payload.coordinates.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TacticalDarkSurface)
                                    .border(1.dp, TacticalEmerald.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = TacticalEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = payload.coordinates,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TacticalEmerald,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(payload.coordinates))
                                        Toast.makeText(context, "Koordinat disalin ke clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Coordinates",
                                        tint = TacticalTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Stego Mode & Reed-Solomon FEC Recovery Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(TacticalDarkSurface)
                                .border(1.dp, TacticalGold.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "MODE DETEKSI: ${res.detectedMode.title}",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TacticalGold,
                                    fontFamily = FontFamily.Monospace
                                )
                                if (res.detectedMode == StegoTransmissionMode.TACTICAL_ROBUST_DCT) {
                                    Text(
                                        text = "FEC REED-SOLOMON GF(2^8): Parity ${res.fecParityPercent}% • Dipulihkan: ${res.fecCorrectedBytes} Byte Rusak",
                                        fontSize = 9.sp,
                                        color = TacticalEmerald,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Telemetry Footer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "WAKTU DEKRIPSI: ${res.executionDurationMs} ms (OFFLINE)",
                                fontSize = 9.sp,
                                color = TacticalTextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "GCM INTEGRITAS: TERVERIFIKASI",
                                fontSize = 9.sp,
                                color = TacticalEmerald,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    // DECOY / PLAUSIBLE DENIABILITY RESULT
                    // Look completely legitimate and routine!
                    TacticalCard(
                        title = "DOKUMEN LOGISTIK TERURAI",
                        badgeText = "STATUS: TERBUKA",
                        badgeColor = TacticalDecoyBlue,
                        borderColor = TacticalDecoyBorder
                    ) {
                        TacticalClassificationBadge(classification = payload.classification)

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = payload.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TacticalTextPrimary,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Message Plaintext Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(TacticalDarkSurface)
                                .border(1.dp, TacticalDecoyBorder.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = payload.message,
                                fontSize = 12.5.sp,
                                color = TacticalTextPrimary,
                                lineHeight = 18.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Covert Deniability Confirmation Badge
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(TacticalDecoySurface)
                                .border(1.dp, TacticalDecoyBorder, RoundedCornerShape(6.dp))
                                .padding(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = TacticalDecoyBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "SANDI KAMUFLASE BERFUNGSI (${res.detectedMode.shortBadge} • FEC Pulih: ${res.fecCorrectedBytes}B): Media berhasil dibuka dengan Sandi Umpan. Tidak ada indikasi adanya pesan kedua yang dapat dideteksi pihak lawan.",
                                    fontSize = 9.sp,
                                    color = TacticalDecoyBlue,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for Entering Officer Override PIN
    if (extractState.showOfficerPinDialog) {
        var pinInput by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { viewModel.setOfficerDialogVisible(false) },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = TacticalGold,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "OTORITAS DARURAT PERWIRA",
                        color = TacticalGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Instruksi ini terikat oleh Tactical Time-Lock. Masukkan PIN Otoritas Komandan/Perwira Pussiberad untuk membuka pesan sebelum jadwal operasi:",
                        fontSize = 11.5.sp,
                        color = TacticalTextPrimary,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TacticalTextField(
                        value = pinInput,
                        onValueChange = { pinInput = it },
                        label = "PIN Otoritas Perwira",
                        placeholder = "Masukkan PIN perwira...",
                        isPassword = true,
                        isMonospace = true,
                        testTag = "input_officer_pin"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset chip for rapid test/operational convenience
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(TacticalGold.copy(alpha = 0.15f))
                            .border(1.dp, TacticalGold.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .clickable { pinInput = com.example.service.TacticalTimeLockService.FIELD_OFFICER_OVERRIDE_PIN }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PIN Pengujian: ${com.example.service.TacticalTimeLockService.FIELD_OFFICER_OVERRIDE_PIN}",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TacticalGold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.applyOfficerOverride(pinInput) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TacticalGold,
                        contentColor = TacticalBlack
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("submit_officer_override_pin")
                ) {
                    Text(
                        text = "BUKA PAKSA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.setOfficerDialogVisible(false) },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("BATAL", fontSize = 11.sp, color = TacticalTextSecondary)
                }
            },
            containerColor = TacticalDarkSurface,
            shape = RoundedCornerShape(12.dp)
        )
    }
}
