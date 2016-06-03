package se.kth.csc.stayawhile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import se.kth.csc.stayawhile.api.APICallback;
import se.kth.csc.stayawhile.api.APITask;
import se.kth.csc.stayawhile.cookies.PersistentCookieStore;

public class QueueListActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private JSONObject mUserData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_queuelist);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        try {
            mUserData = new JSONObject(getApplicationContext().getSharedPreferences("userData", Context.MODE_PRIVATE).getString("userData", "{}"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mRecyclerView = (RecyclerView) findViewById(R.id.queue_list);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        final SwipeRefreshLayout refresh = (SwipeRefreshLayout) findViewById(R.id.queue_list_refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new APITask(new APICallback() {
                    @Override
                    public void r(String result) {
                        try {
                            SharedPreferences.Editor e = getApplicationContext().getSharedPreferences("userData", Context.MODE_PRIVATE).edit();
                            e.putString("userData", result);
                            e.apply();
                            mUserData = new JSONObject(result);
                            sendQueueList(new Runnable() {
                                @Override
                                public void run() {
                                    refresh.setRefreshing(false);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).execute("method", "userData");
            }
        });
        sendQueueList(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sendQueueList(null);
    }

    private void sendQueueList(final Runnable runnable) {
        new APITask(new APICallback() {
            @Override
            public void r(String result) {
                try {
                    JSONArray queues = new JSONArray(result);
                    QueueListActivity.this.onQueueUpdate(queues);
                    if (runnable != null) {
                        runnable.run();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).execute("method", "queueList");
    }

    private void onQueueUpdate(JSONArray queues) {
        mAdapter = new QueueListAdapter(queues, this, mUserData);
        mRecyclerView.setAdapter(mAdapter);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            new PersistentCookieStore(getApplicationContext()).removeAll();
            Intent queueList = new Intent(this, MainActivity.class);
            queueList.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(queueList);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
