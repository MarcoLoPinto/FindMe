package com.liner.findme.ui.home.photo

import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.work.*
import coil.compose.AsyncImage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.liner.findme.R
import com.liner.findme.extensions.getCameraProvider
import com.liner.findme.ui.composable.FindMeLottieAnimation
import com.liner.findme.ui.theme.FindMeTheme
import com.liner.findme.worker.PhotoCardWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class PhotoFragment : Fragment() {

    // region private properties

    private val outputDirectory: File by lazy {
        val mediaDir = requireActivity().externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        if (mediaDir != null && mediaDir.exists()) mediaDir else requireContext().filesDir
    }
    private val cameraExecutor: ExecutorService =
        Executors.newSingleThreadExecutor() // creating new thread

    private lateinit var newPhotoData: NewPhotoData

    private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)
    private var shouldShowPhoto: MutableState<Boolean> = mutableStateOf(false)
    private var shouldShowLoader: MutableState<Boolean> = mutableStateOf(false)

    private val viewModel: PhotoViewModel by viewModels<PhotoViewModelImpl>()

    private val constraints by lazy {
        Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(false)
            .setRequiresCharging(false)
            .setRequiresDeviceIdle(false)
            .setRequiresStorageNotLow(false)
            .build()
    }

    // endregion

    // region public properties

    @Inject
    internal lateinit var workManager: WorkManager

    // endregion

    // region lifecycle

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext())

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (view as ComposeView).setContent {

            FindMeTheme() {

                if (shouldShowLoader.value) {
                    FindMeLottieAnimation(R.raw.world_loading)
                } else {
                    if (shouldShowPhoto.value) {
                        Preview()
                    } else {
                        Photo(
                            outputDirectory = outputDirectory,
                            executor = cameraExecutor,
                            onImageCaptured = ::handleImageCapture,
                            onError = {})
                    }
                }

            }

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    // endregion

    // region composable

    @Composable
    fun Preview() {

        Column(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = newPhotoData.uri,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 20.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        shouldShowPhoto.value = false
                        shouldShowCamera.value = true
                    }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    }
                    IconButton(onClick = {
                        shouldShowLoader.value = true
                        uploadPhoto()
                    }) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    }
                }

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = newPhotoData.caption.value,
                    label = { Text(text = "caption") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    onValueChange = { caption ->
                        newPhotoData.caption.value = caption
                    })

            }
        }

    }

    @Composable
    fun Photo(
        outputDirectory: File,
        executor: Executor,
        onImageCaptured: (Uri) -> Unit,
        onError: (ImageCaptureException) -> Unit
    ) {
        // 1
        var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_FRONT) }
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val preview = Preview.Builder().build()
        val previewView = remember { PreviewView(context) }
        val imageCapture: ImageCapture =
            remember { ImageCapture.Builder().setTargetResolution(Size(1080, 1080)).build() }
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        // 2
        LaunchedEffect(lensFacing) {
            val cameraProvider = context.getCameraProvider()
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            preview.setSurfaceProvider(previewView.surfaceProvider)
        }

        // 3
        Box(contentAlignment = Alignment.BottomCenter, modifier = Modifier.fillMaxSize()) {
            AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

            IconButton(
                modifier = Modifier.padding(bottom = 20.dp),
                onClick = {
                    takePhoto(
                        imageCapture = imageCapture,
                        outputDirectory = outputDirectory,
                        executor = executor,
                        onImageCaptured = onImageCaptured,
                        onError = onError
                    )
                },
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_take_photo),
                        contentDescription = "Take picture",
                        tint = Color.White,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(1.dp)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
            )

            Box(contentAlignment = Alignment.BottomEnd, modifier = Modifier.fillMaxSize()) {
                IconButton(
                    modifier = Modifier.padding(bottom = 20.dp, end = 10.dp),
                    onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_FRONT)
                            CameraSelector.LENS_FACING_BACK else CameraSelector.LENS_FACING_FRONT
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Switch camera",
                            tint = Color.White,
                            modifier = Modifier
                                .size(100.dp)
                                .padding(1.dp)
                                .border(1.dp, Color.White, CircleShape)
                        )
                    }
                )
            }

        }

    }

    // endregion

    // region private functions

    private fun takePhoto(
        imageCapture: ImageCapture,
        outputDirectory: File,
        executor: Executor,
        onImageCaptured: (Uri) -> Unit,
        onError: (ImageCaptureException) -> Unit,
        filenameFormat: String = "yyyy-MM-dd-HH-mm-ss-SSS"
    ) {

        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(filenameFormat, Locale.US).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exception: ImageCaptureException) { // Take photo error
                    onError(exception)
                }

                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    onImageCaptured(savedUri)
                }
            })
    }

    private fun uploadPhoto() {
        with(workManager) {
            val request: OneTimeWorkRequest =
                OneTimeWorkRequestBuilder<PhotoCardWorker>()
                    .setConstraints(constraints)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
                    .addTag(PhotoCardWorker.TAG)
                    .setInputData(
                        workDataOf(
                            PhotoCardWorker.PHOTO_URI to newPhotoData.uri.toString(),
                            PhotoCardWorker.PHOTO_CAPTION to newPhotoData.caption.value,
                            PhotoCardWorker.PHOTO_LATITUDE to newPhotoData.location.latitude,
                            PhotoCardWorker.PHOTO_LONGITUDE to newPhotoData.location.longitude
                        )
                    )
                    .build()
            enqueueUniqueWork(PhotoCardWorker.TAG, ExistingWorkPolicy.REPLACE, request)
            getWorkInfoByIdLiveData(request.id)
                .observe(viewLifecycleOwner) { workInfo ->
                    when (workInfo?.state) {
                        WorkInfo.State.SUCCEEDED -> {
                            findNavController().popBackStack()
                        }
                        WorkInfo.State.CANCELLED,
                        WorkInfo.State.FAILED -> {
                            shouldShowLoader.value = false
                        }
                        else -> {
                            Log.d(PhotoCardWorker.TAG, workInfo?.state?.name ?: "")
                        }
                    }
                }
        }
    }

    private fun handleImageCapture(uri: Uri) { // Image captured
        lifecycleScope.launch {
            viewModel.location.flowWithLifecycle(lifecycle).collectLatest { location ->

                if (location == null) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle(R.string.photo_fragment_position_error_title)
                        .setMessage(R.string.photo_fragment_position_error_message)
                        .setNeutralButton(R.string.photo_fragment_position_error_neutral_button) { _, _ ->
                            findNavController().popBackStack()
                        }.show()
                } else {
                    shouldShowCamera.value = false
                    newPhotoData = NewPhotoData(uri, location)
                    shouldShowPhoto.value = true
                }

            }
        }
    }

    // endregion

}