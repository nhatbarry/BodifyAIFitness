package com.example.bodifyaifitness.pages

import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.composable.BmiCard
import com.example.bodifyaifitness.composable.WorkoutStreakChart
import com.example.bodifyaifitness.dataclass.User
import com.example.bodifyaifitness.ui.theme.ChipInactive
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import com.example.bodifyaifitness.viewmodel.AuthState
import com.example.bodifyaifitness.viewmodel.AuthViewModel
import com.example.bodifyaifitness.viewmodel.UserProfileState
import com.example.bodifyaifitness.viewmodel.UserViewModel
import com.example.bodifyaifitness.viewmodel.WorkoutLogViewModel
import java.time.LocalDate

@Composable
fun AccountPage(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    navController: NavController,
    userViewModel: UserViewModel = viewModel()
) {
    val authState = authViewModel.authState.observeAsState()
    val userState = userViewModel.userState.observeAsState()

    LaunchedEffect(Unit) { userViewModel.loadUserProfile() }

    val user: User? = (userState.value as? UserProfileState.Success)?.user
    val displayName  = user?.name?.ifBlank { null } ?: stringResource(R.string.default_athlete)
    val displayEmail = user?.email?.ifBlank { null } ?: "—"
    val bmi = user?.let { userViewModel.calculateBmi(it) }

    // Current locale for toggle label
    val currentLocale = AppCompatDelegate.getApplicationLocales().toLanguageTags().take(2)
    val isVi = currentLocale.startsWith("vi")

    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate("login_page") { popUpTo(0) { inclusive = true } }
        }
    }

    // ── Workout stats (Activity-scoped ViewModel) ─────────────────────────────
    val activity = LocalContext.current as ComponentActivity
    val workoutLogViewModel: WorkoutLogViewModel = viewModel(activity)
    val allLogsMap by workoutLogViewModel.allLogsMap.collectAsState()

    LaunchedEffect(Unit) { workoutLogViewModel.loadAllLogs() }

    // Computed stats
    val totalSessions  = allLogsMap.size
    val totalExercises = allLogsMap.values.sumOf { it.exercise.size }
    val totalVolumeKg  = allLogsMap.values.sumOf { log ->
        log.exercise.sumOf { ex -> ex.sets.sumOf { it.weight * it.reps } }
    }

    // Build activityData: LocalDate → exercise count (for chart)
    val activityData = remember(allLogsMap) {
        allLogsMap.entries.associate { (dateKey, log) ->
            LocalDate.parse(dateKey) to log.exercise.size
        }
    }

    // ── Format helpers ────────────────────────────────────────────────────────
    fun formatVolume(kg: Double): String = when {
        kg >= 1_000.0 -> "${"%.1f".format(kg / 1000)}k kg"
        else          -> "${kg.toInt()} kg"
    }

    Column(
        modifier = modifier.fillMaxSize().background(GymSurfaceBg).verticalScroll(rememberScrollState())
    ) {
        // ── Profile Header ────────────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
                .background(Brush.verticalGradient(colors = listOf(Color(0xFF1A1A2E), GymSurfaceBg)))
                .padding(top = 32.dp, bottom = 24.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(96.dp).clip(CircleShape).background(ChipInactive)
                    .border(
                        width = 3.dp,
                        brush = Brush.sweepGradient(listOf(GymOrange, Color(0xFFFF4757), GymOrange)),
                        shape = CircleShape
                    )
            ) {
                val avatarUrl = user?.avatarUri
                if (!avatarUrl.isNullOrEmpty()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = "Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = "Avatar", tint = TextMuted, modifier = Modifier.size(56.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text(text = displayName, style = MaterialTheme.typography.headlineMedium, color = TextWhite, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = displayEmail, fontSize = 13.sp, color = TextMuted)
            Spacer(modifier = Modifier.height(20.dp))

            // ── Stats row ─────────────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
                StatItem(
                    label = stringResource(R.string.label_workouts), // "Exercises"
                    value = "$totalExercises"
                )
                StatDivider()
                StatItem(
                    label = stringResource(R.string.label_streak),   // "Sessions"
                    value = "$totalSessions"
                )
                StatDivider()
                StatItem(
                    label = stringResource(R.string.label_volume),   // "Volume"
                    value = formatVolume(totalVolumeKg)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { navController.navigate("edit_profile") },
                    colors = ButtonDefaults.buttonColors(containerColor = ChipInactive, contentColor = TextWhite),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_edit_profile), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Language toggle button
                OutlinedButton(
                    onClick = {
                        val newLocale = if (isVi) "en" else "vi"
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(newLocale))
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GymOrange),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GymOrange),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Default.Language, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isVi) "EN" else "VI", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                OutlinedButton(
                    onClick = { authViewModel.signOut() },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF4757)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF4757)),
                    shape = RoundedCornerShape(12.dp), modifier = Modifier.height(44.dp)
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.btn_sign_out), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }

        // ── BMI Card ──────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            BmiCard(bmi = bmi, heightCm = user?.height ?: 0f, weightKg = user?.weight ?: 0f)
        }

        // ── Activity Chart ────────────────────────────────────────────────────
        Spacer(modifier = Modifier.height(8.dp))
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            WorkoutStreakChart(activityData = activityData)
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, color = TextMuted, fontSize = 12.sp)
    }
}

@Composable
private fun StatDivider() {
    Box(modifier = Modifier.height(36.dp).width(1.dp).background(Color(0xFF2A2A3E)))
}
