package com.example.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.CompanionViewModel

/**
 * Requirement 2 & 3: Student360 Signup and Login Experience.
 * Fixed: Improved button responsiveness, validation, and layout padding.
 */

@Composable
fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = CoralOrange,
    unfocusedBorderColor = NavySurface,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedLabelColor = CoralOrange,
    unfocusedLabelColor = TextMuted,
    cursorColor = CoralOrange,
    focusedLeadingIconColor = CoralOrange,
    unfocusedLeadingIconColor = TextMuted
)

@Composable
fun AuthNavigation(viewModel: CompanionViewModel) {
    var isLogin by remember { mutableStateOf(true) }
    val context = LocalContext.current

    if (isLogin) {
        LoginScreen(
            onLogin = { email, pass -> 
                if (email.isBlank() || pass.isBlank()) {
                    Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                } else {
                    viewModel.login(email, pass) { success ->
                        if (!success) {
                            Toast.makeText(context, "Invalid credentials. Please sign up if you don't have an account.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            onCreateAccount = { isLogin = false }
        )
    } else {
        SignupScreen(
            onSignup = { first, last, email, pass, inst, allowance ->
                viewModel.signup(first, last, email, pass, inst, allowance)
                Toast.makeText(context, "Welcome to Student360!", Toast.LENGTH_SHORT).show()
            },
            onBackToLogin = { isLogin = true }
        )
    }
}

@Composable
fun LoginScreen(onLogin: (String, String) -> Unit, onCreateAccount: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .systemBarsPadding(), // Ensures content is not hidden by system bars
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Student360Branding.Logo(size = 120.dp)
            
            Text("Welcome Back", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                colors = authFieldColors(),
                singleLine = true
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                colors = authFieldColors(),
                singleLine = true
            )

            Button(
                onClick = { onLogin(email, password) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Login", fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = { /* Forgot Password logic */ }) {
                Text("Forgot Password?", color = TextMuted)
            }

            TextButton(onClick = onCreateAccount) {
                Text("Don't have an account? Create one", color = CoralOrange)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun SignupScreen(onSignup: (String, String, String, String, String, Double) -> Unit, onBackToLogin: () -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var institution by remember { mutableStateOf("") }
    var allowance by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NavyBackground)
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Student360Branding.Logo(size = 80.dp)
            Text("Create Your Student360 Account", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            
            OutlinedTextField(value = firstName, onValueChange = { firstName = it }, label = { Text("First Name") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Person, null) }, colors = authFieldColors(), singleLine = true)
            OutlinedTextField(value = lastName, onValueChange = { lastName = it }, label = { Text("Last Name") }, modifier = Modifier.fillMaxWidth(), colors = authFieldColors(), singleLine = true)
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.Email, null) }, colors = authFieldColors(), singleLine = true)
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), colors = authFieldColors(), singleLine = true)
            OutlinedTextField(value = confirmPassword, onValueChange = { confirmPassword = it }, label = { Text("Confirm Password") }, modifier = Modifier.fillMaxWidth(), visualTransformation = PasswordVisualTransformation(), colors = authFieldColors(), singleLine = true)
            
            Text("Optional Details", color = TextMuted, fontSize = 12.sp, modifier = Modifier.align(Alignment.Start).padding(top = 8.dp))
            OutlinedTextField(value = institution, onValueChange = { institution = it }, label = { Text("Institution") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.School, null) }, colors = authFieldColors(), singleLine = true)
            OutlinedTextField(value = allowance, onValueChange = { allowance = it }, label = { Text("Monthly Allowance (P)") }, modifier = Modifier.fillMaxWidth(), colors = authFieldColors(), singleLine = true)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { 
                    if (firstName.isBlank() || email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                    } else if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    } else {
                        onSignup(firstName, lastName, email, password, institution, allowance.toDoubleOrNull() ?: 0.0)
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Create Account", fontWeight = FontWeight.Bold)
            }

            TextButton(onClick = onBackToLogin) {
                Text("Already have an account? Login", color = CoralOrange)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun OnboardingScreen(viewModel: CompanionViewModel) {
    val user by viewModel.userProfile.collectAsState(null)
    
    Box(modifier = Modifier.fillMaxSize().background(NavyBackground).padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Student360Branding.Logo(size = 120.dp)
            Text("Welcome to Student360, ${user?.firstName}!", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            
            Text("Let's get your finances organised.", color = CoralOrange, fontWeight = FontWeight.Bold)

            Text(
                "Student360 helps you:\n" +
                "• Track spending automatically\n" +
                "• Analyse receipts with AI\n" +
                "• Plan your allowance\n" +
                "• Manage recurring expenses\n" +
                "• Get AI-powered financial guidance",
                color = TextMuted,
                lineHeight = 24.sp
            )

            Button(
                onClick = { 
                    viewModel.updateProfile(user!!.copy(hasCompletedOnboarding = true))
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CoralOrange),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("GET STARTED", fontWeight = FontWeight.Black)
            }
        }
    }
}
