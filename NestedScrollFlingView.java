package com.kugou.shiqutouch.widget.customnestedscrollview;

import android.content.Context;
import android.hardware.SensorManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;
import com.kugou.common.utils.KGLog;
import com.kugou.common.utils.SystemUtils;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

public class NestedScrollFlingView extends NestedScrollView {

    private String TAG = "PersonHomeNestedScrollViewParent";
    private int maxScrollHeight = SystemUtils.dip2px(getContext(), 200);
    private OverScroller curScroller;

    public NestedScrollFlingView(Context context) {
        super(context);
        initScroller();
    }

    public NestedScrollFlingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initScroller();
    }

    public NestedScrollFlingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initScroller();
    }

    private void initScroller() {
        try {
            Field field_mScroller = getClass().getSuperclass().getDeclaredField("mScroller");
            field_mScroller.setAccessible(true);
            curScroller = (OverScroller) field_mScroller.get(this);
            if (KGLog.DEBUG) KGLog.d(TAG, "initScroller() mScroller: " + curScroller);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getMaxScrollHeight() {
        return maxScrollHeight;
    }

    public void setMaxScrollHeight(int maxScrollHeight) {
        this.maxScrollHeight = maxScrollHeight;
    }

//    @Override
//    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
//        int curScrollY = getScrollY();
//        if (KGLog.DEBUG) KGLog.d(TAG, "onNestedScrollAccepted() curScrollY: " + curScrollY);
//        //自己作为父view，onStartNestedScroll()=true时，接下来调此方法,对嵌套滑动做一些前期工作
//        super.onNestedScrollAccepted(child, target, nestedScrollAxes);
//    }
//
//    @Override
//    public boolean startNestedScroll(int axes) {
//        //自己滑动时调用
//        int curScrollY = getScrollY();
//        if (KGLog.DEBUG) KGLog.d(TAG, "startNestedScroll() curScrollY: " + curScrollY);
//        return super.startNestedScroll(axes);
//    }
//
//    @Override
//    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
//        //自己作为父view，是否需要滑动
//        int curScrollY = getScrollY();
//        if (KGLog.DEBUG) KGLog.d(TAG, "onStartNestedScroll() curScrollY: " + curScrollY);
//        return super.onStartNestedScroll(child, target, nestedScrollAxes);
//    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        checkChildView(target);
        //子view滑动时调用
        int curScrollY = getScrollY();
        int lengthY = 0; //还要上滑多少才能到max, 或还要下滑多少才能到0
        int needScrollY = 0; //作为父布局需要处理的Y

        if (curScrollY >= 0 && curScrollY < maxScrollHeight) {
            if (dy > 0) {
                //作为父布局，需要上滑
                lengthY = maxScrollHeight - curScrollY;
                needScrollY = dy <= lengthY ? dy : lengthY;
                consumed[1] = needScrollY;
            } else if (dy < 0) {
                //作为父布局需要下滑
                lengthY = curScrollY; //这是个正数
                needScrollY = Math.abs(dy) <= curScrollY ? dy : lengthY * -1; //这是个负数
                consumed[1] = needScrollY;
            }
        }

        KGLog.d(TAG, "onNestedPreScroll() "
                + "dy: " + dy + ", curScrollY: " + curScrollY + ", maxScrollHeight: " + maxScrollHeight
                + ", lengthY: " + lengthY + ", needScrollY: " + needScrollY);
        if (needScrollY != 0) {
            scrollBy(0, needScrollY);
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        checkChildView(target);
        //子view处理完滑动后的滑动距离，作为父布局有"dyUnconsumed"可以处理，处理完剩下的距离是dyUnconsumed
        int curScrollY = getScrollY();
        int needScrollY = 0; //作为父布局需要处理的Y
        int lengthY = 0; //还要上滑多少才能到max, 或还要下滑多少才能到0

        //scrollview处于滑满状态时，如果子view还有剩余dyUnconsumed需要下滑，需要父布局下滑处理（上滑不需要）
        if (curScrollY >= maxScrollHeight && dyUnconsumed < 0) {
            needScrollY = dyUnconsumed;
        }

        KGLog.d(TAG, "onNestedScroll() "
                + "dyConsumed: " + dyConsumed + ", dyUnconsumed: " + dyUnconsumed + ", curScrollY: " + curScrollY + ", maxScrollHeight: " + maxScrollHeight
                + ", lengthY: " + lengthY + ", needScrollY: " + needScrollY
                + ", dyUnconsumed: " + dyUnconsumed);
        if (needScrollY != 0) {
            smoothScrollBy(0, needScrollY);
        }

    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        checkChildView(target);
        int curScrollY = getScrollY();
        boolean needFlingY = false;
        if (velocityY > 0 && curScrollY < maxScrollHeight) {
            //子view向上fling,作为父布局需要上滑,且最多滑到maxScrollHeight
            needFlingY = true;
            fling((int) velocityY);
        } else if (velocityY < 0) {
            if (curScrollY >= maxScrollHeight) {
                //作为父布局需要下滑，此时要判断子view是否不能继续下滑
                if (target instanceof RecyclerView) {
                    boolean childScrollTop = !((RecyclerView) target).canScrollVertically(-1); //是否下滑到顶部了
                    KGLog.d(TAG, "onNestedPreFling() childScrollTop: " + childScrollTop);
                    if (childScrollTop) {
                        //如果子view下滑到顶了
                        needFlingY = true;
                        fling((int) velocityY);
                    }
                }
            } else if (curScrollY > 0) {
                needFlingY = true;
                fling((int) velocityY);
            }
        }
        KGLog.d(TAG, "onNestedPreFling() velocityY: " + velocityY
                + ", curScrollY: " + curScrollY + ", maxScrollHeight: " + maxScrollHeight + ", needFlingY: " + needFlingY);
        return needFlingY;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        checkChildView(target);
        int curScrollY = getScrollY();
        KGLog.d(TAG, "onNestedFling() velocityY: " + velocityY
                + ", consumed: " + consumed + ", curScrollY: " + curScrollY + ", maxScrollHeight: " + maxScrollHeight);
        return false;
    }

    @Override
    public void computeScroll() {
        int velocity = (int) getScrollerCurrVelocityReal(curScroller); //当前剩余的速度
        int curScrollY = getScrollY();

        boolean needChildFling = false;
        if (curScrollY >= maxScrollHeight && velocity > 0) {
            //如果滚动的目标位置超过maxScrollHeight，并且继续上滑，就给子view处理fling
            needChildFling = true;
        }
        KGLog.d(TAG, "computeScroll() velocityY: " + velocity + ", curScrollY: " + curScrollY + ", needChildFling: " + needChildFling);
        if (needChildFling && curChildRecyclerView != null) {
            KGLog.d(TAG, "computeScroll() give to child: curChildRecyclerView: " + curChildRecyclerView);
            curChildRecyclerView.fling(0, velocity);
            fling(0); //清除自己剩余的fling
        } else {
            super.computeScroll();
        }
    }


    //是否使用嵌套滚动模式(这是一种比较low的方法，别介意，好用就行，子view限制了RecyclerView)
    private boolean useNestedFlingMode;

    public void setUseNestedFlingMode(boolean useNestedFlingMode) {
        this.useNestedFlingMode = useNestedFlingMode;
    }

    private RecyclerView curChildRecyclerView; //这里这样用有点死，但是先用着吧
    private int curChildRecyclerViewState = -1;
    private HashMap<RecyclerView, RecyclerView.OnScrollListener> viewScrollListenerMap;

    private void checkChildView(View view) {
        if (!useNestedFlingMode)
            return;
        if (view instanceof RecyclerView && !view.equals(curChildRecyclerView)) {
            KGLog.d(TAG, "checkChildView() curChildRecyclerView: " + curChildRecyclerView + ", view: " + view);
            curChildRecyclerView = (RecyclerView) view;
            RecyclerView.OnScrollListener onScrollListener =
                    viewScrollListenerMap != null ? viewScrollListenerMap.get(curChildRecyclerView) : null;
            if (onScrollListener == null) {
                onScrollListener = new RecyclerView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                        if (newState != curChildRecyclerViewState) {
                            curChildRecyclerViewState = newState;
                            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                                final OverScroller childScoller = getChildViewScroller();
                                float childViewCurrVelocity = getScrollerCurrVelocityReal(childScoller);
                                boolean childScrollTop = !curChildRecyclerView.canScrollVertically(-1); //是否下滑到顶部了
                                KGLog.d(TAG, "RecyclerView.onScrollStateChanged() 停止滚动了 childScrollTop: " + childScrollTop
                                        + ", childViewCurrVelocity: " + childViewCurrVelocity);
                                if (childScrollTop && childViewCurrVelocity != 0) {
                                    //如果下滚，子view到顶部了，还有速度，就给父view去scroll
                                    setScrollerCurrVelocity(childScoller, 0); //清除子view的剩余速度
                                    startParentFling((int) childViewCurrVelocity);
                                }
                            }
                        }
                    }
                };
                if (viewScrollListenerMap == null)
                    viewScrollListenerMap = new HashMap<>();
                viewScrollListenerMap.put(curChildRecyclerView, onScrollListener);
            }
            curChildRecyclerView.addOnScrollListener(onScrollListener);
        }
    }

    private void startParentFling(int flingY) {
        try {
            if (curScroller != null) {
                Class class_SplineOverScroller = Class.forName("android.widget.OverScroller$SplineOverScroller");
                Constructor constructor = class_SplineOverScroller.getDeclaredConstructor(Context.class);
                constructor.setAccessible(true);
                Object object_SplineOverScroller = constructor.newInstance(getContext());

                Method method_getSplineDeceleration = class_SplineOverScroller.getDeclaredMethod("getSplineFlingDistance", int.class);
                method_getSplineDeceleration.setAccessible(true);
                double distance = (double) method_getSplineDeceleration.invoke(object_SplineOverScroller, flingY);
                if (Math.abs(getAbsVelocityByDistance(distance) - Math.abs(flingY)) > 10)
                    throw new Exception("getAbsVelocityByDistance() 算错了");

                int curScrollY = getScrollY();
                int targetScrollY = distance >= curScrollY ? 0 : (int) (curScrollY - distance);
                //这里有两种方案，一种是计算出滑动距离对应的fling速度，直接fling
                int realFling = (int) (getAbsVelocityByDistance(Math.abs(targetScrollY - curScrollY)) * (flingY > 0 ? 1 : -1));
                fling(realFling);
                //另一种是smoothScrollTo，但前提mLastScrollerY要设置一下，如果mLastScrollerY是0，那么computeScroll不会执行smoothScrollTo
                //smoothScrollTo(0, targetScrollY);
                KGLog.d(TAG, "startParentFling() flingY: " + flingY + ", distance: " + distance
                        + ", curScrollY: " + curScrollY + ", targetScrollY: " + targetScrollY + ", realFling: " + realFling);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private OverScroller getChildViewScroller() {
        if (curChildRecyclerView != null) {
            try {
                Field field_mViewFlinger = curChildRecyclerView.getClass().getDeclaredField("mViewFlinger");
                field_mViewFlinger.setAccessible(true);
                Class class_ClassViewFlinger = Class.forName("android.support.v7.widget.RecyclerView$ViewFlinger");
                field_mViewFlinger.get(curChildRecyclerView);
                Field field_mScroller = class_ClassViewFlinger.getDeclaredField("mScroller");
                field_mScroller.setAccessible(true);
                OverScroller mScroller = (OverScroller) field_mScroller.get(field_mViewFlinger.get(curChildRecyclerView));
                return mScroller;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private float getScrollerCurrVelocityReal(OverScroller mScroller) {
        try {
            if (mScroller != null) {
                Field field_mScrollerY = mScroller.getClass().getDeclaredField("mScrollerY");
                field_mScrollerY.setAccessible(true);
                Class class_SplineOverScroller = Class.forName("android.widget.OverScroller$SplineOverScroller");
                Field field_mCurrVelocity = class_SplineOverScroller.getDeclaredField("mCurrVelocity");
                field_mCurrVelocity.setAccessible(true);
                float mCurrVelocity = (float) field_mCurrVelocity.get(field_mScrollerY.get(mScroller));
                return (int) mCurrVelocity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void setScrollerCurrVelocity(final OverScroller mScroller, final float value) {
        try {
            if (mScroller != null) {
                Field field_mScrollerY = mScroller.getClass().getDeclaredField("mScrollerY");
                field_mScrollerY.setAccessible(true);
                Class class_SplineOverScroller = Class.forName("android.widget.OverScroller$SplineOverScroller");
                Field field_mCurrVelocity = class_SplineOverScroller.getDeclaredField("mCurrVelocity");
                field_mCurrVelocity.setAccessible(true);
                field_mCurrVelocity.set(field_mScrollerY.get(mScroller), value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final float INFLEXION = 0.35f;
    private final float mFlingFriction = ViewConfiguration.getScrollFriction();
    private final float ppi = getContext().getResources().getDisplayMetrics().density * 160.0f;
    private final float mPhysicalCoeff = SensorManager.GRAVITY_EARTH // g (m/s^2)
            * 39.37f // inch/meter
            * ppi
            * 0.84f;
    private static float DECELERATION_RATE = (float) (Math.log(0.78) / Math.log(0.9));

    /**
     * 这里返回的是绝对值
     * @param distance
     * @return
     */
    public double getAbsVelocityByDistance(double distance) {
        final double decelMinusOne = DECELERATION_RATE - 1.0;
        final double l = Math.log(distance / (mFlingFriction * mPhysicalCoeff)) / (DECELERATION_RATE / decelMinusOne);
        double velocity = Math.exp(l) * (mFlingFriction * mPhysicalCoeff) / INFLEXION;
        return velocity;
    }

}
