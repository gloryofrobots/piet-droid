package com.example.piet_droid;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.util.Log;

import com.example.jpiet.CodelTableModel;

public class BitmapWriter {
    private static final String LOG_TAG = "BITMAP_WRITER";
    private CodelTableModel mModel;
    
    public BitmapWriter(CodelTableModel model) {
        mModel = model;
    }
    
    private Bitmap.CompressFormat getBitmapDecodeFormat() {
        Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
        /*if(mFormatTag.equals("PNG")) {
            format = Bitmap.CompressFormat.PNG;
        } else if (mFormatTag.equals("JPEG")) {
            format = Bitmap.CompressFormat.JPEG;
        }*/
        
        return format;
    }
    
    public boolean write(String fileName) {
        boolean complete = true;
        FileOutputStream fileOStream = null;
        File file = new File(fileName);
        try {
            fileOStream = new FileOutputStream(file);
            complete = write(fileOStream);
            if(complete == false) {
                Log.e(LOG_TAG , "Error writing " + fileName);
            }
            fileOStream.close();

        } catch (IOException e) {
            complete = false;
        } 
        
        return complete;
    }
    
    public boolean write(FileOutputStream fileOStream) {
        boolean complete = true;
        int width = mModel.getWidth();
        int height = mModel.getHeight();

        int size = width * height;
        int[] colors = new int[size];

        Bitmap bitmap = null;

        try {
            mModel.fillArray(colors);
            bitmap = Bitmap.createBitmap(colors, width, height,
                    Bitmap.Config.ARGB_8888);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            
            Bitmap.CompressFormat format = getBitmapDecodeFormat();
            bitmap.compress(format, 100, bytes);

            fileOStream.write(bytes.toByteArray());
        } catch (IndexOutOfBoundsException e) {
            Log.e(LOG_TAG, e.toString() );
            complete = false;
        } catch (IOException e) {
            Log.e(LOG_TAG ,  e.toString());
            complete = false;
        } finally {
            if (bitmap != null && bitmap.isRecycled() == false) {
                bitmap.recycle();
            }
        }
        
        return complete;
    }
}
