package callbacks

interface GameUpdateCallback {
    fun onScoreUpdate(score: Int)
    fun onLivesUpdate(lives: Int)
    fun onGameOver(score: Int)
}