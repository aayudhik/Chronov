sed -i 's/"chronova_secure_key_123".toByteArray()/privacyManager.getDatabasePassphrase().let { String(it).toByteArray() }/g' app/src/main/java/com/example/di/AppContainer.kt
