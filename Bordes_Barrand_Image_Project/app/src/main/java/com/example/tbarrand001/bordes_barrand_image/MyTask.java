package com.example.tbarrand001.bordes_barrand_image;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by tbarrand001 & cbordes001 on 10/04/2018.
 */

public class MyTask extends AsyncTask<String,Void,FilteredImage>{

    private ProgressBar progress;
    private FilteredImage flImg;
    private AsyncResponse delegate=null;
    private int value;
    private Bitmap bmpUsed;
    private Context context;




    MyTask(FilteredImage flmImg, ProgressBar progressBar, AsyncResponse delegate){
        this.flImg = flmImg;
        this.delegate = delegate;
        this.progress = progressBar;
        this.context = null;
        this.value=-1;
        this.bmpUsed = null;
    }

    public void setContext(Context context){
        this.context=context;
    }


    public void setValue(int value){
        this.value=value;
    }

    public void setBmpUsed(Bitmap bmpUsed) {
        this.bmpUsed = bmpUsed;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        progress.setVisibility(View.VISIBLE);

    }

    @Override
    protected FilteredImage doInBackground(String... filterType) {
        switch (filterType[0]){

            case "gray":
                flImg.toGrayRS(flImg.getBmp(), this.context);
                //flImg.toGray();
                break;

            case "sepia":
                flImg.toSepiaRS(flImg.getBmp(), context);
                //flImg.sepia();
                break;

            case "average":
                flImg.averageConvolution(value);
                break;

            case "gaussian":
                flImg.gaussian(value);
                break;

            case "equalization":
                flImg.equalizationColor();
                break;

            case "sobel":
                flImg.sobelConvolution();
                break;

            case "laplacian":
                flImg.laplacian();
                break;

            case "colorize":
                flImg.colorize(value);
                break;

            case "oneColor":
                flImg.oneColor(value);
                break;

            case "cluster":
                flImg.clusteringCube(8);
                break;

            case "invert":
                flImg.invertRS(flImg.getBmp(), context);
                //flImg.invert();
                break;

            case "cartoon":
                flImg.clusteringCube(8);
                bmpUsed = flImg.getBmp().copy(Bitmap.Config.ARGB_8888, true );
                flImg.setBmp(flImg.getReset());
                flImg.gaussian(3);
                flImg.sobelConvolution();
                flImg.cartoon(bmpUsed, flImg.getBmp());
                break;

            default:
                Log.e("ERROR", "Error asyncTask");
                break;
        }

        return flImg;

    }

    @Override
    protected void onPostExecute(FilteredImage flmImg) {
        progress.setVisibility(View.GONE);
        delegate.processFinish(flmImg);

    }


}
