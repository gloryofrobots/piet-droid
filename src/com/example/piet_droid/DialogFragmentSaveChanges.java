package com.example.piet_droid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class DialogFragmentSaveChanges extends SherlockDialogFragment {
    public interface OnAcceptListener {
        public void onAccept();
    }
    
    private OnAcceptListener mOnAcceptListener;
    
    public void setOnAcceptListener(OnAcceptListener listener) {
        mOnAcceptListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_title_save_changes)
            .setPositiveButton(R.string.button_text_ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                       if(mOnAcceptListener == null) {
                           return;
                       }
                       mOnAcceptListener.onAccept();
                       dismiss();
                   }
               })
               .setNegativeButton(R.string.button_text_cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       DialogFragmentSaveChanges.this.getDialog().cancel();
                   }
               });      
        return builder.create();
    }
}

