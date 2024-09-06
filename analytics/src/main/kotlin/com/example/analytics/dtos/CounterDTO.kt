package com.example.analytics.dtos

import com.example.analytics.entities.CounterEntity

data class CounterDTO(
    var count: Long
)

fun CounterEntity.toDTO() : CounterDTO = CounterDTO(
    this.count
)