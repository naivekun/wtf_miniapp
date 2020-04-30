package com.naivekun.fuckqqminiapp;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class XposedInit implements IXposedHookLoadPackage{
    String packageName = "";

    @Override
public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        packageName = lpparam.packageName;
        if (packageName.equals("com.tencent.mobileqq")) {
            debugOutput("qq detected");
            final Class<?> MiniAppConfig = XposedHelpers.findClass("com.tencent.mobileqq.mini.apkg.MiniAppConfig",lpparam.classLoader);
            final Class<?> MiniAppInfo = XposedHelpers.findClass("com.tencent.mobileqq.mini.apkg.MiniAppInfo", lpparam.classLoader);
            final Class<?> FirstPageInfo = XposedHelpers.findClass("com.tencent.mobileqq.mini.apkg.FirstPageInfo",lpparam.classLoader);
            XposedHelpers.findAndHookMethod(Activity.class, "startActivityForResult", Intent.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    super.beforeHookedMethod(param);
                    Intent interceptedIntent = (Intent)param.args[0];
                    debugOutput("qq intent intercepted");
                    if (interceptedIntent.getBooleanExtra("fucked!YAY!", false)) {
                        return;
                    }
                    Object MiniAppConfigData = MiniAppConfig.getField ("config").get((Object) interceptedIntent.getExtras().getParcelable("CONFIG"));
                    Object FirstPageData  = MiniAppInfo.getField("firstPage").get(MiniAppConfigData);

                    Field pagePathField = FirstPageInfo.getField("pagePath");
                    String pagePath = (String) pagePathField.get(FirstPageData);
                    debugOutput("pagePath:"+pagePath);
                    String rawUrl = "";

                    //support bilibili
                    if (pagePath.startsWith("pages/video/video?bvid=")){
                        debugOutput("bilibili miniprogram detected");
                        rawUrl = "https://www.bilibili.com/video/"+pagePath.substring(23).split("&")[0];
                    }

                    if (pagePath.startsWith("zhihu/answer?")) {
                        debugOutput("zhihu miniprogram detected");;
                        rawUrl = "https://www.zhihu.com/answer/"+pagePath.substring(16).split("&")[0];
                    }

                    debugOutput("sanitized url:"+rawUrl);
                    if (rawUrl != "") {
                        Intent i = new Intent();
                        i.setAction(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(rawUrl));
                        i.putExtra("fucked!YAY!",true);
                        ((Activity)(param.thisObject)).startActivity(i);
                        param.setResult(null);
                    }
                }
            });

        }
    }

    public void debugOutput(String msg) {
        Log.d("qnmd_mini_app",msg);
    }
}

