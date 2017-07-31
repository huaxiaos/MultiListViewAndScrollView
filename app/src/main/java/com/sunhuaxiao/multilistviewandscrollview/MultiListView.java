package com.sunhuaxiao.multilistviewandscrollview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.ListView;
import android.widget.ScrollView;

/**
 * Description 自定义ListView
 * Author sunhuaxiao
 * Date 2017/7/28
 */

public class MultiListView extends ListView {

    private ScrollView mScrollView;
    private boolean mForbidParentScroll = true;
    private boolean mMax;
    private int mMaxHeight;

    public MultiListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiListView(Context context) {
        super(context);
    }

    public MultiListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void forbidParentScroll() {
        mForbidParentScroll = true;
    }

    public void allowParentScroll() {
        mForbidParentScroll = false;
    }

    public void setScrollView(ScrollView scrollView) {
        this.mScrollView = scrollView;
    }

    @SuppressWarnings("unused")
    public ScrollView getScrollView() {
        ViewParent parent = getParent();
        while (parent != null) {
            if (parent instanceof ScrollView) {
                return (ScrollView) parent;
            }
            parent = parent.getParent();
        }

        return null;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (mForbidParentScroll) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mScrollView.requestDisallowInterceptTouchEvent(true);
                    break;
                case MotionEvent.ACTION_UP:
                    mScrollView.requestDisallowInterceptTouchEvent(false);
                    break;
            }
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean isMax() {
        return mMax;
    }

    public void setMax(boolean max) {
        this.mMax = max;
    }

    public int getMaxHeight() {
        return mMaxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        this.mMaxHeight = maxHeight;
    }
}
