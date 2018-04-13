#pragma version(1)
#pragma rs java_package_name(com.example.tbarrand001.bordes_barrand_image)

uchar4 RS_KERNEL sepia(uchar4 in) {

    float4 pixelf = rsUnpackColor8888(in);
    float tRed = (0.393*pixelf.r + 0.769*pixelf.g + 0.189*pixelf.b);
    float tGreen = (0.349*pixelf.r + 0.686*pixelf.g + 0.168*pixelf.b);
    float tBlue = (0.272*pixelf.r + 0.534*pixelf.g + 0.131*pixelf.b);

    if (tRed > 255) {
            tRed = 255;
        }
        else if (tRed < 0) {
            tRed = 0;
        }
        if (tGreen > 255) {
            tGreen = 255;
        }
        else if (tGreen < 0) {
            tGreen = 0;
        }
        if (tBlue > 255) {
           tBlue = 255;
        }
        else if (tBlue < 0) {
            tBlue = 0;
        }

    return rsPackColorTo8888(tRed, tGreen, tBlue, pixelf.a);
}





