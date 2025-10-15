# Create-RdsTruststore.ps1

# This script splits the AWS RDS global-bundle.pem file into individual
# certificates and imports them into a new JKS truststore.

# --- Configuration ---
$pemFile = "C:\myAWS\global-bundle.pem"
$keystoreFile = "rds-truststore.jks"
$keystorePassword = "changeit" # CHANGE THIS TO A SECURE PASSWORD

# Use the full path to keytool.exe from your image
$keytoolPath = "C:\Users\taese\.jdks\corretto-19.0.2\bin\keytool.exe" 

# --- Script Logic ---
$currentDir = Get-Location

# Step 1: Check for prerequisites
if (-not (Test-Path $pemFile)) {
    Write-Host "Error: The PEM file '$pemFile' does not exist."
    Write-Host "Please download it from https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem"
    exit
}

# Check if keytool.exe exists at the specified path
if (-not (Test-Path $keytoolPath)) {
    Write-Host "Error: keytool.exe not found at '$keytoolPath'."
    Write-Host "Please verify your JDK installation path."
    exit
}

# Step 2: Split the PEM bundle into individual certificate files
Write-Host "Splitting the certificate bundle..."
$certFileCount = 0
$lines = Get-Content -Path $pemFile
$outPath = ""

foreach ($line in $lines) {
    if ($line -eq "-----BEGIN CERTIFICATE-----") {
        $certFileCount++
        $outPath = "rds-ca-" + $certFileCount + ".pem"
        $line | Out-File -FilePath $outPath -Encoding ascii
    } else {
        $line | Out-File -FilePath $outPath -Encoding ascii -Append
    }
}

Write-Host "Found $($certFileCount) certificates."

if ($certFileCount -eq 0) {
    Write-Host "Error: No certificates found. The PEM file may be empty or corrupt. Exiting."
    exit
}

# Step 3: Import each certificate into the JKS truststore
Write-Host "Importing certificates into the keystore..."
$certFiles = Get-ChildItem -Path "rds-ca-*.pem"

if ($certFiles.Count -eq 0) {
    Write-Host "Error: No certificate files were found. The previous step failed."
    exit
}

foreach ($cert in $certFiles) {
    $certAlias = $cert.Name.Replace(".pem", "")
    Write-Host "Importing certificate: $($certAlias)"
    # Use the full path to keytool.exe with the call operator '&'
    & $keytoolPath -import -alias $certAlias -keystore $keystoreFile -file $cert.FullName -storepass $keystorePassword -noprompt
}

# Step 4: Clean up and verify
Write-Host "Cleaning up temporary files..."
Remove-Item -Path "rds-ca-*.pem"

if (Test-Path $keystoreFile) {
    Write-Host ""
    Write-Host "SUCCESS: Truststore '$keystoreFile' created successfully in '$currentDir'."
    Write-Host "To list its contents, use: `"`$keytoolPath`" -list -v -keystore $keystoreFile -storepass $keystorePassword`""
    Write-Host ""
} else {
    Write-Host "ERROR: Truststore '$keystoreFile' was not created. Check for errors above."
}