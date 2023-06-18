# MemoVocals
App to record audio of max 30 seconds on an android device.

## Permission
The user must grant two permissions:
- to record audio the app must have the RECORD_AUDIO permission
- to listen the audio the app must have the POST_NOTIFICATIONS permission

## Usage
<p> When the app is launched the MainActivity is created: there is a recycler view with the records already present and a button to record. If the user press the button the app starts to record audio, this is saved in a new file with date-time as name.</p>
<p>From the MainActivity you can delete a record and open a DetailActivity.</p>
<p>In the DetailActivity you see some information about the record and you can listen to it or substitute.</p>
<p>You can listen a record while the app is in foreground or in background, while you can record audio only if the RecordingActivity is in foreground.</p>
<p>This app supports portrait and landscape mode.</p>

## Supported Language
<p>The app support the english and italian date format and strings if the system language is english or italian. if the system language is different the date time is in a generic format and the strings are in English.</p>

## Technical information
<p>The app is written in kotlin(https://kotlinlang.org/docs/home.html) and it uses material3(https://m3.material.io) for interface components. This app is compatible with android device with android 7.0 or newer(android SDK 24 or higher). The app is tested onto two devices: a device with android 9 and a device with android 13.</p>

<p>To allow the user to listen while the app is in background the app use a service in bound and start mode. The service is a mediaPlayback service because the notification must appeared immediatly after the user start the playback.</p>

## SCREENSHOT
Vertical Screenshot MainActivity and DetailActivity in italian language and in a light background
<p float="left">
<img src="https://github.com/Sproc01/MemoVocals/assets/95143387/e8cdb3e0-6bfb-40d4-87f2-c6f6894bed92" alt="MainActivityItalian" width="200" />
<img src="https://github.com/Sproc01/MemoVocals/assets/95143387/02fc534e-ccc5-4a5d-9262-6342042a463b" alt="DetailActivityItalian" width="200"/>
</p>

Vertical Screenshot RecordingActivity in english language and in a light background
<p float="left">
<img src="https://github.com/Sproc01/MemoVocals/assets/95143387/5a533460-0b2e-44f2-9cfe-bdc068b430bc" alt="RecordingActivityEnglish" width="200">
	</p>

 
