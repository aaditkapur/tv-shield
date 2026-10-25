package com.shieldtv.browser;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.KeyEvent;
import android.view.View;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.net.Uri;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private WebView webView;
    private EditText address;
    private TextView status;
    private boolean blockingEnabled = true;
    private final Set<String> blockedHosts = new HashSet<>(Arrays.asList(
        "doubleclick.net", "googlesyndication.com", "googleadservices.com",
        "adservice.google.com", "pagead2.googlesyndication.com", "adnxs.com",
        "adform.net", "adroll.com", "adsrvr.org", "scorecardresearch.com",
        "securepubads.g.doubleclick.net", "imasdk.googleapis.com"
    ));

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(16, 19, 24));
        root.setPadding(12, 8, 12, 8);

        LinearLayout bar = new LinearLayout(this);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setOrientation(LinearLayout.HORIZONTAL);

        TextView brand = new TextView(this);
        brand.setText("SHIELD TV"); brand.setTextColor(Color.rgb(118, 215, 196));
        brand.setTextSize(18); brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.setPadding(8, 0, 14, 0);
        bar.addView(brand, new LinearLayout.LayoutParams(-2, 48));

        address = new EditText(this);
        address.setSingleLine(true); address.setTextSize(16); address.setTextColor(Color.WHITE);
        address.setHintTextColor(Color.LTGRAY); address.setHint("Enter website or search");
        address.setSelectAllOnFocus(true); address.setImeOptions(EditorInfo.IME_ACTION_GO);
        address.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        address.setPadding(12, 0, 12, 0); address.setBackgroundColor(Color.rgb(37, 43, 52));
        bar.addView(address, new LinearLayout.LayoutParams(0, 48, 1));
        address.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                navigate(address.getText().toString());
                ((InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(address.getWindowToken(), 0);
                address.clearFocus(); return true;
            }
            return false;
        });

        Button go = makeButton("GO"); go.setOnClickListener(v -> navigate(address.getText().toString()));
        bar.addView(go, new LinearLayout.LayoutParams(72, 48));
        Button home = makeButton("HOME"); home.setOnClickListener(v -> loadHome());
        bar.addView(home, new LinearLayout.LayoutParams(86, 48));
        Button shield = makeButton("BLOCK: ON");
        shield.setOnClickListener(v -> { blockingEnabled = !blockingEnabled; shield.setText(blockingEnabled ? "BLOCK: ON" : "BLOCK: OFF"); status.setText(blockingEnabled ? "Basic filtering enabled" : "Filtering disabled"); });
        bar.addView(shield, new LinearLayout.LayoutParams(120, 48));
        root.addView(bar, new LinearLayout.LayoutParams(-1, 52));

        status = new TextView(this); status.setText("Basic filtering enabled • YouTube ad blocking is not guaranteed");
        status.setTextColor(Color.rgb(180, 190, 200)); status.setTextSize(11); status.setPadding(8, 2, 8, 4);
        root.addView(status, new LinearLayout.LayoutParams(-1, 22));

        webView = new WebView(this);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true); settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false); settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false); settings.setLoadsImagesAutomatically(true);
        settings.setSupportMultipleWindows(false); settings.setJavaScriptCanOpenWindowsAutomatically(false);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return false;
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                if (blockingEnabled && request != null && request.getUrl() != null && isBlocked(request.getUrl().getHost())) {
                    return new WebResourceResponse("text/plain", "utf-8", new ByteArrayInputStream(new byte[0]));
                }
                return super.shouldInterceptRequest(view, request);
            }
            @Override public void onPageFinished(WebView view, String url) {
                address.setText(url); status.setText("Loaded • Basic host filtering only; some ads may still appear");
            }
        });
        root.addView(webView, new LinearLayout.LayoutParams(-1, 0, 1));
        setContentView(root);
        loadHome();
    }

    private Button makeButton(String text) {
        Button b = new Button(this); b.setText(text); b.setTextSize(11); b.setAllCaps(false);
        b.setFocusable(true); b.setTextColor(Color.WHITE); b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(48, 59, 70)));
        return b;
    }
    private boolean isBlocked(String host) {
        if (host == null) return false;
        host = host.toLowerCase(Locale.ROOT);
        for (String domain : blockedHosts) if (host.equals(domain) || host.endsWith("." + domain)) return true;
        return false;
    }
    private void navigate(String raw) {
        if (raw == null) return;
        String value = raw.trim(); if (value.isEmpty()) return;
        if (!value.contains(".") || value.contains(" ")) value = "https://www.google.com/search?q=" + Uri.encode(value);
        else if (!value.matches("(?i)^https?://.*")) value = "https://" + value;
        webView.loadUrl(value);
    }
    private void loadHome() {
        address.setText("https://www.youtube.com");
        webView.loadUrl("https://www.youtube.com");
        status.setText("Shield TV Browser • Basic filtering enabled");
    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView != null && webView.canGoBack()) { webView.goBack(); return true; }
        if (keyCode == KeyEvent.KEYCODE_MENU && address != null) { address.requestFocus(); return true; }
        return super.onKeyDown(keyCode, event);
    }
    @Override protected void onDestroy() { if (webView != null) { webView.destroy(); } super.onDestroy(); }
}
