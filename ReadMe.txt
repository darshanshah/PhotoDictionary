Android Final Project : PhotoDictionary
Developed By:
1) Darshan Shah(817396483)
2) Harshil Shah(817399577)

-----------------------------------------------------------------------------------------------------------------------------
What is a PhotoDictionary?
Its an app, where you take a picture of a word and it will give you meaning of that word. It's supported with both  online and offline too. So you dont need to worry if you don't have internet connection.
-----------------------------------------------------------------------------------------------------------------------------
How to use this app?

1) Press and hold the center button until camera focuses to your text. This may take < 2 secs depending upon your device configuration.
2) For low light case, goto the Settings and check the "Light" option and then follow step 1.
3) App will capture the single word in the window, identify the word you want to know the meaning of, and gives back the wiktionary meaning, if at all available.
		
-----------------------------------------------------------------------------------------------------------------------------
Library's or 3rd party API used:

Tesseract Tools for Android:
It's an library bt google developers built on top of tesseract library supported for android devices.
Tesseract Tools for Android is a set of Android APIs and build files for the Tesseract OCR and Leptonica image processing libraries.
https://github.com/rmtheis/tess-two/tree/master/eyes-two/src/com/googlecode/eyesfree/textdetect
https://github.com/rmtheis/tess-two
http://www.leptonica.com/
https://code.google.com/p/tesseract-ocr/

------------------------------------------------------------------------------------
Online wikiDictionary:
To make it work with online version we get data using this library that will get the data in JSON format and parse it to html.
https://android.googlesource.com/platform/development/+/master/samples/Wiktionary/src/com/example/android/wiktionary

------------------------------------------------------------------------------------
For some of the concepts like camera usage we used zxing library. 
ZXing project: http://code.google.com/p/zxing
For JSON parsing of data we used google's JSON simple library.
To access the tar/zip or to exctract data from that we user jtar library

-----------------------------------------------------------------------------------------------------------------------------
Supported Version:
We have targated at Android version 10. (Because the Tesseract Tools for Android works with Android 2.2 or higher).

-----------------------------------------------------------------------------------------------------------------------------
Future Work:
1) For the first time when you install the app we need the internet to get the Dictionary so it will work offline if you dont have network connection available.
2) We plan to let user edit the word so if OCR didnt recognized correctly user can edit it.
3) We plan to support multiple language dictionary in the future. For now only English is supported.
4) It only supports single word detection, means you need to select the word within the frame on the screen. So it will detect only one word.


