package com.example.bodifyaifitness.pages

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import com.example.bodifyaifitness.viewmodel.AuthState
import com.example.bodifyaifitness.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpPage(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible        by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    val authState = authViewModel.authState.observeAsState()
    val context   = LocalContext.current

    val errMinLength   = stringResource(R.string.error_password_min_length)
    val errMismatch    = stringResource(R.string.error_password_mismatch)
    val signupSuccess  = stringResource(R.string.toast_signup_success)

    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Error -> {
                Toast.makeText(context, (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            }
            is AuthState.Authenticated -> {
                Toast.makeText(context, signupSuccess, Toast.LENGTH_SHORT).show()
                navController.navigate("setup_profile") { popUpTo("sign_up_page") { inclusive = true } }
            }
            else -> {}
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(GymSurfaceBg.copy(alpha = 0.82f)))

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.title_create_account),
                color = GymOrange, fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold, letterSpacing = 2.sp
            )
            Text(
                text = stringResource(R.string.subtitle_create_account),
                color = TextMuted, fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 36.dp)
            )

            OutlinedTextField(
                value = email, onValueChange = { email = it },
                label = { Text(stringResource(R.string.label_email), color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Email, null, tint = GymOrange) },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = GymOrange, unfocusedIndicatorColor = Color(0xFF2A2A3E),
                    focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                    focusedContainerColor = Color(0xFF12121F), unfocusedContainerColor = Color(0xFF12121F),
                    cursorColor = GymOrange, focusedLabelColor = GymOrange, unfocusedLabelColor = TextMuted
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password, onValueChange = { password = it; passwordError = null },
                label = { Text(stringResource(R.string.label_password), color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = GymOrange) },
                trailingIcon = {
                    val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(icon, null, tint = TextMuted)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = GymOrange, unfocusedIndicatorColor = Color(0xFF2A2A3E),
                    focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                    focusedContainerColor = Color(0xFF12121F), unfocusedContainerColor = Color(0xFF12121F),
                    cursorColor = GymOrange, focusedLabelColor = GymOrange, unfocusedLabelColor = TextMuted
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword, onValueChange = { confirmPassword = it; passwordError = null },
                label = { Text(stringResource(R.string.label_confirm_password), color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = GymOrange) },
                trailingIcon = {
                    val icon = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(icon, null, tint = TextMuted)
                    }
                },
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError != null,
                supportingText = {
                    if (passwordError != null) Text(text = passwordError!!, color = Color(0xFFFF4757), fontSize = 12.sp)
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = GymOrange, unfocusedIndicatorColor = Color(0xFF2A2A3E),
                    errorIndicatorColor = Color(0xFFFF4757),
                    focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                    focusedContainerColor = Color(0xFF12121F), unfocusedContainerColor = Color(0xFF12121F),
                    cursorColor = GymOrange, focusedLabelColor = GymOrange, unfocusedLabelColor = TextMuted
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(36.dp))

            Button(
                onClick = {
                    when {
                        password.length < 6      -> passwordError = errMinLength
                        password != confirmPassword -> passwordError = errMismatch
                        else                     -> authViewModel.signUp(email, password)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = GymOrange),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text(stringResource(R.string.btn_sign_up), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.already_have_account), color = TextMuted, fontSize = 15.sp)
                Text(
                    text = stringResource(R.string.login_link),
                    color = GymOrange, textDecoration = TextDecoration.Underline,
                    fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { navController.popBackStack() }.padding(4.dp)
                )
            }
        }
    }
}
