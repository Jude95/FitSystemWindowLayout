package com.jude.fitsystemwindowlayout;

import android.content.Context;

/**
 * Created by Mr.Jude on 2015/12/24.
 */
public class Utils {
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
}
