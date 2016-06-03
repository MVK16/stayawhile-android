package se.kth.csc.stayawhile.api;

import android.os.AsyncTask;

import java.io.IOException;

/**
 * Used to perform an API call asynchronously.
 * Optionally, a callback can be provided as an
 * <code>APICallback</code> to handle the response
 *
 * The task supports two methods
 * <ul>
 *     <li>
 *         <code>url</code>: fetch a HTTP resource given its URL
 *     </li>
 *     <li>
 *         <code>method</code>: perform an API call to the Stay-A-While HTTP API
 *         and return the content of the call
 *     </li>
 * </ul>
 */
public class APITask extends AsyncTask<String, Void, String> {

    private APICallback callback;

    public APITask() {
    }

    public APITask(APICallback callback) {
        this.callback = callback;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            if (params[0].equals("url")) {
                return API.loadURL(params[1]);
            }
            if (params[0].equals("method")) {
                return API.sendAPICall(params[1]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (this.callback != null) {
            this.callback.r(s);
        }
    }
}
