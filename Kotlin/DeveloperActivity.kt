package com.example.baseconverter

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baseconverter.ui.theme.BaseConverterTheme
import kotlinx.coroutines.delay


class DeveloperActivity : ComponentActivity() {
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
                    navigateToProfileScreen(username)
                } else {
                    backPressedTime = currentTime
                    Toast.makeText(this@DeveloperActivity, "Press back again to return to Profile", Toast.LENGTH_SHORT).show()
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
                    DeveloperScreen(
                        username = username,
                        onBackClick = { navigateToProfileScreen(username) }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(
    username: String,
    onBackClick: () -> Unit
) {
    var isFadingOut by remember { mutableStateOf(false) }

    // Colors matching ProfileActivity and SettingsActivity
    val GradientStart = Color(0xFF800000) // Maroon
    val GradientEnd = Color(0xFFFFFFFF)   // White
    val CardBackground = Color.White.copy(alpha = 0.05f) // Defined here
    val AccentColor = Color(0xFFFFCA28) // Yellow
    val TextColor = Color.White
    // Compatibility colors from original DeveloperActivity
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
            delay(300)
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "About Developer",
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
        modifier = Modifier.alpha(alpha)
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
                // Team Icon
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                    }
                }

                // Meet the Team Header
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(listOf(GradientStart, GradientEnd)),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MEET THE TEAM",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextColor,
                            fontSize = 20.sp
                        )
                    }
                }

                // Team Members
                item {
                    TeamMember(
                        profileImage = R.drawable.icon_1,
                        name = "Cudera, Xianne Jewel S.",
                        bio = "A 'fake it till you make it' girly and runs by the motto 'it is what it is'.",
                        funFact = "Loves sleeping and has a hidden talent for playing the guitar!"
                    )
                }

                item {
                    TeamMember(
                        profileImage = R.drawable.icon_2,
                        name = "Daal, Wyben C.",
                        bio = "An experienced frontend developer who thrives on solving complex problems and optimizing performance.",
                        funFact = "Has a black belt in martial arts and is a coffee connoisseur."
                    )
                }

                // Vision Header
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(listOf(GradientStart, GradientEnd)),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VISION",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextColor,
                            fontSize = 20.sp
                        )
                    }
                }

                // Vision Statement
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBackground, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "\"Innovating the future, one step at a time\"",
                            style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                            color = Pink,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Our team is dedicated to building innovative and user-friendly solutions that make a difference in people's lives. We believe in continuous learning, collaboration, and striving for excellence in everything we do.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = DarkGray,
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp
                        )
                    }
                }

                // Mission Header
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(listOf(GradientStart, GradientEnd)),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "MISSION",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextColor,
                            fontSize = 20.sp
                        )
                    }
                }

                // Mission Statement
                item {
                    Text(
                        text = "To empower users with tools that simplify their lives and foster creativity through technology.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DarkGray,
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CardBackground, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    )
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
fun TeamMember(profileImage: Int, name: String, bio: String, funFact: String) {
    val CardBackground = Color.Gray
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBackground, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AnimatedImage(
            painter = painterResource(id = profileImage),
            contentDescription = "Profile Picture of $name",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.Gray),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = bio,
                style = MaterialTheme.typography.bodyMedium,
                color = DarkGray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Fun Fact: $funFact",
                style = MaterialTheme.typography.bodySmall,
                color = DarkGray,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun AnimatedImage(
    painter: androidx.compose.ui.graphics.painter.Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale
) {
    var isAnimating by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 1.2f else 1f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "image_scale"
    )
    val rotation by animateFloatAsState(
        targetValue = if (isAnimating) 360f else 0f,
        animationSpec = tween(durationMillis = 600, easing = LinearEasing),
        label = "image_rotation"
    )

    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            repeat(2) {
                delay(600)
            }
            isAnimating = false
        }
    }

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier
            .scale(scale)
            .rotate(rotation)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { isAnimating = true }
                )
            },
        contentScale = contentScale
    )
}

@Preview(showBackground = true)
@Composable
fun DeveloperScreenPreview() {
    BaseConverterTheme(darkTheme = true) {
        DeveloperScreen(
            username = "PreviewUser",
            onBackClick = {}
        )
    }
}
