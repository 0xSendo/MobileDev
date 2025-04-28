package com.example.baseconverter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baseconverter.ui.theme.BaseConverterTheme
import kotlinx.coroutines.delay

class SettingsActivity : ComponentActivity() {
    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("logged_in_user") ?: "Unknown"

        // Handle device back button with double-press logic
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime <= BACK_PRESS_INTERVAL) {
                    finishAffinity() // Exit the app
                } else {
                    backPressedTime = currentTime
                    Toast.makeText(this@SettingsActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setContent {
            BaseConverterTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    SettingsScreen(
                        username = username,
                        onBackClick = { navigateToProfileScreen(username) },
                        onDeveloperClick = { navigateToDeveloperScreen(username) }
                    )
                }
            }
        }
    }

    private fun navigateToProfileScreen(username: String) {
        val intent = Intent(this, ProfileActivity::class.java).apply {
            putExtra("logged_in_user", username)
        }
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun navigateToDeveloperScreen(username: String) {
        val intent = Intent(this, DeveloperActivity::class.java).apply {
            putExtra("logged_in_user", username)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    username: String,
    onBackClick: () -> Unit,
    onDeveloperClick: () -> Unit
) {
    var isDarkThemeEnabled by remember { mutableStateOf(false) }
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var fontSize by remember { mutableStateOf("Medium") }
    var isFadingOut by remember { mutableStateOf(false) }

    // Define colors matching ProfileActivity
    val GradientStart = Color(0xFF800000) // Maroon
    val GradientEnd = Color(0xFFFFFFFF)   // White
    val CardBackground = Color.White.copy(alpha = 0.1f)
    val AccentColor = Color(0xFFFFCA28) // Yellow for highlights
    val TextColor = Color.White
    // Additional colors from original SettingsActivity
    val LightYellow = Color(0xFFFFF5E4)
    val LightPink = Color(0xFFFFC1CC)
    val Pink = Color(0xFFFFC1CC)
    val DarkGray = Color.DarkGray

    // Animation for fade-out
    val alpha by animateFloatAsState(
        targetValue = if (isFadingOut) 0f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "fade_animation"
    )

    // Trigger navigation after fade-out
    LaunchedEffect(isFadingOut) {
        if (isFadingOut) {
            delay(300) // Match animation duration
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Settings",
                        color = TextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButtonWithFade(
                        onClick = { isFadingOut = true }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.alpha(alpha) // Apply fade animation to entire scaffold
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(GradientStart, GradientEnd))
                )
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // Preferences Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Preferences",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextColor,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            // Dark Theme Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Dark Theme",
                                    fontSize = 16.sp,
                                    color = TextColor.copy(alpha = 0.8f)
                                )
                                Switch(
                                    checked = isDarkThemeEnabled,
                                    onCheckedChange = { isDarkThemeEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AccentColor,
                                        checkedTrackColor = AccentColor.copy(alpha = 0.5f),
                                        uncheckedThumbColor = TextColor,
                                        uncheckedTrackColor = TextColor.copy(alpha = 0.5f)
                                    )
                                )
                            }

                            // Notifications Toggle
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Enable Notifications",
                                    fontSize = 16.sp,
                                    color = TextColor.copy(alpha = 0.8f)
                                )
                                Switch(
                                    checked = isNotificationsEnabled,
                                    onCheckedChange = { isNotificationsEnabled = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = AccentColor,
                                        checkedTrackColor = AccentColor.copy(alpha = 0.5f),
                                        uncheckedThumbColor = TextColor,
                                        uncheckedTrackColor = TextColor.copy(alpha = 0.5f)
                                    )
                                )
                            }

                            // Font Size (Placeholder)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Font Size",
                                    fontSize = 16.sp,
                                    color = TextColor.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = fontSize,
                                    fontSize = 16.sp,
                                    color = AccentColor
                                )
                            }
                        }
                    }
                }

                // About Developer Section
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "About Developer",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextColor,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Text(
                                text = "Learn more about the developer behind this app.",
                                fontSize = 16.sp,
                                color = TextColor.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            ButtonWithSlide(
                                onClick = onDeveloperClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentColor,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = "View Developer Info",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Footer
            Text(
                text = "© 2025 Base Converter",
                fontSize = 14.sp,
                color = TextColor.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    BaseConverterTheme(darkTheme = true) {
        SettingsScreen(
            username = "PreviewUser",
            onBackClick = {},
            onDeveloperClick = {}
        )
    }
}
