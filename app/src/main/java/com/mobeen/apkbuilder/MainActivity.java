package com.mobeen.apkbuilder;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Loads the existing Accessible GitHub APK Builder tool (assets/index.html) inside a WebView, so
 * it opens instantly as its own app instead of needing to be found and opened as a file each time.
 * The page itself, including every button, label and input, is not touched — only a small CSS
 * spacing fix (button size/margins) was applied to that file, no colours or layout changed.
 *
 * The "Share" button below is native (not part of the web page): it shares a link to this app so
 * the person can pass it on to someone else, without touching the tool's own content.
 */
public class MainActivity extends Activity {

  private static final String SHARE_URL =
      "https://github.com/mobinsayyea-netizen/apk-builder/releases/latest";

  private static final int FILE_CHOOSER_REQ = 1;
  private ValueCallback<Uri[]> filePathCallback;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);

    WebView webView = new WebView(this);
    WebSettings settings = webView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true); // keeps the tool's saved token/Gemini key working
    settings.setAllowFileAccess(true);
    webView.setWebViewClient(new WebViewClient());
    // Without onShowFileChooser, tapping any <input type="file"> does nothing in an Android
    // WebView (the file manager never opens). This hands the request to the system file picker
    // and returns the chosen file(s) to the page.
    webView.setWebChromeClient(
        new WebChromeClient() {
          @Override
          public boolean onShowFileChooser(
              WebView view,
              ValueCallback<Uri[]> callback,
              WebChromeClient.FileChooserParams params) {
            if (filePathCallback != null) {
              filePathCallback.onReceiveValue(null);
            }
            filePathCallback = callback;
            try {
              Intent intent = params.createIntent();
              startActivityForResult(intent, FILE_CHOOSER_REQ);
            } catch (ActivityNotFoundException e) {
              filePathCallback = null;
              callback.onReceiveValue(null);
              return false;
            }
            return true;
          }
        });
    webView.loadUrl("file:///android_asset/index.html");

    Button shareButton = new Button(this);
    shareButton.setText("Share this app");
    shareButton.setOnClickListener(
        (View v) -> {
          Intent intent = new Intent(Intent.ACTION_SEND);
          intent.setType("text/plain");
          intent.putExtra(
              Intent.EXTRA_TEXT,
              "APK Builder \u2014 build and manage Android apps from GitHub, right from your"
                  + " phone: "
                  + SHARE_URL);
          startActivity(Intent.createChooser(intent, "Share APK Builder"));
        });
    LinearLayout.LayoutParams shareParams =
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    int pad = (int) (10 * getResources().getDisplayMetrics().density);
    shareButton.setPadding(pad, pad, pad, pad);
    shareButton.setGravity(Gravity.CENTER);

    root.addView(
        webView,
        new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
    root.addView(shareButton, shareParams);

    setContentView(root);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == FILE_CHOOSER_REQ) {
      if (filePathCallback != null) {
        filePathCallback.onReceiveValue(
            WebChromeClient.FileChooserParams.parseResult(resultCode, data));
        filePathCallback = null;
      }
      return;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}
