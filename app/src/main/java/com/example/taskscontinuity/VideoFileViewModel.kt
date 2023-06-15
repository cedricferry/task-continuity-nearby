package com.example.taskscontinuity

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays


class VideoFileViewModel : NearByCallbacks, ViewModel() {


  val toDispatch = MutableSharedFlow<String>()

  val state = mutableStateOf<String>("HELLO")

  var isConnected = false

  var filename: String = ""

  fun dispatch(data: String) {
    if (isConnected) {
      Log.d("Dispatch", "Emit ${data}")
      //toDispatch.tryEmit(data)
      viewModelScope.launch {
        toDispatch.emit(data)
      }
    }
  }

  override fun onAdvertisingStarted() {
    state.value = "Advertising..."
  }

  override fun onAdvertisingFailed() {
    state.value = "Advertising failed"
    super.onAdvertisingFailed()
  }

  override fun onConnectionInitiated(endpoint: Endpoint?, connectionInfo: ConnectionInfo?) {
    super.onConnectionInitiated(endpoint, connectionInfo)
    state.value = "Connection initiated with ${endpoint?.name}"
  }

  override fun onDiscoveryStarted() {
    super.onDiscoveryStarted()
    state.value = "Discovery started"
  }

  override fun onDiscoveryFailed() {
    super.onDiscoveryFailed()
    state.value = "Discovery failed"
  }

  override fun onEndpointDiscovered(endpoint: Endpoint) {
    super.onEndpointDiscovered(endpoint)
    state.value = "Endpoint discovered: ${endpoint.name}"
  }

  override fun onConnectionFailed(endpoint: Endpoint) {
    super.onConnectionFailed(endpoint)
    state.value = "Connection failed: ${endpoint.name}"
  }

  override fun onEndpointConnected(endpoint: Endpoint) {
    super.onEndpointConnected(endpoint)
    state.value = "Endpoint connected! ${endpoint.name}"
    isConnected = true
  }

  override fun onEndpointDisconnected(endpoint: Endpoint) {
    super.onEndpointDisconnected(endpoint)
    state.value = "Endpoint disconnected: ${endpoint.name}"
    isConnected = false
  }

  override fun onReceive(endpoint: Endpoint, payload: Payload?) {
    super.onReceive(endpoint, payload)
    Log.v("DataReceived", "from ${endpoint.name}")

    if (payload?.type == Payload.Type.STREAM) {
      Log.v("DataReceived", "file size: ${payload.asStream()}")



      val file = File("tmp.mp4")

      val fileOutputStream = FileOutputStream(file)
      payload.asStream()?.asInputStream()?.copyTo(fileOutputStream)
    }
  }
}