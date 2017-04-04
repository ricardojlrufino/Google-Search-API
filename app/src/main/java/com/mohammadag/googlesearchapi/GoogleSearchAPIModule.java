package com.mohammadag.googlesearchapi;

import android.app.AndroidAppHelper;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.widget.TextView;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;


public class GoogleSearchAPIModule implements IXposedHookLoadPackage, IXposedHookZygoteInit, IXposedHookInitPackageResources {

	private static XSharedPreferences mPreferences;

    private String xposedTargetClass = null;
    private Class<?> xposedTargetParam = CharSequence.class;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		mPreferences = new XSharedPreferences(Constants.APP_PACKAGE, "Hooks");
	}

    @Override
    public void handleInitPackageResources(final XC_InitPackageResources.InitPackageResourcesParam resparam) throws Throwable {
        if (!resparam.packageName.equals(Constants.APP_PACKAGE)) {
            return;
        }

        resparam.res.hookLayout(Constants.APP_PACKAGE, "layout", "fragment_intro", new XC_LayoutInflated() {
            @Override
            public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) throws Throwable {
                TextView status = (TextView) liparam.view.findViewById(
                        liparam.res.getIdentifier("status_text", "id", Constants.APP_PACKAGE));
                status.setText(Html.fromHtml("<b>Status:</b> Up and running<br/>"));
            }
        });
    }

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {

		if (!lpparam.packageName.equals(Constants.GOOGLE_SEARCH_PACKAGE))
			return;

		// Thank you to KeepChat For the Following Code Snippet
		// http://git.io/JJZPaw
		Object activityThread = callStaticMethod(findClass("android.app.ActivityThread", null), "currentActivityThread");
        final Context context = (Context) callMethod(activityThread, "getSystemContext");
        
        final int versionCheck = context.getPackageManager().getPackageInfo(lpparam.packageName, 0).versionCode;
        //End Snippet

		XposedBridge.log("Version Code: " + versionCheck);

        mPreferences.makeWorldReadable();

        xposedTargetClass = mPreferences.getString("Hook", Constants.DEFAULT_HOOK);

        XposedBridge.log("GSAPI Class: " + xposedTargetClass);

        try {
            Class<?> targetClass = findClass(xposedTargetClass, lpparam.classLoader);
            Method[] methods = XposedHelpers.findMethodsByExactParameters(targetClass, void.class, xposedTargetParam);

            if(methods.length > 0)  XposedBridge.log(" Found: " + methods[0].getName());

            findAndHookMethod(targetClass, methods[0].getName(), xposedTargetParam, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("GSAPI before: " + param.args[0].toString());
                    broadcastGoogleSearch(AndroidAppHelper.currentApplication().getApplicationContext(), param.args[0].toString());
                    param.args[0] = new String("none");
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("GSAPI after: " + param.args[0].toString());
                }
            });



//            Class<?> targetClass = findClass(xposedTargetClass, lpparam.classLoader);
//            Method[] methods = XposedHelpers.findMethodsByExactParameters(targetClass, void.class, xposedTargetParam);
//
//            if(methods.length > 0)  XposedBridge.log(" Found: " + methods[0].getName());
//
//            findAndHookMethod(targetClass, methods[0].getName(), xposedTargetParam, new XC_MethodHook() {
//
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    XposedBridge.log("GSAPI broadcast : " + param.args[0].toString());
//                    broadcastGoogleSearch(AndroidAppHelper.currentApplication().getApplicationContext(), param.args[0].toString());
//                }
//            });

        } catch (Exception e) {
            XposedBridge.log("GSAPI - " +e);
        }
    }

	private static void broadcastGoogleSearch(Context context, CharSequence searchText) {
        Intent intent = new Intent(GoogleSearchApi.INTENT_NEW_SEARCH);
        intent.putExtra(GoogleSearchApi.KEY_VOICE_TYPE, "voiceResult");
        intent.putExtra(GoogleSearchApi.KEY_QUERY_TEXT, searchText.toString());
		context.sendBroadcast(intent, Constants.PERMISSION);
	}

}
