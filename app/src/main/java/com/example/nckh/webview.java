package com.example.nckh;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class webview extends AppCompatActivity {
    private WebView webView;
    private String url;
    private ImageView btnClose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Không cần requestWindowFeature và getSupportActionBar().hide() vì đã có theme
        setContentView(R.layout.activity_webview);

        // Khởi tạo các view
        webView = findViewById(R.id.webView);
        btnClose = findViewById(R.id.btnClose);
        
        // Lấy URL từ intent
        url = getIntent().getStringExtra("url");

        // Cấu hình WebView
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(url);

        // Xử lý sự kiện click nút đóng
        btnClose.setOnClickListener(v -> finish());
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack())
            webView.goBack();
        else
            super.onBackPressed();
    }
}