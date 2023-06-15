package com.example.taskscontinuity

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.taskscontinuity.ui.theme.TasksContinuityTheme
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.Strategy
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.util.Random

class NewsActivity : ConnectionsActivity() {

  override var name: String = getUniqueName()

  override val serviceId: String = "com.example.taskscontinuity.TasksContinuity.SERVICE_ID"
  override val strategy: Strategy = Strategy.P2P_POINT_TO_POINT // 1 - 1 device

  private val viewModel: NewsViewModel by viewModels()

  override var connectionCallbacks: NearByCallbacks? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    listenToDispatch()
    this.connectionCallbacks = viewModel

    setContent {
      TasksContinuityTheme {
        // A surface container using the 'background' color from the theme
        if (viewModel.articleId.value < 0) {
          Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {

            Column {
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
              ArticleFeed()
            }
          }
        } else {
          val article = viewModel.getArticle(viewModel.articleId.value)



          val lazyListState = rememberLazyListState()
          val scrollState = rememberScrollableState { delta ->
            viewModel.scrollPosition(lazyListState.firstVisibleItemScrollOffset.toFloat())
            delta
          }



          LazyColumn(
            Modifier
              .scrollable(scrollState, orientation = Orientation.Vertical)
              .fillMaxSize(),
            lazyListState){
            item {
              Button(
                onClick = {
                  viewModel.closeArticle()
                }, modifier = Modifier.absoluteOffset(10.dp, 10.dp)
              ) {
                Text(text = "Close")
              }
            }
            item {
              FullArticle(
                id = article.id,
                title = article.title,
                author = article.author,
                date = article.date,
                image = article.image,
                description = article.description
              )
            }
          }
          if(viewModel.scrollPosition.value > 0f) {
            val scope = rememberCoroutineScope()
            LaunchedEffect(Unit) {
              scope.launch {
                delay(700)
                Log.d("DataReceived", "Scroll to ${viewModel.scrollPosition.value}")
                lazyListState.animateScrollBy(viewModel.scrollPosition.value)
              }
            }
          }
        }
      }
    }
  }

  private fun listenToDispatch() {
    Log.d("Sending Data", "Listen dispatch")

    lifecycleScope.launch {
      Log.d("Sending Data", "launch")
      viewModel.toDispatch.collect(collector = {
        // dispatch with nearby
        Log.d("Sending Data", "${it.id}, ${it.position}")

        val buffer = ByteBuffer.allocate(Int.SIZE_BYTES + Float.SIZE_BYTES)

        buffer.putInt(it.id)
        buffer.putFloat(it.position)

        send(Payload.fromBytes(buffer.array()))
      })
    }
  }

  private fun startConnection() {
    disconnectFromAllEndpoints()
    startDiscovering()
    startAdvertising()
  }


  @RequiresApi(Build.VERSION_CODES.Q)
  @Composable
  fun ArticleFeed() {

    LazyColumn(content = {
      for (new in viewModel.articles) {
        item {
          NewsItem(
            new.id,
            new.title,
            new.author,
            new.date,
            new.image,
            new.description
          )
        }
      }
    }, modifier = Modifier.fillMaxSize())
  }

  @Composable
  fun FullArticle(
    id: Int,
    title: String,
    author: String,
    date: String,
    image: Int,
    description: String
  ) {
    NewsItem(id, title, author, date, image, description, isPreview = false)
  }

  @Composable
  fun NewsItem(
    id: Int,
    title: String,
    author: String,
    date: String,
    image: Int,
    description: String,
    isPreview: Boolean = true
  ) {
    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp)) {

      Image(painterResource(id = image), contentDescription = title)
      Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
      )
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(text = author)
        Text(text = date)
      }
      Text(
        overflow = if (isPreview) {
          TextOverflow.Ellipsis
        } else {
          TextOverflow.Clip
        },
        maxLines = if (isPreview) {
          4
        } else {
          400
        },
        text = description,
        modifier = Modifier.padding(bottom = 8.dp)
      )
      Button(onClick = {
        viewModel.openArticle(id)
      }) {
        Text(text = "READ MORE")
      }
    }
  }
}