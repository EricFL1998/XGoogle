# XGoogle

**Xiaomi HyperOS 4 Çin ROM'una Google özellikleri getiren LSPosed modülü.**
[zopulus/Coogle](https://github.com/zopulus/coogle) tabanlıdır; ColorOS yerine Xiaomi HyperOS 4 için uyarlanmıştır.

📥 **İndir:** [Son sürüm](https://github.com/Leekin2/XGoogle/releases/latest) · 💬 **Geri bildirim ve şikayet:** Telegram [@emcleek](https://t.me/emcleek) · 📢 **Kanal:** [t.me/xioamifetiscisi](https://t.me/xioamifetiscisi)

[English below](#english)

## Özellikler

- ✅ **Güç tuşuna basılı tut → Gemini / Google Asistan**
- ✅ **Alt çubuğa basılı tut → Circle to Search**
- ✅ **"Hey Google"** sesli uyandırma, ekran kapalıyken de
- ✅ **Canlı çeviri:** Circle to Search'te Çevir'e basıp kaydırınca çeviri devam eder
- ✅ **Google Lens:** Konuş ve Oluştur dahil; Google güncellense de çalışmaya devam eder
- ✅ **Android Auto:** sistem uygulaması ve araç projeksiyonu rolü
- ✅ **GMS Çin kısıtlamasının kaldırılması** (Quick Share dahil)
- 🧪 **Kilit ekranı kısayolları:** Gemini, Lens/QR, Cüzdan, Ev kontrolleri. Gemini, Lens ve Ev kilit açılmadan açılır
- 🧪 **Güç tuşuna çift bas → Google Cüzdan**, kilit ekranında da açılır
- 🧪 **Kamerada Google Lens butonu** (isteğe bağlı), kilit ekranında da çalışır
- 🧪 Kontrol merkezi Lens/QR kutucuğu
- 🌐 Arayüz dilleri: Türkçe, English, Русский, Español, 简体中文

Her özellik uygulamadan ayrı ayrı açılıp kapatılabilir; kapatınca sistem eski haline döner.

## Gereksinimler

- Xiaomi HyperOS 4 Çin ROM (Android 17). Xiaomi 15 Ultra'da test edildi.
- Root: KernelSU / SukiSU (Magisk ve APatch desteği eklendi, test edilmedi)
- LSPosed (API 102)
- Google uygulaması (Play Store)

## Kurulum

1. [Son sürüm](https://github.com/Leekin2/XGoogle/releases/latest) sayfasından **XGoogle APK**'sını indirip kurun.
2. **LSPosed → Modüller → XGoogle**'ı etkinleştirin ve önerilen kapsamların hepsini seçin:
   Sistem Çerçevesi, Sistem Arayüzü, Ayarlar, Google, XiaoAi (`com.miui.voiceassist`), Her Zaman Açık Ekran (`com.miui.aod`), Güvenlik (`com.miui.securitycore`), Google Cüzdan. Kameradaki Lens butonunu kullanacaksanız Kamera'yı da seçin.
3. Root yöneticinizde XGoogle'a **root izni** verin.
4. **Ayarlar → Uygulamalar → Varsayılan uygulamalar → Dijital asistan** → **Google** seçin.
5. XGoogle'ı açın ve **Hey Google**'ı (ek modül otomatik kurulur) ve **GMS kısıtlamasını kaldır**'ı açın. Circle to Search, Lens ve canlı çeviri varsayılan olarak açıktır.
6. Telefonu yeniden başlatın. Ardından Google uygulamasından **Voice Match**'i tamamlayın.

> ⚠️ **XiaoAi'yi kaldırmayın.** HyperOS 4'te alt çubuk hareketi XiaoAi üzerinden Circle to Search'e yönlendirilir.

## Özellikleri kullanma

- **Kilit ekranı kısayolları:** XGoogle'da *Kilit ekranı kısayolları*'nı açıp yeniden başlatın. Sonra kilit ekranı düzenleyicisinden sol veya sağ kısayola Gemini, Google Lens, Google Cüzdan ya da Ev kontrollerini seçin.
- **Google Cüzdan:** XGoogle'da *Google Cüzdan*'ı açın; güç tuşuna çift basma Google Cüzdan'a ayarlanır. Kilit ekranında ödeme ekranı açılır, *Cüzdan'ı açın* parmak izi/şifre ister.
- **Kamerada Google Lens:** XGoogle'da *Kamerada Google Lens*'i açın, LSPosed'da Kamera kapsamını onaylayıp yeniden başlatın. Lens butonu kamera menüsünde (üstteki ok) çıkar.
- **Ev kontrolleri:** Google Home uygulamasında bir ev ve cihaz kurulu olmalıdır; yoksa panelde "Ev kurulumu yap" görünür.
- **Lens kutucuğu:** Kontrol merkezini düzenleyip Lens kutucuğunu ekleyin.

## Güncelleme

1. Yeni APK'yı mevcut sürümün üstüne kurun.
2. Ek modül değiştiyse root yöneticiniz güncellemeyi kendisi gösterir; güncelleyin.
3. Telefonu yeniden başlatın.

## Sorun giderme

- **Hey Google çalışmıyor:** Root yöneticinizde ek modülün açıklamasına bakın; "✅Google sistem uygulaması" ve "✅Kayıt bileşenleri yüklü" yazmalı. Google'ın varsayılan dijital asistan olduğundan ve Voice Match'in tamamlandığından emin olun.
- **Cüzdan'ı ya da Google'ı yeniden kurdum, özellik çalışmıyor:** LSPosed yeniden kurulan uygulamayı kapsamdan çıkarır. XGoogle'ı açın; LSPosed'dan gelen *Kapsam talebi* bildirimini onaylayın ve uygulamayı yeniden başlatın.
- **Kamerada Lens butonu yok:** *Kamerada Google Lens* açık mı, LSPosed'da Kamera kapsamı seçili mi kontrol edin, sonra yeniden başlatın.
- **Alt çubuk Circle to Search açmıyor:** XiaoAi kurulu olmalı ve XGoogle'da Circle to Search açık olmalı.
- **Hâlâ çözülmediyse:** Telegram'dan [@emcleek](https://t.me/emcleek)'e yazın; mümkünse LSPosed kaydını ekleyin.

## Nitelikler

- **Boyut:** Uygulama ~3,3 MB, ek modül ~0,45 MB
- **RAM:** Ek yük yok. Google asistan işlemi ~90 MB kullanır, bu her Android telefonda zaten böyledir.
- **Otomatik güncelleme:** Ek modül, root yöneticinizde yeni sürüm çıkınca güncelleme bildirir.
- **Güvenli kaldırma:** Ek modül kaldırılınca değiştirdiği tuş ayarları eski haline döner.
- **Temizlikten etkilenmez:** HyperOS "tek tuşla temizle" Google asistanını kapatmaz.

## Sınırlamalar

- Ana ekranda Google arama çubuğu / Discover: HyperOS 4 başlatıcısı yerel (Rust) koddur, hook edilemez.
- "Hey Google" yazılım algılamasıyla çalışır (DSP değil).

## Lisans ve teşekkür

GPL-3.0. Ayrıntılar için [LICENSE](LICENSE), [NOTICE.txt](NOTICE.txt) ve [CHANGELOG](CHANGELOG.md).
Xiaomi HyperOS uyarlaması: **Leekinn**.

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

📥 **Download:** [Latest release](https://github.com/Leekin2/XGoogle/releases/latest) · 💬 **Feedback and complaints:** Telegram [@emcleek](https://t.me/emcleek) · 📢 **Channel:** [t.me/xioamifetiscisi](https://t.me/xioamifetiscisi)

### Features

- ✅ **Long-press power → Gemini / Google Assistant**
- ✅ **Long-press the navigation handle → Circle to Search**
- ✅ **"Hey Google"** voice activation, also with the screen off
- ✅ **Live translate:** in Circle to Search, tap Translate and keep translating as you scroll
- ✅ **Google Lens:** Talk and Create included; keeps working after Google app updates
- ✅ **Android Auto:** system app and car projection role
- ✅ **Removes the GMS China restriction** (Quick Share included)
- 🧪 **Lock screen shortcuts:** Gemini, Lens/QR, Wallet, Home controls. Gemini, Lens and Home open without unlocking
- 🧪 **Double-press power → Google Wallet**, also on the lock screen
- 🧪 **Google Lens button in the camera** (optional), also over the lock screen
- 🧪 Control center Lens/QR tile
- 🌐 UI languages: Türkçe, English, Русский, Español, 简体中文

Every feature can be turned on or off on its own; turning one off brings back the stock behavior.

### Requirements

- Xiaomi HyperOS 4 China ROM (Android 17). Tested on Xiaomi 15 Ultra.
- Root: KernelSU / SukiSU (Magisk and APatch support added, untested)
- LSPosed (API 102)
- Google app (Play Store)

### Installation

1. Download and install the **XGoogle APK** from the [latest release](https://github.com/Leekin2/XGoogle/releases/latest).
2. Enable **XGoogle** in **LSPosed → Modules** and select all recommended scopes:
   System Framework, System UI, Settings, Google, XiaoAi (`com.miui.voiceassist`), Always-on display (`com.miui.aod`), Security (`com.miui.securitycore`), Google Wallet. Add Camera too if you want the camera Lens button.
3. Grant XGoogle **root access** in your root manager.
4. **Settings → Apps → Default apps → Digital assistant** → choose **Google**.
5. Open XGoogle and turn on **Hey Google** (the companion module installs automatically) and **Remove GMS restriction**. Circle to Search, Lens and live translate are on by default.
6. Reboot, then complete **Voice Match** in the Google app.

> ⚠️ **Do not remove XiaoAi.** On HyperOS 4 the navigation handle gesture reaches Circle to Search through XiaoAi.

### Using the features

- **Lock screen shortcuts:** turn on *Lock screen shortcuts* in XGoogle and reboot. Then pick Gemini, Google Lens, Google Wallet or Home controls for the left or right shortcut in the lock screen editor.
- **Google Wallet:** turn on *Google Wallet* in XGoogle; double-pressing power is set to Google Wallet. On the lock screen the tap-to-pay screen opens; *Open Wallet* asks for your fingerprint/PIN.
- **Google Lens in Camera:** turn on *Google Lens in Camera*, approve the Camera scope in LSPosed and reboot. The Lens button is in the camera menu (the arrow at the top).
- **Home controls:** a home with devices must be set up in the Google Home app; otherwise the panel shows "Set up home".
- **Lens tile:** edit the control center and add the Lens tile.

### Updating

1. Install the new APK over the current one.
2. If the companion module changed, your root manager offers the update; install it.
3. Reboot.

### Troubleshooting

- **Hey Google does not work:** check the companion module's description in your root manager; it should show "✅Google sistem uygulaması" (Google system app) and "✅Kayıt bileşenleri yüklü" (enrollment components loaded). Make sure Google is the default digital assistant and Voice Match is complete.
- **I reinstalled Wallet or Google and a feature stopped working:** LSPosed drops a reinstalled app from the scope. Open XGoogle, approve LSPosed's *Scope request* notification and restart the app.
- **No Lens button in the camera:** check that *Google Lens in Camera* is on and the Camera scope is selected in LSPosed, then reboot.
- **The navigation handle does not open Circle to Search:** XiaoAi must be installed and Circle to Search turned on in XGoogle.
- **Still stuck:** message [@emcleek](https://t.me/emcleek) on Telegram, with your LSPosed log if possible.

### Specs

- **Size:** app ~3.3 MB, companion module ~0.45 MB
- **RAM:** no extra cost. The Google assistant process uses ~90 MB, as on any Android phone.
- **Auto-update:** the companion module shows updates in your root manager.
- **Safe uninstall:** removing the companion module restores your key settings.
- **Survives cleanup:** HyperOS one-key clean no longer kills the Google assistant.

### Limitations

- Google search bar / Discover on the home screen: the HyperOS 4 launcher is native (Rust) code and cannot be hooked.
- "Hey Google" uses software detection (not DSP).

### License and credits

GPL-3.0. See [LICENSE](LICENSE), [NOTICE.txt](NOTICE.txt) and the [CHANGELOG](CHANGELOG.md).
Xiaomi HyperOS port: **Leekinn**.

Thanks — this project is built on their work:
- [@zopulus](https://github.com/zopulus) — [Coogle](https://github.com/zopulus/coogle) for ColorOS, the base of XGoogle
- [@MiToverG422](https://github.com/MiToverG422) — [ONextBox](https://github.com/MiToverG422/ONextBox), origin of the assistant hooks
- [@luckyzyx](https://github.com/luckyzyx) — [LuckyTool](https://github.com/luckyzyx/LuckyTool), code reference
- [@tiann](https://github.com/tiann) — [KernelSU](https://github.com/tiann/KernelSU), UI design
- [@LuckyPray](https://github.com/LuckyPray) — [DexKit](https://github.com/LuckyPray/DexKit)

This project is not affiliated with Google or Xiaomi; trademarks belong to their owners.
