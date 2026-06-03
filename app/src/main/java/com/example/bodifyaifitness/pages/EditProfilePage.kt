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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.dataclass.User
import com.example.bodifyaifitness.ui.theme.ChipInactive
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import com.example.bodifyaifitness.utils.CloudinaryUploader
import com.example.bodifyaifitness.viewmodel.UserProfileState
import com.example.bodifyaifitness.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun EditProfilePage(
    modifier: Modifier = Modifier,
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val userState = userViewModel.userState.observeAsState()
    val currentUser = (userState.value as? UserProfileState.Success)?.user

    var name     by remember { mutableStateOf("") }
    var email    by remember { mutableStateOf("") }
    var heightCm by remember { mutableStateOf("") }
    var weightKg by remember { mutableStateOf("") }
    var avatarUri by remember { mutableStateOf("") }
    var isSaving          by remember { mutableStateOf(false) }
    var isUploadingAvatar by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val scope   = rememberCoroutineScope()
    val uid     = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    val uploadingText     = stringResource(R.string.uploading_avatar)
    val tapToChangeText   = stringResource(R.string.tap_to_change_photo)
    val bmiUnderweight    = stringResource(R.string.bmi_underweight)
    val bmiNormal         = stringResource(R.string.bmi_normal)
    val bmiOverweight     = stringResource(R.string.bmi_overweight)
    val bmiObese          = stringResource(R.string.bmi_obese)

    fun bmiColor(bmi: Float): Color = when {
        bmi < 18.5f -> Color(0xFF5DADE2)
        bmi < 25f   -> Color(0xFF2ECC71)
        bmi < 30f   -> Color(0xFFF39C12)
        else        -> Color(0xFFE74C3C)
    }
    fun bmiLabel(bmi: Float): String = when {
        bmi < 18.5f -> bmiUnderweight
        bmi < 25f   -> bmiNormal
        bmi < 30f   -> bmiOverweight
        else        -> bmiObese
    }

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

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            isUploadingAvatar = true
            scope.launch {
                val url = CloudinaryUploader.uploadAvatar(context, it)
                isUploadingAvatar = false
                if (url != null) avatarUri = url
            }
        }
    }

    Column(modifier = modifier.fillMaxSize().background(GymSurfaceBg).verticalScroll(rememberScrollState())) {
        // Top bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1A1A2E), GymSurfaceBg)))
                .padding(horizontal = 8.dp, vertical = 16.dp)
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBackIosNew, stringResource(R.string.content_desc_back), tint = TextWhite)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.title_edit_profile), color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        // Avatar
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(100.dp).clip(CircleShape).background(ChipInactive)
                        .border(3.dp, Brush.sweepGradient(listOf(GymOrange, Color(0xFFFF4757), GymOrange)), CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") }
                ) {
                    if (avatarUri.isNotEmpty()) {
                        AsyncImage(model = avatarUri, contentDescription = "Avatar", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(Icons.Default.Person, null, tint = TextMuted, modifier = Modifier.size(60.dp))
                    }
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(32.dp).clip(CircleShape)
                        .background(if (isUploadingAvatar) Color(0xFF2A2A3E) else GymOrange)
                        .clickable(enabled = !isUploadingAvatar) { imagePickerLauncher.launch("image/*") }
                ) {
                    if (isUploadingAvatar) {
                        CircularProgressIndicator(color = GymOrange, strokeWidth = 2.dp, modifier = Modifier.size(18.dp))
                    } else {
                        Icon(Icons.Default.AddAPhoto, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isUploadingAvatar) uploadingText else tapToChangeText,
                color = if (isUploadingAvatar) GymOrange else TextMuted, fontSize = 12.sp
            )
        }

        // Form fields
        Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.padding(horizontal = 20.dp)) {
            ProfileTextField(name, { name = it }, stringResource(R.string.label_display_name), Icons.Default.Person, KeyboardType.Text)
            ProfileTextField(email, { email = it }, stringResource(R.string.label_email), Icons.Default.Email, KeyboardType.Email)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ProfileTextField(heightCm, { if (it.length <= 3) heightCm = it }, stringResource(R.string.label_height), Icons.Default.Height, KeyboardType.Number, Modifier.weight(1f))
                ProfileTextField(weightKg, { if (it.length <= 5) weightKg = it }, stringResource(R.string.label_weight), Icons.Default.FitnessCenter, KeyboardType.Decimal, Modifier.weight(1f))
            }

            val bmiPreview = run {
                val h = heightCm.toFloatOrNull(); val w = weightKg.toFloatOrNull()
                if (h != null && h > 0 && w != null && w > 0) w / ((h / 100f) * (h / 100f)) else null
            }
            if (bmiPreview != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFF12121F)).padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(stringResource(R.string.label_bmi_preview), color = TextMuted, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("%.1f".format(bmiPreview), color = bmiColor(bmiPreview), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(bmiLabel(bmiPreview), color = bmiColor(bmiPreview), fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    isSaving = true
                    val user = User(id = uid, name = name.trim(), email = email.trim(), height = heightCm.toFloatOrNull() ?: 0f, weight = weightKg.toFloatOrNull() ?: 0f, avatarUri = avatarUri, joinDate = currentUser?.joinDate ?: System.currentTimeMillis())
                    userViewModel.saveUserProfile(user) { isSaving = false; navController.popBackStack() }
                },
                enabled = !isSaving && !isUploadingAvatar && name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GymOrange, contentColor = Color.White, disabledContainerColor = GymOrange.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(20.dp))
                } else {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_save_profile), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileTextField(value: String, onValueChange: (String) -> Unit, label: String, leadingIcon: ImageVector, keyboardType: KeyboardType, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, color = TextMuted, fontSize = 13.sp) },
        leadingIcon = { Icon(leadingIcon, null, tint = GymOrange, modifier = Modifier.size(20.dp)) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = GymOrange, unfocusedBorderColor = Color(0xFF2A2A3E),
            focusedTextColor = TextWhite, unfocusedTextColor = TextWhite,
            cursorColor = GymOrange, focusedContainerColor = Color(0xFF12121F), unfocusedContainerColor = Color(0xFF12121F)
        ),
        shape = RoundedCornerShape(12.dp), modifier = modifier.fillMaxWidth()
    )
}
