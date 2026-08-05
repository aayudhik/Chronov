sed -i 's/Text(/Text(/' app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt
sed -i 's/"${collection.memoryCount} items"/"${collection.memoryCount} items • ${collection.distinctLocations} places"/' app/src/main/java/com/example/ui/screens/memories/MemoriesScreen.kt
