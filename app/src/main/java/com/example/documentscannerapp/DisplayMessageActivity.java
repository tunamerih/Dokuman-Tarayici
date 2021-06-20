package com.example.documentscannerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class DisplayMessageActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{


    private static String TAG = "MainActivity";
    CameraBridgeViewBase cameraBridgeViewBase;
    Mat mat1, mat_orig;
    BaseLoaderCallback baseLoaderCallback;
    int counter = 0;
    Bitmap testBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        Button button_send = (Button)findViewById(R.id.button_send);
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Core.rotate(mat_orig,mat_orig,Core.ROTATE_90_CLOCKWISE);
                //long addr = mat_orig.getNativeObjAddr();
                //Log.d(TAG,"mat tusa basildi");
                Bitmap testBitmap = Bitmap.createBitmap(mat_orig.width(), mat_orig.height(), Bitmap.Config.ARGB_8888);
                org.opencv.android.Utils.matToBitmap(mat_orig,testBitmap);
                //Log.d(TAG,"mat cevrildi");
                //MediaStore.Images.Media.insertImage(getContentResolver(), testBitmap, "yourTitle" , "yourDescription");
                /*
                ByteArrayOutputStream bStream = new ByteArrayOutputStream();
                testBitmap.compress(Bitmap.CompressFormat.JPEG, 20, bStream);
                byte[] byteArray = bStream.toByteArray();
                String temp = Base64.encodeToString(byteArray, Base64.DEFAULT);

                Intent intent = new Intent(DisplayMessageActivity.this,ConvertGrayscale.class);
                intent.putExtra("image",temp);
                //Log.d(TAG,temp);
                Log.d(TAG,"going");
                startActivity( intent );*/
                String filePath= tempFileImage(DisplayMessageActivity.this,testBitmap,"path");
                Intent intent = new Intent(DisplayMessageActivity.this,ConvertGrayscale.class);
                intent.putExtra("path", filePath);
                startActivity( intent );
                finish();
            }
        });

        cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.myCameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
        //cameraBridgeViewBase.enableView();
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {

                switch (status){
                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }
            }
        };

        /*
        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        // Capture the layout's TextView and set the string as its text
        TextView textView = findViewById(R.id.textView);
        textView.setText(message);
        */

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat orig = inputFrame.rgba();
        SetOnCameraView(orig);
        Mat frame = inputFrame.rgba();
        //Mat frame_trans = frame.t();
        //Core.rotate(frame,frame,2);
        /*
        Core.transpose(mat1,mat2);
        Imgproc.resize(mat2,mat3,mat1.size(),0,0,0);
        Core.flip(mat3,mat1,1);
        cameraBridgeViewBase.setMaxFrameSize(512,1024);
        */

        /* MORF İSLEMLER
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(frame, frame, new Size(5, 5), 0);
        Imgproc.threshold(frame, frame, 0, 255, Imgproc.THRESH_OTSU);
        Imgproc.Canny(frame, frame, 50,50);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
        Imgproc.morphologyEx(frame, frame, Imgproc.MORPH_CLOSE, kernel);
        */

        /*
        if (counter % 2 == 0){
            Core.flip(frame, frame, 1);
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2GRAY);
        }
        counter = counter + 1;
        */

        return frame;
    }

    public static String tempFileImage(Context context, Bitmap bitmap, String name) {

        File outputDir = context.getCacheDir();
        File imageFile = new File(outputDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(context.getClass().getSimpleName(), "Error writing file", e);
        }

        return imageFile.getAbsolutePath();
    }

    public void SetOnCameraView(Mat mat){
        mat_orig = mat;
    }

    @Override
    public void onCameraViewStopped() {
        mat1.release();

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mat1 = new Mat(width,height, CvType.CV_8UC4);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"There is a problem in OpenCv",Toast.LENGTH_SHORT).show();
        }
        else{
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }

    }



}