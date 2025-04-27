package com.example.baseconverter

import android.content.Context
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
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
val DrawerItemBackground = Color(0xFF4A0000) // Darker maroon for drawer items

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
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LandingPage(
    username: String,
    onBaseConvertClick: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
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
        drawerState = drawerState,
        gesturesEnabled = true,
        scrimColor = Color.Black.copy(alpha = 0.5f)
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
                                contentDescription = "Open Menu",
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
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .width(280.dp)
            .fillMaxHeight()
            .background(GradientStart)
            .padding(20.dp)
    ) {
        // Drawer Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    Brush.verticalGradient(listOf(GradientStart, Color(0xFFB00000)))
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "User",
                tint = AccentColor,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .padding(6.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = username,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Menu",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
        Divider(color = Color.White.copy(alpha = 0.3f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        // Drawer Items with scale-and-fade animation
        DrawerItem("Profile", Icons.Default.Person) {
            Intent(context, ProfileActivity::class.java).apply {
                putExtra("logged_in_user", username)
                context.startActivity(this)
                (context as? ComponentActivity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            onClose()
        }
        DrawerItem("Notes", Icons.Default.Note) {
            Intent(context, NotesActivity::class.java).apply {
                putExtra("logged_in_user", username)
                context.startActivity(this)
                (context as? ComponentActivity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            onClose()
        }
        DrawerItem("History", Icons.Default.History) {
            Intent(context, HistoryActivity::class.java).apply {
                putExtra("logged_in_user", username)
                context.startActivity(this)
                (context as? ComponentActivity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            onClose()
        }
        DrawerItem("Logout", Icons.Default.ExitToApp) {
            Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                context.startActivity(this)
                (context as? ComponentActivity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            (context as? ComponentActivity)?.finish()
            onClose()
        }
    }
}

@Composable
fun DrawerItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(false) }
    var animationTrigger by remember { mutableStateOf(0) }

    // Scale animation for hover and press
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 0.9f else if (isHovered) 1.05f else 1f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )

    // Alpha animation for press
    val alpha by animateFloatAsState(
        targetValue = if (isAnimating) 0.5f else 1f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )

    LaunchedEffect(animationTrigger) {
        if (animationTrigger > 0) {
            isAnimating = true
            // Total animation duration: 400ms (200ms down, 200ms back)
            kotlinx.coroutines.delay(200)
            isAnimating = false
            kotlinx.coroutines.delay(200)
            onClick()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DrawerItemBackground)
            .clickable(
                onClick = {
                    animationTrigger++
                    isHovered = false
                },
                onClickLabel = "Navigate to $text"
            )
            .scale(scale)
            .alpha(alpha)
            .shadow(4.dp, RoundedCornerShape(8.dp)),
        color = DrawerItemBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = AccentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun BottomNavigationBar(username: String) {
    val context = LocalContext.current

    NavigationBar(
        containerColor = FrostedBackground,
        contentColor = Color.White
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = Color.Black) },
            label = { Text("Home", color = Color.Black) },
            selected = true,
            onClick = { /* Already on Home, no action needed */ }
        )
        NavigationBarItem(
            icon = {
                var animationTrigger by remember { mutableStateOf(0) }
                var isAnimating by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isAnimating) 0.9f else 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isAnimating) 0.5f else 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )

                LaunchedEffect(animationTrigger) {
                    if (animationTrigger > 0) {
                        isAnimating = true
                        kotlinx.coroutines.delay(200)
                        isAnimating = false
                        kotlinx.coroutines.delay(200)
                        Intent(context, ProfileActivity::class.java).apply {
                            putExtra("logged_in_user", username)
                            context.startActivity(this)
                            (context as? ComponentActivity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .scale(scale)
                        .alpha(alpha),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = "Profile",
                        tint = Color.Black,
                        modifier = Modifier.clickable { animationTrigger++ }
                    )
                }
            },
            label = {
                var animationTrigger by remember { mutableStateOf(0) }
                var isAnimating by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isAnimating) 0.9f else 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isAnimating) 0.5f else 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )

                LaunchedEffect(animationTrigger) {
                    if (animationTrigger > 0) {
                        isAnimating = true
                        kotlinx.coroutines.delay(200)
                        isAnimating = false
                        kotlinx.coroutines.delay(200)
                        Intent(context, ProfileActivity::class.java).apply {
                            putExtra("logged_in_user", username)
                            context.startActivity(this)
                            (context as? ComponentActivity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                    }
                }

                Text(
                    "Profile",
                    color = Color.Black,
                    modifier = Modifier
                        .scale(scale)
                        .alpha(alpha)
                        .clickable { animationTrigger++ }
                )
            },
            selected = false,
            onClick = { /* Handled by icon and label */ }
        )
        NavigationBarItem(
            icon = {
                var animationTrigger by remember { mutableStateOf(0) }
                var isAnimating by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isAnimating) 0.9f else 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isAnimating) 0.5f else 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )

                LaunchedEffect(animationTrigger) {
                    if (animationTrigger > 0) {
                        isAnimating = true
                        kotlinx.coroutines.delay(200)
                        isAnimating = false
                        kotlinx.coroutines.delay(200)
                        Intent(context, HistoryActivity::class.java).apply {
                            putExtra("logged_in_user", username)
                            context.startActivity(this)
                            (context as? ComponentActivity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .scale(scale)
                        .alpha(alpha),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = "History",
                        tint = Color.Black,
                        modifier = Modifier.clickable { animationTrigger++ }
                    )
                }
            },
            label = {
                var animationTrigger by remember { mutableStateOf(0) }
                var isAnimating by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(
                    targetValue = if (isAnimating) 0.9f else 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )
                val alpha by animateFloatAsState(
                    targetValue = if (isAnimating) 0.5f else 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )

                LaunchedEffect(animationTrigger) {
                    if (animationTrigger > 0) {
                        isAnimating = true
                        kotlinx.coroutines.delay(200)
                        isAnimating = false
                        kotlinx.coroutines.delay(200)
                        Intent(context, HistoryActivity::class.java).apply {
                            putExtra("logged_in_user", username)
                            context.startActivity(this)
                            (context as? ComponentActivity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                        }
                    }
                }

                Text(
                    "History",
                    color = Color.Black,
                    modifier = Modifier
                        .scale(scale)
                        .alpha(alpha)
                        .clickable { animationTrigger++ }
                )
            },
            selected = false,
            onClick = { /* Handled by icon and label */ }
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
            ButtonWithAnimation(
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
fun ButtonWithAnimation(
    onClick: () -> Unit,
    colors: ButtonColors,
    shape: RoundedCornerShape,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var animationTrigger by remember { mutableStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 0.9f else 1f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )
    val alpha by animateFloatAsState(
        targetValue = if (isAnimating) 0.5f else 1f,
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
    )

    LaunchedEffect(animationTrigger) {
        if (animationTrigger > 0) {
            isAnimating = true
            kotlinx.coroutines.delay(200)
            isAnimating = false
            kotlinx.coroutines.delay(200)
            onClick()
        }
    }

    Button(
        onClick = { animationTrigger++ },
        colors = colors,
        shape = shape,
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
    ) {
        content()
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
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"The smoothest converter app out there!\"",
                fontSize = 16.sp,
                color = Color.Black.copy(alpha = 0.8f),
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
                ButtonWithAnimation(
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
