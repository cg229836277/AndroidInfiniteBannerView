package com.chuck.infinitebannerview;

import android.os.Build;
import android.view.View;

public class ViewStyleSetter {

    private View mView;

    public ViewStyleSetter(View view) {
        this.mView = view;
    }

    /**
     * 为View设置圆角效果
     *
     * @param radius 圆角半径
     */
    public void setRoundCorner(float radius) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        this.mView.setClipToOutline(true);// 用outline裁剪内容区域
        this.mView.setOutlineProvider(new RoundViewOutlineProvider(radius));
    }

    /**
     * 设置View为圆形
     */
    public void setOval() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        this.mView.setClipToOutline(true);// 用outline裁剪内容区域
        this.mView.setOutlineProvider(new OvalViewOutlineProvider());
    }

    /**
     * 清除View的圆角效果
     */
    public void clearShapeStyle() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        this.mView.setClipToOutline(false);
    }
}
