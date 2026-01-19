package com.example.miraclemorning.utils

object QuoteProvider {
    private val quotes = listOf(
        "그저 첫 발걸음을 떼면 됩니다. 계단 전체를 올려다볼 필요도 없습니다. 그저 첫 발걸음만 떼면 됩니다.",
        "다른 누군가가 이룬 꿈은 나도 얼마든지 이룰 수 있다.",
        "당신이 할 수 있다고 믿든, 할 수 없다고 믿든, 믿는 대로 될 것이다.",
        "매일을 당신의 걸작으로 만드세요.",
        "인생은 자전거 타기와 같다. 균형을 잡으려면 계속 움직여야 한다."
    )

    fun getRandomQuote(): String {
        return quotes.random()
    }
}

