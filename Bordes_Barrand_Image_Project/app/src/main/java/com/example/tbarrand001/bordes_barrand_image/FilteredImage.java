package com.example.tbarrand001.bordes_barrand_image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.renderscript.RenderScript;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.nio.IntBuffer;
import java.util.LinkedList;

import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Bitmap.createScaledBitmap;
import static android.graphics.Color.HSVToColor;
import static android.graphics.Color.RGBToHSV;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

import android.renderscript.Allocation;

/**
 * Created by tbarrand001 & cbordes001 on 12/02/18.
 */

public class FilteredImage {


    private ImageView imageView;
    private Bitmap bmp;
    private Bitmap reset;
    private LinkedList<Bitmap> undo;
    private int width;
    private int height;

    /**
     * Initialize the Image
     * @param imgV
     */
    FilteredImage(ImageView imgV){
        this.imageView = imgV;
        this.bmp = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
        this.reset = this.bmp.copy(Bitmap.Config.ARGB_8888, true);
        this.undo = new LinkedList<>();
        undo.add(this.bmp.copy(Bitmap.Config.ARGB_8888, true));
        this.width = this.bmp.getWidth();
        this.height = this.bmp.getHeight();
    }

    /**
     *get the ImageView
     * @return the ImageView
     */
    public ImageView getImageView() {
        return imageView;
    }

    /**
     *get the width of ImageView
     * @return the width of ImageView
     */
    public int getWidth() {
        return this.width;
    }

    /**
     *get the height of ImageView
     * @return the height of ImageView
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * Get the Bitmap
     * @return the bitmap
     */
    public Bitmap getBmp() {
        return bmp;
    }

    /**
     * Update the LinkedList undo
     */
    public void setUndoList(){
        undo.add(this.bmp.copy(Bitmap.Config.ARGB_8888,true));
    }

    /**
     * Clear the LinkedList undo
     */
    public void reloadUndoList() {
        this.undo.clear();
    }

    /**
     * Check if the LinkedList undo is empty or not
     * @return true if undo is empty, else return false
     */
    public boolean undoIsEmpty(){
        if (this.undo.size() == 0){
            return true;
        }
        else {
            return false ;
        }
    }

    /**
     * Reload the old ImageView's bitmap
     */
    public void reload(){
        this.bmp = this.reset.copy(Bitmap.Config.ARGB_8888, true);
        setImageViewFromBitmap();
    }

    /**
     * Undo the latest filter applied
     */
    public void undo() {
        this.bmp = this.undo.getLast();
        setImageViewFromBitmap();
        this.undo.removeLast();

    }

    /**
     * Get the new ImageView 's Bitmap
     */
    public void setBitmapFromImageView(){
       this.bmp = ((BitmapDrawable)this.imageView.getDrawable()).getBitmap();
       this.reset = this.bmp.copy(Bitmap.Config.ARGB_8888, true);
       this.width = this.bmp.getWidth();
       this.height = this.bmp.getHeight();
    }

    /**
     * Change the ImageView depends on the bitmap
     */
    public void setImageViewFromBitmap(){
        this.imageView.setImageBitmap(this.bmp);
    }

    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    /**
     * Change the ImageView depends on the bitmap
     */
    public void setImageViewFromResetBitmap(){
        this.imageView.setImageBitmap(this.reset);
    }

    /**
     * Change the ImageView's Bitmap in a shade of gray using RenderScript
     */
    public void toGrayRS(Bitmap bmp, Context context) {
        RenderScript rs = RenderScript.create(context);

        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_grey greyScript = new ScriptC_grey(rs);

        greyScript.forEach_toGray(input, output);
        output.copyTo(bmp);

        input.destroy();
        output.destroy();
        greyScript.destroy();
        rs.destroy();

    }

    /**
     * Change the ImageView's Bitmap in a shade of gray
     */
    /*public void toGray(){

        int[] pixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

        for (int p=0; p< pixelMap.length; p++){

            int r = red(pixelMap[p]);
            int g = green(pixelMap[p]);
            int b = blue(pixelMap[p]);
            int gray = (int) (0.3*r+0.59*g+0.11*b);
            pixelMap[p] = Color.rgb(gray,gray,gray);


        }
        this.bmp.setPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
    }*/

