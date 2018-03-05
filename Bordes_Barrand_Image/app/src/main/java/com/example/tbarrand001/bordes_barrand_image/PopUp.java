package com.example.tbarrand001.bordes_barrand_image;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by tbarrand001 on 05/03/18.
 */

public class PopUp extends Activity {


    private SeekBar skb;
    static private TextView textView;
    private Button close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public SeekBar getSkb() {
        return skb;
    }

    public void setSkb(SeekBar skb) {
        this.skb = skb;
    }

    public static TextView getTextView() {
        return textView;
    }

    public static void setTextView(TextView textView) {
        PopUp.textView = textView;
    }

    public Button getClose() {
        return close;
    }

    public void setClose(Button close) {
        this.close = close;
    }
}
