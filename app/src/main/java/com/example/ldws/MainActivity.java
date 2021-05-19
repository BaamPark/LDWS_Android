package com.example.ldws;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.*;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;







public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    CameraBridgeViewBase cameraBridgeViewBase;
    BaseLoaderCallback baseLoaderCallback;
    boolean startCanny = false;
    double width;
    double height;




    public Mat Canny(Mat img) {
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2GRAY);
        Imgproc.GaussianBlur(img, img, new Size(5, 5), 0);
        Imgproc.medianBlur(img,img,3);
        Imgproc.Canny(img, img, 100, 200);
        return img;
    }

    public Mat color_filter(Mat img, Mat src) {
        Imgproc.cvtColor(img, img, Imgproc.COLOR_RGB2HSV);
        Scalar white_lower = new Scalar(0,0,0);
        Scalar white_upper = new Scalar(0,0,255);
        Scalar yellow_lower = new Scalar(22,93,0);
        Scalar yellow_upper = new Scalar(45,255,255);
        Mat dstW = new Mat();
        Mat dstY = new Mat();
        Core.inRange(img, white_lower, white_upper, dstW);
        Core.inRange(img, yellow_lower, yellow_upper, dstY);
        Mat dstWY = new Mat();
        Core.bitwise_or(dstW, dstY, dstWY);
//        Mat dst = new Mat();
//        Core.bitwise_and(src, dstY, dst);
        src.copyTo(img, dstWY); //bitwise_and가 안먹혀서 copyTo로 대체
        return img;
    }

    public void ROI( Mat img, double w, double h) {

        Mat mask = Mat.zeros(img.rows(), img.cols(), img.type());


        Point[] rook_points = new Point[4];
        rook_points[0] = new Point(0, h * 1.0); //start drawing from 0 to 1 to 2 to3
        rook_points[1] = new Point(w * 0.45, h * 0.6);
        rook_points[2] = new Point(w * 0.55, h * 0.6);
        rook_points[3] = new Point(w * 1.0, h * 1.0);


        MatOfPoint matPt = new MatOfPoint();
        matPt.fromArray(rook_points);
        List<MatOfPoint> ppt = new ArrayList<MatOfPoint>();
        ppt.add(matPt);
        Imgproc.fillPoly(mask, ppt, new  Scalar( 255 ));

        Core.bitwise_and(img, mask, img);
    }

    public void draw_the_line(Mat img, Mat img2) {

        Mat linesP = new Mat();

        Imgproc.HoughLinesP(img, linesP,6, Math.PI/180, 160, 40, 25);

        for (int x = 0; x < linesP.rows(); x++) {
            double[] l = linesP.get(x, 0);
            Imgproc.line(img2, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0,255,0), 10, Imgproc.LINE_AA, 0);
        }

    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        cameraBridgeViewBase = (JavaCameraView)findViewById(R.id.CameraView);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);


        //System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        baseLoaderCallback = new BaseLoaderCallback(this) {
            @Override
            public void onManagerConnected(int status) {
                super.onManagerConnected(status);

                switch(status){

                    case BaseLoaderCallback.SUCCESS:
                        cameraBridgeViewBase.enableView();
                        break;
                    default:
                        super.onManagerConnected(status);
                        break;
                }


            }

        };




    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        Mat frame = inputFrame.rgba();
        Mat rgbFrame = frame.clone();



        frame = color_filter(frame, rgbFrame);
        Canny(frame);

        ROI(frame, frame.size().width, frame.size().height);

        draw_the_line(frame, rgbFrame);




        return rgbFrame;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }


    @Override
    public void onCameraViewStopped() {

    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()){
            Toast.makeText(getApplicationContext(),"There's a problem, yo!", Toast.LENGTH_SHORT).show();
        }

        else
        {
            baseLoaderCallback.onManagerConnected(baseLoaderCallback.SUCCESS);
        }



    }

    @Override
    protected void onPause() {
        super.onPause();
        if(cameraBridgeViewBase!=null){

            cameraBridgeViewBase.disableView();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase!=null){
            cameraBridgeViewBase.disableView();
        }
    }
}