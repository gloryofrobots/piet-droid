package com.example.piet_droid;

import java.io.FileOutputStream;
import com.example.jpiet.Piet;
import com.example.piet_droid.widget.ColorFieldView;

public class PietFileSaver {
    PietFile mPietFile;
    Piet mPiet;
    ColorFieldView mView;

    public interface SaveListener {
        public void onComplete();
        public void onError(int errorStringId);
    }

    public PietFileSaver(PietFile pietFile) {
        mPietFile = pietFile;
        mPiet = mPietFile.getPiet();
        mView = mPietFile.getView();
    }

    public boolean save(FileOutputStream stream) {
        BitmapWriter writer = new BitmapWriter(mPiet.getModel());

        if (writer.write(stream) == true) {
            mPietFile.untouch();
            return true;
        }

        return false;
    }

    public void saveAsync(String path, SaveListener listener) {
        doSaveAsync(path, listener);
    }

    public void saveAsync(SaveListener listener) {
        if (mPietFile.hasPath() == false) {
            // TODO THROW!!!!!
        }

        String path = mPietFile.getPath();
        saveAsync(path, listener);
    }

    AsyncTaskWriteBitmap mSaveTask;

    private void doSaveAsync(String path, final SaveListener listener) {
        final String filePath = path;

        mSaveTask = new AsyncTaskWriteBitmap(mPiet,
                new AsyncTaskWriteBitmap.SaveProcessListener() {
                    @Override
                    public void onSaveBitmapError() {
                        listener.onError(R.string.runtime_save_bitmap_error);
                        mSaveTask = null;
                    }

                    @Override
                    public void onSaveBitmapComplete() {
                        mPietFile.setPath(filePath);
                        mPietFile.untouch();
                        listener.onComplete();
                        mSaveTask = null;
                    }

                    @Override
                    public void onSaveBitmapCancel() {
                        mSaveTask = null;
                    }
                }, mPietFile.getActivity());
        
        mSaveTask.execute(path);
    }

    public void finalise() {
        mPietFile = null;
        if (mSaveTask != null) {
            mSaveTask.cancel(true);
            mSaveTask = null;
        }
    }
}
