param(
    [string]$AdbPath = 'adb',
    [string]$Serial = '',
    [switch]$GravarVideo
)

$ErrorActionPreference = 'Stop'
$projectPath = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$recorder = $null
$recorderPid = ''
$remoteVideo = ''
$videoName = ''
$runId = Get-Date -Format 'yyyyMMdd-HHmmss'

Push-Location -LiteralPath $projectPath
try {
    $AdbPath = (Get-Command $AdbPath -ErrorAction Stop).Source
    $deviceLines = & $AdbPath devices
    if ($LASTEXITCODE -ne 0) { throw 'Não foi possível executar o ADB.' }
    $devices = @($deviceLines | Select-String '^([^\s]+)\s+device$' | ForEach-Object { $_.Matches[0].Groups[1].Value })
    if (-not $Serial) {
        if ($devices.Count -ne 1) { throw 'Conecte somente um dispositivo de teste ou informe -Serial.' }
        $Serial = $devices[0]
    }
    if ($Serial -notin $devices) { throw 'O dispositivo não está conectado e autorizado.' }
    $evidencePath = Join-Path $projectPath 'docs/evidencias'
    New-Item -ItemType Directory -Force -Path $evidencePath | Out-Null

    function Invoke-AdbChecked([string[]]$CommandArgs) {
        & $AdbPath -s $Serial @CommandArgs
        if ($LASTEXITCODE -ne 0) { throw "ADB falhou: $($CommandArgs -join ' ')" }
    }
    function Invoke-AppTest([string]$TestName) {
        $result = & $AdbPath -s $Serial shell am instrument -w -e class $TestName 'com.example.habittracker.test/androidx.test.runner.AndroidJUnitRunner' 2>&1
        $text = $result -join "`n"
        Write-Host $text
        $text | Add-Content -LiteralPath (Join-Path $evidencePath "$runId-testes.txt") -Encoding utf8
        if ($LASTEXITCODE -ne 0 -or $text -match 'FAILURES!!!|INSTRUMENTATION_FAILED|shortMsg=' -or $text -notmatch 'OK \(') {
            throw "Teste falhou: $TestName"
        }
    }
    function Start-Video([string]$Name) {
        if (-not $GravarVideo) { return }
        # Não interfere com outra gravação já iniciada no aparelho.
        $existing = (& $AdbPath -s $Serial shell pidof screenrecord) -join ''
        if ($existing.Trim()) { throw 'Já existe uma gravação de tela ativa no dispositivo.' }
        $script:videoName = "$runId-$Name.mp4"
        $script:remoteVideo = "/sdcard/$($script:videoName)"
        $script:recorder = Start-Process -FilePath $AdbPath -ArgumentList @(
            '-s', $Serial, 'shell', 'screenrecord', '--time-limit', '180', $script:remoteVideo
        ) -WindowStyle Hidden -PassThru
        Start-Sleep -Milliseconds 1000
        $script:recorderPid = ((& $AdbPath -s $Serial shell pidof screenrecord) -join '').Trim()
        if ($script:recorderPid -notmatch '^\d+$') { throw 'Não foi possível iniciar screenrecord.' }
    }
    function Stop-Video {
        if (-not $script:recorder) { return }
        if (-not $script:recorder.HasExited -and $script:recorderPid -match '^\d+$') {
            & $AdbPath -s $Serial shell kill -2 $script:recorderPid
            if (-not $script:recorder.WaitForExit(10000)) {
                throw 'A gravação não finalizou. Verifique o dispositivo antes de continuar.'
            }
        }
        Invoke-AdbChecked @('pull', $script:remoteVideo, (Join-Path $evidencePath $script:videoName))
        $script:recorder = $null
        $script:recorderPid = ''
    }

    Write-Host 'Use um dispositivo de teste. O script não limpa o armazenamento do aplicativo.'
    & (Join-Path $projectPath 'gradlew.bat') :app:assembleDebug :app:assembleDebugAndroidTest --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'A compilação falhou.' }
    Invoke-AdbChecked @('install', '-r', 'app/build/outputs/apk/debug/app-debug.apk')
    Invoke-AdbChecked @('install', '-r', 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk')

    Invoke-AppTest 'com.example.habittracker.RoomPersistenciaTest'
    Start-Video 'crud'
    Invoke-AppTest 'com.example.habittracker.CrudInterfaceTest#etapa01CrudPelasTelas'
    Stop-Video

    Start-Video 'persistencia-fechar-reabrir'
    Invoke-AppTest 'com.example.habittracker.CrudInterfaceTest#etapa02PrepararPersistencia'
    Invoke-AdbChecked @('shell', 'am', 'force-stop', 'com.example.habittracker')
    # Mostrar a tela inicial do Android no intervalo entre os dois processos.
    Invoke-AdbChecked @('shell', 'input', 'keyevent', 'KEYCODE_HOME')
    Start-Sleep -Seconds 2
    Invoke-AdbChecked @('shell', 'am', 'start', '-n', 'com.example.habittracker/.MainActivity')
    Invoke-AppTest 'com.example.habittracker.CrudInterfaceTest#etapa03VerificarPersistencia'
    Stop-Video

    Invoke-AdbChecked @('pull', '/sdcard/Android/data/com.example.habittracker/files/evidencias/.', $evidencePath)
    $android = (& $AdbPath -s $Serial shell getprop ro.build.version.release) -join ''
    $model = (& $AdbPath -s $Serial shell getprop ro.product.model) -join ''
    @("Dispositivo: $model", "Android: $android", "Serial: $Serial", "Execução: $(Get-Date -Format s)") |
        Set-Content -LiteralPath (Join-Path $evidencePath "$runId-dispositivo.txt") -Encoding utf8
    Write-Host "Testes aprovados. Evidências copiadas para $evidencePath."
} finally {
    if ($recorder) {
        try { Stop-Video } catch { Write-Warning "Não foi possível finalizar a gravação: $_" }
    }
    Pop-Location
}
