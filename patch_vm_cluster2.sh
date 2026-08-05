cat << 'INNER_EOF' >> app/src/main/java/com/example/ui/screens/map/MapViewModel.kt

private fun clusterMemories(memories: List<MemoryWithMedia>): List<MapCluster> {
    val clusters = mutableListOf<MapCluster>()
    val threshold = 1.0 // Degrees
    for (mem in memories) {
        val lat = mem.memory.latitude ?: continue
        val lon = mem.memory.longitude ?: continue
        var added = false
        for (i in clusters.indices) {
            val cluster = clusters[i]
            val dLat = cluster.latitude - lat
            val dLon = cluster.longitude - lon
            if (dLat * dLat + dLon * dLon < threshold * threshold) {
                val newMemories = cluster.memories + mem
                val avgLat = newMemories.map { it.memory.latitude!! }.average()
                val avgLon = newMemories.map { it.memory.longitude!! }.average()
                clusters[i] = MapCluster(avgLat, avgLon, newMemories)
                added = true
                break
            }
        }
        if (!added) {
            clusters.add(MapCluster(lat, lon, listOf(mem)))
        }
    }
    return clusters
}
INNER_EOF
