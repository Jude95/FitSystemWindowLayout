package com.jude.fitsystemwindowlayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.FrameLayout;

/**
 * Created by zhuchenxi on 15/11/7.
 */
public class FitSystemWindowsFrameLayout extends FrameLayout{
    private boolean mPaddingStatusBar;
    private boolean mPaddingNavigationBar;
    private int mStatusBarColor = 0;
    private int mStatusBarHeight = 0;
    private int mNavigationBarHeight = 0;
    private Paint mStatusBarPaint;

    public FitSystemWindowsFrameLayout(Context context) {
        super(context);
        init();
    }

    public FitSystemWindowsFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        init();
    }

    public FitSystemWindowsFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        init();
    }
    protected void initAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.fit_system_windows);
        try {
            TypedValue typedValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.colorPrimary,typedValue,true);
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ){
            if (mPaddingStatusBar) statusBarHeight = Utils.getStatusBarHeight(getContext());
            if (mPaddingNavigationBar) navigationBarHeight = Utils.getNavigationBarHeight(getContext());
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
}
