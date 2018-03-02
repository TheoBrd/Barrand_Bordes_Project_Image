package com.example.tbarrand001.bordes_barrand_app;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import static android.graphics.Color.HSVToColor;
import static android.graphics.Color.RGBToHSV;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Touch";

    private static int RESULT_LOAD_IMAGE = 1;
    private static final int CAMERA_REQUEST = 1888;

    private int valueR;
    private int valueG;
    private int valueB;

    private SeekBar skbR;
    private SeekBar skbG;
    private SeekBar skbB;
    private FilteredImage flImg;
    private ImageView color;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.reset:
                flImg.reload();
                return true;

            case R.id.gallery :
                Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, RESULT_LOAD_IMAGE);
                return true;

            case R.id.camera:
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                return true;

            case R.id.save:
                Bitmap bmp = flImg.getBmp();
                saveImgToGallery(bmp, "imgName");
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSupportActionBar().setTitle("New App");

        flImg = new FilteredImage((ImageView) findViewById(R.id.imageView));
        flImg.getImageView().setOnTouchListener(new View.OnTouchListener(){

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView view = (ImageView) v;
                view.setScaleType(ImageView.ScaleType.MATRIX);
                float scale;

                switch (event.getAction() & MotionEvent.ACTION_MASK)
                {
                    case MotionEvent.ACTION_DOWN:   // first finger down only
                        savedMatrix.set(matrix);
                        start.set(event.getX(), event.getY());
                        Log.d(TAG, "mode=DRAG"); // write to LogCat
                        mode = DRAG;
                        break;

                    case MotionEvent.ACTION_UP: // first finger lifted

                    case MotionEvent.ACTION_POINTER_UP: // second finger lifted

                        mode = NONE;
                        Log.d(TAG, "mode=NONE");
                        break;

                    case MotionEvent.ACTION_POINTER_DOWN: // first and second finger down

                        oldDist = spacing(event);
                        Log.d(TAG, "oldDist=" + oldDist);
                        if (oldDist > 5f) {
                            savedMatrix.set(matrix);
                            midPoint(mid, event);
                            mode = ZOOM;
                            Log.d(TAG, "mode=ZOOM");
                        }
                        break;

                    case MotionEvent.ACTION_MOVE:

                        if (mode == DRAG)
                        {
                            matrix.set(savedMatrix);
                            matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); // create the transformation in the matrix  of points
                        }
                        else if (mode == ZOOM)
                        {
                            // pinch zooming
                            float newDist = spacing(event);
                            Log.d(TAG, "newDist=" + newDist);
                            if (newDist > 5f)
                            {
                                matrix.set(savedMatrix);
                                scale = newDist / oldDist; // setting the scaling of the
                                // matrix...if scale > 1 means
                                // zoom in...if scale < 1 means
                                // zoom out
                                matrix.postScale(scale, scale, mid.x, mid.y);
                            }
                        }
                        break;
                }

                view.setImageMatrix(matrix); // display the transformation on screen

                return true; // indicate event was handled
            }

            /*
             * --------------------------------------------------------------------------
             * Method: spacing Parameters: MotionEvent Returns: float Description:
             * checks the spacing between the two fingers on touch
             * ----------------------------------------------------
             */

            private float spacing(MotionEvent event)
            {
                float x = event.getX(0) - event.getX(1);
                float y = event.getY(0) - event.getY(1);
                return (float) Math.sqrt(x * x + y * y);
            }

            /*
             * --------------------------------------------------------------------------
             * Method: midPoint Parameters: PointF object, MotionEvent Returns: void
             * Description: calculates the midpoint between the two fingers
             * ------------------------------------------------------------
             */

            private void midPoint(PointF point, MotionEvent event)
            {
                float x = event.getX(0) + event.getX(1);
                float y = event.getY(0) + event.getY(1);
                point.set(x / 2, y / 2);
            }
        });

        color = (ImageView) findViewById(R.id.color);
        color.setVisibility(View.GONE);
        //color.setImageBitmap(changeColor(this.color, valueG, valueB, valueR));

        skbG = (SeekBar) findViewById(R.id.seekgreen);
        skbR = (SeekBar) findViewById(R.id.seekred);
        skbB = (SeekBar) findViewById(R.id.seekblue);


        final TextView valuePrintG = (TextView) findViewById(R.id.seekbarValueG);
        this.valueG = skbG.getProgress();
        valuePrintG.setText(String.valueOf(this.valueG));
