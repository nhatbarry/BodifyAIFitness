package com.example.bodifyaifitness.pages

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.dataclass.User
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import com.example.bodifyaifitness.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SetupProfilePage(
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    var heightCm by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val uid   = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

    val bmiPreview = run {
        val h = heightCm.toFloatOrNull()
        val w = weightKg.toFloatOrNull()
        if (h != null && h > 0 && w != null && w > 0) w / ((h / 100f) * (h / 100f)) else null
    }

    val bmiUnderweight = stringResource(R.string.bmi_underweight)
    val bmiNormal      = stringResource(R.string.bmi_normal)
    val bmiOverweight  = stringResource(R.string.bmi_overweight)
    val bmiObese       = stringResource(R.string.bmi_obese)

    fun bmiLabel(bmi: Float) = when {
        bmi < 18.5f -> bmiUnderweight
        bmi < 25f   -> bmiNormal
        bmi < 30f   -> bmiOverweight
        else        -> bmiObese
    }

    fun bmiColor(bmi: Float) = when {
        bmi < 18.5f -> Color(0xFF5DADE2)
        bmi < 25f   -> Color(0xFF2ECC71)
        bmi < 30f   -> Color(0xFFF39C12)
        else        -> Color(0xFFE74C3C)
    }

    fun navigateToMain() {
        navController.navigate("main_app") { popUpTo("setup_profile") { inclusive = true } }
    }

    Box(modifier = Modifier.fillMaxSize().background(GymSurfaceBg)) {
        Box(
            modifier = Modifier.fillMaxWidth().height(280.dp)
                .background(Brush.verticalGradient(colors = listOf(Color(0xFF0F0F1E), GymSurfaceBg)))
        )
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(72.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(88.dp).clip(CircleShape).background(GymOrange.copy(alpha = 0.15f))
            ) {
                Icon(Icons.Default.FitnessCenter, null, tint = GymOrange, modifier = Modifier.size(44.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(R.string.title_setup),
                color = TextWhite, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.subtitle_setup),
                color = TextMuted, fontSize = 14.sp, lineHeight = 22.sp, textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(40.dp))

            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF12121F)).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(stringResource(R.string.label_body_stats), color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = heightCm,
                        onValueChange = { if (it.length <= 3) heightCm = it },
                        label = { Text(stringResource(R.string.label_height_cm), color = TextMuted, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Height, null, tint = GymOrange, modifier = Modifier.size(18.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GymOrange, unfocusedBorderColor = Color(0xFF2A2A3E),
                            focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                            cursorColor = GymOrange, focusedContainerColor = Color(0xFF1A1A2E), unfocusedContainerColor = Color(0xFF1A1A2E)
                        ),
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weightKg,
                        onValueChange = { if (it.length <= 5) weightKg = it },
                        label = { Text(stringResource(R.string.label_weight_kg), color = TextMuted, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.FitnessCenter, null, tint = GymOrange, modifier = Modifier.size(18.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GymOrange, unfocusedBorderColor = Color(0xFF2A2A3E),
                            focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
                            cursorColor = GymOrange, focusedContainerColor = Color(0xFF1A1A2E), unfocusedContainerColor = Color(0xFF1A1A2E)
                        ),
                        shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)
                    )
                }
                if (bmiPreview != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                            .background(GymSurfaceBg).padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(stringResource(R.string.label_bmi), color = TextMuted, fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("%.1f".format(bmiPreview), color = bmiColor(bmiPreview), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(bmiLabel(bmiPreview), color = bmiColor(bmiPreview), fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    isSaving = true
                    userViewModel.saveUserProfile(User(id = uid, email = email, height = heightCm.toFloatOrNull() ?: 0f, weight = weightKg.toFloatOrNull() ?: 0f)) {
                        isSaving = false; navigateToMain()
                    }
                },
                enabled = !isSaving && (heightCm.isNotBlank() || weightKg.isNotBlank()),
                colors = ButtonDefaults.buttonColors(containerColor = GymOrange, contentColor = Color.White, disabledContainerColor = GymOrange.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Text(stringResource(R.string.btn_save_start), fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = { navigateToMain() }) {
                Icon(Icons.Default.SkipNext, null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(stringResource(R.string.btn_skip), color = TextMuted, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
