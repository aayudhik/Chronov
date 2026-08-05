sed -i '/implementation(libs.androidx.room.runtime)/a\
  implementation("net.zetetic:android-database-sqlcipher:4.5.4")\
  implementation("androidx.sqlite:sqlite-ktx:2.4.0")\
  implementation("androidx.biometric:biometric:1.1.0")' app/build.gradle.kts
