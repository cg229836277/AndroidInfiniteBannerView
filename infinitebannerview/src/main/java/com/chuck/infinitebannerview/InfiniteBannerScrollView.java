package com.chuck.infinitebannerview;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

public class InfiniteBannerScrollView extends LinearLayout {
    final String TAG = "InfiniteBannerScrollView";
    private List<View> viewList;
    private int currentIndex = 0;
    private int screenWidth;
    private float lastX = 0;
    private float moveX = 0;
    private int dataSize = 0;
    private final int SIZE_FIX_TWO = 2;
    private final int SIZE_FIX_ONE = 1;
    private InfiniteBannerChangedListener infiniteBannerChangedListener;

    private PlayHandler playHandler;
    private long playIntervalTime = 4000;
    private boolean autoPlay = true;

    private final int MESSAGE_PLAY = 0;
    private boolean touchNow = false;

    public InfiniteBannerScrollView(Context context) {
        this(context, null);
    }

    public InfiniteBannerScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void setInfiniteBannerChangedListener(InfiniteBannerChangedListener infiniteBannerChangedListener) {
        this.infiniteBannerChangedListener = infiniteBannerChangedListener;
    }

    public void setRoundCorner(int radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewStyleSetter viewStyleSetter = new ViewStyleSetter(this);
            viewStyleSetter.setRoundCorner(radius);
        }
    }

    private void initView(Context context) {
        playHandler = new PlayHandler();
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (dataSize <= 1) {
                    return false;
                }
//                Log.d(TAG, "onTouch");
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        moveX = event.getX();
                        float moveDelta = moveX - lastX;
//                        Log.d(TAG, "onTouch ACTION_MOVE moveX:" + moveX + ",moveDelta:" + moveDelta);
                        if (moveDelta < 0) {
                            moveLeft(moveDelta);
                        } else if (moveDelta > 0) {
                            moveRight(moveDelta);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
//                        Log.d(TAG, "onTouch ACTION_UP");
                        touchNow = false;
//                        startPlayDelay();
                        startPlayAtTime();
                        float currentX = event.getX();
                        float delta = currentX - lastX;
                        if (delta > LIMIT_MOVE_DISTANCE) {
//                            Log.d(TAG, "scroll right:" + delta);
                            scrollRight();
                        } else if (delta < -LIMIT_MOVE_DISTANCE) {
//                            Log.d(TAG, "scroll left:" + delta);
                            scrollLeft();
                        } else {
//                            Log.d(TAG, "scroll clicked");
                            return false;
                        }
                        break;
                }
                return true;
            }
        });
    }

    private final float LIMIT_MOVE_DISTANCE = 60F;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        boolean intercepted = false;
        if (dataSize <= 1) {
            return false;
        }
        int x = (int) event.getX();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                intercepted = false;
                touchNow = true;
//                Log.d(TAG, "onInterceptTouchEvent ACTION_DOWN");
                lastX = x;
//                Log.d(TAG, "onInterceptTouchEvent lastX:" + lastX);
                removePlay();
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float distanceDelta = Math.abs(lastX - x);
                intercepted = distanceDelta >= LIMIT_MOVE_DISTANCE;
