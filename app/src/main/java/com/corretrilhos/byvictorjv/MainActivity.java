package com.corretrilhos.byvictorjv;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
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
    private static final long SPLASH_DURATION_MS = 6000L;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private FrameLayout root;
    private WebView webView;
    private View fullscreenCustomView;
    private WebChromeClient.CustomViewCallback fullscreenCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Install the first screen before changing system bars or creating WebView.
        // This keeps startup visible even on devices with customized Android UI.
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(3, 116, 202));
        setContentView(root);

        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_FULLSCREEN);

            LoadingScreenView splash = new LoadingScreenView(this);
            root.addView(splash, matchParent());
            applyImmersiveMode();
            root.post(this::applyImmersiveMode);

            handler.postDelayed(() -> {
                if (!isFinishing() && !isDestroyed()) {
                    showWebsite();
                }
            }, SPLASH_DURATION_MS);
        } catch (Throwable error) {
            showFatalError("Não foi possível iniciar a tela de abertura.", error);
        }
    }

    private void showWebsite() {
        if (webView != null || isFinishing() || root == null) return;

        WebView candidate = null;
        try {
            // Use the Activity context. A WebView created with the application
            // context can fail on some Android/WebView provider combinations.
            candidate = new WebView(this);
            configureWebView(candidate);

            root.removeAllViews();
            root.addView(candidate, matchParent());
            webView = candidate;
            applyImmersiveMode();
            candidate.loadUrl(START_URL);
        } catch (Throwable error) {
            if (candidate != null) {
                try {
                    candidate.stopLoading();
                    candidate.destroy();
                } catch (Throwable ignored) { }
            }
            webView = null;
            showFatalError("Não foi possível abrir o jogo no WebView.", error);
        }
    }

    private void configureWebView(WebView view) {
        view.setBackgroundColor(Color.BLACK);
        view.setOverScrollMode(View.OVER_SCROLL_NEVER);
        view.setVerticalScrollBarEnabled(false);
        view.setHorizontalScrollBarEnabled(false);

        WebSettings settings = view.getSettings();
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
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        cookies.setAcceptThirdPartyCookies(view, true);

        view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest request) {
                return handleUrl(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView v, String url) {
                return handleUrl(Uri.parse(url));
            }

            @Override
            public void onReceivedError(WebView v, WebResourceRequest request,
                                        WebResourceError error) {
                super.onReceivedError(v, request, error);
                if (request.isForMainFrame()) showNetworkError();
            }
        });

        view.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onShowCustomView(View view, CustomViewCallback callback) {
                if (fullscreenCustomView != null) {
                    callback.onCustomViewHidden();
                    return;
                }
                fullscreenCustomView = view;
                fullscreenCallback = callback;
                root.addView(view, matchParent());
                applyImmersiveMode();
            }

            @Override
            public void onHideCustomView() {
                hideCustomView();
            }
        });
    }

    private void hideCustomView() {
        if (fullscreenCustomView == null) return;
        root.removeView(fullscreenCustomView);
        fullscreenCustomView = null;
        if (fullscreenCallback != null) {
            fullscreenCallback.onCustomViewHidden();
            fullscreenCallback = null;
        }
        applyImmersiveMode();
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
                    if (fallback != null && webView != null) webView.loadUrl(fallback);
                    return true;
                }
            } else {
                intent = new Intent(Intent.ACTION_VIEW, uri);
            }
            startActivity(intent);
        } catch (ActivityNotFoundException | java.net.URISyntaxException error) {
            Toast.makeText(this, "Não foi possível abrir este endereço.", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private void showNetworkError() {
        if (root == null || root.findViewWithTag("network-error") != null) return;

        LinearLayout panel = new LinearLayout(this);
        panel.setTag("network-error");
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

        Button retry = new Button(this);
        retry.setText("TENTAR NOVAMENTE");
        retry.setOnClickListener(v -> {
            root.removeView(panel);
            if (webView != null) webView.reload();
        });

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.topMargin = dp(20);

        panel.addView(title);
        panel.addView(detail);
        panel.addView(retry, buttonParams);
        root.addView(panel, matchParent());
    }

    private void showFatalError(String message, Throwable error) {
        if (root == null) {
            root = new FrameLayout(this);
            setContentView(root);
        }
        root.removeAllViews();

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(dp(24), dp(24), dp(24), dp(24));
        panel.setBackgroundColor(Color.rgb(8, 24, 53));

        TextView title = new TextView(this);
        title.setText("Corre Trilho");
        title.setTextColor(Color.WHITE);
        title.setTextSize(26);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER);

        TextView detail = new TextView(this);
        String reason = error == null ? "Erro desconhecido" : error.getClass().getSimpleName();
        detail.setText(message + "\n\nDetalhe: " + reason
                + (error != null && error.getMessage() != null ? "\n" + error.getMessage() : ""));
        detail.setTextColor(Color.rgb(207, 225, 244));
        detail.setTextSize(15);
        detail.setGravity(Gravity.CENTER);

        Button retry = new Button(this);
        retry.setText("TENTAR NOVAMENTE");
        retry.setOnClickListener(v -> recreate());

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.topMargin = dp(20);

        panel.addView(title);
        panel.addView(detail);
        panel.addView(retry, buttonParams);
        root.addView(panel, matchParent());
        applyImmersiveMode();
    }

    private FrameLayout.LayoutParams matchParent() {
        return new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @SuppressWarnings("deprecation")
    private void applyImmersiveMode() {
        Window window = getWindow();
        if (window == null) return;

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) applyImmersiveMode();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
        applyImmersiveMode();
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (fullscreenCustomView != null) {
            hideCustomView();
        } else if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
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
