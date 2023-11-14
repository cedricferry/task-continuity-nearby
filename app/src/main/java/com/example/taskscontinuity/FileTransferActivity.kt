/*
* Copyright (c) 2023 Google LLC
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.example.taskscontinuity

import android.content.ContentValues
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraMetadata
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.AspectRatio.RATIO_16_9
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.lifecycleScope
import com.example.taskscontinuity.ui.theme.TasksContinuityTheme
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class FileTransferActivity : ConnectionsActivity() {

  override var name: String = getUniqueName()

  override val serviceId: String = "com.example.taskscontinuity.TasksContinuity.SERVICE_ID"
  override val strategy: Strategy = Strategy.P2P_POINT_TO_POINT // 1 - 1 device

  private val viewModel: VideoFileViewModel by viewModels()

  override var connectionCallbacks: NearByCallbacks? = null

  private var surfaceView: PreviewView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    listenToDispatch()
    this.connectionCallbacks = viewModel

    setContent {
      TasksContinuityTheme {

        Column {

          Row {
            CameraPreview()
          }

          ConnectionBar()
          Row {


            Button(onClick = { startRecording() }) {
              Text("record")
            }
            Button(onClick = { stopRecording() }) {
              Text("stop")
            }
          }
        }

      }
    }
  }

  @Composable
  fun CameraPreview() {
    val context = LocalContext.current
    surfaceView =
      remember(context) { PreviewView(context) }
    // A surface container using the 'background' color from the theme
    AndroidView(factory = { context ->
      surfaceView!!
    })
    LaunchedEffect(key1 = Unit, block = {
      launch {
        cameraExecutor = Executors.newSingleThreadExecutor()
        setupCamera()
      }
    })
  }

  @Composable
  fun ConnectionBar() {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color.White),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      ConnectionStatus(viewModel.state)
      Text(text = name, modifier = Modifier.padding(8.dp))
      Button(onClick = {
        startConnection()
      }, modifier = Modifier.padding(8.dp)) {
        Text("Connect")
      }
    }
  }

  fun openFile() {
    val name = "CameraX-recording-1686783920187.mp4"
  }

  fun getContentValues(): ContentValues {
    viewModel.filename = "CameraX-recording-${System.currentTimeMillis()}.mp4"
    return ContentValues().apply {
      put(MediaStore.Video.Media.DISPLAY_NAME, viewModel.filename)
    }
  }


  var mediaStoreOutput: MediaStoreOutputOptions? = null


  var recording: Recording? = null

  fun startRecording() {
    mediaStoreOutput = MediaStoreOutputOptions.Builder(
      this.contentResolver,
     //Uri.parse(this.filesDir.absolutePath)
      MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    )
      .setContentValues(getContentValues())
      .build()


    recording = videoCapture?.output
      ?.prepareRecording(this, mediaStoreOutput!!)
      // .withAudioEnabled()
      ?.start(ContextCompat.getMainExecutor(this), Consumer {

      })
  }

  fun stopRecording() {
    recording?.stop()

    //TODO dispatch
    if (viewModel.isConnected) {
      viewModel.dispatch(viewModel.filename)
    }
  }

  private lateinit var cameraExecutor: ExecutorService
  var cameraProvider: ProcessCameraProvider? = null
  private var preview: Preview? = null

  private var videoCapture: VideoCapture<Recorder>? = null

  @androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
  suspend fun setupCamera() {
    cameraProvider = ProcessCameraProvider.getInstance(this).await()

    val cameraInfo = cameraProvider?.availableCameraInfos?.filter {
      Camera2CameraInfo
        .from(it)
        .getCameraCharacteristic(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK
    }

    val supportedQualities = QualitySelector.getSupportedQualities(cameraInfo?.first()!!)
    val filteredQualities = arrayListOf(Quality.HD, Quality.SD)
      .filter { supportedQualities.contains(it) }


    // Inside View.OnClickListener,
    // convert Quality.* constant to QualitySelector
    val qualitySelector = QualitySelector.from(filteredQualities.first())


    val recorder = Recorder.Builder()
      .setExecutor(cameraExecutor).setQualitySelector(qualitySelector)
      .build()

    videoCapture = VideoCapture.withOutput(recorder)

    // Preview
    preview = Preview.Builder()
      // We request aspect ratio but no resolution
      .setTargetAspectRatio(RATIO_16_9)
      // Set initial target rotation
      //.setTargetRotation(rotation)
      .build()

    preview?.setSurfaceProvider(surfaceView?.surfaceProvider)

    try {
      // Bind use cases to camera
      cameraProvider!!.bindToLifecycle(
        this, CameraSelector.DEFAULT_BACK_CAMERA, preview, videoCapture
      )
    } catch (exc: Exception) {
      Log.e(TAG, "Use case binding failed", exc)
    }
  }

  private fun listenToDispatch() {
    Log.d("Sending Data", "Listen dispatch")

    lifecycleScope.launch {
      Log.d("Sending Data", "launch")
      viewModel.toDispatch.collect(collector = {
        if(it.isNotBlank()) {
          // dispatch with nearby
          Log.d("Sending Data", "file: ${MediaStore.Video.Media.EXTERNAL_CONTENT_URI} ${it}")

          val inputStream = contentResolver.openInputStream(Uri.parse(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.path +"/"+it))


          send(Payload.fromStream(inputStream!!))
        }
      })
    }
  }

  private fun startConnection() {
    disconnectFromAllEndpoints()
    startDiscovering()
    startAdvertising()
  }


}