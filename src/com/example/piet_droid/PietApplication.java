package com.example.piet_droid;

import java.util.HashMap;

import android.app.Application;
import org.acra.*;
import org.acra.annotation.*;

@ReportsCrashes(
        formKey = "", // This is required for backward compatibility but not used
        //httpMethod = org.acra.sender.HttpSender.Method.PUT,
        //reportType = org.acra.sender.HttpSender.Type.JSON,
        //formUri = "http://gloryofrobots.iriscouch.com/acra-pietdroid/_design/acra-storage/_update/report",
        //formUriBasicAuthLogin = "pietdroidreport",
        //formUriBasicAuthPassword = "f13badb239e0418cafdaecf63dcfd006130ed74db1402364b9aff9f1053f41ec373f21cc7291fc8eb2f4090fa91d3e5b",
        mode = ReportingInteractionMode.TOAST,
        forceCloseDialogAfterToast = false, // optional, default false
        resToastText = R.string.crash_toast_text
    )
public class PietApplication extends Application {
   
    @Override
    public void onCreate() {
        super.onCreate();
        
        ACRA.init(this);
        HashMap<String,String> ACRAData = new HashMap<String,String>();
        ACRAData.put("ApplicationInternalId", "PietDroid");
        ACRA.getErrorReporter().setReportSender(new ACRAPostSender(ACRAData));
    }
}
