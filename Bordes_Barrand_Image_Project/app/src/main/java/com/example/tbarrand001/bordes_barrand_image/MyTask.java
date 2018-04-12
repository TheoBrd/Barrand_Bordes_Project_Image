package com.example.tbarrand001.bordes_barrand_image;

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




    MyTask(FilteredImage flmImg, ProgressBar progressBar, AsyncResponse delegate){
        this.flImg = flmImg;
        this.delegate = delegate;
        progress = progressBar;
    }



    protected void onPreExecute() {
        super.onPreExecute();
        progress.setVisibility(View.VISIBLE);

    }

    @Override
    protected FilteredImage doInBackground(String... filterType) {
        switch (filterType[0]){
            case "cluster":
                flImg.clusteringCube(8);
                break;

            case "cartoon":
                flImg.clusteringCube(8);
                Bitmap clusterBmp = flImg.getBmp().copy(Bitmap.Config.ARGB_8888, true);
                flImg.sobelConvolution();
                Bitmap edgeBmp = flImg.getBmp();
                flImg.cartoon(clusterBmp, edgeBmp);
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
