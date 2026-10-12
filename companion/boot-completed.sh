#!/system/bin/sh
MODDIR=${0%/*}
# HyperOS: route the power key and nav handle to Google (the CN ROM resets the
# nav-handle key during boot, so reapply both every time).
if [ -n "$(getprop ro.mi.os.version.name)" ]; then
    # Keep the user's own choices once, outside the module, for uninstall.sh.
    backup=/data/adb/coogle_keys_backup
    if [ ! -f "$backup" ]; then
        power=$(settings get system long_press_power_key)
        home=$(settings get system long_press_home_key)
        # Already ours (e.g. an earlier install): the original is unknown, use the CN defaults.
        [ "$power" = launch_google_search ] && power=launch_voice_assistant
        [ "$home" = launch_google_search ] && home=launch_xiaoai_screen_identification
        printf 'power=%s\nhome=%s\n' "$power" "$home" > "$backup"
    fi
    settings put system long_press_power_key launch_google_search
    settings put system long_press_home_key launch_google_search
fi
# Keep the Android Auto allow-list in step with Play Store updates for the next boot.
if [ -d "$MODDIR/system/product/priv-app/AndroidAuto" ]; then
    /system/bin/sh "$MODDIR/systemize-aa.sh" "$MODDIR" permissions-only > /dev/null 2>&1
    # The CN ROM never assigns the car projection role, even with the overlay's config.
    role=android.app.role.SYSTEM_AUTOMOTIVE_PROJECTION
    cmd role get-role-holders --user 0 "$role" | grep -q gearhead ||
        cmd role add-role-holder --user 0 "$role" com.google.android.projection.gearhead 0
fi
sleep 30
# Binder shell commands require pipe FDs rather than module-directory FDs.
/system/bin/sh "$MODDIR/setup.sh" 2>&1 | cat > /dev/null
/system/bin/sh "$MODDIR/status.sh" > /dev/null 2>&1

# HyperOS can kill the Google app during boot; the voice interaction service then stays
# bound but disconnected (power key and Hey Google stop responding). Restarting the app
# makes the system reconnect it.
for attempt in 1 2 3; do
    sleep 30
    dumpsys voiceinteraction 2>/dev/null | grep -q 'mService=null' || break
    am force-stop com.google.android.googlequicksearchbox
done
