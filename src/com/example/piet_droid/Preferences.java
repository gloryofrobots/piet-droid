package com.example.piet_droid;

import java.util.List;
import android.os.Build;
import android.os.Bundle;
import com.actionbarsherlock.app.SherlockPreferenceActivity;

public class Preferences extends SherlockPreferenceActivity {
    private boolean needResource = false;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (needResource
          || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
        addPreferencesFromResource(R.xml.preferences);
      }
    }

    @Override
    public void onBuildHeaders(List<Header> target) {
      if (onIsHidingHeaders() || !onIsMultiPane()) {
        needResource=true;
      }
      else {
        loadHeadersFromResource(R.xml.preference_headers, target);
      }
    }
  }
