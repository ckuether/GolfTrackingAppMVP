package com.example.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class GolfClubType(val clubName: String, val shortName: String, val range: Pair<Int, Int>? = null) {

    Driver("Driver", "Dr",Pair(250, 300)),
    Wood_3("3 Wood", "3w", Pair(220,250)),
    Hybrid_3("3 Hybrid", "3Hy", Pair(200,220)),
    Iron_4("4 Iron", "4i", Pair(185,200)),
    Iron_5("5 Iron", "5i", Pair(170,185)),
    Iron_6("6 Iron", "6i", Pair(155,170)),
    Iron_7("7 Iron", "7i", Pair(140,155)),
    Iron_8("8 Iron", "8i", Pair(125,140)),
    Iron_9("9 Iron", "9i", Pair(110,125)),
    Pitch("Pitch", "Pi", Pair(170,200)),
    Wedge_54("54 Wedge", "54", Pair(20, 65)),
    Putter("Putter", "Put", Pair(0, 20))
}