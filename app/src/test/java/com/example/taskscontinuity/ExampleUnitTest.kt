package com.example.taskscontinuity

import org.junit.Assert.*
import org.junit.Test
import java.nio.ByteBuffer
import java.util.Arrays

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun float_toByteArray() {
    val fl = 3.14f
    val buffer = ByteBuffer.allocate(Float.SIZE_BYTES)

    buffer.putFloat(fl)

    val byteArray = buffer.array()


    val x = ByteBuffer.wrap(byteArray).getFloat()

    assertEquals(fl, x)

  }

  @Test
  fun floatArray_toByteArray() {
    val fla = floatArrayOf(3.14f, 5.16f, 7.18f)
    val buffer = ByteBuffer.allocate(fla.size * Float.SIZE_BYTES)


    for (fl in fla) {
      buffer.putFloat(fl)
    }


    val byteArray = buffer.array()


    val flb1: ByteArray = Arrays.copyOfRange(byteArray, 0, 4)
    val fl1 = ByteBuffer.wrap(flb1).getFloat()

    val flb2: ByteArray = Arrays.copyOfRange(byteArray, 4, 8)
    val fl2 = ByteBuffer.wrap(flb2).getFloat()

    val flb3: ByteArray = Arrays.copyOfRange(byteArray, 8, 12)
    val fl3 = ByteBuffer.wrap(flb3).getFloat()

    assertEquals(fla[0], fl1)
    assertEquals(fla[1], fl2)
    assertEquals(fla[2], fl3)

  }
}