package com.KGRecyclerView;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * 带HeadFoot(兼容listview)的recyclerview
 */
public class KGRecyclerView extends KgDataRecylerView {
    private static String TAG = KGRecyclerView.class.getSimpleName();

    /**
     * 当使用GridLayoutManager时，会由于添加HeaderFooter导致空白区域出现，由此引入hasHeaderFooter
     * 设置此参数，就不能对Header Footer再进行操作，慎重
     * 要在设置setRecyclerAdapter之前设置有效
     */
    private boolean hasHeaderFooter = true;

    public void setHasHeaderFooter(boolean hasHeaderFooter) {
        this.hasHeaderFooter = hasHeaderFooter;
    }

    /**
     * The listener that receives notifications when an item is clicked.
     */
    OnItemClickListener mOnItemClickListener;

    /**
     * The listener that receives notifications when an item is long clicked.
     */
    OnItemLongClickListener mOnItemLongClickListener;

    /**
     * The area to contain headerViews <p>
     * If a headerView is set to the KGRecyclerView, it would be add
     * to this layout.
     */
    LinearLayout mHeaderArea = null;

    /**
     * The area to contain footerViews <p>
     * If a footerView is set to the KGRecyclerView, it would be add
     * to this layout.
     */
    LinearLayout mFooterArea = null;

    public KGRecyclerView(Context context) {
        super(context);
        setItemAnimator(null);
        initExtraView();
    }

