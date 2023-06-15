# Task Continuity

The goal of this sample is to demonstrate how we can use Nearby technologies 
to offer seamless experiences between devices.

## Pre-requisite
 * use Android Studio Hedgehog Canary 8+


## There are 3 experiences in this sample:
 * Shared whiteboard / home work / collaboration / tic-tac-toe
 * News app: continue reading on the next device
 * Video File Transfer: transfer large files to another device within the app (in dev)


### how to use:
#### Shared white board
1. Open the app on 2 devices close to each others
2. tap the connection button at the top on both devices
3. wait for the devices to be connected

#### News app
1. Open the app on 2 devices close to each others
2. tap the connection button at the top on both devices
3. on one device open on of the article and scroll down a bit
4. wait a few seconds, the article should show on the other devices at the same scroll position

#### Video File Transfer
1. Open the app on 2 devices close to each others
2. tap the connection button at the top on both devices
3. wait for the devices to be connected
4. tap record on one device only
5. record a few seconds of video
6. tap stop
7. the file transfer should start (this is not working yet)



## Update manifest to select the experiences:
### Shared whiteboard
```
<activity
            android:name=".MainActivity"
```

### News app
```
<activity
            android:name=".NewsActivity"
```

### video file transfer (not working yet)
```
<activity
            android:name=".FileTransferActivity"
```


## Nearby logic
Most of the implementation is in `ConnectionsActivity.kt`