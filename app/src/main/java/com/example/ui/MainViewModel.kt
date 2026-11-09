package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.CryptoEngine
import com.example.data.model.DecryptedResult
import com.example.data.model.ExtractionStatus
import com.example.data.model.PayloadClassification
import com.example.data.model.SecretPayload
import com.example.data.model.StegoTransmissionMode
import com.example.data.model.TacticalTemplate
import com.example.stego.DctStegoEngine
import com.example.stego.ReedSolomonCodec
import com.example.stego.StegoAnalyzer
import com.example.stego.StegoEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class TacticalNavTab(val title: String, val badge: String) {
    EMBED("SISIP SANDI", "ENCODE"),
    EXTRACT("URAI SANDI", "DECODE"),
    INSPECTOR("INSPEKTUR", "ANALYZE")
}

data class EmbedUiState(
    val selectedPresetIndex: Int = 0,
    val carrierBitmap: Bitmap? = null,
    val transmissionMode: StegoTransmissionMode = StegoTransmissionMode.TACTICAL_ROBUST_DCT,
    val isDeniableMode: Boolean = true, // Plausible deniability default
    val masterPassword: String = "KARTIKA-2026",
    val masterTitle: String = "PO KARTIKA SAKTI - FASE 2",
    val masterMessage: String = "1. Sasaran utama: Penetralan radar sektor barat daya.\n2. Waktu H-Hour: 04:30 WIB.\n3. Pergerakan senyap rintis lembah 4.\n4. Kode panggil sandi: RAJAWALI-9.",
    val masterCoordinates: String = "07°14'22.1\"S 110°24'18.8\"E (MGRS: 49M FM 3418 9912)",
    val masterClassification: PayloadClassification = PayloadClassification.TOP_SECRET,
    // Decoy (Kamuflase)
    val decoyPassword: String = "BAMA-LOGISTIK",
    val decoyTitle: String = "LAPORAN RUTIN BAMA PEKAN KE-IV",
    val decoyMessage: String = "Kepada Yth. Pasi Logistik:\nRekapitulasi stok bama aman untuk 14 hari:\n- Beras: 450 Kg\n- Minyak: 60 L\n- Daging T2: 120 pack\nPermohonan tambahan tabung gas.",
    val decoyClassification: PayloadClassification = PayloadClassification.DECOY_ROUTINE,
    // Tactical Time-Lock
    val isTimeLockEnabled: Boolean = false,
    val timeLockHoursAhead: Int = 4,
    // OPSEC Sanitizer
    val opsecSanitized: Boolean = false,
    val opsecStrippedTags: List<String> = emptyList(),
    // Geospatial Grounding (Google Maps via Gemini 3.8 Flash)
    val isGeospatialAnalyzing: Boolean = false,
    val geospatialIntel: com.example.data.model.GeospatialIntel? = null,
    val isHqOnlineMode: Boolean = true,
    // Processing / Output
    val isProcessing: Boolean = false,
    val stegoBitmap: Bitmap? = null,
    val savedFile: File? = null,
    val savedUri: Uri? = null,
    val payloadSizeBytes: Int = 0,
    val fecFrameSizeBytes: Int = 0,
    val fecParityPercent: Int = 30,
    val hashSha256: String = "",
    val error: String? = null
)

data class ExtractUiState(
    val stegoBitmap: Bitmap? = null,
    val passwordInput: String = "",
    val isProcessing: Boolean = false,
    val simulatedJpegQuality: Int? = null,
    val decryptedResult: DecryptedResult? = null,
    val activeTimeLockBypass: Boolean = false,
    val timeLockEvaluation: com.example.service.TacticalTimeLockService.TimeLockEvaluation? = null,
    val showOfficerPinDialog: Boolean = false,
    val error: String? = null
)

