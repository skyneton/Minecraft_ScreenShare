package GetDesktop;

import static org.jocl.CL.*;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import org.jocl.*;

public class Renderer {
	static final int R[] = {0, 0, 0, 0, 89, 109, 127, 67, 174, 213, 247, 130, 140, 171, 199, 105, 180, 220, 255, 135, 112, 138, 160, 84, 117, 144, 167, 88, 0, 0, 0, 0, 180, 220, 255, 135, 115, 141, 164, 86, 106, 130, 151, 79, 79, 96, 112, 59, 45, 55, 64, 33, 100, 123, 143, 75, 180, 220, 255, 135, 152, 186, 216, 114, 125, 153, 178, 94, 72, 88, 102, 54, 161, 197, 229, 121, 89, 109, 127, 67, 170, 208, 242, 128, 53, 65, 76, 40, 108, 132, 153, 81, 53, 65, 76, 40, 89, 109, 127, 67, 36, 44, 51, 27, 72, 88, 102, 54, 72, 88, 102, 54, 108, 132, 153, 81, 17, 21, 25, 13, 176, 215, 250, 132, 64, 79, 92, 48, 52, 63, 74, 39, 0, 0, 0, 0, 91, 111, 129, 68, 79, 96, 112, 59, 147, 180, 209, 110, 112, 137, 159, 84, 105, 128, 149, 78, 79, 96, 112, 59, 131, 160, 186, 98, 72, 88, 103, 54, 112, 138, 160, 84, 40, 49, 57, 30, 95, 116, 135, 71, 61, 75, 87, 46, 86, 105, 122, 64, 53, 65, 76, 40, 53, 65, 76, 40, 53, 65, 76, 40, 100, 122, 142, 75, 26, 31, 37, 19};
	static final int G[] = {0, 0, 0, 0, 125, 153, 178, 94, 164, 201, 233, 123, 140, 171, 199, 105, 0, 0, 0, 0, 112, 138, 160, 84, 117, 144, 167, 88, 87, 106, 124, 65, 180, 220, 255, 135, 118, 144, 168, 88, 76, 94, 109, 57, 79, 96, 112, 59, 45, 55, 64, 33, 84, 102, 119, 63, 177, 217, 252, 133, 89, 109, 127, 67, 53, 65, 76, 40, 108, 132, 153, 81, 161, 197, 229, 121, 144, 176, 204, 108, 89, 109, 127, 67, 53, 65, 76, 40, 108, 132, 153, 81, 89, 109, 127, 67, 44, 54, 63, 33, 53, 65, 76, 40, 53, 65, 76, 40, 89, 109, 127, 67, 36, 44, 51, 27, 17, 21, 25, 13, 168, 205, 238, 126, 154, 188, 219, 115, 90, 110, 128, 67, 153, 187, 217, 114, 60, 74, 86, 45, 1, 1, 2, 1, 124, 152, 177, 93, 57, 70, 82, 43, 61, 75, 87, 46, 76, 93, 108, 57, 93, 114, 133, 70, 82, 100, 117, 61, 54, 66, 77, 40, 28, 35, 41, 21, 75, 92, 107, 56, 64, 79, 92, 48, 51, 62, 73, 38, 43, 53, 62, 32, 35, 43, 50, 26, 57, 70, 82, 43, 42, 51, 60, 31, 15, 18, 22, 11};
	static final int B[] = {0, 0, 0, 0, 39, 48, 56, 29, 115, 140, 163, 86, 140, 171, 199, 105, 0, 0, 0, 0, 180, 220, 255, 135, 117, 144, 167, 88, 0, 0, 0, 0, 180, 220, 255, 135, 129, 158, 184, 97, 54, 66, 77, 40, 79, 96, 112, 59, 180, 220, 255, 135, 50, 62, 72, 38, 172, 211, 245, 129, 36, 44, 51, 27, 152, 186, 216, 114, 152, 186, 216, 114, 36, 44, 51, 27, 17, 21, 25, 13, 116, 142, 165, 87, 53, 65, 76, 40, 108, 132, 153, 81, 108, 132, 153, 81, 125, 153, 178, 94, 125, 153, 178, 94, 36, 44, 51, 27, 36, 44, 51, 27, 36, 44, 51, 27, 17, 21, 25, 13, 54, 66, 77, 40, 150, 183, 213, 112, 180, 220, 255, 135, 40, 50, 58, 30, 34, 42, 49, 25, 0, 0, 0, 0, 113, 138, 161, 85, 25, 31, 36, 19, 76, 93, 108, 57, 97, 119, 138, 73, 25, 31, 36, 19, 37, 45, 53, 28, 55, 67, 78, 41, 24, 30, 35, 18, 69, 84, 98, 51, 64, 79, 92, 48, 62, 75, 88, 46, 64, 79, 92, 48, 24, 30, 35, 18, 29, 36, 42, 22, 32, 39, 46, 24, 11, 13, 16, 8};
	
	private BufferedImage inputImage;
	private byte[] buf;
	private cl_context context;
	private cl_command_queue commandQueue;
	private cl_program program;
	private cl_kernel kernel;
	private cl_mem inputImageMem;
	private Pointer pbuf;
	private cl_mem cl_buf;
	private cl_mem cl_R;
	private cl_mem cl_G;
	private cl_mem cl_B;
	private int imageSizeX = 128;
	private int imageSizeY = 128;
	