    /**
     *
     * @param bmp
     * @return the image's histogram
     */
    private int[][] histogram(int [] bmp){
        int[][] histo = new int[3][256];
        for(int i=0; i<256; i++){
            histo[0][i]=0;
            histo[1][i]=0;
            histo[2][i]=0;
        }
        for(int p=0; p < bmp.length; p++){
            histo[0][red(bmp[p])]++;
            histo[1][green(bmp[p])]++;
            histo[2][blue(bmp[p])]++;
        }
        return histo;
    }


    public void equalizationColor(){
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
        int[][] histo = histogram(pixelMapGrey);

        int[][] Cumul= new int [3][256];
        for(int i =0; i<256; i++){
            if(i==0){
                Cumul[0][i]=histo[0][i];
                Cumul[1][i]=histo[0][i];
                Cumul[2][i]=histo[0][i];
            }else{
                Cumul[0][i]=Cumul[0][i-1]+histo[0][i];
                Cumul[1][i]=Cumul[1][i-1]+histo[1][i];
                Cumul[2][i]=Cumul[2][i-1]+histo[2][i];
            }
        }

        for(int p=0; p<pixelMap.length; p++){
            int r = red(pixelMap[p]);
            int b = blue(pixelMap[p]);
            int g = green(pixelMap[p]);
            r= (Cumul[0][r]*255)/(pixelMap.length);
            g= (Cumul[1][g]*255)/(pixelMap.length);
            b= (Cumul[2][b]*255)/(pixelMap.length);
            pixelMap[p] = Color.rgb(r,g,b);
        }

        Bitmap bmp2 = this.bmp.copy(Bitmap.Config.ARGB_8888, true);
        bmp2.setPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
        this.bmp = bmp2;

    }

    public void colorize (int valueC ){
        int[] pixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

        float[] hsv = new float[3];

        for (int p=0; p< pixelMap.length; p++){
            int r = red(pixelMap[p]);
            int g = green(pixelMap[p]);
            int b = blue(pixelMap[p]);
            RGBToHSV(r,g,b,hsv);
            hsv[0] = valueC;
            pixelMap[p]=HSVToColor(hsv);
        }
        Bitmap bmp2 = this.bmp.copy(Bitmap.Config.ARGB_8888, true);
        bmp2.setPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
        this.bmp = bmp2;
    }

    public double[] convolution(int n, float[][] masque, int[] pixelMap, int p, int width){

        double red, green, blue;
        red=green=blue=0;
        for(int u=-n; u<=n; u++){
            for(int v=-n; v<=n; v++){
                int indices = p+u+((width) *v);
                red+= red(pixelMap[indices])*(double)(masque[u+n][v+n]);
                green+= green(pixelMap[indices])*(double)(masque[u+n][v+n]);
                blue+= blue(pixelMap[indices])*(double)(masque[u+n][v+n]);
            }
        }
        double[] RGB = {red,green,blue};
        return RGB;

    }

    public int[] largerBitmap(int n) {
        int[] pixelMap = new int[this.width * this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
        int[] largerPixelMap = new int[(this.width+2*n) * (this.height+2*n)];

        /* fill the center of larger bitmap with this.bitmap pixels and the edges with black */
        for (int j=0; j<this.height+2*n; j++) {
            for (int i=0; i<this.width+2*n; i++) {
                int ls = j * (this.width+2*n) + i;
                if (i >= n && j >= n && i < this.width+n && j < this.height+n) {
                    int s = (j-n)*this.width +(i-n);
                    largerPixelMap[ls] = pixelMap[s];
                }
                else {
                    largerPixelMap[ls] = Color.rgb(0, 0, 0);
                }
            }
        }

        for (int j=0; j<this.height+2*n; j++) {
            for (int i = 0; i < this.width + 2 * n; i++) {
                int ls = j * (this.width+2*n) + i;
                /* bande du milieu haut */
                if (i >= n && j < n && i < (this.width + n)) {
                    largerPixelMap[ls] = largerPixelMap[ls + n * (this.width + 2 * n) - j * (this.width + 2 * n)];
                }
                /* bande du milieu bas */
                else if (i >= n && i < (this.width + n) && j >= this.height + n) {
                    largerPixelMap[ls] = largerPixelMap[ls - n * (this.width + 2 * n) + ((this.height + 2 * n) - (j+1)) * (this.width + 2 * n)];
                }
            }
        }

        for (int j=0; j<this.height+2*n; j++) {
            for (int i = 0; i < this.width + 2 * n; i++) {
                int ls = j * (this.width+2*n) + i;
                /* bande gauche */
                if (i<n) {
                    largerPixelMap[ls] = largerPixelMap[ls + n - i];
                }
                /* bande droite */
                else if (i>=this.width+n) {
                    largerPixelMap[ls] = largerPixelMap[ls - n + (this.width+2*n)-(i+1)];
                }
            }
        }
        return largerPixelMap;
    }

    public void averageConvolution(int n){
        int size = 2*n+1;
        int[] pixelMap = new int[this.width * this.height];
        int[] finalPixelMap = new int[this.width * this.height];
        int[] largerPixelMap = largerBitmap(n);
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

        for (int j=0; j<this.height; j++) {
            for (int i = 0; i < this.width; i++) {
                int s = j*this.width + i;
                finalPixelMap[s] = Color.rgb(0, 0, 0);
            }
        }

        float[][] masque = new float[size][size];
        for(int i=0; i<size; i++){
            for(int j=0; j<size; j++){
                masque[i][j]=(float)1.0/(size*size);
            }
        }

        for (int j=0; j<this.height+2*n; j++) {
            for (int i=0; i<this.width+2*n; i++) {
                int ls = j * (this.width+2*n) + i;
                if (i >= n && j >= n && i < this.width+n && j < this.height+n) {
                    int s = (j-n)*this.width + (i-n);
                    double[] RGB = convolution(n, masque, largerPixelMap, ls, this.width+2*n);
                    finalPixelMap[s] = Color.rgb((int) RGB[0], (int) RGB[1], (int) RGB[2]);
                }
            }
        }

        Bitmap bmp2 = this.bmp.copy(Bitmap.Config.ARGB_8888, true);
        bmp2.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);
        this.bmp = bmp2;
    }


