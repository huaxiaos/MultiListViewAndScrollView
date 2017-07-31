package com.sunhuaxiao.multilistviewandscrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

/**
 * Description 自定义ScrollView
 * Author sunhuaxiao
 * Date 2017/7/28
 */

public class MultiScrollView extends ScrollView {

    private ScrollViewListener mScrollViewListener;
    private boolean mForbidChildScroll = false;

    public MultiScrollView(Context context) {
        super(context);
    }

    public MultiScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScrollViewListener(ScrollViewListener scrollViewListener) {
        mScrollViewListener = scrollViewListener;
    }

    public void forbidChildScroll() {
        mForbidChildScroll = true;
    }

    public void allowChildScroll() {
        mForbidChildScroll = false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return mForbidChildScroll || super.onInterceptTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldx, int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (mScrollViewListener != null) {
            mScrollViewListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    interface ScrollViewListener {
        void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy);
    }

}
