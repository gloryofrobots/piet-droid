package com.example.piet_droid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class DialogFragmentNewFileSettings extends SherlockDialogFragment {
    public interface Listener {
        public boolean onAcceptNewFileSettings(int width, int height);
        public void onCancelNewFileSettings();
        
    }
    
    private int mDefaultWidth = 0;
    private int mDefaultHeight = 0;
    private Listener mListener;
    
    public void setListener(Listener listener) {
        mListener = listener;
    }
    
    public void setBitmapDimensions(int width, int height) {
        mDefaultWidth = width;
        mDefaultHeight = height;
    }
    
    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*// Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (DialogNewFileSettingsListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }*/
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View dialogView = inflater.inflate(R.layout.dialog_new_file, null);
        
        final EditText widthEdit = (EditText) dialogView.findViewById(R.id.edittext_new_file_width);
        final EditText heightEdit = (EditText) dialogView.findViewById(R.id.edittext_new_file_height);
        
        widthEdit.setText(String.valueOf(mDefaultWidth));
        heightEdit.setText(String.valueOf(mDefaultHeight));
        
        builder.setView(dialogView)
               .setTitle(R.string.dialog_title_file_settings)
               //Add action buttons
               .setPositiveButton(R.string.button_text_ok, new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int id) {
                       if(mListener == null) {
                           return;
                       }
                       
                       String widthRepr = widthEdit.getText().toString();
                       String heightRepr = heightEdit.getText().toString();
                       
                       if(widthRepr.length() == 0 || heightRepr.length() == 0) {
                           Toast.makeText(getActivity(),
                                   "Please enter valid width and height", Toast.LENGTH_SHORT).show();
                           
                           return;
                       }
                       
                       int width = Integer.valueOf(widthRepr);
                       int height = Integer.valueOf(heightRepr);
                       
                       if(width == 0 || height == 0) {
                           Toast.makeText(getActivity(),
                                   "Please enter non zero width and height", Toast.LENGTH_SHORT).show();
                           
                           return;
                       }
                       
                       mListener.onAcceptNewFileSettings(width, height);
                       dismiss();
                   }
               })
               .setNegativeButton(R.string.button_text_cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       DialogFragmentNewFileSettings.this.getDialog().cancel();
                       
                       if(mListener == null) {
                           return;
                       }
                       
                       mListener.onCancelNewFileSettings();
                   }
               });      
        return builder.create();
    }
   

}
