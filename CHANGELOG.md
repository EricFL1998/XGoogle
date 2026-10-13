# Değişiklikler / Changelog

## XGoogle 1.2.5

- Güvenlik düzeltmesi: kilit ekranında Google Cüzdan, doğrulama yapılmadan "Okuyucuya tutun" ekranına ulaşabiliyordu. XGoogle artık Cüzdan'ın doğrulamasını onun yerine cevaplamıyor; Cüzdan parmak izi/şifre istiyor.
  Security fix: on the lock screen, Google Wallet could reach "Hold to reader" without verifying you. XGoogle no longer answers Wallet's verification for it; Wallet now asks for your fingerprint/PIN.
- Kilit ekranındaki Cüzdan kısayolu, güç tuşuna çift basmadaki gibi ödeme ekranını açar.
  The lock screen Wallet shortcut opens the tap-to-pay screen like double-pressing power.
- Kilit ekranında Lens: "Konuş" artık kilit ekranının üstünde açılır.
  Lens on the lock screen: Talk now opens over the lock screen too.
- Circle to Search sadece alt çubuğa basılı tutunca açılır; çift dokunma ve sürükleyip tutma XiaoAi'de kalır.
  Circle to Search only takes the long press on the navigation handle; double tap and drag-and-hold stay with XiaoAi.
- Yeniden başlatma gerektiren bir ayar değişince kısa bir hatırlatma çıkar.
  A short reminder appears when a setting needs a restart.
- Ek modül 1.2.5: ksud'u /data/adb içinde tutan KernelSU sürümlerinde de kurulur; HyperOS'ta Hey Google durumu daha anlaşılır.
  Companion 1.2.5: installs on KernelSU builds that keep ksud in /data/adb; clearer Hey Google status on HyperOS.

## XGoogle 1.2.4

- Google Lens "Konuş" ve "Oluştur": Google güncellense de hedefleri kendisi bulur; Google 17.66.11'de yeniden çalışıyor.
  Google Lens Talk and Create: finds its targets even after Google updates; works again on Google 17.66.11.
- Google Cüzdan kilit ekranı önizlemesi de Cüzdan güncellemelerinden etkilenmiyor.
  Google Wallet's lock screen preview also survives Wallet updates.
- Ek modül 1.2.4: KernelSU NoMount ile Google artık sistem uygulaması olarak kalıyor; "Google sistem uygulaması" durumu doğru okunuyor.
  Companion 1.2.4: Google stays a system app under KernelSU NoMount; the "Google system app" status is read correctly.
- Daha sade ayar ekranı: bölümlere ayrıldı, açıklamalar kısaldı.
  Simpler settings screen: grouped into sections, shorter descriptions.
- Cüzdan yeniden kurulsa da güç tuşuyla açılan ödeme ekranı kilit ekranının üstünde açılır (HyperOS'un "kilit ekranında göster" izni gerekmez).
  Even after reinstalling Wallet, the power key's tap-to-pay screen opens over the lock screen (no HyperOS "show on lock screen" permission needed).
- Yeniden kurulan uygulamalar LSPosed kapsamından düşünce XGoogle açılışta onları geri eklemek için bir kez onay ister.
  When reinstalled apps drop out of the LSPosed scope, XGoogle asks once at launch to add them back.
- Kilit ekranındaki ödeme ekranında "Cüzdan'ı açın" artık kilidi ister ve kilit açılınca Cüzdan'ı açar (önceden tepki vermiyordu).
  On the lock screen tap-to-pay screen, "Open Wallet" now asks for the lock and opens Wallet once unlocked (it did nothing before).
- HyperOS'ta LSPosed kaydında ColorOS'a ait uyarılar artık çıkmıyor.
  No more ColorOS-only warnings in the LSPosed log on HyperOS.

Lens Konuş/Oluştur, Cüzdan önizlemesi ve NoMount düzeltmesi fikirlerini sağlayan [Coogle](https://github.com/zopulus/coogle) projesine teşekkürler.
Thanks to the [Coogle](https://github.com/zopulus/coogle) project for the Lens Talk/Create lookup, the Wallet preview and the NoMount fix.

## XGoogle 1.2.3

- Kilit ekranı kısayolları: Gemini ve Google Home cihaz kontrolleri kilit açılmadan açılır; Cüzdan önce kilidi ister.
  Lock screen shortcuts: Gemini and Google Home's device controls open without unlocking; Wallet asks for the lock first.
- Yeni "Kamerada Google Lens" düğmesi (varsayılan: kapalı): kameraya Lens butonu ekler, kilit ekranında da şifresiz açılır (fotoğraflar kilitli kalır).
  New "Google Lens in Camera" switch (off by default): adds a Lens button to the camera, which also opens over the lock screen (photos stay locked).
- Canlı çeviri ve "hep titreşsin" artık Circle to Search'ün altında açılır bir bölümde.
  Live translate and "always vibrate" now fold under Circle to Search.

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
