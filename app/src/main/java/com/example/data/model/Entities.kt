package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val description: String,
    val amount: Double,
    val isExpense: Boolean, // true = expense, false = income
    val category: String, // e.g. Alimentação, Transporte, Lazer, etc.
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val category: String, // E.g. "Alimentação", "Transporte"
    val limitAmount: Double
) : Serializable
