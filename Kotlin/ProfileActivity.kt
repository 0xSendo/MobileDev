package com.example.baseconverter

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.baseconverter.ui.theme.BaseConverterTheme
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween

class ProfileActivity : ComponentActivity() {
    private lateinit var databaseManager: DatabaseManager
    private lateinit var sharedPreferences: SharedPreferences
    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("logged_in_user") ?: "Unknown"
        databaseManager = DatabaseManager(this)
        sharedPreferences = getSharedPreferences("ProfilePrefs", MODE_PRIVATE)

        // Handle phone's back button press
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime <= BACK_PRESS_INTERVAL) {
                    finishAffinity() // Exit the app
                } else {
                    backPressedTime = currentTime
                    Toast.makeText(this@ProfileActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
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
                    ProfileScreen(
                        username = username,
                        databaseManager = databaseManager,
                        sharedPreferences = sharedPreferences,
                        onLogoutConfirmed = { navigateToLoginScreen() },
                        onBackClick = { navigateToLandingScreen(username) },
                        onSettingsClick = { navigateToSettingsScreen(username) }
                    )
                }
            }
        }
    }

    private fun navigateToLoginScreen() {
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        sharedPreferences.edit().remove("logged_in_user").apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun navigateToLandingScreen(username: String) {
        val intent = Intent(this, LandingPageActivity::class.java).apply {
            putExtra("logged_in_user", username)
        }
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun navigateToSettingsScreen(username: String) {
        val intent = Intent(this, SettingsActivity::class.java).apply {
            putExtra("logged_in_user", username)
        }
        startActivity(intent)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    username: String,
    databaseManager: DatabaseManager,
    sharedPreferences: SharedPreferences,
    onLogoutConfirmed: () -> Unit,
    onBackClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var userDetails by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var isEditing by remember { mutableStateOf(false) }
    var editedUsername by remember { mutableStateOf("") }
    var editedEmail by remember { mutableStateOf("") }
    var editedFirstName by remember { mutableStateOf("") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var profileImageUri by remember { mutableStateOf<String?>(null) }

    // Define colors for the design
    val GradientStart = Color(0xFF800000) // Maroon
    val GradientEnd = Color(0xFFFFFFFF)   // White
    val CardBackground = Color.White.copy(alpha = 0.1f)
    val AccentColor = Color(0xFFFFCA28) // Yellow for highlights
    val TextColor = Color.White

    // Load user details
    LaunchedEffect(username) {
        try {
            databaseManager.getUserDetails(username)?.let { details ->
                userDetails = details
                editedUsername = details.first
                editedEmail = details.second
                editedFirstName = details.third
            } ?: run {
                userDetails = Triple("Unknown", "No email available", "Unknown")
                editedUsername = "Unknown"
                editedEmail = "No email available"
                editedFirstName = "Unknown"
                Log.w("ProfileScreen", "No user details found for $username")
            }
        } catch (e: Exception) {
            Log.e("ProfileScreen", "Error loading user details for $username", e)
            userDetails = Triple("Error", "Error loading email", "Error")
            editedUsername = "Error"
            editedEmail = "Error loading email"
            editedFirstName = "Error"
        }
    }

    // Load profile picture URI from SharedPreferences
    LaunchedEffect(Unit) {
        profileImageUri = sharedPreferences.getString("profile_image_uri_$username", null)
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            profileImageUri = it.toString()
            // Save the URI to SharedPreferences
            sharedPreferences.edit()
                .putString("profile_image_uri_$username", it.toString())
                .apply()
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
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Logout Confirmation",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextColor
                    )
                    Text(
                        text = "Are you sure you want to logout?",
                        fontSize = 16.sp,
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
                        Text("Yes", fontSize = 16.sp)
                    }
                    TextButton(
                        onClick = { showLogoutDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("No", color = AccentColor, fontSize = 16.sp)
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
                        "Profile",
                        color = TextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButtonWithFade(
                        onClick = onBackClick
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextColor
                        )
                    }
                },
                actions = {
                    IconButtonWithSlide(
                        onClick = onSettingsClick
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
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
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Profile Picture with Upload Option
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .shadow(8.dp, CircleShape)
                        .clickable(enabled = isEditing) { if (isEditing) imagePickerLauncher.launch("image/*") }
                ) {
                    if (profileImageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(profileImageUri),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_background),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    // Overlay to indicate clickable, shown only when editing
                    if (isEditing) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Upload",
                                color = AccentColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // User Information Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardBackground)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (isEditing) {
                            // Editable Fields
                            OutlinedTextField(
                                value = editedUsername,
                                onValueChange = { editedUsername = it },
                                label = { Text("Username", color = TextColor) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "Username",
                                        tint = AccentColor
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = AccentColor,
                                    unfocusedBorderColor = TextColor.copy(alpha = 0.5f),
                                    cursorColor = AccentColor
                                )
                            )
                            OutlinedTextField(
                                value = editedEmail,
                                onValueChange = { editedEmail = it },
                                label = { Text("Email", color = TextColor) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Email,
                                        contentDescription = "Email",
                                        tint = AccentColor
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = AccentColor,
                                    unfocusedBorderColor = TextColor.copy(alpha = 0.5f),
                                    cursorColor = AccentColor
                                )
                            )
                            OutlinedTextField(
                                value = editedFirstName,
                                onValueChange = { editedFirstName = it },
                                label = { Text("First Name", color = TextColor) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = "First Name",
                                        tint = AccentColor
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = AccentColor,
                                    unfocusedBorderColor = TextColor.copy(alpha = 0.5f),
                                    cursorColor = AccentColor
                                )
                            )
                        } else {
                            // Display Fields
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "Username",
                                    tint = AccentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Username",
                                        fontSize = 14.sp,
                                        color = TextColor.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = userDetails?.first ?: "Loading...",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextColor
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = "Email",
                                    tint = AccentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Email",
                                        fontSize = 14.sp,
                                        color = TextColor.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = userDetails?.second ?: "Loading...",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextColor
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = "First Name",
                                    tint = AccentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "First Name",
                                        fontSize = 14.sp,
                                        color = TextColor.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = userDetails?.third ?: "Loading...",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextColor
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Edit/Save Button
                if (isEditing) {
                    ButtonWithSlide(
                        onClick = {
                            // Save changes to database
                            databaseManager.updateUserDetails(
                                username,
                                editedUsername,
                                editedEmail,
                                editedFirstName
                            )
                            // Update userDetails to reflect changes
                            userDetails = Triple(editedUsername, editedEmail, editedFirstName)
                            isEditing = false
                        },
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
                            "Save",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    ButtonWithSlide(
                        onClick = { isEditing = true },
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
                            "Edit Profile",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Logout Button
                ButtonWithSlide(
                    onClick = { showLogoutDialog = true },
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
                        "Logout",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Footer
                Text(
                    text = "© 2025 Base Converter",
                    fontSize = 14.sp,
                    color = TextColor.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 16.dp)
                )
            }
        }
    }
}

@Composable
fun IconButtonWithSlide(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isClicked by remember { mutableStateOf(false) }
    val offsetX by animateFloatAsState(
        targetValue = if (isClicked) 300f else 0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "slide_animation"
    )

    LaunchedEffect(isClicked) {
        if (isClicked) {
            kotlinx.coroutines.delay(300)
            onClick()
            isClicked = false // Reset the state after animation
        }
    }

    IconButton(
        onClick = { isClicked = true },
        modifier = modifier.offset(x = offsetX.dp)
    ) {
        content()
    }
}
