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

        this.bmp.setPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

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
        this.bmp.setPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
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

            if(p>n*this.width && p<((this.width *this.height)-n*this.width) && (p%this.width)>(n-1)
                    && (p%this.width)<(this.width -n)){

                double[] RGB = convolution(n,masque,pixelMap,p);
                finalPixelMap[p] = Color.rgb((int)RGB[0], (int)RGB[1],(int)RGB[2]);
            }
        }

        this.bmp.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);

    }

    public void laplacian( ){
        int[] pixelMap = new int[this.width *this.height];
        int[] finalPixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);
        int gray;

        for(int s =0; s< finalPixelMap.length; s++){
            finalPixelMap[s]=Color.rgb(0,0,0);
        }

        float[][] masque = new float[][]{       { 1,  1,  1},
                                                { 1, -8,  1},
                                                { 1,  1,  1}};


        double[][] RGB_min_max = new double[][] {{0,0,0}, {0,0,0}};
        for(int p =0 ; p<pixelMap.length; p++){

            if(p>this.width && p<((this.width *this.height)-this.width) && (p%this.width)>(0) && (p%this.width)<(this.width -1)){

                double[] RGB = convolution(1,masque, pixelMap, p);

                RGB[0]=resizeValue(-4*255, 4*255, 255, RGB[0]);
                RGB[1]=resizeValue(-4*255, 4*255, 255, RGB[1]);
                RGB[2]=resizeValue(-4*255, 4*255, 255, RGB[2]);

                gray = (int) (0.3*RGB[0]+0.59*RGB[1]+0.11*RGB[2]);
                if(gray<20){
                    gray=0;
                }else if (gray>245){
                    gray=255;
                }
                finalPixelMap[p] = Color.rgb( gray,gray,gray);
            }else {
                finalPixelMap[p] = Color.rgb(0,0,0);
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
        this.bmp.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);
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

        this.bmp.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);
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
        this.bmp.setPixels(finalPixelMap, 0, this.width, 0,0, this.width, this.height);

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
        this.bmp.setPixels(pixelMap, 0, this.width, 0, 0, this.width, this.height);
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


    public void ColorDodgeBlend(Bitmap source, Bitmap layer) {
        Bitmap base = source.copy(Bitmap.Config.ARGB_8888, true);
        Bitmap blend = layer.copy(Bitmap.Config.ARGB_8888, false);

        IntBuffer buffBase = IntBuffer.allocate(base.getWidth() * base.getHeight());
        base.copyPixelsToBuffer(buffBase);
        buffBase.rewind();

        IntBuffer buffBlend = IntBuffer.allocate(blend.getWidth() * blend.getHeight());
        blend.copyPixelsToBuffer(buffBlend);
        buffBlend.rewind();

        IntBuffer buffOut = IntBuffer.allocate(base.getWidth() * base.getHeight());
        buffOut.rewind();

        while (buffOut.position() < buffOut.limit()) {
            int filterInt = buffBlend.get();
            int srcInt = buffBase.get();

            int redValueFilter = Color.red(filterInt);
            int greenValueFilter = Color.green(filterInt);
            int blueValueFilter = Color.blue(filterInt);

            int redValueSrc = Color.red(srcInt);
            int greenValueSrc = Color.green(srcInt);
            int blueValueSrc = Color.blue(srcInt);

            int redValueFinal = colordodge(redValueFilter, redValueSrc);
            int greenValueFinal = colordodge(greenValueFilter, greenValueSrc);
            int blueValueFinal = colordodge(blueValueFilter, blueValueSrc);

            int pixel = Color.argb(255, redValueFinal, greenValueFinal, blueValueFinal);

            buffOut.put(pixel);
        }

        buffOut.rewind();

        base.copyPixelsFromBuffer(buffOut);
        blend.recycle();

        this.reload();
        this.bmp =base;
    }

    private int colordodge(int in1, int in2) {
        float image = (float) in2;
        float mask = (float) in1;
        return ((int) ((image == 255) ? image : Math.min(255, (((long) mask << 8) / (255 - image)))));

    }


    public void cartoon(Bitmap gauss, Bitmap lapla){


        int[] pixelMapG = new int[this.width *this.height];
        gauss.getPixels(pixelMapG, 0, this.width, 0,0, this.width, this.height);

        int[] pixelMapL = new int[this.width *this.height];
        lapla.getPixels(pixelMapL, 0, this.width, 0,0, this.width, this.height);

//        int[] pixelMapWhite = new int[this.width *this.height];
//        for (int p =0; p< pixelMapWhite.length; p++) {
//            pixelMapWhite[p]= Color.rgb(255,255,255);
//        }


        for(int p =0; p< pixelMapG.length; p++){
            if(red(pixelMapL[p])<126){
                pixelMapG[p]= Color.rgb(0,0,0);
            }
        }

        this.bmp.setPixels(pixelMapG, 0, this.width, 0,0, this.width, this.height);
    }



    public void medianCut(){
        int[] pixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

        int[][] histoRGB = histogram(pixelMap);
        int medianR, medianG, medianB, sumR, sumG, sumB;
        sumR=sumG=sumB=0;

        for(int i=0; i<256; i++){
            sumR+= histoRGB[0][i];
            sumG+= histoRGB[1][i];
            sumB+= histoRGB[2][i];
        }
        medianR=sumR/2;
        medianG=sumG/2;
        medianB=sumB/2;

        sumR=sumG=sumB=0;
        int iter=0;
        boolean findR, findG, findB;
        findR=findG=findB=false;
        while(!findR || !findG || !findB){

            sumR+= histoRGB[0][iter];
            sumG+= histoRGB[1][iter];
            sumB+= histoRGB[2][iter];

            if(sumR>=medianR){
                findR=true;
                medianR=iter;
            }
            if(sumG>=medianG){
                findG=true;
                medianG=iter;
            }
            if(sumB>=medianB){
                findB=true;
                medianB=iter;
            }
            iter++;
        }

        for (int p=0; p< pixelMap.length; p++) {
            int red, green, blue;

            if(red(pixelMap[p])<medianR){
                red = histoRGB[0][medianR/2];
            }else {
                red = histoRGB[0][(256+medianR)/2];
            }
            if(green(pixelMap[p])<medianG){
                green = histoRGB[1][medianG/2];
            }else {
                green = histoRGB[1][(256+medianG)/2];
            }
            if(blue(pixelMap[p])<medianB){
                blue = histoRGB[2][medianB/2];
            }else {
                blue = histoRGB[2][(256+medianB)/2];
            }

            pixelMap[p]=Color.rgb(red,green,blue);
        }
        this.bmp.setPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

    }

    public void clustering(){

        int[] pixelMap = new int[this.width *this.height];
        this.bmp.getPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

        for(int p =0; p< pixelMap.length; p++){
            int tmp = pixelMap[p]/16;
            pixelMap[p]=tmp*16;
        }

        this.bmp.setPixels(pixelMap, 0, this.width, 0,0, this.width, this.height);

    }


}
