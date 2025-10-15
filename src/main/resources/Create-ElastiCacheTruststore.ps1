# ======================================================
# Add ElastiCache Root CA to existing rds-truststore.jks
# ======================================================

# Path to keytool.exe
$keytoolPath = "C:\Users\taese\.jdks\corretto-19.0.2\bin\keytool.exe"

# Folder where your truststore is located
$destDir = "C:\myAWS"
$keyStore = Join-Path $destDir "rds-truststore.jks"
$storePass = "changeit"

# Download ElastiCache root CA
$elastiCachePem = Join-Path $destDir "ElastiCacheRootCA1.pem"
Write-Host "Downloading ElastiCacheRootCA1.pem..."
curl.exe -L "https://truststore.pki.rds.amazonaws.com/global/ElastiCacheRootCA1.pem" -o $elastiCachePem

# Verify file exists
if (Test-Path $elastiCachePem) {
    Write-Host "Importing ElastiCacheRootCA1.pem into truststore..."
    & $keytoolPath -import -trustcacerts -alias elasticache-ca `
      -file $elastiCachePem `
      -keystore $keyStore `
      -storepass $storePass -noprompt

    Write-Host "✅ ElastiCache certificate imported successfully into $keyStore"
} else {
    Write-Host "❌ Failed to download ElastiCacheRootCA1.pem"
}
