package com.orhotech.advancepdfviewer.listener;

import android.view.MotionEvent;

public interface OnTapListener {
    /**
     * Called when the user has a tap gesture, before processing scroll handle toggling
     *
     * @param e MotionEvent that registered as a confirmed single tap
     * @return true if the single tap was handled, false to toggle scroll handle
     */
    boolean onTap(MotionEvent e);
}
