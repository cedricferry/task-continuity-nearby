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

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.CallSuper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.api.Status
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionInfo
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback
import com.google.android.gms.nearby.connection.ConnectionResolution
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo
import com.google.android.gms.nearby.connection.DiscoveryOptions
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback
import com.google.android.gms.nearby.connection.Payload
import com.google.android.gms.nearby.connection.PayloadCallback
import com.google.android.gms.nearby.connection.PayloadTransferUpdate
import com.google.android.gms.nearby.connection.Strategy
import com.google.android.gms.tasks.OnFailureListener
import java.util.Locale
import java.util.Random


/** A class that connects to Nearby Connections and provides convenience methods and callbacks.  */
abstract class ConnectionsActivity : ComponentActivity() {
  /** Our handler to Nearby Connections.  */
  private var mConnectionsClient: ConnectionsClient? = null
  abstract var connectionCallbacks: NearByCallbacks?

  /** The devices we've discovered near us.  */
  private val mDiscoveredEndpoints: MutableMap<String, Endpoint> =
    HashMap()

  /**
   * The devices we have pending connections to. They will stay pending until we call [ ][.acceptConnection] or [.rejectConnection].
   */
  private val mPendingConnections: MutableMap<String, Endpoint> =
    HashMap()

  /**
   * The devices we are currently connected to. For advertisers, this may be large. For discoverers,
   * there will only be one entry in this map.
   */
  private val mEstablishedConnections: MutableMap<String, Endpoint> =
    HashMap()
  /** Returns `true` if we're currently attempting to connect to another device.  */
  /**
   * True if we are asking a discovered device to connect to us. While we ask, we cannot ask another
   * device.
   */
  protected var isConnecting = false
    private set
  /** Returns `true` if currently discovering.  */
  /** True if we are discovering.  */
  protected var isDiscovering = false
    private set
  /** Returns `true` if currently advertising.  */
  /** True if we are advertising.  */
  protected var isAdvertising = false
    private set

  /** Callbacks for connections to other devices.  */
  private val mConnectionLifecycleCallback: ConnectionLifecycleCallback =
    object : ConnectionLifecycleCallback() {
      override fun onConnectionInitiated(endpointId: String, connectionInfo: ConnectionInfo) {
        logD(
          java.lang.String.format(
            "onConnectionInitiated(endpointId=%s, endpointName=%s)",
            endpointId, connectionInfo.endpointName
          )
        )
        val endpoint =
          Endpoint(
            endpointId,
            connectionInfo.endpointName
          )
        mPendingConnections[endpointId] = endpoint
        connectionCallbacks?.onConnectionInitiated(endpoint, connectionInfo)
        acceptConnection(endpoint)
      }

      override fun onConnectionResult(endpointId: String, result: ConnectionResolution) {
        logD(
          java.lang.String.format(
            "onConnectionResponse(endpointId=%s, result=%s)",
            endpointId,
            result
          )
        )

        // We're no longer connecting
        isConnecting = false
        if (!result.status.isSuccess) {
          logW(
            String.format(
              "Connection failed. Received status %s.",
              toString(result.status)
            )
          )
          mPendingConnections.remove(endpointId)?.let {
            connectionCallbacks?.onConnectionFailed(it)
          }

          return
        }
        mPendingConnections.remove(endpointId)?.let {
          connectedToEndpoint(it)
        }
      }

      override fun onDisconnected(endpointId: String) {
        if (!mEstablishedConnections.containsKey(endpointId)) {
          logW("Unexpected disconnection from endpoint $endpointId")
          return
        }
        mEstablishedConnections[endpointId]?.let {
          disconnectedFromEndpoint(it)
        }
      }
    }

