package com.namma.santhe.data.model

data class Transaction(
    val id: Int = 0,
    val customerId: Int,
    val amount: Double,
    val type: String, // "UDARI" or "PAYMENT"
    val note: String = "",
    val date: Long = System.currentTimeMillis()
)
