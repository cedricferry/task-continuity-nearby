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