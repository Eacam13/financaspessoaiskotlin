package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.GeminiCategorizer
import com.example.data.AppDatabase
import com.example.data.FinanceRepository
import com.example.data.model.Budget
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class FinanceScreen {
    Dashboard,
    Transactions,
    Budgets
}

class FinanceViewModel(
    application: Application,
    private val repository: FinanceRepository
) : AndroidViewModel(application) {

    private val TAG = "FinanceViewModel"

    // Backing UI Screen navigation state
    private val _currentScreen = MutableStateFlow(FinanceScreen.Dashboard)
    val currentScreen: StateFlow<FinanceScreen> = _currentScreen.asStateFlow()

    // Observable Flows from Room
    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val budgets: StateFlow<List<Budget>> = repository.allBudgets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // AI Categorizer loading state
    private val _isCategorizing = MutableStateFlow(false)
    val isCategorizing: StateFlow<Boolean> = _isCategorizing.asStateFlow()

    // Dynamic Financial Summary metrics
    val totalIncome: StateFlow<Double> = transactions.map { list ->
        list.filter { !it.isExpense }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalExpense: StateFlow<Double> = transactions.map { list ->
        list.filter { it.isExpense }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val netBalance: StateFlow<Double> = combine(totalIncome, totalExpense) { income, expense ->
        income - expense
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // Grouped expenses per category for visual charts
    val expensesByCategory: StateFlow<Map<String, Double>> = transactions.map { list ->
        list.filter { it.isExpense }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Auto-seeding flag to prevent infinite loops
    init {
        viewModelScope.launch {
            // Check if databases are empty and seed default beautiful visual metrics
            transactions.collectLatest { list ->
                if (list.isEmpty()) {
                    seedDefaultData()
                }
            }
        }
    }

    private suspend fun seedDefaultData() {
        Log.d(TAG, "Database is empty. Seeding default transactions and budget guidelines...")
        
        // Seed default budget limits
        val defaultBudgets = listOf(
            Budget("Alimentação", 1200.0),
            Budget("Transporte", 500.0),
            Budget("Lazer", 400.0),
            Budget("Moradia", 2200.0),
            Budget("Compras", 600.0),
            Budget("Saúde", 300.0)
        )
        for (b in defaultBudgets) {
            repository.insertBudget(b)
        }

        // Seed default historical transactions
        val defaultTransactions = listOf(
            Transaction(
                description = "Salário Líquido",
                amount = 6200.00,
                isExpense = false,
                category = "Renda",
                timestamp = System.currentTimeMillis() - 86400000 * 5 // 5 days ago
            ),
            Transaction(
                description = "Trabalho Extra Freelance",
                amount = 850.00,
                isExpense = false,
                category = "Renda",
                timestamp = System.currentTimeMillis() - 86400000 * 2 // 2 days ago
            ),
            Transaction(
                description = "Supermercado Mensal",
                amount = 450.50,
                isExpense = true,
                category = "Alimentação",
                timestamp = System.currentTimeMillis() - 86400000 * 4
            ),
            Transaction(
                description = "Reserva Almoço Especial",
                amount = 85.90,
                isExpense = true,
                category = "Alimentação",
                timestamp = System.currentTimeMillis() - 86400000 * 1
            ),
            Transaction(
                description = "Corrida de Uber",
                amount = 34.20,
                isExpense = true,
                category = "Transporte",
                timestamp = System.currentTimeMillis() - 86400000 * 3
            ),
            Transaction(
                description = "Mensalidade do Streaming",
                amount = 55.90,
                isExpense = true,
                category = "Lazer",
                timestamp = System.currentTimeMillis() - 86400000 * 3
            ),
            Transaction(
                description = "Conta de Luz Coletiva",
                amount = 180.00,
                isExpense = true,
                category = "Moradia",
                timestamp = System.currentTimeMillis() - 86400000 * 4
            ),
            Transaction(
                description = "Livros de Programação",
                amount = 120.00,
                isExpense = true,
                category = "Educação",
                timestamp = System.currentTimeMillis() - 86400000 * 1
            )
        )
        for (t in defaultTransactions) {
            repository.insertTransaction(t)
        }
        Log.d(TAG, "Data completed seeding successfully.")
    }

    // Navigation action handlers
    fun setScreen(screen: FinanceScreen) {
        _currentScreen.value = screen
    }

    // Database Actions
    fun addTransaction(description: String, amount: Double, isExpense: Boolean, category: String) {
        viewModelScope.launch {
            val transaction = Transaction(
                description = description,
                amount = amount,
                isExpense = isExpense,
                category = category,
                timestamp = System.currentTimeMillis()
            )
            repository.insertTransaction(transaction)
        }
    }

    fun deleteTransaction(id: Int) {
        viewModelScope.launch {
            repository.deleteTransactionById(id)
        }
    }

    fun setBudgetLimit(category: String, amount: Double) {
        viewModelScope.launch {
            val budget = Budget(category = category, limitAmount = amount)
            repository.insertBudget(budget)
        }
    }

    fun removeBudgetByCategory(category: String) {
        viewModelScope.launch {
            repository.deleteBudgetByCategory(category)
        }
    }

    /**
     * Calls Gemini automatic categorization API on a background thread.
     * Triggers loading indicator `isCategorizing` during progress.
     */
    suspend fun getSuggestedCategory(description: String): String {
        _isCategorizing.value = true
        return try {
            val suggestion = GeminiCategorizer.categorize(description)
            _isCategorizing.value = false
            suggestion
        } catch (e: Exception) {
            _isCategorizing.value = false
            "Outros"
        }
    }
}

// Custom ViewModel Factory
class FinanceViewModelFactory(
    private val application: Application,
    private val repository: FinanceRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FinanceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FinanceViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
