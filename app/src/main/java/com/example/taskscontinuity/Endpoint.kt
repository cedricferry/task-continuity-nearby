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

/** Represents a device we can talk to.  */
class Endpoint constructor(val id: String, val name: String) {

  override fun equals(obj: Any?): Boolean {
    if (obj is Endpoint) {
      val other: Endpoint? =
        obj as Endpoint?
      return id == other?.id
    }
    return false
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }

  override fun toString(): String {
    return String.format("Endpoint{id=%s, name=%s}", id, name)
  }
}