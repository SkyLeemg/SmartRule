package com.vitec.task.smartrule.bean;

import com.vitec.task.smartrule.view.IconImageView;

public class IconViewBean {

    private IconImageView imageView;
    private float totalScale;
    private ViewLayout layout;

    public IconViewBean(IconImageView imageView, float totalScale) {
        this.imageView = imageView;
        this.totalScale = totalScale;
    }

    public IconImageView getImageView() {
        return imageView;
    }

    public void setImageView(IconImageView imageView) {
        this.imageView = imageView;
    }

    public float getTotalScale() {
        return totalScale;
    }

    public void setTotalScale(float totalScale) {
        this.totalScale = totalScale;
    }

    public ViewLayout getLayout() {
        return layout;
    }

    public void setLayout(ViewLayout layout) {
        this.layout = layout;
    }
}
