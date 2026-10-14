#!/system/bin/sh
PACKAGE=com.google.android.googlequicksearchbox
APP_DIR=$MODPATH/system/priv-app/GoogleQuickSearchBox

# The bundled enrollment APKs are built for Android 16 (API 36).
if [ "$API" -lt 36 ]; then
    abort "! Android 16 or newer is required (this device: API $API)"
fi

mkdir -p "$APP_DIR"
find "$APP_DIR" -maxdepth 1 -type f -name '*.apk' -delete 2>/dev/null

paths=$(pm path "$PACKAGE" 2>/dev/null | sed -n 's/^package://p')
[ -n "$paths" ] || abort "! Google app not found; install it from the Play Store first"

# Loop in this shell (not a pipe subshell) so a failed copy can abort the install.
IFS='
'
for apk in $paths; do
    [ -f "$apk" ] || abort "! Could not read $apk"
    cp -f "$apk" "$APP_DIR/${apk##*/}" || abort "! Could not copy $apk"
done
unset IFS
[ -f "$APP_DIR/base.apk" ] || abort "! Could not copy the Google app"

# NoMount merges instead of replacing; the same check runs again on every boot (post-fs-data.sh).
. "$MODPATH/mount-google.sh"
prepare_google_mount "$MODPATH" || abort "! Could not prepare the Google folder"
if [ -f "$APP_DIR/.replace" ]; then ui_print "- Google folder in replace mode"; else ui_print "- NoMount detected, using merge mode"; fi

# Android Auto is optional: systemize it only when it is installed.
case "$(/system/bin/sh "$MODPATH/systemize-aa.sh" "$MODPATH")" in
    missing) ui_print "- Android Auto not installed, skipped (reinstall the module after installing it)" ;;
    ok*) ui_print "- Android Auto made a system app" ;;
    *) abort "! Could not copy Android Auto" ;;
esac

set_perm_recursive $MODPATH 0 0 0755 0644

set_perm_recursive "$MODPATH/system" 0 0 0755 0644
for script in boot-completed.sh service.sh setup.sh uninstall.sh prepare-audio.sh status.sh systemize-aa.sh; do
    set_perm "$MODPATH/$script" 0 0 0755
done
/system/bin/sh "$MODPATH/prepare-audio.sh"
ui_print "Google system app, enrollment components and DSP support installed. Reboot."
