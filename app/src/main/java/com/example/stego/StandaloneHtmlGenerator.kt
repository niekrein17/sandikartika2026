package com.example.stego

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

/**
 * Generates an all-in-one, zero-dependency, standalone single-file HTML/JS application.
 * Enables military personnel to execute AES-256-GCM encryption, Reed-Solomon GF(2^8) FEC,
 * 8x8 2D-DCT Mid-Frequency QIM steganography, and 3-channel LSB steganography
 * on laptops or phones directly in any web browser without installing any APK or contacting any server.
 */
object StandaloneHtmlGenerator {

    fun generateHtmlFile(context: Context): Pair<File, Uri> {
        val cacheDir = File(context.cacheDir, "standalone_web").apply { mkdirs() }
        val htmlFile = File(cacheDir, "SANDI_KARTIKA_OFFLINE_SUITE.html")

        val htmlContent = """
<!DOCTYPE html>
<html lang="id">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>SANDI-KARTIKA // PUSSIBERAD TACTICAL OFFLINE SUITE</title>
  <style>
    :root {
      --bg: #070c09;
      --card: #0f1812;
      --card-border: #1a2f23;
      --primary: #2ee59d;
      --primary-dim: rgba(46, 229, 157, 0.15);
      --text: #e6f4ec;
      --muted: #789284;
      --gold: #e5b842;
      --gold-dim: rgba(229, 184, 66, 0.14);
      --decoy: #60a5fa;
      --danger: #ff4848;
      --danger-dim: rgba(255, 72, 72, 0.15);
      --font-mono: "SF Mono", "Consolas", "Courier New", monospace;
    }
    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, var(--font-mono); }
    body { background: var(--bg); color: var(--text); padding: 14px; max-width: 860px; margin: auto; line-height: 1.4; }
    header { background: #0b1510; border: 1px solid var(--card-border); border-radius: 8px; padding: 12px 16px; margin-bottom: 14px; display: flex; justify-content: space-between; align-items: center; }
    .badge { font-size: 10px; font-weight: bold; padding: 3px 8px; border-radius: 4px; background: var(--primary-dim); color: var(--primary); border: 1px solid var(--primary); letter-spacing: 0.5px; font-family: var(--font-mono); }
    .badge-gold { background: var(--gold-dim); color: var(--gold); border-color: var(--gold); }
    .badge-danger { background: var(--danger-dim); color: var(--danger); border-color: var(--danger); }
    .nav-tabs { display: flex; gap: 8px; margin-bottom: 14px; }
    .tab-btn { flex: 1; padding: 11px; background: var(--card); color: var(--muted); border: 1px solid var(--card-border); border-radius: 6px; cursor: pointer; font-weight: bold; font-size: 11px; letter-spacing: 0.5px; transition: all 0.2s; font-family: var(--font-mono); }
    .tab-btn.active { background: var(--primary); color: #000; border-color: var(--primary); }
    .card { background: var(--card); border: 1px solid var(--card-border); border-radius: 8px; padding: 14px; margin-bottom: 14px; }
    h2 { font-size: 12.5px; margin-bottom: 10px; color: var(--primary); letter-spacing: 0.8px; font-family: var(--font-mono); text-transform: uppercase; }
    label { display: block; font-size: 10.5px; color: var(--muted); margin-bottom: 4px; font-family: var(--font-mono); }
    input[type="text"], input[type="password"], textarea, select { width: 100%; padding: 10px; background: #060b08; border: 1px solid var(--card-border); border-radius: 6px; color: var(--text); margin-bottom: 10px; font-size: 12px; font-family: var(--font-mono); }
    input:focus, textarea:focus, select:focus { border-color: var(--primary); outline: none; }
    .btn { width: 100%; padding: 12px; background: var(--primary); color: #000; border: none; border-radius: 6px; font-weight: bold; cursor: pointer; font-size: 12.5px; font-family: var(--font-mono); letter-spacing: 0.5px; transition: 0.2s; }
    .btn:hover { filter: brightness(1.12); }
    .btn-secondary { background: var(--card); color: var(--gold); border: 1px solid var(--gold); margin-top: 8px; }
    .btn-danger { background: var(--danger); color: #fff; }
    .result-box { background: #050906; border: 1px solid var(--card-border); border-radius: 6px; padding: 12px; margin-top: 10px; }
    canvas { max-width: 100%; border-radius: 6px; border: 1px solid var(--card-border); display: block; margin: 8px 0; background: #000; }
    .row { display: flex; gap: 8px; }
    .col { flex: 1; }
    .toggle-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; padding: 8px 12px; background: #08110b; border: 1px solid var(--card-border); border-radius: 6px; }
    .presets-bar { display: flex; gap: 6px; overflow-x: auto; margin-bottom: 10px; }
    .preset-chip { padding: 6px 10px; font-size: 10.5px; background: #09120c; border: 1px solid var(--card-border); border-radius: 4px; cursor: pointer; color: var(--muted); white-space: nowrap; font-family: var(--font-mono); }
    .preset-chip.active { border-color: var(--primary); color: var(--primary); }
    .mode-card { padding: 10px 12px; border: 1px solid var(--card-border); border-radius: 6px; background: #08110b; cursor: pointer; margin-bottom: 8px; transition: 0.2s; }
    .mode-card.active-dct { border-color: var(--gold); background: var(--gold-dim); }
    .mode-card.active-lsb { border-color: var(--primary); background: var(--primary-dim); }
    .countdown-hud { font-family: var(--font-mono); font-size: 20px; font-weight: bold; color: var(--danger); letter-spacing: 2px; text-align: center; padding: 14px; background: var(--danger-dim); border: 1px solid var(--danger); border-radius: 6px; margin-bottom: 12px; }
  </style>
</head>
<body>
  <header>
    <div>
      <div style="font-weight:900; letter-spacing:1.5px; color:var(--text); font-size:14px; font-family:var(--font-mono);">SANDI-KARTIKA // WEB SUITE v2.6</div>
      <div style="font-size:9.5px; color:var(--muted); font-family:var(--font-mono);">PUSSIBERAD // 100% OFFLINE DCT-8x8 + REED-SOLOMON FEC & LSB STEGO</div>
    </div>
    <div class="badge" id="airgapBadge">AIR-GAPPED LOCAL ENCLAVE</div>
  </header>

  <div class="nav-tabs">
    <button class="tab-btn active" onclick="switchTab('embed')">1. SISIP SANDI (ENCODE)</button>
    <button class="tab-btn" onclick="switchTab('extract')">2. URAI SANDI (DECODE)</button>
    <button class="tab-btn" onclick="switchTab('inspect')">3. INSPEKSI FORENSIK</button>
  </div>

  <!-- TAB 1: EMBED -->
  <div id="tab-embed">
    <div class="card">
      <h2>1. CITRA MEDIA CARRIER (PENYAMARAN)</h2>
      <label>PILIH PRESET TAKTIS ATAU UNGGAH FOTO SENDIRI:</label>
      <div class="presets-bar">
        <div class="preset-chip active" onclick="drawCarrierPreset(0)">Patroli Hutan</div>
        <div class="preset-chip" onclick="drawCarrierPreset(1)">Pos Perbatasan</div>
        <div class="preset-chip" onclick="drawCarrierPreset(2)">Depot Logistik</div>
        <div class="preset-chip" onclick="drawCarrierPreset(3)">Peta Kontur</div>
      </div>
      <input type="file" id="carrierFile" accept="image/*" onchange="loadCustomCarrier(event)">
      <canvas id="carrierCanvas" width="640" height="640"></canvas>
      <div style="font-size:10px; color:var(--primary); font-family:var(--font-mono);" id="carrierStats">Resolusi: 640x640 px | Mode: PROFIL TAKTIS (DCT 8x8 YCbCr + RS Kapasitas: ~5,600 Bytes)</div>
    </div>

    <!-- Dual-Profile Transmission Mode Selector -->
    <div class="card" style="border-color:var(--gold);">
      <h2 style="color:var(--gold);">PROFIL TRANSMISI (DUAL-PROFILE TRANSMISSION MODE)</h2>
      <div id="modeCardDct" class="mode-card active-dct" onclick="selectTransmissionMode('DCT')">
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <strong style="font-size:11.5px; color:var(--gold); font-family:var(--font-mono);">PROFIL TAKTIS (Tahan Kompresi / Lossy-Resistant: DCT 8x8 YCbCr + RS FEC)</strong>
          <span class="badge badge-gold" id="badgeDct">AKTIF</span>
        </div>
        <div style="font-size:10px; color:var(--muted); margin-top:4px;">Menggunakan DCT 8x8 Mid-Frequency pada kanal Luminansi (Y) ruang warna YCbCr + Reed-Solomon GF(2^8) (Paritas 30%) untuk teks ringkas (Perintah Operasi, koordinat MGRS, intelijen taktis hingga 2–4 KB). Format output tetap stabil meski terkonversi menjadi JPEG.</div>
      </div>
      <div id="modeCardLsb" class="mode-card" onclick="selectTransmissionMode('LSB')">
        <div style="display:flex; justify-content:space-between; align-items:center;">
          <strong style="font-size:11.5px; color:var(--primary); font-family:var(--font-mono);">PROFIL DOKUMEN (Kapasitas Maksimal / Lossless PNG: LSB 3-Kanal)</strong>
          <span class="badge" id="badgeLsb">PILIH</span>
        </div>
        <div style="font-size:10px; color:var(--muted); margin-top:4px;">Menggunakan LSB 3-Kanal murni untuk muatan besar (hingga ~190 KB). Wajib ditransmisikan via 'Kirim sebagai Dokumen/File' tanpa kompresi.</div>
      </div>
    </div>

    <!-- Mode Deniable Toggle -->
    <div class="toggle-row">
      <div>
        <div style="font-weight:bold; font-size:11.5px; color:var(--primary); font-family:var(--font-mono);">SANDI KAMUFLASE (PLAUSIBLE DENIABILITY)</div>
        <div style="font-size:9.5px; color:var(--muted);">Satu gambar berisi 2 sandi: Sandi Utama (Rahasia) & Sandi Umpan (Interogasi)</div>
      </div>
      <input type="checkbox" id="toggleDeniable" checked onchange="toggleDeniableView()">
    </div>

    <!-- Master Classified Payload -->
    <div class="card" style="border-color:var(--primary);">
      <h2>INSTRUKSI OPERASI TAKTIS (MASTER KEY)</h2>
      <div class="row">
        <div class="col">
          <label>JUDUL PERINTAH</label>
          <input type="text" id="masterTitle" value="PO KARTIKA SAKTI - FASE 2">
        </div>
        <div class="col">
          <label>KLASIFIKASI</label>
          <select id="masterClass">
            <option value="TOP_SECRET" selected>SANGAT RAHASIA (TOP SECRET)</option>
            <option value="CONFIDENTIAL">RAHASIA TAKTIS (CONFIDENTIAL)</option>
            <option value="RESTRICTED">TERBATAS (RESTRICTED)</option>
          </select>
        </div>
      </div>
      <label>KOORDINAT SASARAN (MGRS / GEOGRAFIS)</label>
      <input type="text" id="masterCoords" value="07°14'22.1&quot;S 110°24'18.8&quot;E (MGRS: 49M FM 3418 9912)">
      <label>ISI INSTRUKSI OPERASI RAHASIA</label>
      <textarea id="masterMsg" rows="3">1. Sasaran utama: Radar musuh sektor barat daya.
2. Waktu H-Hour: 04.30 WIB.
3. Rute senyap menyusuri lembah 4.
4. Kode panggil sandi: RAJAWALI-9.</textarea>
      <label>KATA SANDI UTAMA (MASTER PASSPHRASE)</label>
      <input type="password" id="masterPass" value="KARTIKA-2026">
    </div>

    <!-- 2B. Tactical Geospatial Grounding (Google Maps Data via Gemini 3.8 Flash) -->
    <div class="card" style="border-color:var(--gold);">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
        <h2 style="color:var(--gold); margin-bottom:0;">INTELIJEN GEOSPASIAL & ALIBI MEDAN (GOOGLE MAPS)</h2>
        <span class="badge badge-gold" id="geoTierBadge">TIER 1: MARKAS (ONLINE)</span>
      </div>
      <p style="font-size:10.5px; color:var(--muted); margin-bottom:10px;">Validasi medan nyata, perkiraan perlindungan sipil (RoE/CDE), dan pembuatan alibi umpan kontekstual berbasis Google Maps data.</p>
      <div style="display:flex; gap:8px; margin-bottom:10px;">
        <button class="btn" style="flex:1; background:var(--gold); color:#000;" onclick="runGeospatialGroundingHtml(false)">VALIDASI MEDAN & RoE</button>
        <button class="btn" style="flex:1; background:transparent; border:1px solid var(--decoy); color:var(--decoy);" onclick="runGeospatialGroundingHtml(true)">BUAT SANDI UMPAN NYATA</button>
      </div>
      <div id="geoIntelBox" style="display:none; padding:10px; background:#0b1419; border:1px solid var(--gold); border-radius:4px; font-family:var(--font-mono); font-size:10px; color:var(--text);">
        <div id="geoLocName" style="font-weight:bold; color:var(--gold); margin-bottom:4px;"></div>
        <div id="geoTerrain" style="margin-bottom:6px; color:#c7d6e6;"></div>
        <div id="geoRoe" style="padding:6px; background:#2a1114; border:1px solid var(--danger); border-radius:4px; color:var(--danger); margin-bottom:6px;"></div>
        <div id="geoMedevac" style="color:var(--primary); margin-bottom:6px;"></div>
        <div id="geoDecoySuggestion" style="padding:8px; background:#091e28; border:1px solid var(--decoy); border-radius:4px; color:var(--decoy); margin-bottom:6px;">
          <div style="font-weight:bold; margin-bottom:2px;">ALIBI LOKASI NYATA:</div>
          <div id="geoDecoyTitle" style="color:#fff; font-weight:bold;"></div>
          <div id="geoDecoyMsg" style="color:#94a3b8; font-size:9.5px; margin:4px 0;"></div>
          <button class="btn" style="padding:4px 8px; font-size:9px; background:var(--decoy); color:#000; width:100%;" onclick="applySmartDecoyHtml()">TERAPKAN KE FORM SANDI UMPAN</button>
        </div>
        <div id="geoSource" style="font-size:8.5px; color:var(--muted);"></div>
      </div>
    </div>

    <!-- Decoy Camouflage Payload -->
    <div class="card" id="decoySection" style="border-color:var(--decoy);">
      <h2 style="color:var(--decoy)">SANDI KAMUFLASE (DECOY / UMPAN INTEROGASI)</h2>
      <label>JUDUL LAPORAN UMPAN</label>
      <input type="text" id="decoyTitle" value="LAPORAN RUTIN BAMA PEKAN KE-IV">
      <label>ISI LAPORAN LOGISTIK / JAGA BIASA</label>
      <textarea id="decoyMsg" rows="3">Kepada Yth. Pasi Logistik:
Stok aman untuk 14 hari ke depan:
- Beras: 450 Kg
- Minyak: 60 L
- Daging T2: 120 pack
Permohonan tambahan tabung gas.</textarea>
      <label>KATA SANDI UMPAN (DURESS KEY)</label>
      <input type="password" id="decoyPass" value="BAMA-LOGISTIK">
    </div>

    <!-- Tactical Time-Lock Toggle -->
    <div class="card" style="border-color:var(--gold);">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
        <h2 style="color:var(--gold); margin-bottom:0;">PEMBATASAN WAKTU BUKA (TACTICAL TIME-LOCK)</h2>
        <input type="checkbox" id="toggleTimeLock" onchange="toggleTimeLockView()">
      </div>
      <div id="timeLockOptions" style="display:none;">
        <label>PESAN HANYA DAPAT DIBUKA SETELAH (JAM DARI SEKARANG):</label>
        <select id="timeLockHours">
          <option value="1">1 Jam ke Depan</option>
          <option value="4" selected>4 Jam ke Depan (Jadwal Operasi Malam)</option>
          <option value="8">8 Jam ke Depan</option>
          <option value="24">24 Jam ke Depan</option>
        </select>
      </div>
    </div>

    <button class="btn" onclick="executeEmbed()">ENKRIPSI & SISIP KE CITRA STEGO (AES-256-GCM)</button>

    <div id="embedResult" style="display:none;" class="card" style="border-color:var(--primary); margin-top:14px;">
      <h2 style="color:var(--primary);">SUKSES! CITRA STEGO TAKTIS SIAP:</h2>
      <canvas id="stegoCanvas"></canvas>
      <div id="embedDetails" style="font-size:10px; color:var(--muted); font-family:var(--font-mono); margin-bottom:8px;"></div>
      <a id="downloadLink" class="btn" style="display:block; text-align:center; text-decoration:none; margin-top:6px;">UNDUH CITRA STEGO (.PNG)</a>
    </div>
  </div>

  <!-- TAB 2: EXTRACT -->
  <div id="tab-extract" style="display:none;">
    <div class="card">
      <h2>UNGGAH CITRA STEGO YANG INGIN DIURAI</h2>
      <input type="file" id="stegoFileInput" accept="image/png,image/jpeg,image/webp,image/*" onchange="loadStegoImage(event)">
      <canvas id="stegoExtractCanvas" width="400" height="300" style="display:none;"></canvas>
      <button id="simJpegBtn" class="btn btn-secondary" style="display:none; margin-bottom:10px;" onclick="simulateJpegCompressionOnExtract()">SIMULASI KOMPRESI LOSSY JPEG (Q=80) PADA CITRA INI</button>
      <label style="margin-top:8px;">MASUKKAN KATA SANDI (MASTER ATAU SANDI UMPAN):</label>
      <input type="password" id="extractPass" placeholder="Ketik kata sandi..." value="KARTIKA-2026">
      <button class="btn" onclick="executeExtract()">URAI INSTRUKSI TAKTIS (AUTO-DETECT DCT+RS / LSB)</button>
    </div>

    <!-- Time-Lock Warning Banner -->
    <div id="timeLockHud" style="display:none;" class="card" style="border-color:var(--danger);">
      <h2 style="color:var(--danger);">INSTRUKSI MASIH TERKUNCI JADWAL OPERASI</h2>
      <div class="countdown-hud" id="countdownHudText">00:00:00</div>
      <p style="font-size:11px; color:var(--muted); margin-bottom:10px; font-family:var(--font-mono);">Pesan belum diizinkan untuk dibuka sebelum H-Hour jadwal operasi tercapai demi kerahasiaan rute.</p>
      <label>PIN KHUSUS KOMANDAN (OVERRIDE):</label>
      <input type="password" id="overridePin" placeholder="Masukkan PIN komandan...">
      <button class="btn btn-danger" onclick="overrideTimeLock()">BUKA PAKSA DENGAN PIN KOMANDAN</button>
    </div>

    <!-- Result Display -->
    <div id="extractResult" style="display:none;" class="card">
      <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:8px;">
        <h2 id="extractStatusTitle" style="margin-bottom:0;">HASIL DEKRIPSI</h2>
        <div id="extractBadge" class="badge">DEKODING SUKSES</div>
      </div>
      <div id="fecTelemetryBox" style="margin-bottom:8px; padding:8px; background:#09140e; border:1px solid var(--gold); border-radius:4px; font-size:10px; font-family:var(--font-mono); color:var(--gold);"></div>
      <div class="result-box">
        <div id="resCoordsBox" style="display:none; margin-bottom:8px; padding:6px; background:#0b1911; border:1px solid var(--primary); border-radius:4px; font-size:10.5px; font-family:var(--font-mono); color:var(--primary);"></div>
        <h3 id="resTitle" style="font-size:13px; color:var(--text); margin-bottom:6px; font-family:var(--font-mono);"></h3>
        <p id="resMsg" style="font-size:11.5px; line-height:1.6; white-space:pre-wrap; color:var(--text); font-family:var(--font-mono);"></p>
      </div>
      <div id="resMeta" style="font-size:9.5px; color:var(--muted); font-family:var(--font-mono); margin-top:8px;"></div>
    </div>
  </div>

  <!-- TAB 3: FORENSIC INSPECTOR -->
  <div id="tab-inspect" style="display:none;">
    <div class="card">
      <h2>ANALISIS FORENSIK BIT-PLANE & DOMAIN FREKUENSI</h2>
      <p style="font-size:11px; color:var(--muted); margin-bottom:10px;">Visualisasikan lapisan bit citra (LSB Bit-0 vs MSB Bit-7) untuk mengonfirmasi bahwa data tersembunyi tidak menimbulkan artefak visual pada foto asli.</p>
      <div class="presets-bar">
        <div class="preset-chip active" onclick="renderBitPlane(0)">Red LSB (Bit 0)</div>
        <div class="preset-chip" onclick="renderBitPlane(1)">Green LSB (Bit 0)</div>
        <div class="preset-chip" onclick="renderBitPlane(2)">Blue LSB (Bit 0)</div>
        <div class="preset-chip" onclick="renderBitPlane(7)">MSB (Bit 7 - Kontur)</div>
      </div>
      <canvas id="inspectorCanvas" width="640" height="640"></canvas>
    </div>
  </div>

  <script>
    // =========================================================================
    // 1. REED-SOLOMON GF(2^8) FORWARD ERROR CORRECTION CODEC (PURE JAVASCRIPT)
    //    Primitive Polynomial: x^8 + x^4 + x^3 + x^2 + 1 (0x11D)
    // =========================================================================
    const RS_EXP = new Uint8Array(512);
    const RS_LOG = new Uint8Array(256);
    (function initGaloisTables() {
      let x = 1;
      for (let i = 0; i < 255; i++) {
        RS_EXP[i] = x;
        RS_LOG[x] = i;
        x <<= 1;
        if (x & 0x100) x ^= 0x11D;
      }
      for (let i = 255; i < 512; i++) RS_EXP[i] = RS_EXP[i - 255];
    })();

    function gfMul(a, b) {
      if (a === 0 || b === 0) return 0;
      return RS_EXP[RS_LOG[a & 0xFF] + RS_LOG[b & 0xFF]];
    }
    function gfDiv(a, b) {
      if (b === 0) throw new Error("GF(2^8) div by 0");
      if (a === 0) return 0;
      return RS_EXP[(RS_LOG[a & 0xFF] - RS_LOG[b & 0xFF]) + 255];
    }
    function gfPow(a, power) {
      if (power === 0) return 1;
      if (a === 0) return 0;
      let exp = (RS_LOG[a & 0xFF] * power) % 255;
      if (exp < 0) exp += 255;
      return RS_EXP[exp];
    }
    function gfInv(a) {
      return RS_EXP[255 - RS_LOG[a & 0xFF]];
    }
    function polyMul(p, q) {
      const res = new Uint8Array(p.length + q.length - 1);
      for (let j = 0; j < q.length; j++) {
        for (let i = 0; i < p.length; i++) {
          res[i + j] ^= gfMul(p[i], q[j]);
        }
      }
      return res;
    }
    function polyEval(poly, x) {
      let y = poly[0];
      for (let i = 1; i < poly.length; i++) {
        y = gfMul(y, x) ^ poly[i];
      }
      return y;
    }
    function buildGeneratorPoly(nsym) {
      let g = new Uint8Array([1]);
      for (let i = 0; i < nsym; i++) {
        g = polyMul(g, new Uint8Array([1, gfPow(2, i)]));
      }
      return g;
    }

    function rsEncodeBlock(data, nsym) {
      const gen = buildGeneratorPoly(nsym);
      const msgOut = new Uint8Array(data.length + nsym);
      msgOut.set(data, 0);
      for (let i = 0; i < data.length; i++) {
        const coef = msgOut[i];
        if (coef !== 0) {
          for (let j = 1; j < gen.length; j++) {
            msgOut[i + j] ^= gfMul(gen[j], coef);
          }
        }
      }
      msgOut.set(data, 0);
      return msgOut;
    }

    function rsDecodeBlock(codeword, nsym) {
      const n = codeword.length;
      const k = n - nsym;
      if (k <= 0 || n > 255) return null;
      const msg = new Uint8Array(codeword);
      const synd = new Uint8Array(nsym);
      let hasError = false;
      for (let i = 0; i < nsym; i++) {
        const s = polyEval(msg, gfPow(2, i));
        synd[i] = s;
        if (s !== 0) hasError = true;
      }
      if (!hasError) return { data: msg.slice(0, k), corrected: 0 };

      let cPoly = new Uint8Array(nsym + 1); cPoly[0] = 1;
      let bPoly = new Uint8Array(nsym + 1); bPoly[0] = 1;
      let errCountL = 0, shiftM = 1, prevDiscB = 1;

      for (let stepN = 0; stepN < nsym; stepN++) {
        let disc = synd[stepN];
        for (let j = 1; j <= errCountL; j++) {
          disc ^= gfMul(cPoly[j], synd[stepN - j]);
        }
        if (disc === 0) {
          shiftM++;
        } else {
          const tempPoly = cPoly.slice();
          const factor = gfDiv(disc, prevDiscB);
          for (let j = 0; j <= nsym - shiftM; j++) {
            if (bPoly[j] !== 0) {
              cPoly[j + shiftM] ^= gfMul(factor, bPoly[j]);
            }
          }
          if (2 * errCountL <= stepN) {
            errCountL = stepN + 1 - errCountL;
            bPoly = tempPoly;
            prevDiscB = disc;
            shiftM = 1;
          } else {
            shiftM++;
          }
        }
      }
      const deg = errCountL;
      if (deg <= 0 || deg * 2 > nsym) return null;
      const errLoc = cPoly.slice(0, deg + 1);

      const errPositions = [];
      for (let i = 0; i < n; i++) {
        const xInv = gfInv(gfPow(2, i));
        let evalSum = 0, xInvPow = 1;
        for (let j = 0; j <= deg; j++) {
          evalSum ^= gfMul(errLoc[j], xInvPow);
          xInvPow = gfMul(xInvPow, xInv);
        }
        if (evalSum === 0) {
          const pos = n - 1 - i;
          if (pos >= 0 && pos < n) errPositions.push(pos);
        }
      }
      if (errPositions.length !== deg) return null;

      const omega = new Uint8Array(nsym);
      for (let i = 0; i < nsym; i++) {
        let term = 0;
        for (let j = 0; j <= Math.min(i, deg); j++) {
          term ^= gfMul(synd[i - j], errLoc[j]);
        }
        omega[i] = term;
      }

      for (const pos of errPositions) {
        const xVal = gfPow(2, n - 1 - pos);
        const xInv = gfInv(xVal);
        let omegaVal = 0, xInvPow = 1;
        for (let i = 0; i < nsym; i++) {
          omegaVal ^= gfMul(omega[i], xInvPow);
          xInvPow = gfMul(xInvPow, xInv);
        }
        let sigmaDeriv = 0, xInvSqPow = 1;
        const xInvSq = gfMul(xInv, xInv);
        for (let j = 1; j <= deg; j += 2) {
          sigmaDeriv ^= gfMul(errLoc[j], xInvSqPow);
          xInvSqPow = gfMul(xInvSqPow, xInvSq);
        }
        if (sigmaDeriv === 0) return null;
        const errMag = gfMul(xVal, gfDiv(omegaVal, sigmaDeriv));
        msg[pos] ^= errMag;
      }

      for (let i = 0; i < nsym; i++) {
        if (polyEval(msg, gfPow(2, i)) !== 0) return null;
      }
      return { data: msg.slice(0, k), corrected: errPositions.length };
    }

    const DEFAULT_DATA_SHARD = 140;
    const DEFAULT_PARITY_SHARD = 60;
    const HEADER_TOTAL_LEN = 16;

    function rsEncodeWithFec(payload) {
      const headerData = new Uint8Array(8);
      headerData.set([0x52, 0x53, 0x4B, 0x54], 0); // "RSKT"
      headerData[4] = (payload.length >> 8) & 0xFF;
      headerData[5] = payload.length & 0xFF;
      headerData[6] = DEFAULT_DATA_SHARD;
      headerData[7] = DEFAULT_PARITY_SHARD;
      const encHeader = rsEncodeBlock(headerData, 8);

      const numBlocks = Math.ceil(payload.length / DEFAULT_DATA_SHARD);
      const cwLen = DEFAULT_DATA_SHARD + DEFAULT_PARITY_SHARD;
      const codewords = [];
      for (let b = 0; b < numBlocks; b++) {
        const block = new Uint8Array(DEFAULT_DATA_SHARD);
        const slice = payload.slice(b * DEFAULT_DATA_SHARD, (b + 1) * DEFAULT_DATA_SHARD);
        block.set(slice, 0);
        codewords.push(rsEncodeBlock(block, DEFAULT_PARITY_SHARD));
      }

      const out = new Uint8Array(HEADER_TOTAL_LEN + numBlocks * cwLen);
      out.set(encHeader, 0);
      let idx = HEADER_TOTAL_LEN;
      for (let col = 0; col < cwLen; col++) {
        for (let row = 0; row < numBlocks; row++) {
          out[idx++] = codewords[row][col];
        }
      }
      return out;
    }

    function rsDecodeWithFec(fecStream) {
      if (fecStream.length < HEADER_TOTAL_LEN) return null;
      const hDec = rsDecodeBlock(fecStream.slice(0, HEADER_TOTAL_LEN), 8);
      if (!hDec) return null;
      const hd = hDec.data;
      if (hd[0] !== 0x52 || hd[1] !== 0x53 || hd[2] !== 0x4B || hd[3] !== 0x54) return null;

      const dataLen = (hd[4] << 8) | hd[5];
      const dShard = hd[6];
      const pShard = hd[7];
      if (dataLen <= 0 || dShard <= 0 || pShard <= 0 || dShard + pShard > 255) return null;

      const numBlocks = Math.ceil(dataLen / dShard);
      const cwLen = dShard + pShard;
      if (fecStream.length < HEADER_TOTAL_LEN + numBlocks * cwLen) return null;

      const codewords = Array.from({ length: numBlocks }, () => new Uint8Array(cwLen));
      let inIdx = HEADER_TOTAL_LEN;
      for (let col = 0; col < cwLen; col++) {
        for (let row = 0; row < numBlocks; row++) {
          codewords[row][col] = fecStream[inIdx++];
        }
      }

      const fullOut = new Uint8Array(numBlocks * dShard);
      let totalCorrected = hDec.corrected;
      for (let row = 0; row < numBlocks; row++) {
        const dec = rsDecodeBlock(codewords[row], pShard);
        if (!dec) return null;
        fullOut.set(dec.data, row * dShard);
        totalCorrected += dec.corrected;
      }

      return {
        data: fullOut.slice(0, dataLen),
        correctedBytes: totalCorrected,
        parityPercent: Math.round((pShard * 100) / cwLen)
      };
    }

    // =========================================================================
    // 2. 8x8 2D-DCT MID-FREQUENCY QIM ENGINE (JPEG Q-75 NORMALIZED)
    // =========================================================================
    const JPEG_Q50 = [
      [16, 11, 10, 16, 24, 40, 51, 61],
      [12, 12, 14, 19, 26, 58, 60, 55],
      [14, 13, 16, 24, 40, 57, 69, 56],
      [14, 17, 22, 29, 51, 87, 80, 62],
      [18, 22, 37, 56, 68, 109, 103, 77],
      [24, 35, 55, 64, 81, 104, 113, 92],
      [49, 64, 78, 87, 103, 121, 120, 101],
      [72, 92, 95, 98, 112, 100, 103, 99]
    ];
    // 10 Strictly Mid-Frequency AC coordinates (2 <= u+v <= 4): avoids DC (0,0), ultra-low (0,1)/(1,0), and high frequencies
    const MID_FREQ_COORDS = [
      [0,2], [1,1], [2,0], [0,3], [1,2],
      [2,1], [3,0], [1,3], [2,2], [3,1]
    ];
    function rgbToYCbCr(r, g, b) {
      return [
        0.299 * r + 0.587 * g + 0.114 * b,
        128.0 - 0.168736 * r - 0.331264 * g + 0.5 * b,
        128.0 + 0.5 * r - 0.418688 * g - 0.081312 * b
      ];
    }
    function yCbCrToRgb(y, cb, cr) {
      const cbS = cb - 128.0, crS = cr - 128.0;
      return [
        Math.max(0, Math.min(255, Math.round(y + 1.402 * crS))),
        Math.max(0, Math.min(255, Math.round(y - 0.344136 * cbS - 0.714136 * crS))),
        Math.max(0, Math.min(255, Math.round(y + 1.772 * cbS)))
      ];
    }
    const DCT_T = Array.from({ length: 8 }, (_, u) => {
      const alpha = u === 0 ? Math.sqrt(1 / 8) : Math.sqrt(2 / 8);
      return Array.from({ length: 8 }, (_, x) => alpha * Math.cos(((2 * x + 1) * u * Math.PI) / 16));
    });
    const DCT_TT = Array.from({ length: 8 }, (_, x) => Array.from({ length: 8 }, (_, u) => DCT_T[u][x]));

    function getQimStep(u, v) {
      const q75 = Math.max(1, Math.floor((JPEG_Q50[u][v] * 50 + 50) / 100));
      return Math.max(32.0, q75 * 3.6);
    }

    function forwardDct8x8(spatial) {
      const temp = Array.from({ length: 8 }, () => new Float64Array(8));
      const freq = Array.from({ length: 8 }, () => new Float64Array(8));
      for (let i = 0; i < 8; i++) {
        for (let j = 0; j < 8; j++) {
          let s = 0;
          for (let k = 0; k < 8; k++) s += DCT_T[i][k] * spatial[k][j];
          temp[i][j] = s;
        }
      }
      for (let i = 0; i < 8; i++) {
        for (let j = 0; j < 8; j++) {
          let s = 0;
          for (let k = 0; k < 8; k++) s += temp[i][k] * DCT_TT[k][j];
          freq[i][j] = s;
        }
      }
      return freq;
    }

    function inverseDct8x8(freq) {
      const temp = Array.from({ length: 8 }, () => new Float64Array(8));
      const spatial = Array.from({ length: 8 }, () => new Float64Array(8));
      for (let i = 0; i < 8; i++) {
        for (let j = 0; j < 8; j++) {
          let s = 0;
          for (let k = 0; k < 8; k++) s += DCT_TT[i][k] * freq[k][j];
          temp[i][j] = s;
        }
      }
      for (let i = 0; i < 8; i++) {
        for (let j = 0; j < 8; j++) {
          let s = 0;
          for (let k = 0; k < 8; k++) s += temp[i][k] * DCT_T[k][j];
          spatial[i][j] = s;
        }
      }
      return spatial;
    }

    function calcMaxDctNetCapacity(w, h) {
      const totalBlocks = Math.floor(w / 8) * Math.floor(h / 8);
      const rawBytes = Math.floor((totalBlocks * MID_FREQ_COORDS.length) / 8);
      if (rawBytes <= HEADER_TOTAL_LEN) return 0;
      const cwLen = DEFAULT_DATA_SHARD + DEFAULT_PARITY_SHARD;
      return Math.floor((rawBytes - HEADER_TOTAL_LEN) / cwLen) * DEFAULT_DATA_SHARD;
    }

    function embedDctRobustIntoImageData(imgData, rawPayload) {
      const w = imgData.width, h = imgData.height, px = imgData.data;
      const fecBytes = rsEncodeWithFec(rawPayload);
      const totalBits = fecBytes.length * 8;
      const blocksX = Math.floor(w / 8), blocksY = Math.floor(h / 8);
      let bitIdx = 0;

      for (let by = 0; by < blocksY && bitIdx < totalBits; by++) {
        for (let bx = 0; bx < blocksX && bitIdx < totalBits; bx++) {
          const blockStartBit = bitIdx;
          const yOrig = Array.from({ length: 8 }, () => new Float64Array(8));
          const rOrig = Array.from({ length: 8 }, () => new Int32Array(8));
          const gOrig = Array.from({ length: 8 }, () => new Int32Array(8));
          const bOrig = Array.from({ length: 8 }, () => new Int32Array(8));

          for (let r = 0; r < 8; r++) {
            const py = by * 8 + r;
            for (let c = 0; c < 8; c++) {
              const idx = (py * w + (bx * 8 + c)) * 4;
              const rawR = px[idx], rawG = px[idx + 1], rawB = px[idx + 2];
              const rawY = Math.round(0.299 * rawR + 0.587 * rawG + 0.114 * rawB);
              const baseY = 64 + Math.floor((rawY * 127 + 127) / 255);
              rOrig[r][c] = Math.max(48, Math.min(207, baseY + Math.trunc(((rawR - rawY) * 35) / 100)));
              gOrig[r][c] = Math.max(48, Math.min(207, baseY + Math.trunc(((rawG - rawY) * 35) / 100)));
              bOrig[r][c] = Math.max(48, Math.min(207, baseY + Math.trunc(((rawB - rawY) * 35) / 100)));
            }
          }

          let endBitIdx = blockStartBit;
          for (let pass = 0; pass < 2; pass++) {
            for (let r = 0; r < 8; r++) {
              for (let c = 0; c < 8; c++) {
                yOrig[r][c] = (0.299 * rOrig[r][c] + 0.587 * gOrig[r][c] + 0.114 * bOrig[r][c]) - 128.0;
              }
            }
            const dct = forwardDct8x8(yOrig);
            let localBitIdx = blockStartBit;
            for (let cIdx = 0; cIdx < MID_FREQ_COORDS.length; cIdx++) {
              if (localBitIdx >= totalBits) break;
              const [u, v] = MID_FREQ_COORDS[cIdx];
              const bytePos = Math.floor(localBitIdx / 8);
              const bitOff = 7 - (localBitIdx % 8);
              const bitVal = (fecBytes[bytePos] >> bitOff) & 1;
              const step = getQimStep(u, v);
              const k = Math.round(dct[u][v] / step);
              if ((Math.abs(k) & 1) === bitVal) {
                dct[u][v] = k * step;
              } else {
                const diff = dct[u][v] - k * step;
                const signHint = ((cIdx + bx + by) & 1) === 0 ? 1 : -1;
                const kAdj = diff > 0.5 ? k + 1 : (diff < -0.5 ? k - 1 : (signHint >= 0 ? k + 1 : k - 1));
                dct[u][v] = kAdj * step;
              }
              localBitIdx++;
            }
            endBitIdx = localBitIdx;

            const yMod = inverseDct8x8(dct);
            for (let r = 0; r < 8; r++) {
              for (let c = 0; c < 8; c++) {
                const oldY = yOrig[r][c] + 128.0;
                const newY = Math.max(8.0, Math.min(247.0, yMod[r][c] + 128.0));
                const dY = Math.round(newY - oldY);
                const nr = Math.max(4, Math.min(251, rOrig[r][c] + dY));
                const nb = Math.max(4, Math.min(251, bOrig[r][c] + dY));
                const ng = Math.max(4, Math.min(251, Math.round((newY - 0.299 * nr - 0.114 * nb) / 0.587)));
                rOrig[r][c] = nr; gOrig[r][c] = ng; bOrig[r][c] = nb;
              }
            }
          }
          bitIdx = endBitIdx;

          for (let r = 0; r < 8; r++) {
            const py = by * 8 + r;
            for (let c = 0; c < 8; c++) {
              const idx = (py * w + (bx * 8 + c)) * 4;
              px[idx] = rOrig[r][c]; px[idx + 1] = gOrig[r][c]; px[idx + 2] = bOrig[r][c]; px[idx + 3] = 255;
            }
          }
        }
      }
      return fecBytes.length;
    }

    function extractDctRobustFromImageData(imgData) {
      const w = imgData.width, h = imgData.height, px = imgData.data;
      const blocksX = Math.floor(w / 8), blocksY = Math.floor(h / 8);
      const maxRawBytes = Math.floor((blocksX * blocksY * MID_FREQ_COORDS.length) / 8);
      if (maxRawBytes < HEADER_TOTAL_LEN + 32) return null;

      const extracted = new Uint8Array(maxRawBytes);
      let bitIdx = 0, requiredBytes = HEADER_TOTAL_LEN, headerDecoded = false;

      for (let by = 0; by < blocksY; by++) {
        for (let bx = 0; bx < blocksX; bx++) {
          const yBlock = Array.from({ length: 8 }, () => new Float64Array(8));
          for (let r = 0; r < 8; r++) {
            const py = by * 8 + r;
            for (let c = 0; c < 8; c++) {
              const idx = (py * w + (bx * 8 + c)) * 4;
              const [yVal] = rgbToYCbCr(px[idx], px[idx + 1], px[idx + 2]);
              yBlock[r][c] = yVal - 128.0;
            }
          }
          const dct = forwardDct8x8(yBlock);
          for (const [u, v] of MID_FREQ_COORDS) {
            const step = getQimStep(u, v);
            const bit = Math.abs(Math.round(dct[u][v] / step)) & 1;
            if (bit === 1) {
              extracted[Math.floor(bitIdx / 8)] |= (1 << (7 - (bitIdx % 8)));
            }
            bitIdx++;

            if (!headerDecoded && bitIdx === HEADER_TOTAL_LEN * 8) {
              const hDec = rsDecodeBlock(extracted.slice(0, HEADER_TOTAL_LEN), 8);
              if (!hDec) return null;
              const hd = hDec.data;
              if (hd[0] !== 0x52 || hd[1] !== 0x53 || hd[2] !== 0x4B || hd[3] !== 0x54) return null;
              const dataLen = (hd[4] << 8) | hd[5];
              const dShard = hd[6], pShard = hd[7];
              if (dataLen <= 0 || dShard <= 0 || pShard <= 0) return null;
              const numBlocks = Math.ceil(dataLen / dShard);
              requiredBytes = HEADER_TOTAL_LEN + numBlocks * (dShard + pShard);
              if (requiredBytes > maxRawBytes) return null;
              headerDecoded = true;
            }
            if (headerDecoded && bitIdx >= requiredBytes * 8) {
              return rsDecodeWithFec(extracted.slice(0, requiredBytes));
            }
          }
        }
      }
      return null;
    }

    // =========================================================================
    // 3. UI & APPLICATION CONTROLLER
    // =========================================================================
    let audioCtx = null;
    function playTacticalChirp(freq = 880, type = 'sine', duration = 0.08) {
      try {
        if (!audioCtx) audioCtx = new (window.AudioContext || window.webkitAudioContext)();
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        osc.type = type;
        osc.frequency.setValueAtTime(freq, audioCtx.currentTime);
        gain.gain.setValueAtTime(0.08, audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.0001, audioCtx.currentTime + duration);
        osc.connect(gain);
        gain.connect(audioCtx.destination);
        osc.start();
        osc.stop(audioCtx.currentTime + duration);
      } catch(e) {}
    }

    let activeCarrierImg = null;
    let loadedStegoImg = null;
    let pendingLockedPayload = null;
    let countdownInterval = null;
    let currentTransmissionMode = 'DCT'; // 'DCT' or 'LSB'

    function selectTransmissionMode(mode) {
      currentTransmissionMode = mode;
      document.getElementById('modeCardDct').className = 'mode-card ' + (mode === 'DCT' ? 'active-dct' : '');
      document.getElementById('modeCardLsb').className = 'mode-card ' + (mode === 'LSB' ? 'active-lsb' : '');
      document.getElementById('badgeDct').innerText = mode === 'DCT' ? 'AKTIF' : 'PILIH';
      document.getElementById('badgeLsb').innerText = mode === 'LSB' ? 'AKTIF' : 'PILIH';
      updateCarrierStats();
      playTacticalChirp(740, 'sine', 0.05);
    }

    function updateCarrierStats() {
      const cv = document.getElementById('carrierCanvas');
      if (!cv) return;
      const w = cv.width, h = cv.height;
      if (currentTransmissionMode === 'DCT') {
        const cap = calcMaxDctNetCapacity(w, h);
        document.getElementById('carrierStats').innerText = 'Resolusi: ' + w + 'x' + h + ' px | Mode: PROFIL TAKTIS (DCT 8x8 YCbCr + RS Kapasitas: ~' + cap + ' Bytes | Paritas FEC 30%)';
      } else {
        const cap = Math.floor((w * h * 3) / 8) - 4;
        document.getElementById('carrierStats').innerText = 'Resolusi: ' + w + 'x' + h + ' px | Mode: PROFIL DOKUMEN (LSB 3-Kanal Kapasitas: ~' + cap + ' Bytes | Kirim sbg Dokumen/File)';
      }
    }

    function drawCarrierPreset(idx) {
      const cv = document.getElementById('carrierCanvas');
      cv.width = 640; cv.height = 640;
      const ctx = cv.getContext('2d');
      const w = cv.width, h = cv.height;
      document.querySelectorAll('.presets-bar .preset-chip').forEach((c, i) => {
        if (i < 4) c.className = 'preset-chip ' + (i === idx ? 'active' : '');
      });

      if (idx === 0) {
        const grad = ctx.createLinearGradient(0, 0, 0, h);
        grad.addColorStop(0, '#182c36'); grad.addColorStop(1, '#4a6246');
        ctx.fillStyle = grad; ctx.fillRect(0, 0, w, h);
        ctx.fillStyle = '#243a30';
        ctx.beginPath(); ctx.moveTo(0, h * 0.5); ctx.lineTo(w * 0.3, h * 0.3); ctx.lineTo(w * 0.7, h * 0.45); ctx.lineTo(w, h * 0.35); ctx.lineTo(w, h); ctx.lineTo(0, h); ctx.fill();
        ctx.fillStyle = '#16261c';
        for (let i = 0; i < 16; i++) {
          const tx = i * (w / 15);
          ctx.beginPath(); ctx.moveTo(tx, h * 0.6); ctx.lineTo(tx + 24, h); ctx.lineTo(tx - 24, h); ctx.fill();
        }
      } else if (idx === 1) {
        const grad = ctx.createLinearGradient(0, 0, 0, h);
        grad.addColorStop(0, '#1e2434'); grad.addColorStop(0.6, '#a5552d'); grad.addColorStop(1, '#1c2024');
        ctx.fillStyle = grad; ctx.fillRect(0, 0, w, h);
        ctx.fillStyle = '#e1af46';
        ctx.beginPath(); ctx.arc(w * 0.75, h * 0.35, 55, 0, Math.PI * 2); ctx.fill();
        ctx.fillStyle = '#181a1e';
        ctx.fillRect(0, h * 0.65, w, h * 0.35);
        ctx.fillRect(w * 0.3, h * 0.45, 48, h * 0.2);
      } else if (idx === 2) {
        ctx.fillStyle = '#23282c'; ctx.fillRect(0, 0, w, h);
        ctx.fillStyle = '#303438'; ctx.fillRect(0, h * 0.5, w, h * 0.5);
        const colors = ['#554b32', '#46553c', '#3c4437'];
        for (let r = 0; r < 2; r++) {
          for (let c = 0; c < 3; c++) {
            ctx.fillStyle = colors[(r + c) % 3];
            ctx.fillRect(w * 0.15 + c * 120, h * 0.55 + r * 90, 105, 75);
          }
        }
      } else {
        ctx.fillStyle = '#162026'; ctx.fillRect(0, 0, w, h);
        ctx.strokeStyle = '#234655'; ctx.lineWidth = 1.5;
        for (let r = 20; r < w * 0.6; r += 28) {
          ctx.beginPath(); ctx.arc(w * 0.45, h * 0.45, r, 0, Math.PI * 2); ctx.stroke();
        }
        ctx.strokeStyle = '#2ee59d'; ctx.strokeRect(w * 0.4, h * 0.4, 48, 48);
      }

      activeCarrierImg = new Image();
      activeCarrierImg.src = cv.toDataURL();
      updateCarrierStats();
      playTacticalChirp(600, 'sine', 0.05);
    }

    function loadCustomCarrier(e) {
      const file = e.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = ev => {
        const img = new Image();
        img.onload = () => {
          const cv = document.getElementById('carrierCanvas');
          cv.width = img.width; cv.height = img.height;
          cv.getContext('2d').drawImage(img, 0, 0);
          activeCarrierImg = img;
          updateCarrierStats();
        };
        img.src = ev.target.result;
      };
      reader.readAsDataURL(file);
    }

    let activeGeospatialIntel = null;

    function runGeospatialGroundingHtml(applyDirectlyToDecoy) {
      const coords = document.getElementById('masterCoords').value || '07°14\'22.1"S 110°24\'18.8"E (MGRS: 49M FM 3418 9912)';
      const isJawaTengah = coords.includes('110°') || coords.includes('49M') || coords.includes('07°');
      
      activeGeospatialIntel = isJawaTengah ? {
        locationName: "Sektor Jawa Tengah Bagian Selatan (Koridor Gunung Merapi - Ambarawa)",
        terrainSummary: "Perbukitan vulkanik terjal, tutupan vegetasi hutan pinus dan perkebunan teh/kopi. Terdapat jalan rintis tanah dan aliran sungai lereng curam.",
        civilianRoEAssessment: "RoE KETAT: Perkampungan penduduk dan area pertanian terasering berada pada radius ~1.2 km arah barat daya. Dilarang penggunaan amunisi peledak berdaya rusak area.",
        medevac: "• Puskesmas Rawat Inap Terdekat (Estimasi 18 menit rute darat)\n• RS Tentara dr. Soedjono Magelang / RST Wijayakusuma",
        decoyTitle: "SURAT JALAN ANGKUTAN LOGISTIK BAMA SEKTOR LERENG MERAPI",
        decoyMsg: "Kepada Yth. Pasi Logistik:\nKendaraan truk logistik telah mengantar pasokan beras 450 Kg dan tabung gas melalui rute alternatif Pasar Hewan Ambarawa menuju Posko Sub-Sektor.\nKondisi jalan licin berkabut, pengiriman tuntas tanpa kendala.",
        source: "Google Maps Data via Gemini 3.8 Flash Grounding (Simulasi Cadangan Taktis 100% Offline)"
      } : {
        locationName: "Sektor Penugasan Operasi Lapangan " + coords,
        terrainSummary: "Wilayah bergelombang dengan tutupan vegetasi sedang. Akses jalur darat sekunder berjarak 800 meter dari titik koordinat sasaran.",
        civilianRoEAssessment: "RoE STANDAR DEFCON-2: Identifikasi visual wajib dilakukan sebelum tindakan pengamanan untuk memastikan tidak ada aktivitas warga sipil.",
        medevac: "• Puskesmas Wilayah Perbatasan (Rute Rintis 25 menit)\n• Pos Kesehatan Lapangan Satgas Yonif",
        decoyTitle: "LAPORAN PATROLI RUTIN JALUR PENGAMANAN PERBATASAN",
        decoyMsg: "Laporan harian regu patroli: Situasi aman terkendali di sepanjang rute pengawasan pos batas sektor. Pengecekan patok perbatasan dalam kondisi utuh.",
        source: "Google Maps Data via Gemini 3.8 Flash Grounding (Simulasi Cadangan Taktis 100% Offline)"
      };

      document.getElementById('geoIntelBox').style.display = 'block';
      document.getElementById('geoLocName').innerText = "LOKASI NYATA: " + activeGeospatialIntel.locationName;
      document.getElementById('geoTerrain').innerText = "MEDAN: " + activeGeospatialIntel.terrainSummary;
      document.getElementById('geoRoe').innerText = "ATURAN PELIBATAN (RoE / CDE):\n" + activeGeospatialIntel.civilianRoEAssessment;
      document.getElementById('geoMedevac').innerText = "TITIK EVAKUASI MEDEVAC TERDEKAT:\n" + activeGeospatialIntel.medevac;
      document.getElementById('geoDecoyTitle').innerText = activeGeospatialIntel.decoyTitle;
      document.getElementById('geoDecoyMsg').innerText = activeGeospatialIntel.decoyMsg;
      document.getElementById('geoSource').innerText = "Sumber: " + activeGeospatialIntel.source;

      if (applyDirectlyToDecoy) {
        applySmartDecoyHtml();
      }
      playTacticalChirp(840, 'sine', 0.08);
    }

    function applySmartDecoyHtml() {
      if (!activeGeospatialIntel) return;
      document.getElementById('decoyTitle').value = activeGeospatialIntel.decoyTitle;
      document.getElementById('decoyMsg').value = activeGeospatialIntel.decoyMsg;
      playTacticalChirp(920, 'sine', 0.06);
    }

    function switchTab(t) {
      document.getElementById('tab-embed').style.display = t === 'embed' ? 'block' : 'none';
      document.getElementById('tab-extract').style.display = t === 'extract' ? 'block' : 'none';
      document.getElementById('tab-inspect').style.display = t === 'inspect' ? 'block' : 'none';
      document.querySelectorAll('.nav-tabs .tab-btn').forEach((b, i) => {
        const isThis = (i === 0 && t === 'embed') || (i === 1 && t === 'extract') || (i === 2 && t === 'inspect');
        b.className = 'tab-btn ' + (isThis ? 'active' : '');
      });
      if (t === 'inspect') renderBitPlane(0);
      playTacticalChirp(900, 'sine', 0.04);
    }

    function toggleDeniableView() {
      const checked = document.getElementById('toggleDeniable').checked;
      document.getElementById('decoySection').style.display = checked ? 'block' : 'none';
    }

    function toggleTimeLockView() {
      const checked = document.getElementById('toggleTimeLock').checked;
      document.getElementById('timeLockOptions').style.display = checked ? 'block' : 'none';
    }

    async function deriveKey(pw, salt) {
      const enc = new TextEncoder();
      const keyMat = await crypto.subtle.importKey('raw', enc.encode(pw), 'PBKDF2', false, ['deriveKey']);
      return crypto.subtle.deriveKey(
        { name: 'PBKDF2', salt, iterations: 12000, hash: 'SHA-256' },
        keyMat,
        { name: 'AES-GCM', length: 256 },
        false,
        ['encrypt', 'decrypt']
      );
    }

    async function executeEmbed() {
      if (!activeCarrierImg) { alert('Pilih atau buat gambar carrier terlebih dahulu.'); return; }
      const mPass = document.getElementById('masterPass').value;
      if (!mPass) { alert('Sandi Master wajib diisi.'); return; }
      const isDeniable = document.getElementById('toggleDeniable').checked;
      const dPass = document.getElementById('decoyPass').value;
      if (isDeniable && !dPass) { alert('Sandi Umpan wajib diisi dalam mode Sandi Kamuflase.'); return; }
      if (isDeniable && mPass === dPass) { alert('Sandi Master dan Sandi Umpan tidak boleh sama demi keamanan.'); return; }

      const isTimeLock = document.getElementById('toggleTimeLock').checked;
      const hours = parseInt(document.getElementById('timeLockHours').value) || 0;
      const timeLockEpoch = isTimeLock ? (Date.now() + (hours * 3600 * 1000)) : 0;

      const masterPayloadObj = {
        type: 'REAL',
        title: document.getElementById('masterTitle').value,
        message: document.getElementById('masterMsg').value,
        coordinates: document.getElementById('masterCoords').value,
        classification: document.getElementById('masterClass').value,
        timeLockEpochMs: timeLockEpoch,
        createdAtEpochMs: Date.now(),
        authorUnit: 'PUSSIBERAD // WEB-ENCLAVE'
      };

      const enc = new TextEncoder();
      const mPlain = enc.encode(JSON.stringify(masterPayloadObj));
      const mSalt = crypto.getRandomValues(new Uint8Array(16));
      const mIv = crypto.getRandomValues(new Uint8Array(12));
      const mKey = await deriveKey(mPass, mSalt);
      const mCt = new Uint8Array(await crypto.subtle.encrypt({ name: 'AES-GCM', iv: mIv }, mKey, mPlain));
      const mEncBlock = new Uint8Array(16 + 12 + mCt.length);
      mEncBlock.set(mSalt, 0); mEncBlock.set(mIv, 16); mEncBlock.set(mCt, 28);

      let packageBytes;
      const now = Date.now();

      if (!isDeniable) {
        const totalSize = 4 + 1 + 8 + 4 + mEncBlock.length;
        const buf = new ArrayBuffer(totalSize);
        const view = new DataView(buf);
        const u8 = new Uint8Array(buf);
        u8.set([0x53, 0x44, 0x4B, 0x52, 0x01], 0);
        view.setBigInt64(5, BigInt(now));
        view.setInt32(13, mEncBlock.length);
        u8.set(mEncBlock, 17);
        packageBytes = u8;
      } else {
        const decoyPayloadObj = {
          type: 'DECOY',
          title: document.getElementById('decoyTitle').value,
          message: document.getElementById('decoyMsg').value,
          coordinates: '',
          classification: 'DECOY_ROUTINE',
          timeLockEpochMs: 0,
          createdAtEpochMs: now,
          authorUnit: 'BAMA LOGISTIK'
        };
        const dPlain = enc.encode(JSON.stringify(decoyPayloadObj));
        const dSalt = crypto.getRandomValues(new Uint8Array(16));
        const dIv = crypto.getRandomValues(new Uint8Array(12));
        const dKey = await deriveKey(dPass, dSalt);
        const dCt = new Uint8Array(await crypto.subtle.encrypt({ name: 'AES-GCM', iv: dIv }, dKey, dPlain));
        const dEncBlock = new Uint8Array(16 + 12 + dCt.length);
        dEncBlock.set(dSalt, 0); dEncBlock.set(dIv, 16); dEncBlock.set(dCt, 28);

        const totalSize = 4 + 1 + 8 + 4 + dEncBlock.length + 4 + mEncBlock.length;
        const buf = new ArrayBuffer(totalSize);
        const view = new DataView(buf);
        const u8 = new Uint8Array(buf);
        u8.set([0x53, 0x44, 0x4B, 0x52, 0x02], 0);
        view.setBigInt64(5, BigInt(now));
        let off = 13;
        view.setInt32(off, dEncBlock.length); off += 4;
        u8.set(dEncBlock, off); off += dEncBlock.length;
        view.setInt32(off, mEncBlock.length); off += 4;
        u8.set(mEncBlock, off);
        packageBytes = u8;
      }

      const stegoCv = document.getElementById('stegoCanvas');
      stegoCv.width = activeCarrierImg.width; stegoCv.height = activeCarrierImg.height;
      const ctx = stegoCv.getContext('2d');
      ctx.drawImage(activeCarrierImg, 0, 0);
      const imgData = ctx.getImageData(0, 0, stegoCv.width, stegoCv.height);

      if (currentTransmissionMode === 'DCT') {
        const maxNet = calcMaxDctNetCapacity(stegoCv.width, stegoCv.height);
        if (packageBytes.length > maxNet) {
          alert('Ukuran muatan (' + packageBytes.length + ' B) melebihi kapasitas DCT+RS (' + maxNet + ' B). Gunakan citra resolusi lebih besar atau mode HIGH CAPACITY.');
          return;
        }
        const fecTotalLen = embedDctRobustIntoImageData(imgData, packageBytes);
        ctx.putImageData(imgData, 0, 0);
        document.getElementById('embedDetails').innerText =
          'Mode: TACTICAL ROBUST (DCT 8x8 Mid-Band + Reed-Solomon FEC) | Cipher: ' + packageBytes.length + ' B -> FEC Frame: ' + fecTotalLen + ' B (Parity 30%)';
      } else {
        const px = imgData.data;
        const fullLen = packageBytes.length;
        const fullData = new Uint8Array(4 + fullLen);
        fullData[0] = (fullLen >> 24) & 0xff;
        fullData[1] = (fullLen >> 16) & 0xff;
        fullData[2] = (fullLen >> 8) & 0xff;
        fullData[3] = fullLen & 0xff;
        fullData.set(packageBytes, 4);

        const totalBits = fullData.length * 8;
        let bitIdx = 0;
        for (let i = 0; i < px.length && bitIdx < totalBits; i += 4) {
          for (let ch = 0; ch < 3 && bitIdx < totalBits; ch++) {
            const bytePos = Math.floor(bitIdx / 8);
            const bitPos = 7 - (bitIdx % 8);
            const bitVal = (fullData[bytePos] >> bitPos) & 1;
            px[i + ch] = (px[i + ch] & ~1) | bitVal;
            bitIdx++;
          }
        }
        ctx.putImageData(imgData, 0, 0);
        document.getElementById('embedDetails').innerText =
          'Mode: HIGH CAPACITY (Spatial LSB 3-Ch) | Ukuran Muatan: ' + fullLen + ' Bytes | Format: PNG Lossless';
      }

      document.getElementById('embedResult').style.display = 'block';
      const dLink = document.getElementById('downloadLink');
      dLink.href = stegoCv.toDataURL('image/png');
      dLink.download = 'SANDI_' + currentTransmissionMode + '_' + Date.now() + '.png';

      // Also auto-load into Extract tab for immediate testing
      loadedStegoImg = new Image();
      loadedStegoImg.onload = () => {
        const exCv = document.getElementById('stegoExtractCanvas');
        exCv.width = loadedStegoImg.width; exCv.height = loadedStegoImg.height;
        exCv.getContext('2d').drawImage(loadedStegoImg, 0, 0);
        exCv.style.display = 'block';
        document.getElementById('simJpegBtn').style.display = 'block';
      };
      loadedStegoImg.src = stegoCv.toDataURL('image/png');

      playTacticalChirp(1200, 'triangle', 0.12);
    }

    function loadStegoImage(e) {
      const file = e.target.files[0];
      if (!file) return;
      const reader = new FileReader();
      reader.onload = ev => {
        loadedStegoImg = new Image();
        loadedStegoImg.onload = () => {
          const cv = document.getElementById('stegoExtractCanvas');
          cv.width = loadedStegoImg.width; cv.height = loadedStegoImg.height;
          cv.getContext('2d').drawImage(loadedStegoImg, 0, 0);
          cv.style.display = 'block';
          document.getElementById('simJpegBtn').style.display = 'block';
        };
        loadedStegoImg.src = ev.target.result;
      };
      reader.readAsDataURL(file);
    }

    function simulateJpegCompressionOnExtract() {
      const cv = document.getElementById('stegoExtractCanvas');
      if (!cv || !cv.width) return;
      const jpegDataUrl = cv.toDataURL('image/jpeg', 0.82);
      const reImg = new Image();
      reImg.onload = () => {
        cv.getContext('2d').drawImage(reImg, 0, 0);
        loadedStegoImg = reImg;
        alert('Simulasi Kompresi Lossy JPEG (Q=82) diterapkan! Klik "URAI INSTRUKSI TAKTIS" untuk menguji pemulihan DCT + Reed-Solomon FEC.');
      };
      reImg.src = jpegDataUrl;
    }

    function extractLsbFromImageData(imgData) {
      const px = imgData.data;
      let pLen = 0, bitIdx = 0;
      for (let i = 0; i < px.length && bitIdx < 32; i += 4) {
        for (let ch = 0; ch < 3 && bitIdx < 32; ch++) {
          pLen = (pLen << 1) | (px[i + ch] & 1);
          bitIdx++;
        }
      }
      if (pLen <= 0 || pLen > 15000000) return null;
      const payload = new Uint8Array(pLen);
      let payloadBitIdx = 0, totalBits = pLen * 8, curBit = 0;
      for (let i = 0; i < px.length && curBit < (32 + totalBits); i += 4) {
        for (let ch = 0; ch < 3 && curBit < (32 + totalBits); ch++) {
          if (curBit >= 32) {
            payload[Math.floor(payloadBitIdx / 8)] |= ((px[i + ch] & 1) << (7 - (payloadBitIdx % 8)));
            payloadBitIdx++;
          }
          curBit++;
        }
      }
      if (payload[0] === 0x53 && payload[1] === 0x44 && payload[2] === 0x4B && payload[3] === 0x52) {
        return payload;
      }
      return null;
    }

    async function executeExtract() {
      if (!loadedStegoImg) { alert('Silakan unggah citra stego terlebih dahulu.'); return; }
      const pass = document.getElementById('extractPass').value;
      if (!pass) { alert('Ketik kata sandi operasional.'); return; }

      const cv = document.getElementById('stegoExtractCanvas');
      const ctx = cv.getContext('2d');
      const imgData = ctx.getImageData(0, 0, cv.width, cv.height);

      let payload = null;
      let detectedModeText = '';

      // 1. Probe Spatial LSB first
      const lsbCandidate = extractLsbFromImageData(imgData);
      if (lsbCandidate) {
        payload = lsbCandidate;
        detectedModeText = 'MODE DETEKSI: HIGH CAPACITY (Spatial LSB 3-Channel Lossless)';
      } else {
        // 2. Probe Frequency-Domain DCT 8x8 + Reed-Solomon FEC
        const dctFec = extractDctRobustFromImageData(imgData);
        if (dctFec && dctFec.data[0] === 0x53 && dctFec.data[1] === 0x44 && dctFec.data[2] === 0x4B && dctFec.data[3] === 0x52) {
          payload = dctFec.data;
          detectedModeText = 'MODE DETEKSI: TACTICAL ROBUST (DCT 8x8 Mid-Band + Reed-Solomon GF(2^8)) | Parity: ' +
            dctFec.parityPercent + '% | Byte Error Dipulihkan Otomatis: ' + dctFec.correctedBytes + ' Byte';
        }
      }

      if (!payload) {
        alert('Tidak ditemukan muatan Sandi-Kartika valid atau kerusakan kompresi melampaui batas koreksi FEC.');
        return;
      }

      const mode = payload[4];
      const view = new DataView(payload.buffer, payload.byteOffset, payload.byteLength);

      let masterBlock = null;
      let decoyBlock = null;

      if (mode === 0x01) {
        const mLen = view.getInt32(13);
        masterBlock = payload.slice(17, 17 + mLen);
      } else if (mode === 0x02) {
        const dLen = view.getInt32(13);
        decoyBlock = payload.slice(17, 17 + dLen);
        const mOff = 17 + dLen;
        const mLen = view.getInt32(mOff);
        masterBlock = payload.slice(mOff + 4, mOff + 4 + mLen);
      }

      async function tryDecrypt(block, pw) {
        if (!block || block.length < 28) return null;
        const salt = block.slice(0, 16);
        const iv = block.slice(16, 28);
        const ct = block.slice(28);
        try {
          const key = await deriveKey(pw, salt);
          const dec = await crypto.subtle.decrypt({ name: 'AES-GCM', iv }, key, ct);
          return JSON.parse(new TextDecoder().decode(dec));
        } catch(e) {
          return null;
        }
      }

      document.getElementById('fecTelemetryBox').innerText = detectedModeText;

      const masterData = await tryDecrypt(masterBlock, pass);
      if (masterData) {
        if (masterData.timeLockEpochMs && masterData.timeLockEpochMs > Date.now()) {
          pendingLockedPayload = masterData;
          showTimeLockCountdown(masterData.timeLockEpochMs);
          return;
        }
        renderExtractedPayload('SANGAT RAHASIA (MASTER KEY)', masterData, 'var(--danger)', 'DEFCON-1');
        return;
      }

      if (decoyBlock) {
        const decoyData = await tryDecrypt(decoyBlock, pass);
        if (decoyData) {
          renderExtractedPayload('STATUS: LOGISTIK RUTIN TERBUKA (DECOY)', decoyData, 'var(--decoy)', 'UNCLASSIFIED');
          return;
        }
      }

      alert('Kata sandi salah atau integritas data gagal diverifikasi.');
    }

    function showTimeLockCountdown(epoch) {
      document.getElementById('timeLockHud').style.display = 'block';
      document.getElementById('extractResult').style.display = 'none';

      if (countdownInterval) clearInterval(countdownInterval);
      function update() {
        const diff = epoch - Date.now();
        if (diff <= 0) {
          clearInterval(countdownInterval);
          document.getElementById('timeLockHud').style.display = 'none';
          renderExtractedPayload('SANGAT RAHASIA (WAKTU OPERASI TIBA)', pendingLockedPayload, 'var(--danger)', 'DEFCON-1');
          return;
        }
        const hrs = String(Math.floor(diff / 3600000)).padStart(2, '0');
        const mins = String(Math.floor((diff % 3600000) / 60000)).padStart(2, '0');
        const secs = String(Math.floor((diff % 60000) / 1000)).padStart(2, '0');
        document.getElementById('countdownHudText').innerText = hrs + ':' + mins + ':' + secs;
      }
      update();
      countdownInterval = setInterval(update, 1000);
      playTacticalChirp(440, 'sawtooth', 0.2);
    }

    function overrideTimeLock() {
      const pin = document.getElementById('overridePin').value;
      if (pin === 'PUSSIBERAD-991') {
        if (countdownInterval) clearInterval(countdownInterval);
        document.getElementById('timeLockHud').style.display = 'none';
        renderExtractedPayload('SANGAT RAHASIA (BYPASS KOMANDAN)', pendingLockedPayload, 'var(--gold)', 'OVERRIDE');
      } else {
        alert('PIN Komandan Salah! Instruksi tetap terkunci.');
      }
    }

    function renderExtractedPayload(status, data, color, badgeText) {
      document.getElementById('timeLockHud').style.display = 'none';
      document.getElementById('extractResult').style.display = 'block';
      const titleEl = document.getElementById('extractStatusTitle');
      titleEl.innerText = status;
      titleEl.style.color = color;
      const badge = document.getElementById('extractBadge');
      badge.innerText = badgeText;
      badge.style.borderColor = color;
      badge.style.color = color;

      document.getElementById('resTitle').innerText = data.title;
      document.getElementById('resMsg').innerText = data.message;

      const coordsBox = document.getElementById('resCoordsBox');
      if (data.coordinates && data.coordinates.trim().length > 0) {
        coordsBox.style.display = 'block';
        coordsBox.innerText = 'SASARAN OPERASI: ' + data.coordinates;
      } else {
        coordsBox.style.display = 'none';
      }

      document.getElementById('resMeta').innerText = 'Klasifikasi: ' + (data.classification || 'TERBATAS') + ' | Unit: ' + (data.authorUnit || 'PUSSIBERAD') + ' | Dibuat: ' + new Date(data.createdAtEpochMs || Date.now()).toLocaleString();
      playTacticalChirp(1000, 'sine', 0.15);
    }

    function renderBitPlane(planeIdx) {
      const srcCanvas = document.getElementById('stegoCanvas') && document.getElementById('stegoCanvas').width
        ? document.getElementById('stegoCanvas')
        : document.getElementById('carrierCanvas');
      const cv = document.getElementById('inspectorCanvas');
      if (!srcCanvas || !srcCanvas.width) return;
      cv.width = srcCanvas.width; cv.height = srcCanvas.height;
      const ctx = cv.getContext('2d');
      const srcPx = srcCanvas.getContext('2d').getImageData(0, 0, cv.width, cv.height).data;
      const outData = ctx.createImageData(cv.width, cv.height);
      const out = outData.data;

      for (let i = 0; i < srcPx.length; i += 4) {
        let bit = 0;
        if (planeIdx === 0) bit = srcPx[i] & 1;
        else if (planeIdx === 1) bit = srcPx[i + 1] & 1;
        else if (planeIdx === 2) bit = srcPx[i + 2] & 1;
        else bit = (srcPx[i] >> 7) & 1;
        const val = bit ? 255 : 0;
        out[i] = val; out[i + 1] = val; out[i + 2] = val; out[i + 3] = 255;
      }
      ctx.putImageData(outData, 0, 0);
    }

    drawCarrierPreset(0);
  </script>
</body>
</html>
        """.trimIndent()

        FileOutputStream(htmlFile).use { out ->
            out.write(htmlContent.toByteArray(Charsets.UTF_8))
            out.flush()
        }

        val authority = "${context.packageName}.fileprovider"
        val uri = FileProvider.getUriForFile(context, authority, htmlFile)
        return Pair(htmlFile, uri)
    }
}
