package com.example.data.model

data class TacticalTemplate(
    val id: String,
    val title: String,
    val isDecoy: Boolean,
    val defaultTitle: String,
    val defaultMessage: String,
    val defaultCoordinates: String?,
    val defaultClassification: PayloadClassification
) {
    companion object {
        val REAL_TEMPLATES = listOf(
            TacticalTemplate(
                id = "real_ops",
                title = "Perintah Operasi (PO) Khusus",
                isDecoy = false,
                defaultTitle = "PO KARTIKA SAKTI - FASE 2",
                defaultMessage = "1. Sasaran utama: Penetralan frekuensi pemancar musuh sektor barat daya.\n2. Waktu Serangan (H-Hour): 04:30 WIB.\n3. Pergerakan senyap melalui jalur rintis lembah 4.\n4. Kode panggil sandi: RAJAWALI-9. Jangan buka komunikasi radio terbuka hingga sasaran dikuasai.",
                defaultCoordinates = "07°14'22.1\"S 110°24'18.8\"E (MGRS: 49M FM 3418 9912)",
                defaultClassification = PayloadClassification.TOP_SECRET
            ),
            TacticalTemplate(
                id = "real_recon",
                title = "Laporan Pengintaian Titik Kritis",
                isDecoy = false,
                defaultTitle = "INTEL RECON // PATROLI MALAM",
                defaultMessage = "Pengamatan sektor bravo: Terlihat pergerakan 3 unit rantis tanpa identifikasi di koordinat sasaran. Penjagaan pos penjagaan diperketat 2 shift. Rencana penyusupan disarankan melalui titik buta sektor selatan pada titik koordinat terlampir.",
                defaultCoordinates = "06°55'12.4\"S 107°37'05.2\"E (MGRS: 48M XM 6231 3489)",
                defaultClassification = PayloadClassification.CONFIDENTIAL
            ),
            TacticalTemplate(
                id = "real_evac",
                title = "Prosedur Evakuasi Darurat (CASEVAC)",
                isDecoy = false,
                defaultTitle = "CASEVAC EXFIL PROTOKOL DELTA",
                defaultMessage = "Zona Penjemputan Darurat (LZ): Lapangan darurat lereng bukit sektor C. Sinyal identifikasi visual: Suar infra-merah kode berkedip 3-1. Hubungi frekuensi cadangan 44.80 MHz UHF sandi 'GARUDA EMAS'. Waktu bertahan maksimal 45 menit.",
                defaultCoordinates = "07°45'30.0\"S 110°22'10.5\"E",
                defaultClassification = PayloadClassification.TOP_SECRET
            )
        )

        val DECOY_TEMPLATES = listOf(
            TacticalTemplate(
                id = "decoy_ration",
                title = "Laporan Bama & Bahan Makanan Kompi",
                isDecoy = true,
                defaultTitle = "LAPORAN RUTIN BAMA PEKAN KE-IV",
                defaultMessage = "Kepada Yth. Pasi Logistik:\nRekapitulasi persediaan bahan makanan (Bama) Kompi Markas dalam kondisi cukup untuk 14 hari ke depan:\n- Beras Kelas I: 450 Kg\n- Minyak Goreng: 60 Liter\n- Gula & Kopi: 30 Kg\n- Daging Kaleng T2: 120 pack\nPermohonan pengiriman tambahan garam dan tabung gas untuk dapur lapangan pada rotasi pekan depan. Demikian untuk menjadi periksa.",
                defaultCoordinates = null,
                defaultClassification = PayloadClassification.DECOY_ROUTINE
            ),
            TacticalTemplate(
                id = "decoy_fuel",
                title = "Inspeksi BBM & Pemeliharaan Truk NPS",
                isDecoy = true,
                defaultTitle = "STATUS LOGISTIK HAR-RANPUR RUTIN",
                defaultMessage = "Hasil pengecekan fisik 4 unit truk dinas Isuzu NPS Yonif:\n- Ranpur 01: Siap gerak, solar 85%\n- Ranpur 02: Siap gerak, solar 90%\n- Ranpur 03: Perbaikan kampas rem roda belakang selesai\n- Ranpur 04: Penggantian oli mesin terjadwal lusa.\nKondisi keseluruhan kendaraan dalam status laik jalan untuk dinas pengangkutan personel.",
                defaultCoordinates = null,
                defaultClassification = PayloadClassification.DECOY_ROUTINE
            ),
            TacticalTemplate(
                id = "decoy_weather",
                title = "Catatan Kondisi Cuaca & Curah Hujan",
                isDecoy = true,
                defaultTitle = "LOG HARIAN METEOROLOGI POS LAPANGAN",
                defaultMessage = "Pencatatan stasiun cuaca lapangan sektor pangkalan:\n- Suhu rata-rata: 24.5°C\n- Kelembapan udara: 82%\n- Curah hujan harian: 12 mm (Hujan ringan sore hari)\n- Kecepatan angin: 8 knot arah barat daya.\nDebit air sungai di hilir normal, jalur pengangkutan logistik aman dilalui kendaraan roda dua dan roda empat.",
                defaultCoordinates = null,
                defaultClassification = PayloadClassification.DECOY_ROUTINE
            )
        )
    }
}
