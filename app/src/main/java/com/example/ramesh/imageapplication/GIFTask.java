package com.example.ramesh.imageapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by ramesh on 27/4/16.
 */
public class GIFTask extends AsyncTask<ArrayList<Bitmap>, Void, byte[]> {

    AnimatedGifEncoder encoder;
    Context context;

    @Override
    protected byte[] doInBackground(ArrayList<Bitmap>... params) {
        encoder = new AnimatedGifEncoder();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        encoder.start(outputStream);
        for(Bitmap bitmap : params[0]) {
            encoder.addFrame(bitmap);
        }
        encoder.finish();
        return outputStream.toByteArray();
    }

    @Override
    protected void onPostExecute(byte[] data) {
//        super.onPostExecute(data);
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//        byte[] data = stream.toByteArray();
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(String.format("/sdcard/%d.gif", System.currentTimeMillis()));
            outStream.write(data);
            outStream.close();
            Toast.makeText(context, "Writing file", Toast.LENGTH_SHORT).show();
            Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }

    }

    public void setContext(Context context) {
        this.context = context;
    }
}
