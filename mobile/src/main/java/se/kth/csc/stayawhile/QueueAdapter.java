package se.kth.csc.stayawhile;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QueueAdapter extends RecyclerView.Adapter<QueueAdapter.ViewHolder> {

    private JSONArray mDataset;
    private boolean firstInQueue = true;
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        public View mCardView;
        public JSONObject mData;

        public ViewHolder(View v) {
            super(v);
            mCardView = v;
            v.setOnCreateContextMenuListener(this);
        }

        public void setData(JSONObject data) {
            this.mData = data;
            try {
                TextView title = (TextView) mCardView.findViewById(R.id.queuee_name);
                title.setText(data.getString("realname"));
                TextView location = (TextView) mCardView.findViewById(R.id.queuee_location);
                location.setText(data.getString("location"));
                TextView comment = (TextView) mCardView.findViewById(R.id.queuee_comment);
                comment.setText(data.getString("comment"));
                TextView description = (TextView) mCardView.findViewById(R.id.queuee_description);
                if(data.getBoolean("help")) {
                    description.setText("Help");
                } else {
                    description.setText("Present");
                }

                if(firstInQueue &&!data.getBoolean("gettingHelp")){
                    title.setText("\u25B6 " + title.getText());
                    title.setTypeface(Typeface.DEFAULT_BOLD);
                    location.setTypeface(Typeface.DEFAULT_BOLD);
                    firstInQueue = false;
                } else {
                    title.setTypeface(Typeface.DEFAULT);
                    location.setTypeface(Typeface.DEFAULT);
                }
            } catch (JSONException e) {
                //TODO
            }
        }


        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.add("Remove from queue");
            menu.add("Send message");
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public QueueAdapter(JSONArray myDataset, Context context) {
        mDataset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public QueueAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.queuee, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        try {
            JSONObject queue = mDataset.getJSONObject(position);
            holder.setData(queue);
        } catch (JSONException e) {
            e.printStackTrace();
            // TODO
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length();
    }

}
