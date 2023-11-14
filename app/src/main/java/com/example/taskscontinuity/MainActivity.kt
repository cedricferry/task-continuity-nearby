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

import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.example.lowlatencysample.ui.CanvasSimpleBrush
import com.example.lowlatencysample.ui.LowLatencyRendererCanvas
import com.example.lowlatencysample.ui.LowLatencySurfaceView
import com.example.taskscontinuity.ui.theme.TasksContinuityTheme
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.launch
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.Random

class MainActivity : ConnectionsActivity() {

  override var name: String = getUniqueName()

  override val serviceId: String = "com.example.taskscontinuity.TasksContinuity.SERVICE_ID"
  override val strategy: Strategy = Strategy.P2P_POINT_TO_POINT // 1 - 1 device

  private val viewModel: MainViewModel by viewModels()
  private lateinit var lowLatencyRenderer: LowLatencyRendererCanvas

  override var connectionCallbacks: NearByCallbacks? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    this.connectionCallbacks = viewModel

    lowLatencyRenderer = LowLatencyRendererCanvas(
      CanvasSimpleBrush(5f),
      viewModel,
      BitmapFactory.decodeResource(resources, R.drawable.bg_collab)
    )

    listenToDispatch()

    setContent {
      TasksContinuityTheme {
        // A surface container using the 'background' color from the theme
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
          Column {
            ConnectionBar()
            LowLatencyDrawing()
          }
        }
      }
    }
  }

  private fun startConnection() {
    disconnectFromAllEndpoints()
    startDiscovering()
    startAdvertising()
  }

  private fun listenToDispatch() {
    Log.d("Sending Data", "Listen dispatch")

    lifecycleScope.launch {
      Log.d("Sending Data", "launch")
      viewModel._toDispatch.collect(collector = {
        // dispatch with nearby
        Log.d("Sending Data", "${it.size}")

        val buffer = ByteBuffer.allocate(it.size * Float.SIZE_BYTES)

        for (fl in it) {
          buffer.putFloat(fl)
        }
//        val inputStream = ByteArrayInputStream(buffer.array())
//        send(Payload.fromStream(inputStream))
        send(Payload.fromBytes(buffer.array()))
        Log.d("Sending Data", "${it.size}")
      })
    }
  }


  @Composable
  fun LowLatencyDrawing() {
    val context = LocalContext.current
    val lowLatencySurfaceView =
      remember(context) { LowLatencySurfaceView(context, lowLatencyRenderer) }

    AndroidView(factory = { context ->
      lowLatencySurfaceView
    })
  }

  @Composable
  fun ConnectionBar() {
    Row(
      modifier = Modifier.fillMaxWidth(),
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

  @OptIn(ExperimentalComposeUiApi::class)
  fun test() {
    KeyShortcut(Key.A, true)

  }
}



class KeyShortcut(
  val key: Key,
  val ctrl: Boolean = false,
  val shift: Boolean = false,
  val alt: Boolean = false,
  val meta: Boolean = false,
) {
  fun match(keyEvent: KeyEvent): Boolean {
    return keyEvent.isAltPressed == alt
            && keyEvent.isCtrlPressed == alt
            && keyEvent.isMetaPressed == alt
            && keyEvent.isShiftPressed == alt
            && keyEvent.key == key
  }
}

fun Modifier.keyShortcut(keys: KeyShortcut, callback: () -> Any) {
  this.onPreviewKeyEvent { keyEvent ->
    if (keys.match(keyEvent)) {
      callback()
      return@onPreviewKeyEvent true
    }
    return@onPreviewKeyEvent false
  }
}


@Composable
fun ConnectionStatus(state: MutableState<String>) {
  var name by remember { state }
  Text(
    text = "Status: ${state.value}", modifier = Modifier
      .padding(8.dp)
  )
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  TasksContinuityTheme {

  }
}