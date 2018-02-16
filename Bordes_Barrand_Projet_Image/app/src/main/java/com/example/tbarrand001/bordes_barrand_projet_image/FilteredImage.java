package com.example.tbarrand001.bordes_barrand_projet_image;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.widget.ImageView;

import java.util.Random;

import static android.graphics.Color.HSVToColor;
import static android.graphics.Color.RGBToHSV;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

/**
 * Created by tbarrand001 & cbordes001 on 12/02/18.
 */

public class FilteredImage {


    private ImageView imageView;
    private ImageView restartImg;
    private Bitmap bmp;
    private int width;
    private int height;

    FilteredImage(ImageView imgV){
        this.imageView = imgV;
        this.restartImg = imgV;
        this.bmp = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        this.width = this.bmp.getWidth();
        this.height = this.bmp.getHeight();
    }


    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        this.imageView = imageView;
    }

    public Bitmap getBmp() {
        return bmp;
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void reload(){
        this.imageView = this.restartImg;
    }

    public void setBitmapFromImageView(){
       this.bmp = ((BitmapDrawable)this.imageView.getDrawable()).getBitmap();
    }

    public void setImageViewFromBitmap(){
        this.imageView.setImageBitmap(this.bmp);
    }

    private int[] histogram(int [] bmp){
        int[] histo = new int[256];
        for(int i=0; i<256; i++){
            histo[i]=0;
        }
        for(int p=0; p < bmp.length; p++){
            histo[red(bmp[p])]++;
        }
        return histo;
    }

    public void equalisationColor(){
        int[] pixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
        int[] pixelMapGrey =new int[this.width *this.height];

        for (int p=0; p< pixelMap.length; p++){
            int r = red(pixelMap[p]);
            int g = green(pixelMap[p]);
            int b = blue(pixelMap[p]);
            int gray = (int) (0.3*r+0.59*g+0.11*b);
            pixelMapGrey[p] = Color.rgb(gray,gray,gray);
        }
        int[] histo = histogram(pixelMapGrey);

        int[] Cumul= new int [256];
        for(int i =0; i<256; i++){
            if(i==0){
                Cumul[i]=histo[i];
            }else{
                Cumul[i]=Cumul[i-1]+histo[i];
            }
        }

        for(int p=0; p<pixelMap.length; p++){
            int r = red(pixelMap[p]);
            int b = blue(pixelMap[p]);
            int g = green(pixelMap[p]);
            r= (Cumul[r]*255)/(pixelMap.length);
            b= (Cumul[b]*255)/(pixelMap.length);
            g= (Cumul[g]*255)/(pixelMap.length);
            pixelMap[p] = Color.rgb(r,g,b);
        }

        this.bmp.setPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

    }

    public void colorize (){
        int[] pixelMap = new int[this.width *this.width];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
        float[] hsv = new float[3];
        Random rand = new Random();
        int hue =rand.nextInt(361);

        for (int p=0; p< pixelMap.length; p++){
            int r = red(pixelMap[p]);
            int g = green(pixelMap[p]);
            int b = blue(pixelMap[p]);
            RGBToHSV(r,g,b,hsv);
            hsv[0] = hue;
            pixelMap[p]=HSVToColor(hsv);
        }
        bmp.setPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
    }

    public double[] convolution(int n, float[][] masque, int[] pixelMap, int p){

        double red, green, blue;
        red=green=blue=0;
        for(int u=-n; u<=n; u++){
            for(int v=-n; v<=n; v++){
                int indices = p+u+(this.width *v);
                red+= red(pixelMap[indices])*(double)(masque[u+n][v+n]);
                green+= green(pixelMap[indices])*(double)(masque[u+n][v+n]);
                blue+= blue(pixelMap[indices])*(double)(masque[u+n][v+n]);
            }
        }
        double[] RGB = {red,green,blue};
        return RGB;

    }

    public void averageConvolution(int n){
        int size = 2*n+1;
        int[] pixelMap = new int[this.width *this.height];
        int[] finalPixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

        for(int s =0; s< finalPixelMap.length; s++){
            finalPixelMap[s]=Color.rgb(0,0,0);
        }

        float[][] masque = new float[size][size];
        for(int i=0; i<size; i++){
            for(int j=0; j<size; j++){
                masque[i][j]=(float)1.0/(size*size);
            }
        }

        for(int p =0 ; p<pixelMap.length; p++){

            if(p>n*this.width && p<((this.width *this.height)-n*this.width) && (p%this.width)>(n-1) && (p%this.width)<(this.width -n)){

                double[] RGB = convolution(n,masque,pixelMap,p);
                finalPixelMap[p] = Color.rgb((int)RGB[0], (int)RGB[1],(int)RGB[2]);
            }
        }

        this.bmp.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);

    }

    public void sobelConvolution( ){
        int[] pixelMap = new int[this.width *this.height];
        int[] finalPixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
        double redT, greenT, blueT;
        int gray;

        for(int s =0; s< finalPixelMap.length; s++){
            finalPixelMap[s]=Color.rgb(0,0,0);
        }

        float[][] masque2 = new float[][]{      {-1,-2,-1},
                                                { 0, 0, 0},
                                                { 1, 2, 1}};

        float[][] masque1 = new float[][]{      {-1, 0, 1},
                                                {-2, 0, 2},
                                                {-1, 0, 1}};



        for(int p =0 ; p<pixelMap.length; p++){

            if(p>this.width && p<((this.width *this.height)-this.width) && (p%this.width)>(0) && (p%this.width)<(this.width -1)){

                double[] RGBX = convolution(1,masque1, pixelMap, p);
                double[] RGBY = convolution(1,masque2, pixelMap, p);


                redT=Math.sqrt((RGBX[0]*RGBX[0])+(RGBY[0]*RGBY[0]));
                greenT=Math.sqrt((RGBX[1]*RGBX[1])+(RGBY[1]*RGBY[1]));
                blueT=Math.sqrt((RGBX[2]*RGBX[2])+(RGBY[2]*RGBY[2]));

                if (blueT > 255)
                { blueT= 255; }

                if (greenT > 255)
                { greenT= 255; }

                if (redT> 255)
                { redT = 255; }

                gray = (int) (0.3*redT+0.59*greenT+0.11*blueT);
                finalPixelMap[p] = Color.rgb( gray,gray,gray);
            }else {
                finalPixelMap[p] = Color.rgb(0,0,0);
            }
        }
        Bitmap bmp2 = this.bmp.copy(Bitmap.Config.ARGB_8888, true);
        bmp2.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);
        this.bmp = bmp2;
    }

    public void gaussien(int n){
        int size = 2*n+1;
        int[] pixelMap = new int[this.width *this.height];
        int[] finalPixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

        for(int s =0; s< finalPixelMap.length; s++){
            finalPixelMap[s]=Color.rgb(0,0,0);
        }

        double sigma = Math.sqrt((n*n)/Math.log(10*n));

        float[][] masque = new float[size][size];
        float sum=0;
        for(int i=0; i<size; i++){
            for(int j=0; j<size; j++){
                masque[i][j]= (float) (10*n*Math.exp(-( ((i-n)*(i-n)+(j-n)*(j-n))/(2*sigma*sigma) )));
                sum += masque[i][j];
            }
        }

        for(int p =0 ; p<pixelMap.length; p++){

            if(p>n*this.width && p<((this.width *this.height)-n*this.width) && (p%this.width)>(n-1) && (p%this.width)<(this.width -n)){

                double[] RGB = convolution(n,masque,pixelMap,p);
                finalPixelMap[p] = Color.rgb((int)(RGB[0]/sum), (int)(RGB[1]/sum),(int)(RGB[2]/sum));
            }
        }
        this.bmp.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);

    }


}
