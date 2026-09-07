package com.example.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun ScanningCameraScreen(
    onDismiss: () -> Unit,
    onReceiptDetected: (merchant: String, amount: Double, date: String, category: String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    var flashEnabled by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var detectedData by remember { mutableStateOf<ReceiptData?>(null) }
    
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "scanner")
    val scanY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scan_line"
    )

    LaunchedEffect(flashEnabled) {
        cameraControl?.enableTorch(flashEnabled)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (detectedData == null) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            ) { view ->
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(view.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageCapture
                        )
                        cameraControl = camera.cameraControl
                    } catch (exc: Exception) {
                        Log.e("ScanningCamera", "Use case binding failed", exc)
                    }
                }, ContextCompat.getMainExecutor(context))
            }

            // Scanner Overlay
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text("Scan Receipt", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    IconButton(onClick = { flashEnabled = !flashEnabled }) {
                        Icon(
                            if (flashEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Flash",
                            tint = Color.White
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .border(2.dp, Color(0xFFF15A24).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.01f)
                            .offset(y = (scanY * 300).dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xFFF15A24), Color.Transparent)
                                )
                            )
                    )
                }

                if (isProcessing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFFF15A24))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("AI Extracting Data...", color = Color.White, fontSize = 12.sp)
                    }
                } else {
                    Button(
                        onClick = {
                            isProcessing = true
                            takePhotoAndProcess(
                                imageCapture,
                                cameraExecutor,
                                onSuccess = { data ->
                                    isProcessing = false
                                    detectedData = data
                                },
                                onError = {
                                    isProcessing = false
                                    Log.e("ScanningCamera", "Error: $it")
                                }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF15A24)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("CAPTURE & SCAN", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        } else {
            // Review Screen
            ReviewReceiptScreen(
                data = detectedData!!,
                onConfirm = { 
                    onReceiptDetected(it.merchant, it.amount, it.date, it.category)
                },
                onCancel = { detectedData = null }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

@Composable
fun ReviewReceiptScreen(
    data: ReceiptData,
    onConfirm: (ReceiptData) -> Unit,
    onCancel: () -> Unit
) {
    var merchant by remember { mutableStateOf(data.merchant) }
    var amountText by remember { mutableStateOf(data.amount.toString()) }
    var category by remember { mutableStateOf(data.category) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF001F3F)).padding(24.dp), contentAlignment = Alignment.Center) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF002D5A)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Review Scanned Receipt", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                
                OutlinedTextField(
                    value = merchant,
                    onValueChange = { merchant = it },
                    label = { Text("Merchant", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, 
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF15A24),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedLabelColor = Color(0xFFF15A24),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Total Amount (P)", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, 
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF15A24),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedLabelColor = Color(0xFFF15A24),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category", color = Color.White) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, 
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFFF15A24),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedLabelColor = Color(0xFFF15A24),
                        unfocusedLabelColor = Color.White.copy(alpha = 0.7f)
                    )
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Retake")
                    }
                    Button(
                        onClick = { 
                            onConfirm(ReceiptData(merchant, amountText.toDoubleOrNull() ?: 0.0, data.date, category))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF15A24))
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

private fun takePhotoAndProcess(
    imageCapture: ImageCapture,
    executor: ExecutorService,
    onSuccess: (ReceiptData) -> Unit,
    onError: (String) -> Unit
) {
    imageCapture.takePicture(
        executor,
        object : ImageCapture.OnImageCapturedCallback() {
            @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
            override fun onCaptureSuccess(imageProxy: ImageProxy) {
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                    
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            onSuccess(parseReceiptText(visionText.text))
                            imageProxy.close()
                        }
                        .addOnFailureListener { e ->
                            onError(e.message ?: "OCR Failed")
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                    onError("Image is null")
                }
            }

            override fun onError(exception: ImageCaptureException) {
                onError(exception.message ?: "Capture Failed")
            }
        }
    )
}

private fun parseReceiptText(text: String): ReceiptData {
    val lines = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
    var merchant = "Unknown Merchant"
    var amount = 0.0
    var category = "Other"

    if (lines.isNotEmpty()) {
        merchant = lines[0]
    }

    val amountRegex = Regex("""(?i)(?:total|amount|due|pula|bwp)?\s*[P]?\s*(\d+[.,]\d{2})""")
    val foundAmounts = amountRegex.findAll(text).map { 
        it.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 0.0 
    }.toList()
    
    if (foundAmounts.isNotEmpty()) {
        amount = foundAmounts.maxOrNull() ?: 0.0
    }

    val textLower = text.lowercase()
    when {
        textLower.contains("choppies") || textLower.contains("spar") || textLower.contains("pick n pay") -> category = "Groceries"
        textLower.contains("orange") || textLower.contains("mascom") || textLower.contains("btc") -> category = "Data/WiFi"
        textLower.contains("kfc") || textLower.contains("hungry lion") || textLower.contains("nandos") -> category = "Food & Takeaways"
        textLower.contains("combi") || textLower.contains("taxi") || textLower.contains("shell") -> category = "Transport"
    }

    return ReceiptData(merchant, amount, "Today", category)
}

data class ReceiptData(val merchant: String, val amount: Double, val date: String, val category: String)
