package com.example.analytics.entities

import jakarta.persistence.*

@Entity
class CounterEntity(s: String, i: Long) {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    var id: Long = 0

    @Column(unique = true)
    var type: String = s
    var count: Long = i
}