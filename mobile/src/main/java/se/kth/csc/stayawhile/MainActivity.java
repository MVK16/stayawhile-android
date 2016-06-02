package se.kth.csc.stayawhile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.ExecutionException;

import se.kth.csc.stayawhile.api.APICallback;
import se.kth.csc.stayawhile.api.APITask;
import se.kth.csc.stayawhile.cookies.PersistentCookieStore;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        android.webkit.CookieManager.getInstance().setAcceptCookie(true);
        CookieManager cookieManager = new CookieManager(new PersistentCookieStore(getApplicationContext()), CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        new APITask(new APICallback() {
            @Override
            public void r(String result) {
                if (result.length() != 0) {
                    Intent queueList = new Intent(MainActivity.this, QueueListActivity.class);
                    startActivity(queueList);
                } else {
                    setContentView(R.layout.activity_main);
                    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
                    setSupportActionBar(toolbar);
                }
            }
        }).execute("method", "userData");
    }

    public void onLogin(View view) {
        WebView webView = new WebView(this);
        setContentView(webView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("https://login.kth.se/login?service=http://queue.csc.kth.se/auth");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (url.startsWith("http://queue.csc.kth.se/auth")) {
                    MainActivity.this.onTicket(url);
                    view.stopLoading();
                }
            }
        });
    }

    public void onTicket(String url) {
        try {
            new APITask().execute("url", url).get();
            new APITask(new APICallback() {
                @Override
                public void r(String result) {
                    SharedPreferences.Editor editor = getApplicationContext().getSharedPreferences("userData", Context.MODE_PRIVATE).edit();
                    editor.putString("userData", result);
                    editor.apply();
                    Intent queueList = new Intent(MainActivity.this, QueueListActivity.class);
                    queueList.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                    startActivity(queueList);
                }
            }).execute("method", "userData");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }



    @Override
    protected void onDestroy(){
        super.onDestroy();
    }
}
