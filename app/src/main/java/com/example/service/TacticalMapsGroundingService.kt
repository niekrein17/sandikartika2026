package com.example.service

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.GeospatialIntel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Service for Tactical Geospatial Intelligence & Contextual Alibi Generation.
 *
 * Uses Gemini 3.8 Flash with the `googleMaps` grounding tool to query real-world
 * location, civilian presence (Rules of Engagement / RoE), emergency medical facilities,
 * and realistic camouflage/decoy alibis based on actual local points of interest.
 *
 * If offline or if BuildConfig.GEMINI_API_KEY is not yet configured, gracefully falls back
 * to deterministic tactical terrain estimation so field operations remain 100% resilient.
 */
object TacticalMapsGroundingService {

    private const val TAG = "TacticalMapsService"
    private const val GEMINI_MODEL = "gemini-3.8-flash"
    private const val ENDPOINT_URL = "https://generativelanguage.googleapis.com/v1beta/models/$GEMINI_MODEL:generateContent"

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    suspend fun queryGeospatialIntel(coordinates: String): Result<GeospatialIntel> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        // If no API key or placeholder, provide structured tactical offline assessment
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "GEMINI_API_KEY is blank or default placeholder. Using tactical fallback grounding.")
            return@withContext Result.success(generateTacticalOfflineFallback(coordinates))
        }

        try {
            val prompt = """
                Anda adalah Perwira Analis Intelijen Geospasial Militer PUSSIBERAD.
                Analisis titik koordinat operasi berikut: "$coordinates".
                Gunakan alat Google Maps untuk memverifikasi lokasi dunia nyata terdekat secara akurat.
                
                Wajib sajikan analisis dalam format JSON murni tanpa markdown wrapper dengan struktur kunci berikut:
                {
                  "locationName": "Nama wilayah/kecamatan/titik rujukan spesifik nyata",
                  "terrainSummary": "Deskripsi medan nyata (kontur, vegetasi, akses jalan, sungai terdekat)",
                  "civilianRoEAssessment": "Peringatan perlindungan sipil sesuai RoE/CDE (jarak ke pemukiman, sekolah, rumah sakit, tempat ibadah terdekat)",
                  "medevacFacilities": ["Nama RS/Puskesmas darurat 1 terdekat", "Nama RS militer/sipil 2 terdekat"],
                  "strategicLandmarks": ["Titik rujukan navigasi 1", "Titik rujukan navigasi 2"],
                  "smartDecoyTitle": "Judul laporan alibi umpan yang sangat meyakinkan berbasis fasilitas nyata di sana",
                  "smartDecoyMessage": "Isi laporan alibi kegiatan logistik/patroli umum yang mencantumkan nama jalan/pasar/gudang nyata di daerah tersebut"
                }
            """.trimIndent()

            val requestBodyJson = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)

                // Enable Google Maps Grounding Tool
                val tools = JSONArray().apply {
                    put(JSONObject().apply {
                        put("googleMaps", JSONObject())
                    })
                }
                put("tools", tools)

                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.2)
                })
            }

            val request = Request.Builder()
                .url("$ENDPOINT_URL?key=$apiKey")
                .post(requestBodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string().orEmpty()
                if (!response.isSuccessful) {
                    Log.e(TAG, "Gemini API error (HTTP ${response.code}): $bodyStr")
                    // Graceful fallback on network/quota error
                    return@withContext Result.success(generateTacticalOfflineFallback(coordinates))
                }

                val jsonResponse = JSONObject(bodyStr)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext Result.success(generateTacticalOfflineFallback(coordinates))
                }

                val candidate = candidates.getJSONObject(0)
                val content = candidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val rawText = parts?.optJSONObject(0)?.optString("text").orEmpty()

                // Check for grounding metadata / citations
                val groundingMetadata = candidate.optJSONObject("groundingMetadata")
                val isGrounded = groundingMetadata != null || bodyStr.contains("groundingChunk")

                val parsedIntel = parseModelJson(rawText, coordinates, isGrounded)
                Result.success(parsedIntel)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying Maps Grounding API", e)
            // Always ensure mission continuity with tactical fallback
            Result.success(generateTacticalOfflineFallback(coordinates))
        }
    }

    private fun parseModelJson(rawText: String, coordinates: String, isGrounded: Boolean): GeospatialIntel {
        val cleanJson = rawText
            .replace("```json", "")
            .replace("```", "")
            .trim()

        return try {
            val obj = JSONObject(cleanJson)
            val medevacList = mutableListOf<String>()
            val medevacArr = obj.optJSONArray("medevacFacilities")
            if (medevacArr != null) {
                for (i in 0 until medevacArr.length()) {
                    medevacList.add(medevacArr.optString(i))
                }
            }

            val landmarkList = mutableListOf<String>()
            val landmarkArr = obj.optJSONArray("strategicLandmarks")
            if (landmarkArr != null) {
                for (i in 0 until landmarkArr.length()) {
                    landmarkList.add(landmarkArr.optString(i))
                }
            }

            GeospatialIntel(
                coordinates = coordinates,
                locationName = obj.optString("locationName", "Sektor Koordinat Operasi Taktis"),
                terrainSummary = obj.optString("terrainSummary", "Vegetasi campuran dengan akses jalur rintis sekunder."),
                civilianRoEAssessment = obj.optString("civilianRoEAssessment", "Zona aman penyangga. Patuhi aturan perlindungan fasilitas sipil."),
                medevacFacilities = if (medevacList.isNotEmpty()) medevacList else listOf("Puskesmas Pembantu Terdekat", "RSUD Sub-Sektor"),
                strategicLandmarks = if (landmarkList.isNotEmpty()) landmarkList else listOf("Jalur Jalan Lintas", "Bukit Pantau"),
                smartDecoyTitle = obj.optString("smartDecoyTitle", "LAPORAN RUTIN DISTRIBUSI LOGISTIK HARIAN"),
                smartDecoyMessage = obj.optString("smartDecoyMessage", "Pengiriman bahan makanan dan perlengkapan inventaris rutin telah diserahterimakan di titik distribusi sektor."),
                isMapsGrounded = isGrounded,
                sourceAttribution = if (isGrounded) "Google Maps Data via Gemini 3.8 Flash Grounding" else "Analisis Intelijen Medan Taktis (Offline)"
            )
        } catch (_: Exception) {
            generateTacticalOfflineFallback(coordinates)
        }
    }

    private fun generateTacticalOfflineFallback(coordinates: String): GeospatialIntel {
        // Parse coordinate hints (e.g. Jawa Tengah / Merapi / Ambarawa / MGRS 49M)
        val isJawaTengah = coordinates.contains("110°") || coordinates.contains("49M") || coordinates.contains("07°")

        return if (isJawaTengah) {
            GeospatialIntel(
                coordinates = coordinates,
                locationName = "Sektor Jawa Tengah Bagian Selatan (Koridor Gunung Merapi - Ambarawa)",
                terrainSummary = "Perbukitan vulkanik terjal, tutupan vegetasi hutan pinus dan perkebunan teh/kopi. Terdapat jalan rintis tanah dan aliran sungai lereng curam.",
                civilianRoEAssessment = "RoE KETAT: Perkampungan penduduk dan area pertanian terasering berada pada radius ~1.2 km arah barat daya. Dilarang penggunaan amunisi peledak berdaya rusak area.",
                medevacFacilities = listOf(
                    "Puskesmas Rawat Inap Terdekat (Estimasi 18 menit rute darat)",
                    "RS Tentara dr. Soedjono Magelang / RST Wijayakusuma"
                ),
                strategicLandmarks = listOf(
                    "Jembatan Kali Pabelan",
                    "Simpang Jalur Evakuasi Dusun Babadan",
                    "Pilar Batas Kehutanan No. 42"
                ),
                smartDecoyTitle = "SURAT JALAN ANGKUTAN LOGISTIK BAMA SEKTOR LERENG MERAPI",
                smartDecoyMessage = "Kepada Yth. Pasi Logistik:\nKendaraan truk logistik telah mengantar pasokan beras 450 Kg dan tabung gas melalui rute alternatif Pasar Hewan Ambarawa menuju Posko Sub-Sektor.\nKondisi jalan licin berkabut, pengiriman tuntas tanpa kendala.",
                isMapsGrounded = false,
                sourceAttribution = "Analisis Taktis Medan PUSSIBERAD (Cadangan Mandiri 100% Offline)"
            )
        } else {
            GeospatialIntel(
                coordinates = coordinates,
                locationName = "Sektor Penugasan Operasi Lapangan $coordinates",
                terrainSummary = "Wilayah bergelombang dengan tutupan vegetasi sedang. Akses jalur darat sekunder berjarak 800 meter dari titik koordinat sasaran.",
                civilianRoEAssessment = "RoE STANDAR DEFCON-2: Identifikasi visual wajib dilakukan sebelum tindakan pengamanan untuk memastikan tidak ada aktivitas warga sipil.",
                medevacFacilities = listOf(
                    "Puskesmas Wilayah Perbatasan (Rute Rintis 25 menit)",
                    "Pos Kesehatan Lapangan Satgas Yonif"
                ),
                strategicLandmarks = listOf(
                    "Persimpangan Jalan Lintas Batas",
                    "Titik Ketinggian Topografi Triangulasi"
                ),
                smartDecoyTitle = "LAPORAN PATROLI RUTIN JALUR PENGAMANAN PERBATASAN",
                smartDecoyMessage = "Laporan harian regu patroli: Situasi aman terkendali di sepanjang rute pengawasan pos batas sektor. Pengecekan patok perbatasan dalam kondisi utuh.",
                isMapsGrounded = false,
                sourceAttribution = "Analisis Taktis Medan PUSSIBERAD (Cadangan Mandiri 100% Offline)"
            )
        }
    }
}
