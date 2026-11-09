# BUKU PANDUAN PENGGUNA TEKNIS (USER TECHNICAL GUIDE)
# APLIKASI KOMUNIKASI TAKTIS & STEGANOGRAFI "SANDI-KARTIKA"
**Edisi Prajurit Baru & Panduan Operasional Lapangan // PUSSIBERAD**  
*Ditulis dengan bahasa taktis yang lugas, terstruktur, dan mudah dipahami oleh anggota baru maupun masyarakat umum.*

---

## DAFTAR ISI PANDUAN

1. **PENGENALAN CEPAT (UNTUK APA APLIKASI INI?)**
2. **CARA MEMBUKA APLIKASI (MENEMBUS SAMARAN KALKULATOR)**
3. **LANGKAH 1: MEMILIH FOTO PEMBAWA (CARRIER MEDIA) & PEMBERSIHAN EXIF**
4. **LANGKAH 2: MEMILIH PROFIL TRANSMISI (TAHAN KOMPRESI VS KAPASITAS BESAR)**
5. **LANGKAH 3: MEMBUAT PESAN RAHASIA ASLI (MASTER PAYLOAD)**
6. **LANGKAH 4: MEMBUAT PESAN UMPAN PENYELAMAT NYAWA (DENIABLE DECOY)**
7. **LANGKAH 5: MENGGUNAKAN INTELIJEN MEDAN & ALIBI NYATA (GOOGLE MAPS DATA)**
8. **LANGKAH 6: MENGATUR WAKTU BUKA PESAN (TACTICAL TIME-LOCK)**
9. **LANGKAH 7: MENYIMPAN & MENGIRIM FOTO BERISI SANDI**
10. **LANGKAH 8: CARA MEMBUKA / MENGURAI PESAN DI SISI PENERIMA**
11. **FITUR KEAMANAN DARURAT: PANIC ZEROIZE (TOMBOL HANCURKAN JEJAK)**
12. **MENGGUNAKAN VERSI LAPTOP / KOMPUTER POSKO (SINGLE-FILE HTML)**
13. **ATURAN DISIPLIN OPERASI (OPSEC CHECKLIST PRAJURIT)**

---

## 1. PENGENALAN CEPAT (UNTUK APA APLIKASI INI?)

### Konsep Sederhana:
Bayangkan Anda ingin mengirimkan koordinat sasaran penting atau perintah operasi rahasia melalui WhatsApp, Telegram, radio panggil, atau email publik. 
- Jika Anda mengirim teks acak bertuliskan kode terenkripsi seperti `7F8A9B2C...`, musuh yang menyadap akan **langsung curiga** dan mencegat pesan Anda.
- **SANDI-KARTIKA** bekerja dengan cara menyembunyikan tulisan rahasia tersebut **ke dalam titik-titik warna (piksel) foto dokumentasi biasa**. Bagi orang luar atau sistem penyadap musuh, foto tersebut hanyalah foto pemandangan hutan, pos penjagaan, atau peta biasa.

### 3 Keunggulan Utama yang Wajib Dipahami:
1. **100% Offline (Tanpa Server Luar)**: Seluruh perhitungan enkripsi dan penyembunyian dilakukan langsung di dalam chipset perangkat HP Anda. Tidak ada data yang dikirim ke server internet mana pun.
2. **Sandi Kamuflase Ganda (Anti-Interogasi)**: Dalam satu foto terdapat dua pintu rahasia. Satu kunci untuk membuka pesan rahasia asli, dan satu kunci darurat untuk menampilkan laporan logistik dapur umum jika Anda ditangkap musuh dan dipaksa menyerahkan sandi.
3. **Tahan Kompresi Saluran Radio & Chat**: Pesan tidak akan rusak meskipun foto otomatis terkompresi menjadi format JPEG saat dikirim lewat jaringan radio VHF/HF lapangan atau aplikasi chat.

---

## 2. CARA MEMBUKA APLIKASI (MENEMBUS SAMARAN KALKULATOR)

Aplikasi ini dilengkapi fitur **Penyamaran Aplikasi (Disguise Mode)** agar tidak mencolok saat HP Anda diperiksa pihak lain.

```
+--------------------------------------------------------+
|                   KALKULATOR SISTEM                    |
|                        [  0  ]                         |
|   [ C ]   [ ( ]   [ ) ]   [ / ]                        |
|   [ 7 ]   [ 8 ]   [ 9 ]   [ * ]                        |
|   [ 4 ]   [ 5 ]   [ 6 ]   [ - ]                        |
|   [ 1 ]   [ 9 ]   [ 4 ]   [ + ]   <-- Ketik "1945="    |
|   [ 0 ]   [ . ]   [ = ]                                |
+--------------------------------------------------------+
```

