package se.kth.csc.stayawhile;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import se.kth.csc.stayawhile.api.APICallback;
import se.kth.csc.stayawhile.api.APITask;
import se.kth.csc.stayawhile.swipe.QueueTouchListener;

public class QueueActivity extends AppCompatActivity implements MessageDialogFragment.MessageListener, QueueAdapter.StudentActionListener {

    private RecyclerView mRecyclerView;
    private QueueAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Socket mSocket;
    private JSONObject mQueue;
    private String mQueueName;
    private String mUgid;
    private PowerManager.WakeLock mWakeLock;
    private JSONObject curContextMenuObj;
    private JSONObject comments;

    {
        try {
            mSocket = IO.socket("http://queue.csc.kth.se/");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mQueueName = getIntent().getStringExtra("queue");
        this.comments = new JSONObject();
        try {
            JSONObject userData = new JSONObject(getApplicationContext().getSharedPreferences("userData", Context.MODE_PRIVATE).getString("userData", "{}"));
            this.mUgid = userData.getString("ugKthid");
        } catch (JSONException json) {
            throw new RuntimeException(json);
        }
        setContentView(R.layout.activity_queue);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(mQueueName);

        this.mRecyclerView = (RecyclerView) findViewById(R.id.queue_people);
        this.mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        this.mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addOnItemTouchListener(
                new QueueTouchListener(
                        mRecyclerView,
                        new QueueTouchListener.QueueSwipeListener() {

                            @Override
                            public void onDismiss(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                List<JSONObject> users = new ArrayList<>();
                                for (int position : reverseSortedPositions) {
                                    users.add(mAdapter.onPosition(position));
                                    mAdapter.removePosition(position);
                                }
                                mAdapter.notifyDataSetChanged();
                                for (JSONObject user : users) {
                                    sendKick(user);
                                }
                            }

                            @Override
                            public void onSetHelp(RecyclerView recyclerView, int[] reverseSortedPositions) {
                                List<JSONObject> users = new ArrayList<>();
                                for (int position : reverseSortedPositions) {
                                    users.add(mAdapter.onPosition(position));
                                }
                                for (JSONObject user : users) {
                                    try {
                                        if (user.getBoolean("gettingHelp")) {
                                            sendStopHelp(user);
                                        } else {
                                            sendHelp(user);
                                        }
                                    } catch (JSONException e) {
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        }
                )
        );

        registerForContextMenu(mRecyclerView);

        sendQueueUpdate();
        PowerManager pm = (PowerManager) getApplicationContext().getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "QueueActivity");
        mWakeLock.acquire();
        setSocketListeners();
        setupNotifications();
    }

    private void setSocketListeners() {
        final Handler h = new Handler();
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
        mSocket.on("update", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                updateUser(args);
            }
        });
        mSocket.on("help", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                setHelp(args);
                try {
                    MainActivity.wearMessageHandler.sendQueueToWear(mQueue);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        mSocket.on("stopHelp", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                h.post(new Runnable() {
                    @Override
                    public void run() {
                        setStopHelp(args);
                    }
                });
            }
        });
        mSocket.on("msg", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Bundle msg = new Bundle();
                try {
                    JSONObject jsonArg = (JSONObject) args[0];
                    msg.putString("title", "Message");
                    msg.putString("message", jsonArg.getString("message"));
                    msg.putString("sender", jsonArg.getString("sender"));
                    RecievedMessageDialogFragment fragment = new RecievedMessageDialogFragment();
                    fragment.setArguments(msg);
                    fragment.show(getFragmentManager(), "RecievedMessageDialogFragment");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                System.out.println("msg " + Arrays.toString(args));
            }
        });
        mSocket.on("flag", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject jsonArg = (JSONObject) args[0];
                try {
                    if (comments.has(jsonArg.getString("ugKthid"))) {
                        comments.put(jsonArg.getString("ugKthid"), (comments.getString(jsonArg.getString("ugKthid")) + "\n\n" +jsonArg.getString("message")));
                    } else {
                        comments.put(jsonArg.getString("ugKthid"), jsonArg.getString("message"));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
        mSocket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                mSocket.emit("listen", mQueueName);
                sendQueueUpdate();
            }
        });
        mSocket.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mWakeLock.release();
    }

