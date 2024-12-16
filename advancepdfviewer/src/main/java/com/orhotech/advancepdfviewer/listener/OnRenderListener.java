package com.orhotech.advancepdfviewer.listener;

public interface OnRenderListener {
    /**
     * Called only once, when document is rendered
     * @param nbPages number of pages
     * @param pageWidth width of page
     * @param pageHeight height of page
     */
    void onInitiallyRendered(int nbPages, float pageWidth, float pageHeight);
}