### Prosedur Membuka:
1. Di layar utama HP, aplikasi akan tampil dengan ikon dan nama kalkulator biasa bernama **"Kalkulator"**.
2. Buka aplikasi tersebut. Layar akan menampilkan kalkulator fungsional yang bisa dipakai berhitung seperti biasa.
3. **Kunci Pembuka Rahasia (Trapdoor)**:
   - Ketik angka: `1945=` pada tombol kalkulator, ATAU
   - Tekan dan tahan (Long-Press) judul kalkulator di bagian atas selama 2 detik.
4. Layar kalkulator akan berganti seketika menjadi antarmuka konsol taktis militer **SANDI-KARTIKA**.

---

## 3. LANGKAH 1: MEMILIH FOTO PEMBAWA & PEMBERSIHAN EXIF

Pada menu **"1. SISIP SANDI"**, tugas pertama Anda adalah memilih foto yang akan dijadikan wadah penyamaran.

### Pilihan Foto:
1. **Gunakan Foto Bawaan Taktis (Preset)**:
   - Di layar sudah disediakan 4 pilihan foto ilustrasi buatan sistem: *Patroli Hutan*, *Pos Perbatasan*, *Depot Logistik*, atau *Peta Kontur*.
   - Kelebihan: Dibuat langsung dari memori tanpa kamera fisik sehingga tidak meninggalkan jejak digital apapun.
2. **Gunakan Foto dari Galeri Sendiri**:
   - Klik tombol **"PILIH FOTO DARI GALERI"**.
   - Pilih foto apa saja (misalnya foto pemandangan di sekitar pos tugas).
   - **Fitur Otomatis OPSEC Sanitizer**: Aplikasi akan secara otomatis menghapus seluruh data GPS lokasi, waktu pemotretan, dan merek kamera (data EXIF) dari foto tersebut sehingga lokasi markas aju Anda tidak bocor ke lawan.

---

## 4. LANGKAH 2: MEMILIH PROFIL TRANSMISI

Pilih salah satu dari dua profil transmisi sesuai kondisi jaringan komunikasi Anda:

| Pilihan Profil | Kapan Harus Dipakai? | Daya Tampung | Ketahanan Saluran |
| :--- | :--- | :--- | :--- |
| **PROFIL TAKTIS (Tahan Kompresi)** *(Direkomendasikan untuk tugas lapangan)* | Untuk kirim Perintah Operasi ringkas, koordinat sasaran MGRS, dan laporan intelijen melalui radio HT/VHF, MMS, atau chat biasa. | Hingga ±5.600 Karakter (5,6 KB) | **Sangat Kuat**. Kebal kompresi JPEG/WebP berkat algoritma frekuensi DCT + koreksi kesalahan Reed-Solomon 30%. |
| **PROFIL DOKUMEN (Kapasitas Besar)** | Untuk mengirim file laporan panjang, lampiran dokumen, atau tabel logistik besar. | Hingga ±190.000 Karakter (190 KB) | Memerlukan transmisi utuh. Wajib dikirim dengan opsi *'Kirim sebagai Dokumen/File'* tanpa kompresi. |

---

## 5. LANGKAH 3: MEMBUAT PESAN RAHASIA ASLI (MASTER PAYLOAD)

Pada kotak berwarna merah berlabel **"2. Pesan Rahasia Operasi (Master)"**:

1. **Judul Perintah**: Masukkan nama sandi operasi (misal: `PO KARTIKA SAKTI - FASE 2`).
2. **Isi Instruksi Rahasia**: Tuliskan perintah taktis Anda (misal: sasaran serangan, waktu jam-H, kata sandi panggil).
3. **Koordinat Sasaran**: Tuliskan koordinat peta (contoh: `07°14'22.1"S 110°24'18.8"E` atau MGRS: `49M FM 3418 9912`).
4. **Kata Sandi Utama (Master Key)**: Buat kata sandi rahasia yang kuat (contoh: `KARTIKA-2026`). 
   > *PENTING: Hanya beritahukan kata sandi ini kepada rekan tim atau komandan yang berhak menerima perintah.*

---

## 6. LANGKAH 4: MEMBUAT PESAN UMPAN PENYELAMAT NYAWA (DENIABLE DECOY)

Fitur ini dirancang khusus untuk melindungi prajurit jika tertangkap dan diinterogasi musuh (*Plausible Deniability*).

```
SATU FOTO STEGO MEMILIKI DUA PINTU:
  [Kata Sandi Utama]  ---> Terbuka: INSTRUKSI PENYERANGAN RAHASIA
  [Kata Sandi Umpan]  ---> Terbuka: LAPORAN STOK BERAS & TABUNG GAS DAPUR
```

