sed -i 's/androidx.activity.ComponentActivity/androidx.fragment.app.FragmentActivity/' app/src/main/java/com/example/MainActivity.kt
sed -i 's/class MainActivity : ComponentActivity()/class MainActivity : FragmentActivity()/' app/src/main/java/com/example/MainActivity.kt
