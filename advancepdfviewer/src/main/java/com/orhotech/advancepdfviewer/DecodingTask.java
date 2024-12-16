package com.orhotech.advancepdfviewer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.orhotech.advancepdfviewer.source.DocumentSource;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

public class DecodingTask {

    private boolean cancelled;

    private final PDFView pdfView;
    private final WeakReference<Context> contextRef;
    private final PdfiumCore pdfiumCore;
    private PdfDocument pdfDocument;
    private final String password;
    private final DocumentSource docSource;
    private final int firstPageIdx;
    private int pageWidth;
    private int pageHeight;

    // Executor to handle background task
    private final ExecutorService executorService;

    public DecodingTask(DocumentSource docSource, String password, PDFView pdfView, PdfiumCore pdfiumCore, int firstPageIdx) {
        this.docSource = docSource;
        this.firstPageIdx = firstPageIdx;
        this.cancelled = false;
        this.pdfView = pdfView;
        this.password = password;
        this.pdfiumCore = pdfiumCore;
        this.contextRef = new WeakReference<>(pdfView.getContext());
        this.executorService = Executors.newSingleThreadExecutor(); // Using executor service for background task
    }

    public void execute() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {
                final Throwable result = doInBackground();
                // Posting result to main thread
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        onPostExecute(result);
                    }
                });
            }
        });
    }

    @Nullable
    private Throwable doInBackground() {
        try {
            Context context = contextRef.get();
            if (context == null) {
                return new IllegalStateException("Context is no longer available");
            }

            pdfDocument = docSource.createDocument(context, pdfiumCore, password);
            // We assume all the pages are the same size
            pdfiumCore.openPage(pdfDocument, firstPageIdx);
            pageWidth = pdfiumCore.getPageWidth(pdfDocument, firstPageIdx);
            pageHeight = pdfiumCore.getPageHeight(pdfDocument, firstPageIdx);
            return null;
        } catch (Throwable t) {
            return t;
        }
    }

    private void onPostExecute(Throwable t) {
        if (t != null) {
            pdfView.loadError(t);
            return;
        }
        if (!cancelled) {
            pdfView.loadComplete(pdfDocument, pageWidth, pageHeight);
        }
    }

    public void cancel() {
        cancelled = true;
        executorService.shutdownNow(); // Stop the task if needed
    }

}
