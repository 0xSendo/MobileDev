package com.example.baseconverter

import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
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
                        onDeveloperClick = { navigateToDeveloperScreen(username) },
                        onLogoutConfirmed = { navigateToLoginScreen() }
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

    private fun navigateToLoginScreen() {
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        sharedPreferences.edit().remove("logged_in_user").apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    username: String,
    onBackClick: () -> Unit,
    onDeveloperClick: () -> Unit,
    onLogoutConfirmed: () -> Unit
) {
    var isDarkThemeEnabled by remember { mutableStateOf(false) }
    var isNotificationsEnabled by remember { mutableStateOf(true) }
    var fontSize by remember { mutableStateOf("Medium") }
    var isFadingOut by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showFontSizeDropdown by remember { mutableStateOf(false) }

    // Define colors matching ProfileActivity
    val GradientStart = Color(0xFF800000) // Maroon
    val GradientEnd = Color(0xFFFFFFFF)   // White
    val CardBackground = Color.White.copy(alpha = 0.05f) // Higher transparency
    val AccentColor = Color(0xFFFFCA28) // Yellow for highlights
    val TextColor = Color.White
    // Additional colors for compatibility
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

    // Logout dialog
    if (showLogoutDialog) {
        Dialog(onDismissRequest = { showLogoutDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = CardBackground,
                modifier = Modifier
                    .width(280.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Logout Confirmation",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextColor
                    )
                    Text(
                        text = "Are you sure you want to logout?",
                        fontSize = 14.sp,
                        color = TextColor.copy(alpha = 0.8f)
                    )
                    ButtonWithSlide(
                        onClick = {
                            showLogoutDialog = false
                            onLogoutConfirmed()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentColor,
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Yes", fontSize = 14.sp)
                    }
                    TextButton(
                        onClick = { showLogoutDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("No", color = AccentColor, fontSize = 14.sp)
                    }
                }
            }
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
                        fontSize = 20.sp
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
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Preferences Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBackground, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Preferences",
                                tint = AccentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Preferences",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextColor
                            )
                        }

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
                                color = TextColor.copy(alpha = 0.9f)
                            )
                            AnimatedSwitch(
                                checked = isDarkThemeEnabled,
                                onCheckedChange = { isDarkThemeEnabled = it },
                                accentColor = AccentColor,
                                textColor = TextColor
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
                                color = TextColor.copy(alpha = 0.9f)
                            )
                            AnimatedSwitch(
                                checked = isNotificationsEnabled,
                                onCheckedChange = { isNotificationsEnabled = it },
                                accentColor = AccentColor,
                                textColor = TextColor
                            )
                        }

                        // Font Size Selector
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
                                color = TextColor.copy(alpha = 0.9f)
                            )
                            Box {
                                TextButton(
                                    onClick = { showFontSizeDropdown = true },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(
                                        text = fontSize,
                                        fontSize = 16.sp,
                                        color = AccentColor
                                    )
                                }
                                DropdownMenu(
                                    expanded = showFontSizeDropdown,
                                    onDismissRequest = { showFontSizeDropdown = false }
                                ) {
                                    listOf("Small", "Medium", "Large").forEach { size ->
                                        DropdownMenuItem(
                                            text = { Text(size, color = TextColor) },
                                            onClick = {
                                                fontSize = size
                                                showFontSizeDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Divider(
                        color = TextColor.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                // About Developer Section
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBackground, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Developer",
                                tint = AccentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "About Developer",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextColor
                            )
                        }
                        Text(
                            text = "Learn more about the developer behind this app.",
                            fontSize = 14.sp,
                            color = TextColor.copy(alpha = 0.9f),
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
                                .height(48.dp)
                        ) {
                            Text(
                                text = "View Developer Info",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Divider(
                        color = TextColor.copy(alpha = 0.2f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                // Account Section (Logout)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBackground, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Account",
                                tint = AccentColor,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Account",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextColor
                            )
                        }
                        ButtonWithSlide(
                            onClick = { showLogoutDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentColor,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(
                                text = "Logout",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
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

@Composable
fun AnimatedSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    accentColor: Color,
    textColor: Color
) {
    val scale by animateFloatAsState(
        targetValue = if (checked) 1.1f else 1f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "switch_scale"
    )

    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = accentColor,
            checkedTrackColor = accentColor.copy(alpha = 0.5f),
            uncheckedThumbColor = textColor,
            uncheckedTrackColor = textColor.copy(alpha = 0.5f)
        ),
        modifier = Modifier.scale(scale)
    )
}
