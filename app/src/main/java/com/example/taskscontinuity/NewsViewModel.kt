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

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays

class ArticleInfo(val id: Int, val position: Float)

class Article(
  val id: Int,
  val title: String,
  val author: String,
  val date: String,
  val image: Int,
  val description: String,
)

class NewsViewModel : NearByCallbacks, ViewModel() {


  val articles = arrayListOf<Article>(
    Article(
      1,
      "Neque porro quisquam est",
      "Robert Author",
      "June 9, 2023",
      R.drawable.one,
      getLoremIpsum()
      ),
    Article(
      2,
      "Sed ut perspiciatis unde omnis",
      "Jane Author",
      "June 9, 2023",
      R.drawable.two,
      getLoremIpsum()
    ),
    Article(
      3,
      "Ipsum Lorem ipsam voluptatem quia voluptas",
      "July Rohtua",
      "June 9, 2023",
      R.drawable.three,
      getLoremIpsum()
    ),

    Article(
      4,
      "Quis autem vel eum iure reprehenderit",
      "Droid Tuaroh",
      "June 9, 2023",
      R.drawable.four,
      getLoremIpsum()
    )
  )

  private fun getLoremIpsum(): String {
    return "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat." +
            "\nDuis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum." +
            "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo.\n\n" +
            "Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. \n" +
            "Neque porro quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat voluptatem. " +
            "\n\nUt enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit laboriosam, nisi ut aliquid ex ea commodi consequatur? \n" +
            "Quis autem vel eum iure reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel illum qui dolorem eum fugiat quo voluptas nulla pariatur?" +
            "\n\nAt vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident, similique sunt in culpa qui officia deserunt mollitia animi, id est laborum et dolorum fuga.\n\n" +
            "Et harum quidem rerum facilis est et expedita distinctio. " +
            "\nNam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut rerum necessitatibus saepe eveniet ut et voluptates repudiandae sint et molestiae non recusandae." +
            "\n\nItaque earum rerum hic tenetur a sapiente delectus, ut aut reiciendis voluptatibus maiores alias consequatur aut perferendis doloribus asperiores rep"
  }

  val toDispatch = MutableSharedFlow<ArticleInfo>()

  val state = mutableStateOf<String>("HELLO")

  val articleId = mutableStateOf(-1)

  var scrollPosition = mutableStateOf(2500f)


  private var isConnected = false

  fun dispatch(data: ArticleInfo) {
    if (isConnected) {
      Log.d("Dispatch", "Emit ${data.id}")
      //toDispatch.tryEmit(data)
      viewModelScope.launch {
        toDispatch.emit(data)
      }
    }
  }

  fun closeArticle() {
    this.articleId.value = -1
    scrollPosition.value = 0f
  }

  fun openArticle(id: Int, position: Float = 0f) {
    Log.d("DataReceived", "openArticle: $id at $position")
    this.articleId.value = id
    scrollPosition.value = position
  }

  fun getArticle(id: Int): Article {
    return articles.find { it.id == id }!!
  }

  fun scrollPosition(position: Float) {
    scrollPosition.value = position
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

    if (articleId.value > 0) {
      Log.d("Sending Data", "dispatch: $${articleId.value}, ${scrollPosition.value}")
      dispatch(ArticleInfo(articleId.value, scrollPosition.value))
    }
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


    } else if (payload?.type == Payload.Type.BYTES) {
      val data = payload.asBytes()
      if (data?.size == Int.SIZE_BYTES + Float.SIZE_BYTES) {
        val idByte = Arrays.copyOfRange(data, 0, 4)
        val id = ByteBuffer.wrap(idByte).order(ByteOrder.BIG_ENDIAN).getInt()

        val scrollByte = Arrays.copyOfRange(data, 4, 8)
        val scroll = ByteBuffer.wrap(scrollByte).order(ByteOrder.BIG_ENDIAN).getFloat()

        openArticle(id = id, position = scroll)
      }
    }
  }
}