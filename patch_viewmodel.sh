sed -i 's/monthCounts\[index\]! \* 15.5f/(monthCounts\[index\] ?: 0).toFloat() \* 15.5f/g' app/src/main/java/com/example/ui/screens/insights/InsightsViewModel.kt
