package com.jude.fitsystemwindowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.WindowInsets;
import android.widget.RelativeLayout;

/**
 * Created by zhuchenxi on 15/11/7.
 */
public class FitSystemWindowsRelativeLayout extends RelativeLayout{
    private static int STATUSBAR_HEIGHT;
    private static int NAVIGATIONBAR_HEIGHT;

    private boolean mPaddingStatusBar;
    private boolean mPaddingNavigationBar;
    private int mStatusBarColor = 0;
    private int mStatusBarHeight = 0;
    private int mNavigationBarHeight = 0;
    private Paint mStatusBarPaint;

    public FitSystemWindowsRelativeLayout(Context context) {
        super(context);
        init();
    }

    public FitSystemWindowsRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        init();
    }

    public FitSystemWindowsRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        init();
    }

    protected void initAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.fit_system_windows);
        try {
            TypedValue typedValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.colorPrimary,typedValue,true);
            if (typedValue.resourceId!=0)
            mStatusBarColor = a.getColor(R.styleable.fit_system_windows_status_color,getResources().getColor(typedValue.resourceId));
            mPaddingStatusBar = a.getBoolean(R.styleable.fit_system_windows_padding_status, true);
            mPaddingNavigationBar = a.getBoolean(R.styleable.fit_system_windows_padding_navigation, false);
        } finally {
            a.recycle();
        }
    }

    private void init(){
        int statusBarHeight = 0;
        int navigationBarHeight = 0;
        setWillNotDraw(false);
        STATUSBAR_HEIGHT = Utils.getStatusBarHeight(getContext());
        NAVIGATIONBAR_HEIGHT = Utils.getNavigationBarHeight(getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ){
            if (mPaddingStatusBar) statusBarHeight = STATUSBAR_HEIGHT;
            if (mPaddingNavigationBar&&Utils.hasSoftKeys(getContext())) navigationBarHeight = NAVIGATIONBAR_HEIGHT;
        }
        setPadding(0,statusBarHeight,0,navigationBarHeight);
        mStatusBarHeight = statusBarHeight;
        mNavigationBarHeight = navigationBarHeight;
        mStatusBarPaint = new Paint();
        mStatusBarPaint.setColor(mStatusBarColor);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mPaddingStatusBar){
            canvas.drawRect(0,0,getRight(),mStatusBarHeight,mStatusBarPaint);
        }
    }

    public void setStatusBarColor(int color){
        mStatusBarColor = color;
        invalidate();
    }


    @Override
    public final WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            int bottom = insets.getSystemWindowInsetBottom();
            if(insets.getSystemWindowInsetBottom() == NAVIGATIONBAR_HEIGHT)bottom = mNavigationBarHeight;
            return super.onApplyWindowInsets(insets.replaceSystemWindowInsets(0, mStatusBarHeight, 0,bottom));
        } else {
            return insets;
        }
    }
}
