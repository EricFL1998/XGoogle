#!/system/bin/sh
# Make the installed Android Auto a privileged system app so it can start car projection.
# Usage: systemize-aa.sh <module dir> [permissions-only]
# Prints "missing" when Android Auto is not installed (nothing is changed then).
M=$1
AA=com.google.android.projection.gearhead
DIR=$M/system/product/priv-app/AndroidAuto
XML=$M/system/product/etc/permissions/privapp-permissions-xgoogle-androidauto.xml

pm path "$AA" > /dev/null 2>&1 || { echo missing; exit 0; }

if [ "$2" != permissions-only ]; then
    mkdir -p "$DIR"
    find "$DIR" -maxdepth 1 -type f -name '*.apk' -delete 2>/dev/null
    paths=$(pm path "$AA" | sed -n 's/^package://p')
    IFS='
'
    for apk in $paths; do
        cp -f "$apk" "$DIR/${apk##*/}" || exit 1
    done
    unset IFS
    [ -f "$DIR/base.apk" ] || exit 1
fi

# HyperOS stops boot when a privileged app holds a privileged permission that is not
# allow-listed, so list everything the installed version requests (extra entries are harmless).
perms=$(dumpsys package "$AA" | awk '
    /requested permissions:/ { f = 1; next }
    f && /^ *[A-Za-z ]+:$/   { f = 0 }
    f { p = $1; sub(/:.*/, "", p); if (p ~ /\./) print p }' | sort -u)
[ -n "$perms" ] || exit 1

mkdir -p "${XML%/*}"
{
    echo '<?xml version="1.0" encoding="utf-8"?>'
    echo '<permissions>'
    echo "    <privapp-permissions package=\"$AA\">"
    for p in $perms; do
        echo "        <permission name=\"$p\" />"
    done
    echo '    </privapp-permissions>'
    echo '</permissions>'
} > "$XML.tmp" && mv "$XML.tmp" "$XML"
echo "ok $(echo "$perms" | wc -l)"
