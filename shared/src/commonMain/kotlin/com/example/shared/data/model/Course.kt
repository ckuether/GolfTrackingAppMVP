package com.example.shared.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val id: Long,
    val name: String,
    val holes: Map<Int, Hole>
)

val Course.parMap: Map<Int, Int>
    get() = holes.mapValues { (_, hole) ->
        hole.par
    }