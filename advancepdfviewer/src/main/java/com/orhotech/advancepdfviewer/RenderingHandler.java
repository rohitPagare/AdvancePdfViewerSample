package com.orhotech.advancepdfviewer;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.SparseBooleanArray;

import com.orhotech.advancepdfviewer.exception.PageRenderingException;
import com.orhotech.advancepdfviewer.model.PagePart;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;


public class RenderingHandler extends Handler {
    /**
     * {@link Message#what} kind of message this handler processes.
     */
    static final int MSG_RENDER_TASK = 1;

    private static final String TAG = RenderingHandler.class.getName();

    private final PdfiumCore pdfiumCore;
    private final PdfDocument pdfDocument;

    private final PDFView pdfView;

    private final RectF renderBounds = new RectF();
    private final Rect roundedRenderBounds = new Rect();
    private final Matrix renderMatrix = new Matrix();
    private final SparseBooleanArray openedPages = new SparseBooleanArray();
    private boolean running = false;

    RenderingHandler(Looper looper, PDFView pdfView, PdfiumCore pdfiumCore, PdfDocument pdfDocument) {
        super(looper);
        this.pdfView = pdfView;
        this.pdfiumCore = pdfiumCore;
        this.pdfDocument = pdfDocument;
    }

    void addRenderingTask(int userPage, int page, float width, float height, RectF bounds, boolean thumbnail, int cacheOrder, boolean bestQuality, boolean annotationRendering) {
        RenderingTask task = new RenderingTask(width, height, bounds, userPage, page, thumbnail, cacheOrder, bestQuality, annotationRendering);
        Message msg = obtainMessage(MSG_RENDER_TASK, task);
        sendMessage(msg);
    }

    @Override
    public void handleMessage(Message message) {
        RenderingTask task = (RenderingTask) message.obj;
        try {
            final PagePart part = proceed(task);
            if (part != null) {
                if (running) {
                    pdfView.post(() -> pdfView.onBitmapRendered(part));
                } else {
                    part.getRenderedBitmap().recycle();
                }
            }
        } catch (final PageRenderingException ex) {
            pdfView.post(() -> pdfView.onPageError(ex));
        }
    }

    private PagePart proceed(RenderingTask renderingTask) throws PageRenderingException {
        if (openedPages.indexOfKey(renderingTask.page) < 0) {
            try {
                pdfiumCore.openPage(pdfDocument, renderingTask.page);
                openedPages.put(renderingTask.page, true);
            } catch (Exception e) {
                openedPages.put(renderingTask.page, false);
                throw new PageRenderingException(renderingTask.page, e);
            }
        }

        int w = Math.round(renderingTask.width);
        int h = Math.round(renderingTask.height);
        Bitmap render;
        try {
            render = Bitmap.createBitmap(w, h, renderingTask.bestQuality ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
        calculateBounds(w, h, renderingTask.bounds);
        if (openedPages.get(renderingTask.page)) {

            pdfiumCore.renderPageBitmap(pdfDocument, render, renderingTask.page,
                    roundedRenderBounds.left, roundedRenderBounds.top,
                    roundedRenderBounds.width(), roundedRenderBounds.height(), renderingTask.annotationRendering);
        } else {
            render.eraseColor(pdfView.getInvalidPageColor());
        }
        return new PagePart(renderingTask.userPage, renderingTask.page, render,
                renderingTask.width, renderingTask.height,
                renderingTask.bounds, renderingTask.thumbnail,
                renderingTask.cacheOrder);
    }

    private void calculateBounds(int width, int height, RectF pageSliceBounds) {
        renderMatrix.reset();
        renderMatrix.postTranslate(-pageSliceBounds.left * width, -pageSliceBounds.top * height);
        renderMatrix.postScale(1 / pageSliceBounds.width(), 1 / pageSliceBounds.height());

        renderBounds.set(0, 0, width, height);
        renderMatrix.mapRect(renderBounds);
        renderBounds.round(roundedRenderBounds);
    }

    void stop() {
        running = false;
    }

    void start() {
        running = true;
    }

    private static class RenderingTask {

        float width, height;

        RectF bounds;

        int page;

        int userPage;

        boolean thumbnail;

        int cacheOrder;

        boolean bestQuality;

        boolean annotationRendering;

        RenderingTask(float width, float height, RectF bounds, int userPage, int page, boolean thumbnail, int cacheOrder, boolean bestQuality, boolean annotationRendering) {
            this.page = page;
            this.width = width;
            this.height = height;
            this.bounds = bounds;
            this.userPage = userPage;
            this.thumbnail = thumbnail;
            this.cacheOrder = cacheOrder;
            this.bestQuality = bestQuality;
            this.annotationRendering = annotationRendering;
        }
    }
}
