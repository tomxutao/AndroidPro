package com.KGRecyclerView;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;


public class KgDataRecylerView extends RecyclerView {
    public KgDataRecylerView(Context context) {
        super(context);
        initListener();
    }

    public KgDataRecylerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initListener();
    }

    public KgDataRecylerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initListener();
    }

    public void initListener() {
        OnScrollListener onScrollListener = new OnScrollListener() {
            int lastFirstVisibleItem;
            int visibleItemCount;
            int totalItemCount;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                if (layoutManager instanceof LinearLayoutManager) {
                    lastFirstVisibleItem = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        };
        addOnScrollListener(onScrollListener);
    }
}
