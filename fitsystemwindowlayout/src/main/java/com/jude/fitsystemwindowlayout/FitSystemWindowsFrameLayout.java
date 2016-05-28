package com.jude.fitsystemwindowlayout;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.FrameLayout;

import java.util.ArrayList;

/**
 * Created by zhuchenxi on 15/11/7.
 */
public class FitSystemWindowsFrameLayout extends FrameLayout{
    private static int STATUSBAR_HEIGHT;
    private static int NAVIGATIONBAR_HEIGHT;

    private boolean mPaddingStatusBar;
    private boolean mPaddingNavigationBar;
    private int mStatusBarColor = 0;
    private int mStatusBarHeight = 0;
    private int mNavigationBarHeight = 0;
    private int mScreenOrientation = VERTICAL;
    private boolean isInputMethod = false;
    private int mInputMethodHeight = 0;
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;

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
            int colorAttr;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                colorAttr = android.R.attr.colorPrimary;
            } else {
                colorAttr = getContext().getResources().getIdentifier("colorPrimary", "attr", getContext().getPackageName());
            }
            TypedValue outValue = new TypedValue();
            getContext().getTheme().resolveAttribute(colorAttr, outValue, true);

            if (outValue.resourceId!=0)mStatusBarColor = getResources().getColor(outValue.resourceId);
            mStatusBarColor = a.getColor(R.styleable.fit_system_windows_status_color,mStatusBarColor);
            mPaddingStatusBar = a.getBoolean(R.styleable.fit_system_windows_padding_status, true);
            mPaddingNavigationBar = a.getBoolean(R.styleable.fit_system_windows_padding_navigation, false);
            Utils.log("initAttrs"+" mStatusBarColor"+mStatusBarColor+"  mPaddingStatusBar:"+mPaddingStatusBar+"  mPaddingStatusBar:"+mPaddingStatusBar);
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

    private final ArrayList<View> mMatchParentChildren = new ArrayList<View>(1);
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        final boolean measureMatchParentChildren =
                MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY ||
                        MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        mMatchParentChildren.clear();

        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth,
                        child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight = Math.max(maxHeight,
                        child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT ||
                            lp.height == LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child);
                    }
                }
                //将父布局的属性转换到子View上。
                if (lp.isPaddingNavigation()&&mScreenOrientation==VERTICAL)Utils.paddingToNavigationBar(child);
                if (!lp.hasSetMarginStatus())lp.setMarginStatus(mPaddingStatusBar);
                if (!lp.hasSetMarginNavigation())lp.setMarginNavigation(mPaddingNavigationBar);
                Utils.log("measure "+ child.getClass().getSimpleName()+"  isMarginStatus:"+(lp.isMarginStatus()?"true":"false")+"  isMarginNavigation:"+(lp.isMarginNavigation()?"true":"false"));
            }
        }

        // Account for padding too
