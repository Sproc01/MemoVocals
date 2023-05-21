# MemoVocals
 App to record audio of max 30 seconds on an android device. The user must grand permission to record using microphone
 
 When the app is first launched the mainActivity is created with an empty recycler view and a button record. When the button is pressed the app starts to record audio creating a file with date-time as name.
 From the detail activity you can play/delete a record and open a detailActivty given a record.
 In the detailActivity you can listen to the record or substitute the record.
 
 The playback of the record is managed with a service so you can listen the record while the app is in background, while you can record audio only if the app is in foreground.
