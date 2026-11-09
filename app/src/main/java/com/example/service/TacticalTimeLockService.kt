package com.example.service

import android.os.SystemClock
import com.example.data.model.DecryptedResult
import com.example.data.model.ExtractionStatus
import com.example.data.model.SecretPayload
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit

/**
 * Service managing the tactical time-lock mechanism.
 *
 * Prevents the decryption and disclosure of sensitive operational payloads
 * until a verified future timestamp (H-Hour) is reached. Includes anti-tamper
 * clock verification and emergency commanding officer override capabilities.
 */
class TacticalTimeLockService private constructor() {

    // Monotonic anchor paired with wall clock on initialization to detect reverse time tampering
    private val initWallClockMs: Long = System.currentTimeMillis()
    private val initElapsedRealtimeMs: Long = SystemClock.elapsedRealtime()

    companion object {
        val instance: TacticalTimeLockService by lazy { TacticalTimeLockService() }

        // Standard tactical override authorization PIN for emergency field command bypass
        const val FIELD_OFFICER_OVERRIDE_PIN = "PUSSIBERAD-991"
        const val EMERGENCY_COMMAND_TOKEN = "OVERRIDE-H-HOUR"
    }

    enum class TimeLockStatus {
        UNLOCKED_NO_LOCK,          // Payload has no time-lock configured
        ACTIVE_LOCKED,             // Still locked, current time < target unlock time
        MATURED_UNLOCKED,          // Target timestamp reached or passed, decryption permitted
        OFFICER_OVERRIDDEN,        // Emergency bypass authenticated via officer token
        CLOCK_TAMPER_DETECTED      // Clock alteration detected
    }

    data class TimeLockEvaluation(
        val status: TimeLockStatus,
        val targetEpochMs: Long,
        val currentEpochMs: Long,
        val remainingMs: Long,
        val formattedCountdown: String,
        val formattedTargetDate: String,
        val canDecryptNow: Boolean,
        val statusMessage: String
    )

    /**
     * Calculates the estimated current time using monotonic elapsed realtime
     * to audit against manual system clock changes.
     */
    fun getMonotonicAuditedCurrentTimeMs(): Long {
        val elapsedDelta = SystemClock.elapsedRealtime() - initElapsedRealtimeMs
        val calculatedCurrentTime = initWallClockMs + elapsedDelta
        val systemTime = System.currentTimeMillis()

        // If system time was manually pushed backward, return the monotonic calculated time
        return if (systemTime < calculatedCurrentTime) {
            calculatedCurrentTime
        } else {
            systemTime
        }
    }

    /**
     * Checks whether the system wall clock has been tampered with (e.g. rolled backwards).
     */
    fun isClockTampered(): Boolean {
        val elapsedDelta = SystemClock.elapsedRealtime() - initElapsedRealtimeMs
        val expectedTime = initWallClockMs + elapsedDelta
        val systemTime = System.currentTimeMillis()
        // Allow up to 5 seconds jitter, but if system time is significantly behind expected, flag tampering
        return (expectedTime - systemTime) > 5000L
    }