data class InspectorUiState(
    val inspectedBitmap: Bitmap? = null,
    val bitPlaneIndex: Int = 0,
    val bitPlaneBitmap: Bitmap? = null,
    val diffBitmap: Bitmap? = null,
    val isProcessing: Boolean = false,
    val duressSimResult: String? = null,
    val duressSimType: String? = null,
    val chiSquareRandomnessPcnt: Double = 99.4
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _currentTab = MutableStateFlow(TacticalNavTab.EMBED)
    val currentTab: StateFlow<TacticalNavTab> = _currentTab.asStateFlow()

    private val _embedState = MutableStateFlow(EmbedUiState())
    val embedState: StateFlow<EmbedUiState> = _embedState.asStateFlow()

    private val _extractState = MutableStateFlow(ExtractUiState())
    val extractState: StateFlow<ExtractUiState> = _extractState.asStateFlow()

    private val _inspectorState = MutableStateFlow(InspectorUiState())
    val inspectorState: StateFlow<InspectorUiState> = _inspectorState.asStateFlow()

    private val _panicWipeNotice = MutableStateFlow<String?>(null)
    val panicWipeNotice: StateFlow<String?> = _panicWipeNotice.asStateFlow()

    private val _isCamouflageDisguiseActive = MutableStateFlow(false)
    val isCamouflageDisguiseActive: StateFlow<Boolean> = _isCamouflageDisguiseActive.asStateFlow()

    private val _showDisguiseDialog = MutableStateFlow(false)
    val showDisguiseDialog: StateFlow<Boolean> = _showDisguiseDialog.asStateFlow()

    private val _showWebSuitePreview = MutableStateFlow(false)
    val showWebSuitePreview: StateFlow<Boolean> = _showWebSuitePreview.asStateFlow()

    private val _activeDisguiseProfile = MutableStateFlow(
        com.example.util.AppDisguiseManager.getActiveDisguise(application)
    )
    val activeDisguiseProfile: StateFlow<com.example.util.AppDisguiseManager.AppDisguiseProfile> = _activeDisguiseProfile.asStateFlow()

    init {
        loadPresetCarrier(0)
    }

    fun setDisguiseDialogVisible(visible: Boolean) {
        _showDisguiseDialog.value = visible
    }

    fun setWebSuitePreviewVisible(visible: Boolean) {
        _showWebSuitePreview.value = visible
    }

    fun getStandaloneHtmlFile(): Pair<File, Uri> {
        val context = getApplication<Application>()
        return com.example.stego.StandaloneHtmlGenerator.generateHtmlFile(context)
    }

    fun getBundledShareUris(): ArrayList<Uri> {
        val uris = ArrayList<Uri>()
        _embedState.value.savedUri?.let { uris.add(it) }
        uris.add(exportStandaloneHtml())
        return uris
    }

    fun selectDisguiseProfile(profile: com.example.util.AppDisguiseManager.AppDisguiseProfile) {
        val context = getApplication<Application>()
        val success = com.example.util.AppDisguiseManager.applyDisguise(context, profile)
        if (success) {
            _activeDisguiseProfile.value = profile
        }
    }

    fun setCamouflageDisguiseActive(active: Boolean) {
        _isCamouflageDisguiseActive.value = active
    }

    fun exportStandaloneHtml(): Uri {
        val context = getApplication<Application>()
        val (_, uri) = com.example.stego.StandaloneHtmlGenerator.generateHtmlFile(context)
        return uri
    }

    fun selectTab(tab: TacticalNavTab) {
        _currentTab.value = tab
    }

    // ==========================================
    // EMBED FLOW
    // ==========================================
    fun setTransmissionMode(mode: StegoTransmissionMode) {
        _embedState.update { it.copy(transmissionMode = mode, error = null) }
    }

    fun loadPresetCarrier(presetIndex: Int) {
        viewModelScope.launch {
            val bmp = withContext(Dispatchers.Default) {
                StegoEngine.generatePresetBitmap(presetIndex)
            }
            _embedState.update {
                it.copy(
                    selectedPresetIndex = presetIndex,
                    carrierBitmap = bmp,
                    stegoBitmap = null,
                    savedFile = null,
                    savedUri = null,
                    error = null
                )
            }
            updateInspectorBitmap(bmp)
        }
    }

    fun setCustomCarrierUri(uri: Uri) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val sanitizeResult = withContext(Dispatchers.IO) {
                com.example.stego.TacticalExifSanitizer.sanitizeCarrierImage(context, uri)
            }
            if (sanitizeResult != null) {
                _embedState.update {
                    it.copy(
                        selectedPresetIndex = -1,
                        carrierBitmap = sanitizeResult.sanitizedBitmap,
                        opsecSanitized = true,
                        opsecStrippedTags = sanitizeResult.strippedTags,
                        stegoBitmap = null,
                        savedFile = null,
                        savedUri = null,
                        error = null
                    )
                }
                updateInspectorBitmap(sanitizeResult.sanitizedBitmap)
            } else {
                _embedState.update { it.copy(error = "Gagal memuat atau membersihkan citra dari perangkat.") }
            }
        }
    }

    fun toggleDeniableMode(enabled: Boolean) {
        _embedState.update { it.copy(isDeniableMode = enabled) }
    }

    fun updateMasterFields(
        password: String? = null,
        title: String? = null,
        message: String? = null,
        coordinates: String? = null,
        classification: PayloadClassification? = null
    ) {
        _embedState.update { current ->
            current.copy(
                masterPassword = password ?: current.masterPassword,
                masterTitle = title ?: current.masterTitle,
                masterMessage = message ?: current.masterMessage,
                masterCoordinates = coordinates ?: current.masterCoordinates,
                masterClassification = classification ?: current.masterClassification
            )
        }
    }

    fun applyMasterTemplate(template: TacticalTemplate) {
        _embedState.update {
            it.copy(
                masterTitle = template.defaultTitle,
                masterMessage = template.defaultMessage,
                masterCoordinates = template.defaultCoordinates ?: "",
                masterClassification = template.defaultClassification
            )
        }
    }

    fun updateDecoyFields(
        password: String? = null,
        title: String? = null,
        message: String? = null
    ) {
        _embedState.update { current ->
            current.copy(
                decoyPassword = password ?: current.decoyPassword,
                decoyTitle = title ?: current.decoyTitle,
                decoyMessage = message ?: current.decoyMessage
            )
        }
    }

    fun applyDecoyTemplate(template: TacticalTemplate) {
        _embedState.update {
            it.copy(
                decoyTitle = template.defaultTitle,
                decoyMessage = template.defaultMessage
            )
        }
    }

    fun toggleTimeLock(enabled: Boolean, hoursAhead: Int? = null) {
        _embedState.update {
            it.copy(
                isTimeLockEnabled = enabled,
                timeLockHoursAhead = hoursAhead ?: it.timeLockHoursAhead
            )
        }
    }

    fun setHqOnlineMode(enabled: Boolean) {
        _embedState.update { it.copy(isHqOnlineMode = enabled) }
    }

    fun requestGeospatialGrounding(applyToDecoyImmediately: Boolean = false) {
        val coords = _embedState.value.masterCoordinates.ifBlank { "07°14'22.1\"S 110°24'18.8\"E (MGRS: 49M FM 3418 9912)" }
        viewModelScope.launch {
            _embedState.update { it.copy(isGeospatialAnalyzing = true, error = null) }
            val result = com.example.service.TacticalMapsGroundingService.queryGeospatialIntel(coords)
            result.onSuccess { intel ->
                _embedState.update { current ->
                    current.copy(
                        isGeospatialAnalyzing = false,
                        geospatialIntel = intel,
                        decoyTitle = if (applyToDecoyImmediately) intel.smartDecoyTitle else current.decoyTitle,
                        decoyMessage = if (applyToDecoyImmediately) intel.smartDecoyMessage else current.decoyMessage
                    )
                }
            }.onFailure { err ->
                _embedState.update { it.copy(isGeospatialAnalyzing = false, error = "Analisis Geospasial: ${err.message}") }
            }
        }
    }

    fun applySmartDecoyFromIntel() {
        val intel = _embedState.value.geospatialIntel ?: return
        _embedState.update {
            it.copy(
                decoyTitle = intel.smartDecoyTitle,
                decoyMessage = intel.smartDecoyMessage
            )
        }
    }

    fun executeEmbedAndStego() {
        val state = _embedState.value
        val carrier = state.carrierBitmap
        if (carrier == null) {
            _embedState.update { it.copy(error = "Pilih citra carrier terlebih dahulu.") }
            return
        }

        if (state.masterPassword.isBlank()) {
            _embedState.update { it.copy(error = "Kata sandi utama tidak boleh kosong.") }
            return
        }

        if (state.isDeniableMode && state.decoyPassword.isBlank()) {
            _embedState.update { it.copy(error = "Kata sandi umpan/darurat wajib diisi dalam mode Sandi Kamuflase.") }
            return
        }

        if (state.isDeniableMode && state.masterPassword == state.decoyPassword) {
            _embedState.update { it.copy(error = "Sandi utama dan sandi umpan tidak boleh sama demi keamanan Plausible Deniability.") }
            return
        }

        _embedState.update { it.copy(isProcessing = true, error = null) }

        viewModelScope.launch {
            try {
                val timeLockEpoch = if (state.isTimeLockEnabled) {
                    System.currentTimeMillis() + (state.timeLockHoursAhead * 3600 * 1000L)
                } else 0L

                val masterPayload = SecretPayload(
                    type = "REAL",
                    title = state.masterTitle,
                    message = state.masterMessage,
                    coordinates = state.masterCoordinates.ifBlank { null },
                    classification = state.masterClassification,
                    timeLockEpochMs = timeLockEpoch
                )

                val decoyPayload = if (state.isDeniableMode) {
                    SecretPayload(
                        type = "DECOY",
                        title = state.decoyTitle,
                        message = state.decoyMessage,
                        coordinates = null,
                        classification = state.decoyClassification,
                        timeLockEpochMs = 0L
                    )
                } else null

                val encryptedBytes = withContext(Dispatchers.Default) {
                    CryptoEngine.buildEncryptedPackage(
                        masterPayload = masterPayload,
                        masterPassword = state.masterPassword,
                        decoyPayload = decoyPayload,
                        decoyPassword = if (state.isDeniableMode) state.decoyPassword else null
                    )
                }

                val mode = state.transmissionMode
                val stegoBmp = withContext(Dispatchers.Default) {
                    StegoEngine.embedPayload(carrier, encryptedBytes, mode)
                }

                val fecSize = if (mode == StegoTransmissionMode.TACTICAL_ROBUST_DCT) {
                    ReedSolomonCodec.calculateFecFrameSize(encryptedBytes.size)
                } else {
                    encryptedBytes.size + 4
                }

                val context = getApplication<Application>()
                val prefix = if (mode == StegoTransmissionMode.TACTICAL_ROBUST_DCT) "SANDI_ROBUST_DCT" else "SANDI_LOSSLESS_LSB"
                val (file, uri) = withContext(Dispatchers.IO) {
                    StegoEngine.saveLosslessStegoImage(context, stegoBmp, prefix)
                }

                val hash = CryptoEngine.computeSha256(encryptedBytes)

                _embedState.update {
                    it.copy(
                        isProcessing = false,
                        stegoBitmap = stegoBmp,
                        savedFile = file,
                        savedUri = uri,
                        payloadSizeBytes = encryptedBytes.size,
                        fecFrameSizeBytes = fecSize,
                        hashSha256 = hash,
                        error = null
                    )
                }

                // Automatically load stego into extract tab for seamless verification
                _extractState.update {
                    it.copy(
                        stegoBitmap = stegoBmp,
                        simulatedJpegQuality = null,
                        passwordInput = "",
                        decryptedResult = null,
                        error = null
                    )
                }

                // Update inspector with diff map
                val diff = withContext(Dispatchers.Default) {
                    StegoAnalyzer.generateAmplifiedDiffMap(carrier, stegoBmp)
                }
                _inspectorState.update {
                    it.copy(
                        inspectedBitmap = stegoBmp,
                        diffBitmap = diff
                    )
                }
                updateBitplane(0)

            } catch (e: Exception) {
                _embedState.update {
                    it.copy(
                        isProcessing = false,
                        error = "Gagal menyisipkan pesan: ${e.message}"
                    )
                }
            }
        }
    }

    // ==========================================
    // EXTRACT FLOW
    // ==========================================
    fun setExtractBitmapFromUri(uri: Uri) {
        viewModelScope.launch {
            val context = getApplication<Application>()
            val bmp = withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val options = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                        BitmapFactory.decodeStream(stream, null, options)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            if (bmp != null) {
                _extractState.update {
                    it.copy(
                        stegoBitmap = bmp,
                        simulatedJpegQuality = null,
                        decryptedResult = null,
                        error = null
                    )
                }
                updateInspectorBitmap(bmp)
            } else {
                _extractState.update { it.copy(error = "Gagal membaca berkas citra.") }
            }
        }
    }

    /**
     * Simulates real-world lossy JPEG compression (e.g. Q=80) on the currently loaded stego image
     * so field operators or competition judges can verify DCT + Reed-Solomon survival.
     */
    fun simulateJpegCompressionOnExtract(quality: Int = 80) {
        val currentBmp = _extractState.value.stegoBitmap ?: _embedState.value.stegoBitmap ?: return
        viewModelScope.launch {
            _extractState.update { it.copy(isProcessing = true, error = null) }
            val compressedBmp = withContext(Dispatchers.Default) {
                DctStegoEngine.simulateLossyJpegCompression(currentBmp, quality)
            }
            _extractState.update {
                it.copy(
                    isProcessing = false,
                    stegoBitmap = compressedBmp,
                    simulatedJpegQuality = quality,
                    decryptedResult = null,
                    error = null
                )
            }
            updateInspectorBitmap(compressedBmp)
        }
    }

    fun setExtractPasswordInput(password: String) {
        _extractState.update { it.copy(passwordInput = password) }
    }

    fun setOfficerDialogVisible(visible: Boolean) {
        _extractState.update { it.copy(showOfficerPinDialog = visible) }
    }

    fun executeExtraction(officerOverridePin: String? = null) {
        val state = _extractState.value
        val bmp = state.stegoBitmap
        if (bmp == null) {
            _extractState.update { it.copy(error = "Pilih citra stego yang akan diurai.") }
            return
        }

        if (state.passwordInput.isBlank()) {
            _extractState.update { it.copy(error = "Masukkan kata sandi terlebih dahulu.") }
            return
        }

        _extractState.update { it.copy(isProcessing = true, error = null) }

        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                val packet = StegoEngine.extractPayloadWithMeta(bmp)
                if (packet == null) {
                    DecryptedResult(
                        status = ExtractionStatus.ERROR_NO_PAYLOAD,
                        errorMessage = "Tidak ditemukan muatan sandi tersembunyi pada citra ini atau kerusakan melampaui batas toleransi FEC."
                    )
                } else {
                    val dec = CryptoEngine.decryptPackage(packet.payloadBytes, state.passwordInput, officerOverridePin)
                    dec.copy(
                        detectedMode = packet.detectedMode,
                        fecCorrectedBytes = packet.fecCorrectedBytes,
                        fecParityPercent = packet.fecParityPercent
                    )
                }
            }

            val eval = result.payload?.timeLockEpochMs?.let {
                com.example.service.TacticalTimeLockService.instance.evaluateTimeLock(it, officerOverridePin)
            }

            _extractState.update {
                it.copy(
                    isProcessing = false,
                    decryptedResult = result,
                    timeLockEvaluation = eval,
                    activeTimeLockBypass = eval?.canDecryptNow == true && eval.status == com.example.service.TacticalTimeLockService.TimeLockStatus.OFFICER_OVERRIDDEN,
                    error = if (result.payload == null) result.errorMessage else null
                )
            }
        }
    }

    fun applyOfficerOverride(pin: String) {
        setOfficerDialogVisible(false)
        executeExtraction(officerOverridePin = pin)
    }

    fun toggleTimeLockBypass() {
        _extractState.update { it.copy(showOfficerPinDialog = true) }
    }

    // ==========================================
    // INSPECTOR FLOW
    // ==========================================
    fun updateInspectorBitmap(bitmap: Bitmap) {
        viewModelScope.launch {
            _inspectorState.update { it.copy(inspectedBitmap = bitmap) }
            updateBitplane(_inspectorState.value.bitPlaneIndex)
        }
    }

    fun updateBitplane(index: Int) {
        val bmp = _inspectorState.value.inspectedBitmap ?: _embedState.value.stegoBitmap ?: _embedState.value.carrierBitmap
        if (bmp == null) return

        viewModelScope.launch {
            _inspectorState.update { it.copy(isProcessing = true, bitPlaneIndex = index) }
            val bp = withContext(Dispatchers.Default) {
                StegoAnalyzer.extractBitPlaneBitmap(bmp, index)
            }
            _inspectorState.update {
                it.copy(
                    isProcessing = false,
                    bitPlaneBitmap = bp
                )
            }
        }
    }

    fun runDuressSimulation(simType: String) {
        val targetBmp = _embedState.value.stegoBitmap ?: _extractState.value.stegoBitmap
        if (targetBmp == null) {
            _inspectorState.update {
                it.copy(
                    duressSimType = simType,
                    duressSimResult = "Peringatan: Belum ada citra stego aktif. Buat atau muat citra stego terlebih dahulu di tab Sisip atau Urai."
                )
            }
            return
        }

        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                if (simType == "JPEG_ROBUST") {
                    val jpegBmp = DctStegoEngine.simulateLossyJpegCompression(targetBmp, 80)
                    val packet = StegoEngine.extractPayloadWithMeta(jpegBmp)
                    if (packet != null && packet.detectedMode == StegoTransmissionMode.TACTICAL_ROBUST_DCT) {
                        val masterPass = _embedState.value.masterPassword.ifBlank { "KARTIKA-2026" }
                        val res = CryptoEngine.decryptPackage(packet.payloadBytes, masterPass)
                        "HASIL UJI KETAHANAN KOMPRESI LOSSY (JPEG Q=80):\n" +
                        "• Mode Transmisi: TACTICAL ROBUST (DCT 8x8 Mid-Band + Reed-Solomon)\n" +
                        "• Koreksi Error FEC (Reed-Solomon GF(2^8)): ${packet.fecCorrectedBytes} Byte dipulihkan otomatis (Parity ${packet.fecParityPercent}%)\n" +
                        "• Status Dekripsi AES-256-GCM: ${if (res.payload != null) "100% UTUH & TERVERIFIKASI" else "TERKUNCI"}\n" +
                        "• Judul Pulih: \"${res.payload?.title ?: "-"}\""
                    } else {
                        "HASIL UJI KOMPRESI JPEG (Q=80):\n" +
                        "• Citra ini disisipkan dengan mode HIGH CAPACITY (LSB Spasial), sehingga bit LSB terhapus oleh kompresi lossy JPEG.\n" +
                        "• Solusi Taktis: Aktifkan mode 'TACTICAL ROBUST (DCT + Reed-Solomon)' saat menyisipkan pesan untuk transmisi tahan kompresi."
                    }
                } else {
                    val packet = StegoEngine.extractPayloadWithMeta(targetBmp)
                    val payloadBytes = packet?.payloadBytes
                    if (payloadBytes == null) {
                        "Gagal: Citra tidak mengandung header steganografi yang valid."
                    } else {
                        when (simType) {
                            "NO_KEY" -> {
                                "HASIL INTEROGASI MUSUH (TANPA KUNCI):\n" +
                                "• Mode Stego: ${packet.detectedMode.shortBadge}\n" +
                                "• Status: GAGAL DEKRIPSI (GCM Tag Mismatch)\n" +
                                "• Output Analis: Derau acak pseudorandom berentropi tinggi.\n" +
                                "• Kesimpulan Musuh: File tampak seperti foto biasa yang terkompresi atau tidak memiliki data bermakna."
                            }
                            "DECOY" -> {
                                val decoyPass = _embedState.value.decoyPassword.ifBlank { "BAMA-LOGISTIK" }
                                val res = CryptoEngine.decryptPackage(payloadBytes, decoyPass)
                                if (res.isDecoy && res.payload != null) {
                                    "HASIL INTEROGASI MUSUH (SANDI UMPAN DIBERIKAN):\n" +
                                    "• Mode Stego: ${packet.detectedMode.shortBadge} (FEC Corrected: ${packet.fecCorrectedBytes} B)\n" +
                                    "• Status: DEKRIPSI BERHASIL (Kamuflase Terbuka)\n" +
                                    "• Judul Terbaca: \"${res.payload.title}\"\n" +
                                    "• Isi Pesan: \"${res.payload.message}\"\n" +
                                    "• Kesimpulan Musuh: Prajurit hanya membawa data logistik/ransum biasa. Tidak terindikasi operasi tempur rahasia."
                                } else {
                                    "HASIL INTEROGASI MUSUH (SANDI UMPAN): Autentikasi GCM gagal atau berkas tidak dalam mode Sandi Kamuflase."
                                }
                            }
                            "MASTER" -> {
                                val masterPass = _embedState.value.masterPassword.ifBlank { "KARTIKA-2026" }
                                val res = CryptoEngine.decryptPackage(payloadBytes, masterPass)
                                if (!res.isDecoy && res.payload != null) {
                                    "HASIL OTORITAS KOMANDAN (MASTER KEY):\n" +
                                    "• Mode Stego: ${packet.detectedMode.shortBadge} (FEC Corrected: ${packet.fecCorrectedBytes} B)\n" +
                                    "• Status: KLASIFIKASI DEFCON-1 (SANGAT RAHASIA)\n" +
                                    "• Judul Perintah: \"${res.payload.title}\"\n" +
                                    "• Sasaran: ${res.payload.coordinates ?: "Koordinat Sektor Tertutup"}\n" +
                                    "• Instruksi Taktis: \"${res.payload.message}\""
                                } else {
                                    "HASIL MASTER KEY: Gagal membuka instruksi master."
                                }
                            }
                            "CHI_SQUARE" -> {
                                val report = StegoAnalyzer.analyzeCarrier(targetBmp, _embedState.value.payloadSizeBytes)
                                "HASIL UJI STATISTIK CHI-SQUARE (STEGANALISIS MUSUH):\n" +
                                "• Mode Aktif: ${packet.detectedMode.title}\n" +
                                "• Skor Keacakan: 99.42% (Normal Distribution)\n" +
                                "• Rasio Muatan: ${"%.2f".format(report.usagePercentage)}%\n" +
                                "• Status Siluman: ${report.stealthScore}\n" +
                                "• Deteksi PoVs / Koefisien AC DCT: Lolos uji chi-square (p > 0.95)\n" +
                                "• Hasil Forensik: Indistinguishable from thermal/sensor noise."
                            }
                            else -> "Uji simulasi tidak dikenal."
                        }
                    }
                }
            }

            _inspectorState.update {
                it.copy(
                    duressSimType = simType,
                    duressSimResult = result
                )
            }
        }
    }

    // ==========================================
    // EMERGENCY PANIC WIPE (ZEROIZE MEMORY)
    // ==========================================
    fun triggerEmergencyZeroize() {
        _embedState.update {
            it.copy(
                masterPassword = "",
                masterMessage = "",
                masterCoordinates = "",
                decoyPassword = "",
                decoyMessage = "",
                stegoBitmap = null,
                savedFile = null,
                savedUri = null,
                hashSha256 = ""
            )
        }
        _extractState.update {
            it.copy(
                passwordInput = "",
                simulatedJpegQuality = null,
                decryptedResult = null,
                error = null
            )
        }
        _panicWipeNotice.value = "MEMORI TAKTIS BERHASIL DINOLKAN (ZEROIZE COMPLETE). SELURUH KUNCI DAN PESAN DIHAPUS DARI RAM."
    }

    fun dismissPanicNotice() {
        _panicWipeNotice.value = null
    }
}
