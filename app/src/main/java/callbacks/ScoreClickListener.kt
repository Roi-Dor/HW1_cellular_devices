package callbacks

interface ScoreClickListener {
    fun onScoreSelected(location: Pair<Double, Double>)
}