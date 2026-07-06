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
 * CameraX [ImageAnalysis.Analyzer]: decodes each frame with ML Kit Pose Detection
 * (stream mode) and forwards the pose to the active [ExerciseProcessor]. This class only
 * ever runs on the [ImageAnalysis] background executor supplied at bind time — it must
 * never be invoked on the main thread, and detection results come back on that same
 * executor's thread via the ML Kit listeners.
 */
class PoseAnalyzer(
    private var processor: ExerciseProcessor,
    private val onResult: (ProcessorResult) -> Unit,
    private val onPoseDetected: (Pose) -> Unit = {}
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

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        detector.process(inputImage)
            .addOnSuccessListener { pose ->
                onPoseDetected(pose)
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
