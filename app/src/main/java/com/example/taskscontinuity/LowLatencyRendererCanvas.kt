/*
* Copyright (c) 2022 Google LLC
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

package com.example.lowlatencysample.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.TOOL_TYPE_STYLUS
import android.view.SurfaceView
import android.view.View
import androidx.graphics.lowlatency.CanvasFrontBufferedRenderer
import com.example.taskscontinuity.DrawingManager
import com.example.taskscontinuity.DrawingManagerRenderer


class LowLatencyRendererCanvas(
  private var lineRenderer: CanvasSimpleBrush,
  private val drawingManager: DrawingManager,
  private val backgroundImg: Bitmap
) : DrawingManagerRenderer, CanvasFrontBufferedRenderer.Callback<FloatArray> {
  private val mvpMatrix = FloatArray(16)
  private val projection = FloatArray(16)

  private var canvasFrontBufferRenderer: CanvasFrontBufferedRenderer<FloatArray>? = null

  private var previousX: Float = 0f
  private var previousY: Float = 0f
  private var currentX: Float = 0f
  private var currentY: Float = 0f

  val cursorEnabled = false

  init {
    drawingManager.drawingManagerRenderer = this

  }

  fun initCanvas() {
    this.canvasFrontBufferRenderer?.renderMultiBufferedLayer(listOf(floatArrayOf(1f)))
  }

  override fun requestRendering(floatArray: FloatArray) {
    this.canvasFrontBufferRenderer?.renderFrontBufferedLayer(floatArray)
  }

  override fun renderCursor(coordinates: PointF, name: String) {
    val cursorData = FloatArray(name.length + 3)
    cursorData[0] = 2f
    cursorData[1] = coordinates.x
    cursorData[2] = coordinates.y
    var i = 3
    for (character in name) {
      cursorData[i] = character.code.toFloat()
      i++
    }

    this.canvasFrontBufferRenderer?.renderMultiBufferedLayer(listOf(cursorData))
  }

  override fun onDrawFrontBufferedLayer(
    canvas: Canvas,
    bufferWidth: Int,
    bufferHeight: Int,
    param: FloatArray
  ) {
    // Canvas code to be executed on the Front buffer.
    // Front buffer should be used to modify/render small area of the screen
    lineRenderer.render(canvas, param, inkColor)
  }

  val inkColor = Color.parseColor("#4285F4")

  override fun onDrawMultiBufferedLayer(
    canvas: Canvas,
    bufferWidth: Int,
    bufferHeight: Int,
    params: Collection<FloatArray>
  ) {
    canvas.drawColor(-1) // White background

    canvas.drawBitmap(backgroundImg, 0f, 0f, null)

    if (params.size > 0 && params.toList().first().size > 0 && params.toList()
        .first()[0] == 2f
    ) { // cursor
      renderCursorToCanvas(canvas, params.toList().first())

    } else {
      drawingManager.saveLines(params)
    }

    // Canvas code to redraw the entire scene (all the lines here)
    for (line in drawingManager.getLines()) {
      lineRenderer.render(canvas, line, inkColor)
    }
  }

  private var circlePaint: Paint = Paint().apply {
    style = Paint.Style.FILL;
    color = Color.BLUE;
    textSize = 34f
  }

  private fun renderCursorToCanvas(canvas: Canvas, floatArray: FloatArray) {
    val x = floatArray[1]
    val y = floatArray[2]

    var name = ""
    for (i in 3..floatArray.size - 1) {
      name += floatArray[i].toInt().toChar()
    }


    canvas.drawCircle(x, y, 15f, circlePaint)
    canvas.drawText(name, x + 25f, y, circlePaint)
  }

  fun attachSurfaceView(surfaceView: SurfaceView) {
    canvasFrontBufferRenderer = CanvasFrontBufferedRenderer(surfaceView, this)
  }

  fun release() {
    canvasFrontBufferRenderer?.release(true)
    {

    }
  }

  val onGenericMotionEvent = View.OnGenericMotionListener { v, event ->

    if (event.actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
      val distance = event.getAxisValue(MotionEvent.AXIS_DISTANCE)
      Log.d("Hover", "distance: $distance")


      drawingManager.dispatch(floatArrayOf(1f, event.x, event.y))
      true

    } else {
      false
    }

  }

  val onTouchListener = View.OnTouchListener { view, event ->

    val isStylus = TOOL_TYPE_STYLUS == event.getToolType(event.actionIndex)

    if (!isStylus) {
      return@OnTouchListener false
    }

    when (event?.action) {
      MotionEvent.ACTION_DOWN -> {
        // Ask that the input system not batch MotionEvents
        // but instead deliver them as soon as they're available
        view.requestUnbufferedDispatch(event)

        currentX = event.x
        currentY = event.y


        val line = FloatArray(DrawingManager.DATA_STRUCTURE_SIZE).apply {
          this[DrawingManager.X1_INDEX] = currentX
          this[DrawingManager.Y1_INDEX] = currentY
          this[DrawingManager.X2_INDEX] = currentX
          this[DrawingManager.Y2_INDEX] = currentY
          // Helps differentiate between User and Predicted events
          this[DrawingManager.EVENT_TYPE] = DrawingManager.IS_USER_EVENT
        }

        canvasFrontBufferRenderer?.renderFrontBufferedLayer(line)
        drawingManager.dispatch(line)
      }

      MotionEvent.ACTION_MOVE -> {
        previousX = currentX
        previousY = currentY
        currentX = event.x
        currentY = event.y

        val line = FloatArray(DrawingManager.DATA_STRUCTURE_SIZE).apply {
          this[DrawingManager.X1_INDEX] = previousX
          this[DrawingManager.Y1_INDEX] = previousY
          this[DrawingManager.X2_INDEX] = currentX
          this[DrawingManager.Y2_INDEX] = currentY
          // Helps differentiate between User and Predicted events
          this[DrawingManager.EVENT_TYPE] = DrawingManager.IS_USER_EVENT
        }
        // Send the short line to front buffered layer: fast rendering
        canvasFrontBufferRenderer?.renderFrontBufferedLayer(line)
        drawingManager.dispatch(line)

      }

      MotionEvent.ACTION_CANCEL -> {
        canvasFrontBufferRenderer?.commit()
      }

      MotionEvent.ACTION_UP -> {
        canvasFrontBufferRenderer?.commit()
      }
    }
    true
  }
}