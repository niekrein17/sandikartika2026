package com.example

import android.graphics.Bitmap
import com.example.crypto.CryptoEngine
import com.example.data.model.ExtractionStatus
import com.example.data.model.PayloadClassification
import com.example.data.model.SecretPayload
import com.example.stego.StegoEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun testSingleModeEncryptionAndDecryption() {
        val masterPayload = SecretPayload(
            type = "REAL",
            title = "PERINTAH OPERASI",
            message = "Pergerakan ke titik Alpha pukul 04.00",
            coordinates = "07°14'22.1\"S 110°24'18.8\"E",
            classification = PayloadClassification.TOP_SECRET
        )

        val masterPass = "KARTIKA-TOP-SECRET"
        val encryptedBytes = CryptoEngine.buildEncryptedPackage(
            masterPayload = masterPayload,
            masterPassword = masterPass
        )

        assertNotNull(encryptedBytes)
        assertTrue(encryptedBytes.size > 50)

        // Decrypt with correct password
        val result = CryptoEngine.decryptPackage(encryptedBytes, masterPass)
        assertEquals(ExtractionStatus.SUCCESS_MASTER, result.status)
        assertFalse(result.isDecoy)
        assertNotNull(result.payload)
        assertEquals("PERINTAH OPERASI", result.payload?.title)
        assertEquals("Pergerakan ke titik Alpha pukul 04.00", result.payload?.message)

        // Decrypt with wrong password
        val wrongResult = CryptoEngine.decryptPackage(encryptedBytes, "SALAH-SANDI")
        assertEquals(ExtractionStatus.ERROR_WRONG_PASSWORD, wrongResult.status)
    }

    @Test
    fun testPlausibleDeniabilityDualKeyEncryption() {
        val masterPayload = SecretPayload(
            type = "REAL",
            title = "RAHASIA OPERASI KHUSUS",
            message = "Koordinat bunker radar lawan terlampir",
            coordinates = "07°45'30.0\"S 110°22'10.5\"E",
            classification = PayloadClassification.TOP_SECRET
        )

        val decoyPayload = SecretPayload(
            type = "DECOY",
            title = "LAPORAN RUTIN BAMA",
            message = "Persediaan beras 450 Kg dan minyak 60 Liter aman.",
            coordinates = null,
            classification = PayloadClassification.DECOY_ROUTINE
        )

        val masterPass = "OPERASI-RAHASIA-99"
        val decoyPass = "BAMA-LOGISTIK-KOMPI"

        // Build composite deniable package
        val packageBytes = CryptoEngine.buildEncryptedPackage(
            masterPayload = masterPayload,
            masterPassword = masterPass,
            decoyPayload = decoyPayload,
            decoyPassword = decoyPass
        )

        // Probe 1: Legitimate Officer enters Master Key
        val masterResult = CryptoEngine.decryptPackage(packageBytes, masterPass)
        assertEquals(ExtractionStatus.SUCCESS_MASTER, masterResult.status)
        assertFalse(masterResult.isDecoy)
        assertEquals("RAHASIA OPERASI KHUSUS", masterResult.payload?.title)

        // Probe 2: Soldier under Duress enters Decoy Key (Kamuflase)
        val decoyResult = CryptoEngine.decryptPackage(packageBytes, decoyPass)
        assertEquals(ExtractionStatus.SUCCESS_DECOY, decoyResult.status)
        assertTrue(decoyResult.isDecoy)
        assertEquals("LAPORAN RUTIN BAMA", decoyResult.payload?.title)
        assertEquals("Persediaan beras 450 Kg dan minyak 60 Liter aman.", decoyResult.payload?.message)

        // Probe 3: Adversary guessing random key fails
        val failResult = CryptoEngine.decryptPackage(packageBytes, "HACKER-ATTEMPT")
        assertEquals(ExtractionStatus.ERROR_WRONG_PASSWORD, failResult.status)
    }

    @Test
    fun testTacticalTimeLock() {
        val futureEpoch = System.currentTimeMillis() + 3600_000L // 1 hour ahead
        val timelockedPayload = SecretPayload(
            type = "REAL",
            title = "H-HOUR SYNC",
            message = "Serangan serentak dimulai",
            timeLockEpochMs = futureEpoch
        )

        val pass = "H-HOUR-KEY"
        val bytes = CryptoEngine.buildEncryptedPackage(timelockedPayload, pass)
        val result = CryptoEngine.decryptPackage(bytes, pass)

        assertEquals(ExtractionStatus.SUCCESS_MASTER, result.status)
        assertTrue(result.isTimeLocked)
        assertTrue(result.timeLockRemainingMs > 0)
    }

    @Test
    fun testStegoEmbeddingAndExtractionRoundtrip() {
        // Create a 200x200 ARGB test bitmap
        val testBitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val testPayload = "PUSSIBERAD-TACTICAL-PAYLOAD-VERIFIED".toByteArray(Charsets.UTF_8)

        val stegoBmp = StegoEngine.embedPayload(testBitmap, testPayload)
        assertNotNull(stegoBmp)

        val extractedBytes = StegoEngine.extractPayload(stegoBmp)
        assertNotNull(extractedBytes)
        val extractedString = String(extractedBytes!!, Charsets.UTF_8)
        assertEquals("PUSSIBERAD-TACTICAL-PAYLOAD-VERIFIED", extractedString)
    }

    @Test
    fun testLsbTextSteganographyUtilityStringRoundtrip() {
        val bitmap = Bitmap.createBitmap(150, 150, Bitmap.Config.ARGB_8888)
        val encryptedString = "ENC:AES256GCM:aW5zdHJ1a3NpLXJhaGFzaWEtcHVzc2liZXJhZC0yMDI2:tag_verified_991823"

        // Capacity and fit checks
        assertTrue(com.example.stego.LsbTextSteganographyUtil.canFitText(bitmap, encryptedString))
        assertTrue(com.example.stego.LsbTextSteganographyUtil.calculateMaxCapacityBytes(bitmap) > 8000)

        // Embed encrypted text string
        val stegoBitmap = com.example.stego.LsbTextSteganographyUtil.embedEncryptedText(bitmap, encryptedString)
        assertNotNull(stegoBitmap)
        assertEquals(bitmap.width, stegoBitmap.width)
        assertEquals(bitmap.height, stegoBitmap.height)

        // Extract encrypted text string
        val extracted = com.example.stego.LsbTextSteganographyUtil.extractEncryptedText(stegoBitmap)
        assertNotNull(extracted)
        assertEquals(encryptedString, extracted)

        // Safe wrapper test
        val safeResult = com.example.stego.LsbTextSteganographyUtil.extractEncryptedTextSafe(stegoBitmap)
        assertTrue(safeResult is com.example.stego.LsbTextSteganographyUtil.StegoResult.Success)
        assertEquals(encryptedString, (safeResult as com.example.stego.LsbTextSteganographyUtil.StegoResult.Success).data)
    }

    @Test
    fun testLsbTextSteganographyCleanImageReturnsNull() {
        // A clean blank bitmap with all 0s has 0 length header -> extract returns null
        val cleanBitmap = Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888)
        val extracted = com.example.stego.LsbTextSteganographyUtil.extractEncryptedText(cleanBitmap)
        org.junit.Assert.assertNull(extracted)

        val safeResult = com.example.stego.LsbTextSteganographyUtil.extractEncryptedTextSafe(cleanBitmap)
        assertTrue(safeResult is com.example.stego.LsbTextSteganographyUtil.StegoResult.Error)
    }

    @Test
    fun testTacticalTimeLockServiceFutureTimestampLocked() {
        val service = com.example.service.TacticalTimeLockService.instance
        val targetFutureEpoch = service.createTargetTimestamp(hoursAhead = 2)

        val evaluation = service.evaluateTimeLock(targetFutureEpoch)
        assertEquals(com.example.service.TacticalTimeLockService.TimeLockStatus.ACTIVE_LOCKED, evaluation.status)
        assertFalse(evaluation.canDecryptNow)
        assertTrue(evaluation.remainingMs > 0)
        assertTrue(evaluation.formattedCountdown.startsWith("01:") || evaluation.formattedCountdown.startsWith("02:"))
    }

    @Test
    fun testTacticalTimeLockServicePastTimestampUnlocked() {
        val service = com.example.service.TacticalTimeLockService.instance
        val pastEpoch = System.currentTimeMillis() - 10_000L

        val evaluation = service.evaluateTimeLock(pastEpoch)
        assertEquals(com.example.service.TacticalTimeLockService.TimeLockStatus.MATURED_UNLOCKED, evaluation.status)
        assertTrue(evaluation.canDecryptNow)
        assertEquals(0L, evaluation.remainingMs)
    }

    @Test
    fun testTacticalTimeLockServiceOfficerOverride() {
        val service = com.example.service.TacticalTimeLockService.instance
        val futureEpoch = service.createTargetTimestamp(hoursAhead = 5)

        // Valid PIN unlocks early
        val validEval = service.evaluateTimeLock(futureEpoch, com.example.service.TacticalTimeLockService.FIELD_OFFICER_OVERRIDE_PIN)
        assertEquals(com.example.service.TacticalTimeLockService.TimeLockStatus.OFFICER_OVERRIDDEN, validEval.status)
        assertTrue(validEval.canDecryptNow)

        // Invalid PIN stays locked
        val invalidEval = service.evaluateTimeLock(futureEpoch, "WRONG-PIN")
        assertEquals(com.example.service.TacticalTimeLockService.TimeLockStatus.ACTIVE_LOCKED, invalidEval.status)
        assertFalse(invalidEval.canDecryptNow)
    }

    @Test
    fun testAppDisguiseProfilesAndPersistence() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        
        // Check default initial disguise
        val initial = com.example.util.AppDisguiseManager.getActiveDisguise(context)
        assertNotNull(initial)

        // Verify all 4 profiles
        val profiles = com.example.util.AppDisguiseManager.AppDisguiseProfile.values()
        assertEquals(4, profiles.size)

        val calcProfile = com.example.util.AppDisguiseManager.AppDisguiseProfile.CALCULATOR
        assertEquals("MainActivityCalculatorDisguise", calcProfile.aliasSimpleName)
        assertEquals("Kalkulator", calcProfile.displayName)
        assertTrue(calcProfile.isCamouflage)

        val toolsProfile = com.example.util.AppDisguiseManager.AppDisguiseProfile.SYSTEM_TOOLS
        assertEquals("MainActivityToolsDisguise", toolsProfile.aliasSimpleName)
        assertEquals("Alat Sistem", toolsProfile.displayName)
        assertTrue(toolsProfile.isCamouflage)

        val weatherProfile = com.example.util.AppDisguiseManager.AppDisguiseProfile.WEATHER_NOTES
        assertEquals("MainActivityWeatherDisguise", weatherProfile.aliasSimpleName)
        assertEquals("Catatan Cuaca", weatherProfile.displayName)
        assertTrue(weatherProfile.isCamouflage)

        val defaultProfile = com.example.util.AppDisguiseManager.AppDisguiseProfile.MILITARY_DEFAULT
        assertEquals("MainActivityDefault", defaultProfile.aliasSimpleName)
        assertEquals("SANDI-KARTIKA", defaultProfile.displayName)
        assertFalse(defaultProfile.isCamouflage)

        // Test applying and retrieving profile
        val appliedCalc = com.example.util.AppDisguiseManager.applyDisguise(context, calcProfile)
        assertTrue(appliedCalc)
        assertEquals(calcProfile, com.example.util.AppDisguiseManager.getActiveDisguise(context))

        // Revert to military default
        val appliedDefault = com.example.util.AppDisguiseManager.applyDisguise(context, defaultProfile)
        assertTrue(appliedDefault)
        assertEquals(defaultProfile, com.example.util.AppDisguiseManager.getActiveDisguise(context))
    }

    @Test
    fun testStandaloneHtmlGeneratorAirgapParity() {
        val context = androidx.test.core.app.ApplicationProvider.getApplicationContext<android.content.Context>()
        val (file, uri) = com.example.stego.StandaloneHtmlGenerator.generateHtmlFile(context)

        assertNotNull(file)
        assertNotNull(uri)
        assertTrue(file.exists())
        assertTrue(file.length() > 5000)

        val htmlContent = file.readText(Charsets.UTF_8)
        // Verify 100% offline W3C Web Crypto API
        assertTrue(htmlContent.contains("crypto.subtle"))
        assertTrue(htmlContent.contains("PBKDF2"))
        assertTrue(htmlContent.contains("AES-GCM"))
        // Verify military commander override PIN
        assertTrue(htmlContent.contains("PUSSIBERAD-991"))
        // Verify binary header compatibility (SDKR)
        assertTrue(htmlContent.contains("0x53, 0x44, 0x4B, 0x52"))
        // Verify zero external network resources (no CDN or external scripts)
        assertFalse(htmlContent.contains("http://"))
        assertFalse(htmlContent.contains("https://"))
    }
}
