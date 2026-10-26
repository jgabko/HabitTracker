param(
    [string]$AdbPath = 'adb',
    [string]$Serial = ''
)

$ErrorActionPreference = 'Stop'
$projectPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
Push-Location -LiteralPath $projectPath
try {
    $deviceLines = & $AdbPath devices
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível executar o adb. Informe -AdbPath com o caminho do adb.exe.' }
    $devices = @($deviceLines | Select-String '^([^\s]+)\s+device$' | ForEach-Object { $_.Matches[0].Groups[1].Value })
    if (-not $Serial) {
        if ($devices.Count -ne 1) { throw 'Conecte somente um aparelho/emulador de teste ou informe -Serial.' }
        $Serial = $devices[0]
    }
    if ($Serial -notin $devices) { throw 'O dispositivo selecionado não está conectado e autorizado.' }
    Write-Host 'Use um dispositivo de teste. O script cria e remove somente itens com nomes de teste; não limpa o armazenamento do app.'

    & (Join-Path $projectPath 'gradlew.bat') :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'A compilação falhou.' }
    & $AdbPath -s $Serial install -r 'app/build/outputs/apk/debug/app-debug.apk'
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao instalar o aplicativo.' }
    & $AdbPath -s $Serial install -r 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk'
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao instalar os testes.' }

    function Invoke-AppTest([string]$TestName) {
        $result = & $AdbPath -s $Serial shell am instrument -w -e class $TestName 'com.example.habittracker.test/androidx.test.runner.AndroidJUnitRunner' 2>&1
        $text = $result -join "`n"
        Write-Host $text
        if ($LASTEXITCODE -ne 0 -or $text -match 'FAILURES!!!|INSTRUMENTATION_FAILED|shortMsg=' -or $text -notmatch 'OK \(') {
            throw "Teste falhou: $TestName"
        }
    }

    Invoke-AppTest 'com.example.habittracker.RoomPersistenciaTest'
    Invoke-AppTest 'com.example.habittracker.CrudInterfaceTest#etapa01CrudPelasTelas'
    Invoke-AppTest 'com.example.habittracker.CrudInterfaceTest#etapa02PrepararPersistencia'
    & $AdbPath -s $Serial shell am force-stop com.example.habittracker
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível encerrar o app.' }
    & $AdbPath -s $Serial shell am start -n 'com.example.habittracker/.MainActivity'
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível reabrir o app.' }
    Invoke-AppTest 'com.example.habittracker.CrudInterfaceTest#etapa03VerificarPersistencia'

    $evidencePath = Join-Path $projectPath 'docs/evidencias'
    New-Item -ItemType Directory -Force -Path $evidencePath | Out-Null
    & $AdbPath -s $Serial pull '/sdcard/Android/data/com.example.habittracker/files/evidencias/.' $evidencePath
    if ($LASTEXITCODE -ne 0) { throw 'Os testes terminaram, mas a cópia das capturas falhou.' }
    Write-Host 'Testes aprovados e capturas copiadas para docs/evidencias. Grave também o vídeo solicitado no enunciado.'
} finally {
    Pop-Location
}
