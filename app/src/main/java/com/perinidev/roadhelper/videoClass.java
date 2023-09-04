package com.perinidev.roadhelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class videoClass extends CameraActivity {

    Button detect, detectfile;

    ImageButton soundon, soundoff;

    boolean isChecked;

    CheckBox checkBox, checkBoxLane;

    ImageView imageViewAlertR, imageViewAlertL;

    ImageView videoView;

    CameraBridgeViewBase cameraBridgeViewBase;

    int SELECT_VIDEO_CODE2 = 102, CAMERA_CODE2 = 104;

    LaneDetector1 laneDetector;

    EditText factorDisplay;

    TextView textViewTest;

    String factorDisplayString;

    Double factorDouble;

    boolean isAssist = false;

    private Context context;

    private String videoPath;

    private boolean isSoundOn = true; // variable to keep track of sound state

    boolean isRoutineRunning = false; // set initial state to false

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.roadvideo_land);

        context = this;

        //FULLSCREEN MODE
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // for landscape mode

        if(OpenCVLoader.initDebug()) {
            Log.d("LOADED", "Success");
        }
        else Log.d("LOADED", "error");

        //camera permission method
        getPermission();

        //sound objets

        soundon = findViewById(R.id.soundon);
        soundoff = findViewById(R.id.soundoff);

        // lane object alerts
        imageViewAlertR = findViewById(R.id.imageViewAlertR);
        imageViewAlertL = findViewById(R.id.imageViewAlertL);

        imageViewAlertR.setVisibility(View.GONE);
        imageViewAlertL.setVisibility(View.GONE);

        textViewTest = findViewById(R.id.textViewTest);
        textViewTest.setText("");


        detect = findViewById(R.id.detect); //button camera

        detectfile = findViewById(R.id.detectfile); // button video file

        checkBox = findViewById(R.id.checkBox);

        checkBoxLane = findViewById(R.id.checkBoxLane);

        //camera view
        cameraBridgeViewBase = findViewById(R.id.cameraView);

        // hide camera view
        cameraBridgeViewBase.setVisibility(View.GONE);

        videoView = findViewById(R.id.videoView);
        videoView.setVisibility(View.GONE);


        // enter factor scale real mensurment
        factorDisplay = findViewById(R.id.factor);
        factorDisplay.setText("0.0035"); //initial scale

        //String input = factorDisplay.getText().toString();
        //double input1 = Double.parseDouble(input);

        //
        laneDetector = new LaneDetector1();

        //initial value of dist lanes
        double distTorightLane = 0.00;
        double distToleftLane = 0.00;
        double distlanes = 0.00;




        factorDisplay.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if(i == EditorInfo.IME_ACTION_DONE) {

                   factorDisplayString = factorDisplay.getText().toString();
                   factorDouble = Double.parseDouble(factorDisplayString);
                   laneDetector.factor = factorDouble;

                }
                return false;
            }
        });


        // checkbox shows ROI
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked) {
                    laneDetector.ROI=true;
                    Toast.makeText(getApplicationContext(), "SHOWING ROI", Toast.LENGTH_SHORT).show();
                } else {
                    laneDetector.ROI=false;
                    Toast.makeText(getApplicationContext(), "HIDING ROI", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // checkbox shows lane out
        checkBoxLane.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if (isChecked) {

                    Toast.makeText(getApplicationContext(), "LANE ASSIST. ON", Toast.LENGTH_SHORT).show();
                    isAssist = true;

                } else {

                    isAssist = false;
                    Toast.makeText(getApplicationContext(), "LANE ASSIST. OFF", Toast.LENGTH_SHORT).show();
                }
            }
        });


        final int mHeight = cameraBridgeViewBase.getHeight();
        final int mWidth = cameraBridgeViewBase.getWidth();



