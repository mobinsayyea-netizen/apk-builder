package com.mobeen.apkbuilder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
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
    webView.setWebChromeClient(new WebChromeClient());
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
}
