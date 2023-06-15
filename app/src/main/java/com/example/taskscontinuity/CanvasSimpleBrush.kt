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
package com.example.lowlatencysample.ui

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.taskscontinuity.DrawingManager

class CanvasSimpleBrush(var size: Float) {

    private var strokePaint: Paint = Paint()

    init {
        strokePaint.style = Paint.Style.STROKE;
        strokePaint.color = Color.BLUE;
        strokePaint.strokeWidth = size;
        strokePaint.strokeCap = Paint.Cap.ROUND;
    }

    private fun getPaint(): Paint = strokePaint

    fun render(canvas: Canvas, lines: FloatArray, color: Int) {
        val path = Path()

        strokePaint.color = color

        for (i in 0 until lines.size  step DrawingManager.DATA_STRUCTURE_SIZE) {
            if( i + DrawingManager.DATA_STRUCTURE_SIZE -1 < lines.size) {
                if (i == 0) {
                    path.moveTo(
                        lines[i + DrawingManager.X1_INDEX],
                        lines[i + DrawingManager.Y1_INDEX]
                    )
                }
                path.lineTo(lines[i + DrawingManager.X2_INDEX], lines[i + DrawingManager.Y2_INDEX])
            } else {
                continue
            }
        }

        canvas.drawPath(path, getPaint())
    }

}