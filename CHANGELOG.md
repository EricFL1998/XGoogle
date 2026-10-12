# Değişiklikler / Changelog

## XGoogle 1.2.2

- Android Auto: araç projeksiyonu rolü artık veriliyor; Android Auto araca bağlanmak için gereken izinleri alıyor.
  Android Auto: the car projection role is now granted, so Android Auto gets the permissions it needs to connect to the car.
- Yeni "Ayarlar'da Google" düğmesi: Ayarlar'ın altındaki Google hizmetleri girişini gösterir ya da gizler (varsayılan: açık).
  New "Google in Settings" switch: shows or hides the Google services entry at the bottom of Settings (on by default).
- Ek modül 1.2.2: HyperOS'ta kullanılmayan ses modeli kayıt uygulamalarını kapatır; açılıştaki çökmeleri giderildi.
  Companion module 1.2.2: turns off the hotword enrollment apps HyperOS doesn't use, fixing their crashes at boot.
- HyperOS'ta LSPosed kaydında ColorOS'a ait Ayarlar hataları artık çıkmıyor.
  No more ColorOS-only Settings errors in the LSPosed log on HyperOS.

## XGoogle 1.2.1

- HyperOS "tek tuşla temizle" artık Google'ın asistanını ve Hey Google algılayıcısını kapatmıyor; güç tuşu ve Hey Google temizlikten sonra da çalışır.
  HyperOS one-key clean no longer kills the Google assistant and the Hey Google detector; the power key and Hey Google keep working after cleaning.
- Uygulama açılırken ayarlar artık bir anlığına kapalı görünmüyor.
  Settings no longer flash as off while the app opens.
- Ek modül HyperOS'ta DSP ses modeli kaydetmiyor (Hey Google'ı durdurabiliyordu); yazılım algılaması kullanılır.
  The companion module no longer enrolls a DSP keyphrase model on HyperOS (it could stop Hey Google); software detection is used.
- Ek modül durum yazısı HyperOS'ta yanlış uyarı göstermiyor.
  The companion status no longer shows a false warning on HyperOS.
- README: Google'ı varsayılan asistan yapma ve açılacak seçenekler eklendi.
  README: setting Google as default assistant and which options to enable.

## XGoogle 1.2

Coogle 1.2'den (zopulus) çatallandı ve Xiaomi HyperOS 4 Çin ROM'una uyarlandı.
Forked from Coogle 1.2 (zopulus) and ported to the Xiaomi HyperOS 4 China ROM.

- HyperOS 4 desteği; ColorOS'a özel hook'lar HyperOS'ta kendiliğinden atlanır.
  HyperOS 4 support; ColorOS-only hooks are skipped on HyperOS.
- Alt çubuk → Circle to Search (XiaoAi üzerinden yönlendirme).
  Navigation handle → Circle to Search (routed through XiaoAi).
- Güç tuşu → Gemini / Google Asistan; açılışta kopan asistan servisi kendiliğinden yeniden bağlanır.
  Power key → Gemini / Google Assistant; the assistant service reconnects automatically after boot.
- Canlı çeviri (Circle to Search, kaydırarak çeviri); Google uygulaması taraması önbelleğe alınır.
  Live translate (Circle to Search, translate as you scroll); the Google app scan is cached.
- Android Auto: sistem uygulaması ve araç projeksiyonu rolü.
  Android Auto: system app and car projection role.
- Kilit ekranı kısayolları ve kontrol merkezi Lens kutucuğu.
  Lock screen shortcuts and control center Lens tile.
- Ek modül: Xiaomi ayrıcalıklı izin listesi, Android 16+ kontrolü, kurulum hata denetimi, Magisk/APatch desteği, kaldırırken tuş ayarlarının geri yüklenmesi, otomatik güncelleme.
  Companion module: Xiaomi privileged-permission allowlist, Android 16+ check, install failure checks, Magisk/APatch support, key settings restored on uninstall, automatic updates.
- Türkçe, English, Русский, Español, 简体中文 arayüz.
  Türkçe, English, Русский, Español, 简体中文 UI.
- Yeni paket kimliği: `io.github.leekin2.xgoogle`.
  New package id: `io.github.leekin2.xgoogle`.
