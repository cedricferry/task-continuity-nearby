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

package com.example.taskscontinuity

import android.graphics.PointF

interface DrawingManager {
    fun saveLines(lines: Collection<FloatArray>)
    fun getLines(): Collection<FloatArray>
    var isPredictionEnabled: Boolean

    fun dispatch(data: FloatArray)

    var drawingManagerRenderer: DrawingManagerRenderer?

    companion object {
        const val DATA_STRUCTURE_SIZE = 5
        const val X1_INDEX = 0
        const val Y1_INDEX = 1
        const val X2_INDEX = 2
        const val Y2_INDEX = 3
        const val EVENT_TYPE = 4

        const val IS_USER_EVENT = 0.0f
        const val IS_PREDICTED_EVENT = 1.0f
    }
}

interface DrawingManagerRenderer {
    fun requestRendering(floatArray: FloatArray)
    fun renderCursor(coordinates: PointF, name: String)
}