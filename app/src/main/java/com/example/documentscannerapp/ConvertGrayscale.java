package com.example.documentscannerapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.math.MathUtils;
import com.google.android.material.slider.Slider;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;

public class ConvertGrayscale extends AppCompatActivity  {

    static public  float TO_DEGREES = (1 / (float) Math.PI) * 180;
    ImageView imageView;
    Uri imageUri;
    Bitmap grayBitmap, imageBitmap, bm3;
    Bitmap theBitmap ;
    int image_has_changed = 0;
    int image_has_converted = 0;
    private static String TAG = "MainActivity";
    private static String TAG2 = "intent Activity";
    private static final int MAX_HEIGHT = 500;
	private int warpedHeight = 0;
	private int warpedWidth = 0;
	Slider slider;
    long addr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "im here");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert_grayscale);
        imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setDrawingCacheEnabled(true);
        slider = findViewById(R.id.thresh_slider);

        if(getIntent().getStringExtra("path")!=null){
            //gets the file path
            String filePath=getIntent().getStringExtra("path");

            //loads the file
            File file = new File(filePath);
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            imageView.setImageBitmap(bitmap);
            image_has_changed=-1;
        }

        /*
        Bundle bundle = getIntent().getExtras();
        String message = bundle.getString("image");

        if(getIntent().hasExtra("image")){
            /*
            addr = getIntent().getLongExtra("image", 0);

            String s = String.valueOf(addr);
            Log.d(TAG, "long: "+s);
            Mat tempImg = new Mat( addr );
            Size le_size = tempImg.size();
            s = String.valueOf(le_size);
            Log.d(TAG, "size: "+s);

            Bitmap bm3 = Bitmap.createBitmap(tempImg.width(), tempImg.height(), Bitmap.Config.ARGB_8888);
            Log.d(TAG,"mat yuklendi");
            org.opencv.android.Utils.matToBitmap(tempImg,bm3);
            Log.d(TAG,"mat cevrildi");

            Log.d(TAG,"bitmap geldi");
            byte[] byteArray = getIntent().getByteArrayExtra("image");
            //Log.d(TAG,byteArray.toString());
            Log.d(TAG,"bytearray alindi");
            try{
                bm3 = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                Log.d(TAG,"bm3 acildi");
                MediaStore.Images.Media.insertImage(getContentResolver(), bm3, "yourTitle" , "yourDescription");
                Log.d(TAG,"mat kaydedildi");
                //SetImageFromIntent(img);
                imageView.setImageBitmap(bm3);
            }
            catch (Exception e){
                Log.d(TAG,e.toString());
            }

        }*/

    }