  /** Callbacks for payloads (bytes of data) sent from another device to us.  */
  private val mPayloadCallback: PayloadCallback = object : PayloadCallback() {
    override fun onPayloadReceived(endpointId: String, payload: Payload) {
      logD(
        java.lang.String.format(
          "onPayloadReceived(endpointId=%s, payload=%s)",
          endpointId,
          payload
        )
      )
      mEstablishedConnections[endpointId]?.let {
        connectionCallbacks?.onReceive(it, payload)
      }

    }

    override fun onPayloadTransferUpdate(endpointId: String, update: PayloadTransferUpdate) {
      logD(
        java.lang.String.format(
          "onPayloadTransferUpdate(endpointId=%s, update=%s)", endpointId, update
        )
      )
    }
  }

  /** Called when our Activity is first created.  */
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    mConnectionsClient = Nearby.getConnectionsClient(this)
  }

  /** Called when our Activity has been made visible to the user.  */
  override fun onStart() {
    super.onStart()
    Log.d("Permission", "onStart")
    if (!hasPermissions(this, requiredPermissions)) {
      if (Build.VERSION.SDK_INT < 23) {
        ActivityCompat.requestPermissions(
          this, requiredPermissions, REQUEST_CODE_REQUIRED_PERMISSIONS
        )
      } else {
        requestPermissions(requiredPermissions, REQUEST_CODE_REQUIRED_PERMISSIONS)
      }
    }
  }

  /** Called when the user has accepted (or denied) our permission request.  */
  @CallSuper
  override fun onRequestPermissionsResult(
    requestCode: Int, permissions: Array<String>, grantResults: IntArray
  ) {
    if (requestCode == REQUEST_CODE_REQUIRED_PERMISSIONS) {
      for ((i, grantResult) in grantResults.withIndex()) {
        if (grantResult == PackageManager.PERMISSION_DENIED) {
          logW("Failed to request the permission " + permissions[i])
          Toast.makeText(this, "Missing permission", Toast.LENGTH_LONG).show()
          finish()
          return
        }
      }
      recreate()
    }
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  /**
   * Sets the device to advertising mode. It will broadcast to other devices in discovery mode.
   * Either [.onAdvertisingStarted] or [.onAdvertisingFailed] will be called once
   * we've found out if we successfully entered this mode.
   */
  protected fun startAdvertising() {
    isAdvertising = true
    val localEndpointName = name
    val advertisingOptions: AdvertisingOptions.Builder = AdvertisingOptions.Builder()
    strategy?.let {
      advertisingOptions.setStrategy(it)
    }

    mConnectionsClient
      ?.startAdvertising(
        localEndpointName,
        serviceId,
        mConnectionLifecycleCallback,
        advertisingOptions.build()
      )
      ?.addOnSuccessListener {
        logV("Now advertising endpoint $localEndpointName")
        connectionCallbacks?.onAdvertisingStarted()
      }
      ?.addOnFailureListener { e ->
        isAdvertising = false
        logW("startAdvertising() failed.", e)
        connectionCallbacks?.onAdvertisingFailed()
      }
  }

  /** Stops advertising.  */
  protected fun stopAdvertising() {
    isAdvertising = false
    mConnectionsClient?.stopAdvertising()
  }



  /** Accepts a connection request.  */
  protected fun acceptConnection(endpoint: Endpoint) {
    mConnectionsClient
      ?.acceptConnection(endpoint.id, mPayloadCallback)
      ?.addOnCompleteListener {
        stopDiscovering()
        stopAdvertising()
      }
      ?.addOnFailureListener(
        object : OnFailureListener {
          override fun onFailure(e: Exception) {
            logW("acceptConnection() failed.", e)
          }
        })
  }

  /** Rejects a connection request.  */
  protected fun rejectConnection(endpoint: Endpoint) {
    mConnectionsClient
      ?.rejectConnection(endpoint.id)
      ?.addOnFailureListener { e -> logW("rejectConnection() failed.", e) }
  }

  /**
   * Sets the device to discovery mode. It will now listen for devices in advertising mode. Either
   * [.onDiscoveryStarted] or [.onDiscoveryFailed] will be called once we've found
   * out if we successfully entered this mode.
   */
  protected fun startDiscovering() {
    isDiscovering = true
    mDiscoveredEndpoints.clear()
    val discoveryOptions: DiscoveryOptions.Builder = DiscoveryOptions.Builder()
    strategy?.let {
      discoveryOptions.setStrategy(it)
    }

    mConnectionsClient
      ?.startDiscovery(
        serviceId,
        object : EndpointDiscoveryCallback() {
          override fun onEndpointFound(endpointId: String, info: DiscoveredEndpointInfo) {
            logD(
              java.lang.String.format(
                "onEndpointFound(endpointId=%s, serviceId=%s, endpointName=%s)",
                endpointId, info.serviceId, info.endpointName
              )
            )
            if (serviceId == info.serviceId) {
              val endpoint =
                Endpoint(
                  endpointId,
                  info.endpointName
                )
              mDiscoveredEndpoints[endpointId] = endpoint
              connectionCallbacks?.onEndpointDiscovered(endpoint)
              stopDiscovering()
              connectToEndpoint(endpoint)
            }
          }

          override fun onEndpointLost(endpointId: String) {
            logD(String.format("onEndpointLost(endpointId=%s)", endpointId))
          }
        },
        discoveryOptions.build()
      )
      ?.addOnSuccessListener { connectionCallbacks?.onDiscoveryStarted() }
      ?.addOnFailureListener { e ->
        isDiscovering = false
        logW("startDiscovering() failed.", e)
        connectionCallbacks?.onDiscoveryFailed()
      }
  }

  /** Stops discovery.  */
  protected fun stopDiscovering() {
    isDiscovering = false
    mConnectionsClient?.stopDiscovery()
  }


  /** Disconnects from the given endpoint.  */
  protected fun disconnect(endpoint: Endpoint) {
    mConnectionsClient?.disconnectFromEndpoint(endpoint.id)
    mEstablishedConnections.remove(endpoint.id)
  }

  /** Disconnects from all currently connected endpoints.  */
  protected fun disconnectFromAllEndpoints() {
    for (endpoint in mEstablishedConnections.values) {
      mConnectionsClient?.disconnectFromEndpoint(endpoint.id)
    }
    mEstablishedConnections.clear()
  }

  /** Resets and clears all state in Nearby Connections.  */
  protected fun stopAllEndpoints() {
    mConnectionsClient?.stopAllEndpoints()
    isAdvertising = false
    isDiscovering = false
    isConnecting = false
    mDiscoveredEndpoints.clear()
    mPendingConnections.clear()
    mEstablishedConnections.clear()
  }

  /**
   * Sends a connection request to the endpoint. Either [.onConnectionInitiated] or [.onConnectionFailed] will be called once we've found out
   * if we successfully reached the device.
   */
  protected fun connectToEndpoint(endpoint: Endpoint) {
    logV("Sending a connection request to endpoint $endpoint")
    // Mark ourselves as connecting so we don't connect multiple times
    isConnecting = true

    // Ask to connect
    mConnectionsClient
      ?.requestConnection(name, endpoint.id, mConnectionLifecycleCallback)
      ?.addOnFailureListener { e ->
        logW("requestConnection() failed.", e)
        isConnecting = false
        connectionCallbacks?.onConnectionFailed(endpoint)
        startDiscovering()
      }
  }

  private fun connectedToEndpoint(endpoint: Endpoint) {
    logD(String.format("connectedToEndpoint(endpoint=%s)", endpoint))
    mEstablishedConnections[endpoint.id] = endpoint
    connectionCallbacks?.onEndpointConnected(endpoint)
  }

  private fun disconnectedFromEndpoint(endpoint: Endpoint) {
    logD(String.format("disconnectedFromEndpoint(endpoint=%s)", endpoint))
    mEstablishedConnections.remove(endpoint?.id)
    connectionCallbacks?.onEndpointDisconnected(endpoint)
  }

  protected val discoveredEndpoints: Set<Endpoint>
    /** Returns a list of currently connected endpoints.  */
    protected get() = HashSet<Endpoint>(
      mDiscoveredEndpoints.values
    )

  protected val connectedEndpoints: Set<Endpoint>
    /** Returns a list of currently connected endpoints.  */
    protected get() = HashSet<Endpoint>(
      mEstablishedConnections.values
    )

  /**
   * Sends a [Payload] to all currently connected endpoints.
   *
   * @param payload The data you want to send.
   */
  protected fun send(payload: Payload) {
    send(payload, mEstablishedConnections.keys)
  }

  private fun send(payload: Payload, endpoints: Set<String>) {
    mConnectionsClient
      ?.sendPayload(endpoints.toList(), payload)
      ?.addOnFailureListener { e -> logW("sendPayload() failed.", e) }
  }

  protected fun updateState() {

  }



  protected abstract val name: String
  protected abstract val serviceId: String
  protected abstract val strategy: Strategy?

  @CallSuper
  protected fun logV(msg: String) {
    Log.v(TAG, msg)
  }

  @CallSuper
  protected fun logD(msg: String) {
    Log.d(TAG, msg)
  }

  @CallSuper
  protected fun logW(msg: String) {
    Log.w(TAG, msg)
  }

  @CallSuper
  protected fun logW(msg: String, e: Throwable? = null) {
    Log.w(TAG, msg, e)
  }

  @CallSuper
  protected fun logE(msg: String, e: Throwable? = null) {
    Log.e(TAG, msg, e)
  }



  companion object {

    const val TAG = "ConnectionsActivity"

    /**
     * These permissions are required before connecting to Nearby Connections.
     */
    val requiredPermissions: Array<String>
      get() = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
          arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.NEARBY_WIFI_DEVICES,
            Manifest.permission.CAMERA
          )
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
          arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
          )
        }

        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
          arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
          )
        }

        else -> {
          arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION
          )
        }
      }

    //     = arrayOf(