    private void sendQueueUpdate() {
        new APITask(new APICallback() {
            @Override
            public void r(String result) {
                try {
                    mQueue = new JSONObject(result);
                    QueueActivity.this.onQueueUpdate();
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }).execute("method", "queue/" + Uri.encode(mQueueName));
    }

    private void sendStopHelp(JSONObject user) {
        try {
            System.out.println("send stopHelp " + user);
            JSONObject obj = new JSONObject();
            obj.put("ugKthid", user.get("ugKthid"));
            obj.put("queueName", mQueueName);
            mSocket.emit("stopHelp", obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendHelp(JSONObject user) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("ugKthid", user.get("ugKthid"));
            obj.put("queueName", mQueueName);
            System.out.println("send help " + obj);
            mSocket.emit("help", obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendKick(JSONObject user) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", user);
            obj.put("queueName", mQueueName);
            mSocket.emit("kick", obj);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendLock() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("queueName", mQueueName);
            mSocket.emit("lock", obj);
            sendQueueUpdate();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendUnlock() {
        try {
            JSONObject obj = new JSONObject();
            obj.put("queueName", mQueueName);
            mSocket.emit("unlock", obj);
            sendQueueUpdate();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void setStopHelp(Object... args) {
        try {
            JSONObject user = (JSONObject) args[0];
            int pos = mAdapter.positionOf(user.getString("ugKthid"));
            if (pos >= 0) {
                JSONObject existing = mAdapter.onPosition(pos);
                mAdapter.removePosition(pos);
                existing.put("gettingHelp", false);
                existing.remove("helper");
                mAdapter.add(existing);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void setHelp(Object... args) {
        try {
            JSONObject user = (JSONObject) args[0];
            int pos = mAdapter.positionOf(user.getString("ugKthid"));
            if (pos >= 0) {
                JSONObject existing = mAdapter.onPosition(pos);
                mAdapter.removePosition(pos);
                existing.put("gettingHelp", true);
                if (user.has("helper")) {
                    existing.put("helper", user.get("helper"));
                }
                mAdapter.add(existing);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void updateUser(Object... args) {
        try {
            JSONObject user = (JSONObject) args[0];
            int pos = mAdapter.positionOf(user.getString("ugKthid"));
            if (pos >= 0) {
                mAdapter.set(pos, user);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        try {
            JSONObject user = (JSONObject) args[0];
            JSONArray queuees = mQueue.getJSONArray("queue");
            for (int i = 0; i < queuees.length(); i++) {
                if (queuees.getJSONObject(i).getString("ugKthid").equals(user.getString("ugKthid"))) {
                    queuees.put(i, user);
                    break;
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onQueueUpdate();
                }
            });
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void newUser(Object... args) {
        JSONObject person = (JSONObject) args[0];
        mAdapter.add(person);
    }

    private void removeUser(Object... args) {
        try {
            String id = ((JSONObject) args[0]).getString("ugKthid");
            int pos = mAdapter.positionOf(id);
            if (pos >= 0) {
                mAdapter.removePosition(pos);
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isLocked() {
        try {
            return mQueue.getBoolean("locked");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void onQueueUpdate() {
        try {
            mAdapter = new QueueAdapter(mQueue.getJSONArray("queue"), this, mUgid);
            mRecyclerView.setAdapter(mAdapter);
            supportInvalidateOptionsMenu();
            comments = new JSONObject();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }


        try {
            MainActivity.wearMessageHandler.sendQueueToWear(mQueue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.queue_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mQueue != null) {
            MenuItem lockItem = menu.findItem(R.id.action_lock);
            if (isLocked()) {
                lockItem.setIcon(R.drawable.ic_lock_open_white_24dp);
                lockItem.setTitle("Unlock");
            } else {
                lockItem.setIcon(R.drawable.ic_lock_outline_white_24dp);
                lockItem.setTitle("Lock");
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_broadcast:
            case R.id.action_broadcast_faculty:
                MessageDialogFragment fragment = new MessageDialogFragment();
                fragment.setTitle("Send message");
                Bundle args = new Bundle();
                args.putInt("target", id == R.id.action_broadcast ? BROADCAST_ALL : BROADCAST_FACULTY);
                fragment.setArguments(args);
                fragment.show(getFragmentManager(), "MessageDialogFragment");
                return true;
            case R.id.action_lock:
                if (isLocked()) {
                    sendUnlock();
                } else {
                    sendLock();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void message(String message, Bundle arguments) {
        try {
            int target = arguments.getInt("target");
            JSONObject obj = new JSONObject();
            obj.put("queueName", mQueueName);
            obj.put("message", message);
            if (target == BROADCAST_ALL) {
                mSocket.emit("broadcast", obj);
            } else if (target == BROADCAST_FACULTY) {
                mSocket.emit("broadcastFaculty", obj);
            } else if (target == PRIVATE_MESSAGE) {
                obj.put("ugKthid", arguments.get("ugKthid"));
                mSocket.emit("messageUser", obj);
            } else if (target == COMMENT){
                obj.put("ugKthid", arguments.get("ugKthid"));
                mSocket.emit("flag", obj);
            }else if (target == COMPLETION){
                obj.put("ugKthid", arguments.get("ugKthid"));
                mSocket.emit("completion", obj);
            } else {
                throw new RuntimeException("message with invalid target");
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void cantFind(JSONObject student) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("user", student);
            obj.put("queueName", mQueueName);
            obj.put("type", "unknown");
            mSocket.emit("badLocation", obj);
            sendStopHelp(student);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void setupNotifications() {
        mSocket.on("join", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (mAdapter.getWaiting().size() == 0 && mAdapter.getHelpedByMe().size() == 0)
                    return;
                Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplication())
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Stay-A-While")
                        .setContentText("Someone just joined the queue " + mQueueName)
                        .setSound(alarmSound);
                Intent notificationIntent = getIntent();
                notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                PendingIntent contentIntent = PendingIntent.getActivity(getApplication(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(contentIntent);
                builder.setAutoCancel(true);
                builder.setLights(Color.BLUE, 500, 500);
                builder.setVibrate(new long[]{200, 200, 200});
                builder.setStyle(new NotificationCompat.InboxStyle());
                NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                manager.notify(mQueueName, 1, builder.build());
            }
        });
    }

    public MenuInflater getMenuInflater(JSONObject curObj) {
        this.curContextMenuObj = curObj;
        return super.getMenuInflater();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int target = -1;
        String title = "";
        final int id = item.getItemId();
        switch (id) {
            case R.id.actionRemove:
                sendKick(curContextMenuObj);
                System.out.println("kick");
                return true;
            case R.id.actionHelp:
                if (gettingHelp(curContextMenuObj)) {
                    sendStopHelp(curContextMenuObj);
                } else {
                    sendHelp(curContextMenuObj);
                    System.out.println("help");
                }
                return true;
            case R.id.actioBadLocation:
                cantFind(curContextMenuObj);
                System.out.println("bad location");
                return true;
            case R.id.actionMessage:
                target = PRIVATE_MESSAGE;
                title = "Send message";
                break;
            case R.id.actionComment:
                target = COMMENT;
                title = "Add comment";
                break;
            case R.id.actionRead:
                readComments(curContextMenuObj);
                return true;

        }
        MessageDialogFragment fragment = new MessageDialogFragment();
        fragment.setTitle(title);
        Bundle args = new Bundle();

        args.putInt("target", target);
        try {
            args.putString("ugKthid", curContextMenuObj.getString("ugKthid"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        fragment.setArguments(args);
        fragment.show(getFragmentManager(), "MessageDialogFragment");
        return true;
    }

    public  void readComments(JSONObject student){
        Bundle msg = new Bundle();;
        try {
            JSONArray messages = student.getJSONArray("messages");
            String readyString = "";
            for(int i = 0; i < messages.length(); i++){
                readyString = readyString + "\n\n" + messages.getString(i);
            }
            if(comments.has(student.getString("ugKthid"))) {
                readyString =  readyString + "\n\n" + comments.getString(student.getString("ugKthid"));
            }
            msg.putString("title", "Comments");
            msg.putString("message", readyString);
            msg.putString("sender", "");
            RecievedMessageDialogFragment fragment = new RecievedMessageDialogFragment();
            fragment.setArguments(msg);
            fragment.show(getFragmentManager(), "RecievedMessageDialogFragment");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public boolean hasComments(JSONObject student){
        try {
            return (comments.has(student.getString("ugKthid")) || (student.getJSONArray("messages").length() != 0));
            } catch (JSONException e1) {
            e1.printStackTrace();
        }
        return false;
    }

    public boolean gettingHelp(JSONObject student) {
        try {
            return student.getBoolean("gettingHelp");
        } catch (JSONException e) {
        }
        return false;
    }
}
