package com.example.tbarrand001.bordes_barrand_image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by tbarrand001 & cbordes001 on 05/03/18.
 */

public class PopUpColor extends PopUp{

    private ImageView colorShow;

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.color);

        setSkb( (SeekBar)findViewById(R.id.seekBarConvo));
        setTextView((TextView) findViewById(R.id.convoValue));


        colorShow = (ImageView) findViewById(R.id.colorView);
        final float[] hsvColor = {0, 1, 1};
        hsvColor[0] = 360f * getSkb().getProgress() / getSkb().getMax();
        colorShow.setBackgroundColor(Color.HSVToColor(hsvColor));


        getTextView().setText(String.valueOf(getSkb().getProgress()));

        getSkb().setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                hsvColor[0] = 360f * progress / getSkb().getMax();
                colorShow.setBackgroundColor(Color.HSVToColor(hsvColor));
                getTextView().setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setClose((Button)findViewById(R.id.button3));
        getClose().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("color",String.valueOf(getSkb().getProgress())); // data is the value you need in parent
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });

    }

}