### Cara Mengaktifkan:
1. Pastikan sakelar **"SANDI KAMUFLASE"** dalam posisi aktif (ON).
2. Pada kotak berwarna biru berlabel **"3. Pesan Umpan Kamuflase (Decoy)"**:
   - **Judul Laporan Umpan**: Isi dengan hal-hal biasa (misal: `LAPORAN RUTIN BAMA PEKAN KE-IV`).
   - **Isi Pesan Umpan**: Isi dengan laporan umum yang tidak mencurigakan (misal: stok beras 450 Kg, minyak goreng aman, butuh isi ulang tabung gas dapur posko).
   - **Kata Sandi Umpan (Duress Key)**: Tentukan sandi darurat (misal: `BAMA-LOGISTIK`).

### Prosedur Tempur Saat Ditangkap Lawan:
Jika musuh menyita HP Anda dan memaksa meminta sandi:
- **Berikan Kata Sandi Umpan (`BAMA-LOGISTIK`)**.
- Aplikasi akan terbuka dengan wajar dan hanya menampilkan daftar belanja beras dapur posko.
- Musuh tidak akan curiga karena sistem tidak meninggalkan petunjuk matematis sedikit pun bahwa di dalam foto tersebut masih tersimpan pesan rahasia penyerangan.

---

## 7. LANGKAH 5: MENGGUNAKAN INTELIJEN MEDAN & ALIBI NYATA (GOOGLE MAPS DATA)

Jika Anda sedang berada di Markas Komando atau Posko Taktis yang memiliki akses jaringan aman, Anda dapat mengaktifkan fitur **"2B. Intelijen Geospasial & Alibi Medan"**.

```
[ Sakelar Mode: TIER 1 - MODE MARKAS ]
  -> Tombol: [ VALIDASI MEDAN & RoE ]
  -> Tombol: [ BUAT SANDI UMPAN NYATA ]
```

### Manfaat & Cara Penggunaan:
1. **Validasi Medan & Aturan Pelibatan (RoE)**:
   - Klik tombol **"VALIDASI MEDAN & RoE"**.
   - Gemini 3.8 Flash akan memindai peta Google Maps nyata di sekitar koordinat sasaran Anda.
   - Layar akan menampilkan nama daerah asli, kontur tanah, sungai terdekat, serta **jarak pemukiman sipil/sekolah/rumah sakit terdekat** agar tembakan tidak melanggar aturan perlindungan warga sipil.
   - Layar juga mencantumkan **daftar Puskesmas / RS Tentara terdekat** untuk jalur evakuasi korban darurat (*Medevac*).
2. **Pembuatan Sandi Umpan Berbasis Lokasi Asli**:
   - Klik tombol **"BUAT SANDI UMPAN NYATA"**.
   - Sistem akan mencari pasar, rute jalan, atau fasilitas logistik nyata di dekat koordinat tersebut, lalu membuatkan draf laporan alibi yang valid secara geografis.
   - Klik tombol **"TERAPKAN KE FORM SANDI UMPAN"** agar kolom sandi umpan terisi otomatis. Alibi Anda kini 100% didukung data peta nyata jika diselidiki intelijen musuh.

> **Catatan Doktrin**: Saat prajurit bergerak ke garis depan, geser sakelar ke **"TIER 2: MODE TEMPUR (100% OFFLINE)"**. Aplikasi akan mematikan seluruh fungsi internet untuk menjaga disiplin hening radio (*Radio Silence*).

---

## 8. LANGKAH 6: MENGATUR WAKTU BUKA PESAN (TACTICAL TIME-LOCK)

Fitur ini berguna agar dokumen operasi tidak bisa dibuka terlalu cepat sebelum waktu penyerangan dimulai (mencegah kebocoran rencana operasi sebelum jam-H).

1. Aktifkan sakelar **"PEMBATASAN WAKTU BUKA (TIME-LOCK)"**.
2. Pilih durasi penguncian (contoh: *4 Jam ke Depan*).
3. Hasilnya: Penerima yang memiliki sandi yang benar sekali pun **tetap tidak bisa membaca pesan** sebelum 4 jam berlalu, kecuali perwira yang memegang **PIN Khusus Komandan Lapangan** (`PUSSIBERAD-991`).

---

## 9. LANGKAH 7: MENYIMPAN & MENGIRIM FOTO BERISI SANDI

1. Gulir ke bagian paling bawah, lalu tekan tombol hijau besar:  
   **"ENKRIPSI & SISIPKAN SANDI (AES-256-GCM)"**.
2. Sistem akan memproses dalam 1–2 detik:
   - Data dienkripsi dengan standar militer AES-256-GCM.
   - Ditambahkan lapisan koreksi kesalahan Reed-Solomon.
   - Disisipkan secara tak terlihat ke dalam piksel foto.
3. Muncul kotak hasil berisikan foto stego yang telah siap:
   - Tekan tombol **"BAGIKAN .PNG"** untuk langsung mengirimkan foto ke rekan melalui aplikasi perpesanan atau Bluetooth.
   - Tekan tombol **"PAKET LENGKAP (.PNG + .HTML)"** jika Anda ingin membagikan foto sekaligus file kunci pembukanya untuk dibuka di laptop penerima.

