# MemoVocals
 App to record audio of max 30 seconds on an android device.

 ## Usage
 The user must grant permission to record using microphone and to listen the audio user must grant the permission to send notification.
 When the app is first launched the mainActivity is created with an empty recycler view and a button record. When the button is pressed the app starts to   record audio, the record is saved in a new file with date-time as name.
 From the mainActivity you can delete a record and open a detailActivty.
 In the detailActivity you can listen to the record or substitute the record.
 You can listen a record while the app is in foreground or in background, while you can record audio only if the recordingActivity is in foreground.

 ## Technical information
 The app is written in kotlin(https://kotlinlang.org/docs/home.html) and it uses material3(https://m3.material.io) for interface components. This app is compatible with android device with android 7.0 or newer(android SDK 24 or higher). The app is tested onto two devices: a device with android 9 and a device with android 13.
 To allow the user to listen while the app is in background the app use a service in bound and started mode.


 ## SCREENSHOT
 Vertical Screenshot in light background
 <p float="left">
  <img src="https://github.com/Sproc01/MemoVocals/assets/95143387/e8cdb3e0-6bfb-40d4-87f2-c6f6894bed92" alt="MainActivity" width="200" />
  <img src="https://github.com/Sproc01/MemoVocals/assets/95143387/244c3b19-6956-40fe-a16c-50b16b0dcc20" alt="RecordingActivity" width="200" /> 
  <img src="https://github.com/Sproc01/MemoVocals/assets/95143387/02fc534e-ccc5-4a5d-9262-6342042a463b" alt="DetailActivity" width="200"/>
</p>

 
