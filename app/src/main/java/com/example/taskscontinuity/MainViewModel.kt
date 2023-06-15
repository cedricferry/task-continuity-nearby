package com.example.taskscontinuity

import android.graphics.PointF
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays

class MainViewModel : DrawingManager, NearByCallbacks, ViewModel() {

  private val lines: MutableList<FloatArray> = mutableListOf()

  val toDispatch = MutableSharedFlow<FloatArray>()
  val _toDispatch: SharedFlow<FloatArray> = toDispatch

  val state = mutableStateOf<String>("HELLO")

  val state2 = MutableStateFlow("")

  override var isPredictionEnabled: Boolean = false

  override var drawingManagerRenderer: DrawingManagerRenderer? = null

  private var isConnected  = false

  override fun dispatch(data: FloatArray) {
    if(isConnected) {
      Log.d("Dispatch", "Emit ${data.size}")
      //toDispatch.tryEmit(data)
      viewModelScope.launch {
        toDispatch.emit(data)
      }
    }
  }

  override fun saveLines(lines: Collection<FloatArray>) {
    this.lines.addAll(lines)
  }

  override fun getLines(): Collection<FloatArray> {
    return lines
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
      val count = payload.asStream()?.asInputStream()?.available()
      Log.e("Stream", "byte: $count")

    } else if (payload?.type == Payload.Type.BYTES) {
      val data = payload.asBytes()

      if(data?.size == 3 * Float.SIZE_BYTES) {
        val flagByte = Arrays.copyOfRange(data, 0, 4)
        val flag = ByteBuffer.wrap(flagByte).order(ByteOrder.BIG_ENDIAN).getFloat()
        if(flag == 1f) { // Cursor
          val xByte = Arrays.copyOfRange(data, 4, 8)
          val x = ByteBuffer.wrap(xByte).order(ByteOrder.BIG_ENDIAN).getFloat()

          val yByte = Arrays.copyOfRange(data, 8, 12)
          val y = ByteBuffer.wrap(yByte).order(ByteOrder.BIG_ENDIAN).getFloat()

          this.drawingManagerRenderer?.renderCursor(PointF(x, y), endpoint.name)
        }
      } else if(data?.size == DrawingManager.DATA_STRUCTURE_SIZE * Float.SIZE_BYTES) {


        val x1Byte = Arrays.copyOfRange(data, 0, 4)
        val x1 = ByteBuffer.wrap(x1Byte).order(ByteOrder.BIG_ENDIAN).getFloat()

        val y1Byte = Arrays.copyOfRange(data, 4, 8)
        val y1 = ByteBuffer.wrap(y1Byte).order(ByteOrder.BIG_ENDIAN).getFloat()

        val x2Byte = Arrays.copyOfRange(data, 8, 12)
        val x2 = ByteBuffer.wrap(x2Byte).order(ByteOrder.BIG_ENDIAN).getFloat()

        val y2Byte = Arrays.copyOfRange(data, 12, 16)
        val y2 = ByteBuffer.wrap(y2Byte).order(ByteOrder.BIG_ENDIAN).getFloat()

        val floatArray = FloatArray(DrawingManager.DATA_STRUCTURE_SIZE)
        floatArray[DrawingManager.X1_INDEX] = x1
        floatArray[DrawingManager.Y1_INDEX] = y1
        floatArray[DrawingManager.X2_INDEX] = x2
        floatArray[DrawingManager.Y2_INDEX] = y2
        floatArray[DrawingManager.EVENT_TYPE] = DrawingManager.IS_USER_EVENT

        this.drawingManagerRenderer?.requestRendering(floatArray)
      } else {
        Log.e("DataReceived", "invalid size: ${data?.size}")
      }
    }
  }
}