//        maxWidth += getPaddingLeftWithForeground() + getPaddingRightWithForeground();
//        maxHeight += getPaddingTopWithForeground() + getPaddingBottomWithForeground();

        // Check against our minimum height and width
        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());

        // Check against our foreground's minimum height and width
        final Drawable drawable = getForeground();
        if (drawable != null) {
            maxHeight = Math.max(maxHeight, drawable.getMinimumHeight());
            maxWidth = Math.max(maxWidth, drawable.getMinimumWidth());
        }

        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec,
                        childState << MEASURED_HEIGHT_STATE_SHIFT));

        count = mMatchParentChildren.size();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final View child = mMatchParentChildren.get(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int childWidthMeasureSpec;
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    final int width = Math.max(0, getMeasuredWidth()
//                            - getPaddingLeftWithForeground() - getPaddingRightWithForeground()
                            - lp.leftMargin - lp.rightMargin - getNavigationHorizontalValue(lp));
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(
                            width, MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
//                            getPaddingLeftWithForeground() + getPaddingRightWithForeground() +
                            lp.leftMargin + lp.rightMargin + getNavigationHorizontalValue(lp),
                            lp.width);
                }

                final int childHeightMeasureSpec;
                if (lp.height == LayoutParams.MATCH_PARENT) {
                    final int height = Math.max(0, getMeasuredHeight()
//                            - getPaddingTopWithForeground() - getPaddingBottomWithForeground()
                            - lp.topMargin - lp.bottomMargin - getStatusValue(lp) - getNavigationVerticalValue(lp));
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
                            height, MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec,
//                            getPaddingTopWithForeground() + getPaddingBottomWithForeground() +
                            lp.topMargin + lp.bottomMargin + getStatusValue(lp) + getNavigationVerticalValue(lp),//当设置时增加额外的Padding
                            lp.height);
                }

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
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
    /**
     * {@inheritDoc}
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutChildren(left, top, right, bottom, false /* no force left gravity */);
    }

    void layoutChildren(int left, int top, int right, int bottom,
                        boolean forceLeftGravity) {
        final int count = getChildCount();

        final int parentLeft = 0;
        final int parentRight = right - left ;

        final int parentTop = 0;
        final int parentBottom = bottom - top ;

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft;
                int childTop;

                int gravity = lp.gravity;
                if (gravity == -1) {
                    gravity = Gravity.TOP|Gravity.START;
                }

                final int layoutDirection = ViewCompat.getLayoutDirection(this);
                final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
                final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                    case Gravity.CENTER_HORIZONTAL:
                        childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                                lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        if (!forceLeftGravity) {
                            childLeft = parentRight - width - lp.rightMargin - getNavigationHorizontalValue(lp);//减去横屏时右侧的导航栏
                            break;
                        }
                    case Gravity.LEFT:
                    default:
                        childLeft = parentLeft + lp.leftMargin;
                }

                switch (verticalGravity) {
                    case Gravity.TOP:
                        childTop = parentTop + lp.topMargin + getStatusValue(lp);
                        Utils.log(child.getClass().getSimpleName()+" topMargin:"+lp.topMargin+" getStatusValue:"+getStatusValue(lp));
                        break;
                    case Gravity.CENTER_VERTICAL:
                        childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                                lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = parentBottom - height - lp.bottomMargin - getNavigationVerticalValue(lp);//减去竖屏时的导航栏
                        Utils.log(child.getClass().getSimpleName()+" bottomMargin:"+lp.bottomMargin+" getNavigationVerticalValue:"+getNavigationVerticalValue(lp));
                        break;
                    default:
                        childTop = parentTop + lp.topMargin + getStatusValue(lp);
                }
                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
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
    protected FrameLayout.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams && super.checkLayoutParams(p);
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {
        private boolean mPaddingNavigation = false;
        private boolean mMarginStatus = false;
        private boolean mHasSetMarginStatus = false;
        private boolean mMarginNavigation = false;
        private boolean mHasSetMarginNavigation = false;


        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.fit_system_windows);
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
            Utils.log("LayoutParams "
                    +" mHasSetMarginStatus:"+mHasSetMarginStatus
                    +" mMarginStatus:"+mMarginStatus
                    +" mHasSetMarginNavigation:"+mHasSetMarginNavigation
                    +" mMarginNavigation:"+mMarginNavigation);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public boolean isMarginStatus() {
            return mMarginStatus;
        }

        public void setMarginStatus(boolean mMarginStatus) {
            this.mMarginStatus = mMarginStatus;
        }

        public boolean isMarginNavigation() {
            return mMarginNavigation;
        }

        public void setMarginNavigation(boolean mMarginNavigation) {
            this.mMarginNavigation = mMarginNavigation;
        }

        public boolean isPaddingNavigation() {
            return mPaddingNavigation;
        }

        public void setPaddingNavigation(boolean mPaddingNavigation) {
            this.mPaddingNavigation = mPaddingNavigation;
        }

        public boolean hasSetMarginStatus() {
            return mHasSetMarginStatus;
        }

        public boolean hasSetMarginNavigation() {
            return mHasSetMarginNavigation;
        }
    }
}
