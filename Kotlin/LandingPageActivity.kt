package com.example.baseconverter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.zIndex
import com.airbnb.lottie.compose.*
import com.example.baseconverter.ui.theme.BaseConverterTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

// Use the specified color theme
val GradientStart = Color(0xFF800000) // Maroon
val GradientEnd = Color(0xFFFFFFFF)   // White
val FrostedBackground = Color.White.copy(alpha = 0.1f)
val AccentColor = Color(0xFFFFCA28) // Yellow for highlights
val DrawerItemBackground = Color(0xFF4A0000) // Darker maroon for drawer items

class LandingPageActivity : ComponentActivity() {
    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime <= BACK_PRESS_INTERVAL) {
                    finishAffinity()
                } else {
                    backPressedTime = currentTime
                    Toast.makeText(this@LandingPageActivity, "Press back again to exit", Toast.LENGTH_SHORT).show()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)

        setContent {
            BaseConverterTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
                    var username = intent.getStringExtra("logged_in_user")
                        ?: sharedPreferences.getString("logged_in_user", null)
                    if (username == null) {
                        val databaseManager = DatabaseManager(this@LandingPageActivity)
                        username = databaseManager.getAnyUser()?.first ?: "User"
                    }
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
    var isRefreshing by remember { mutableStateOf(false) }
    var isDarkMode by remember { mutableStateOf(false) }

    val animatedAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
    )

    ModalNavigationDrawer(
        drawerContent = {
            DrawerContent(
                username = username,
                isDarkMode = isDarkMode,
                onDarkModeToggle = { isDarkMode = !isDarkMode },
                onClose = { scope.launch { drawerState.close() } }
            )
        },
        drawerState = drawerState,
        gesturesEnabled = true,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(GradientStart, GradientEnd))
                )
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = "$greeting, $username",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextColor,
                                modifier = Modifier.alpha(animatedAlpha)
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Default.Menu,
                                    contentDescription = "Open Menu",
                                    tint = TextColor,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        },
                        actions = {
                            IconButton(
                                onClick = {
                                    isRefreshing = true
                                    scope.launch {
                                        delay(800)
                                        isRefreshing = false
                                    }
                                },
                                enabled = !isRefreshing
                            ) {
                                Icon(
                                    Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = if (isRefreshing) TextColor.copy(alpha = 0.5f) else TextColor,
                                    modifier = Modifier.size(30.dp)
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
                        targetValue = if (showConversionDialog) 1.15f else 1f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    )
                    FloatingActionButton(
                        onClick = { showConversionDialog = true },
                        containerColor = AccentColor,
                        contentColor = Color.Black,
                        shape = CircleShape,
                        modifier = Modifier
                            .size(64.dp)
                            .scale(scale)
                    ) {
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = "Start Conversion",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                containerColor = Color.Transparent
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    item {
                        QuickAccessToolbar(username = username)
                    }
                    item {
                        HeroSection(onBaseConvertClick)
                    }
                    item {
                        QuickCalculatorWidget()
                    }
                    item {
                        FeaturesSection()
                    }
                    item {
                        TestimonialSection()
                    }
                }
            }

            if (isRefreshing) {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.refresh_icon))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = 72.dp)
                        .zIndex(1f)
                        .background(AccentColor, CircleShape)
                        .padding(6.dp)
                )
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
fun DrawerContent(
    username: String,
    isDarkMode: Boolean,
    onDarkModeToggle: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight()
            .background(GradientStart)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.user_icon))
            LottieAnimation(
                composition = composition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(FrostedBackground)
                    .padding(4.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = username,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextColor
                )
                Text(
                    text = "Menu",
                    fontSize = 16.sp,
                    color = TextColor.copy(alpha = 0.7f)
                )
            }
        }
        Divider(color = TextColor.copy(alpha = 0.3f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(8.dp))

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
        DrawerItem("Theme: ${if (isDarkMode) "Dark" else "Light"}", Icons.Default.Brightness6) {
            onDarkModeToggle()
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
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "© 2025 Base Converter",
            fontSize = 12.sp,
            color = TextColor.copy(alpha = 0.7f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DrawerItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    var isHovered by remember { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(false) }
    var animationTrigger by remember { mutableStateOf(0) }

    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 0.95f else if (isHovered) 1.03f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
    )

    LaunchedEffect(animationTrigger) {
        if (animationTrigger > 0) {
            isAnimating = true
            delay(150)
            isAnimating = false
            delay(150)
            onClick()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isHovered) DrawerItemBackground else Color.Transparent)
            .clickable(
                onClick = {
                    animationTrigger++
                    isHovered = false
                },
                onClickLabel = "Navigate to $text"
            )
            .scale(scale)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = AccentColor,
            modifier = Modifier.size(26.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            color = TextColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun BottomNavigationBar(username: String) {
    val context = LocalContext.current

    NavigationBar(
        containerColor = FrostedBackground,
        contentColor = TextColor
    ) {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = AccentColor, modifier = Modifier.size(28.dp)) },
            label = { Text("Home", color = AccentColor, fontSize = 12.sp) },
            selected = true,
            onClick = { /* Already on Home */ }
        )
        NavigationBarItem(
            icon = {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.profile_icon))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(28.dp)
                )
            },
            label = { Text("Profile", color = AccentColor, fontSize = 12.sp) },
            selected = false,
            onClick = {
                Intent(context, ProfileActivity::class.java).apply {
                    putExtra("logged_in_user", username)
                    context.startActivity(this)
                    (context as? ComponentActivity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        )
        NavigationBarItem(
            icon = {
                val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.history_icon))
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(28.dp)
                )
            },
            label = { Text("History", color = AccentColor, fontSize = 12.sp) },
            selected = false,
            onClick = {
                Intent(context, HistoryActivity::class.java).apply {
                    putExtra("logged_in_user", username)
                    context.startActivity(this)
                    (context as? ComponentActivity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                }
            }
        )
    }
}

