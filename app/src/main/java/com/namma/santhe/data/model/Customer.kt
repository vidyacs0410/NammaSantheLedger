package com.namma.santhe.data.model

data class Customer(
    val id: Int = 0,
    val name: String,
    val phone: String,
    val createdAt: Long = System.currentTimeMillis()
)
