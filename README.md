# Android Infinite Banner View
An Infinite scroll left or right banner view without RecyclerView &amp; ViewPager and adapt for AndroidX or not.

# How

Because we want to display ad view in banner scroll style,and it must be matched several points below:

- can be loop-scroll infinity

- banner item can be dragged to left or right

- banner item can be removed

- banner item was dynamic inflated and may not be local resource

- Not depend on AndroidX or RecyclerView or ViewPager

# Search And Compare

With requirement,I searched Google and github for a long time,i found out that most open-source was combined with RecyclerView or ViewPager.

RecyclerView depend on AndroidX,or it would display in a wired way,especially when you got origin data and deleted one item,

then reset your data,then invoked notifyDataSetChanged,it would display one item in white and could not be dragged from one 

to another.

For ViewPager,some open-source would set a large number of items to implement infinity scroll,when this way met monkey test ,it would produce anr.

# How to do

Inspired from circle List,we can define three views,which named **previous** and **middle** and **next**.

Middle view is the one to be displayed,previous view is the one to be prepared when auto play is set,

next view is the one which generate from middle view when scroll from left to right.

if origin index is:previous    **middle**    next

scroll from left to right:next    **previous**    middle

in this way,next turned to be previous,and middle turned to be next,and previous turned to be middle to display in screen.

scroll from right to left:middle    **next**    previous

in this way,middle turned to be previous,and previous turned to be next,and next turned to be middle to display in screen.

Yes,we make three position to be fixed and can be filled in a circle way.On scrolling,ignore scroll left or right,

only generating three index right can we display banner item in a right way.


# How to use

## include dependency

implementation project(path: ':infinitebannerview')

## define view and indicator view

```xml
<com.chuck.infinitebannerview.InfiniteBannerScrollView
    android:id="@+id/banner_scroll_view"
    android:layout_width="match_parent"
    android:layout_height="200dp"
    android:layout_marginTop="8dp"
    android:clipToPadding="false" />

<com.chuck.infinitebannerview.BannerScrollPagerIndicatorView
    android:id="@+id/indicator"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="8dp"
    android:gravity="center" />
```

## get banner view and set data

you should set banner width first then set data

```java
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
```

In demo module,you can find how to delete one item.





