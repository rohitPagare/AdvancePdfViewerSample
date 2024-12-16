package com.orhotech.advancepdfviewer.listener;

public interface OnPageScrollListener {
    /**
     * Called on every move while scrolling
     *
     * @param page current page index
     * @param positionOffset see {}
     */
    void onPageScrolled(int page, float positionOffset);
}