#!/system/bin/sh
MODDIR=${0%/*}
# KernelSU and APatch run boot-completed.sh themselves; Magisk only runs service.sh.
if [ "$KSU" = true ] || [ "$APATCH" = true ]; then
    exit 0
fi
until [ "$(getprop sys.boot_completed)" = 1 ]; do sleep 2; done
exec /system/bin/sh "$MODDIR/boot-completed.sh"