/*
    public void SetImageFromIntent(Mat img){
        //imageView.destroyDrawingCache();

        //converting



    }*/

    public void openGallery(View v){
        Intent myIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(myIntent,100);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100 && resultCode==RESULT_OK && data!=null){
            Uri imageUri = data.getData();
            try{
                imageView.destroyDrawingCache();
                imageBitmap= MediaStore.Images.Media.getBitmap(getContentResolver(),imageUri);
                imageView.setImageBitmap(imageBitmap);
                image_has_changed++;
            }
            catch (IOException e){
                e.printStackTrace();
            }
            //imageView.setImageURI(imageUri);

        }
    }

    public void convertToGray(View v){
        if(image_has_changed < 0){
            Log.d(TAG,"kucuktur sifir");
            Mat originMat = new Mat();
            Mat grayMat = new Mat();
            Log.d(TAG,"kucuktur sifir 2");
            //Bitmap theBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(imageView.getDrawingCache(),grayMat);
            Utils.bitmapToMat(imageView.getDrawingCache(),originMat);
            //org.opencv.android.Utils.bitmapToMat(theBitmap, grayMat);
            //org.opencv.android.Utils.bitmapToMat(theBitmap, originMat);
            Log.d(TAG,"kucuktur sifir 3");
            Log.d(TAG,grayMat.toString());
            Bitmap bm = Bitmap.createBitmap(grayMat.width(), grayMat.height(), Bitmap.Config.ARGB_8888);

            Imgproc.cvtColor(grayMat, grayMat, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(grayMat, grayMat, new Size(5, 5), 0);
            Imgproc.threshold(grayMat, grayMat, 0, 255, Imgproc.THRESH_OTSU);
            Imgproc.Canny(grayMat, grayMat, 50,50);
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
            Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_CLOSE, kernel);
            MatOfPoint2f mat1 = findLargestContour(grayMat);

            Point[] sortedPoints = SortPoints2(mat1.toList());
            ArrayList<Point> theList = new ArrayList<>();
            theList.add(sortedPoints[0]);
            theList.add(sortedPoints[1]);
            theList.add(sortedPoints[2]);
            theList.add(sortedPoints[3]);

			/*// resized image width and height old
			warpedHeight = (int)sortedPoints[2].y - (int)sortedPoints[1].y + 150;
			warpedWidth = (int)sortedPoints[1].x - (int)sortedPoints[0].x + 100;*/

            // resized image width and height new
            warpedHeight = 1024;
            warpedWidth = 512;

            Mat srcPointsMat = Converters.vector_Point_to_Mat(theList, CvType.CV_32F);
            List<Point> dstPoints = new ArrayList<>();
            dstPoints.add(new Point(0, 0));
            dstPoints.add(new Point(warpedWidth, 0));
            dstPoints.add(new Point(warpedWidth, warpedHeight));
            dstPoints.add(new Point(0, warpedHeight));

            Mat dstPointsMat = Converters.vector_Point_to_Mat(dstPoints, CvType.CV_32F);

            //make perspective transform
            Mat M = Imgproc.getPerspectiveTransform(srcPointsMat, dstPointsMat);
            Mat warpedMat = new Mat(grayMat.size(), grayMat.type());
            //crop and warp the image
            Imgproc.warpPerspective(originMat, warpedMat, M, new Size(warpedWidth, warpedHeight), Imgproc.INTER_LINEAR);
            warpedMat.convertTo(grayMat, CvType.CV_8UC3);
            Mat warpedMat2 = new Mat();
            warpedMat.copyTo(warpedMat2);
            Imgproc.cvtColor(warpedMat2, warpedMat2, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(warpedMat2, warpedMat2, new Size(5, 5), 0);
            Imgproc.adaptiveThreshold(warpedMat2, warpedMat2, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY, 21, slider.getValue() );
            Log.d(TAG,"E");
            Bitmap bm2 = Bitmap.createBitmap(warpedMat2.width(), warpedMat2.height(), Bitmap.Config.ARGB_8888);
            //converting
            org.opencv.android.Utils.matToBitmap(warpedMat2, bm2);
            Log.d(TAG,"Converted.");
            /*
            try {
                MediaStore.Images.Media.insertImage(getContentResolver(), bm2, "yourTitle" , "yourDescription");
            }catch (Exception e){
                Log.d(TAG,e.toString());
            }
                */
            image_has_converted++;
            imageView.setImageBitmap(bm2);
            Log.d(TAG,"F");
            

        }

        else if(image_has_changed > 0){
            imageView.destroyDrawingCache();
            Mat originMat = new Mat();
            Mat grayMat = new Mat();

            org.opencv.android.Utils.bitmapToMat(imageBitmap, grayMat);
            org.opencv.android.Utils.bitmapToMat(imageBitmap, originMat);
            Log.d(TAG,grayMat.toString());
            Bitmap bm = Bitmap.createBitmap(grayMat.width(), grayMat.height(), Bitmap.Config.ARGB_8888);

            Imgproc.cvtColor(grayMat, grayMat, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(grayMat, grayMat, new Size(5, 5), 0);
            Imgproc.threshold(grayMat, grayMat, 0, 255, Imgproc.THRESH_OTSU);
            Imgproc.Canny(grayMat, grayMat, 50,50);
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
            Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_CLOSE, kernel);
            MatOfPoint2f mat1 = findLargestContour(grayMat);

            Point[] sortedPoints = SortPoints2(mat1.toList());
            ArrayList<Point> theList = new ArrayList<>();
            theList.add(sortedPoints[0]);
            theList.add(sortedPoints[1]);
            theList.add(sortedPoints[2]);
            theList.add(sortedPoints[3]);

			/*// resized image width and height old
			warpedHeight = (int)sortedPoints[2].y - (int)sortedPoints[1].y + 150;
			warpedWidth = (int)sortedPoints[1].x - (int)sortedPoints[0].x + 100;*/

            // resized image width and height new
            warpedHeight = 1024;
            warpedWidth = 512;

            Mat srcPointsMat = Converters.vector_Point_to_Mat(theList, CvType.CV_32F);
            List<Point> dstPoints = new ArrayList<>();
            dstPoints.add(new Point(0, 0));
            dstPoints.add(new Point(warpedWidth, 0));
            dstPoints.add(new Point(warpedWidth, warpedHeight));
            dstPoints.add(new Point(0, warpedHeight));

            Mat dstPointsMat = Converters.vector_Point_to_Mat(dstPoints, CvType.CV_32F);

            //make perspective transform
            Mat M = Imgproc.getPerspectiveTransform(srcPointsMat, dstPointsMat);
            Mat warpedMat = new Mat(grayMat.size(), grayMat.type());
            //crop and warp the image
            Imgproc.warpPerspective(originMat, warpedMat, M, new Size(warpedWidth, warpedHeight), Imgproc.INTER_LINEAR);
            warpedMat.convertTo(grayMat, CvType.CV_8UC3);
            Mat warpedMat2 = new Mat();
            warpedMat.copyTo(warpedMat2);
            Imgproc.cvtColor(warpedMat2, warpedMat2, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(warpedMat2, warpedMat2, new Size(5, 5), 0);
            Imgproc.adaptiveThreshold(warpedMat2, warpedMat2, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY, 21, slider.getValue() );
            Log.d(TAG,"E");
            Bitmap bm2 = Bitmap.createBitmap(warpedMat2.width(), warpedMat2.height(), Bitmap.Config.ARGB_8888);
            //converting
            org.opencv.android.Utils.matToBitmap(warpedMat2, bm2);
            Log.d(TAG,"Converted.");
            /*
            try {
                MediaStore.Images.Media.insertImage(getContentResolver(), bm2, "yourTitle" , "yourDescription");
            }catch (Exception e){
                Log.d(TAG,e.toString());
            }
                */
            image_has_converted++;
            imageView.setImageBitmap(bm2);
            Log.d(TAG,"F");


        }
        else{ Toast.makeText(getApplicationContext(),"Bir resim seçmediniz.",Toast.LENGTH_SHORT).show(); }

    }

    public void convertToColor(View v){
        if(image_has_changed != 0){
            imageView.destroyDrawingCache();
            Mat originMat = new Mat();
            Mat grayMat = new Mat();

            org.opencv.android.Utils.bitmapToMat(imageBitmap, grayMat);
            org.opencv.android.Utils.bitmapToMat(imageBitmap, originMat);
            Log.d(TAG,grayMat.toString());
            Bitmap bm = Bitmap.createBitmap(grayMat.width(), grayMat.height(), Bitmap.Config.ARGB_8888);

            Imgproc.cvtColor(grayMat, grayMat, Imgproc.COLOR_BGR2GRAY);
            Imgproc.GaussianBlur(grayMat, grayMat, new Size(5, 5), 0);
            Imgproc.threshold(grayMat, grayMat, 0, 255, Imgproc.THRESH_OTSU);
            Imgproc.Canny(grayMat, grayMat, 50,50);
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(5, 5));
            Imgproc.morphologyEx(grayMat, grayMat, Imgproc.MORPH_CLOSE, kernel);
            MatOfPoint2f mat1 = findLargestContour(grayMat);

            Point[] sortedPoints = SortPoints2(mat1.toList());
            ArrayList<Point> theList = new ArrayList<>();
            theList.add(sortedPoints[0]);
            theList.add(sortedPoints[1]);
            theList.add(sortedPoints[2]);
            theList.add(sortedPoints[3]);

			/*// resized image width and height old
			warpedHeight = (int)sortedPoints[2].y - (int)sortedPoints[1].y + 150;
			warpedWidth = (int)sortedPoints[1].x - (int)sortedPoints[0].x + 100;*/

            // resized image width and height new
            warpedHeight = 1024;
            warpedWidth = 512;

            Mat srcPointsMat = Converters.vector_Point_to_Mat(theList, CvType.CV_32F);
            List<Point> dstPoints = new ArrayList<>();
            dstPoints.add(new Point(0, 0));
            dstPoints.add(new Point(warpedWidth, 0));
            dstPoints.add(new Point(warpedWidth, warpedHeight));
            dstPoints.add(new Point(0, warpedHeight));

            Mat dstPointsMat = Converters.vector_Point_to_Mat(dstPoints, CvType.CV_32F);

            //make perspective transform
            Mat M = Imgproc.getPerspectiveTransform(srcPointsMat, dstPointsMat);
            Mat warpedMat = new Mat(grayMat.size(), grayMat.type());
            //crop and warp the image
            Imgproc.warpPerspective(originMat, warpedMat, M, new Size(warpedWidth, warpedHeight), Imgproc.INTER_LINEAR);
            warpedMat.convertTo(grayMat, CvType.CV_8UC3);
            Mat warpedMat2 = new Mat();
            warpedMat.copyTo(warpedMat2);
            //Imgproc.cvtColor(warpedMat2, warpedMat2, Imgproc.COLOR_BGR2GRAY);
            //Imgproc.GaussianBlur(warpedMat2, warpedMat2, new Size(5, 5), 0);
            //Imgproc.adaptiveThreshold(warpedMat2, warpedMat2, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C,Imgproc.THRESH_BINARY, 21, 7);
            Log.d(TAG,"E");
            Bitmap bm2 = Bitmap.createBitmap(warpedMat2.width(), warpedMat2.height(), Bitmap.Config.ARGB_8888);
            //converting
            org.opencv.android.Utils.matToBitmap(warpedMat2, bm2);
            Log.d(TAG,"Converted.");
            /*
            try {
                MediaStore.Images.Media.insertImage(getContentResolver(), bm2, "yourTitle" , "yourDescription");
            }catch (Exception e){
                Log.d(TAG,e.toString());
            }
                */
            image_has_converted++;
            imageView.setImageBitmap(bm2);
            Log.d(TAG,"F");


        }
        else{ Toast.makeText(getApplicationContext(),"Bir resim seçmediniz.",Toast.LENGTH_SHORT).show(); }

    }


    public void SaveToGallery(View v){
        if(image_has_converted != 0){
            try {
                Bitmap bmap = imageView.getDrawingCache();
                MediaStore.Images.Media.insertImage(getContentResolver(), bmap, "yourTitle" , "yourDescription");
                Log.d(TAG,"Saved to Gallery.");
                Toast.makeText(getApplicationContext(),"Başarıyla galeriye kaydedildi.",Toast.LENGTH_SHORT).show();
            }catch (Exception e){
                Log.d(TAG,e.toString());
            }
        }
    }

    static private Point[] SortPoints2(List<Point> source) {
        ArrayList<Point> theList = new ArrayList<>();
        ArrayList<Point> source2 = new ArrayList<>(source);
        Point[] result = {null, null, null, null};
        Point originPoint = new Point(0,0);
        Point largestPoing = new Point(1000000,1000000);
        ArrayList<Integer> listOne = new ArrayList<>(Arrays.asList(0,1,2,3));
        ArrayList<Integer> listTwo = new ArrayList<>(Arrays.asList());

        // sol-üst
        double smallest = 100000000;
        int theIndex = 0;
        for(int i=0;i<source.size();i++) {
            double theDistance = GetDistance(source.get(i),originPoint);
            if(theDistance < smallest) {
                smallest = theDistance;
                theIndex = i;
            }
        }
        theList.add(0,source.get(theIndex));
        result[0] = source.get(theIndex);

        listTwo.add(theIndex);

        // sag-alt
        double largest = 0;
        int theIndex1 = 0;
        for(int i=0;i<source.size();i++) {
            double theDistance = GetDistance(source.get(i),originPoint);
            if(theDistance > largest) {
                largest = theDistance;
                theIndex1 = i;
            }
        }
        theList.add(1,source.get(theIndex1));
        result[2] = source.get(theIndex1);
        listTwo.add(theIndex1);
        //sag-ust
        Point thePoint;
        System.out.println(source2);
        source2.remove(theIndex);
        smallest = 100000000;
        int theIndex2=0;
        for(int i=0;i<source2.size();i++) {
            double theDistance = GetDistance(source.get(theIndex),source2.get(i));
            System.out.println(theDistance);
            if(theDistance < smallest) {
                smallest = theDistance;
                thePoint = source2.get(i);
                theIndex2 = source.indexOf(thePoint);
            }
        }
        theList.add(2,source.get(theIndex2));
        result[1] = source.get(theIndex2);
        listTwo.add(theIndex2);
        //sol-alt
        listOne.removeAll(listTwo);
        result[3] =source.get(listOne.get(0));
        return result;

    }


    static private double GetDistance(Point a, Point b) {
        double distance = Math.sqrt(Math.pow((a.x-b.x), 2)+ Math.pow((a.y-b.y),2));
        return distance;
    }

    private MatOfPoint2f findLargestContour(Mat src) {
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(src, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        // Get the 5 largest contours
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                double area1 = Imgproc.contourArea(o1);
                double area2 = Imgproc.contourArea(o2);
                return (int) (area2 - area1);
            }
        });
        if (contours.size() > 5) contours.subList(4, contours.size() - 1).clear();

        MatOfPoint2f largest = null;
        for (MatOfPoint contour : contours) {
            MatOfPoint2f approx = new MatOfPoint2f();
            MatOfPoint2f c = new MatOfPoint2f();
            contour.convertTo(c, CvType.CV_32FC2);
            Imgproc.approxPolyDP(c, approx, Imgproc.arcLength(c, true) * 0.02, true);

            if (approx.total() == 4 && Imgproc.contourArea(contour) > 150) {
                // the contour has 4 points, it's valid
                largest = approx;
                break;
            }
        }

        return largest;
    }




}