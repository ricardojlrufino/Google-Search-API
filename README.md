Google Now Xposed API
===

Original Module (Support Thread):
https://forum.xda-developers.com/xposed/modules/mod-google-search-api-t2554173

You need ROOT and Xposed:
https://forum.xda-developers.com/showthread.php?t=3034811

This repo is a Android Studio project (disable *Instant Run*).


## Create plugins

See [Support Thread](https://forum.xda-developers.com/xposed/modules/mod-google-search-api-t2554173)

Implementing plugins is as easy as copying one file to your source, and implementing a BroadcastReceiver that listens for the search queries (sent as text). If you're implementing a UI, implement TextToSpeech for voice feedback, if not, use GoogleNowApi.speak for voice replies.
All this is demonstrated in the *support thread*.
You'll also need to add a permission to AndroidManifest.xml: *"com.mohammadag.googlesearchapi.permission.ACCESS_ GGOGLE_SEARCH_API"*
Remember that any package needs one activity to be started to work, see the music controls example for a way around that.
Of course, you're free to make your plugins free/paid/whatever.


## How to Hack Google API

XPosed tutorial:
https://github.com/rovo89/XposedBridge/wiki/Development-tutorial

Add new hooks in file and make a PR  
_Key is Google search version, and value is the className_  
*hooksversion.properties*


Find apk on device and copy to PC
> db pull /data/app/com.google.android.googlequicksearchbox-2/base.apk /media/YOU/FOLDER

Unpack and decompile (use: [Apktool](https://ibotpeaches.github.io/Apktool/)
> java -jar apktool_2.2.2.jar d base.apk

We need to find out which method is called after the text is recognized
> grep -Ril --include \*.smali "setFinalRecognizedText"

```
smali/com/google/android/apps/gsa/assist/GsaVoiceInteractionSession$GsaVoiceInteractionViewUiCallback.smali
smali/com/google/android/apps/gsa/search/shared/actions/o.smali
smali/com/google/android/apps/gsa/search/shared/overlay/a/ac.smali
smali/com/google/android/apps/gsa/search/shared/overlay/a/v.smali
smali/com/google/android/apps/gsa/searchplate/api/ISearchPlate.smali
smali/com/google/android/apps/gsa/searchplate/SearchPlate.smali
smali_classes2/com/google/android/apps/gsa/legacyui/a/cc.smali
smali_classes2/com/google/android/apps/gsa/legacyui/VelvetSearchPlate.smali
smali_classes2/com/google/android/apps/gsa/search/core/service/m.smali
smali_classes2/com/google/android/apps/gsa/search/shared/actions/h.smali
^Tsmali_classes2/com/google/android/apps/gsa/staticplugins/bq/b.smali
smali_classes3/com/google/android/remotesearch/j.smali
```

Open textin file editor (Will need a basic knowledge of smali / bytecode)
I found two promising methods in the files below:
> smali_classes2/com/google/android/apps/gsa/search/core/service/m.smali
> smali/com/google/android/apps/gsa/search/shared/overlay/a/v.smali

The name in the hook must be the name of the class.
On app, click in settings > Change Hooks
> com.google.android.apps.gsa.search.core.service.m
