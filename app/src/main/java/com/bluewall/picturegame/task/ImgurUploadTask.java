package com.bluewall.picturegame.task;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.bluewall.picturegame.login.ImgurAuthorization;

import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@SuppressLint("NewApi")
public class ImgurUploadTask extends AsyncTask<Void, Void, String> {

    private static final String TAG = "ImgurUploadTask";

    private static final String UPLOAD_URL = "https://api.imgur.com/3/image";

    private Activity mActivity;
    private Uri mImageUri;  // local Uri to upload
    ProgressDialog myPd_ring;

    public ImgurUploadTask(Uri imageUri, Activity activity) {
        this.mImageUri = imageUri;
        this.mActivity = activity;
    }

    @Override
    protected void onPreExecute() {

        myPd_ring = new ProgressDialog(mActivity);
        myPd_ring.setMessage("Saving Image");
        myPd_ring.show();

    }

    @Override
    protected String doInBackground(Void... params) {
        InputStream imageIn;
        Log.i("URI",mImageUri.toString());
        try {
            imageIn = mActivity.getContentResolver().openInputStream(mImageUri);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "could not open InputStream", e);
            return null;
        }

        HttpURLConnection conn = null;
        InputStream responseIn = null;

        try {
            conn = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();
            conn.setDoOutput(true);

            ImgurAuthorization.getInstance().addToHttpURLConnection(conn);

            OutputStream out = conn.getOutputStream();
            copy(imageIn, out);
            out.flush();
            out.close();

            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                responseIn = conn.getInputStream();
                myPd_ring.dismiss();
                return onInput(responseIn);
            } else {
                Log.i(TAG, "responseCode=" + conn.getResponseCode());
                responseIn = conn.getErrorStream();
                StringBuilder sb = new StringBuilder();
                Scanner scanner = new Scanner(responseIn);
                while (scanner.hasNext()) {
                    sb.append(scanner.next());
                }
                Log.i(TAG, "error response: " + sb.toString());
                myPd_ring.dismiss();
                return null;
            }
        } catch (Exception ex) {
            Log.e(TAG, "Error during POST", ex);
            return null;
        } finally {
            try {
                responseIn.close();
            } catch (Exception ignore) {
            }
            try {
                conn.disconnect();
            } catch (Exception ignore) {
            }
            try {
                imageIn.close();
            } catch (Exception ignore) {
            }
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        myPd_ring.dismiss();
    }

    private static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[8192];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    protected String onInput(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        Scanner scanner = new Scanner(in);
        while (scanner.hasNext()) {
            sb.append(scanner.next());
        }

        JSONObject root = new JSONObject(sb.toString());
        String id = root.getJSONObject("data").getString("id");
        String deletehash = root.getJSONObject("data").getString("deletehash");

        Log.i(TAG, "new imgur url: http://imgur.com/" + id + " (delete hash: " + deletehash + ")");
        return id;
    }

}