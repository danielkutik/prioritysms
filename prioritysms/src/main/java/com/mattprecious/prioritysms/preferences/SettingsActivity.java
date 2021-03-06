/*
 * Copyright 2013 Matthew Precious
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mattprecious.prioritysms.preferences;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.mattprecious.prioritysms.R;
import com.mattprecious.prioritysms.util.Helpers;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Scanner;

public class SettingsActivity extends SherlockPreferenceActivity
    implements SharedPreferences.OnSharedPreferenceChangeListener {
  private static final String TAG = SettingsActivity.class.getSimpleName();
  private static final String ENCODING = "UTF-8";

  public static final String PREFS_GENERAL =
      "com.mattprecious.prioritysms.preferences.PREFS_GENERAL";
  public static final String PREFS_ALARM = "com.mattprecious.prioritysms.preferences.PREFS_ALARM";
  public static final String PREFS_ADVANCED =
      "com.mattprecious.prioritysms.preferences.PREFS_ADVANCED";
  public static final String PREFS_ABOUT = "com.mattprecious.prioritysms.preferences.PREFS_ABOUT";

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    PreferenceManager.getDefaultSharedPreferences(this)
        .registerOnSharedPreferenceChangeListener(this);

    String action = getIntent().getAction();
    if (PREFS_GENERAL.equals(action)) {
      addPreferencesFromResource(R.xml.general_preferences);
    } else if (PREFS_ALARM.equals(action)) {
      addPreferencesFromResource(R.xml.alarm_preferences);

      ListPreference timeoutPreference =
          (ListPreference) findPreference(getString(R.string.pref_key_alarm_timeout));
      updateTimeoutSummary(timeoutPreference, timeoutPreference.getValue());
      timeoutPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override public boolean onPreferenceChange(Preference preference, Object newValue) {
              updateTimeoutSummary(preference, (String) newValue);
              return true;
            }
          }
      );
    } else if (PREFS_ADVANCED.equals(action)) {
      addPreferencesFromResource(R.xml.advanced_preferences);
    } else if (PREFS_ABOUT.equals(action)) {
      addPreferencesFromResource(R.xml.about_preferences);
      findPreference(getString(R.string.pref_key_about_version)).setSummary(getAppVersion(this));
      findPreference(getString(R.string.pref_key_about_change_log)).setOnPreferenceClickListener(
          new Preference.OnPreferenceClickListener() {

            @Override public boolean onPreferenceClick(Preference preference) {
              buildChangeLogDialog(SettingsActivity.this).show();
              return false;
            }
          });
      findPreference(getString(R.string.pref_key_about_attributions)).setOnPreferenceClickListener(
          new Preference.OnPreferenceClickListener() {

            @Override public boolean onPreferenceClick(Preference preference) {
              buildAttributionsDialog(SettingsActivity.this).show();
              return false;
            }
          });
      findPreference(getString(R.string.pref_key_about_translate)).setOnPreferenceClickListener(
          new Preference.OnPreferenceClickListener() {
            @Override public boolean onPreferenceClick(Preference preference) {
              Helpers.openTranslatePage(SettingsActivity.this);
              return false;
            }
          }
      );
      findPreference(getString(R.string.pref_key_about_feedback)).setOnPreferenceClickListener(
          new Preference.OnPreferenceClickListener() {

            @Override public boolean onPreferenceClick(Preference preference) {
              Helpers.openSupportPage(SettingsActivity.this);
              return false;
            }
          });
    } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      addPreferencesFromResource(R.xml.preference_headers_legacy);
    }
  }

  @Override protected void onStart() {
    super.onStart();
    EasyTracker.getInstance().activityStart(this);
  }

  @Override protected void onStop() {
    super.onStop();
    EasyTracker.getInstance().activityStop(this);
  }

  @Override protected void onDestroy() {
    super.onDestroy();

    PreferenceManager.getDefaultSharedPreferences(this)
        .unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override protected boolean isValidFragment(String fragmentName) {
    // Nothing to worry about here... I'm not hiding PrefereceFragments.
    return true;
  }

  @Override @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public void onBuildHeaders(List<Header> target) {
    loadHeadersFromResource(R.xml.preference_headers, target);
    updateHeaderList(target);
  }

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  private void updateHeaderList(List<Header> target) {
    //        int i = 0;
    //        while (i < target.size()) {
    //            Header header = target.get(i);
    //            if (i < target.size() && target.get(i) == header) {
    //                i++;
    //            }
    //        }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (getString(R.string.pref_key_general_analytics).equals(key)) {
      boolean value = sharedPreferences.getBoolean(key, true);
      GoogleAnalytics.getInstance(this).setAppOptOut(!value);
    }
  }

  public static void updateTimeoutSummary(Preference preference, String delay) {
    int i = Integer.parseInt(delay);
    if (i == -1) {
      preference.setSummary(R.string.alarm_timeout_summary_never);
    } else {
      preference.setSummary(
          preference.getContext().getString(R.string.alarm_timeout_summary_other, i));
    }
  }

  public static String getAppVersion(Context context) {
    try {
      PackageManager packageManager = context.getPackageManager();
      PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);

      return packageInfo.versionName;
    } catch (PackageManager.NameNotFoundException ignored) {
    }

    return null;
  }

  public static Dialog buildChangeLogDialog(Context context) {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);

    builder.setTitle(R.string.change_log_title);

    InputStream changelogStream = context.getResources().openRawResource(R.raw.changelog);
    Scanner s = new Scanner(changelogStream, ENCODING).useDelimiter("\\A");
    String changelogHtml = s.hasNext() ? s.next() : "";

    try {
      changelogHtml = URLEncoder.encode(changelogHtml, ENCODING).replaceAll("\\+", "%20");
    } catch (UnsupportedEncodingException e) {
      Log.e(TAG, "failed to encode change log html", e);
    }

    WebView webView = new WebView(context);
    webView.loadData(changelogHtml, "text/html", ENCODING);
    builder.setView(webView);

    builder.setNeutralButton(R.string.change_log_close, new DialogInterface.OnClickListener() {

      @Override public void onClick(@NonNull DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    return builder.create();
  }

  public static Dialog buildAttributionsDialog(Context context) {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);

    LayoutInflater inflater = LayoutInflater.from(context);
    View rootView = inflater.inflate(R.layout.about_attributions, null);

    // for some reason when you replace the view on a legacy dialog it wipes
    // the background colour...
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
      rootView.setBackgroundColor(
          context.getResources().getColor(android.R.color.background_light));
    }

    TextView attributionsView = (TextView) rootView.findViewById(R.id.attributions);
    attributionsView.setText(Html.fromHtml(context.getString(R.string.attributions)));
    attributionsView.setMovementMethod(new LinkMovementMethod());

    builder.setTitle(R.string.attributions_title);
    builder.setView(rootView);
    builder.setPositiveButton(R.string.attributions_close, new DialogInterface.OnClickListener() {

      @Override public void onClick(@NonNull DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });

    return builder.create();
  }
}
