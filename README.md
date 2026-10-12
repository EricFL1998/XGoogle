# XGoogle

**Xiaomi HyperOS 4 Çin ROM'una Google özellikleri getiren LSPosed modülü.**
[zopulus/Coogle](https://github.com/zopulus/coogle) tabanlıdır; ColorOS yerine Xiaomi HyperOS 4 için uyarlanmıştır.

[English below](#english)

## Özellikler

- ✅ **Güç tuşuna basılı tut → Gemini / Google Asistan**
- ✅ **Alt çubuğa basılı tut → Circle to Search**
- ✅ **"Hey Google"** sesli uyandırma
- ✅ **Canlı çeviri:** Circle to Search'te Çevir'e basıp kaydırınca çeviri devam eder
- ✅ **Google Lens**
- ✅ **Android Auto:** sistem uygulaması ve araç projeksiyonu rolü
- ✅ **GMS Çin kısıtlamasının kaldırılması** (Quick Share dahil)
- 🧪 Kilit ekranı kısayolları: Asistan, Lens/QR, Cüzdan, Ev kontrolleri
- 🧪 Kontrol merkezi Lens/QR kutucuğu
- 🧪 Güç tuşuna çift bas → Google Cüzdan
- 🌐 Arayüz dilleri: Türkçe, English, Русский, Español, 简体中文

## Gereksinimler

- Xiaomi HyperOS 4 Çin ROM (Android 17). Xiaomi 15 Ultra'da test edildi.
- Root: KernelSU / SukiSU (Magisk ve APatch desteği eklendi, test edilmedi)
- LSPosed (API 102)
- Google uygulaması (Play Store)

## Kurulum

1. [Releases](https://github.com/Leekin2/XGoogle/releases) sayfasından **XGoogle APK**'sını indirip kurun.
2. **LSPosed → Modüller → XGoogle**'ı etkinleştirin, önerilen kapsamların hepsini seçin:
   Sistem Çerçevesi, Sistem Arayüzü, Ayarlar, Google, XiaoAi (`com.miui.voiceassist`), Her Zaman Açık Ekran (`com.miui.aod`).
3. Root yöneticinizde XGoogle'a **root izni** verin.
4. XGoogle'ı açıp **Hey Google**'ı açın; ek modül otomatik kurulur. (Ya da Releases'teki ek modül ZIP'ini root yöneticinizden kurun.)
5. Telefonu yeniden başlatın. Ardından Google uygulamasından **Voice Match**'i tamamlayın.

> ⚠️ **XiaoAi'yi kaldırmayın.** HyperOS 4'te alt çubuk hareketi XiaoAi üzerinden Circle to Search'e yönlendirilir.

Ek modül, root yöneticinizde **otomatik güncelleme** destekler.

## Sınırlamalar

- Ana ekranda Google arama çubuğu / Discover: HyperOS 4 başlatıcısı yerel (Rust) koddur, hook edilemez.
- "Hey Google" yazılım algılamasıyla çalışır (DSP değil).

## Lisans ve teşekkür

GPL-3.0. Ayrıntılar için [LICENSE](LICENSE) ve [NOTICE.txt](NOTICE.txt).
Xiaomi HyperOS uyarlaması: **Leekinn**.
Telegram: [t.me/xioamifetiscisi](https://t.me/xioamifetiscisi)

Teşekkürler — bu proje onların emeği üzerine kurulu:
- [@zopulus](https://github.com/zopulus) — ColorOS için [Coogle](https://github.com/zopulus/coogle), XGoogle'ın temeli
- [@MiToverG422](https://github.com/MiToverG422) — [ONextBox](https://github.com/MiToverG422/ONextBox), asistan hook'larının kaynağı
- [@luckyzyx](https://github.com/luckyzyx) — [LuckyTool](https://github.com/luckyzyx/LuckyTool), kod referansı
- [@tiann](https://github.com/tiann) — [KernelSU](https://github.com/tiann/KernelSU), arayüz tasarımı
- [@LuckyPray](https://github.com/LuckyPray) — [DexKit](https://github.com/LuckyPray/DexKit)

Bu proje Google veya Xiaomi ile bağlantılı değildir; markalar sahiplerine aittir.

---

## English

**An LSPosed module bringing Google features to the Xiaomi HyperOS 4 China ROM.**
Based on [zopulus/Coogle](https://github.com/zopulus/coogle), ported from ColorOS to Xiaomi HyperOS 4.

### Features

- ✅ **Long-press power → Gemini / Google Assistant**
- ✅ **Long-press the navigation handle → Circle to Search**
- ✅ **"Hey Google"** voice activation
- ✅ **Live translate:** in Circle to Search, tap Translate and keep translating as you scroll
- ✅ **Google Lens**
- ✅ **Android Auto:** system app and car projection role
- ✅ **Removes the GMS China restriction** (Quick Share included)
- 🧪 Lock screen shortcuts: Assistant, Lens/QR, Wallet, Home controls
- 🧪 Control center Lens/QR tile
- 🧪 Double-press power → Google Wallet
- 🌐 UI languages: Türkçe, English, Русский, Español, 简体中文

### Requirements

- Xiaomi HyperOS 4 China ROM (Android 17). Tested on Xiaomi 15 Ultra.
- Root: KernelSU / SukiSU (Magisk and APatch support added, untested)
- LSPosed (API 102)
- Google app (Play Store)

### Installation

1. Download and install the **XGoogle APK** from [Releases](https://github.com/Leekin2/XGoogle/releases).
2. Enable **XGoogle** in **LSPosed → Modules** and select all recommended scopes:
   System Framework, System UI, Settings, Google, XiaoAi (`com.miui.voiceassist`), Always-on display (`com.miui.aod`).
3. Grant XGoogle **root access** in your root manager.
4. Open XGoogle and turn on **Hey Google**; the companion module installs automatically. (Or install the companion ZIP from Releases in your root manager.)
5. Reboot, then complete **Voice Match** in the Google app.

> ⚠️ **Do not remove XiaoAi.** On HyperOS 4 the navigation handle gesture reaches Circle to Search through XiaoAi.

The companion module supports **automatic updates** in your root manager.

### Limitations

- Google search bar / Discover on the home screen: the HyperOS 4 launcher is native (Rust) code and cannot be hooked.
- "Hey Google" uses software detection (not DSP).

### License and credits

GPL-3.0. See [LICENSE](LICENSE) and [NOTICE.txt](NOTICE.txt).
Xiaomi HyperOS port: **Leekinn**.

Thanks — this project is built on their work:
- [@zopulus](https://github.com/zopulus) — [Coogle](https://github.com/zopulus/coogle) for ColorOS, the base of XGoogle
- [@MiToverG422](https://github.com/MiToverG422) — [ONextBox](https://github.com/MiToverG422/ONextBox), origin of the assistant hooks
- [@luckyzyx](https://github.com/luckyzyx) — [LuckyTool](https://github.com/luckyzyx/LuckyTool), code reference
- [@tiann](https://github.com/tiann) — [KernelSU](https://github.com/tiann/KernelSU), UI design
- [@LuckyPray](https://github.com/LuckyPray) — [DexKit](https://github.com/LuckyPray/DexKit)

This project is not affiliated with Google or Xiaomi; trademarks belong to their owners.
