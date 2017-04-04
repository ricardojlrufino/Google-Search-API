Google Now Xposed API
===

Original Module (Support Thread):
https://forum.xda-developers.com/xposed/modules/mod-google-search-api-t2554173

You need ROOT and Xposed:
https://forum.xda-developers.com/showthread.php?t=3034811

This repo is a Android Studio project (disable *Instant Run*).


## Create plugins

See [Support Thread](https://forum.xda-developers.com/xposed/modules/mod-google-search-api-t2554173)

Implementing plugins is as easy as copying one file to your source, and implementing a BroadcastReceiver that listens for the search queries (sent as text). 
All this is demonstrated in the *support thread*.

AndroidManifest.xml 
```xml
<uses-permission android:name="com.mohammadag.googlesearchapi.permission.ACCESS_GGOGLE_SEARCH_API" />
...
<receiver android:name=".now.GoogleSearchReceiver" >
    <intent-filter>
        <action android:name="com.mohammadag.googlesearchapi.NEW_SEARCH" />
    </intent-filter>
</receiver>
```
*GoogleSearchReceiver.java*  
```java
public class GoogleSearchReceiver extends BroadcastReceiver {
    @Override
	public void onReceive(Context context, Intent intent) {
		String queryText = intent.getStringExtra(GoogleSearchApi.KEY_QUERY_TEXT);
		Toast.makeText(context, "Test de API GOOGLE: " + queryText, Toast.LENGTH_SHORT).show();
	}
}
```

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
