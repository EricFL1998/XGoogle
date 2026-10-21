# Derleme / Building

**APK:** JDK 17, Android SDK Platform 37 ve Build Tools 36.0.0. `JAVA_HOME` ve `ANDROID_HOME` (veya `local.properties`) ayarlayıp `./build.ps1` çalıştırın. Yalnızca arm64-v8a derlenir.

**Ek modül:** `./build-companion.ps1` → `app/src/main/assets/xgoogle-companion.zip`. Modül kaynakları `companion/` klasöründedir; APK'yı derlemeden önce çalıştırın.

**İmza:** APK, derleyen bilgisayarın Android debug anahtarıyla imzalanır. Güncellemelerin mevcut kurulumun üstüne kurulabilmesi için aynı anahtar kullanılmalıdır.

---

**APK:** JDK 17, Android SDK Platform 37 and Build Tools 36.0.0. Set `JAVA_HOME` and `ANDROID_HOME` (or `local.properties`) and run `./build.ps1`. Only arm64-v8a is built.

**Companion module:** `./build-companion.ps1` → `app/src/main/assets/xgoogle-companion.zip`. The module sources live in `companion/`; run it before building the APK.

**Signing:** the APK is signed with the building machine's Android debug key. Updates must use the same key to install over an existing copy.