	public Renderer(int x, int y) {
		imageSizeX = x;
		imageSizeY = y;
		inputImage = new BufferedImage(imageSizeX, imageSizeY, BufferedImage.TYPE_INT_RGB);
		buf = new byte[x * y]; 
		initCL();
		initImageMem();
	}
	
//	public String readFile() {
//		String s = "";
//		//try(BufferedReader br = new BufferedReader(new FileReader(new File("res/program.cl")))) {
//		try(BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("res/program.cl")))) {
//		//try(BufferedReader br = new BufferedReader(new FileReader(new File(ClassLoader.getSystemResource("res/program.cl").toURI())))) {
//		for (String ss = br.readLine(); ss != null; ss = br.readLine()) {
//		s += ss + "\n";
//		}
//		} catch (Exception e) {
//		}
//		return s;
//	}
	
	void initCL() {
		final int platformIndex = 0;
		final long deviceType = CL_DEVICE_TYPE_ALL;
		final int deviceIndex = 0;
		
		// Enable exceptions and subsequently omit error checks in this sample
		// CL.setExceptionsEnabled(true);
		
		// Obtain the number of platforms
		int numPlatformsArray[] = new int[1];
		clGetPlatformIDs(0, null, numPlatformsArray);
		int numPlatforms = numPlatformsArray[0];
		
		// Obtain a platform ID
		cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
		clGetPlatformIDs(platforms.length, platforms, null);
		cl_platform_id platform = platforms[platformIndex];
		
		// Initialize the context properties
		cl_context_properties contextProperties = new cl_context_properties();
		contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
		
		// Obtain the number of devices for the platform
		int numDevicesArray[] = new int[1];
		clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
		int numDevices = numDevicesArray[0];
		
		// Obtain a device ID 
		cl_device_id devices[] = new cl_device_id[numDevices];
		clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
		cl_device_id device = devices[deviceIndex];
		
		// Create a context for the selected device
		context = clCreateContext(
		contextProperties, 1, new cl_device_id[]{device}, null, null, null);
		
		// Check if images are supported
		int imageSupport[] = new int[1];
		clGetDeviceInfo (device, CL.CL_DEVICE_IMAGE_SUPPORT,
		Sizeof.cl_int, Pointer.to(imageSupport), null);
		System.out.println("Images supported: "+(imageSupport[0]==1));
		if (imageSupport[0]==0) {
			System.out.println("Images are not supported");
			System.exit(1);
			return;
		}
		
		// Create a command-queue for the selected device
		cl_queue_properties properties = new cl_queue_properties();
		properties.addProperty(CL_QUEUE_PROFILING_ENABLE, 1);
		properties.addProperty(CL_QUEUE_OUT_OF_ORDER_EXEC_MODE_ENABLE, 1);
		commandQueue = clCreateCommandQueueWithProperties(
		context, device, properties, null);
		
		// Create the program
		System.out.println("Creating program...");
		/*program = clCreateProgramWithSource(context,
		1, new String[]{ readFile() }, null, null);*/
		program = clCreateProgramWithSource(context,
		 1, new String[]{ OpenClProgram.program }, null, null);
		
		// Build the program
		System.out.println("Building program...");
		clBuildProgram(program, 0, null, null, null, null);
		
		// Create the kernel
		System.out.println("Creating kernel...");
		kernel = clCreateKernel(program, "imageToBytes", null);
		
		pbuf = Pointer.to(buf);
		
		cl_R = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 207, Pointer.to(R), null);
		cl_G = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 207, Pointer.to(G), null);
		cl_B = clCreateBuffer(context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR, Sizeof.cl_int * 207, Pointer.to(B), null);
		cl_buf = clCreateBuffer(context, CL_MEM_WRITE_ONLY, Sizeof.cl_char * imageSizeX * imageSizeY , null, null);
	}
	
	public void finalizeCL() {
		clReleaseMemObject(inputImageMem);
		clReleaseMemObject(cl_buf); 
		clReleaseMemObject(cl_R);
		clReleaseMemObject(cl_G);
		clReleaseMemObject(cl_B); 
		clReleaseKernel(kernel);
		clReleaseProgram(program);
		clReleaseCommandQueue(commandQueue);
		clReleaseContext(context);
	}
	
	@SuppressWarnings("deprecation")
	private void initImageMem() {
		// Create the memory object for the input- and output image
		DataBufferInt dataBufferSrc =
		(DataBufferInt)inputImage.getRaster().getDataBuffer();
		int dataSrc[] = dataBufferSrc.getData();
		
		cl_image_format imageFormat = new cl_image_format();
		imageFormat.image_channel_order = CL_RGBA;
		imageFormat.image_channel_data_type = CL_UNSIGNED_INT8;
		
		inputImageMem = clCreateImage2D(
		context, CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR,
		new cl_image_format[]{imageFormat}, imageSizeX, imageSizeY,
		imageSizeX * Sizeof.cl_uint, Pointer.to(dataSrc), null);
	}
		
	byte[] render(Image i) {
		Graphics g = inputImage.getGraphics();
		g.drawImage(i, 0, 0, null);
		g.dispose();
		initImageMem();
		
		// Set up the work size and arguments, and execute the kernel
		long globalWorkSize[] = new long[2];
		globalWorkSize[0] = imageSizeX;
		globalWorkSize[1] = imageSizeY;
		int a = 0;
		clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(inputImageMem));
		clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(cl_buf));
		clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(cl_B));
		clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(cl_G));
		clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(cl_R));
		clSetKernelArg(kernel, a++, Sizeof.cl_int, Pointer.to(new int[] {207}));
		clEnqueueNDRangeKernel(commandQueue, kernel, 2, null,
		globalWorkSize, null, 0, null, null);
		
		// Read the pixel data into the output image
		clEnqueueReadBuffer(commandQueue, cl_buf, CL_TRUE, 0, Sizeof.cl_char * buf.length, pbuf, 0, null, null);
		clReleaseMemObject(inputImageMem);
		return buf;
	}

}