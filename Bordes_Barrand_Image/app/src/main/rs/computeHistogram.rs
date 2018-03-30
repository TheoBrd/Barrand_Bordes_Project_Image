#pragma version(1)
#pragma rs java_package_name(com.example.tbarrand001.bordes_barrand_image)

rs_allocation histogramAllocation;

uchar4 RS_KERNEL computeHistogram(uchar4 in) {

    float4 pixelf = rsUnpackColor8888(in);
    histogramAllocation[3][256];
    for(int i=0; i<256; i++){
                histogramAllocation[0][i]=0;
                histogramAllocation[1][i]=0;
                histogramAllocation[2][i]=0;
    }
    for(int p=0; p < 256; p++){
                histo[0][pixelf.r[p]]++;
                histo[1][green(bmp[p])]++;
                histo[2][blue(bmp[p])]++;
            }
            return histo;

}