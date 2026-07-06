package com.example.bodifyaifitness.pages

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.bodifyaifitness.R
import com.example.bodifyaifitness.dataclass.Exercise
import com.example.bodifyaifitness.posecoach.FeedbackKey
import com.example.bodifyaifitness.posecoach.PoseAnalyzer
import com.example.bodifyaifitness.posecoach.PoseFrame
import com.example.bodifyaifitness.posecoach.PoseOverlay
import com.example.bodifyaifitness.posecoach.ProcessorResult
import com.example.bodifyaifitness.posecoach.TtsCoach
import com.example.bodifyaifitness.posecoach.buildPoseImageAnalysis
import com.example.bodifyaifitness.posecoach.exerciseProcessorFor
import com.example.bodifyaifitness.ui.theme.GymOrange
import com.example.bodifyaifitness.ui.theme.GymSurfaceBg
import com.example.bodifyaifitness.ui.theme.TextMuted
import com.example.bodifyaifitness.ui.theme.TextWhite
import com.example.bodifyaifitness.viewmodel.PoseCameraState
import com.example.bodifyaifitness.viewmodel.PoseCameraViewModel
import java.util.concurrent.Executors
import kotlinx.coroutines.delay

/**
 * Camera-based rep counter / form coach for exercises with Exercise.isAISupported = true.
 * Loads the exercise, resolves its ExerciseProcessor (Squat/PushUp) via aiAlgorithm, then
 * binds CameraX + ML Kit Pose Detection and overlays the live skeleton + rep count + feedback.
 * Defaults to the back camera (better for framing a full-body exercise from a few steps away)
 * but the user can flip to the front camera at any time.
 */
@Composable
fun PoseCameraPage(
    exerciseId: String,
    navController: NavController,
    viewModel: PoseCameraViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(exerciseId) { viewModel.loadExercise(exerciseId) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (val s = state) {
            is PoseCameraState.Loading -> CenteredMessage { CircularProgressIndicator(color = GymOrange) }

            is PoseCameraState.Error -> CenteredMessage {
                Text(s.message, color = TextMuted, fontSize = 14.sp)
            }

            is PoseCameraState.Unsupported -> CenteredMessage {
                Text(
                    "Bài tập này chưa được camera AI hỗ trợ.",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }

            is PoseCameraState.Ready -> PoseCameraContent(exercise = s.exercise, navController = navController)
        }

        // Top bar luôn hiện, kể cả khi đang loading/lỗi, để người dùng thoát ra được.
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .statusBarsPadding()
                .padding(4.dp)
        ) {
            Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Đóng", tint = TextWhite)
        }
    }
}

private fun FeedbackKey.stringRes(): Int = when (this) {
    FeedbackKey.GO_DEEPER -> R.string.pose_feedback_go_deeper
    FeedbackKey.BACK_STRAIGHT -> R.string.pose_feedback_back_straight
    FeedbackKey.RAISE_HIPS -> R.string.pose_feedback_raise_hips
    FeedbackKey.LOWER_HIPS -> R.string.pose_feedback_lower_hips
    FeedbackKey.LOWER_FURTHER -> R.string.pose_feedback_lower_further
}

@Composable
private fun CenteredMessage(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun PoseCameraContent(exercise: Exercise, navController: NavController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!hasCameraPermission) {
        CenteredMessage {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Cần quyền Camera để đếm rep tự động.",
                    color = TextWhite,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(12.dp))
                TextButton(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Cấp quyền Camera", color = GymOrange, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        return
    }

    val processor = remember(exercise.id) { exerciseProcessorFor(exercise) }
    if (processor == null) {
        CenteredMessage {
            Text("Thuật toán AI \"${exercise.aiAlgorithm}\" chưa được hỗ trợ.", color = TextMuted, fontSize = 14.sp)
        }
        return
    }

    val ttsCoach = remember { TtsCoach(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }

    var useFrontCamera by remember { mutableStateOf(false) }
    var poseFrame by remember { mutableStateOf<PoseFrame?>(null) }
    var result by remember { mutableStateOf<ProcessorResult?>(null) }
    var displayedFeedback by remember { mutableStateOf<String?>(null) }

    val analyzer = remember(processor) {
        PoseAnalyzer(
            processor = processor,
            onResult = { r ->
                result = r
                r.feedback?.let { fb ->
                    // context.getString() is a plain resource lookup, safe to call from this
                    // background analyzer-thread callback (unlike stringResource(), which
                    // requires being inside composition).
                    val text = context.getString(fb.key.stringRes())
                    displayedFeedback = text
                    ttsCoach.speak(text)
                }
            },
            onPoseDetected = { frame -> poseFrame = frame }
        )
    }

    // Clear the feedback banner a bit after the processor's own cooldown so it doesn't linger.
    LaunchedEffect(displayedFeedback) {
        if (displayedFeedback != null) {
            delay(3000)
            displayedFeedback = null
        }
    }

    // Rebinds whenever the camera facing is flipped. Kept separate from the one-time
    // teardown below so flipping doesn't shut down the (reusable) analyzer/executor/TTS.
    DisposableEffect(analyzer, useFrontCamera) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val cameraProvider = providerFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val imageAnalysis = buildPoseImageAnalysis(analyzer, cameraExecutor)
            val selector = if (useFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA
                           else CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis)
            } catch (_: Exception) {
                // Binding can fail if the selected camera isn't available; preview stays blank.
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            if (providerFuture.isDone) providerFuture.get().unbindAll()
        }
    }

    // One-time teardown when leaving the screen entirely.
    DisposableEffect(Unit) {
        onDispose {
            analyzer.shutdown()
            cameraExecutor.shutdown()
            ttsCoach.shutdown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        PoseOverlay(
            frame = poseFrame,
            isFrontCamera = useFrontCamera,
            modifier = Modifier.fillMaxSize()
        )

        IconButton(
            onClick = {
                poseFrame = null // tránh khung skeleton cũ hiện sai 1 nhịp khi vừa đổi camera
                useFrontCamera = !useFrontCamera
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.4f))
            ) {
                Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Đổi camera", tint = TextWhite)
            }
        }

        // ── Feedback banner ──────────────────────────────────────────────────
        displayedFeedback?.let { text ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(top = 56.dp, start = 16.dp, end = 16.dp)
                    .align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GymOrange.copy(alpha = 0.92f))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }

        // ── Bottom rep counter + Stop ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(GymSurfaceBg.copy(alpha = 0.9f))
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = exercise.name.replaceFirstChar { it.uppercase() },
                        color = TextWhite,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (poseFrame == null) "Đang tìm người trong khung hình…" else "Đứng để camera thấy toàn thân",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "${result?.repCount ?: 0}",
                    color = GymOrange,
                    fontWeight = FontWeight.Black,
                    fontSize = 40.sp
                )
            }

            Spacer(Modifier.height(14.dp))

            Button(
                onClick = {
                    val repCount = result?.repCount ?: 0
                    // Bỏ màn camera khỏi back stack trước, để bấm Back từ trang log không
                    // quay lại camera nữa.
                    navController.popBackStack()
                    navController.navigate(
                        "exercise_detail/${exercise.id}?showLog=true&prefillReps=$repCount"
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE74C3C)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Dừng & ghi số rep", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
