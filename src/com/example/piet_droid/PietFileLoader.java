package com.example.piet_droid;

import java.io.File;
import java.io.FileInputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;

import com.example.jpiet.CodelTableModel;
import com.example.jpiet.Piet;
import com.example.piet_droid.widget.ColorFieldView;

public class PietFileLoader {
    public interface LoadListener {
        public void onComplete();

        public void onError(int errorStringId);
    };

    PietFile mPietFile;
    Piet mPiet;
    ColorFieldView mView;

    public PietFileLoader(PietFile pietFile) {
        mPietFile = pietFile;
        mPiet = mPietFile.getPiet();
        mView = mPietFile.getView();
    }

    AsyncTaskLoadBitmap mLoadTask;
    
    private boolean prepareToLoad(Bitmap bitmap, LoadListener listener) {
        // TODO CODEL SIZE HERE
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        long amountOfMemory = mView.getAmountOfMemory(width, height);
        // if amountOfMemory == -1, view can`t determine memory size we just try
        // to load program.
        // if it would fall it would fall
        if (amountOfMemory != -1) {
            long freeMemory = MemoryUtils.getFreeMemory();
            if (freeMemory < (amountOfMemory + 1024)) {
                listener.onError(R.string.runtime_large_bitmap_error);
                return false;
            }
        }
        
        mPiet.setNewModel(width, height);

        mView.setVisibility(View.INVISIBLE);
        mView.resize(width, height);
        return true;
    }

    private BitmapFactory.Options createBitmapOptions() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return options;
    }

    public void loadAsync(String path, LoadListener listener) {
        BitmapFactory.Options options = createBitmapOptions();
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        if (bitmap == null) {
            listener.onError(R.string.runtime_decoding_error);
            return;
        }

        loadAsync(bitmap, path, listener);
    }

    public void loadAsync(FileInputStream stream, LoadListener listener) {
        BitmapFactory.Options options = createBitmapOptions();
        Bitmap bitmap = BitmapFactory.decodeStream(stream, null, options);

        if (bitmap == null) {
            listener.onError(R.string.runtime_decoding_error);
            return;
        }

        loadAsync(bitmap, null, listener);
    }

    private void loadAsync(Bitmap bitmap, final String filePath,
            final LoadListener listener) {
        if (prepareToLoad(bitmap, listener) == false) {
            return;
        }
        final PietFileActor actor = mPietFile.getActor();
        mLoadTask = new AsyncTaskLoadBitmap(
                new AsyncTaskLoadBitmap.LoadProcessListener() {
                    @Override
                    public void onLoadBitmapCancel() {
                        mLoadTask = null;
                    }

                    public void onLoadBitmapPixel(int x, int y, int color) {
                        actor.setCell(x, y, color);
                    }

                    @Override
                    public void onLoadBitmapComplete() {
                        mView.setVisibility(View.VISIBLE);
                        actor.invalidateView();
                        mPietFile.setPath(filePath);
                        mPietFile.untouch();
                        mLoadTask = null;
                       
                        listener.onComplete();
                    }

                    @Override
                    public void onLoadBitmapError() {
                        mView.setVisibility(View.VISIBLE);
                        mPietFile.getActor().invalidateView();
                        listener.onError(R.string.runtime_load_bitmap_error);
                    }
                }, mPietFile.getActivity());
        mLoadTask.execute(bitmap);
    }

    public void finalise() {
        mPietFile = null;

        if (mLoadTask != null) {
            mLoadTask.cancel(true);
            mLoadTask = null;
        }
    }
}