//camera image button
        detect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isRoutineRunning) {
                    // show camera view
                    cameraBridgeViewBase.setVisibility(View.VISIBLE);

                    cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
                        @Override
                        public void onCameraViewStarted(int width, int height) {

                        }

                        @Override
                        public void onCameraViewStopped() {
                            //not used yet
                        }

                        @Override
                        public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

                            final int mHeight = cameraBridgeViewBase.getHeight();
                            final int mWidth = cameraBridgeViewBase.getWidth();

                            //laneDetector.factor = input1;
                            Mat result = laneDetector.detectLanes(inputFrame.rgba(),mHeight, mWidth);
                            double distTorightLane = laneDetector.distTorightLane;
                            double distToleftLane = laneDetector.distToLeftLane;
                            double distlanes = laneDetector.betweenLanex;

                            SpannableString spannableString = TextFormatter.formatText("Right Lane ", "Left Lane   ", "Road          ", distTorightLane, distToleftLane, distlanes, getResources().getColor(R.color.white), R.color.black);
                            textViewTest.setText(spannableString);

                            laneout(distToleftLane,distTorightLane, 0.7, isAssist);

                            return  result;
                        }
                    });

                    if(OpenCVLoader.initDebug()) {
                        //enable camera feed
                        cameraBridgeViewBase.enableView();
                    }

                    isRoutineRunning = true; // set flag to true
                } else {
                    // hide camera view
                    cameraBridgeViewBase.setVisibility(View.GONE);

                    cameraBridgeViewBase.disableView(); // disable camera feed

                    isRoutineRunning = false; // set flag to false
                }
            }
        });


        //video from file button

        detectfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Open the file picker
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 0);


            }

        });

        // set initial state of sound buttons
        if (isSoundOn) {
            soundon.setVisibility(View.VISIBLE);
            soundoff.setVisibility(View.GONE);
        } else {
            soundon.setVisibility(View.GONE);
            soundoff.setVisibility(View.VISIBLE);
        }

        soundon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // turn sound off
                isSoundOn = false;
                soundon.setVisibility(View.GONE);
                soundoff.setVisibility(View.VISIBLE);

                // TODO: Mute sounds in your app
            }
        });

        soundoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // turn sound on
                isSoundOn = true;
                soundon.setVisibility(View.VISIBLE);
                soundoff.setVisibility(View.GONE);

                // TODO: Unmute sounds in your app
            }
        });






    }

    private void laneout (final double distL, double distR,final double delta, final boolean flag){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (flag){

                    if (distL <=delta){
                        imageViewAlertL.setVisibility(View.VISIBLE);
                        blinkImageView(context,imageViewAlertL, 3, 500);
                    } else if (distR <=delta) {
                        imageViewAlertR.setVisibility(View.VISIBLE);
                        blinkImageView(context,imageViewAlertR, 3, 500);
                    }
                 }
            }
        });

    }

    //blink image lane
    private boolean canPlaySound = true;
    private Timer soundTimer;
    private void blinkImageView(final Context context, final ImageView imageView, int numRepetitions, int delayMillis) {

        Animation blinkAnimation = new AlphaAnimation(0.0f, 1.0f);
        blinkAnimation.setDuration(500);
        blinkAnimation.setStartOffset(20);
        blinkAnimation.setRepeatMode(Animation.REVERSE);
        blinkAnimation.setRepeatCount(numRepetitions);

        final MediaPlayer mp = MediaPlayer.create(context, R.raw.alert);

        blinkAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // Not needed
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                imageView.setVisibility(View.GONE);
                mp.stop();
                mp.release();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

                // Not needed
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                imageView.startAnimation(blinkAnimation);

                if (canPlaySound && isSoundOn) {
                    mp.start();
                    canPlaySound = false;
                    soundTimer = new Timer();
                    soundTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            canPlaySound = true;
                        }
                    }, 1000); // 1 seconds
                }
            }
        }, delayMillis);
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);
    }

//    @Override
//    protected void  onResume() {
//        super.onResume();
//        cameraBridgeViewBase.enableView();
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraBridgeViewBase.disableView();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraBridgeViewBase.disableView();
    }

    //PERMISSION
    void getPermission(){
        if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 102);
        }
    }

    public void OnRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==102 && grantResults.length>0){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getPermission();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        videoView.setVisibility(View.VISIBLE);

        if (resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            // Load the image as a bitmap
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Convert the bitmap to a Mat
            Mat imageMat = new Mat();
            Utils.bitmapToMat(bitmap, imageMat);

            // Convert the grayscale image to RGBA format
            Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_RGBA2RGB);

            // Convert the image to grayscale
            //Imgproc.cvtColor(imageMat, imageMat, Imgproc.COLOR_BGR2GRAY);

            Mat result = laneDetector.detectLanes(imageMat,imageMat.height(), imageMat.width());

            // Display the converted image on the ImageView
            Bitmap grayBitmap = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(result, grayBitmap);
            videoView.setImageBitmap(grayBitmap);
        }
    }
}
