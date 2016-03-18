package se.kth.csc.stayawhile;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Arrays;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class QueueActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Socket mSocket;
    private JSONObject mQueue;
    private String mQueueName;

    {
        try {
            mSocket = IO.socket("http://queue.csc.kth.se/");
        } catch (URISyntaxException e) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.mQueueName = getIntent().getStringExtra("queue");
        setContentView(R.layout.activity_queue);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mQueueName);

        mRecyclerView = (RecyclerView) findViewById(R.id.queue_people);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        registerForContextMenu(mRecyclerView);

        new APITask(new APICallback() {
            @Override
            public void r(String result) {
                try {
                    mQueue = new JSONObject(result);
                    QueueActivity.this.onQueueUpdate();
                } catch (JSONException e) {
                    //TODO
                    e.printStackTrace();
                }
            }
        }).execute("method", "queue/" + mQueueName);
        mSocket.on("join", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                newUser(args);
            }
        });
        mSocket.on("leave", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                removeUser(args);
            }
        });
        mSocket.connect();
        mSocket.emit("listen", mQueueName);
    }

    private void newUser(Object... args) {
        try {
            mQueue.getJSONArray("queue").put(args[0]);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onQueueUpdate();
                }
            });
        } catch (JSONException e) {
        }
    }

    private void removeUser(Object... args) {
        try {
            String id = ((JSONObject) args[0]).getString("ugKthid");
            JSONArray queuees = mQueue.getJSONArray("queue");
            for (int i = 0; i < queuees.length(); i++) {
                if (queuees.getJSONObject(i).getString("ugKthid").equals(id)) {
                    queuees.remove(i);
                    break;
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onQueueUpdate();
                }
            });
            System.out.println(Arrays.toString(args));
        } catch (JSONException e) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
    }

    private void onQueueUpdate() {
        try {
            mAdapter = new QueueAdapter(mQueue.getJSONArray("queue"), this);
            mRecyclerView.setAdapter(mAdapter);
        } catch (JSONException e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
