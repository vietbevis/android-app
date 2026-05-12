package vn.vietbevis.apkbasic.feature.capture

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import vn.vietbevis.apkbasic.core.di.AppContainer
import vn.vietbevis.apkbasic.domain.model.TransactionType
import vn.vietbevis.apkbasic.domain.model.UserProfile
import vn.vietbevis.apkbasic.ui.theme.CapExpenseCoral
import vn.vietbevis.apkbasic.ui.theme.CapIncomeMint
import vn.vietbevis.apkbasic.ui.theme.CapSurface
import vn.vietbevis.apkbasic.ui.theme.CapTextSecondary
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CaptureScreen(
    modifier: Modifier = Modifier,
    appContainer: AppContainer,
    userProfile: UserProfile,
) {
    val viewModel = remember(userProfile.id) {
        CaptureViewModel(
            userProfile = userProfile,
            walletRepository = appContainer.walletRepository,
            categoryRepository = appContainer.categoryRepository,
            transactionRepository = appContainer.transactionRepository,
            photoRepository = appContainer.photoRepository,
        )
    }
    val uiState by viewModel.uiState.collectAsState()
    CaptureContent(
        uiState = uiState,
        onPhotoCaptured = viewModel::onPhotoCaptured,
        onPhotoCaptureFailed = viewModel::onPhotoCaptureFailed,
        onRetakePhoto = viewModel::retakePhoto,
        onAmountChange = viewModel::onAmountChange,
        onCalculatorKey = viewModel::onCalculatorKey,
        onTypeChange = viewModel::onTypeChange,
        onWalletSelected = viewModel::onWalletSelected,
        onCategorySelected = viewModel::onCategorySelected,
        onNoteChange = viewModel::onNoteChange,
        onResetOccurredAt = viewModel::resetOccurredAtToNow,
        onSave = viewModel::save,
        onSaveWithoutPhoto = viewModel::saveWithoutPhoto,
        modifier = modifier,
    )
}

@Composable
private fun CaptureContent(
    uiState: CaptureUiState,
    onPhotoCaptured: (String) -> Unit,
    onPhotoCaptureFailed: () -> Unit,
    onRetakePhoto: () -> Unit,
    onAmountChange: (String) -> Unit,
    onCalculatorKey: (CalculatorKey) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onWalletSelected: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onResetOccurredAt: () -> Unit,
    onSave: () -> Unit,
    onSaveWithoutPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
    }

    when {
        uiState.isLoading -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        !hasCameraPermission -> PermissionRationale(
            modifier = modifier,
            onRequestPermission = { permissionLauncher.launch(Manifest.permission.CAMERA) },
        )
        else -> CaptureForm(
            uiState = uiState,
            onPhotoCaptured = onPhotoCaptured,
            onPhotoCaptureFailed = onPhotoCaptureFailed,
            onRetakePhoto = onRetakePhoto,
            onAmountChange = onAmountChange,
            onCalculatorKey = onCalculatorKey,
            onTypeChange = onTypeChange,
            onWalletSelected = onWalletSelected,
            onCategorySelected = onCategorySelected,
            onNoteChange = onNoteChange,
            onResetOccurredAt = onResetOccurredAt,
            onSave = onSave,
            onSaveWithoutPhoto = onSaveWithoutPhoto,
            modifier = modifier,
        )
    }
}

@Composable
private fun PermissionRationale(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Cần quyền camera",
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = "SnapChi dùng camera để chụp hóa đơn và tạo giao dịch nhanh.",
            modifier = Modifier.padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
            onClick = onRequestPermission,
            modifier = Modifier.padding(top = 20.dp),
        ) {
            Text("Cấp quyền camera")
        }
    }
}

@Composable
private fun AmountCalculatorPanel(
    calculatorState: CalculatorState,
    transactionType: TransactionType,
    enabled: Boolean,
    onCalculatorKey: (CalculatorKey) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = CapSurface,
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(
                    text = if (transactionType == TransactionType.EXPENSE) "-" else "+",
                    style = MaterialTheme.typography.headlineLarge,
                    color = if (transactionType == TransactionType.EXPENSE) CapExpenseCoral else CapIncomeMint,
                )
                Text(
                    text = calculatorState.amountInput,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                )
                Text(" d", color = CapTextSecondary, style = MaterialTheme.typography.titleLarge)
            }
            Text(
                text = "Thêm chi tiết",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                color = CapTextSecondary,
            )
            CalculatorKeypad(enabled = enabled, onCalculatorKey = onCalculatorKey)
        }
    }
}

