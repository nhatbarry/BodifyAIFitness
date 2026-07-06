package com.example.bodifyaifitness.posecoach

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import java.util.concurrent.Executor

/**
 * One analyzed frame's pose plus the frame's own upright (post-rotation) dimensions —
 * [PoseOverlay] needs the latter to map ML Kit's image-space landmark coordinates onto the
 * on-screen preview.
 */
data class PoseFrame(val pose: Pose, val imageWidth: Int, val imageHeight: Int)

/**
 * CameraX [ImageAnalysis.Analyzer]: decodes each frame with ML Kit Pose Detection
 * (stream mode) and forwards the pose to the active [ExerciseProcessor]. This class only
 * ever runs on the [ImageAnalysis] background executor supplied at bind time — it must
 * never be invoked on the main thread, and detection results come back on that same
 * executor's thread via the ML Kit listeners.
 */
class PoseAnalyzer(
    private var processor: ExerciseProcessor,
    private val onResult: (ProcessorResult) -> Unit,
    private val onPoseDetected: (PoseFrame) -> Unit = {}
) : ImageAnalysis.Analyzer {

    private val detector: PoseDetector = PoseDetection.getClient(
        PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
    )

    /** Swap exercises (e.g. Squat <-> Push-up) without recreating the analyzer/detector. */
    fun switchExercise(newProcessor: ExerciseProcessor) {
        processor.reset()
        processor = newProcessor
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val rotation = imageProxy.imageInfo.rotationDegrees
        val inputImage = InputImage.fromMediaImage(mediaImage, rotation)

        // ML Kit returns landmarks in the upright (post-rotation) coordinate space, so a
        // 90/270 rotation means the logical width/height are the raw buffer's swapped.
        val (frameWidth, frameHeight) = if (rotation == 90 || rotation == 270) {
            imageProxy.height to imageProxy.width
        } else {
            imageProxy.width to imageProxy.height
        }

        detector.process(inputImage)
            .addOnSuccessListener { pose ->
                onPoseDetected(PoseFrame(pose, frameWidth, frameHeight))
                processor.process(pose)?.let(onResult)
            }
            .addOnCompleteListener {
                // Must close the ImageProxy or CameraX stalls the analysis pipeline —
                // this runs whether detection succeeded or failed.
                imageProxy.close()
            }
    }

    fun shutdown() = detector.close()
}

/**
 * Wires a [PoseAnalyzer] into an [ImageAnalysis] use case bound to a background executor,
 * e.g.:
 *
 * ```
 * val cameraExecutor = Executors.newSingleThreadExecutor()
 * val analyzer = PoseAnalyzer(processor = SquatProcessor(), onResult = { result -> ... })
 * val imageAnalysis = buildPoseImageAnalysis(analyzer, cameraExecutor)
 * cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageAnalysis)
 * ```
 */
fun buildPoseImageAnalysis(analyzer: PoseAnalyzer, executor: Executor): ImageAnalysis =
    ImageAnalysis.Builder()
        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
        .build()
        .also { it.setAnalyzer(executor, analyzer) }
