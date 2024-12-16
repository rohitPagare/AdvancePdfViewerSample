package com.orhotech.advancepdfviewersample;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.orhotech.advancepdfviewer.PDFView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private PDFView pdfView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        pdfView = findViewById(R.id.pdfView);

        // Copy PDF from raw resource to internal storage and load it
        File file = copyPdfToFile();
        if (file != null) {
            loadPdfFromFile(file);
        }
    }

    private void loadPdfFromFile(File file) {
        if (file != null) {
            pdfView.fromFile(file)  // Use fromFile to load PDF from internal storage
                    .enableSwipe(true)
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .onError(t -> Toast.makeText(this, "Error loading PDF", Toast.LENGTH_SHORT).show())
                    .onPageError((page, t) -> Toast.makeText(this, "Error on page " + page, Toast.LENGTH_SHORT).show())
                    .load(); // Load the PDF into the PDFView
        } else {
            Toast.makeText(this, "Error: Failed to copy PDF", Toast.LENGTH_SHORT).show();
        }
    }

    private File copyPdfToFile() {
        try {
            // Create file in internal storage
            File outputFile = new File(getFilesDir(), "sample.pdf");

            // Get InputStream from raw resource
            InputStream inputStream = getResources().openRawResource(R.raw.sample);

            // Create OutputStream to write the file
            OutputStream outputStream = new FileOutputStream(outputFile);

            // Copy from InputStream to OutputStream
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            // Close streams
            inputStream.close();
            outputStream.close();

            return outputFile; // Return the file that was written to internal storage
        } catch (IOException e) {
            Log.e("TAG", "copyPdfToFile: "+e);
            return null; // Return null if error occurred
        }
    }
}