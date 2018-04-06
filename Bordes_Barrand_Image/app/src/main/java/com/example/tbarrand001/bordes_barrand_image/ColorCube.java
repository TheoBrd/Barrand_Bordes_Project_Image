package com.example.tbarrand001.bordes_barrand_image;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Created by Theo on 23/03/2018.
 */

public class ColorCube {

    private int rmin;
    private int rmax;
    private int gmin;
    private int gmax;
    private int bmin;
    private int bmax;
    private int averageColor;
    private int[][] histogramRGB;


    ColorCube(int[][] histoPixel){

        this.averageColor=-1;
        this.histogramRGB = histoPixel;
        this.rmin=-1;
        this.gmin=-1;
        this.bmin=-1;


        for (int p = 0; p<histogramRGB.length; p++){

            if(histogramRGB[p][0]>0){
                this.rmax=p;
                if(this.rmin==-1){
                    rmin=p;
                }
            }
            if(histogramRGB[p][1]>0){
                this.gmax=p;
                if(this.gmin==-1){
                    gmin=p;
                }
            }
            if(histogramRGB[p][2]>0){
                this.bmax=p;
                if(this.bmin==-1){
                    bmin=p;
                }
            }
        }

    }

    private int getMedian(int RGB){
        int sum =0;
        for (int p = 0; p<histogramRGB.length; p++){
            sum+= histogramRGB[p][RGB];
        }

        int median = sum/2;
        int indiceMedian=0;

        sum=0;
        while (sum<median){
            sum+=histogramRGB[indiceMedian][RGB];
            indiceMedian++;
        }
        return indiceMedian;
    }


    private int maxEdges(){
        int R = rmax-rmin;
        int G = gmax-gmin;
        int B = bmax-bmin;

        if(R>=B && R>=G){
            return 0;
        }else if(G>=R && G >=B){
            return 1;
        }else if (B>=R && B>=G){
            return 2;
        }
        return -1;
    }



    public ArrayList<ColorCube> medianCutBox(int indiceMedian, int RGB){
        int edge = maxEdges();
        if(edge!=-1){
            int median = getMedian(edge);
            int[][] histo1, histo2;
            histo1=new int[median+1][3];
            histo2=new int[histogramRGB.length - median-1][3];
            for(int i = 0; i<histogramRGB.length; i++ ){
                if(i<=median){
                    histo1[i][0] = histogramRGB[i][0];
                    histo1[i][1] = histogramRGB[i][1];
                    histo1[i][2] = histogramRGB[i][2];
                }else{
                    histo2[i][0] = histogramRGB[i][0];
                    histo2[i][1] = histogramRGB[i][1];
                    histo2[i][2] = histogramRGB[i][2];
                }
            }

            ColorCube c1 = new ColorCube(histo1);
            ColorCube c2 = new ColorCube(histo2);

            ArrayList<ColorCube> listeC = new ArrayList<ColorCube>();
            listeC.add(c1);
            listeC.add(c2);

            return listeC;

        }else {
            return null;
        }
    }

}
