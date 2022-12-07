
$client = new-object System.Net.WebClient
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$pwd= Get-Location
$shell = New-Object -ComObject Shell.Application

Write-Host "Unzipping Java 17"
#Expand-Archive -Force .\openjdk-17.0.1_windows-x64_bin.zip
$Source="$pwd\openjdk-17.0.1_windows-x64_bin.zip"
$Destination="$pwd\openJdk17"

$sourceFolder = $shell.NameSpace($Source)
$destinationFolder=$shell.NameSpace($Destination)
$destinationFolder.CopyHere($sourceFolder.Items())

Write-Host "Unzipping Python 3.8"
#Expand-Archive -Force .\python-3.8.7-embed-amd64.zip
$Source="$pwd\python3WithRnn.zip"
$Destination="$pwd\python3"
$sourceFolder = $shell.NameSpace($Source)
$destinationFolder=$shell.NameSpace($Destination)
$destinationFolder.CopyHere($sourceFolder.Items())

$WshShell = New-Object -comObject WScript.Shell
$currentDir = $WshShell.CurrentDirectory()
$Shortcut = $WshShell.CreateShortcut( $currentDir +"\CliniDeID.lnk")
$Shortcut.TargetPath = ( $currentDir +"\runCliniDeID.vbs")
$Shortcut.IconLocation = ($currentDir + "\classes\gui\CliniDeID-Windows.ico")
$Shortcut.Description = "Run CliniDeID GUI"
$Shortcut.WorkingDirectory = $currentDir
$Shortcut.Save()