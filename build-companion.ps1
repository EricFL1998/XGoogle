# Packs companion/ into the module ZIP the app installs (app/src/main/assets/xgoogle-companion.zip).
# Shell scripts are written with LF endings and executable mode bits.
$ErrorActionPreference = 'Stop'
$source = Join-Path $PSScriptRoot 'companion'
$target = Join-Path $PSScriptRoot 'app/src/main/assets/xgoogle-companion.zip'
Add-Type -AssemblyName System.IO.Compression.FileSystem

$utf8 = New-Object System.Text.UTF8Encoding $false
Get-ChildItem $source -File | Where-Object { $_.Extension -in '.sh', '.awk', '.prop' } | ForEach-Object {
    [IO.File]::WriteAllText($_.FullName, ([IO.File]::ReadAllText($_.FullName) -replace "`r`n", "`n"), $utf8)
}

if (Test-Path $target) { Remove-Item $target }
$zip = [IO.Compression.ZipFile]::Open($target, 'Create')
try {
    foreach ($file in Get-ChildItem $source -Recurse -File) {
        $name = $file.FullName.Substring($source.Length + 1) -replace '\\', '/'
        $entry = [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip, $file.FullName, $name)
        $entry.ExternalAttributes = $(if ($name -like '*.sh') { 0x81ED } else { 0x81A4 }) -shl 16
    }
} finally {
    $zip.Dispose()
}
Write-Output $target
