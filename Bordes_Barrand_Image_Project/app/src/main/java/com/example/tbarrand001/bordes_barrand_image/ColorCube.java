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
    private static final int ITERATION_CLUSTER = 15;

    ColorCube(int[] pixelMaptmp){
        this.pixelMap = pixelMaptmp;
        histo = new int[256];

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

    private double labFunction(double x){
        if (x> 0.008856){
            x= x/3.0;
            return x;
        }else {
            x=(7.787 * x + 0.138);
            return x;
        }
    }


    private double[] RGBToLab(int R, int G, int B, double[] Lab){

        double var_R = ( R / 255.0 );
        double var_G = ( G / 255.0 );
        double var_B = ( B / 255.0 );

        if( var_R > 0.04045 ) {
            var_R = Math.pow((var_R + 0.055) / 1.055 , 2.4);
        }else{
            var_R = var_R / 12.92;
        }

        if( var_G > 0.04045 ) {
            var_G = Math.pow (( var_G + 0.055 ) / 1.055 ,2.4);
        }else{
            var_G = var_G / 12.92;
        }

        if( var_B > 0.04045 ){
            var_B = Math.pow (( var_B + 0.055 ) / 1.055 ,2.4);
        }else{
            var_B = var_B / 12.92;
        }
        var_R *= 100.0;
        var_G *= 100.0;
        var_B *= 100.0;



        double X = 0.412453*var_R + 0.357580*var_G + 0.180423*var_B;
        double Y  =  0.212671*var_R + 0.715160*var_G + 0.072169*var_B;
        double Z  =  0.019334*var_R + 0.119193*var_G + 0.950227*var_B;

        double Xn = 041.2453 + 35.7580 + 18.0423;
        double Yn  =  21.2671 + 71.5160 + 07.2169;
        double Zn  =  01.9334 + 11.9193 +95.0227;

        double L, a, b;
        L = 116.0 * (Y/Yn) - 16.0;


        a= 500.0 * (labFunction(X/Xn)- labFunction(Y/Yn));
        b= 200.0 * (labFunction(Y/Yn)- labFunction(Z/Zn));

        Lab[0]=L;
        Lab[1]=a;
        Lab[2]=b;
        return Lab;
    }



<<<<<<< HEAD
    /*private double euclidianDist(int origine, int pixel){
        double[] LabOrigine;
        double[] LabPixel;
    }*/

   /* public void clustering(int n){
        int[] tabCenterCluster = new int [n];
=======
    private double euclideanDist(double[] origin, double[] pixel){


        return (((origin[0]-pixel[0])*(origin[0]-pixel[0])) +
                ((origin[1]-pixel[1])*(origin[1]-pixel[1])) +
                ((origin[2]-pixel[2])*(origin[2]-pixel[2])));
    }



    public void clustering(int nbColor){
        //ce tableau contient la liste de point centraux
        int[] tabCenterCluster = new int [nbColor];
        int iter=0;

        //cette liste contient les différents clusters
>>>>>>> d4a08296cf26af00b512d1ae3bf090f586e157a8
        ArrayList<ArrayList<Integer>> cluster = new ArrayList<ArrayList<Integer>>();
        ArrayList<double[]> pixelMapLab = new ArrayList();
        for (int pixel :pixelMap) {
            double[] pixelLab = new double[3];
            RGBToLab(red(pixel), green(pixel), blue(pixel), pixelLab);
            pixelMapLab.add(pixelLab);
        }



        //création des points aléatoirement
        for(int i =0; i<tabCenterCluster.length; i++){
            Random rand = new Random();

            tabCenterCluster[i]= Color.argb(rand.nextInt(256), rand.nextInt(rmax-rmin)+rmin,
                    rand.nextInt(gmax-gmin)+gmin,
                    rand.nextInt(bmax-bmin)+bmin);

            cluster.add(new ArrayList<Integer>());
        }
        ArrayList<double[]> centerClusterLab = new ArrayList();



        int iteration =0;
        while(iteration <ITERATION_CLUSTER){


            for (int center : tabCenterCluster) {
                double[] pixelLab = new double[3];
                RGBToLab(red(center), green(center), blue(center), pixelLab);
                centerClusterLab.add(pixelLab);
            }



            //pour chaque pixel, je regarde sa distance avec les points, et garde la plus petite
            //avant de ranger le pixel dans le cluster conrrespondant
            iter =0;
            for( double[] pixel : pixelMapLab){
                int numClust=0;
                double distE=Double.POSITIVE_INFINITY;
                int nb=0;
                for(double[] center : centerClusterLab){
                    double distTmp = euclideanDist(center, pixel);
                    if(distE >= distTmp){
                        distE = distTmp;
                        numClust = nb;
                    }
                    nb++;
                }
                cluster.get(numClust).add(pixelMap[iter]);
                iter++;
            }


            iter =0;
            for (ArrayList<Integer> arrayList: cluster){
                if(arrayList.size()!=0){
                    int sumR=0;
                    int sumG=0;
                    int sumB=0;
                    for (int pixel:arrayList) {
                        sumR+=red(pixel);
                        sumG+=green(pixel);
                        sumB+=blue(pixel);
                    }


                    int sum= Color.rgb(sumR/arrayList.size(), sumG/arrayList.size(), sumB/arrayList.size());
                    tabCenterCluster[iter] = sum;
                }

                iter++;
            }

            for (ArrayList<Integer> arrayList : cluster) {
                arrayList.clear();
            }
            centerClusterLab.clear();

            iteration++;
        }




        for (int center : tabCenterCluster) {
            double[] pixelLab = new double[3];
            RGBToLab(red(center), green(center), blue(center), pixelLab);
            centerClusterLab.add(pixelLab);
        }

        iter=0;
        for(double[] pixel : pixelMapLab){
            int numClust=0;
            double distE=Double.POSITIVE_INFINITY;
            int nb=0;
            for(double[] center : centerClusterLab){
                double distTmp =  euclideanDist(center, pixel);
                if(distE > distTmp){
                    distE = distTmp;
                    numClust = nb;
                }
                nb++;
            }

            pixelMap[iter]=tabCenterCluster[numClust];
            iter++;
        }
    }

    public int[] getPixelMap() {
        return pixelMap;
    }*/
}
