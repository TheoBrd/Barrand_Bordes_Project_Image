#pragma version(1)
#pragma rs java_package_name(com.example.tbarrand001.bordes_barrand_image)

uchar4 RS_KERNEL toSepia(uchar4 in) {

    float4 pixelf = rsUnpackColor8888(in);
    float red = (0.393*pixelf.r + 0.769*pixelf.g + 0.189*pixelf.b);
    float green = (0.349*pixelf.r + 0.686*pixelf.g + 0.168*pixelf.b);
    float blue = (0.272*pixelf.r + 0.534*pixelf.g + 0.131*pixelf.b);

    return rsPackColorTo8888(red, green, blue, pixelf.a);
}


