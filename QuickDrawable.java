package com.kugou.common.utils;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.view.View;
import com.kugou.common.app.KGCommonApplication;
import com.kugou.common.skinpro.manager.SkinResourcesUtils;
import java.util.HashMap;
import java.util.Map;
import static android.graphics.drawable.GradientDrawable.RECTANGLE;

/**
 *
 * To reduce XML files.
 * Support GradientDrawable, SelectStateDrawable
 *
 *
 * examples:
 *
 * QuickDrawable.create().cornerDp(10).bgColorId(R.color.colorButtonSecondBlue).intoBackground(findViewById(R.id.button_id));
 *
 * //水波纹效果
 * Drawable rippleDrawable = QuickDrawable.createRippleDrawable();
 *
 * button.setBackground(QuickDrawable.create().corner(10)
 *      //.bgColor(Color.parseColor("#FF69B4"))
 *      .bgColor(GradientDrawable.Orientation.LEFT_RIGHT, new int[] {Color.RED, Color.YELLOW})
 *      .build());
 *
 * button.setBackground(QuickDrawable.create()
 *      .stateDrawable(android.R.attr.state_pressed, QuickDrawable.create().bgColor(Color.parseColor("#FF69B4")).build())
 *      .stateDrawable(android.R.attr.drawable, QuickDrawable.create().bgColor(Color.parseColor("#00BFFF")).build())
 *      .build());
 *
 * QuickDrawable.create()
 *      .stateDrawable(-android.R.attr.state_pressed, QuickDrawable.create().bgColor(Color.parseColor("#00BFFF")).build())
 *      .stateDrawable(android.R.attr.state_pressed, QuickDrawable.create().bgColor(Color.parseColor("#FF69B4")).build())
 *      .intoBackground(button);
 *
 */
public class QuickDrawable {

    //默认矩形
    private int shape = RECTANGLE;

    //背景颜色
    private int backgroundColor = Color.WHITE;
    //背景颜色渐变
    private @ColorInt
    int[] backgroundColors;
    private GradientDrawable.Orientation backgroundColorsOrientation;

    //圆角半径
    private float defaultRadius = 0;
    private float topLeftRadius = defaultRadius;
    private float topRightRadius = defaultRadius;
    private float bottomLeftRadius = defaultRadius;
    private float bottomRightRadius = defaultRadius;

    //边框线宽度,颜色
    private int borderWidth = 0;
    private int borderColor = Color.GRAY;
    //边框线长度,间隔距离(用于虚线)
    private float dashWidth = 0;
    private float dashGap = 0;

    //存放stateSet
    Map<Integer, Drawable> selectorDrawableMap;

    public static QuickDrawable create() {
        return QuickDrawable.create();
    }

    private Drawable createDrawable() {
        GradientDrawable drawable = new GradientDrawable();
        if (backgroundColorsOrientation != null && backgroundColors != null) {
            drawable = new GradientDrawable(backgroundColorsOrientation, backgroundColors);
        } else {
            drawable.setColor(backgroundColor);
        }
        drawable.setShape(shape);
        drawable.setStroke(borderWidth, borderColor, dashWidth, dashGap);
        drawable.setCornerRadii(new float[]{
                topLeftRadius, topLeftRadius,
                topRightRadius, topRightRadius,
                bottomRightRadius, bottomRightRadius,
                bottomLeftRadius, bottomLeftRadius});
        return drawable;
    }

    public Drawable build() {
        if (selectorDrawableMap != null && selectorDrawableMap.size() > 0) {
            StateListDrawable selectorDrawable = new StateListDrawable();
            for (Map.Entry<Integer, Drawable> entry : selectorDrawableMap.entrySet()) {
                selectorDrawable.addState(new int[]{entry.getKey()}, entry.getValue());
            }
            return selectorDrawable;
        } else {
            return createDrawable();
        }
    }

    public void intoBackground(View view) {
        view.setBackgroundDrawable(build());
    }


    public QuickDrawable bgColor(int color) {
        this.backgroundColor = color;
        return this;
    }

    public QuickDrawable bgColorId(int colorid) {
        int color = KGCommonApplication.getContext().getResources().getColor(colorid);
        this.backgroundColor = color;
        return this;
    }

    public QuickDrawable bgColor(GradientDrawable.Orientation orientation, @ColorInt int[] colors) {
        this.backgroundColorsOrientation = orientation;
        this.backgroundColors = colors;
        return this;
    }

    public QuickDrawable corner(float radius) {
        corner(radius, radius, radius, radius);
        return this;
    }

    public QuickDrawable cornerDp(int radiusDp) {
        int radius = SystemUtils.dip2px(KGCommonApplication.getContext(), radiusDp);
        corner(radius, radius, radius, radius);
        return this;
    }

    public QuickDrawable corner(float topLeft, float topRight, float bottomLeft, float bottomRight) {
        this.topLeftRadius = topLeft;
        this.topRightRadius = topRight;
        this.bottomLeftRadius = bottomLeft;
        this.bottomRightRadius = bottomRight;
        return this;
    }

    public QuickDrawable border(int width) {
        this.borderWidth = width;
        return this;
    }

    public QuickDrawable borderColor(int color) {
        if (borderWidth == 0 && dashWidth == 0) {
            borderWidth = 1;
        }
        this.borderColor = color;
        return this;
    }

    public QuickDrawable dashWidth(int width) {
        this.dashWidth = width;
        return this;
    }

    public QuickDrawable dashGap(int gap) {
        this.dashGap = gap;
        return this;
    }

    /**
     * 设置selector-state的效果
     *
     * @param state    如android:state_pressed,android:state_selected等
     * @param drawable 对应状态的drawable
     */
    @SuppressLint("UseSparseArrays")
    public QuickDrawable stateDrawable(int state, Drawable drawable) {
        if (selectorDrawableMap == null)
            selectorDrawableMap = new HashMap<>();
        selectorDrawableMap.put(state, drawable);
        return this;
    }

    /**
     * 添加水波纹效果
     * 这里不能使用intoBackground()
     */
    public static Drawable createRippleDrawable() {
        Drawable content = QuickDrawable.create()
                .stateDrawable(android.R.attr.drawable, QuickDrawable.create().bgColorId(R.color.skin_list_selected).build())
                .stateDrawable(android.R.attr.state_pressed, QuickDrawable.create().bgColorId(R.color.skin_list_selected).build())
                .stateDrawable(android.R.attr.state_selected, QuickDrawable.create().bgColorId(R.color.skin_list_selected).build())
                .stateDrawable(android.R.attr.state_focused, QuickDrawable.create().bgColorId(R.color.skin_list_selected).build())
                .build();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int rippleColor = Color.parseColor("#11222222");//getAlphaColor(getColor("skin_list_selected", R.color.skin_list_selected), 0.2f);
            return new RippleDrawable(ColorStateList.valueOf(rippleColor), content, null);
        }
        return content;
    }
}
