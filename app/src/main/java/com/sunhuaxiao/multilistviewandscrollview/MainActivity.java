package com.sunhuaxiao.multilistviewandscrollview;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private int mLastY;
    private boolean mMix;
    private MultiListView mLv1;
    private MultiListView mLv2;
    private MultiScrollView mSv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSv = (MultiScrollView) findViewById(R.id.sv);
        mLv1 = (MultiListView) findViewById(R.id.list_1);
        mLv2 = (MultiListView) findViewById(R.id.list_2);
        final ConstraintLayout root = (ConstraintLayout) findViewById(R.id.root);

        mLv1.setScrollView(mSv);
        mLv2.setScrollView(mSv);

        final MyAdapter adapter1 = new MyAdapter(this, 1, 100);
        mLv1.setAdapter(adapter1);

        MyAdapter adapter2 = new MyAdapter(this, 2, 100);
        mLv2.setAdapter(adapter2);

        mLv1.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        // lv1滚动到底部，屏蔽lv1的滚动，开启sv的滚动，状态设置为混合状态
                        // 这段代码不能省略，否则在lv1滚动到底部后，无法开启sv的滚动，进而无法加载lv2
                        if (view.getLastVisiblePosition() == (view.getCount() - 1)) {
                            setMixStatus(true);
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // Empty
            }
        });

        mLv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "lv1 " + position, Toast.LENGTH_SHORT).show();
            }
        });

        mLv2.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                switch (scrollState) {
                    case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
                        // lv2滚动到顶部，屏蔽lv2的滚动，开启sv的滚动
                        if (view.getFirstVisiblePosition() == 0) {
                            mLv2.allowParentScroll();
                            mSv.forbidChildScroll();
                        }
                        break;
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (visibleItemCount == totalItemCount) {
                    // 说明listview2的高度是小于父控件高度的
                    mLv2.setMaxHeight(setListViewHeightBasedOnChildren(mLv2, 0));
                    mLv2.setMax(true);
                }
            }
        });

        mLv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, "lv2 " + position, Toast.LENGTH_SHORT).show();
            }
        });

        mSv.setScrollViewListener(new MultiScrollView.ScrollViewListener() {
            @Override
            public void onScrollChanged(ScrollView scrollView, int x, int y, int oldx, int oldy) {
                int[] svLocation = new int[2];
                mSv.getLocationOnScreen(svLocation);

                int[] lv1location = new int[2];
                mLv1.getLocationOnScreen(lv1location);

                // 如果listview1正好填满整个屏幕，屏蔽scrollview的滚动
                if (lv1location[1] == svLocation[1]) {
                    mLv1.forbidParentScroll();
                    mSv.allowChildScroll();
                    mLv1.setMix(false);
                    return;
                }

                int[] lv2Location = new int[2];
                mLv2.getLocationOnScreen(lv2Location);

                if (lv2Location[1] == svLocation[1]) {
                    mLv2.forbidParentScroll();
                    mSv.allowChildScroll();
                    mLv2.setMix(false);
                    return;
                }

                // lv1与lv2的混合状态下，屏蔽listview的滚动
                mSv.forbidChildScroll();
            }
        });

        mSv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        mLastY = (int) event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        int dy = (int) event.getRawY() - mLastY;

                        if (mMix) {
                            if (mLv2.isMax()) {
                                if (mLv2.getHeight() < mLv2.getMaxHeight()) {
                                    // 用于防止出现最后一项显示不全的状况
                                    ViewGroup.LayoutParams layoutParams = mLv2.getLayoutParams();
                                    layoutParams.height = mLv2.getMaxHeight();
                                    mLv2.setLayoutParams(layoutParams);
                                }
                            } else if (dy < 0) {
                                // 只有在lv2高度扩大时才执行
                                ViewGroup.LayoutParams layoutParams = mLv2.getLayoutParams();
                                int dex = mLv2.getHeight() - dy;
                                if (dex > root.getHeight()) {
                                    dex = root.getHeight();
                                }
                                layoutParams.height = dex;
                                mLv2.setLayoutParams(layoutParams);
                            }
                        }

                        mLastY = (int) event.getRawY();
                        break;

                    case MotionEvent.ACTION_UP:
                        mSv.allowChildScroll();
                        break;
                }

                return false;
            }
        });

        root.post(new Runnable() {
            @Override
            public void run() {
                int lv1Height = setListViewHeightBasedOnChildren(mLv1, root.getHeight());

                if (lv1Height < root.getHeight()) {
                    int residue = root.getHeight() - lv1Height;
                    setListViewHeightBasedOnChildren(mLv2, residue);
                    setMixStatus(true);
                }
            }
        });
    }

    private void setMixStatus(boolean mix) {
        if (mix) {
            mLv1.allowParentScroll();
            mLv2.allowParentScroll();
            mLv1.setMix(true);
            mLv2.setMix(true);
            mSv.forbidChildScroll();
        } else {
            mLv1.forbidParentScroll();
            mLv2.forbidParentScroll();
            mLv1.setMix(false);
            mLv2.setMix(false);
            mSv.allowChildScroll();
        }

        mMix = mix;
    }

    private static class MyAdapter extends BaseAdapter {

        private Context mContext;
        private int mType;
        private int mCount;

        MyAdapter(Context context, int type, int count) {
            mContext = context;
            mType = type;
            mCount = count;
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(mContext);
            textView.setText("listview " + mType + " " + position);
            convertView = textView;
            return convertView;
        }
    }

    public static int setListViewHeightBasedOnChildren(ListView listView, int maxHeight) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return 0;
        }

        int totalHeight = 0;
        int dividerHeight = listView.getDividerHeight();
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight = totalHeight + listItem.getMeasuredHeight() + dividerHeight;

            if (totalHeight > maxHeight && maxHeight > 0) {
                ViewGroup.LayoutParams params = listView.getLayoutParams();
                params.height = maxHeight;
                listView.setLayoutParams(params);
                return maxHeight;
            }
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight;
        listView.setLayoutParams(params);

        return totalHeight;
    }

}
