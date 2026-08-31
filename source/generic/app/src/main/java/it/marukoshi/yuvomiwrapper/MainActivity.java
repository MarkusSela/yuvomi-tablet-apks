package it.marukoshi.yuvomiwrapper;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.ServiceWorkerController;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public final class MainActivity extends Activity {
    private static final String PREFS = "yuvomi_generic";
    private static final String ENDPOINT_KEY = "endpoint";
    private static final int FILE_CHOOSER_REQUEST = 4101;

    private WebView webView;
    private ProgressBar progressBar;
    private LinearLayout errorView;
    private android.webkit.ValueCallback<Uri[]> uploadCallback;
    private Uri configuredEndpoint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        hideSystemUi();

        configuredEndpoint = readEndpoint();
        if (savedInstanceState != null && configuredEndpoint != null) {
            showWebView(configuredEndpoint, savedInstanceState);
        } else if (configuredEndpoint != null) {
            showWebView(configuredEndpoint, null);
        } else {
            showSetup(null);
        }
    }

    private Uri readEndpoint() {
        String value = getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(ENDPOINT_KEY, null);
        return value == null ? null : parseEndpoint(value);
    }

    private Uri parseEndpoint(String raw) {
        if (raw == null) return null;
        String value = raw.trim();
        if (value.length() == 0) return null;
        if (value.indexOf("://") < 0) value = "http://" + value;
        Uri uri;
        try {
            uri = Uri.parse(value);
        } catch (Exception ignored) {
            return null;
        }
        String scheme = uri.getScheme();
        String host = uri.getHost();
        if (scheme == null || host == null || host.length() == 0) return null;
        if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) return null;
        if (uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null) {
            return null;
        }
        int port = uri.getPort();
        if (port < -1 || port > 65535) return null;
        String path = uri.getPath();
        if (path == null || path.length() == 0) path = "/";
        return uri.buildUpon().path(path).build();
    }

    private void showSetup(String message) {
        webView = null;
        errorView = null;

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(248, 247, 243));
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER_HORIZONTAL);
        panel.setPadding(48, 72, 48, 48);
        scroll.addView(panel, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.MATCH_PARENT));

        TextView title = new TextView(this);
        title.setText(getString(R.string.setup_title));
        title.setTextColor(Color.rgb(40, 40, 40));
        title.setTextSize(30);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        panel.addView(title, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView subtitle = new TextView(this);
        subtitle.setText(getString(R.string.setup_subtitle));
        subtitle.setTextColor(Color.DKGRAY);
        subtitle.setTextSize(17);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subtitleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        subtitleParams.topMargin = 18;
        panel.addView(subtitle, subtitleParams);

        EditText endpoint = new EditText(this);
        endpoint.setSingleLine(true);
        endpoint.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        endpoint.setHint(getString(R.string.server_url_hint));
        endpoint.setText(configuredEndpoint == null ? "" : configuredEndpoint.toString());
        endpoint.setSelectAllOnFocus(false);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        inputParams.topMargin = 36;
        panel.addView(endpoint, inputParams);

        TextView help = new TextView(this);
        help.setText("Esempi: http://192.168.1.20:3001, https://server.tailnet.ts.net:8454, https://yuvomi.example.it");
        help.setTextColor(Color.GRAY);
        help.setTextSize(13);
        LinearLayout.LayoutParams helpParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        helpParams.topMargin = 8;
        panel.addView(help, helpParams);

        TextView error = new TextView(this);
        error.setTextColor(Color.rgb(170, 35, 35));
        error.setTextSize(15);
        error.setGravity(Gravity.CENTER);
        error.setVisibility(message == null ? View.GONE : View.VISIBLE);
        if (message != null) error.setText(message);
        LinearLayout.LayoutParams errorParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        errorParams.topMargin = 22;
        panel.addView(error, errorParams);

        Button continueButton = new Button(this);
        continueButton.setText(getString(R.string.verify_continue));
        continueButton.setOnClickListener(view -> {
            Uri endpointUri = parseEndpoint(endpoint.getText().toString());
            if (endpointUri == null) {
                error.setText(getString(R.string.invalid_url));
                error.setVisibility(View.VISIBLE);
                return;
            }
            configuredEndpoint = endpointUri;
            getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                    .putString(ENDPOINT_KEY, endpointUri.toString())
                    .apply();
            showWebView(endpointUri, null);
        });
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        buttonParams.topMargin = 28;
        panel.addView(continueButton, buttonParams);

        setContentView(scroll);
    }

    private void showWebView(Uri endpoint, Bundle savedInstanceState) {
        configuredEndpoint = endpoint;
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);

        webView = new WebView(this);
        configureWebView();
        root.addView(webView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        root.addView(progressBar, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, 6, Gravity.TOP));

        errorView = buildErrorView();
        errorView.setVisibility(View.GONE);
        root.addView(errorView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT));

        Button settings = new Button(this);
        settings.setText("⚙️");
        settings.setContentDescription("Impostazioni tablet Android");
        settings.setOnClickListener(view -> startActivity(new Intent(this, TabletSettingsActivity.class)));
        FrameLayout.LayoutParams settingsParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP | Gravity.END);
        settingsParams.topMargin = 12;
        settingsParams.rightMargin = 12;
        root.addView(settings, settingsParams);

        setContentView(root);
        if (savedInstanceState == null) webView.loadUrl(endpoint.toString());
        else webView.restoreState(savedInstanceState);
    }

    private void configureWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setSupportZoom(false);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setLoadsImagesAutomatically(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ServiceWorkerController.getInstance().getServiceWorkerWebSettings()
                    .setAllowContentAccess(true);
        }

        CookieManager cookies = CookieManager.getInstance();
        cookies.setAcceptCookie(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookies.setAcceptThirdPartyCookies(webView, true);
        }

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
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
                errorView.setVisibility(View.GONE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
                errorView.setVisibility(View.GONE);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request,
                                        WebResourceError error) {
                if (request.isForMainFrame()) showError();
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setProgress(newProgress);
                if (newProgress >= 100) progressBar.setVisibility(View.GONE);
                else progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public boolean onShowFileChooser(WebView view,
                                             android.webkit.ValueCallback<Uri[]> callback,
                                             FileChooserParams params) {
                if (uploadCallback != null) uploadCallback.onReceiveValue(null);
                uploadCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                try {
                    startActivityForResult(intent, FILE_CHOOSER_REQUEST);
                    return true;
                } catch (Exception ignored) {
                    uploadCallback = null;
                    return false;
                }
            }
        });

        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition,
                                        String mimetype, long contentLength) {
                try {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                    String cookie = CookieManager.getInstance().getCookie(url);
                    if (cookie != null) request.addRequestHeader("Cookie", cookie);
                    if (userAgent != null) request.addRequestHeader("User-Agent", userAgent);
                    if (mimetype != null) request.setMimeType(mimetype);
                    request.setNotificationVisibility(
                            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(
                            Environment.DIRECTORY_DOWNLOADS,
                            URLUtil.guessFileName(url, contentDisposition, mimetype));
                    DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    if (manager != null) manager.enqueue(request);
                    Toast.makeText(MainActivity.this, "Download avviato", Toast.LENGTH_SHORT).show();
                } catch (Exception ignored) {
                    Toast.makeText(MainActivity.this, "Download non disponibile", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean handleUrl(Uri uri) {
        if (uri == null) return true;
        String scheme = uri.getScheme();
        if (("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme))
                && isSameOrigin(uri)) {
            return false;
        }
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, uri));
        } catch (Exception ignored) {
            Toast.makeText(this, "Collegamento non disponibile", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    private boolean isSameOrigin(Uri uri) {
        if (configuredEndpoint == null || uri.getHost() == null) return false;
        if (!configuredEndpoint.getScheme().equalsIgnoreCase(uri.getScheme())) return false;
        if (!configuredEndpoint.getHost().equalsIgnoreCase(uri.getHost())) return false;
        return effectivePort(configuredEndpoint) == effectivePort(uri);
    }

    private int effectivePort(Uri uri) {
        if (uri.getPort() >= 0) return uri.getPort();
        return "https".equalsIgnoreCase(uri.getScheme()) ? 443 : 80;
    }

    private LinearLayout buildErrorView() {
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setGravity(Gravity.CENTER);
        panel.setPadding(48, 48, 48, 48);
        panel.setBackgroundColor(Color.BLACK);

        TextView message = new TextView(this);
        message.setText(getString(R.string.connection_error));
        message.setTextColor(Color.WHITE);
        message.setTextSize(18);
        message.setGravity(Gravity.CENTER);
        panel.addView(message, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        Button retry = new Button(this);
        retry.setText(getString(R.string.retry));
        retry.setOnClickListener(view -> {
            errorView.setVisibility(View.GONE);
            webView.reload();
        });
        panel.addView(retry, centeredButtonParams(24));

        Button change = new Button(this);
        change.setText(getString(R.string.change_server));
        change.setOnClickListener(view -> showSetup(null));
        panel.addView(change, centeredButtonParams(8));
        return panel;
    }

    private LinearLayout.LayoutParams centeredButtonParams(int topMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.topMargin = topMargin;
        return params;
    }

    private void showError() {
        progressBar.setVisibility(View.GONE);
        errorView.setVisibility(View.VISIBLE);
    }

    private void hideSystemUi() {
        getWindow().getDecorView().setSystemUiVisibility(
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
        if (hasFocus) hideSystemUi();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (webView != null) webView.saveState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.resumeTimers();
        hideSystemUi();
    }

    @Override
    protected void onPause() {
        if (webView != null) webView.pauseTimers();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else if (webView != null) {
            showSetup(null);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != FILE_CHOOSER_REQUEST || uploadCallback == null) return;
        Uri[] results = null;
        if (resultCode == RESULT_OK && data != null) {
            ClipData clipData = data.getClipData();
            if (clipData != null) {
                results = new Uri[clipData.getItemCount()];
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    results[i] = clipData.getItemAt(i).getUri();
                }
            } else if (data.getData() != null) {
                results = new Uri[]{data.getData()};
            }
        }
        uploadCallback.onReceiveValue(results);
        uploadCallback = null;
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }
        super.onDestroy();
    }
}
