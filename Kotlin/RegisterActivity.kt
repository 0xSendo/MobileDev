package com.example.baseconverter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.baseconverter.ui.theme.BaseConverterTheme
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {
    private lateinit var databaseManager: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseManager = DatabaseManager(this)
        setContent {
            BaseConverterTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF98FB98) // PaleGreen, matching login page
                ) {
                    RegisterScreen(
                        databaseManager = databaseManager,
                        onLoginClick = { navigateToLoginScreen() }
                    )
                }
            }
        }
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun RegisterScreen(databaseManager: DatabaseManager, onLoginClick: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var registerMessage by remember { mutableStateOf("") }
    var isUsernameValid by remember { mutableStateOf(true) }
    var isFirstNameValid by remember { mutableStateOf(true) }
    var isPasswordValid by remember { mutableStateOf(true) }
    var isConfirmPasswordValid by remember { mutableStateOf(true) }
    var isEmailValid by remember { mutableStateOf(true) }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Define colors to match login page
    val LightYellow = Color(0xFFFFF5E4)
    val Maroon = Color(0xFF660000)
    val LightRed = Color(0xFFFFA8A8)
    val Pink = Color(0xFFFFC1CC)

    // Helper function to validate and handle registration (non-composable)
    fun validateAndRegister() {
        if (username.isEmpty()) {
            registerMessage = "Username cannot be empty"
            return
        }

        if (firstName.isEmpty()) {
            registerMessage = "First name cannot be empty"
            return
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            registerMessage = "Please enter a valid email address"
            return
        }

        if (password.length < 6) {
            registerMessage = "Password must be at least 6 characters"
            return
        }

        if (password != confirmPassword) {
            registerMessage = "Passwords do not match"
            return
        }

        if (isUsernameValid && isFirstNameValid && isEmailValid && isPasswordValid && isConfirmPasswordValid) {
            scope.launch {
                val (usernameExists, emailExists) = databaseManager.userExists(username, email)
                when {
                    usernameExists -> registerMessage = "Username already exists"
                    emailExists -> registerMessage = "Email already exists"
                    else -> {
                        val (success, message) = databaseManager.registerUser(username, email, firstName, password)
                        registerMessage = if (success) "Registration successful! You can now sign in." else "Registration failed: $message"
                        if (success) {
                            onLoginClick()
                        }
                    }
                }
            }
        } else {
            registerMessage = "Please fill in all fields correctly"
        }
    }

    // Background with app branding
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg2),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Registration card
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp)
                .verticalScroll(scrollState),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.7f) // Semi-transparent white
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Registration header
                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )

                Text(
                    text = "Please fill in your details",
                    color = Color.DarkGray,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Username field
                Column {
                    Text(
                        text = "Username",
                        color = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp, start = 4.dp),
                        fontSize = 18.sp
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            isUsernameValid = username.isNotEmpty()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = { Text("Enter username") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LightYellow,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color(0xFFFFA07A),
                            focusedBorderColor = Pink,
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = Pink,
                            focusedTextColor = Color.DarkGray,
                            unfocusedTextColor = Color.DarkGray
                        ),
                        isError = !isUsernameValid,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    if (!isUsernameValid) {
                        Text(
                            text = "Username cannot be empty",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }

                // First Name field
                Column {
                    Text(
                        text = "First Name",
                        color = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp, start = 4.dp),
                        fontSize = 18.sp
                    )

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = {
                            firstName = it
                            isFirstNameValid = firstName.isNotEmpty()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = { Text("Enter first name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LightYellow,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color(0xFFFFA07A),
                            focusedBorderColor = Pink,
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = Pink,
                            focusedTextColor = Color.DarkGray,
                            unfocusedTextColor = Color.DarkGray
                        ),
                        isError = !isFirstNameValid,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    if (!isFirstNameValid) {
                        Text(
                            text = "First name cannot be empty",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }

                // Email field
                Column {
                    Text(
                        text = "Email",
                        color = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp, start = 4.dp),
                        fontSize = 18.sp
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            isEmailValid = email.isEmpty() || android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = { Text("Enter email address") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LightYellow,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color(0xFFFFA07A),
                            focusedBorderColor = Pink,
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = Pink,
                            focusedTextColor = Color.DarkGray,
                            unfocusedTextColor = Color.DarkGray
                        ),
                        isError = !isEmailValid,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    if (!isEmailValid) {
                        Text(
                            text = "Invalid email address",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }

                // Password field
                Column {
                    Text(
                        text = "Password",
                        color = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp, start = 4.dp),
                        fontSize = 18.sp
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            isPasswordValid = password.length >= 6
                            isConfirmPasswordValid = confirmPassword.isEmpty() || confirmPassword == password
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = { Text("Create a password") },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LightYellow,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color(0xFFFFA07A),
                            focusedBorderColor = Pink,
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = Pink,
                            focusedTextColor = Color.DarkGray,
                            unfocusedTextColor = Color.DarkGray
                        ),
                        isError = !isPasswordValid,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    if (!isPasswordValid) {
                        Text(
                            text = "Password must be at least 6 characters",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }

                // Confirm Password field
                Column {
                    Text(
                        text = "Confirm Password",
                        color = Color.DarkGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp, start = 4.dp),
                        fontSize = 18.sp
                    )

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                            isConfirmPasswordValid = it == password
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        placeholder = { Text("Re-enter your password") },
                        visualTransformation = PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LightYellow,
                            unfocusedContainerColor = Color.White,
                            errorContainerColor = Color(0xFFFFA07A),
                            focusedBorderColor = Pink,
                            unfocusedBorderColor = Color.DarkGray,
                            cursorColor = Pink,
                            focusedTextColor = Color.DarkGray,
                            unfocusedTextColor = Color.DarkGray
                        ),
                        isError = !isConfirmPasswordValid,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                validateAndRegister()
                            }
                        ),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    if (!isConfirmPasswordValid) {
                        Text(
                            text = "Passwords do not match",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Register button
                Button(
                    onClick = {
                        keyboardController?.hide()
                        validateAndRegister()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Maroon
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 2.dp
                    )
                ) {
                    Text(
                        text = "CREATE ACCOUNT",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Register message
                if (registerMessage.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.7f) // Semi-transparent white
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = registerMessage,
                            textAlign = TextAlign.Center,
                            color = if (registerMessage.startsWith("Registration successful")) Maroon else Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
                        )
                    }
                }

                // Back to login button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        color = Color.DarkGray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Sign In",
                        color = Pink,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline,
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onLoginClick() }
                    )
                }
            }
        }

        // Footer text
        Text(
            text = "© 2025 Base Converter",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp),
            fontSize = 12.sp,
            color = Color(0xFF0A1F44) // Matching login page
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    BaseConverterTheme(darkTheme = true) {
        RegisterScreen(
            databaseManager = object : DatabaseManager(null) {
                override fun userExists(username: String, email: String): Pair<Boolean, Boolean> {
                    return Pair(false, false)
                }

                override fun registerUser(username: String, email: String, firstName: String, password: String): Pair<Boolean, String> {
                    return Pair(true, "Success")
                }
            },
            onLoginClick = {}
        )
    }
}
