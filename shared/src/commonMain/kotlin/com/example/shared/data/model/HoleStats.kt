package com.example.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class HoleStats(
    var score: Int? = null,
    var putts: Int? = null
)