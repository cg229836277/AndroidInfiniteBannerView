package com.chuck.infinitebannerview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BannerScrollPagerIndicatorView extends LinearLayout {
    private final String TAG = "BannerScrollPagerIndicatorView";
    private Context context;
    private ViewGroup parentView;
    private List<View> indicatorItems = new ArrayList<>();

    private String defaultSelectedColorStr = "#FF0095BF";
    private String defaultUnSelectedColorStr = "#55333333";

    private int defaultSelectedColor = Color.parseColor(defaultSelectedColorStr);
    private int defaultUnSelectedColor = Color.parseColor(defaultUnSelectedColorStr);
    private int defaultIndicatorSize = 16;
    private int defaultMarginSize = 6;
    private Resources resources;

    public BannerScrollPagerIndicatorView(Context context) {
        this(context, null);
    }

    public BannerScrollPagerIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        setOrientation(LinearLayout.HORIZONTAL);
        this.context = context;
        resources = context.getResources();
        parentView = (ViewGroup) LayoutInflater.from(context).inflate(R.layout.activity_ad_banner_scroll_indicator_view, this);
    }

    public void setSelectedColor(int color) {
        this.defaultSelectedColor = resources.getColor(color);
    }

    public void setUnselectedColor(int color) {
        this.defaultUnSelectedColor = resources.getColor(color);
    }

    public void setIndicatorSize(int size) {
        this.defaultIndicatorSize = size;
    }


    public void setIndicators(int totalSize, int selectedIndex) {
        if (parentView != null) {
            parentView.removeAllViews();
        }
        if (indicatorItems != null) {
            indicatorItems.clear();
        }
        LayoutParams layoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        layoutParams.width = defaultIndicatorSize;
        layoutParams.height = defaultIndicatorSize;
        layoutParams.leftMargin = defaultMarginSize;
        layoutParams.rightMargin = defaultMarginSize;
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        for (int index = 0; index < totalSize; index++) {
            TextView indicatorView = (TextView) layoutInflater.inflate(R.layout.activity_ad_banner_scroll_indicator_item, null);
            indicatorView.setLayoutParams(layoutParams);
            GradientDrawable shape = new GradientDrawable();
            shape.setShape(GradientDrawable.OVAL);
            if (selectedIndex == index) {
                shape.setColor(defaultSelectedColor);
            } else {
                shape.setColor(defaultUnSelectedColor);
            }
            indicatorView.setBackground(shape);
            parentView.addView(indicatorView);
            indicatorItems.add(indicatorView);
        }
    }

    public void updateSelectIndex(int selectedIndex) {
        int totalSize = indicatorItems.size();
        for (int index = 0; index < totalSize; index++) {
            View indicatorView = indicatorItems.get(index);
            GradientDrawable shape = (GradientDrawable) indicatorView.getBackground();
            if (selectedIndex == index) {
                shape.setColor(defaultSelectedColor);
            } else {
                shape.setColor(defaultUnSelectedColor);
            }
            indicatorView.setBackground(shape);
        }
    }
}
