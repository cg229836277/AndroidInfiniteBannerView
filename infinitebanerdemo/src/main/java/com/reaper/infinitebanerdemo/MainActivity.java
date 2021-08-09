package com.reaper.infinitebanerdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.chuck.infinitebannerview.BannerScrollPagerIndicatorView;
import com.chuck.infinitebannerview.InfiniteBannerChangedListener;
import com.chuck.infinitebannerview.InfiniteBannerScrollView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private InfiniteBannerScrollView sliderLayout;
    private BannerScrollPagerIndicatorView bannerScrollPagerIndicatorView;

    private final int[] resource = new int[]{
            R.drawable.banner_1,
            R.drawable.banner_2,
            R.drawable.banner_3,
            R.drawable.banner_4,
            R.drawable.banner_5};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loadStaticButton = findViewById(R.id.load_static_image);
        loadStaticButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<View> viewList = new ArrayList<>();
                for (int imageRes : resource) {
                    viewList.add(getDemoViews(imageRes));
                }
                sliderLayout.setData(viewList);
                bannerScrollPagerIndicatorView.setIndicators(viewList.size(), 0);
            }
        });

        Button deleteOneStaticButton = findViewById(R.id.delete_one_static_image);
        deleteOneStaticButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<View> viewList = sliderLayout.getData();
                if (viewList == null || viewList.size() == 1) {
                    return;
                }
                Random random = new Random();
                int index = random.nextInt(viewList.size());
                viewList.remove(index);
                sliderLayout.setData(viewList);
                bannerScrollPagerIndicatorView.setIndicators(viewList.size(), 0);
            }
        });

        Button loadInflatedView = findViewById(R.id.load_inflated_view);
        loadInflatedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<View> viewList = new ArrayList<>();
                for (int imageRes : resource) {
                    View childView = getDemoViews(imageRes);
                    ViewGroup parentView = (ViewGroup) MainActivity.this.getLayoutInflater().inflate(R.layout.activity_main_banner_item, null);
                    parentView.addView(childView);
                    viewList.add(parentView);
                }
                sliderLayout.setData(viewList);
                bannerScrollPagerIndicatorView.setIndicators(viewList.size(), 0);
            }
        });

        initBannerView();
    }

    private View getDemoViews(int imageRes) {
        ImageView imageView = new ImageView(this);
        imageView.setImageResource(imageRes);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return imageView;
    }

    private void initBannerView() {
        List<View> viewList = new ArrayList<>();
        for (int imageRes : resource) {
            viewList.add(getDemoViews(imageRes));
        }
        sliderLayout = findViewById(R.id.banner_scroll_view);
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        sliderLayout.setBannerWidth(width);
        sliderLayout.setData(viewList);

        bannerScrollPagerIndicatorView = findViewById(R.id.indicator);
        bannerScrollPagerIndicatorView.setIndicators(viewList.size(), 0);

        sliderLayout.setInfiniteBannerChangedListener(new InfiniteBannerChangedListener() {
            @Override
            public void pageSelected(View view, int selectedPage) {
                Log.i(TAG, "selectedPage: " + selectedPage);
                bannerScrollPagerIndicatorView.updateSelectIndex(selectedPage);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sliderLayout.replay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sliderLayout.stopPlay();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sliderLayout.release();
    }
}