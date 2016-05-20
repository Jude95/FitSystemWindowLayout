package com.jude.fitsystemwindowlayout;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.RelativeLayout;

/**
 * Created by zhuchenxi on 15/11/7.
 */
public class FitSystemWindowsRelativeLayout extends RelativeLayout{
    private static int STATUSBAR_HEIGHT;
    private static int NAVIGATIONBAR_HEIGHT;
    private int mScreenOrientation = VERTICAL;

    private boolean mPaddingStatusBar;
    private boolean mPaddingNavigationBar;
    private int mStatusBarColor = 0;
    private int mStatusBarHeight = 0;
    private int mNavigationBarHeight = 0;
    private Paint mStatusBarPaint;
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

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
        setFitsSystemWindows(true);
        mScreenOrientation = (getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)?VERTICAL:HORIZONTAL;

        STATUSBAR_HEIGHT = Utils.getStatusBarHeight(getContext());
        NAVIGATIONBAR_HEIGHT = Utils.getNavigationBarHeight(getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ){
            if (mPaddingStatusBar) statusBarHeight = STATUSBAR_HEIGHT;
            if (mPaddingNavigationBar&&Utils.hasSoftKeys(getContext())) navigationBarHeight = NAVIGATIONBAR_HEIGHT;
        }
        if(getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT){
            setPadding(0,statusBarHeight,0,navigationBarHeight);
        }else {
            setPadding(0,statusBarHeight,navigationBarHeight,0);
        }
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
        mStatusBarPaint.setColor(mStatusBarColor);
        invalidate();
    }


    @Override
    public final WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int bottom = insets.getSystemWindowInsetBottom();
            if(insets.getSystemWindowInsetBottom() == NAVIGATIONBAR_HEIGHT)bottom = mNavigationBarHeight;
            return super.onApplyWindowInsets(insets.replaceSystemWindowInsets(0, mStatusBarHeight, 0,bottom));
        } else {
            return insets;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            if (mScreenOrientation == VERTICAL&&lp.isPaddingNavigation())Utils.paddingToNavigationBar(child);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
        } else if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }

    @Override
    protected RelativeLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    public static class LayoutParams extends RelativeLayout.LayoutParams {
        private boolean mPaddingNavigation = false;


        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.fit_system_windows);
            this.mPaddingNavigation = a.getBoolean(
                    R.styleable.fit_system_windows_padding_navigation,
                    false);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public boolean isPaddingNavigation() {
            return mPaddingNavigation;
        }

        public void setPaddingNavigation(boolean mPaddingNavigation) {
            this.mPaddingNavigation = mPaddingNavigation;
        }
    }
}
