package com.example.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.R

/**
 * Manages dynamic runtime app icon and launcher label disguising.
 * Toggles Android activity-aliases to make the application appear on the home screen
 * and app drawer as an innocent system utility (e.g., 'Kalkulator', 'Alat Sistem', or 'Catatan Cuaca').
 */
object AppDisguiseManager {

    private const val PREFS_NAME = "opsec_disguise_prefs"
    private const val KEY_ACTIVE_DISGUISE = "active_disguise_profile"

    enum class AppDisguiseProfile(
        val aliasSimpleName: String,
        val displayName: String,
        val subtitle: String,
        val iconRes: Int,
        val isCamouflage: Boolean
    ) {
        MILITARY_DEFAULT(
            aliasSimpleName = "MainActivityDefault",
            displayName = "SANDI-KARTIKA",
            subtitle = "Ikon & Identitas Resmi Militer",
            iconRes = R.drawable.logo_sandi_kartika,
            isCamouflage = false
        ),
        CALCULATOR(
            aliasSimpleName = "MainActivityCalculatorDisguise",
            displayName = "Kalkulator",
            subtitle = "Menyamar sebagai Kalkulator Biasa",
            iconRes = R.drawable.ic_disguise_calculator,
            isCamouflage = true
        ),
        SYSTEM_TOOLS(
            aliasSimpleName = "MainActivityToolsDisguise",
            displayName = "Alat Sistem",
            subtitle = "Menyamar sebagai Utilitas Sistem",
            iconRes = R.drawable.ic_disguise_tools,
            isCamouflage = true
        ),
        WEATHER_NOTES(
            aliasSimpleName = "MainActivityWeatherDisguise",
            displayName = "Catatan Cuaca",
            subtitle = "Menyamar sebagai Buku Log Cuaca",
            iconRes = R.drawable.ic_disguise_weather,
            isCamouflage = true
        )
    }

    /**
     * Activates the selected launcher disguise profile and disables all other aliases.
     *
     * @param context Application context
     * @param targetProfile The desired disguise profile
     * @return True if successfully applied, false on failure
     */
    fun applyDisguise(context: Context, targetProfile: AppDisguiseProfile): Boolean {
        return try {
            val packageManager = context.packageManager
            val packageName = context.packageName

            for (profile in AppDisguiseProfile.values()) {
                val componentName = ComponentName(packageName, "$packageName.${profile.aliasSimpleName}")
                val newState = if (profile == targetProfile) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }

                packageManager.setComponentEnabledSetting(
                    componentName,
                    newState,
                    PackageManager.DONT_KILL_APP
                )
            }

            // Persist active state in local preferences
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_ACTIVE_DISGUISE, targetProfile.name).apply()

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Retrieves the currently active disguise profile.
     */
    fun getActiveDisguise(context: Context): AppDisguiseProfile {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_ACTIVE_DISGUISE, AppDisguiseProfile.MILITARY_DEFAULT.name)
        return try {
            AppDisguiseProfile.valueOf(savedName ?: AppDisguiseProfile.MILITARY_DEFAULT.name)
        } catch (e: Exception) {
            AppDisguiseProfile.MILITARY_DEFAULT
        }
    }
}
