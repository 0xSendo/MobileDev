package com.example.baseconverter

import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getSystemService
import com.example.baseconverter.ui.theme.BaseConverterTheme
import kotlinx.coroutines.delay
import java.lang.NumberFormatException

val TextColor = Color.White
val ErrorColor = Color(0xFFFF6666) // Soft red for errors

class BaseConverterActivity : ComponentActivity() {
    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val username = intent.getStringExtra("logged_in_user") ?: "Unknown"

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentTime = System.currentTimeMillis()
                if (currentTime - backPressedTime <= BACK_PRESS_INTERVAL) {
                    navigateToLandingPage(username)
                } else {
                    backPressedTime = currentTime
                    Toast.makeText(this@BaseConverterActivity, "Press back again to return to Profile", Toast.LENGTH_SHORT).show()
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
                    BaseConverterScreen(
                        username = username,
                        onBackClick = { navigateToLandingPage(username) },
                        databaseManager = DatabaseManager(this)
                    )
                }
            }
        }
    }

    private fun navigateToLandingPage(username: String) {
        val intent = Intent(this, LandingPageActivity::class.java).apply {
            putExtra("logged_in_user", username)
        }
        startActivity(intent)
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaseConverterScreen(
    username: String,
    onBackClick: () -> Unit,
    databaseManager: DatabaseManager
) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("ConversionHistory", Context.MODE_PRIVATE)
    var inputNumber by rememberSaveable { mutableStateOf("") }
    var selectedConversion by rememberSaveable { mutableStateOf("Decimal to Binary") }
    var result by rememberSaveable { mutableStateOf("") }
    var errorMessage by rememberSaveable { mutableStateOf("") }
    var showHistory by remember { mutableStateOf(false) }
    var showBaseInfo by remember { mutableStateOf(false) }
    var isFadingOut by remember { mutableStateOf(false) }
    val history = remember { mutableStateListOf<String>() }

    // Input validation function
    fun isValidInput(input: String, base: String): Boolean {
        if (input.isBlank()) return true
        return try {
            when (base) {
                "Decimal" -> input.all { it.isDigit() } && input.toLong() >= 0
                "Binary" -> input.all { it == '0' || it == '1' }
                "Octal" -> input.all { it in '0'..'7' }
                "Hexadecimal" -> input.all { it.isDigit() || it in 'A'..'F' || it in 'a'..'f' }
                else -> false
            }
        } catch (e: Exception) {
            false
        }
    }

    // Input validation state using derivedStateOf
    val isValidInput by remember(inputNumber, selectedConversion) {
        derivedStateOf {
            isValidInput(inputNumber, selectedConversion.split(" ")[0])
        }
    }

    val alpha by animateFloatAsState(
        targetValue = if (isFadingOut) 0f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "fade_animation"
    )

    LaunchedEffect(isFadingOut) {
        if (isFadingOut) {
            delay(300)
            onBackClick()
        }
    }

    LaunchedEffect(Unit) {
        val dbHistory = databaseManager.getConversionHistory(username)
        history.clear()
        history.addAll(dbHistory.map { "${it.inputValue} (Base ${it.inputBase}) → ${it.outputValue} (Base ${it.outputBase})" })
    }

    val conversions = listOf(
        "Decimal to Binary", "Decimal to Octal", "Decimal to Hexadecimal",
        "Binary to Decimal", "Binary to Octal", "Binary to Hexadecimal",
        "Octal to Decimal", "Octal to Binary", "Octal to Hexadecimal",
        "Hexadecimal to Decimal", "Hexadecimal to Binary", "Hexadecimal to Octal"
    )

    fun convertNumber() {
        if (inputNumber.isBlank()) {
            result = ""
            errorMessage = "Please enter a number"
            return
        }

        val (fromBaseStr, toBaseStr) = selectedConversion.split(" to ")
        if (!isValidInput(inputNumber, fromBaseStr)) {
            errorMessage = "Invalid $fromBaseStr number"
            result = ""
            return
        }

        try {
            val fromBase = when (fromBaseStr) {
                "Decimal" -> 10
                "Binary" -> 2
                "Octal" -> 8
                "Hexadecimal" -> 16
                else -> throw IllegalArgumentException("Unknown base: $fromBaseStr")
            }
            val toBase = when (toBaseStr) {
                "Decimal" -> 10
                "Binary" -> 2
                "Octal" -> 8
                "Hexadecimal" -> 16
                else -> throw IllegalArgumentException("Unknown base: $toBaseStr")
            }

            val decimalValue = inputNumber.toLong(fromBase)
            result = when (toBase) {
                16 -> decimalValue.toString(16).uppercase()
                else -> decimalValue.toString(toBase)
            }

            errorMessage = ""
            databaseManager.saveConversion(
                username = username,
                inputValue = inputNumber,
                inputBase = fromBase,
                outputValue = result,
                outputBase = toBase
            )
            val conversionEntry = "$inputNumber (Base $fromBase) → $result (Base $toBase)"
            if (!history.contains(conversionEntry)) {
                history.add(0, conversionEntry)
                if (history.size > 10) history.removeAt(history.size - 1)
                prefs.edit().putStringSet("history", history.toSet()).apply()
            }
        } catch (e: NumberFormatException) {
            errorMessage = "Invalid $fromBaseStr number"
            result = ""
        } catch (e: Exception) {
            errorMessage = "Conversion error: ${e.message}"
            result = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Base Converter",
                        color = TextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { isFadingOut = true }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = TextColor
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { showHistory = true }) {
                        Text("History", color = AccentColor, fontSize = 16.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent,
        modifier = Modifier.alpha(alpha)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(GradientStart, GradientEnd)))
                .padding(paddingValues)
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FrostedBackground, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Input",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextColor
                            )
                            IconButton(onClick = { showBaseInfo = true }) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Base Info",
                                    tint = AccentColor
                                )
                            }
                        }
                        OutlinedTextField(
                            value = inputNumber,
                            onValueChange = {
                                inputNumber = it
                                convertNumber()
                            },
                            label = { Text("${selectedConversion.split(" ")[0]} Number", color = TextColor.copy(alpha = 0.7f)) },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = {
                                Icon(
                                    painter = painterResource(
                                        when (selectedConversion.split(" ")[0]) {
                                            "Decimal" -> R.drawable.ic_decimal
                                            "Binary" -> R.drawable.ic_binary
                                            "Octal" -> R.drawable.ic_octal
                                            "Hexadecimal" -> R.drawable.ic_hex
                                            else -> R.drawable.ic_decimal
                                        }
                                    ),
                                    contentDescription = null,
                                    tint = AccentColor
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = FrostedBackground,
                                unfocusedContainerColor = FrostedBackground,
                                disabledContainerColor = FrostedBackground,
                                focusedIndicatorColor = if (isValidInput) AccentColor else ErrorColor,
                                unfocusedIndicatorColor = if (isValidInput) TextColor.copy(alpha = 0.5f) else ErrorColor,
                                cursorColor = AccentColor,
                                focusedTextColor = TextColor,
                                unfocusedTextColor = TextColor,
                                focusedLabelColor = AccentColor,
                                unfocusedLabelColor = TextColor.copy(alpha = 0.7f)
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = when (selectedConversion.split(" ")[0]) {
                                    "Hexadecimal" -> KeyboardType.Text
                                    else -> KeyboardType.Number
                                }
                            )
                        )

                        ConversionSelector(
                            selectedConversion = selectedConversion,
                            conversions = conversions,
                            onConversionChange = {
                                selectedConversion = it
                                convertNumber()
                            }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Decimal to Binary", "Binary to Decimal", "Decimal to Hexadecimal").forEach { preset ->
                                FilterChip(
                                    selected = selectedConversion == preset,
                                    onClick = {
                                        selectedConversion = preset
                                        convertNumber()
                                    },
                                    label = { Text(preset, fontSize = 14.sp, color = TextColor) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AccentColor.copy(alpha = 0.3f),
                                        selectedLabelColor = TextColor,
                                        containerColor = FrostedBackground,
                                        labelColor = TextColor.copy(alpha = 0.7f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { inputNumber = ""; result = ""; errorMessage = "" },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentColor,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_clear),
                                        contentDescription = "Clear",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Clear", fontSize = 14.sp)
                                }
                            }
                            Button(
                                onClick = {
                                    val clipboard = getSystemService(context, ClipboardManager::class.java)
                                    clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Input", inputNumber))
                                    Toast.makeText(context, "Input copied!", Toast.LENGTH_SHORT).show()
                                },
                                enabled = inputNumber.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AccentColor,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = "Copy Input",
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy", fontSize = 14.sp)
                                }
                            }
                        }
                        Button(
                            onClick = {
                                val (fromBase, toBase) = selectedConversion.split(" to ")
                                val swapped = "$toBase to $fromBase"
                                if (conversions.contains(swapped)) {
                                    selectedConversion = swapped
                                    convertNumber()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentColor,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.SwapHoriz,
                                    contentDescription = "Swap Bases",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Swap Bases", fontSize = 14.sp)
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(FrostedBackground, RoundedCornerShape(12.dp))
                            .clickable(enabled = result.isNotEmpty()) {
                                val clipboard = getSystemService(context, ClipboardManager::class.java)
                                clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Result", result))
                                Toast.makeText(context, "Result copied!", Toast.LENGTH_SHORT).show()
                            }
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${selectedConversion.split(" to ")[1]} Result",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextColor
                        )
                        Text(
                            text = result.ifEmpty { "—" },
                            fontSize = 26.sp,
                            color = TextColor,
                            modifier = Modifier.padding(top = 6.dp),
                            maxLines = 1
                        )
                        if (errorMessage.isNotEmpty()) {
                            Text(
                                text = errorMessage,
                                color = ErrorColor,
                                fontSize = 14.sp,
                                modifier = Modifier.padding(top = 6.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Text(
                text = "© 2025 Base Converter",
                fontSize = 12.sp,
                color = TextColor.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            )
        }

        if (showHistory) {
            AlertDialog(
                onDismissRequest = { showHistory = false },
                title = { Text("Conversion History", color = AccentColor, fontSize = 18.sp) },
                text = {
                    if (history.isEmpty()) {
                        Text("No conversions yet", color = TextColor.copy(alpha = 0.7f))
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(max = 280.dp)
                        ) {
                            items(history) { entry ->
                                Text(
                                    text = entry,
                                    color = TextColor,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(
                            onClick = {
                                prefs.edit().clear().apply()
                                databaseManager.clearConversionHistory(username)
                                history.clear()
                            }
                        ) {
                            Text("Clear History", color = AccentColor, fontSize = 14.sp)
                        }
                        TextButton(onClick = { showHistory = false }) {
                            Text("Close", color = AccentColor, fontSize = 14.sp)
                        }
                    }
                },
                containerColor = FrostedBackground,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            )
        }

        if (showBaseInfo) {
            AlertDialog(
                onDismissRequest = { showBaseInfo = false },
                title = { Text("${selectedConversion.split(" ")[0]} Base", color = AccentColor, fontSize = 18.sp) },
                text = {
                    Text(
                        text = when (selectedConversion.split(" ")[0]) {
                            "Decimal" -> "Decimal (Base 10) uses digits 0-9."
                            "Binary" -> "Binary (Base 2) uses digits 0 and 1."
                            "Octal" -> "Octal (Base 8) uses digits 0-7."
                            "Hexadecimal" -> "Hexadecimal (Base 16) uses digits 0-9 and letters A-F."
                            else -> "Unknown base."
                        },
                        color = TextColor,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showBaseInfo = false }) {
                        Text("Close", color = AccentColor, fontSize = 14.sp)
                    }
                },
                containerColor = FrostedBackground,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversionSelector(
    selectedConversion: String,
    conversions: List<String>,
    onConversionChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedConversion,
            onValueChange = {},
            label = { Text("Conversion Type", color = TextColor.copy(alpha = 0.7f)) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = FrostedBackground,
                unfocusedContainerColor = FrostedBackground,
                disabledContainerColor = FrostedBackground,
                focusedIndicatorColor = AccentColor,
                unfocusedIndicatorColor = TextColor.copy(alpha = 0.5f),
                cursorColor = AccentColor,
                focusedTextColor = TextColor,
                unfocusedTextColor = TextColor,
                focusedLabelColor = AccentColor,
                unfocusedLabelColor = TextColor.copy(alpha = 0.7f)
            )
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(FrostedBackground)
        ) {
            conversions.forEach { conversion ->
                DropdownMenuItem(
                    text = { Text(conversion, color = TextColor, fontSize = 14.sp) },
                    onClick = {
                        onConversionChange(conversion)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BaseConverterPreview() {
    BaseConverterTheme(darkTheme = true) {
        BaseConverterScreen(
            username = "PreviewUser",
            onBackClick = {},
            databaseManager = object : DatabaseManager(null) {
                override fun saveConversion(username: String, inputValue: String, inputBase: Int, outputValue: String, outputBase: Int): Boolean = true
                override fun getConversionHistory(username: String): List<ConversionEntry> = listOf(
                    ConversionEntry("123", 10, "7B", 16, System.currentTimeMillis())
                )
                override fun clearConversionHistory(username: String): Boolean = true
            }
        )
    }
}
