package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.Repository
import com.example.ui.DashboardViewModel
import com.example.ui.DashboardViewModelFactory
import com.example.ui.screens.StudentOSLayout
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize SQLite Room database & Repository
        val database = AppDatabase.getDatabase(this)
        val repository = Repository(database)

        setContent {
            MyApplicationTheme {
                // Initialize Viewmodel with database repository
                val viewModel: DashboardViewModel = viewModel(
                    factory = DashboardViewModelFactory(repository)
                )

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    StudentOSLayout(viewModel = viewModel)
                }
            }
        }
    }
}

