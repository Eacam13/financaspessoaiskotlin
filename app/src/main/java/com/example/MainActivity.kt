package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.FinanceRepository
import com.example.ui.FinanceScreen
import com.example.ui.FinanceViewModel
import com.example.ui.FinanceViewModelFactory
import com.example.ui.screens.BudgetsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable Edge-to-Edge display (notch and system navigation bar integration)
        enableEdgeToEdge()
        
        // Initialize Room Database singletons
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FinanceRepository(database.transactionDao(), database.budgetDao())
        
        // Instantiate factory ViewModel
        val factory = FinanceViewModelFactory(application, repository)
        val viewModel = ViewModelProvider(this, factory)[FinanceViewModel::class.java]
        
        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(viewModel: FinanceViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val budgets by viewModel.budgets.collectAsState()
    
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Finanças Pessoais",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Box(
                            modifier = androidx.compose.ui.Modifier
                                .padding(horizontal = 4.dp)
                                .size(24.dp)
                        ) {
                            Text("🌱", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Nav 1: Dashboard
                NavigationBarItem(
                    selected = currentScreen == FinanceScreen.Dashboard,
                    onClick = { viewModel.setScreen(FinanceScreen.Dashboard) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home index") },
                    label = { Text("Resumo") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_dashboard")
                )
                
                // Nav 2: Transactions
                NavigationBarItem(
                    selected = currentScreen == FinanceScreen.Transactions,
                    onClick = { viewModel.setScreen(FinanceScreen.Transactions) },
                    icon = { Icon(Icons.Default.List, contentDescription = "Receipt transaction register") },
                    label = { Text("Transações") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_transactions")
                )
                
                // Nav 3: Budgets
                NavigationBarItem(
                    selected = currentScreen == FinanceScreen.Budgets,
                    onClick = { viewModel.setScreen(FinanceScreen.Budgets) },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Stars budgets criteria") },
                    label = { Text("Orçamentos") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.testTag("nav_budgets")
                )
            }
        }
    ) { innerPadding ->
        // Smooth fade transactions between content screen switches
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                FinanceScreen.Dashboard -> {
                    DashboardScreen(
                        viewModel = viewModel,
                        transactions = transactions,
                        budgets = budgets
                    )
                }
                FinanceScreen.Transactions -> {
                    TransactionsScreen(
                        viewModel = viewModel,
                        transactions = transactions
                    )
                }
                FinanceScreen.Budgets -> {
                    BudgetsScreen(
                        viewModel = viewModel,
                        budgets = budgets
                    )
                }
            }
        }
    }
}
