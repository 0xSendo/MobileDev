package com.example.baseconverter

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baseconverter.ui.theme.BaseConverterTheme

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("logged_in_user") ?: "Unknown"
        setContent {
            BaseConverterTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF98FB98) // PaleGreen background
                ) {
                    HistoryScreen(
                        username = username,
                        onBackClick = { onBackPressedDispatcher.onBackPressed() },
                        databaseManager = DatabaseManager(this)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    username: String,
    onBackClick: () -> Unit,
    databaseManager: DatabaseManager
) {
    val historyList = remember { mutableStateListOf<DatabaseManager.ConversionEntry>() }

    // Colors matching your theme
    val LightYellow = Color(0xFFFFF5E4)
    val Maroon = Color(0xFF660000)
    val LightRed = Color(0xFFFFA8A8)
    val Pink = Color(0xFFFFC1CC)

    // Fetch conversion history with error handling
    LaunchedEffect(Unit) {
        try {
            val historyFromDB = databaseManager.getConversionHistory(username)
            historyList.clear()
            historyList.addAll(historyFromDB)
            if (historyFromDB.isEmpty()) {
                Log.w("HistoryScreen", "No conversion history found for $username")
            }
        } catch (e: Exception) {
            Log.e("HistoryScreen", "Error loading conversion history for $username", e)
            historyList.clear() // Ensure list is empty on error
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Conversion History", color = Color.DarkGray) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Maroon
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        try {
                            databaseManager.clearConversionHistory(username)
                            historyList.clear()
                        } catch (e: Exception) {
                            Log.e("HistoryScreen", "Error clearing conversion history", e)
                        }
                    }) {
                        Text("Delete All", color = Pink)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.7f))
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Gray) // Placeholder background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (historyList.isEmpty()) {
                    Text(
                        text = "No conversion history available",
                        color = Color.DarkGray,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(historyList) { item ->
                            HistoryItem(conversion = item)
                        }
                    }
                }
            }

            Text(
                text = "© 2025 Base Converter",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                fontSize = 12.sp,
                color = Color(0xFF0A1F44)
            )
        }
    }
}

@Composable
fun HistoryItem(conversion: DatabaseManager.ConversionEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${conversion.inputValue} (Base ${conversion.inputBase}) → ${conversion.outputValue} (Base ${conversion.outputBase})",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.DarkGray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Timestamp: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(conversion.timestamp))}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    BaseConverterTheme(darkTheme = true) {
        val mockDatabaseManager = object : DatabaseManager(null) {
            override fun getConversionHistory(username: String) = listOf(
                ConversionEntry("123", 10, "7B", 16, System.currentTimeMillis()),
                ConversionEntry("1010", 2, "A", 16, System.currentTimeMillis() - 100000)
            )
        }
        HistoryScreen(
            username = "PreviewUser",
            onBackClick = {},
            databaseManager = mockDatabaseManager
        )
    }
}