//      Manifest.permission.BLUETOOTH_SCAN,
//      Manifest.permission.BLUETOOTH_ADVERTISE,
//      Manifest.permission.BLUETOOTH_CONNECT,
//      Manifest.permission.ACCESS_WIFI_STATE,
//      Manifest.permission.CHANGE_WIFI_STATE,
//      Manifest.permission.NEARBY_WIFI_DEVICES
//    )

    /**
     * An optional hook to pool any permissions the app needs with the permissions ConnectionsActivity
     * will request.
     *
     * @return All permissions required for the app to properly function.
     */
//      protected get() = Companion.field


    private const val REQUEST_CODE_REQUIRED_PERMISSIONS = 1

    /**
     * Transforms a [Status] into a English-readable message for logging.
     *
     * @param status The current status
     * @return A readable String. eg. [404]File not found.
     */
    private fun toString(status: Status): String {
      return java.lang.String.format(
        Locale.US,
        "[%d]%s",
        status.getStatusCode(),
        if (status.getStatusMessage() != null) status.getStatusMessage() else ConnectionsStatusCodes.getStatusCodeString(
          status.getStatusCode()
        )
      )
    }

    /**
     * Returns `true` if the app was granted all the permissions. Otherwise, returns `false`.
     */
    fun hasPermissions(context: Context?, permissions: Array<String>): Boolean {
      for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(context!!, permission)
          != PackageManager.PERMISSION_GRANTED
        ) {
          return false
        }
      }
      return true
    }
  }

  fun getUniqueName(): String {
    val names =
      arrayListOf("Jane", "Erica", "Marie", "Anna", "Julia", "Lily", "Rose", "Emilie", "Christina")
    val names2 =
      arrayListOf("Jack", "Ryan", "David", "Luke", "Matt", "Bruce", "Will", "Chris", "Alex")

    var tmpname = ""
    val random = Random()

    tmpname += names[random.nextInt(9)]
    tmpname += " "
    tmpname += names2[random.nextInt(9)]

    return tmpname
  }
}
