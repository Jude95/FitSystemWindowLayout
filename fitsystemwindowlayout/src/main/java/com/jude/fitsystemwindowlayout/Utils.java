package com.jude.fitsystemwindowlayout;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Mr.Jude on 2015/12/24.
 */
public class Utils {
    public static final String TAG = "FitSystemBar";
    public static boolean DEBUG = true;
    /**
     * 取导航栏高度
     * @return
     */
    public static int getNavigationBarHeight(Context ctx) {
        int result = 0;
        int resourceId = ctx.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = ctx.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }


    /**
     * 取状态栏高度
     * @return
     */
    public static int getStatusBarHeight(Context ctx) {
        int result = 0;
        int resourceId = ctx.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = ctx.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static boolean hasSoftKeys(Context ctx){
        boolean hasSoftwareKeys;
        WindowManager manager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR1){
            Display d = manager.getDefaultDisplay();

            DisplayMetrics realDisplayMetrics = new DisplayMetrics();
            d.getRealMetrics(realDisplayMetrics);

            int realHeight = realDisplayMetrics.heightPixels;
            int realWidth = realDisplayMetrics.widthPixels;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            d.getMetrics(displayMetrics);

            int displayHeight = displayMetrics.heightPixels;
            int displayWidth = displayMetrics.widthPixels;

            hasSoftwareKeys =  (realWidth - displayWidth) > 0 || (realHeight - displayHeight) > 0;
        }else{
            boolean hasMenuKey = ViewConfiguration.get(ctx).hasPermanentMenuKey();
            boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            hasSoftwareKeys = !hasMenuKey && !hasBackKey;
        }
        return hasSoftwareKeys;
    }

    public static void log(String text){
        if (DEBUG)
        Log.i(TAG,text);
    }

    public static void paddingToNavigationBar(View view){
        if (!hasSoftKeys(view.getContext())||!(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT))return;
        Method method = null;
        try {
            method = view.getClass().getMethod("setClipToPadding", boolean.class);
        } catch (NoSuchMethodException e) {
            return;
        }
        try {
            method.invoke(view,false);
            view.setPadding(0,0,0,getNavigationBarHeight(view.getContext()));
        } catch (IllegalAccessException e) {
            return;
        } catch (InvocationTargetException e) {
            return;
        }
    }
}
