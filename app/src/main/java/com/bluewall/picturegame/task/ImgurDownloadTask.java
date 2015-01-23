package com.bluewall.picturegame.task;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@SuppressLint("NewApi")
public class ImgurDownloadTask extends AsyncTask<Void, Void, Bitmap> {

    private String url;

    public ImgurDownloadTask(String url) {
        this.url = url;
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            return BitmapFactory.decodeStream((InputStream) new URL(url).getContent());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
