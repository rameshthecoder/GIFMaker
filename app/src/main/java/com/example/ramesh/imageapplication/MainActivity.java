package com.example.ramesh.imageapplication;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    private static final int NUMBER_OF_FRAMES = 2;
    TextView testView;

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;

    Button clickButton;
    PictureCallback rawCallback;
    ShutterCallback shutterCallback;
    PictureCallback jpegCallback;
    int orientation;
    int photoCount;
    private ArrayList<Bitmap> bitmapList;
    private ArrayList<String> imagePathList = new ArrayList<String>();
    private BitmapDrawable bitmapDrawable;

    AnimatedGifEncoder encoder;
    ByteArrayOutputStream outputStream;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        encoder = new AnimatedGifEncoder();
        outputStream = new ByteArrayOutputStream();
        encoder.start(outputStream);
        photoCount = 0;
        bitmapList = new ArrayList<Bitmap>();
        clickButton = (Button) findViewById(R.id.clickButton);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        //        surfaceView.setBackground(getResources().getDrawable(android.R.drawable.btn_radio));
        surfaceHolder = surfaceView.getHolder();

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        surfaceHolder.addCallback(this);

        // deprecated setting, but required on Android versions prior to 3.0
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        clickButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    captureImage(v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        jpegCallback = new PictureCallback() {
            public void onPictureTaken(byte[] data, Camera camera) {
//                FileOutputStream outStream = null;
//                try {
//                    outStream = new FileOutputStream(String.format("/sdcard/%d.jpg", System.currentTimeMillis()));
//                    outStream.write(data);
//                    outStream.close();
//                    Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } finally {
//                }
//
//
//                Toast.makeText(getApplicationContext(), "Picture Saved", Toast.LENGTH_LONG).show();
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                bitmap = rotateBitmap(bitmap);
                setSurfaceBG(bitmap);
                refreshCamera();

//                bitmapList.add(bitmap);
                imagePathList.add(writeToFile(bitmap));
                photoCount++;
                if (photoCount == NUMBER_OF_FRAMES) {
                    camera.stopPreview();
                    ImageTask imageTask = new ImageTask();
                    imageTask.execute();
                }
            }
        };
    }

    public void setSurfaceBG(Bitmap bmp) {
        bitmapDrawable = new BitmapDrawable(bmp);
        bitmapDrawable.setAlpha(80);
        surfaceView.setBackground(bitmapDrawable);
    }


    public Bitmap rotateBitmap(Bitmap bmp) {
        Bitmap bitmapRotate = null;
        if (bmp.getHeight() < bmp.getWidth()) {
            orientation = 90;
        } else {
            orientation = 0;
        }
        if (orientation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            bitmapRotate = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(),
                    bmp.getHeight(), matrix, true);
        } else
            bitmapRotate = Bitmap.createScaledBitmap(bmp, bmp.getWidth(),
                    bmp.getHeight(), true);
        return bitmapRotate;
    }

    private class ImageTask extends AsyncTask<Bitmap, Void, Integer> {

        @Override
        protected void onPreExecute() {
            Toast.makeText(MainActivity.this, "Saving", Toast.LENGTH_LONG).show();
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Bitmap... params) {
//            for (Bitmap bitmap : bitmapList) {
//                encoder.addFrame(bitmap);
//            }
//            bitmapList = null;
//            System.gc();
            for(String filePath : imagePathList) {
//                encoder.addFrame(getBitmapFromFile(filePath));
                BitmapTask bitmapTask = new BitmapTask();
                bitmapTask.execute(filePath);
            }
//            writeToFile(outputStream.toByteArray());
            return 0;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            encoder.finish();
            writeGIFToFile(outputStream.toByteArray());
            Toast.makeText(MainActivity.this, "File saved", Toast.LENGTH_LONG).show();
        }
    }

    private class BitmapTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            String filePath = params[0];
            BitmapFactory bitmapFactory = new BitmapFactory();
            encoder.addFrame(bitmapFactory.decodeFile(filePath));
            return null;
        }
    }

    public String writeGIFToFile(byte[] data) {
//        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//        byte[] data = stream.toByteArray();
        FileOutputStream outStream = null;
        String filePath = null;
        try {
            filePath = String.format("/sdcard/%d.gif", System.currentTimeMillis());
            outStream = new FileOutputStream(filePath);
            outStream.write(data);
            outStream.close();
//            Toast.makeText(this, "Writing file", Toast.LENGTH_SHORT).show();
            Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return filePath;
    }

    public String writeToFile(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();
        FileOutputStream outStream = null;
        String filePath = null;
        try {
            filePath = String.format("/sdcard/%d.jpg", System.currentTimeMillis());
            outStream = new FileOutputStream(filePath);
            outStream.write(data);
            outStream.close();
//            Toast.makeText(this, "Writing file", Toast.LENGTH_SHORT).show();
            Log.d("Log", "onPictureTaken - wrote bytes: " + data.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return filePath;
    }

    public void captureImage(View v) throws IOException {
        //take the picture
        camera.takePicture(null, null, jpegCallback);
    }

    public void refreshCamera() {
        if (surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            camera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here
        // start preview with new settings
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {

        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        refreshCamera();
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            // open the camera
            camera = Camera.open();
            camera.setDisplayOrientation(90);
        } catch (RuntimeException e) {
            // check for exceptions
            System.err.println(e);
            return;
        }
        Camera.Parameters param;
        param = camera.getParameters();

        // modify parameter
        param.setPreviewSize(352, 288);
        param.set("orientation", "portrait");
        camera.setParameters(param);
        List<Camera.Size> sizes = param.getSupportedPictureSizes();
        Camera.Size size = sizes.get(0);
        for(int i=0;i<sizes.size();i++)
        {
            if(sizes.get(i).width < size.width)
                size = sizes.get(i);
        }
        param.setPictureSize(size.width, size.height);

        try {
            // The Surface has been created, now tell the camera where to draw
            // the preview.
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (Exception e) {
            // check for exceptions
            System.err.println(e);
            return;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // stop preview and release camera
        camera.stopPreview();
        camera.release();
        camera = null;
    }
}