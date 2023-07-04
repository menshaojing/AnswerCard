package answercard;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * 通过文本查找区域的定位信息不精确，已抛弃
 * 
 * 
 * @author EDUTECH
 *
 */
public class ImageCorrection {
	static List<MatOfPoint> myContours = new ArrayList<MatOfPoint>();
	static {
    	System.out.println("Welcome to OpenCV VH " + Core.VERSION);
    	//String root=System.getProperty("user.dir");
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
       // System.load("D:\\OpenCV\\opencv4.5\\build\\java\\x64\\opencv_java450.dll");
    }
	public static void main(String[] args)
	{ 
		
		String path="D:\\workspaces\\AnswerCard\\img\\A3.jpg";
		 Mat img = Imgcodecs.imread(path);
		Mat mat= imgCorrection(img);
		 Imgcodecs.imwrite("D:\\workspaces\\AnswerCard\\img\\A3-ok1.jpg", mat);
	}
	public static Mat imgCorrection(Mat srcImage) {
		// 二值化
		Mat binary = ImgBinarization(srcImage);
		// 膨胀与腐蚀
		Mat preprocess = preprocess(binary);
		// 查找和筛选文字区域
	    List<RotatedRect> rects = findTextRegion(preprocess) ;
	    
	    for (int i = 0; i < rects.size(); i++) {
        	RotatedRect rr=rects.get(i);
        //	map=rects.get(i);
        	Rect rect= rr.boundingRect();
        	
            // Imgproc.drawContours(background, myContours, i, new Scalar(0, 255, 0), 4, 8);
        	Imgproc.rectangle(srcImage, new Point(rect.x+15,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar(0, 255, 0), 1,8,0);
        }
        HighGui.imshow("ces", srcImage);
        HighGui.waitKey(15);
	  //将获取到的矩形根据面积倒序  或 将被包含和重复的去掉
	    Mat correction = correction(rects,srcImage);
	    return correction;
	}
	//二值化
	public static Mat ImgBinarization(Mat srcImage){
        Mat gray_image = null;
        try {
        	gray_image = new Mat(srcImage.height(), srcImage.width(), CvType.CV_8UC1);
        	Imgproc.cvtColor(srcImage,gray_image,Imgproc.COLOR_RGB2GRAY);
		} catch (Exception e) {
			gray_image = srcImage.clone();
			gray_image.convertTo(gray_image, CvType.CV_8UC1);
			System.out.println("原文异常，已处理...");
		}
        Mat thresh_image = new Mat(srcImage.height(), srcImage.width(), CvType.CV_8UC1);
        Imgproc.threshold(gray_image, thresh_image,100, 255, Imgproc.THRESH_BINARY);
		return thresh_image;
	}
	/**
	 * 根据二值化图片进行膨胀与腐蚀
	 * 
	 */
	public static Mat preprocess(Mat binary){
		Mat dilation2 = new Mat();

	    //以下为修正值
	    Mat sobel = new Mat();
	    Imgproc.Sobel(binary, sobel, CvType.CV_8U, 1, 0, 3);

        //2.二值化
	    Imgproc.threshold(sobel, binary, 0, 255, Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY);

        //3. 膨胀和腐蚀操作的核函数
        Mat element1 = new Mat();
        Mat element2 = new Mat();
        Size size1 = new Size(36, 9);
        Size size2 = new Size(24, 6);

        element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, size1);
        element2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, size2);

        //4. 膨胀一次，让轮廓突出
        Mat dilation = new Mat();
        Imgproc.dilate(binary, dilation, element2, new Point(-1, -1), 2);

        //5. 腐蚀一次，去掉细节，如表格线等。注意这里去掉的是竖直的线
        Mat erosion = new Mat();
        Imgproc.erode(dilation, erosion, element1,new Point(-1, -1), 2);

        //6. 再次膨胀，让轮廓明显一些
        Imgproc.dilate(erosion, dilation2, element2, new Point(0,0), 2);
	    
	    return dilation2;
	}
	/**
	 * 文字区域
	 *
	 */
    public static List<RotatedRect> findTextRegion(Mat img)
    {
    	List<RotatedRect> rects = new ArrayList<RotatedRect>();
        //1.查找轮廓
    	List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    	
    	Mat hierarchy = new Mat();
        Imgproc.findContours(img, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE,new Point(3,3));
        int img_width = img.width();
        int img_height = img.height();
        int size = contours.size();
       
//        for (int i = 0; i < contours.size(); i++) {
//        	   RotatedRect rr = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
//               Rect rect=rr.boundingRect();
//           // Imgproc.drawContours(background, myContours, i, new Scalar(0, 255, 0), 4, 8);
//        	Imgproc.rectangle(img, new Point(rect.x+15,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar(0, 255, 0), 1);
//        }
//        HighGui.imshow("ces111", img);
//        HighGui.waitKey(15);
//        
        //2.筛选那些面积小的
        for (int i = 0; i < size; i++){
        	double area = Imgproc.contourArea(contours.get(i));
	        if (area < 1000)
	            continue;
            //轮廓近似，作用较小，approxPolyDP函数有待研究
            double epsilon = 0.001*Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true);
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approxCurve, epsilon, true);
     
            //找到最小矩形，该矩形可能有方向
            RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            //计算高和宽
            int m_width = rect.boundingRect().width;
            int m_height = rect.boundingRect().height;
     
            //筛选那些太细的矩形，留下扁的
            if (m_width < m_height)
                continue;
            if(img_width == rect.boundingRect().br().x)
            	continue;
            if(img_height == rect.boundingRect().br().y)
            	continue;
            //符合条件的rect添加到rects集合中
          //  System.out.println(rect.boundingRect());
          //  myContours.add(contours.get(i));
            rects.add(rect);
        }
        Mat background = new Mat(img.size(), CvType.CV_8UC3, new Scalar(0, 0, 0));
        
      
        return rects;
    }
    /**
	 * 倾斜矫正
	 * @param rects 
	 */
	public static Mat correction(List<RotatedRect> rects,Mat srcImage) {
    	double degree = 0;
    	double degreeCount = 0;
    	for(int i = 0; i < rects.size();i++){
	    	if(rects.get(i).angle >= -90 && rects.get(i).angle < -45){
    			degree = rects.get(i).angle;
    			if(rects.get(i).angle != 0){
    				degree += 90;
    			}
	    	}
	    	if(rects.get(i).angle > -45 && rects.get(i).angle <= 0){
    			degree = rects.get(i).angle;
	    	}
	    	if(rects.get(i).angle <= 90 && rects.get(i).angle > 45){
	    		degree = rects.get(i).angle;
    			if(rects.get(i).angle != 0){
    				degree -= 90;
    			}
	    	}
	    	if(rects.get(i).angle < 45 && rects.get(i).angle >= 0){
	    		degree = rects.get(i).angle;
	    	}
	    	if(degree > -5 && degree < 5){
	    		degreeCount += degree;
	    	}
	    	
	    }
    	if(degreeCount != 0){
    		// 获取平均水平度数
    		degree = degreeCount/rects.size();
    	}
    	Point center = new Point(srcImage.cols() / 2, srcImage.rows() / 2);
    	Mat rotm = Imgproc.getRotationMatrix2D(center, degree, 1.0);    //获取仿射变换矩阵
    	Mat dst = new Mat();
    	Imgproc.warpAffine(srcImage, dst, rotm, srcImage.size(), Imgproc.INTER_LINEAR, 0, new Scalar(255, 255, 255));    // 进行图像旋转操作
    	return dst;
	}
}
