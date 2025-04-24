package com.example.baseconverter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.baseconverter.ui.theme.BaseConverterTheme
import kotlinx.coroutines.launch
import java.util.*

// Define colors for the new design
val GradientStart = Color(0xFF800000) // Maroon
val GradientEnd = Color(0xFFFFFFFF)   // White
val FrostedBackground = Color.White.copy(alpha = 0.1f)
val AccentColor = Color(0xFFFFCA28) // Yellow for highlights

class LandingPageActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BaseConverterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    val username = intent.getStringExtra("logged_in_user") ?: "User"
                    LandingPage(
                        username = username,
                        onBaseConvertClick = { navigateToBaseConverter(username) }
                    )
                }
            }
        }
    }

    private fun navigateToBaseConverter(username: String) {
        val intent = Intent(this, BaseConverterActivity::class.java)
        intent.putExtra("logged_in_user", username)
        startActivity(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingPage(
    username: String,
    onBaseConvertClick: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 5..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }
    var showConversionDialog by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerContent = {
            DrawerContent(username = username, onClose = { scope.launch { drawerState.close() } })
        },
        drawerState = drawerState
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "$greeting, $username",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "Open टीमenu",
                                tint = Color.White
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomNavigationBar(username = username)
            },
            floatingActionButton = {
                val scale by animateFloatAsState(
                    targetValue = if (showConversionDialog) 1.2f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
                )
                FloatingActionButton(
                    onClick = { showConversionDialog = true },
                    containerColor = AccentColor,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier
                        .size(60.dp)
                        .scale(scale)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Start Conversion")
                }
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(GradientStart, GradientEnd))
                    )
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        HeroSection(onBaseConvertClick)
                    }
                    item {
                        FeaturesSection()
                    }
                    item {
                        TestimonialSection()
                    }
                }
            }
        }
    }

    if (showConversionDialog) {
        CompactConversionDialog(
            onDismiss = { showConversionDialog = false },
            onBaseConvertClick = {
                showConversionDialog = false
                onBaseConvertClick()
            }
        )
    }
}

@Composable
fun DrawerContent(username: String, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FrostedBackground)
            .padding(16.dp)
    ) {
        Text(
            text = "Menu",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(8.dp))
        DrawerItem("Profile", Icons.Default.Person) {
            val context = LocalContext.current
            Intent(context, ProfileActivity::class.java).apply {
                putExtra("logged_in_user", username)
                context.startActivity(this)
            }
            onClose()
        }
        DrawerItem("Notes", Icons.Default.Note) {
            val context = LocalContext.current
            Intent(context, NotesActivity::class.java).apply {
                putExtra("logged_in_user", username)
                context.startActivity(this)
            }
            onClose()
        }
        DrawerItem("History", Icons.Default.History) {
            val context = LocalContext.current
            Intent(context, HistoryActivity::class.java).apply {
                putExtra("logged_in_user", username)
                context.startActivity(this)
            }
            onClose()
        }
        DrawerItem("Logout", Icons.Default.ExitToApp) {
            val context = LocalContext.current
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                context.startActivity(this)
            }
            (context as? ComponentActivity)?.finish()
            onClose()
        }
    }
}

@Composable
fun DrawerItem(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: @Composable () -> Unit) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 200)
    )
    ListItem(
        headlineContent = { Text(text, color = Color.White, fontSize = 16.sp) },
        leadingContent = {
            Icon(icon, contentDescription = text, tint = AccentColor)
        },
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .scale(scale)
            .background(FrostedBackground)
    )
}

@Composable
fun BottomNavigationBar(username: String) {
    val context = LocalContext.current

    NavigationBar(
        containerColor = FrostedBackground,
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.White) },
            label = { Text("Home", color = Color.White) },
            selected = true, // Home is the current screen
            onClick = { /* Already on Home, no action needed */ }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White) },
            label = { Text("Profile", color = Color.White) },
            selected = false,
            onClick = {
                Intent(context, ProfileActivity::class.java).apply {
                    putExtra("logged_in_user", username)
                    context.startActivity(this)
                }
            }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Default.History, contentDescription = "History", tint = Color.White) },
            label = { Text("History", color = Color.White) },
            selected = false,
            onClick = {
                Intent(context, HistoryActivity::class.java).apply {
                    putExtra("logged_in_user", username)
                    context.startActivity(this)
                }
            }
        )
    }
}

@Composable
fun HeroSection(onBaseConvertClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FrostedBackground)
            .padding(20.dp)
            .clickable { onBaseConvertClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Convert Numbers Effortlessly",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Start converting between bases 2-36 instantly!",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onBaseConvertClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Start Now", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun FeaturesSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FrostedBackground)
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Why Choose Us?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            FeatureItem("Instant Results", "Convert bases in real-time")
            FeatureItem("Wide Range", "Supports bases 2 to 36")
            FeatureItem("Track History", "Access your past conversions")
        }
    }
}

@Composable
fun TestimonialSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(FrostedBackground)
            .padding(16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "User Feedback",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"The smoothest converter app out there!\"",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CompactConversionDialog(
    onDismiss: () -> Unit,
    onBaseConvertClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = FrostedBackground,
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
                    text = "Start Conversion",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Button(
                    onClick = onBaseConvertClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentColor,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Base Converter")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cancel", color = AccentColor)
                }
            }
        }
    }
}

@Composable
fun FeatureItem(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(AccentColor, CircleShape)
        )
        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LandingPagePreview() {
    BaseConverterTheme {
        LandingPage(username = "User", {})
    }
}