@Composable
private fun CalculatorKeypad(
    enabled: Boolean,
    onCalculatorKey: (CalculatorKey) -> Unit,
) {
    val rows = listOf(
        listOf(KeySpec("1", CalculatorKey.Digit("1")), KeySpec("2", CalculatorKey.Digit("2")), KeySpec("3", CalculatorKey.Digit("3")), KeySpec("÷", CalculatorKey.Operator(CalculatorOperator.DIVIDE), true)),
        listOf(KeySpec("4", CalculatorKey.Digit("4")), KeySpec("5", CalculatorKey.Digit("5")), KeySpec("6", CalculatorKey.Digit("6")), KeySpec("×", CalculatorKey.Operator(CalculatorOperator.MULTIPLY), true)),
        listOf(KeySpec("7", CalculatorKey.Digit("7")), KeySpec("8", CalculatorKey.Digit("8")), KeySpec("9", CalculatorKey.Digit("9")), KeySpec("-", CalculatorKey.Operator(CalculatorOperator.SUBTRACT), true)),
        listOf(KeySpec(".", CalculatorKey.Decimal), KeySpec("0", CalculatorKey.Digit("0")), KeySpec("000", CalculatorKey.TripleZero), KeySpec("+", CalculatorKey.Operator(CalculatorOperator.ADD), true)),
        listOf(KeySpec("⌫", CalculatorKey.Backspace), KeySpec("C", CalculatorKey.Clear, true), KeySpec("=", CalculatorKey.Equals, true, wide = true)),
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { key ->
                    Button(
                        onClick = { onCalculatorKey(key.key) },
                        modifier = Modifier
                            .weight(if (key.wide) 2f else 1f)
                            .height(58.dp),
                        enabled = enabled,
                    ) {
                        Text(
                            text = key.label,
                            color = if (key.accent) CapExpenseCoral else MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
            }
        }
    }
}

private data class KeySpec(
    val label: String,
    val key: CalculatorKey,
    val accent: Boolean = false,
    val wide: Boolean = false,
)

@Composable
private fun CaptureForm(
    uiState: CaptureUiState,
    onPhotoCaptured: (String) -> Unit,
    onPhotoCaptureFailed: () -> Unit,
    onRetakePhoto: () -> Unit,
    onAmountChange: (String) -> Unit,
    onCalculatorKey: (CalculatorKey) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onWalletSelected: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onResetOccurredAt: () -> Unit,
    onSave: () -> Unit,
    onSaveWithoutPhoto: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text("Chụp giao dịch", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        PhotoCapturePanel(
            photoPath = uiState.selectedPhotoPath,
            isSaving = uiState.isSaving,
            onPhotoCaptured = onPhotoCaptured,
            onPhotoCaptureFailed = onPhotoCaptureFailed,
            onRetakePhoto = onRetakePhoto,
        )
        Spacer(Modifier.height(16.dp))
        AmountCalculatorPanel(
            calculatorState = uiState.calculatorState,
            transactionType = uiState.type,
            enabled = !uiState.isSaving,
            onCalculatorKey = onCalculatorKey,
        )
        Spacer(Modifier.height(12.dp))
        TypeSelector(
            selectedType = uiState.type,
            enabled = !uiState.isSaving,
            onTypeChange = onTypeChange,
        )
        Spacer(Modifier.height(12.dp))
        SelectionGroup(
            title = "Ví",
            options = uiState.wallets.map { it.id to it.name },
            selectedId = uiState.selectedWalletId,
            enabled = !uiState.isSaving,
            onSelected = onWalletSelected,
            emptyMessage = "Chưa có ví. Vui lòng tải lại hoặc đăng nhập lại để khởi tạo dữ liệu.",
        )
        Spacer(Modifier.height(12.dp))
        SelectionGroup(
            title = "Danh mục",
            options = uiState.categories
                .filter { it.transactionType == uiState.type }
                .map { it.id to it.name },
            selectedId = uiState.selectedCategoryId,
            enabled = !uiState.isSaving,
            onSelected = onCategorySelected,
            emptyMessage = "Chưa có danh mục cho loại giao dịch này.",
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = uiState.noteInput,
            onValueChange = onNoteChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Ghi chú") },
            minLines = 2,
            enabled = !uiState.isSaving,
        )
        Spacer(Modifier.height(12.dp))
        OccurredAtRow(
            epochMillis = uiState.occurredAtEpochMillis,
            enabled = !uiState.isSaving,
            onResetOccurredAt = onResetOccurredAt,
        )
        uiState.errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }
        uiState.infoMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isSaving && uiState.wallets.isNotEmpty(),
        ) {
            Text(if (uiState.isSaving) "Đang lưu..." else "Lưu giao dịch")
        }
        if (uiState.canSaveWithoutPhoto) {
            OutlinedButton(
                onClick = onSaveWithoutPhoto,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving,
            ) {
                Text("Lưu không ảnh")
            }
        }
    }
}

