package com.example.tbarrand001.bordes_barrand_image;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.Random;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

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
    private String maxAxes;
    private int[] pixelMap;
    private int[] histo;

    ColorCube(int[] pixelMaptmp){
        this.pixelMap = pixelMaptmp;
        histo = new int[256];
        for(int i =0; i<histo.length; i++){
            histo[i]=0;
        }

        rmax = gmax = bmax =0;
        rmin=gmin=bmin=255;
        for(int p =0; p<this.pixelMap.length; p++){
            int r = red(pixelMap[p]);
            int g = green(pixelMap[p]);
            int b = blue(pixelMap[p]);

            if(r>rmax) rmax=r;
            if(r<rmin) rmin=r;
            if(g>gmax) gmax=g;
            if(g<gmin) gmin=g;
            if(b>bmax) bmax=b;
            if(b<bmin) bmin=b;
        }
        axes();
        histogram();

    }


    private void histogram(){
        switch (maxAxes){
            case "red":
                for(int p =0; p<this.pixelMap.length; p++){
                    histo[red(pixelMap[p])]++;
                }
                break;

            case "green":
                for(int p =0; p<this.pixelMap.length; p++){
                    histo[green(pixelMap[p])]++;
                }
                break;

            case "blue":
                for(int p =0; p<this.pixelMap.length; p++){
                    histo[blue(pixelMap[p])]++;
                }
                break;

            default: return;
        }

    }

    private void axes(){
        int R = rmax-rmin;
        int G = gmax-gmin;
        int B = bmax-bmin;

        maxAxes = "none";

        if(R>=B && R>=G){
            maxAxes = "red";
        }else if(G>=R && G>=B){
            maxAxes = "green";
        }else if(B>=R && B>=G){
            maxAxes = "blue";
        }
    }


    private double[] RGBToLab(int R, int G, int B, double[] Lab){

        return Lab;
    }



    /*private double euclidianDist(int origine, int pixel){
        double[] LabOrigine;
        double[] LabPixel;
    }*/

   /* public void clustering(int n){
        int[] tabCenterCluster = new int [n];
        ArrayList<ArrayList<Integer>> cluster = new ArrayList<ArrayList<Integer>>();
        for(int i =0; i<tabCenterCluster.length; i++){
            Random rand = new Random();
            int red  = rand.nextInt(256);
            int green  = rand.nextInt(256);
            int blue  = rand.nextInt(256);

            tabCenterCluster[i]= Color.rgb(red, green, blue);
            cluster.add(new ArrayList<Integer>());
        }

        int iteration =0;
        while(iteration <5){

            for(int p=0; p<pixelMap.length;p++){
                int numClust=0;
                double distE=Double.POSITIVE_INFINITY;
                for(int nb = 0; nb <tabCenterCluster.length; nb++){
                    if(distE > euclidianDist(tabCenterCluster[nb], pixelMap[nb])){
                        distE = euclidianDist(tabCenterCluster[nb], pixelMap[nb]);
                        numClust = nb;
                    }
                }
                cluster.get(numClust).add(pixelMap[p]);
            }

            int cpt=0;
            for (ArrayList<Integer> arrayList: cluster){
                int sum=0;
                for (int pixel:arrayList) {
                    sum+=pixel;
                }

                sum= sum/arrayList.size();
                tabCenterCluster[cpt] = sum;
                cpt++;
                arrayList.clear();
            }
            iteration++;
        }

        for(int p=0; p<pixelMap.length;p++){
            int numClust=0;
            double distE=Double.POSITIVE_INFINITY;
            for(int nb = 0; nb <tabCenterCluster.length; nb++){
                if(distE > euclidianDist(tabCenterCluster[nb], pixelMap[nb])){
                    distE = euclidianDist(tabCenterCluster[nb], pixelMap[nb]);
                    numClust = nb;
                }
            }

            pixelMap[p]=tabCenterCluster[numClust];
        }
    }

    public int[] getPixelMap() {
        return pixelMap;
    }*/
}
