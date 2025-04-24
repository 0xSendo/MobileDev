package com.example.baseconverter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baseconverter.ui.theme.BaseConverterTheme
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api

class ProfileActivity : ComponentActivity() {
    private lateinit var databaseManager: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("logged_in_user") ?: "Unknown"
        databaseManager = DatabaseManager(this)

        setContent {
            BaseConverterTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF98FB98)
                ) {
                    ProfileScreen(
                        username = username,
                        databaseManager = databaseManager,
                        onLogoutConfirmed = { navigateToLoginScreen() },
                        onBackClick = { onBackPressedDispatcher.onBackPressed() },
                        onNotesClick = { navigateToNotesPage(username) },
                        onHistoryClick = { navigateToHistoryPage(username) }
                    )
                }
            }
        }
    }

    private fun navigateToLoginScreen() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun navigateToHistoryPage(username: String) {
        startActivity(
            Intent(this, HistoryActivity::class.java)
                .putExtra("logged_in_user", username)
        )
    }

    private fun navigateToNotesPage(username: String) {
        startActivity(
            Intent(this, NotesActivity::class.java)
                .putExtra("logged_in_user", username)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    username: String,
    databaseManager: DatabaseManager,
    onLogoutConfirmed: () -> Unit,
    onBackClick: () -> Unit,
    onNotesClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val LightYellow = Color(0xFFFFF5E4)
    val Maroon = Color(0xFF660000)
    val Pink = Color(0xFFFFC1CC)

    LaunchedEffect(username) {
        try {
            databaseManager.getUserDetails(username)?.let { details ->
                email = details.second
            } ?: run {
                email = "No email available"
                Log.w("ProfileScreen", "No user details found for $username")
            }
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Error loading user details for $username", e)
            email = "Error loading email"
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout Confirmation", color = Color.DarkGray) },
            text = { Text("Are you sure you want to logout?", color = Color.DarkGray) },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogoutConfirmed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Maroon)
                ) {
                    Text("Yes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("No", color = Pink)
                }
            },
            containerColor = Color.White.copy(alpha = 0.7f)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(username, color = Color.DarkGray, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Maroon)
                    }
                },
                actions = {
                    IconButton(onClick = { /* Settings? */ }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Maroon)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White.copy(alpha = 0.7f))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightYellow)
                .padding(padding)
                .padding(16.dp)
        ) {
            Column {
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_background),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(Color.Gray),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(email, style = MaterialTheme.typography.bodyLarge, color = Maroon)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { /* Edit profile */ },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Maroon)
                ) {
                    Text("Edit Profile", fontSize = 18.sp, color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clickable(onClick = onNotesClick),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Notes", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .clickable(onClick = onHistoryClick),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.7f))
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("History", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("© 2025 Base Converter", modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}
