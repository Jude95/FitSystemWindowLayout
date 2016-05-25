package com.jude.demo;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.WindowInsets;
import android.widget.FrameLayout;

public class TestView extends FrameLayout {

        public TestView(Context context) {
            super(context);
        }

        public TestView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public TestView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        public WindowInsets dispatchApplyWindowInsets(WindowInsets insets) {
            Log.i(getTag()+" TestActivity","dispatchApplyWindowInsets "+insets);
            return super.dispatchApplyWindowInsets(insets);
        }

        @Override
        public WindowInsets onApplyWindowInsets(WindowInsets insets) {
            Log.i(getTag()+" TestActivity","onApplyWindowInsets "+insets);
            return super.onApplyWindowInsets(insets);
        }

        @Override
        protected boolean fitSystemWindows(Rect insets) {
            Log.i(getTag()+" TestActivity","fitSystemWindows " +insets);
            return super.fitSystemWindows(insets);
        }
    }