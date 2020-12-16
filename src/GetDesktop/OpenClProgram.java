package GetDesktop;

public class OpenClProgram {
   public static String program = "#pragma OPENCL EXTENSION cl_khr_fp16 : enable\r\n" + 
         "\r\n" + 
         "const sampler_t samplerIn = CLK_NORMALIZED_COORDS_FALSE | CLK_ADDRESS_CLAMP | CLK_FILTER_NEAREST;\r\n" + 
         "\r\n" + 
         "int getDistance(int r1, int g1, int b1, int r2, int g2, int b2) {\r\n" + 
         "    int rmean = (r1 + r2) / 2;\r\n" + 
         "   int r = r1 - r2; \r\n" + 
         "   int g = g1 - g2; \r\n" + 
         "   int b = b1 - b2;\r\n" + 
         "   int weightR = 2 + rmean / 256;\r\n" + 
         "    int weightG = 4.0;\r\n" + 
         "    int weightB = 2 + (255 - rmean) / 256;\r\n" + 
         "   return weightR * r * r + weightG * g * g + weightB * b * b;\r\n" + 
         "}\r\n" + 
         "\r\n" + 
         "int matchColor(int r, int g, int b, __global int *pr, __global int *pg, __global int *pb, const int size) {\r\n" + 
         "   int index = 0;\r\n" + 
         "   double best = -1;\r\n" + 
         "   for (int i = 4; i < size; i++) {\r\n" + 
         "      double distance = getDistance(r, g, b, pr[i], pg[i], pb[i]);\r\n" + 
         "      if (distance < best || best == -1) {\r\n" + 
         "         best = distance;\r\n" + 
         "         index = i;\r\n" + 
         "      }\r\n" + 
         "   }\r\n" + 
         "   return index;  \r\n" + 
         "}\r\n" + 
         "\r\n" + 
         "__kernel void imageToBytes(__read_only image2d_t sourceImage, __global char *buf, __global int *R, __global int *G, __global int *B, const int size) {\r\n" + 
         "   int gidX = get_global_id(0);\r\n" + 
         "   int gidY = get_global_id(1);\r\n" + 
         "   int w = get_image_width(sourceImage);\r\n" + 
         "   int2 pos = (int2)(gidX, gidY);\r\n" + 
         "   uint4 pixel = read_imageui(sourceImage, samplerIn, pos);\r\n" + 
         "   buf[gidY * w + gidX] = matchColor(pixel.x, pixel.y, pixel.z, R, G, B, size);\r\n" + 
         "}\r\n" + 
         "\r\n" + 
         "/*__kernel void imageToBytes(__read_only image2d_t sourceImage, __global char *buf, __global int *R, __global int *G, __global int *B, __constant int size) {\r\n" + 
         "   int gidX = get_global_id(0);\r\n" + 
         "   int gidY = get_global_id(1);\r\n" + 
         "   int w = get_image_width(sourceImage);\r\n" + 
         "   int h = get_image_height(sourceImage); \r\n" + 
         "   int index = 0;\r\n" + 
         "   int best = -1;\r\n" + 
         "   int2 pos = (int2)(gidX, gidY);\r\n" + 
         "   uint4 pixel = read_imageui(sourceImage, samplerIn, pos);\r\n" + 
         "   for (int i = 4; i < size; ++i) {\r\n" + 
         "      int r = pixel.x - R[i]; \r\n" + 
         "      int g = pixel.y - G[i]; \r\n" + 
         "      int b = pixel.z - B[i]; \r\n" + 
         "      int weightR = 10; \r\n" + 
         "      int weightG = 10; \r\n" + 
         "      int weightB = 10; \r\n" + 
         "      int distance = weightR * r * r + weightG * g * g + weightB * b * b;\r\n" + 
         "      if (distance < best || best == -1) {\r\n" + 
         "         best = distance;\r\n" + 
         "         index = i;\r\n" + 
         "      }\r\n" + 
         "   }\r\n" + 
         "   buf[gidY * w + gidX] = index;\r\n" + 
         "}*/";

}