//
//        final TextView valuePrintR = (TextView) findViewById(R.id.seekbarValueR);
//        this.valueR = skbR.getProgress();
//        valuePrintR.setText(String.valueOf(this.valueR));
//
//        final TextView valuePrintB = (TextView) findViewById(R.id.seekbarValueB);
//        this.valueB = skbB.getProgress();
//        valuePrintB.setText(String.valueOf(this.valueB));

        skbG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                valueG = i;
                switch (skbG.getMax()){
                    case 200 :
                        valuePrintG.setText(String.valueOf(valueG -100));
                        break;
                    case 240 :
                        valuePrintG.setText(String.valueOf(valueG -120));
                        break;

                    default:
                        valuePrintG.setText(String.valueOf(valueG));
                        break;
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        skbG.setVisibility(View.GONE);

        /*skbR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                valueR = i;
                valuePrintR.setText(String.valueOf(valueR));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/

        skbR.setVisibility(View.GONE);

        /*skbB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                valueB = i;
                valuePrintB.setText(String.valueOf(valueB));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/

        skbB.setVisibility(View.GONE);





        Button sobelBut = (Button) findViewById(R.id.sobel);
        sobelBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flImg.reload();
                if(skbG.getVisibility()==View.VISIBLE ){
                    skbG.setVisibility(View.GONE);
                }
//              if(skbR.getVisibility()==View.VISIBLE){
//                    skbR.setVisibility(View.GONE);
//              }
//              if(skbB.getVisibility()==View.VISIBLE){
//                    skbB.setVisibility(View.GONE);
//              }
                flImg.sobelConvolution();
                flImg.setImageViewFromBitmap();

            }
        });

        Button convolBut = (Button) findViewById(R.id.convol);
        convolBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
//                if(skbR.getVisibility()==View.VISIBLE){
//                    skbR.setVisibility(View.GONE);
//                }if(skbB.getVisibility()==View.VISIBLE){
//                    skbB.setVisibility(View.GONE);
//                }
                if(skbG.getVisibility()==View.GONE){
                    skbG.setVisibility(View.VISIBLE);
                    skbG.setProgress(1);
                }
                if(skbG.getMax()!=10){
                    skbG.setProgress(1);
                    skbG.setMax(10);
                }
                flImg.averageConvolution(valueG);
                flImg.setImageViewFromBitmap();
            }
        });

        Button gaussienBut = (Button) findViewById(R.id.gauss);
        gaussienBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
//                if(skbR.getVisibility()==View.VISIBLE){
//                    skbR.setVisibility(View.GONE);
//                }if(skbB.getVisibility()==View.VISIBLE){
//                    skbB.setVisibility(View.GONE);
//                }
                if(skbG.getVisibility()==View.GONE){
                    skbG.setVisibility(View.VISIBLE);
                    skbG.setProgress(1);
                }
                if(skbG.getMax()!=10){
                    skbG.setProgress(1);
                    skbG.setMax(10);
                }
                flImg.gaussien(valueG);
                flImg.setImageViewFromBitmap();
            }
        });

        Button laplacianBut = (Button) findViewById(R.id.laplacian);
        laplacianBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
//                if(skbR.getVisibility()==View.VISIBLE){
//                    skbR.setVisibility(View.GONE);
//                }if(skbB.getVisibility()==View.VISIBLE){
//                    skbB.setVisibility(View.GONE);
//                }
                if(skbG.getVisibility()==View.VISIBLE){
                    skbG.setVisibility(View.GONE);
                }
                flImg.laplacian();
                flImg.setImageViewFromBitmap();
            }
        });

        Button brightBut = (Button) findViewById(R.id.bright);
        brightBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
//                if(skbR.getVisibility()==View.VISIBLE){
//                    skbR.setVisibility(View.GONE);
//                }if(skbB.getVisibility()==View.VISIBLE){
//                    skbB.setVisibility(View.GONE);
//                }
                if(skbG.getVisibility()==View.GONE){
                    skbG.setVisibility(View.VISIBLE);
                    skbG.setProgress(1);
                }
                if(skbG.getMax()!=200){
                    skbG.setMax(200);
                    skbG.setProgress(100);
                }
                flImg.brightness(valueG -100);
                flImg.setImageViewFromBitmap();
            }
        });

        Button contrastBut = (Button) findViewById(R.id.contrast);
        contrastBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
//                if(skbR.getVisibility()==View.VISIBLE){
//                    skbR.setVisibility(View.GONE);
//                }if(skbB.getVisibility()==View.VISIBLE){
//                    skbB.setVisibility(View.GONE);
//                }
                if(skbG.getVisibility()==View.GONE){
                    skbG.setVisibility(View.VISIBLE);
                    skbG.setProgress(1);
                }
                if(skbG.getMax()!=240){
                    skbG.setMax(240);
                    skbG.setProgress(120);
                }
                flImg.contrast(valueG -120);
                flImg.setImageViewFromBitmap();
            }
        });

        Button equalizationBut = (Button) findViewById(R.id.equal);
        equalizationBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
//                if(skbR.getVisibility()==View.VISIBLE){
//                    skbR.setVisibility(View.GONE);
//                }if(skbB.getVisibility()==View.VISIBLE){
//                    skbB.setVisibility(View.GONE);
//                }
                if(skbG.getVisibility()==View.VISIBLE){
                    skbG.setVisibility(View.GONE);
                }
                flImg.equalizationColor();
                flImg.setImageViewFromBitmap();
            }
        });

        Button colorizeBut = (Button) findViewById(R.id.colorize);
        colorizeBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
//                if(skbR.getVisibility()==View.VISIBLE){
//                    skbR.setVisibility(View.GONE);
//                }if(skbB.getVisibility()==View.VISIBLE){
//                    skbB.setVisibility(View.GONE);
//                }

                if(skbG.getVisibility()==View.GONE){
                    skbG.setVisibility(View.VISIBLE);
                    skbG.setProgress(0);
                }
                if(skbG.getMax()!=360){
                    skbG.setMax(360);
                    skbG.setProgress(0);
                }
                flImg.colorize(valueG);
                flImg.setImageViewFromBitmap();
            }
        });

        Button grayBut = (Button) findViewById(R.id.gray);
        grayBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
//                if(skbR.getVisibility()==View.VISIBLE){
//                    skbR.setVisibility(View.GONE);
//                }if(skbB.getVisibility()==View.VISIBLE){
//                    skbB.setVisibility(View.GONE);
//                }
                if(skbG.getVisibility()==View.VISIBLE){
                    skbG.setVisibility(View.GONE);
                }
                flImg.toGray();
                flImg.setImageViewFromBitmap();
            }
        });


    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK){
            Uri imageUri = data.getData();
            flImg.getImageView().setImageURI(imageUri);
            flImg.setBitmapFromImageView();

        }

        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            flImg.getImageView().setImageBitmap(photo);
            flImg.setBitmapFromImageView();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImgToGallery(Bitmap bm, String imgName) {
        OutputStream fOut = null;
        String strDirectory = Environment.getExternalStorageDirectory().toString();

        File f = new File(strDirectory, imgName);

        try {
            fOut = new FileOutputStream(f);
            /**Compress image**/
            bm.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
            /**Update image to gallery**/
            MediaStore.Images.Media.insertImage(getContentResolver(), f.getAbsolutePath(), f.getName(), f.getName());
            Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*public Bitmap changeColor (ImageView img, int h, int s, int v){
        Bitmap bmp = ((BitmapDrawable)img.getDrawable()).getBitmap();
        int[] pixelMap = new int[bmp.getWidth()*bmp.getHeight()];
        bmp.getPixels(pixelMap, 0, bmp.getWidth(), 0,0, bmp.getWidth(), bmp.getHeight());
        float[] hsv = new float[3];

        for (int p=0; p< pixelMap.length; p++){
            int r = red(pixelMap[p]);
            int g = green(pixelMap[p]);
            int b = blue(pixelMap[p]);
            RGBToHSV(r,g,b,hsv);
            hsv[0] = h;
            hsv[1] = s/100;
            hsv[2] = v/100;
            pixelMap[p]=HSVToColor(hsv);
        }
        bmp =bmp.copy(Bitmap.Config.ARGB_8888 , true);
        bmp.setPixels(pixelMap, 0, bmp.getWidth(), 0,0, bmp.getWidth(), bmp.getHeight());
        return bmp;
    }*/

}