@Composable
private fun PhotoCapturePanel(
    photoPath: String?,
    isSaving: Boolean,
    onPhotoCaptured: (String) -> Unit,
    onPhotoCaptureFailed: () -> Unit,
    onRetakePhoto: () -> Unit,
) {
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashEnabled by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f),
        shape = MaterialTheme.shapes.medium,
        color = Color.Black,
    ) {
        Box(Modifier.fillMaxSize()) {
            if (photoPath == null) {
                CameraPreview(
                    lensFacing = lensFacing,
                    flashEnabled = flashEnabled,
                    onImageCaptureReady = { imageCapture = it },
                    modifier = Modifier.fillMaxSize(),
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.42f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedButton(
                        onClick = { flashEnabled = !flashEnabled },
                        modifier = Modifier.semantics {
                            contentDescription = if (flashEnabled) "Tắt flash" else "Bật flash"
                        },
                        enabled = !isSaving,
                    ) {
                        Text(if (flashEnabled) "Tắt flash" else "Bật flash")
                    }
                    Button(
                        onClick = {
                            val file = File(context.cacheDir, "snapchi-${System.currentTimeMillis()}.jpg")
                            val output = ImageCapture.OutputFileOptions.Builder(file).build()
                            imageCapture?.takePicture(
                                output,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                        onPhotoCaptured(file.absolutePath)
                                    }

                                    override fun onError(exception: ImageCaptureException) {
                                        onPhotoCaptureFailed()
                                    }
                                },
                            )
                        },
                        modifier = Modifier
                            .size(72.dp)
                            .semantics { contentDescription = "Chụp ảnh giao dịch" },
                        shape = CircleShape,
                        enabled = !isSaving && imageCapture != null,
                    ) {
                        Text("Chụp")
                    }
                    OutlinedButton(
                        onClick = {
                            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                CameraSelector.LENS_FACING_FRONT
                            } else {
                                CameraSelector.LENS_FACING_BACK
                            }
                        },
                        modifier = Modifier.semantics { contentDescription = "Đổi camera trước sau" },
                        enabled = !isSaving,
                    ) {
                        Text("Đổi")
                    }
                }
            } else {
                CapturedPhotoPreview(photoPath = photoPath, modifier = Modifier.fillMaxSize())
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.42f))
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    OutlinedButton(
                        onClick = onRetakePhoto,
                        modifier = Modifier.semantics { contentDescription = "Chụp lại ảnh giao dịch" },
                        enabled = !isSaving,
                    ) {
                        Text("Chụp lại")
                    }
                }
            }
        }
    }
}

@Composable
private fun CameraPreview(
    lensFacing: Int,
    flashEnabled: Boolean,
    onImageCaptureReady: (ImageCapture) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier,
    )

    LaunchedEffect(lensFacing, flashEnabled) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()
        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setJpegQuality(82)
            .setFlashMode(if (flashEnabled) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF)
            .build()
        val selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
        cameraProvider.unbindAll()
        runCatching {
            cameraProvider.bindToLifecycle(lifecycleOwner, selector, preview, imageCapture)
            onImageCaptureReady(imageCapture)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ProcessCameraProvider.getInstance(context).addListener(
                { ProcessCameraProvider.getInstance(context).get().unbindAll() },
                ContextCompat.getMainExecutor(context),
            )
        }
    }
}

@Composable
private fun CapturedPhotoPreview(
    photoPath: String,
    modifier: Modifier = Modifier,
) {
    val imageBitmap = remember(photoPath) {
        BitmapFactory.decodeFile(photoPath)?.asImageBitmap()
    }
    if (imageBitmap == null) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text("Không đọc được ảnh", color = Color.White)
        }
    } else {
        Image(
            bitmap = imageBitmap,
            contentDescription = "Ảnh giao dịch vừa chụp",
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun TypeSelector(
    selectedType: TransactionType,
    enabled: Boolean,
    onTypeChange: (TransactionType) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TransactionType.entries.forEach { type ->
            Row(
                modifier = Modifier.selectable(
                    selected = selectedType == type,
                    enabled = enabled,
                    onClick = { onTypeChange(type) },
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selectedType == type,
                    onClick = { onTypeChange(type) },
                    enabled = enabled,
                )
            Text(if (type == TransactionType.EXPENSE) "Chi" else "Thu")
            }
        }
    }
}

@Composable
private fun SelectionGroup(
    title: String,
    options: List<Pair<String, String>>,
    selectedId: String?,
    enabled: Boolean,
    onSelected: (String) -> Unit,
    emptyMessage: String,
) {
    Text(title, style = MaterialTheme.typography.titleSmall)
    if (options.isEmpty()) {
        Text(emptyMessage, color = MaterialTheme.colorScheme.onSurfaceVariant)
        return
    }
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        options.forEach { (id, label) ->
            FilterChip(
                selected = id == selectedId,
                onClick = { onSelected(id) },
                label = { Text(label) },
                enabled = enabled,
            )
        }
    }
}

@Composable
private fun OccurredAtRow(
    epochMillis: Long,
    enabled: Boolean,
    onResetOccurredAt: () -> Unit,
) {
    val formatted = remember(epochMillis) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("vi-VN")).format(Date(epochMillis))
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Thời gian", style = MaterialTheme.typography.titleSmall)
            Text(formatted, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(12.dp))
        OutlinedButton(
            onClick = onResetOccurredAt,
            enabled = enabled,
        ) {
            Text("Bây giờ")
        }
    }
}
