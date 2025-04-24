package com.example.baseconverter

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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

class MainActivity : ComponentActivity() {
    private lateinit var databaseManager: DatabaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        databaseManager = DatabaseManager(this)
        setContent {
            BaseConverterTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF98FB98) // PaleGreen background
                ) {
                    LoginScreen(
                        databaseManager = databaseManager,
                        onRegisterClick = { navigateToRegisterScreen() },
                        onLoginSuccess = { username -> navigateToLandingPage(username) }
                    )
                }
            }
        }
    }

    private fun navigateToRegisterScreen() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToLandingPage(username: String) {
        val intent = Intent(this, LandingPageActivity::class.java)
        intent.putExtra("logged_in_user", username)
        startActivity(intent)
        finish()
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun LoginScreen(
        databaseManager: DatabaseManager,
        onRegisterClick: () -> Unit,
        onLoginSuccess: (String) -> Unit
    ) {
        var username by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var loginMessage by remember { mutableStateOf("") }
        var isUsernameValid by remember { mutableStateOf(true) }
        var isPasswordValid by remember { mutableStateOf(true) }

        val focusManager = LocalFocusManager.current
        val keyboardController = LocalSoftwareKeyboardController.current
        val scope = rememberCoroutineScope()

        // Define colors from the first code snippet
        val LightYellow = Color(0xFFFFF5E4)
        val Maroon = Color(0xFF660000)
        val LightRed = Color(0xFFFFA8A8)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bg),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )


            // Login card with semi-transparent background
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
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
                    // Login header
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )

                    Text(
                        text = "Please sign in to continue",
                        color = Color.DarkGray,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Username field with improved label
                    Column {
                        Text(
                            text = "Username",
                            color = Color.DarkGray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
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
                                focusedContainerColor = Color(0xFF90EE90),
                                unfocusedContainerColor = Color.White,
                                errorContainerColor = Color(0xFFFFA07A),
                                focusedBorderColor = Color(0xFF2E8B57),
                                unfocusedBorderColor = Color(0xFFDD88CF)
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

                    // Password field with improved label
                    Column {
                        Text(
                            text = "Password",
                            color = Color.DarkGray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 4.dp),
                            fontSize = 18.sp
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                isPasswordValid = password.length >= 6
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            placeholder = { Text("Enter password") },
                            visualTransformation = PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF90EE90),
                                unfocusedContainerColor = Color.White,
                                errorContainerColor = Color(0xFFFFA07A),
                                focusedBorderColor = Color(0xFFB2A5FF),
                                unfocusedBorderColor = Color(0xFFDD88CF)
                            ),
                            isError = !isPasswordValid,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    if (isUsernameValid && isPasswordValid) {
                                        scope.launch {
                                            val success = databaseManager.loginUser(username, password)
                                            if (success) {
                                                onLoginSuccess(username)
                                            } else {
                                                loginMessage = "Invalid login credentials"
                                            }
                                        }
                                    } else {
                                        loginMessage = "Please fill in all fields correctly"
                                    }
                                }
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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Login button with improved styling
                    Button(
                        onClick = {
                            keyboardController?.hide()
                            if (isUsernameValid && isPasswordValid) {
                                scope.launch {
                                    val success = databaseManager.loginUser(username, password)
                                    if (success) {
                                        onLoginSuccess(username)
                                    } else {
                                        loginMessage = "Invalid login credentials"
                                    }
                                }
                            } else {
                                loginMessage = "Please fill in all fields correctly"
                            }
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
                            text = "SIGN IN",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Login message (error or success)
                    if (loginMessage.isNotEmpty()) {
                        Text(
                            text = loginMessage,
                            textAlign = TextAlign.Center,
                            color = if (loginMessage.startsWith("Invalid")) Color.Red else Color(0xFF006400),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Register link with improved styling
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Don't have an account? ",
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Register",
                            color = Maroon,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { onRegisterClick() }
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
                color = Color(0xFF0A1F44)
            )
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun LoginScreenPreview() {
        BaseConverterTheme(darkTheme = true) {
            LoginScreen(
                databaseManager = object : DatabaseManager(null) {
                    override fun loginUser(username: String, password: String): Boolean {
                        return username == "test" && password == "password"
                    }
                },
                onRegisterClick = {},
                onLoginSuccess = {}
            )
        }
    }
}