@Composable
fun QuickAccessToolbar(username: String) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickAccessButton(Icons.Default.Calculate, "Converter") {
            Intent(context, BaseConverterActivity::class.java).apply {
                putExtra("logged_in_user", username)
                context.startActivity(this)
                (context as? ComponentActivity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
        QuickAccessButton(Icons.Default.Note, "Notes") {
            Intent(context, NotesActivity::class.java).apply {
                putExtra("logged_in_user", username)
                context.startActivity(this)
                (context as? ComponentActivity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
        QuickAccessButton(Icons.Default.History, "History") {
            Intent(context, HistoryActivity::class.java).apply {
                putExtra("logged_in_user", username)
                context.startActivity(this)
                (context as? ComponentActivity)?.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }
}

@Composable
fun QuickAccessButton(icon: ImageVector, label: String, onClick: () -> Unit) {
    var isAnimating by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150)
    )
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable {
                isAnimating = true
                scope.launch {
                    delay(150)
                    isAnimating = false
                    onClick()
                }
            }
            .scale(scale)
            .padding(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AccentColor,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = label,
            color = TextColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun QuickCalculatorWidget() {
    var inputNumber by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue("")) }
    var fromBase by remember { mutableStateOf(10) }
    var toBase by remember { mutableStateOf(2) }
    var result by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(FrostedBackground, RoundedCornerShape(12.dp)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quick Base Converter",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextColor,
            modifier = Modifier.padding(top = 12.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = inputNumber,
            onValueChange = { inputNumber = it },
            label = { Text("Enter Number", color = TextColor.copy(alpha = 0.7f)) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentColor,
                unfocusedBorderColor = TextColor.copy(alpha = 0.5f),
                cursorColor = AccentColor,
                focusedTextColor = TextColor,
                unfocusedTextColor = TextColor
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BaseSelector("From Base", fromBase) { fromBase = it }
            BaseSelector("To Base", toBase) { toBase = it }
        }
        Spacer(modifier = Modifier.height(8.dp))
        ButtonWithAnimation(
            onClick = {
                try {
                    val number = inputNumber.text.trim()
                    if (number.isNotEmpty()) {
                        val value = number.toLong(fromBase)
                        result = value.toString(toBase).uppercase()
                    } else {
                        result = "Enter a number"
                    }
                } catch (e: Exception) {
                    result = "Invalid input"
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentColor,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Text("Convert", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = result,
            fontSize = 18.sp,
            color = TextColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp)
        )
    }
}

@Composable
fun BaseSelector(label: String, selectedBase: Int, onBaseSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val bases = (2..36).toList()

    Box {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.width(110.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextColor)
        ) {
            Text("$selectedBase", fontSize = 14.sp)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(FrostedBackground)
        ) {
            bases.forEach { base ->
                DropdownMenuItem(
                    text = { Text(base.toString(), color = TextColor, fontSize = 14.sp) },
                    onClick = {
                        onBaseSelected(base)
                        expanded = false
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun HeroSection(onBaseConvertClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(FrostedBackground, RoundedCornerShape(12.dp))
            .clickable { onBaseConvertClick() }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.converter_icon))
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier.size(80.dp)
        )
        Text(
            text = "Convert Numbers Seamlessly",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = TextColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Instantly convert between bases 2-36 with ease!",
            fontSize = 16.sp,
            color = TextColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        ButtonWithAnimation(
            onClick = onBaseConvertClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentColor,
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Start Now", fontSize = 16.sp)
        }
    }
}

@Composable
fun FeaturesSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(FrostedBackground, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Why Choose Us?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        FeatureItem("Fast Conversions", "Real-time results as you type")
        FeatureItem("Wide Support", "Bases 2 to 36 covered")
        FeatureItem("History Tracking", "Review past conversions")
    }
}

@Composable
fun TestimonialSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .background(FrostedBackground, RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What Users Say",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = TextColor
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "\"Super intuitive and lightning fast!\"",
            fontSize = 16.sp,
            color = TextColor.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CompactConversionDialog(
    onDismiss: () -> Unit,
    onBaseConvertClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .width(280.dp)
                .background(FrostedBackground, RoundedCornerShape(12.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Start Conversion",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            ButtonWithAnimation(
                onClick = onBaseConvertClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Base Converter", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancel", color = AccentColor, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun FeatureItem(title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(AccentColor, CircleShape)
        )
        Column(
            modifier = Modifier.padding(start = 10.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextColor
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = TextColor.copy(alpha = 0.8f)
            )
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
        targetValue = if (isAnimating) 0.95f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)
    )

    LaunchedEffect(animationTrigger) {
        if (animationTrigger > 0) {
            isAnimating = true
            delay(150)
            isAnimating = false
            delay(150)
            onClick()
        }
    }

    Button(
        onClick = { animationTrigger++ },
        colors = colors,
        shape = shape,
        modifier = modifier.scale(scale)
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun LandingPagePreview() {
    BaseConverterTheme {
        LandingPage(username = "User", {})
    }
}
