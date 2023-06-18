# MemoVocals
 App to record audio of max 30 seconds on an android device. The user must grant permission to record using microphone, to listen the audio user must grant the permission to send notification.
 
 When the app is first launched the mainActivity is created with an empty recycler view and a button record. When the button is pressed the app starts to   record audio, creating a file with date-time as name.
 From the main activity you can delete a record and open a detailActivty.
 In the detailActivity you can listen to the record or substitute the record.
 
 The playback of the record is managed with a service so you can listen the record while the app is in background, while you can record audio only if the recording activity is in foreground.


 ## SCREENSHOT
 Vertical Screenshot in light background
 <p float="left">
  <img src="https://github.com/Sproc01/MemoVocals/assets/95143387/e8cdb3e0-6bfb-40d4-87f2-c6f6894bed92" width="100" />
  <img src="https://github.com/Sproc01/MemoVocals/assets/95143387/244c3b19-6956-40fe-a16c-50b16b0dcc20" width="100" /> 
  <img src="https://github.com/Sproc01/MemoVocals/assets/95143387/02fc534e-ccc5-4a5d-9262-6342042a463b" width="100"/>
</p>

 
