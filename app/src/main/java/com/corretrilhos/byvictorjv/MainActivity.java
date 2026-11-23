package com.corretrilhos.byvictorjv;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String START_URL = "https://subwayrunblodvt.lovable.app/";
    private static final long SPLASH_DURATION_MS = 6_000L;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private FrameLayout rootLayout;
    private WebView webView;
    private View customFullscreenView;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private WebChromeClient webChromeClient;
    private boolean webViewStarted = false;

    private final Runnable openWebsite = this::showWebsite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyImmersiveMode();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        rootLayout = new FrameLayout(this);
        rootLayout.setBackgroundColor(Color.BLACK);
        setContentView(rootLayout);

        rootLayout.addView(new LoadingScreenView(this), new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));

        mainHandler.postDelayed(openWebsite, SPLASH_DURATION_MS);
    }

    private void showWebsite() {
        if (isFinishing() || isDestroyed() || webViewStarted) {
            return;
        }
        webViewStarted = true;
        rootLayout.removeAllViews();

        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(false);
        settings.setDefaultTextEncodingName("UTF-8");
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            settings.setSafeBrowsingEnabled(true);
        }

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(Uri.parse(url));
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (request.isForMainFrame()) {
                    showLoadError();
                }
            }
        });

        webChromeClient = new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (customFullscreenView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                customFullscreenView = view;
                customViewCallback = callback;
                rootLayout.addView(view, new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));
                applyImmersiveMode();
            }

            @Override
            public void onHideCustomView() {
                if (customFullscreenView == null) {
                    return;
                }
                rootLayout.removeView(customFullscreenView);
                customFullscreenView = null;
                if (customViewCallback != null) {
                    customViewCallback.onCustomViewHidden();
                    customViewCallback = null;
                }
                applyImmersiveMode();
            }
        };
        webView.setWebChromeClient(webChromeClient);

        rootLayout.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        webView.loadUrl(START_URL);
    }

    private boolean handleUrl(Uri uri) {
        String scheme = uri.getScheme();
        if (scheme == null || "http".equalsIgnoreCase(scheme)
                || "https".equalsIgnoreCase(scheme)
                || "about".equalsIgnoreCase(scheme)
                || "data".equalsIgnoreCase(scheme)
                || "blob".equalsIgnoreCase(scheme)) {
            return false;
        }

        try {
            Intent intent;
            if ("intent".equalsIgnoreCase(scheme)) {
                intent = Intent.parseUri(uri.toString(), Intent.URI_INTENT_SCHEME);
                if (intent.resolveActivity(getPackageManager()) == null) {
                    String fallback = intent.getStringExtra("browser_fallback_url");
                    if (fallback != null && webView != null) {
                        webView.loadUrl(fallback);
                    }
                    return true;
                }
            } else {
                intent = new Intent(Intent.ACTION_VIEW, uri);
            }
            startActivity(intent);
        } catch (ActivityNotFoundException | java.net.URISyntaxException exception) {
            Toast.makeText(this, "Não foi possível abrir este endereço.", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void showLoadError() {
        if (rootLayout == null || webView == null || rootLayout.findViewWithTag("load-error") != null) {
            return;
        }

        LinearLayout panel = new LinearLayout(this);
        panel.setTag("load-error");
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(28), dp(26), dp(28), dp(26));
        panel.setBackgroundColor(Color.rgb(8, 24, 53));

        TextView title = new TextView(this);
        title.setText("Ops! A pista não carregou.");
        title.setTextColor(Color.WHITE);
        title.setTextSize(21);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView detail = new TextView(this);
        detail.setText("Confira sua conexão com a internet e tente novamente.");
        detail.setTextColor(Color.rgb(207, 225, 244));
        detail.setTextSize(15);
        detail.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams detailParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        detailParams.topMargin = dp(12);

        Button retry = new Button(this);
        retry.setText("TENTAR NOVAMENTE");
        retry.setTextColor(Color.rgb(25, 36, 60));
        retry.setOnClickListener(v -> {
            rootLayout.removeView(panel);
            webView.reload();
        });
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.topMargin = dp(22);

        panel.addView(title);
        panel.addView(detail, detailParams);
        panel.addView(retry, buttonParams);
        rootLayout.addView(panel, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void applyImmersiveMode() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false);
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            applyImmersiveMode();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) {
            webView.onResume();
        }
        applyImmersiveMode();
    }

    @Override
    protected void onPause() {
        if (webView != null) {
            webView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (customFullscreenView != null) {
            if (webView != null) {
                if (webChromeClient != null) {
                    webChromeClient.onHideCustomView();
                }
            }
            return;
        }
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mainHandler.removeCallbacks(openWebsite);
        if (webView != null) {
            webView.stopLoading();
            webView.setWebChromeClient(null);
            webView.setWebViewClient(null);
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