    public KGRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setItemAnimator(null);
        initExtraView();
    }

    public KGRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setItemAnimator(null);
        initExtraView();
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);

        LinearLayoutManager layoutManager = getLinearLayoutManager();
        if (layoutManager != null) {
            boolean isHorizontal = layoutManager.getOrientation() == HORIZONTAL;

            mHeaderArea.setLayoutParams(new LayoutParams(isHorizontal ? LayoutParams.WRAP_CONTENT
                    : LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            mFooterArea.setLayoutParams(new LayoutParams(isHorizontal ? LayoutParams.WRAP_CONTENT
                    : LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
    }

    /**
     * If the KGRecyclerView has a layoutManager which is instanceof LinearLayoutManager,
     * return it, else return null.
     */
    public LinearLayoutManager getLinearLayoutManager() {
        LayoutManager layoutManager = getLayoutManager();
        if (layoutManager != null && layoutManager instanceof LinearLayoutManager)
            return (LinearLayoutManager) layoutManager;
        return null;
    }

    private void checkArgumentNotNull(Object o) {
        if (o == null)
            throw new IllegalArgumentException("Argument can not be null!");
    }

    public LinearLayout getmHeaderArea() {
        return mHeaderArea;
    }

    private void initExtraView() {
        if (mHeaderArea == null) {
            mHeaderArea = new LinearLayout(getContext());
            mHeaderArea.setOrientation(LinearLayout.VERTICAL);
        }
        if (mFooterArea == null) {
            mFooterArea = new LinearLayout(getContext());
            mFooterArea.setOrientation(LinearLayout.VERTICAL);

        }
    }

    public LinearLayout getmFooterArea() {
        return mFooterArea;
    }

    public int headerAreaCount() {
        return hasHeaderFooter ? 1 : 0;
    }

    public int footerAreaCount() {
        return hasHeaderFooter ? 1 : 0;
    }

    public int getExtraViewCount() {
        return hasHeaderFooter ? 2 : 0;
    }

    public View findViewInHeaderArea(int id) {
        return mHeaderArea.findViewById(id);
    }

    public View findViewInFooterArea(int id) {
        return mFooterArea.findViewById(id);
    }

    public void addHeaderView(View v) {
        checkArgumentNotNull(v);
        mHeaderArea.addView(v, LayoutParams.MATCH_PARENT , LayoutParams.WRAP_CONTENT);
        if (getAdapter() != null) {
            getAdapter().notifyItemChanged(0);
        }
    }

    public void removeHeaderView(View v) {
        checkArgumentNotNull(v);
        mHeaderArea.removeView(v);
        if (getAdapter() != null) {
            getAdapter().notifyItemChanged(0);
        }
    }

    public void addFooterView(View v) {
        checkArgumentNotNull(v);
        mFooterArea.addView(v, LayoutParams.MATCH_PARENT , LayoutParams.WRAP_CONTENT);
        if (getAdapter() != null) {
            int footerPos = getAdapter().getItemCount() - 1;
            getAdapter().notifyItemChanged(footerPos);
        }
    }

    public void removeFooterView(View v) {
        checkArgumentNotNull(v);
        mFooterArea.removeView(v);
        if (getAdapter() != null) {
            int footerPos = getAdapter().getItemCount() - 1;
            getAdapter().notifyItemChanged(footerPos);
        }
    }

    public int getFooterHeight() {
        return mFooterArea.getHeight();
    }

    public int getHeaderHeight() {
        return mHeaderArea.getHeight();
    }

    @Override
    public KGRecyclerView.Adapter getAdapter() {
        return (Adapter) super.getAdapter();
    }

    public void setAdapter(KGRecyclerView.Adapter adapter) {
        adapter.setHasHeaderFooter(hasHeaderFooter);
        super.setAdapter(adapter);
    }

    /**
     * The adapter args must A type of {@link KGRecyclerView.Adapter},
     * or it would throw an Exception. Use {@link #setAdapter(KGRecyclerView.Adapter)}
     * instead.
     */
    @Override
    @Deprecated
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (adapter instanceof KGRecyclerView.Adapter) {
            setAdapter((KGRecyclerView.Adapter) adapter);
            return;
        }
        throw new IllegalArgumentException(KGRecyclerView.class.getCanonicalName()
                + " must use A adapter which is " + KGRecyclerView.Adapter.class.getCanonicalName());
    }

    /**
     * Interface definition for a callback to be invoked when an item in this
     * KGRecyclerView has been clicked.
     */
    public interface OnItemClickListener {

        /**
         * Callback method to be invoked when an item in this KGRecyclerView has
         * been clicked.
         * @param position the position ignores the headerArea
         * @param id the id associates the <code>position</code>
         */
        void onItemClick(KGRecyclerView parent, View view, int position, long id);
    }

    /**
     * Register a callback to be invoked when an item in this KGRecyclerView has
     * been clicked.
     *
     * @param listener The callback that will be invoked.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    /**
     * @return The callback to be invoked with an item in this KGRecyclerView has
     *         been clicked, or null id no callback has been set.
     */
    public final OnItemClickListener getOnItemClickListener() {
        return mOnItemClickListener;
    }

    /**
     * Interface definition for a callback to be invoked when an item in this
     * view has been clicked and held.
     */
    public interface OnItemLongClickListener {
        /**
         * Callback method to be invoked when an item in this view has been
         * clicked and held.
         *
         * @return true if the callback consumed the long click, false otherwise
         */
        boolean onItemLongClick(KGRecyclerView parent, View view, int position, long id);
    }


    /**
     * Register a callback to be invoked when an item in this KGRecyclerView has
     * been clicked and held
     *
     * @param listener The callback that will run
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        if (!isLongClickable()) {
            setLongClickable(true);
        }
        mOnItemLongClickListener = listener;
    }

    /**
     * @return The callback to be invoked with an item in this KGRecyclerView has
     *         been clicked and held, or null id no callback as been set.
     */
    public final OnItemLongClickListener getOnItemLongClickListener() {
        return mOnItemLongClickListener;
    }

    /**
     * This BaseViewHolder must just use for KGRecyclerView, or it will throw an exception
     * while its itemview being clicked. In the other way, the Adapter of A KGRecyclerView
     * must use this BaseViewHolder, otherwise the listeners about click action which is set by
     * {@link KGRecyclerView#setOnClickListener(OnClickListener)} and
     * {@link KGRecyclerView#setOnLongClickListener(OnLongClickListener)}
     * would not be invoked.
     */
    public abstract static class ViewHolder<D> extends RecyclerView.ViewHolder {

        private OnClickListener mItemClick = new OnClickListener() {
            @Override
            public void onClick(View v) {
                KGRecyclerView recyclerViewParent = checkIfIsKGRecyclerView((View) v.getParent());
                if (recyclerViewParent == null)
                    return;
                Adapter adapter = recyclerViewParent.getAdapter();
                if (recyclerViewParent.mOnItemClickListener != null && adapter != null) {
                    int posIgnoreHeaderArea = getPosition() - recyclerViewParent.headerAreaCount();
                    recyclerViewParent.mOnItemClickListener.onItemClick(recyclerViewParent, v,
                            posIgnoreHeaderArea, adapter.getItemId(posIgnoreHeaderArea));
                }
            }
        };

        private OnLongClickListener mItemLongClick = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                KGRecyclerView recyclerViewParent = checkIfIsKGRecyclerView((View) v.getParent());
                if (recyclerViewParent == null)
                    return false;
                Adapter adapter = recyclerViewParent.getAdapter();
                if (recyclerViewParent.mOnItemLongClickListener != null && adapter != null) {
                    int posIgnoreHeaderArea = getPosition() - recyclerViewParent.headerAreaCount();
                    return recyclerViewParent.mOnItemLongClickListener.onItemLongClick(
                            recyclerViewParent, v, posIgnoreHeaderArea, adapter.getItemId(posIgnoreHeaderArea));
                }
                return false;
            }
        };

        private KGRecyclerView checkIfIsKGRecyclerView(View parent) {
            if (parent == null)
                return null;
            if (! (parent instanceof KGRecyclerView))
                throw new IllegalStateException("Make sure the clicking itemView's " +
                        "parent is A kind of " + KGRecyclerView.class.getSimpleName());
            return (KGRecyclerView) parent;
        }

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(mItemClick);
            itemView.setOnLongClickListener(mItemLongClick);
        }

        public void refresh(D data, int position) {

        }

        public void refresh(D data, int position, Object obj) {

        }

    }

    public abstract static class Adapter extends RecyclerView.Adapter<KGRecyclerView.ViewHolder> {
        public static final int ITEM_TYPE_HEADERAREA = -100;
        public static final int ITEM_TYPE_FOOTERAREA = -101;
        private KGRecyclerView mView;

        //当使用GridLayoutManager时，会由于添加headerFooter导致空白区域出现，由此引入hasHeaderFooter
        private boolean hasHeaderFooter = true;

        public void setHasHeaderFooter(boolean hasHeaderFooter) {
            this.hasHeaderFooter = hasHeaderFooter;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            mView = (KGRecyclerView) recyclerView;
            Log.i(TAG, "Adapter.onAttachedToRecyclerView");
        }

        @Override
        public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
            mView = null;
            Log.i(TAG, "Adapter.onDetachedFromRecyclerView");
        }

        @Override
        public final KGRecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
            if (type == ITEM_TYPE_HEADERAREA)
                return new ExtraAreaHolder(mView.mHeaderArea);
            if (type == ITEM_TYPE_FOOTERAREA)
                return new ExtraAreaHolder(mView.mFooterArea);
            return onMakeViewHolder(viewGroup, type);
        }

        public abstract KGRecyclerView.ViewHolder onMakeViewHolder(ViewGroup viewGroup, int type);

        @Override
        public final void onBindViewHolder(KGRecyclerView.ViewHolder kvh, int position) {
            if (hasHeaderFooter) {
                if (position <= 0 || position == getItemCount() - 1)
                    return;
            } else {
                if (position < 0 || position > getItemCount() - 1)
                    return;
            }
            int posIgnoreHeaderArea = position - mView.headerAreaCount();
            onBoundViewHolder(kvh, posIgnoreHeaderArea);
        }

        public abstract void onBoundViewHolder(KGRecyclerView.ViewHolder kvh, int position);

        @Override
        public final int getItemViewType(int position) {
            if (hasHeaderFooter) {
                if (position == 0)
                    return ITEM_TYPE_HEADERAREA;
                if (position > 0 && position == getItemCount() - 1)
                    return ITEM_TYPE_FOOTERAREA;
            }
            int posIgnoreHeaderArea = position - mView.headerAreaCount();
            return getItemType(posIgnoreHeaderArea);
        }

        public int getItemType(int position) {
            return 0;
        }

        @Override
        public final int getItemCount() {
            if (mView == null) {
                return getCount();
            } else {
                int extraAreaCount = mView.getExtraViewCount();
                return getCount() + extraAreaCount;
            }
        }

        public abstract int getCount();

        /**
         * Notify any registered observers that the item at <code>position</code> has changed.
         * @param position position Position of the item that has changed
         * @param fromHeaderArea true if the <code>position</code> has taken the HeaderArea
         *                        item into consideration, false else.
         */
        public void notifyItemChanged(int position, boolean fromHeaderArea) {
            if (mView == null)
                return;
            int pos = fromHeaderArea ? position : position + mView.headerAreaCount();
            try {
                super.notifyItemChanged(pos);
            } catch (IllegalStateException e) {
                String msg = "RecyclerView is computing a layout or scrolling";
                if (e.getMessage() != null && e.getMessage().contains(msg)) {
                    Log.e("KGRecyclerView", "Warning:" + e.getMessage());
                } else {
                    throw e;
                }
            }
        }
    }

    public static class ExtraAreaHolder extends KGRecyclerView.ViewHolder {
        public ExtraAreaHolder(View itemView) {
            super(itemView);
            itemView.setClickable(false);
            itemView.setLongClickable(false);
        }
    }

}