---

## 10. LANGKAH 8: CARA MEMBUKA / MENGURAI PESAN DI PENERIMA

Bagi prajurit penerima foto di lapangan:

1. Buka aplikasi **SANDI-KARTIKA**, lalu masuk ke tab kedua: **"URAI SANDI"**.
2. Masukkan foto yang baru saja diterima dari rekan/komando:
   - Klik tombol **"PILIH CITRA STEGO DARI GALERI"**, atau gunakan hasil dari tab sebelumnya.
3. Masukkan kata sandi pada kolom input:
   - **Jika Anda memasukkan Sandi Utama (`KARTIKA-2026`)**: Layar akan menampilkan kartu hijau bertuliskan **"INSTRUKSI OPERASI RAHASIA"**, lengkap dengan koordinat titik tembak MGRS dan isi perintah lengkap.
   - **Jika Anda memasukkan Sandi Umpan (`BAMA-LOGISTIK`)**: Layar akan menampilkan kartu biru bertuliskan **"LAPORAN RUTIN UMUM"** berisikan stok beras dan logistik dapur posko biasa.
4. **Tombol "Uji Simulasi JPEG (Q=80)"**:
   - Jika Anda ingin menguji keandalan sistem terhadap kompresi saluran transmisi, tekan tombol ini.
   - Anda akan melihat indikator **Reed-Solomon FEC** berhasil memulihkan puluhan hingga ratusan byte data yang sempat rusak terkompresi secara otomatis!

---

## 11. FITUR KEAMANAN DARURAT: PANIC ZEROIZE

Dalam situasi darurat di mana posko diserbu atau prajurit terkepung:

1. Masuk ke tab **"INSPEKTUR FORENSIK"**.
2. Di bagian paling bawah terdapat tombol merah berkedip:  
   **"PANIC ZEROIZE (HANCURKAN SELURUH DATA MEMORI)"**.
3. Sekali tombol ini ditekan:
   - Seluruh kunci sandi di memori RAM HP langsung ditimpa angka nol (*cryptographic zeroization*).
   - Seluruh teks perintah operasi, koordinat, dan riwayat di layar dihapus seketika.
   - Aplikasi otomatis mengunci dirinya dan kembali ke layar **Kalkulator Samaran biasa**.
   - Musuh yang merebut perangkat Anda tidak akan mendapatkan sisa data apa pun dari RAM ponsel.

---

## 12. MENGGUNAKAN VERSI LAPTOP / KOMPUTER POSKO (SINGLE-FILE HTML)

Jika operasi dilakukan di markas dengan laptop taktis lapangan:

1. Anda tidak perlu menginstal software apa pun. Cukup cari file bernama **`SANDI_KARTIKA_OFFLINE_SUITE.html`**.
2. Klik ganda (Double-Click) file tersebut. File akan langsung terbuka di peramban apa pun (Chrome, Edge, Firefox, Safari).
3. **100% Berjalan Tanpa Internet**: Matikan Wi-Fi atau cabut kabel LAN laptop Anda untuk membuktikan kedaulatan data. Seluruh fungsi penyisipan, penguraian, enkripsi AES-256-GCM, dan rekonstruksi Reed-Solomon bekerja murni menggunakan Web Cryptography API internal browser.

---

## 13. ATURAN DISIPLIN OPERASI (OPSEC CHECKLIST PRAJURIT)

Setiap prajurit yang mengoperasikan sistem ini wajib mematuhi 5 Doktrin Keamanan Komunikasi berikut:

- [ ] **Disiplin Sandi**: Jangan pernah mencatat kata sandi Master dan kata sandi Decoy di secarik kertas atau di aplikasi catatan ponsel.
- [ ] **Disiplin Saluran**: Jika mengirim dokumen besar lewat **Profil Dokumen**, gunakan fitur *'Send as Document / File'* agar aplikasi chat tidak memangkas kualitas foto PNG secara berlebihan.
- [ ] **Disiplin Hening Radio**: Gunakan **Profil Taktis (DCT + Reed-Solomon)** saat berkomunikasi melalui radio HT/VHF lapangan.
- [ ] **Disiplin Interogasi**: Jika tertangkap musuh, berikan Sandi Umpan dengan tenang dan meyakinkan. Jangan mengaku bahwa ada sandi kedua.
- [ ] **Disiplin Panic Zeroize**: Tekan tombol *Panic Zeroize* saat tanda bahaya pangkalan berbunyi sebelum perangkat berpindah tangan ke pihak lawan.

---
*Dokumen ini merupakan pedoman resmi pegangan prajurit SANDI-KARTIKA. Satria Yudha Waskita.*