//                Log.d(TAG, "onInterceptTouchEvent ACTION_MOVE:" + intercepted);
                break;
            }
            case MotionEvent.ACTION_UP: {
                touchNow = false;
                float distanceDelta = Math.abs(lastX - x);
                intercepted = distanceDelta >= LIMIT_MOVE_DISTANCE;
//                Log.d(TAG, "onInterceptTouchEvent distanceDelta:" + distanceDelta);
                break;
            }
        }
        return intercepted;
    }

    public void setLayoutParams(ViewGroup.LayoutParams layoutParams) {
        super.setLayoutParams(layoutParams);
        screenWidth = layoutParams.width;
        Log.d(TAG, "screenWidth:" + screenWidth);
    }

    public void setBannerWidth(int widthInPx) {
        screenWidth = widthInPx;
    }

    public void setData(List<View> viewList) {
        currentIndex = 0;
        this.viewList = viewList;
        dataSize = viewList.size();
        setItemView();
        if (autoPlay && dataSize > 1) {
            startPlayDelay();
        } else {
            removePlay();
        }
    }

    public List<View> getData() {
        return this.viewList;
    }

    public void release() {
        if (playHandler != null) {
            playHandler.removeCallbacksAndMessages(null);
            playHandler = null;
        }
        if (this.viewList != null) {
            this.viewList.clear();
            this.viewList = null;
        }
    }

    private final int TYPE_PREVIOUS = -1;
    private final int TYPE_MIDDLE = 0;
    private final int TYPE_NEXT = 1;

    private LayoutParams getItemViewLayoutParams(int type) {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.width = screenWidth;
        switch (type) {
            case TYPE_PREVIOUS:
                layoutParams.leftMargin = -screenWidth;
                break;
            case TYPE_MIDDLE:
//                layoutParams.leftMargin = 0;
                break;
            case TYPE_NEXT:
//                layoutParams.leftMargin = screenWidth;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }
        return layoutParams;
    }

    private void setItemView() {
        removeAllViews();
        int size = viewList.size();
        for (int index = 0; index < size; index++) {
            View itemView = viewList.get(index);
            ViewGroup parentView = (ViewGroup) itemView.getParent();
            if (parentView != null) {
                parentView.removeView(itemView);
            }
        }
        if (dataSize == SIZE_FIX_ONE) {
            addViewWithOneItem();
            return;
        }
        int[] itemIndexs = getPreviousAndNextIndex();
        if (dataSize == SIZE_FIX_TWO) {
            generateCurrentAndOtherItems(itemIndexs);
            return;
        }
        generatePreviousAndNextItems(itemIndexs);
    }

    private void addViewWithOneItem() {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.width = screenWidth;
        layoutParams.leftMargin = 0;

        removeFromParentView(previousView);
        removeFromParentView(nextView);
        removeFromParentView(middleView);
        middleView = viewList.get(currentIndex);
        addView(middleView, layoutParams);
    }

    private int[] getPreviousAndNextIndex() {
        int size = viewList.size();
        int nextIndex = currentIndex + 1;
        if (currentIndex == size - 1) {
            nextIndex = 0;
        }

        int previousIndex = currentIndex - 1;
        if (currentIndex == 0) {
            previousIndex = size - 1;
        }
        return new int[]{previousIndex, nextIndex};
    }

    private View previousView, middleView, nextView;

    private void removeFromParentView(View itemView) {
        ViewGroup parentView = (ViewGroup) itemView.getParent();
        if (parentView != null) {
            parentView.removeView(itemView);
        }
    }

    private void generateCurrentAndOtherItems(int[] indexs) {
        int previousIndex = indexs[0], nextIndex = indexs[1];
        Log.d(TAG, "generateCurrentAndOtherItems previousIndex:" + previousIndex + ",nextIndex:" + nextIndex);
        if (previousIndex != nextIndex) {
            Log.d(TAG, "generateCurrentAndOtherItems illegal");
            return;
        }
        previousView = viewList.get(previousIndex);
        middleView = viewList.get(currentIndex);
        nextView = previousView;

        previousView.setLayoutParams(getItemViewLayoutParams(TYPE_PREVIOUS));
        middleView.setLayoutParams(getItemViewLayoutParams(TYPE_MIDDLE));

        removeFromParentView(previousView);
        removeFromParentView(nextView);
        removeFromParentView(middleView);

        addView(previousView);
        addView(middleView);
    }

    private void generatePreviousAndNextItems(int[] indexs) {
        int previousIndex = indexs[0], nextIndex = indexs[1];
        Log.d(TAG, "generatePreviousAndNextItems previousIndex:" + previousIndex + ",nextIndex:" + nextIndex);
        previousView = viewList.get(previousIndex);
        middleView = viewList.get(currentIndex);
        nextView = viewList.get(nextIndex);

        previousView.setLayoutParams(getItemViewLayoutParams(TYPE_PREVIOUS));
        middleView.setLayoutParams(getItemViewLayoutParams(TYPE_MIDDLE));
        nextView.setLayoutParams(getItemViewLayoutParams(TYPE_NEXT));

        removeFromParentView(previousView);
        removeFromParentView(middleView);
        removeFromParentView(nextView);

        addView(previousView);
        addView(middleView);
        addView(nextView);
    }

    private void moveLeft(float delta) {
        LayoutParams layoutParams2 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams2.leftMargin = (int) delta;
        layoutParams2.width = screenWidth;
        middleView.setLayoutParams(layoutParams2);
        if (dataSize == SIZE_FIX_TWO) {
            if (nextView.getParent() == null) {
                Log.d(TAG, "moveLeft addView");
                addView(nextView);
            }
        }

        LayoutParams layoutParams1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams1.leftMargin = (int) (screenWidth - Math.abs(delta));
        layoutParams1.width = screenWidth;
        nextView.setLayoutParams(layoutParams1);
    }

    private void moveRight(float delta) {
        LayoutParams layoutParams2 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams2.leftMargin = (int) delta;
        layoutParams2.width = screenWidth;
        middleView.setLayoutParams(layoutParams2);
        if (dataSize == SIZE_FIX_TWO) {
            if (previousView.getParent() == null) {
                Log.d(TAG, "moveRight addView");
                addView(previousView, 0);
            }
        }

        LayoutParams layoutParams1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams1.leftMargin = (int) (-screenWidth + moveX);
        layoutParams1.width = screenWidth;
        previousView.setLayoutParams(layoutParams1);
    }

    private synchronized void scrollLeft() {
        LayoutParams layoutParams2 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams2.leftMargin = -screenWidth;
        layoutParams2.width = screenWidth;
        middleView.setLayoutParams(layoutParams2);

        LayoutParams layoutParams1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams1.leftMargin = 0;
        layoutParams1.width = screenWidth;
        nextView.setLayoutParams(layoutParams1);

        if (dataSize == SIZE_FIX_TWO) {
            Log.d(TAG, "scrollLeft removeFromParentView");
            removeFromParentView(middleView);
            View tempView = middleView;
            middleView = nextView;
            previousView = tempView;
            nextView = tempView;
        } else {
            removeFromParentView(previousView);
            previousView = null;
            previousView = middleView;
            middleView = nextView;
        }

        if (currentIndex == dataSize - 1) {
            currentIndex = 0;
        } else {
            ++currentIndex;
        }
        if (infiniteBannerChangedListener != null) {
            infiniteBannerChangedListener.pageSelected(middleView, currentIndex);
        }
        if (dataSize == SIZE_FIX_TWO) {
            return;
        }

        int[] newIndexs = getPreviousAndNextIndex();
//        Log.d(TAG, "scrollLeft currentIndex:" + currentIndex + ",previousIndex:" + newIndexs[0] + ",nextIndex:" + newIndexs[1]);
        nextView = viewList.get(newIndexs[1]);
        nextView.setLayoutParams(getItemViewLayoutParams(TYPE_NEXT));
        removeFromParentView(nextView);
        addView(nextView);
    }

    private synchronized void scrollRight() {
        LayoutParams layoutParams2 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams2.leftMargin = screenWidth;
        layoutParams2.width = screenWidth;
        middleView.setLayoutParams(layoutParams2);

        LayoutParams layoutParams1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams1.leftMargin = 0;
        layoutParams1.width = screenWidth;
        previousView.setLayoutParams(layoutParams1);
        if (dataSize == SIZE_FIX_TWO) {
//            removeView(middleView);
            Log.d(TAG, "scrollRight removeFromParentView");
            removeFromParentView(middleView);
            View tempView = middleView;
            middleView = previousView;
            previousView = tempView;
            nextView = tempView;
        } else {
            removeFromParentView(nextView);
            nextView = null;
            nextView = middleView;
            middleView = previousView;
        }

        if (currentIndex == 0) {
            currentIndex = dataSize - 1;
        } else {
            --currentIndex;
        }
        if (infiniteBannerChangedListener != null) {
            infiniteBannerChangedListener.pageSelected(middleView, currentIndex);
        }
        if (dataSize == SIZE_FIX_TWO) {
            return;
        }

        int[] newIndexs = getPreviousAndNextIndex();
//        Log.d(TAG, "scrollLeft currentIndex:" + currentIndex + ",previousIndex:" + newIndexs[0] + ",nextIndex:" + newIndexs[1]);
        previousView = viewList.get(newIndexs[0]);
        ViewGroup.LayoutParams layoutParams = getItemViewLayoutParams(TYPE_PREVIOUS);
        previousView.setLayoutParams(layoutParams);
        removeFromParentView(previousView);
        addView(previousView, 0);
        if (infiniteBannerChangedListener != null) {
            infiniteBannerChangedListener.pageSelected(middleView, currentIndex);
        }
    }

    public void setAutoPlay(boolean autoPlay) {
        this.autoPlay = autoPlay;
    }

    public void startPlayDelay() {
        if (dataSize <= 1 || !autoPlay) {
            removePlay();
            return;
        }
        playHandler.removeMessages(MESSAGE_PLAY);
        playHandler.sendEmptyMessageDelayed(MESSAGE_PLAY, playIntervalTime);
    }

    public void startPlayAtTime() {
        if (dataSize <= 1 || !autoPlay) {
            removePlay();
            return;
        }
        removePlay();
        playHandler.sendEmptyMessageAtTime(MESSAGE_PLAY, SystemClock.uptimeMillis() + playIntervalTime);
    }

    public void stopPlay() {
        removePlay();
    }

    public void replay() {
        if (autoPlay && dataSize > 1) {
            startPlayDelay();
        }
    }

    private void removePlay() {
        playHandler.removeCallbacksAndMessages(null);
    }

    private class PlayHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if (what == MESSAGE_PLAY) {
                removeMessages(MESSAGE_PLAY);
                if (!touchNow) {
                    if (dataSize == SIZE_FIX_TWO && nextView.getParent() == null) {
//                        Log.d(TAG, "handleMessage moveLeft addView");
                        addView(nextView);
                    }
//                    Log.d(TAG, "handleMessage scrollLeft");
                    scrollLeft();
                    sendEmptyMessageDelayed(MESSAGE_PLAY, playIntervalTime);
                }
            }
        }
    }

}
