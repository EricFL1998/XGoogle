#!/system/bin/sh
PACKAGE=com.google.android.googlequicksearchbox
APP_DIR=$MODPATH/system/priv-app/GoogleQuickSearchBox

# The bundled enrollment APKs are built for Android 16 (API 36).
if [ "$API" -lt 36 ]; then
    abort "! Android 16 veya üstü gerekli (bu cihaz: API $API)"
fi

mkdir -p "$APP_DIR"
find "$APP_DIR" -maxdepth 1 -type f -name '*.apk' -delete 2>/dev/null

paths=$(pm path "$PACKAGE" 2>/dev/null | sed -n 's/^package://p')
[ -n "$paths" ] || abort "! Google uygulaması bulunamadı; önce Play Store'dan kurun"

# Loop in this shell (not a pipe subshell) so a failed copy can abort the install.
IFS='
'
for apk in $paths; do
    [ -f "$apk" ] || abort "! $apk okunamadı"
    cp -f "$apk" "$APP_DIR/${apk##*/}" || abort "! $apk kopyalanamadı"
done
unset IFS
[ -f "$APP_DIR/base.apk" ] || abort "! Google uygulaması kopyalanamadı"

# Android Auto is optional: systemize it only when it is installed.
case "$(/system/bin/sh "$MODPATH/systemize-aa.sh" "$MODPATH")" in
    missing) ui_print "- Android Auto kurulu değil, atlandı (kurduktan sonra modülü yeniden kurun)" ;;
    ok*) ui_print "- Android Auto sistem uygulaması yapıldı" ;;
    *) abort "! Android Auto kopyalanamadı" ;;
esac

set_perm_recursive $MODPATH 0 0 0755 0644

set_perm_recursive "$MODPATH/system" 0 0 0755 0644
for script in boot-completed.sh service.sh setup.sh uninstall.sh prepare-audio.sh status.sh systemize-aa.sh; do
    set_perm "$MODPATH/$script" 0 0 0755
done
/system/bin/sh "$MODPATH/prepare-audio.sh"
ui_print "Google sistem uygulaması, kayıt bileşenleri ve DSP desteği kuruldu. Yeniden başlatın."
