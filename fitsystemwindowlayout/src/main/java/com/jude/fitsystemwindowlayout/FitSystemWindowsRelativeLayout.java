package com.jude.fitsystemwindowlayout;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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

    private boolean isInputMethod = false;
    private int mInputMethodHeight = 0;
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
        setFitsSystemWindows(false);//不然4.4就会绘制默认的statusBar遮罩
        mScreenOrientation = (getResources().getConfiguration().orientation== Configuration.ORIENTATION_PORTRAIT)?VERTICAL:HORIZONTAL;
        STATUSBAR_HEIGHT = Utils.getStatusBarHeight(getContext());
        NAVIGATIONBAR_HEIGHT = Utils.getNavigationBarHeight(getContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT ){
            statusBarHeight = STATUSBAR_HEIGHT;
            if (Utils.hasSoftKeys(getContext())) navigationBarHeight = NAVIGATIONBAR_HEIGHT;
        }
        mStatusBarHeight = statusBarHeight;
        mNavigationBarHeight = navigationBarHeight;
        mStatusBarPaint = new Paint();
        mStatusBarPaint.setColor(mStatusBarColor);
        Utils.log("init"+"  mStatusBarHeight:"+mStatusBarHeight+"  mNavigationBarHeight:"+mNavigationBarHeight);
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
    protected boolean fitSystemWindows(Rect insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Utils.log("fitSystemWindows"
                    +"  Left:"+insets.left
                    +"  Top:"+insets.top
                    +"  Right:"+insets.right
                    +"  Bottom:"+insets.bottom);
            if(insets.bottom > NAVIGATIONBAR_HEIGHT){
                mInputMethodHeight = insets.bottom;
                isInputMethod = true;
            }else {
                mInputMethodHeight = 0;
                isInputMethod = false;
            }
            insets.set(0,0,0,0);
        }
        return super.fitSystemWindows(insets);
    }

    @Override
    public final WindowInsets onApplyWindowInsets(WindowInsets insets) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Utils.log("onApplyWindowInsets"
                    +"  Left:"+insets.getSystemWindowInsetLeft()
                    +"  Top:"+insets.getSystemWindowInsetTop()
                    +"  Right:"+insets.getSystemWindowInsetRight()
                    +"  Bottom:"+insets.getSystemWindowInsetBottom());

            if(insets.getSystemWindowInsetBottom() > NAVIGATIONBAR_HEIGHT){
                mInputMethodHeight = insets.getSystemWindowInsetBottom();
                isInputMethod = true;
            }else {
                mInputMethodHeight = 0;
                isInputMethod = false;
            }
            insets.replaceSystemWindowInsets(0,0,0,0);//使默认的padding效果失效，因为我完全自己处理了。
            return insets;//我重写了自己的Padding规则，所以我可以无视对insets的处理。
        } else {
            return insets;
        }
    }

    //返回底部应有padding
    private int getNavigationVerticalValue(LayoutParams lp){
        if (isInputMethod)return mInputMethodHeight;
        return (mScreenOrientation == VERTICAL)?(lp.mMarginNavigation?mNavigationBarHeight:0):0;
    }
    //返回右边应有padding
    private int getNavigationHorizontalValue(LayoutParams lp){
        return (mScreenOrientation == HORIZONTAL)?(lp.mMarginNavigation?mNavigationBarHeight:0):0;
    }

    //返回顶部应有padding
    private int getStatusValue(LayoutParams lp){
        return (lp.mMarginStatus?mStatusBarHeight:0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            LayoutParams lp = (LayoutParams) child.getLayoutParams();
            //可滑动View内Pading
            if (mScreenOrientation == VERTICAL&&lp.isPaddingNavigation())Utils.paddingToNavigationBar(child);

            //合适的marginStatus与marginNavigation
            if (!lp.hasSetMarginStatus())lp.topMargin += getStatusValue(lp);
            else lp.topMargin += lp.isMarginStatus()?mStatusBarHeight:0;

            if (!lp.hasSetMarginNavigation()){
                lp.bottomMargin +=  getNavigationVerticalValue(lp);
                lp.rightMargin  +=  getNavigationHorizontalValue(lp);
            }else {
                lp.bottomMargin +=  (mScreenOrientation == VERTICAL)?mNavigationBarHeight:0;
                lp.rightMargin  +=  (mScreenOrientation == HORIZONTAL)?mNavigationBarHeight:0;
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
        private boolean mMarginStatus = false;
        private boolean mHasSetMarginStatus = false;
        private boolean mMarginNavigation = false;
        private boolean mHasSetMarginNavigation = false;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.fit_system_windows);
            this.mPaddingNavigation = a.getBoolean(
                    R.styleable.fit_system_windows_padding_navigation,
                    false);

            mHasSetMarginStatus = a.hasValue(R.styleable.fit_system_windows_margin_status);
            mHasSetMarginNavigation = a.hasValue(R.styleable.fit_system_windows_margin_navigation);
            this.mMarginStatus = a.getBoolean(
                    R.styleable.fit_system_windows_margin_status,
                    false);
            this.mMarginNavigation = a.getBoolean(
                    R.styleable.fit_system_windows_margin_navigation,
                    false);
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

        public boolean isMarginStatus() {
            return mMarginStatus;
        }

        public void setMarginStatus(boolean mMarginStatus) {
            mHasSetMarginStatus = true;
            this.mMarginStatus = mMarginStatus;
        }

        public boolean hasSetMarginStatus() {
            return mHasSetMarginStatus;
        }

        public boolean isMarginNavigation() {
            return mMarginNavigation;
        }

        public void setMarginNavigation(boolean mMarginNavigation) {
            mHasSetMarginNavigation = true;
            this.mMarginNavigation = mMarginNavigation;
        }

        public boolean hasSetMarginNavigation() {
            return mHasSetMarginNavigation;
        }
    }
}
