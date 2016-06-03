package se.kth.csc.stayawhile;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;

import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {

    private StudentActionListener mActionListener;
    private List<JSONObject> mWaiting;
    private List<JSONObject> mGettingHelp;
    private List<JSONObject> mGettingHelpByAssistant;
    private String mUgid;

    public List<JSONObject> getWaiting() {
        return mWaiting;
    }

    public List<JSONObject> getHelpedByMe() {
        return mGettingHelpByAssistant;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        public View mCardView;
        public JSONObject mData;

        public ViewHolder(View v) {
            super(v);
            mCardView = v;
            v.setOnCreateContextMenuListener(this);
            Button cantFind = (Button) mCardView.findViewById(R.id.queuee_action_cantfind);
            cantFind.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActionListener.cantFind(mData);
                }
            });
        }

        public void setData(JSONObject data, int position) {
            this.mData = data;
            try {
                TextView title = (TextView) mCardView.findViewById(R.id.queuee_name);
                title.setText(data.getString("realname"));
                TextView location = (TextView) mCardView.findViewById(R.id.queuee_location);
                if (data.getBoolean("badLocation")) {
                    location.setText("???");
                } else {
                    location.setText(data.getString("location"));
                }
                TextView comment = (TextView) mCardView.findViewById(R.id.queuee_comment);
                comment.setText(data.getString("comment"));
                TextView description = (TextView) mCardView.findViewById(R.id.queuee_description);
                View titleBar = mCardView.findViewById(R.id.queuee_background);
                View actions = mCardView.findViewById(R.id.queuee_actions);

                if (data.getBoolean("help")) {
                    description.setText("Help");
                } else {
                    description.setText("Present");
                }
                if (QueueAdapter.this.helpedByMe(data)) {
                    actions.setVisibility(View.VISIBLE);
                } else {
                    actions.setVisibility(View.GONE);
                }
                if (QueueAdapter.this.helpedByMe(data) || (!data.getBoolean("gettingHelp") && position == 0)) {
                    title.setText("\u25B6 " + title.getText());
                    title.setTypeface(Typeface.DEFAULT_BOLD);
                    location.setTypeface(Typeface.DEFAULT_BOLD);
                } else {
                    title.setTypeface(Typeface.DEFAULT);
                    location.setTypeface(Typeface.DEFAULT);
                }
                if (data.getBoolean("gettingHelp")) {
                    titleBar.setBackgroundColor(mCardView.getResources().getColor(R.color.colorPrimary));
                } else if (data.getBoolean("badLocation")) {
                    titleBar.setBackgroundColor(mCardView.getResources().getColor(android.R.color.holo_red_dark));
                } else {
                    titleBar.setBackgroundColor(mCardView.getResources().getColor(R.color.colorAccent));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle(((TextView) v.findViewById(R.id.queuee_name)).getText());
            mActionListener.getMenuInflater(mData).inflate(R.menu.student_context_menu, menu);

            if (mData != null) {
                MenuItem helpItem = menu.findItem(R.id.actionHelp);
                if (mActionListener.gettingHelp(mData)) {
                    helpItem.setTitle("Stop Help");
                } else {
                    helpItem.setTitle("Help");
                }
                if (!mActionListener.hasComments(mData)){
                    menu.removeItem(R.id.actionRead);
                }
            }
        }
    }

    public QueueAdapter(JSONArray myDataset, QueueActivity activity, String ugid) {
        mActionListener = activity;
        mWaiting = new ArrayList<>();
        mGettingHelp = new ArrayList<>();
        mGettingHelpByAssistant = new ArrayList<>();
        mUgid = ugid;
        for (int i = 0; i < myDataset.length(); i++) {
            try {
                JSONObject obj = myDataset.getJSONObject(i);
                System.out.println("queue add person " + obj);
                if (obj.getBoolean("gettingHelp")) {
                    System.out.println("person is getting help");
                    if (obj.has("helper")) {
                        System.out.println("with helper " + obj.getString("helper"));
                    }
                    if (obj.has("helper") && obj.getString("helper").equals(mUgid)) {
                        mGettingHelpByAssistant.add(obj);
                    } else {
                        mGettingHelp.add(obj);
                    }
                } else {
                    mWaiting.add(obj);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        Comparator<JSONObject> studentComparator = new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject jsonObject, JSONObject t1) {
                try {
                    return Long.compare(jsonObject.getLong("time"), t1.getLong("time"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return 0;
            }
        };
        Collections.sort(mWaiting, studentComparator);
        Collections.sort(mGettingHelp, studentComparator);
        Collections.sort(mGettingHelpByAssistant, studentComparator);
    }

    public boolean helpedByMe(JSONObject user) {
        try {
            return user.getBoolean("gettingHelp") && user.has("helper") && user.getString("helper").equals(mUgid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int positionOf(String ugKthid) {
        try {
            for (int i = 0; i < getItemCount(); i++) {
                if (onPosition(i).getString("ugKthid").equals(ugKthid)) {
                    return i;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public JSONObject onPosition(int pos) {
        if (pos < mGettingHelpByAssistant.size()) return mGettingHelpByAssistant.get(pos);
        pos -= mGettingHelpByAssistant.size();
        if (pos < mWaiting.size()) return mWaiting.get(pos);
        pos -= mWaiting.size();
        return mGettingHelp.get(pos);
    }

    public void set(int pos, JSONObject user) {
        if (pos < mGettingHelpByAssistant.size()) mGettingHelpByAssistant.set(pos, user);
        else if (pos < mGettingHelpByAssistant.size() + mWaiting.size())
            mWaiting.set(pos - mGettingHelpByAssistant.size(), user);
        else mGettingHelp.set(pos - mGettingHelpByAssistant.size() - mWaiting.size(), user);
        notifyItemChanged(pos);
    }

    public void add(JSONObject person) {
        try {
            if (person.getBoolean("gettingHelp")) {
                if (person.has("helper") && person.getString("helper").equals(mUgid)) {
                    notifyItemInserted(insertInto(mGettingHelpByAssistant, person));
                } else {
                    notifyItemInserted(mGettingHelpByAssistant.size() + mWaiting.size() + insertInto(mGettingHelp, person));
                }
            } else {
                int pos = mGettingHelpByAssistant.size() + insertInto(mWaiting, person);
                notifyItemInserted(pos);
            }
            if (getItemCount() > 1) notifyItemChanged(1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int insertInto(List<JSONObject> queue, JSONObject person) {
        try {
            int at = 0;
            if (person.getBoolean("badLocation")) {
                at = queue.size();
            } else {
                while (at < queue.size() && queue.get(at).getLong("time") < person.getLong("time"))
                    at++;
            }
            queue.add(at, person);
            return at;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void removePosition(int pos) {
        if (pos < mGettingHelpByAssistant.size()) mGettingHelpByAssistant.remove(pos);
        else if (pos < mGettingHelpByAssistant.size() + mWaiting.size())
            mWaiting.remove(pos - mGettingHelpByAssistant.size());
        else mGettingHelp.remove(pos - mGettingHelpByAssistant.size() - mWaiting.size());
        notifyItemRemoved(pos);
        if (getItemCount() > 0) notifyItemChanged(0);
    }

    public boolean isWaiting(int position) {
        return mGettingHelpByAssistant.size() <= position && position < mGettingHelpByAssistant.size() + mWaiting.size();
    }

    @Override
    public int getItemCount() {
        return mGettingHelpByAssistant.size() + mWaiting.size() + mGettingHelp.size();
    }

    @Override
    public QueueAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.queuee, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        JSONObject queue = onPosition(position);
        holder.setData(queue, position);
    }

    public interface StudentActionListener {
        void cantFind(JSONObject student);
        boolean gettingHelp(JSONObject data);
        MenuInflater getMenuInflater(JSONObject data);
        boolean hasComments(JSONObject student);
    }
}