    public void laplacian(){
        int[] pixelMap = new int[this.width *this.height];
        int[] finalPixelMap = new int[this.width *this.height];
        int[] largerPixelMap = largerBitmap(1);
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
        int gray;

        for (int j=0; j<this.height; j++) {
            for (int i = 0; i < this.width; i++) {
                int s = j*this.width + i;
                finalPixelMap[s] = Color.rgb(0, 0, 0);
            }
        }

        float[][] masque = new float[][]{       { 1,  1,  1},
                                                { 1, -8,  1},
                                                { 1,  1,  1}};


        for (int j=0; j<this.height+2; j++) {
            for (int i=0; i<this.width+2; i++) {
                int ls = j * (this.width + 2) + i;
                if (i >= 1 && j >= 1 && i < this.width + 1 && j < this.height + 1) {
                    int s = (j - 1) * this.width + (i - 1);

                    double[] RGB = convolution(1, masque, largerPixelMap, ls, this.width + 2);
                    RGB[0] = resizeValue(-4 * 255, 4 * 255, 255, RGB[0]);
                    RGB[1] = resizeValue(-4 * 255, 4 * 255, 255, RGB[1]);
                    RGB[2] = resizeValue(-4 * 255, 4 * 255, 255, RGB[2]);

                    gray = (int) (0.3 * RGB[0] + 0.59 * RGB[1] + 0.11 * RGB[2]);
                    if (gray < 20) {
                        gray = 0;
                    } else if (gray > 245) {
                        gray = 255;
                    }
                    finalPixelMap[s] = Color.rgb(gray, gray, gray);
                }
            }
        }
        Bitmap bmp2 = this.bmp.copy(Bitmap.Config.ARGB_8888, true);
        bmp2.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);
        this.bmp = bmp2;
    }

    private double resizeValue(int minV, int maxV, int max, double value){
        value = ((value + Math.abs(minV))/(maxV+ Math.abs(minV)))*max;
        return value;
    }


    public void gaussian(int n){
        int size = 2*n+1;
        int[] pixelMap = new int[this.width *this.height];
        int[] finalPixelMap = new int[this.width *this.height];
        int[] largerPixelMap = largerBitmap(n);
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

        for (int j=0; j<this.height; j++) {
            for (int i = 0; i < this.width; i++) {
                int s = j*this.width + i;
                finalPixelMap[s] = Color.rgb(0, 0, 0);
            }
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

        for (int j=0; j<this.height+2*n; j++) {
            for (int i=0; i<this.width+2*n; i++) {
                int ls = j * (this.width+2*n) + i;
                if (i >= n && j >= n && i < this.width+n && j < this.height+n) {
                    int s = (j-n)*this.width + (i-n);
                    double[] RGB = convolution(n,masque,largerPixelMap,ls,this.width+2*n);
                    finalPixelMap[s] = Color.rgb((int)(RGB[0]/sum), (int)(RGB[1]/sum),(int)(RGB[2]/sum));                }
            }
        }

        Bitmap bmp2 = this.bmp.copy(Bitmap.Config.ARGB_8888, true);
        bmp2.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);
        this.bmp = bmp2;
    }

    public void sobelConvolution( ){
        int[] pixelMap = new int[this.width *this.height];
        int[] finalPixelMap = new int[this.width *this.height];
        int[] largerPixelMap = largerBitmap(1);
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
        double redT, greenT, blueT;
        int gray;

        for (int j=0; j<this.height; j++) {
            for (int i = 0; i < this.width; i++) {
                int s = j*this.width + i;
                finalPixelMap[s] = Color.rgb(0, 0, 0);
            }
        }

        float[][] masque2 = new float[][]{      {-1,-2,-1},
                                                { 0, 0, 0},
                                                { 1, 2, 1}};

        float[][] masque1 = new float[][]{      {-1, 0, 1},
                                                {-2, 0, 2},
                                                {-1, 0, 1}};


        for (int j=0; j<this.height+2; j++) {
            for (int i=0; i<this.width+2; i++) {
                int ls = j * (this.width+2) + i;
                if (i >= 1 && j >= 1 && i < this.width+1 && j < this.height+1) {
                    int s = (j-1)*this.width + (i-1);

                    double[] RGBX = convolution(1,masque1, largerPixelMap, ls, this.width+2);
                    double[] RGBY = convolution(1,masque2, largerPixelMap, ls, this.width+2);

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
                    finalPixelMap[s] = Color.rgb( gray,gray,gray);
                }
            }
        }

        Bitmap bmp2 = this.bmp.copy(Bitmap.Config.ARGB_8888, true);
        bmp2.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);
        this.bmp = bmp2;
    }

    public void brightness(int n){

        int[] pixelMap = new int[this.width *this.height];
        int[] finalPixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

        for(int s =0; s< finalPixelMap.length; s++){
            finalPixelMap[s]=Color.rgb(0,0,0);
        }

        for(int p=0; p<pixelMap.length; p++){
            int red, blue, green;
            if(n>=0){
                int value = 255-n;
                if(red(pixelMap[p])<=value){
                    red = red(pixelMap[p])+n;
                }else {
                    red = 255;
                }
                if(green(pixelMap[p])<=value){
                    green = green(pixelMap[p])+n;
                }else {
                    green = 255;
                }
                if(blue(pixelMap[p])<=value){
                    blue = blue(pixelMap[p])+n;
                }else {
                    blue = 255;
                }
            }else{
                int value = 0-n;
                if(red(pixelMap[p])>=value){
                    red = red(pixelMap[p])+n;
                }else {
                    red = 0;
                }
                if(green(pixelMap[p])>=value){
                    green = green(pixelMap[p])+n;
                }else {
                    green = 0;
                }
                if(blue(pixelMap[p])>=value){
                    blue = blue(pixelMap[p])+n;
                }else {
                    blue = 0;
                }
            }


            finalPixelMap[p]=Color.rgb(red,green,blue);
        }
        this.bmp.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);

    }

    public void contrast(float n){

        int[] pixelMap = new int[this.width *this.height];
        int[] finalPixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

        for(int s =0; s< finalPixelMap.length; s++){
            finalPixelMap[s]=Color.rgb(0,0,0);
        }

        float factor = (259*(n+255))/(255*(259-n));

        for(int p=0; p<pixelMap.length; p++){

            float red, blue, green;
            red = factor*(red(pixelMap[p])-128) +128;
            green = factor*(green(pixelMap[p])-128) +128;
            blue = factor*(blue(pixelMap[p])-128) +128;

            if (red > 255) {
                red = 255;
            } else if (red < 0) {
                red = 0;
            }
            if (green > 255) {
                green = 255;
            } else if (green < 0) {
                green = 0;
            }
            if (blue > 255) {
                blue = 255;
            } else if (blue < 0) {
                blue = 0;
            }


            finalPixelMap[p]=Color.rgb((int)red,(int)green,(int)blue);
        }

        this.bmp.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);

    }

    public void oneColor(int valueC){

        int[] pixelMap = new int[this.width *this.height];
        int[] finalPixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

        for (int p=0; p< pixelMap.length; p++){
            int r = red(pixelMap[p]);
            int g = green(pixelMap[p]);
            int b = blue(pixelMap[p]);

            float[] hue = new float[3];
            RGBToHSV(r,g,b,hue);

            int valueMax = (valueC +20)%361;
            int valueMin = (valueC -20)%361;

            if(hue[0]<valueMin || hue[0]>valueMax){
                int gray = (int) (0.3*r+0.59*g+0.11*b);

                finalPixelMap[p] = Color.rgb( gray,gray,gray);
            }else {
                finalPixelMap[p] = pixelMap[p];
            }
        }

        Bitmap bmp2 = this.bmp.copy(Bitmap.Config.ARGB_8888, true);
        bmp2.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);
        this.bmp = bmp2;
    }

    /*public void toSepiaRS(Bitmap bmp, Context context) {
        RenderScript rs = RenderScript.create(context);

        Allocation input = Allocation.createFromBitmap(rs, bmp);
        Allocation output = Allocation.createTyped(rs, input.getType());

        ScriptC_sepia sepiaScript = new ScriptC_sepia(rs);

        sepiaScript.forEach_toSepia(input, output);
        output.copyTo(bmp);

        input.destroy();
        output.destroy();
        sepiaScript.destroy();
        rs.destroy();
    }*/

    public void sepia(){
        int[] pixelMap = new int[this.width *this.height];
        int[] finalPixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

        for (int p=0; p< pixelMap.length; p++) {
            int red = red(pixelMap[p]);
            int green = green(pixelMap[p]);
            int blue = blue(pixelMap[p]);
            int tRed = (int) (0.393*red + 0.769*green + 0.189*blue);
            int tGreen = (int) (0.349*red + 0.686*green + 0.168*blue);
            int tBlue = (int) (0.272*red + 0.534*green + 0.131*blue);

            if (tRed > 255) {
                tRed = 255;
            } else if (tRed < 0) {
                tRed = 0;
            }
            if (tGreen > 255) {
                tGreen = 255;
            } else if (tGreen < 0) {
                tGreen = 0;
            }
            if (tBlue > 255) {
                tBlue = 255;
            } else if (blue < 0) {
                tBlue = 0;
            }
            finalPixelMap[p] = Color.rgb( tRed,tGreen,tBlue);
        }
        Bitmap bmp2 = this.bmp.copy(Bitmap.Config.ARGB_8888, true);
        bmp2.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);
        this.bmp = bmp2;

    }

    public void invert() {
        int[] pixelMap = new int[this.width * this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0, 0, this.width, this.height);

        for (int p=0; p<pixelMap.length; p++) {
            int red = red(pixelMap[p]);
            int green = green(pixelMap[p]);
            int blue = blue(pixelMap[p]);

            int redInvert = 255 - red;
            int greenInvert = 255 - green;
            int blueInvert = 255 - blue;
            pixelMap[p] = Color.rgb(redInvert, greenInvert, blueInvert);
        }
        Bitmap bmp2 = this.bmp.copy(Bitmap.Config.ARGB_8888, true);
        bmp2.setPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
        this.bmp = bmp2;
    }

    /*public void invert() {

        int masque = 0x00ffffffff;
        int[] pixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
        for (int p=0; p< pixelMap.length; p++) {
            pixelMap[p] ^= masque;
        }
        this.bmp.setPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
    }*/



    private int colordodge(int in1, int in2) {
        float image = (float) in2;
        float mask = (float) in1;
        return ((int) ((image == 255) ? image : Math.min(255, (((long) mask << 8) / (255 - image)))));

    }


    public void cartoon(Bitmap color, Bitmap edges){


        int[] pixelMapC = new int[this.width *this.height];
        color.getPixels(pixelMapC, 0, this.width, 0,0, this.width, this.height);

        int[] pixelMapE = new int[this.width *this.height];
        edges.getPixels(pixelMapE, 0, this.width, 0,0, this.width, this.height);


        for(int p =0; p< pixelMapC.length; p++){
            if(red(pixelMapE[p])!=0){
                pixelMapC[p]= Color.rgb(0,0,0);
            }
        }

        this.bmp.setPixels(pixelMapC, 0, this.width, 0,0, this.width, this.height);
    }

    public void clusteringCube(int nbColor){
        int[] pixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
        ColorCube cc = new ColorCube(pixelMap);
        cc.clustering(nbColor);
        this.bmp.setPixels(cc.getPixelMap(), 0, this.width, 0,0, this.width, this.height);
    }


}
