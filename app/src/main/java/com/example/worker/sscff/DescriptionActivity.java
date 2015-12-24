package com.example.worker.sscff;

import android.app.Activity;

import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class DescriptionActivity extends Activity {

    private WebView mWebView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        mWebView = (WebView) findViewById(R.id.webView);
        // включаем поддержку JavaScript
        mWebView.getSettings().setJavaScriptEnabled(true);
        // указываем страницу загрузки
        Uri url = getIntent().getData();
        mWebView.loadUrl(url.toString());

//      Определим экземпляр MyWebViewClient
        mWebView.setWebViewClient(new MyWebViewClient());

//      Добавляем увеличение без кнопок через мультитач
        mWebView.getSettings().setBuiltInZoomControls(true);
        mWebView.getSettings().setDisplayZoomControls(false);

    }

//    @Override
//    public void onBackPressed() {
//        if(mWebView.canGoBack()) {
//            mWebView.goBack();
//        } else {
//            super.onBackPressed();
//        }
//    }

    /* Учим открывать ссылки в своей программе. Для этого переопределяем класс WebViewClient
    и позволяем нашему приложению обрабатывать ссылки.*/

    private class MyWebViewClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            view.loadUrl(url);
            return true;
        }
    }
}
