package com.example.bodifyaifitness.pages

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bodifyaifitness.dataclass.User
import com.example.bodifyaifitness.ui.theme.ChipInactive
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import com.example.bodifyaifitness.viewmodel.UserProfileState
import com.example.bodifyaifitness.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EditProfilePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val userState = userViewModel.userState.observeAsState()
    val currentUser = (userState.value as? UserProfileState.Success)?.user

    // Form state — initialised from loaded user
    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var heightCm by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf("") }
    var isSaving  by remember { mutableStateOf(false) }
    var savedOk   by remember { mutableStateOf(false) }

    // Populate fields when user data arrives
    LaunchedEffect(currentUser) {
        currentUser?.let {
            name      = it.name
            email     = it.email
            heightCm  = if (it.height > 0f) it.height.toInt().toString() else ""
            weightKg  = if (it.weight > 0f) it.weight.toInt().toString() else ""
            avatarUri = it.avatarUri
        }
    }

    LaunchedEffect(Unit) { userViewModel.loadUserProfile() }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { avatarUri = it.toString() }
    }

    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(GymSurfaceBg)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top bar ─────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1A1A2E), GymSurfaceBg)
                    )
                )
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = TextWhite
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Edit Profile",
                color = TextWhite,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        // ── Avatar picker ────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                // Avatar image / placeholder
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(ChipInactive)
                        .border(
                            width = 3.dp,
                            brush = Brush.sweepGradient(
                                listOf(GymOrange, Color(0xFFFF4757), GymOrange)
                            ),
                            shape = CircleShape
                        )
                        .clickable { imagePickerLauncher.launch("image/*") }
                ) {
                    if (avatarUri.isNotEmpty()) {
                        AsyncImage(
                            model = avatarUri,
                            contentDescription = "Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                }

                // Camera overlay badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GymOrange)
                        .clickable { imagePickerLauncher.launch("image/*") }
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Upload photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tap to change photo",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        // ── Form fields ──────────────────────────────────────────────────────
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            ProfileTextField(
                value = name,
                onValueChange = { name = it },
                label = "Display Name",
                leadingIcon = Icons.Default.Person,
                keyboardType = KeyboardType.Text
            )

            ProfileTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                leadingIcon = Icons.Default.Email,
                keyboardType = KeyboardType.Email
            )

            // Height & Weight side by side
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileTextField(
                    value = heightCm,
                    onValueChange = { if (it.length <= 3) heightCm = it },
                    label = "Height (cm)",
                    leadingIcon = Icons.Default.Height,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                ProfileTextField(
                    value = weightKg,
                    onValueChange = { if (it.length <= 5) weightKg = it },
                    label = "Weight (kg)",
                    leadingIcon = Icons.Default.FitnessCenter,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }

            // BMI preview
            val bmiPreview = run {
                val h = heightCm.toFloatOrNull()
                val w = weightKg.toFloatOrNull()
                if (h != null && h > 0 && w != null && w > 0)
                    w / ((h / 100f) * (h / 100f))
                else null
            }
            if (bmiPreview != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF12121F))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(text = "BMI Preview:", color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "%.1f".format(bmiPreview),
                        color = bmiColor(bmiPreview),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = bmiLabel(bmiPreview),
                        color = bmiColor(bmiPreview),
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Save button ──────────────────────────────────────────────
            Button(
                onClick = {
                    isSaving = true
                    savedOk  = false
                    val user = User(
                        id        = uid,
                        name      = name.trim(),
                        email     = email.trim(),
                        height    = heightCm.toFloatOrNull() ?: 0f,
                        weight    = weightKg.toFloatOrNull() ?: 0f,
                        avatarUri = avatarUri,
                        joinDate  = currentUser?.joinDate ?: System.currentTimeMillis()
                    )
                    userViewModel.saveUserProfile(user) {
                        isSaving = false
                        savedOk  = true
                        navController.popBackStack()
                    }
                },
                enabled = !isSaving && name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GymOrange,
                    contentColor   = Color.White,
                    disabledContainerColor = GymOrange.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label, color = TextMuted, fontSize = 13.sp)
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = GymOrange,
                modifier = Modifier.size(20.dp)
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor    = GymOrange,
            unfocusedBorderColor  = Color(0xFF2A2A3E),
            focusedTextColor      = TextWhite,
            unfocusedTextColor    = TextWhite,
            cursorColor           = GymOrange,
            focusedContainerColor = Color(0xFF12121F),
            unfocusedContainerColor = Color(0xFF12121F)
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.fillMaxWidth()
    )
}

private fun bmiColor(bmi: Float): Color = when {
    bmi < 18.5f -> Color(0xFF5DADE2)
    bmi < 25f   -> Color(0xFF2ECC71)
    bmi < 30f   -> Color(0xFFF39C12)
    else        -> Color(0xFFE74C3C)
}

private fun bmiLabel(bmi: Float): String = when {
    bmi < 18.5f -> "Underweight"
    bmi < 25f   -> "Normal"
    bmi < 30f   -> "Overweight"
    else        -> "Obese"
}
