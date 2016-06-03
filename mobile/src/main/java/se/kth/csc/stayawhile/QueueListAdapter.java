package se.kth.csc.stayawhile;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import se.kth.csc.stayawhile.api.APICallback;
import se.kth.csc.stayawhile.api.APITask;

public class QueueListAdapter extends RecyclerView.Adapter<QueueListAdapter.ViewHolder> {

    private JSONObject mUserData;
    private QueueListActivity activity;
    private List<JSONObject> mAssistantQueue;
    private List<JSONObject> mOtherQueues;

    public class ViewHolder extends RecyclerView.ViewHolder {

        public View mCardView;
        public JSONObject mData;

        public ViewHolder(View v) {
            super(v);
            mCardView = v;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (isAssistant(mData)) {
                            Intent intent = new Intent(activity, QueueActivity.class);
                            intent.putExtra("queue", mData.getString("name"));
                            QueueListAdapter.this.activity.startActivity(intent);
                        } else {
                            new APITask(new APICallback() {
                                @Override
                                public void r(String result) {
                                    Bundle args = new Bundle();
                                    args.putString("queue", result);
                                    QueueAccessRequestDialogFragment q = new QueueAccessRequestDialogFragment();
                                    q.setArguments(args);
                                    q.show(activity.getFragmentManager(), "QueueAccessRequestDialogFragment");
                                }
                            }).execute("method", "queue/" + Uri.encode(mData.getString("name")));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public void setData(JSONObject data) {
            this.mData = data;
            try {
                TextView title = (TextView) mCardView.findViewById(R.id.queue_name);
                title.setText(data.getString("name"));
                TextView length = (TextView) mCardView.findViewById(R.id.queue_people);
                length.setText(String.valueOf(data.getInt("length")));
                View titlebar = mCardView.findViewById(R.id.queue_titlebar);
                ImageView icon = (ImageView) mCardView.findViewById(R.id.queue_icon);
                if (isAssistant(data)) {
                    titlebar.setBackgroundColor(activity.getResources().getColor(R.color.colorPrimary));
                    title.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    titlebar.setBackgroundColor(activity.getResources().getColor(android.R.color.darker_gray));
                    title.setTypeface(Typeface.DEFAULT);
                }
                if (data.getBoolean("locked")) {
                    icon.setImageResource(R.drawable.ic_lock_white_18dp);
                    length.setVisibility(View.GONE);
                } else {
                    icon.setImageResource(R.drawable.ic_people_outline_white_18dp);
                    length.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public QueueListAdapter(JSONArray queues, QueueListActivity context, JSONObject userData) {
        mUserData = userData;
        mAssistantQueue = new ArrayList<>();
        mOtherQueues = new ArrayList<>();
        for (int i = 0; i < queues.length(); i++) {
            try {
                JSONObject queue = queues.getJSONObject(i);
                // if (queue.getBoolean("hiding")) continue;
                if (isAssistant(queue)) {
                    mAssistantQueue.add(queue);
                } else {
                    mOtherQueues.add(queue);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        final Comparator<JSONObject> nameComparator = new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject lhs, JSONObject rhs) {
                try {
                    return lhs.getString("name").compareTo(rhs.getString("name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        };
        Collections.sort(mAssistantQueue, nameComparator);
        Collections.sort(mOtherQueues, nameComparator);
        activity = context;
    }

    public boolean isAssistant(JSONObject queue) {
        try {
            String queueName = queue.getString("name");
            JSONArray assistants = mUserData.getJSONArray("assistant");
            JSONArray teachers = mUserData.getJSONArray("teacher");
            for (int i = 0; i < assistants.length(); i++) {
                if (assistants.getString(i).equals(queueName)) {
                    return true;
                }
            }
            for (int i = 0; i < teachers.length(); i++) {
                if (teachers.getString(i).equals(queueName)) {
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public JSONObject onPosition(int position) {
        if (position < mAssistantQueue.size()) {
            return mAssistantQueue.get(position);
        }
        return mOtherQueues.get(position - mAssistantQueue.size());
    }

    @Override
    public QueueListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.queue, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JSONObject queue = onPosition(position);
        holder.setData(queue);
    }

    @Override
    public int getItemCount() {
        return mAssistantQueue.size() + mOtherQueues.size();
    }

}