    /**
     * Evaluates whether a payload can be decrypted based on its target time-lock epoch.
     *
     * @param targetEpochMs The timestamp before which access is prohibited (0 if no lock)
     * @param officerOverridePin Optional commander authorization PIN for emergency early unlock
     */
    fun evaluateTimeLock(
        targetEpochMs: Long,
        officerOverridePin: String? = null
    ): TimeLockEvaluation {
        if (targetEpochMs <= 0L) {
            return TimeLockEvaluation(
                status = TimeLockStatus.UNLOCKED_NO_LOCK,
                targetEpochMs = 0L,
                currentEpochMs = System.currentTimeMillis(),
                remainingMs = 0L,
                formattedCountdown = "00:00:00",
                formattedTargetDate = "BEBAS KUNCI WAKTU",
                canDecryptNow = true,
                statusMessage = "Tidak ada pembatasan waktu operasional."
            )
        }

        // Check for commander emergency override
        val isOverrideValid = !officerOverridePin.isNullOrBlank() &&
                (officerOverridePin.trim() == FIELD_OFFICER_OVERRIDE_PIN ||
                        officerOverridePin.trim() == EMERGENCY_COMMAND_TOKEN)

        if (isOverrideValid) {
            return TimeLockEvaluation(
                status = TimeLockStatus.OFFICER_OVERRIDDEN,
                targetEpochMs = targetEpochMs,
                currentEpochMs = System.currentTimeMillis(),
                remainingMs = 0L,
                formattedCountdown = "00:00:00",
                formattedTargetDate = formatTimestamp(targetEpochMs),
                canDecryptNow = true,
                statusMessage = "DIBUKA MELALUI OTORITAS DARURAT PERWIRA OPERASI (BYPASS BERHASIL)."
            )
        }

        val currentTimeMs = getMonotonicAuditedCurrentTimeMs()
        val remainingMs = targetEpochMs - currentTimeMs

        return if (remainingMs <= 0L) {
            TimeLockEvaluation(
                status = TimeLockStatus.MATURED_UNLOCKED,
                targetEpochMs = targetEpochMs,
                currentEpochMs = currentTimeMs,
                remainingMs = 0L,
                formattedCountdown = "00:00:00",
                formattedTargetDate = formatTimestamp(targetEpochMs),
                canDecryptNow = true,
                statusMessage = "Waktu jadwal operasi (H-Hour) telah tiba. Instruksi dapat diakses penuh."
            )
        } else {
            val tampered = isClockTampered()
            TimeLockEvaluation(
                status = if (tampered) TimeLockStatus.CLOCK_TAMPER_DETECTED else TimeLockStatus.ACTIVE_LOCKED,
                targetEpochMs = targetEpochMs,
                currentEpochMs = currentTimeMs,
                remainingMs = remainingMs,
                formattedCountdown = formatCountdown(remainingMs),
                formattedTargetDate = formatTimestamp(targetEpochMs),
                canDecryptNow = false,
                statusMessage = if (tampered) {
                    "PERINGATAN DOKTRIN: Terdeteksi manipulasi jam perangkat. Kunci taktis tetap ditegakkan secara independen."
                } else {
                    "INSTRUKSI TERKUNCI HINGGA JADWAL OPERASI. Akses dini dilarang oleh Doktrin Operasi."
                }
            )
        }
    }

    /**
     * Filters decrypted result, ensuring that if time-locked and not authorized,
     * the secret payload plaintext is masked and protected from unauthorized preview.
     */
    fun enforceTimeLockOnResult(
        result: DecryptedResult,
        officerOverridePin: String? = null
    ): DecryptedResult {
        val payload = result.payload ?: return result
        if (payload.timeLockEpochMs <= 0L) return result

        val evaluation = evaluateTimeLock(payload.timeLockEpochMs, officerOverridePin)

        return if (evaluation.canDecryptNow) {
            result.copy(
                isTimeLocked = false,
                timeLockRemainingMs = 0L
            )
        } else {
            // Mask the secret instructions until time has arrived
            val maskedPayload = payload.copy(
                message = "[TERKUNCI SECARA KRIPTOGRAFIS HINGGA ${evaluation.formattedTargetDate} (SISA: ${evaluation.formattedCountdown})]\n\n" +
                        "Doktrin Siber Pussiberad: Perintah operasi hanya boleh didekripsi saat jam-H tiba guna menghindari kompromi intelijen dini.",
                coordinates = "[KOORDINAT TERKUNCI HINGGA JADWAL OPERASI]"
            )
            result.copy(
                payload = maskedPayload,
                isTimeLocked = true,
                timeLockRemainingMs = evaluation.remainingMs,
                errorMessage = evaluation.statusMessage
            )
        }
    }

    /**
     * Emits a real-time ticking Flow of [TimeLockEvaluation] once per second
     * until the target unlock time is reached.
     */
    fun observeTimeLockCountdown(
        targetEpochMs: Long,
        officerPin: String? = null
    ): Flow<TimeLockEvaluation> = flow {
        while (true) {
            val eval = evaluateTimeLock(targetEpochMs, officerPin)
            emit(eval)
            if (eval.canDecryptNow) break
            delay(1000L)
        }
    }

    /**
     * Calculates future timestamp from offset hours and minutes.
     */
    fun createTargetTimestamp(hoursAhead: Int, minutesAhead: Int = 0): Long {
        val totalMs = (hoursAhead * 3600_000L) + (minutesAhead * 60_000L)
        return System.currentTimeMillis() + totalMs
    }

    /**
     * Formats remaining milliseconds into HH:mm:ss.
     */
    fun formatCountdown(remainingMs: Long): String {
        if (remainingMs <= 0) return "00:00:00"
        val hours = TimeUnit.MILLISECONDS.toHours(remainingMs)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(remainingMs) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(remainingMs) % 60
        return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    }

    /**
     * Formats epoch timestamp into military operational date string.
     */
    fun formatTimestamp(epochMs: Long): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm 'WIB'", Locale("id", "ID"))
        sdf.timeZone = TimeZone.getTimeZone("GMT+7")
        return sdf.format(Date(epochMs))
    }
}
