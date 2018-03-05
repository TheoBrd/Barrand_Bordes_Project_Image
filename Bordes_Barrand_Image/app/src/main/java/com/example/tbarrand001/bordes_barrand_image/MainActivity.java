package com.example.tbarrand001.bordes_barrand_image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
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



public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Touch";

    /**The different channels**/
    private static int RESULT_LOAD_IMAGE = 1;
    private static int RESULT_POPUP_COLOR = 2;
    private static int RESULT_POPUP_ONE_COLOR = 3;
    private static int RESULT_POPUP_GAUSSIAN = 4;
    private static int RESULT_POPUP_AVERAGE = 5;
    private static final int CAMERA_REQUEST = 6;


    private int valueSKB;
    private SeekBar skb;
    private FilteredImage flImg;

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private int returnPopupValue;

    /** Initialize menu from menu layout**/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }


    /** For each option in the menu, give them an action**/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

            case R.id.reset:
                flImg.reload();
                return true;

            /** Go find a image in the phone's gallery **/
            case R.id.gallery :
                //create a intent, and next call an activity which return an image;
                Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery, RESULT_LOAD_IMAGE);
                return true;

            /** Go take a photo with the phone's camera **/
            case R.id.camera:
                //create a intent, and next call an activity which return an image;
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
                return true;

            case R.id.save:
                //create a intent, and next call an activity;
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

        //Set name Action BAr
        getSupportActionBar().setTitle("Option");

        /** Initialize variables  **/
        flImg = new FilteredImage((ImageView) findViewById(R.id.imageView));
        skb = (SeekBar) findViewById(R.id.seekgreen);
        //Hide seekBar, which is useful in only few cases
        skb.setVisibility(View.GONE);
        //This textView show the value of the seekBar's progress
        final TextView valuePrintG = (TextView) findViewById(R.id.seekbarValueG);
        this.valueSKB = skb.getProgress();
        valuePrintG.setText(String.valueOf(this.valueSKB));


        /**Zoom in and Zoom out**/
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




        /**Apply change when using the SeekBar**/
        skb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                valueSKB = i;
                switch (skb.getMax()){
                    //If SeekBar max =200, then we using Brightness
                    case 200 :
                        //center the seekBar progress in order to use negative values
                        valuePrintG.setText(String.valueOf(valueSKB -100));
                        flImg.reload();
                        flImg.brightness(valueSKB -100);
                        flImg.setImageViewFromBitmap();

                        break;

                    //If SeekBar max =200, then we using Contrast
                    case 240 :

                        //center the seekBar progress in order to use negative values
                        valuePrintG.setText(String.valueOf(valueSKB -120));
                        flImg.reload();
                        flImg.contrast(valueSKB -120);
                        flImg.setImageViewFromBitmap();
                        break;

                    default:
                        valuePrintG.setText(String.valueOf(valueSKB));
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





        /** Initialize all button which apply the filters **/

        //Sobel button
        Button sobelBut = (Button) findViewById(R.id.sobel);
        sobelBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flImg.reload();
                if(skb.getVisibility()==View.VISIBLE ){
                    skb.setVisibility(View.GONE);
                }
                flImg.sobelConvolution();
                flImg.setImageViewFromBitmap();

            }
        });

        //Simple convolution Button
        Button convolBut = (Button) findViewById(R.id.convol);
        convolBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
                if(skb.getVisibility()==View.VISIBLE){
                    skb.setVisibility(View.GONE);
                }
                //Call the PopUp to get the value (here the size) of the convolution masque
                Intent popupC = new Intent(MainActivity.this, PopUpConvolution.class);
                startActivityForResult(popupC, RESULT_POPUP_AVERAGE);
            }
        });

        //Gaussian Button
        Button gaussienBut = (Button) findViewById(R.id.gauss);
        gaussienBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
                if(skb.getVisibility()==View.VISIBLE){
                    skb.setVisibility(View.GONE);
                }
                //Call the PopUp to get the value (here the size) of the convolution masque
                Intent popupC = new Intent(MainActivity.this, PopUpConvolution.class);
                startActivityForResult(popupC, RESULT_POPUP_GAUSSIAN);
            }
        });

        //Laplacian Button
        Button laplacianBut = (Button) findViewById(R.id.laplacian);
        laplacianBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
                if(skb.getVisibility()==View.VISIBLE){
                    skb.setVisibility(View.GONE);
                }
                flImg.laplacian();
                flImg.setImageViewFromBitmap();
            }
        });

        //Brightness Button
        Button brightBut = (Button) findViewById(R.id.bright);
        brightBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
                //Display the SeekBar and set progress in the center
                if(skb.getVisibility()==View.GONE){
                    skb.setVisibility(View.VISIBLE);
                    skb.setProgress(100);
                }

                //Set the max
                if(skb.getMax()!=200){
                    skb.setMax(200);
                    skb.setProgress(100);
                }
            }
        });

        //Contrast Button
        Button contrastBut = (Button) findViewById(R.id.contrast);
        contrastBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
                if(skb.getVisibility()==View.GONE){
                    skb.setVisibility(View.VISIBLE);
                    skb.setProgress(120);
                }
                if(skb.getMax()!=240){
                    skb.setMax(240);
                    skb.setProgress(120);
                }
            }
        });

        //Histogram equalization Button
        Button equalizationBut = (Button) findViewById(R.id.equal);
        equalizationBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
                if(skb.getVisibility()==View.VISIBLE){
                    skb.setVisibility(View.GONE);
                }
                flImg.equalizationColor();
                flImg.setImageViewFromBitmap();
            }
        });

        //Colorize Button
        Button colorizeBut = (Button) findViewById(R.id.colorize);
        colorizeBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
                if(skb.getVisibility()==View.VISIBLE){
                    skb.setVisibility(View.GONE);
                }
                Intent popupC = new Intent(MainActivity.this, PopUpColor.class);
                startActivityForResult(popupC, RESULT_POPUP_COLOR);

            }
        });

        //Gray Button
        Button grayBut = (Button) findViewById(R.id.gray);
        grayBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
                if(skb.getVisibility()==View.VISIBLE){
                    skb.setVisibility(View.GONE);
                }
                flImg.toGray();
                flImg.setImageViewFromBitmap();
            }
        });

        //Sepia Button
        Button sepiaBut = (Button) findViewById(R.id.sepia);
        sepiaBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
                if(skb.getVisibility()==View.VISIBLE){
                    skb.setVisibility(View.GONE);
                }
                flImg.sepia();
                flImg.setImageViewFromBitmap();
            }
        });

        //Keep On Color Button
        Button oneColorButt = (Button) findViewById(R.id.oneColor);
        oneColorButt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flImg.reload();
                if(skb.getVisibility()==View.VISIBLE){
                    skb.setVisibility(View.GONE);
                }
                Intent popupC = new Intent(MainActivity.this, PopUpColor.class);
                startActivityForResult(popupC, RESULT_POPUP_ONE_COLOR);

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

        if(requestCode==RESULT_POPUP_COLOR && resultCode==Activity.RESULT_OK){
            returnPopupValue = Integer.parseInt(data.getStringExtra("color"));
            flImg.colorize(returnPopupValue);
            flImg.setImageViewFromBitmap();
        }

        if(requestCode==RESULT_POPUP_ONE_COLOR && resultCode==Activity.RESULT_OK){
            returnPopupValue = Integer.parseInt(data.getStringExtra("color"));
            flImg.oneColor(returnPopupValue);
            flImg.setImageViewFromBitmap();
        }

        if(requestCode==RESULT_POPUP_AVERAGE && resultCode==Activity.RESULT_OK){
            returnPopupValue = Integer.parseInt(data.getStringExtra("convol"));
            flImg.averageConvolution(returnPopupValue);
            flImg.setImageViewFromBitmap();
        }

        if(requestCode== RESULT_POPUP_GAUSSIAN && resultCode==Activity.RESULT_OK){
            returnPopupValue = Integer.parseInt(data.getStringExtra("convol"));
            flImg.gaussian(returnPopupValue);
            flImg.setImageViewFromBitmap();
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






}