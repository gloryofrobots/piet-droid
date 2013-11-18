package com.example.piet_droid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class HelpActivity extends Activity {
    private WebView mWebView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        mWebView = (WebView) findViewById(R.id.webviewHelp);
        //webview.loadData(readTextFromResource(R.a.help), "text/html", "utf-8");
        mWebView.loadUrl("file:///android_asset/help/index.html");
    }
    
    public void onBackPressed (){
        if (mWebView.isFocused() && mWebView.canGoBack()) {
            mWebView.goBack();       
        }
        else {
                super.onBackPressed();
                finish();
        }
    }
    
    private String readTextFromResource(int resourceID) {
        InputStream raw = getResources().openRawResource(resourceID);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int i;
        try {
            i = raw.read();
            while (i != -1) {
                stream.write(i);
                i = raw.read();
            }
            raw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream.toString();
    }
}
