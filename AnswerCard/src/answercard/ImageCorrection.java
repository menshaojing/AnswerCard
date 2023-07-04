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
 * ͨ���ı���������Ķ�λ��Ϣ����ȷ��������
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
		// ��ֵ��
		Mat binary = ImgBinarization(srcImage);
		// �����븯ʴ
		Mat preprocess = preprocess(binary);
		// ���Һ�ɸѡ��������
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
	  //����ȡ���ľ��θ����������  �� �����������ظ���ȥ��
	    Mat correction = correction(rects,srcImage);
	    return correction;
	}
	//��ֵ��
	public static Mat ImgBinarization(Mat srcImage){
        Mat gray_image = null;
        try {
        	gray_image = new Mat(srcImage.height(), srcImage.width(), CvType.CV_8UC1);
        	Imgproc.cvtColor(srcImage,gray_image,Imgproc.COLOR_RGB2GRAY);
		} catch (Exception e) {
			gray_image = srcImage.clone();
			gray_image.convertTo(gray_image, CvType.CV_8UC1);
			System.out.println("ԭ���쳣���Ѵ���...");
		}
        Mat thresh_image = new Mat(srcImage.height(), srcImage.width(), CvType.CV_8UC1);
        Imgproc.threshold(gray_image, thresh_image,100, 255, Imgproc.THRESH_BINARY);
		return thresh_image;
	}
	/**
	 * ���ݶ�ֵ��ͼƬ���������븯ʴ
	 * 
	 */
	public static Mat preprocess(Mat binary){
		Mat dilation2 = new Mat();

	    //����Ϊ����ֵ
	    Mat sobel = new Mat();
	    Imgproc.Sobel(binary, sobel, CvType.CV_8U, 1, 0, 3);

        //2.��ֵ��
	    Imgproc.threshold(sobel, binary, 0, 255, Imgproc.THRESH_OTSU | Imgproc.THRESH_BINARY);

        //3. ���ͺ͸�ʴ�����ĺ˺���
        Mat element1 = new Mat();
        Mat element2 = new Mat();
        Size size1 = new Size(36, 9);
        Size size2 = new Size(24, 6);

        element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, size1);
        element2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, size2);

        //4. ����һ�Σ�������ͻ��
        Mat dilation = new Mat();
        Imgproc.dilate(binary, dilation, element2, new Point(-1, -1), 2);

        //5. ��ʴһ�Σ�ȥ��ϸ�ڣ������ߵȡ�ע������ȥ��������ֱ����
        Mat erosion = new Mat();
        Imgproc.erode(dilation, erosion, element1,new Point(-1, -1), 2);

        //6. �ٴ����ͣ�����������һЩ
        Imgproc.dilate(erosion, dilation2, element2, new Point(0,0), 2);
	    
	    return dilation2;
	}
	/**
	 * ��������
	 *
	 */
    public static List<RotatedRect> findTextRegion(Mat img)
    {
    	List<RotatedRect> rects = new ArrayList<RotatedRect>();
        //1.��������
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
        //2.ɸѡ��Щ���С��
        for (int i = 0; i < size; i++){
        	double area = Imgproc.contourArea(contours.get(i));
	        if (area < 1000)
	            continue;
            //�������ƣ����ý�С��approxPolyDP�����д��о�
            double epsilon = 0.001*Imgproc.arcLength(new MatOfPoint2f(contours.get(i).toArray()), true);
            MatOfPoint2f approxCurve = new MatOfPoint2f();
            Imgproc.approxPolyDP(new MatOfPoint2f(contours.get(i).toArray()), approxCurve, epsilon, true);
     
            //�ҵ���С���Σ��þ��ο����з���
            RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            //����ߺͿ�
            int m_width = rect.boundingRect().width;
            int m_height = rect.boundingRect().height;
     
            //ɸѡ��Щ̫ϸ�ľ��Σ����±��
            if (m_width < m_height)
                continue;
            if(img_width == rect.boundingRect().br().x)
            	continue;
            if(img_height == rect.boundingRect().br().y)
            	continue;
            //����������rect��ӵ�rects������
          //  System.out.println(rect.boundingRect());
          //  myContours.add(contours.get(i));
            rects.add(rect);
        }
        Mat background = new Mat(img.size(), CvType.CV_8UC3, new Scalar(0, 0, 0));
        
      
        return rects;
    }
    /**
	 * ��б����
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
    		// ��ȡƽ��ˮƽ����
    		degree = degreeCount/rects.size();
    	}
    	Point center = new Point(srcImage.cols() / 2, srcImage.rows() / 2);
    	Mat rotm = Imgproc.getRotationMatrix2D(center, degree, 1.0);    //��ȡ����任����
    	Mat dst = new Mat();
    	Imgproc.warpAffine(srcImage, dst, rotm, srcImage.size(), Imgproc.INTER_LINEAR, 0, new Scalar(255, 255, 255));    // ����ͼ����ת����
    	return dst;
	}
}
