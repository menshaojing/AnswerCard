package answercard;


import org.junit.Test;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.opencv.imgproc.Imgproc.RETR_LIST;

/**
 * @Author : alexliu
 * @Description : opencv ����
 * @Date : Create at ����3:12 2018/1/26
 */
public class SmartRecognitionTable {

    String test_file_path = System.getProperty("user.dir") + File.separator + "img"+File.separator;
    List<Rect> rectList = new ArrayList<Rect>();
    static {
        //���ض�̬���ӿ�ʱ����ʹ��System.loadLibrary(xxx);�� ����ʹ�� ����·�����أ�System.load(xxx);

        /*
         * ���ض�̬��
         *
         * ��һ�ַ�ʽ --------------System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
         * loadLibrary(Core.NATIVE_LIBRARY_NAME); //ʹ�����ַ�ʽ���أ���Ҫ�� IDE �����ò���.
         * Eclipse ���ã�http://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html#set-up-opencv-for-java-in-eclipse
         * IDEA ���� ��http://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html#set-up-opencv-for-java-in-other-ides-experimental
         *
         * �ڶ��ַ�ʽ --------------System.load(path of lib);
         * System.load(your path of lib) ,��ʽ�Ƚ����ɸ��ݻ�����ϵͳ��λ����������������
         */
       // loadLibraries();
    	System.out.println("Welcome to OpenCV VH " + Core.VERSION);
    	//String root=System.getProperty("user.dir");
       // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.load("D:\\work\\AnswerCard\\AnswerCard\\libs\\x64\\opencv_java450.dll");
    }
     public static void main(String[] args)
     {
    	 SmartRecognitionTable  srtable=new SmartRecognitionTable();
         List<Rect> list = srtable.readTable();
         list=  getContainRect(new Rect((int)96,(int)744,(int)198,(int)166),list);
         List<Integer> xList=new ArrayList();
         List<Integer> yList=new ArrayList();
         list.forEach(rect -> {
             xList.add(rect.x);
             xList.add(rect.x+ rect.width);
             yList.add(rect.y);
             yList.add(rect.y+ rect.height);
         });
         //��ȡ���X,Y ����С x,Y
         int maxX = xList.stream().max(Integer::compareTo).get();
         int maxY = yList.stream().max(Integer::compareTo).get();
         int minX = xList.stream().min(Integer::compareTo).get();
         int minY = yList.stream().min(Integer::compareTo).get();
         Mat source_image = Imgcodecs.imread("D:\\image\\test\\1.jpg");
         Mat clone = source_image.clone();
         //��ȡ׼��֤�ŷ�Χ����
         Rect maxRect = new Rect( new Point(minX,minY),new Point(maxX,maxY));

         Imgproc.rectangle(source_image, maxRect.tl(), maxRect.br(), new Scalar(0, 255, 0), 1, 8, 0);
         Mat maxRectMat = source_image.submat(maxRect);
         Imgcodecs.imwrite("D:\\image\\card\\maxRect.jpg",source_image);
         Imgcodecs.imwrite("D:\\image\\card\\maxRectMat.jpg",maxRectMat);
         Mat maxMat = source_image.submat(maxRect);
         //����
         int row = 5;
         int col = 4;
         int xStep = maxMat.cols()/col;
         int yStep =maxMat.rows()/row;
         for (int i = 0; i < col; i++) {
             Imgproc.line(maxMat,new Point(xStep*i,0),new Point(xStep*i,source_image.rows()),new Scalar(0, 255, 0));
         }
         for (int i = 0; i < row; i++) {
             Imgproc.line(maxMat,new Point(0,i*yStep),new Point(source_image.cols(),i*yStep),new Scalar(0, 255, 0));

         }
         List<Rect> resultList=new ArrayList<>();
         for (int i = 0; i < col; i++) {
             for (int j = 0; j < row; j++) {
                 int x =  xStep*(i+1);
                 int y = yStep*(j+1);
                 Point p1 = new Point((x - xStep)+maxRect.x, (y - yStep)+maxRect.y);
                 //  Point p2 = new Point(x, y - yStep);
                 Point p3 = new Point(x+maxRect.x, y+maxRect.y);
                 // Point p4 = new Point(x - xStep, y);
                 resultList.add(new Rect(p1,p3));
                 Imgproc.rectangle(clone, new Rect(p1,p3).tl(), new Rect(p1,p3).br(), new Scalar(255, 0, 0), 1, 8, 0);
             }

         }
         Imgcodecs.imwrite("D:\\image\\card\\line2.jpg",clone);

         resultList=resultList.stream().sorted(Comparator.comparing(rect -> {
             return  rect.y;
         })).collect(Collectors.toList());
         System.out.println(resultList);
         Imgcodecs.imwrite("D:\\image\\card\\line.jpg",maxMat);

     }
    public Rect getMaxRect(String fileUrl){
        Mat imgIn = Imgcodecs.imread(fileUrl);
        Mat grayImg = new Mat();
        Mat maxMat=new Mat();
        Imgproc.resize(imgIn,maxMat,new Size(imgIn.width(),imgIn.height()));
        Imgproc.cvtColor(maxMat, grayImg, Imgproc.COLOR_RGB2GRAY);
        // C ������ȡ��ɫ��������ֵ��Ϊ��ɫ������Ϊ��ɫ
        Imgproc.adaptiveThreshold(grayImg, grayImg,255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,7,-2);

        //��̬ѧ�仯��ȷ��Ŀ������������һ���
        Mat element = Imgproc.getStructuringElement(RETR_LIST, new Size(3, 3));
        Imgproc.morphologyEx(grayImg,grayImg,Imgproc.MORPH_CLOSE,element);

        Mat cannyImg = new Mat();
        int ksize =3;
        double sigma = 0.3 * ((ksize - 1) * 0.5 - 1) + 0.8;
        Mat gaussianKernel = Imgproc.getGaussianKernel(ksize, sigma);
        Imgproc.GaussianBlur(grayImg, grayImg, gaussianKernel.size(), 0, 0);
        Imgproc.Canny(grayImg, cannyImg, 35, 35*3, 3);



        Imgcodecs.imwrite("D:\\image\\test1\\cannyImg.png",cannyImg);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
        Imgproc.dilate(cannyImg, cannyImg, kernel, new Point(-1, -1), 98, 1, new Scalar(1));
        Imgcodecs.imwrite("D:\\image\\test1\\dilate.png",cannyImg);
        Imgproc.erode(cannyImg, cannyImg, kernel, new Point(-1, -1), 10, 1, new Scalar(1));
        Imgcodecs.imwrite("D:\\image\\test1\\erode.png",cannyImg);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        // �ҳ�������Ӧ͹�����ı������
        List<MatOfPoint> squares = new ArrayList<>();
        List<MatOfPoint> hulls = new ArrayList<>();
        MatOfInt hull = new MatOfInt();
        MatOfPoint2f approx = new MatOfPoint2f();
        approx.convertTo(approx, CvType.CV_32F);
        for (MatOfPoint contour : contours) {
            // �߿��͹��
            Imgproc.convexHull(contour, hull);

            // ��͹��������µ�������
            Point[] contourPoints = contour.toArray();
            int[] indices = hull.toArray();
            List<Point> newPoints = new ArrayList<>();
            for (int index : indices) {
                newPoints.add(contourPoints[index]);
            }
            MatOfPoint2f contourHull = new MatOfPoint2f();
            contourHull.fromList(newPoints);

            // ��������͹���߿�(��ʱ����ϵľ��Ƚϵ�)
            Imgproc.approxPolyDP(contourHull, approx, Imgproc.arcLength(contourHull, true) * 0.1, true);

            // ɸѡ���������ĳһ��ֵ�ģ����ı��εĸ����Ƕȶ��ӽ�ֱ�ǵ�͹�ı���
            MatOfPoint approxf1 = new MatOfPoint();
            approx.convertTo(approxf1, CvType.CV_32S);
            if (approx.rows() == 4 && Imgproc.isContourConvex(approxf1)) {
                MatOfPoint tmp = new MatOfPoint();
                contourHull.convertTo(tmp, CvType.CV_32S);
                squares.add(approxf1);
                hulls.add(tmp);
            }
        }

        Imgproc.drawContours(maxMat, squares, -1, new Scalar(0, 0, 205), 2);
        Imgcodecs.imwrite("D:\\image\\test1\\imgIn.jpg",maxMat);
        MatOfPoint matOf = squares.stream().max(Comparator.comparing(matOfPoint -> {
            Rect rect = Imgproc.boundingRect(matOfPoint);
            return rect.area();
        })).get();
        return Imgproc.boundingRect(matOf);
    }
     //ȡ��������
     public static List<Rect>   getContainRect(Rect a,List<Rect> list){
         List<Rect> listRect=new ArrayList<>();
         for (Rect rect:list) {
             boolean flag = containRectangle(a, rect);
             if(flag){
                 listRect.add(rect);
             }
         }
         return listRect;
    }
    /**
     * @Description �������Ƿ��ཻ
     * @param a ����
     * @param b Ŀ�����
     * @throws
     * @return boolean
     * @Author menshaojing
     * @Date  2021/6/28  14:13
     **/
    public  boolean isRectIntersect(Rect a, Rect b)
    {
        int x01=a.x;
        int x02=a.x+a.width;
        int y01=a.y;
        int y02=a.y+a.height;
        int x11=b.x;
        int x12=b.x+b.width;
        int y11=b.y;
        int y12=b.y+b.height;
        int zx = Math.abs(x01 + x02 -x11 - x12);
        int x  = Math.abs(x01 - x02) + Math.abs(x11 - x12);
        int zy = Math.abs(y01 + y02 - y11 - y12);
        int y  = Math.abs(y01 - y02) + Math.abs(y11 - y12);
        if(zx <= x && zy <= y)
        { //ȥ���ཻ��
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * @Description �������Ƿ����
     * @param a  ����
     * @param b Ŀ�����
     * @throws
     * @return boolean
     * @Author menshaojing
     * @Date  2021/7/7  8:51
     **/
    public static boolean  containRectangle(Rect a, Rect b){
        Point pointA0=new Point(a.x,a.y);
        Point pointA1=new Point(a.x+a.width,a.y);
        Point pointA2=new Point(a.x+a.width,a.y+a.height);
        Point pointA3=new Point(a.x,a.y+a.height);
        Point pointB0 = new Point(b.x, b.y);
        Point pointB1 = new Point(b.x+b.width, b.y);
        Point pointB2 = new Point(b.x+b.width, b.y+b.height);
        Point pointB3 = new Point(b.x, b.y+b.height);
        if(isPointInMatrix(pointA0,pointA1,pointA2,pointA3,pointB0) &&
                isPointInMatrix(pointA0,pointA1,pointA2,pointA3,pointB1) &&
                isPointInMatrix(pointA0,pointA1,pointA2,pointA3,pointB2) &&
                isPointInMatrix(pointA0,pointA1,pointA2,pointA3,pointB3)
        ){
            return true;
        }
        return false;
    }
    /**
     * @Description �жϵ�p�Ƿ���p1p2p3p4����������
     * @Date  2021/6/2  11:25
     * @param p1 �����Ľǵ�һ����λ
     * @param p2 �����Ľǵڶ�����λ
     * @param p3 �����Ľǵ�������λ
     * @param p4 �����Ľǵ��ĸ���λ
     * @param p  �жϵ�λ
     * @throws
     * @return boolean
     * @Author menshaojing
     * @Date  2021/6/2  11:25
     **/
    static boolean  isPointInMatrix( Point p1,  Point p2,  Point p3, Point p4,  Point p) {
        boolean isPointIn = getCross(p1, p2, p) * getCross(p3, p4, p) >= 0 && getCross(p2, p3, p) * getCross(p4, p1, p) >= 0;
        return isPointIn;
    }
    /**
     * @Description  ���� |p1 p2| X |p1 p|
     * @Date  2021/6/2  11:25
     * @param p1
     * @param p2
     * @param p
     * @throws
     * @return double
     * @Author menshaojing
     * @Date  2021/6/2  11:25
     **/
    public static double getCross( Point p1, Point p2,  Point p) {
        return (p2.x - p1.x) * (p.y - p1.y) - (p.x - p1.x) * (p2.y - p1.y);
    }

    @Test
    public void test(){
        readTable();
    }
    /**
     * ��ȡ table
     */
    public  List<Rect>  readTable(){

        Mat source_image = Imgcodecs.imread("D:\\work\\AnswerCard\\img\\0802\\dst.png");
        //�Ҷȴ���
        Mat gray_image = new Mat();
        Imgproc.cvtColor(source_image,gray_image,Imgproc.COLOR_RGB2GRAY);

        //��ֵ��
        Mat thresh_image = new Mat(source_image.height(), source_image.width(), CvType.CV_8UC1);
        // C ������ȡ��ɫ��������ֵ��Ϊ��ɫ������Ϊ��ɫ
        Imgproc.adaptiveThreshold(gray_image, thresh_image,255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,3,-1);
       // this.saveImage("out-table/1-thresh.png",thresh_image);

        //��¡һ�� Mat��������ȡˮƽ��
        Mat horizontal_image = thresh_image.clone();

        //��¡һ�� Mat��������ȡ��ֱ��
        Mat vertical_image = thresh_image.clone();

        /*
         * ��ˮƽ��
         * 1. ����ҳ����������������Ϊ��ȣ�����ҳ�滯�����ɵ�ɨ������
         * 2. ����ɨ������Ŀ�ȣ�����һ��ˮƽ��
         * 3. ͨ����ʴ�����ͣ�������������������ˮƽ�߹�������
         *
         * scale Խ��ʶ�����Խ�࣬��Ϊ��Խ��ҳ�滮��������ԽС���ڸ�ʴ�󣬶������ֻ��γ�һ���飬��ô�ͻ���һ����
         * ��ʶ����ʱ�����ǿ���������Ǵ�ҳ����� �� ҳ���ұߵģ���ô����������ԽС�����������Խ�٣�����Ҳ��׼ȷ
         */
        int scale = horizontal_image.cols();
        int horizontalsize = horizontal_image.cols() / scale;
        // Ϊ�˻�ȡ����ı���ߣ����ø�ʴ�����͵Ĳ�������Ϊһ���Ƚϴ�ĺ���ֱ��
        Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontalsize, 1));
        // �ȸ�ʴ������ new Point(-1, -1) ������ԭ�㿪ʼ
        // iterations ���һ������������������Խ�࣬��Խ�ࡣ��ҳ�������������1�μ��ɡ�
        Imgproc.erode(horizontal_image, horizontal_image, horizontalStructure, new Point(-1, -1),1);
        Imgproc.dilate(horizontal_image, horizontal_image, horizontalStructure, new Point(-1, -1),1);
        //this.saveImage("out-table/2-horizontal.png",horizontal_image);

        // ��ֱ��
        scale = vertical_image.rows();
        int verticalsize = vertical_image.rows() / scale;
        Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, verticalsize));
        Imgproc.erode(vertical_image, vertical_image, verticalStructure, new Point(-1, -1),1);
        Imgproc.dilate(vertical_image, vertical_image, verticalStructure, new Point(-1, -1),1);
       // this.saveImage("out-table/3-vertical.png",vertical_image);

        /*
         * �ϲ�����
         * ����ֱ�ߣ�ˮƽ�ߺϲ�Ϊһ��ͼ
         */
        Mat mask_image = new Mat();
        Core.add(horizontal_image,vertical_image,mask_image);
       // this.saveImage("out-table/4-mask.png",mask_image);

        /*
         * ͨ�� bitwise_and ��λ���ߡ���ֱ�߽���ĵ�
         */
        Mat points_image = new Mat();
        Core.bitwise_and(horizontal_image, vertical_image, points_image);
        //this.saveImage("out-table/5-points.png",points_image);

        /*
         * ͨ�� findContours ������
         *
         * ��һ��������������ͼ��ͼ��ĸ�ʽ��8λ��ͨ����ͼ�񣬲��ұ�����Ϊ��ֵͼ�񣨼�ͼ�е����з�������֮�䶼����ȵģ���
         * �ڶ�����������һ�� MatOfPoint ���飬�ڶ���ʵ�ʵĲ����м���STL vectors��STL vector�����ｫʹ���ҵ����������б������䣨�����⽫��һ��contours��vector,����contours[i]��ʾһ���ض���������������contours[i][j]����ʾcontour[i]��һ���ض��Ķ˵㣩��
         * ������������hierarchy�������������ָ����Ҳ���Բ�ָ�������ָ���Ļ������hierarchy��������������������Ľṹ��Ϣ��0��Ԫ�ر�ʾ��һ��������ͬһ�㼶����1��Ԫ�ر�ʾǰһ��������ͬһ�㼶����2��Ԫ�ر�ʾ��һ������������һ�㼶����3��Ԫ�ر�ʾ����������һ�㼶��
         * ���ĸ�������������ģʽ���������OpenCV�����ú��ַ�ʽ��������������ȡ�����ĸ���ѡ��ֵ��
         *      RETR_EXTERNAL ��0������ʾֻ��ȡ�������������
         *      RETR_LIST ��1������ʾ��ȡ������������������б�
         *      RETR_CCOMP ��2��:��ʾ��ȡ��������������֯��һ������ṹ�����ж����������ⲿ�������ڶ��������ǡ�������������
         *      RETR_TREE ��3������ʾ��ȡ������������֯������Ƕ�׵������㼶�ṹ��
         * �������������ʶ��������������γ��ֵķ����������ֿ�ѡ�ķ�����
         *      CHAIN_APPROX_NONE ��1�����������е����е�ı���ת���ɵ㣻
         *      CHAIN_APPROX_SIMPLE ��2����ѹ��ˮƽ����ֱ�ͶԽ�ֱ�߶Σ����������ǵĶ˵㣻
         *      CHAIN_APPROX_TC89_L1  ��3��or CV_CHAIN_APPROX_TC89_KCOS��4����Ӧ��Teh-Chin�������㷨�е�һ�ַ��
         * ������������ƫ�ƣ���ѡ������Ƕ�����ô���ص������е����е����ָ������ƫ��
         */

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask_image,contours,hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_L1,new Point(0,0));


        List<MatOfPoint> contours_poly = contours;
        Rect[] boundRect = new Rect[contours.size()];

        LinkedList<Mat> tables = new LinkedList<Mat>();
        //STARTȥ�أ���Ҫ��1)�������������У����������500  
        // 2)�������ڲ��С����
        //û��Ч��������������ӽ�����л�ȡ

        List<MatOfPoint> myContours = new ArrayList<MatOfPoint>();
        List<MatOfPoint> smallContours = new ArrayList<MatOfPoint>();

        List<MatOfPoint> repContours = new ArrayList<MatOfPoint>();;
        List<RectComp> RectCompList = new ArrayList<>();
        List<RectComp> smallCompList = new ArrayList<>();
        List<MatOfPoint> repSmallContours = new ArrayList<MatOfPoint>();;
        for (int i = 0; i < contours.size(); i++) {
            Rect rm = Imgproc.boundingRect(contours.get(i));
            RectComp ti = new RectComp(rm);
            //��������������� 50 - 80 ��Χ�ڵ�����װ�����μ���
            if (ti.rm.width > 20 ) {
                RectCompList.add(ti);
                myContours.add(contours.get(i));
            }else {
            	smallContours.add(contours.get(i));
            	smallCompList.add(ti);
            }
        }

        List<RectComp> aList=RectCompList;
        for(int i=0;i<RectCompList.size();i++)
        {
        	RectComp rc=RectCompList.get(i);
        	
        	for(int j=i+1;j<aList.size();j++)
        	{
        		RectComp point = aList.get(j);
        		if(rc.rm.x<point.rm.x&&rc.rm.y<point.rm.y)
        		{
        			if(rc.rm.contains(new Point(point.rm.x,point.rm.y)))
        			if(rc.rm.width>=point.rm.width&&rc.rm.height>=point.rm.height)
        			{
        				repContours.add(myContours.get(j));
        			}
        		}
        	}
        }
        for(int i=0;i<repContours.size();i++)
        {
        	contours.remove(repContours.get(i));
        }
        //С���ȥ��
        aList=smallCompList;
        for(int i=0;i<smallCompList.size();i++)
        {
        	RectComp rc=smallCompList.get(i);
        	
        	for(int j=i+1;j<aList.size();j++)
        	{
        		RectComp point = aList.get(j);
        		if(rc.rm.x<point.rm.x&&rc.rm.y<point.rm.y)
        		{
        			if(rc.rm.contains(new Point(point.rm.x,point.rm.y)))
        			if(rc.rm.width>=point.rm.width&&rc.rm.height>=point.rm.height)
        			{
        				repSmallContours.add(smallContours.get(j));
        			}
        		}
        	}
        }
        for(int i=0;i<repSmallContours.size();i++)
        {
        	contours.remove(repSmallContours.get(i));
        }
        //
        //ENDȥ��
       //ѭ�������ҵ�������-��
        for(int i=0 ; i< contours.size(); i++){

            MatOfPoint point = contours.get(i);
            MatOfPoint contours_poly_point = contours_poly.get(i);

            /*
             * ��ȡ��������
             * ��һ��������InputArray contour������ĵ㣬һ����ͼ���������
             * �ڶ���������bool oriented = false:��ʾĳһ�������������ĵ����ֵ��˳ʱ�������ʱ�룬һ��ѡ��Ĭ��false
             */
            double area = Imgproc.contourArea(contours.get(i));
            //���С��ĳ��ֵ�ͺ��ԣ����������߲��Ǳ��
            //�ر�ע���С��18 ���߲�С��18 �����С��324��Ϊ�˼������ڴ�����Ϊ324
            //ԭ���������Ϊ�ĺ� �ĺ�������Ϊ18px
            //��λ:  ����
            //Ŀ�ģ�ȥ������ĸ��ѡ���
            if(area < 325){
                continue;
            }

            /*
             * approxPolyDP ���������ƽ������Ϊһ����״��trueֵ��ʾ����������Ϊ�պ����򡣱���һ��������ȵ����ߣ��������
             *
             * MatOfPoint2f curve�����ص���������ݡ�
             * MatOfPoint2f approxCurve��������ص�ת�����������ݡ�
             * double epsilon���жϵ㵽���Ӧ��line segment �ľ������ֵ����������ڴ���ֵ��������С�ڴ���ֵ������epsilonԽС�����ߵ���״Խ���ӽ������ߡ���
             * bool closed�������Ƿ�պϵı�־λ��
             */
            Imgproc.approxPolyDP(new MatOfPoint2f(point.toArray()),new MatOfPoint2f(contours_poly_point.toArray()),3,true);

            //Ϊ����Ƭ����ת��Ϊ���Σ��˾��ΰ����������״
            boundRect[i] = Imgproc.boundingRect(contours_poly.get(i));

            // �ҵ����㴦�ĵı��������
            Mat table_image = points_image.submat(boundRect[i]);

            List<MatOfPoint> table_contours = new ArrayList<MatOfPoint>();
            Mat joint_mat = new Mat();
            Imgproc.findContours(table_image, table_contours,joint_mat, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
            //�ӱ������Կ��������Ƭ����ĵ���С��4���Ǿʹ���û��һ�������ı�񣬺��Ե�
           /* if (table_contours.size() < 4)
                continue;*/

            RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            //����ߺͿ�
            int m_width = rect.boundingRect().width;
            int m_height = rect.boundingRect().height;
            if(m_width>300||m_height>300)
            {
            	 continue;
            }
            //����ͼƬ
            tables.addFirst(source_image.submat(boundRect[i]).clone());

            //�����λ���ԭͼ��
            Imgproc.rectangle(source_image, boundRect[i].tl(), boundRect[i].br(), new Scalar(0, 255, 0), 1, 8, 0);
            rectList.add(boundRect[i]);
        }


        Collections.sort(rectList,new Comparator<Rect>() {
			@Override
			public int compare(Rect a, Rect b) {
				// TODO Auto-generated method stub
			    if(a.x<b.x) return -1;
			    if(a.x==b.x) return 0;
			    if(a.x>b.x)return 1;
				return 0;
			}
        });

        for(int i=0;i<rectList.size();i++)
        {
        	if(rectList.get(i).y>460) {
        	System.out.println(rectList.get(i));
        	}
        }
        this.saveImage("out-table/7-source.png",source_image);
        return rectList;
    }

    private void saveImage(String path,Mat image){

        String outPath = this.test_file_path + File.separator + path;

        File file = new File(outPath);
        //Ŀ¼�Ƿ����
        this.dirIsExist(file.getParent());

        Imgcodecs.imwrite(outPath,image);

    }

    private void dirIsExist(String dirPath){
        File dir = new File(dirPath);
        if(!dir.exists()){
            dir.mkdirs();
        }
    }
    /**
     * @param area--�͹����������С[�Ǳ�Ҫ����չ��]
     * @param NoVH--��ź�������[�Ǳ�Ҫ����չ��]
     * @param ItemVH--ѡ���������[�Ǳ�Ҫ����չ��]
     * @param hasNo--���[�Ǳ�Ҫ����չ��]
     * @param rows--����[�Ǳ�Ҫ����չ��]
     * @param cols--����[�Ǳ�Ҫ����չ��]
     * @param areas--�͹��������б� * ������д�
     * TODO:�ر�ע��Areas �������겻�ܰ�����һ��/��һ�е�����У����ۺ�����������У�
     */
    public List<Rect>  FormatAreas(int noVH,int itemVH,int rows,int cols,List<Rect> areas)
    {
    	//1) ��ȡx��С�����ֵ
    	//2����ȡY����С�����ֵ
    	//3���������д𰸴�ֱ�����ϼ�����Сֵ
    	//4���������з����Զ�����ÿһ����źʹ���������
    	int minX=0,minY=0,maxX=0,maxY=0;
    	int gapV=0,gapH=0;//ˮƽ��Ⱥʹ�ֱ�߶ȵ���Сֵ
    	int width=0;//һ����Ŀ�ȼ���
    	if(areas.size()>1) {
    		minX=areas.get(0).x;
    		minY=areas.get(0).y;
    		maxX=minX;
    		maxY=minY;
    		gapV=areas.get(0).height;
    		gapH=areas.get(0).width;
	    	for(int i=1;i<areas.size();i++)
	    	{
	    		Rect rect=areas.get(i);
	    		if(rect.x<minX)
	    			minX=rect.x;
	    		if(rect.x>maxX)
	    			maxX=rect.x;
	    		if(rect.y<minY)
	    			minY=rect.x;
	    		if(rect.y>maxY)
	    			maxY=rect.y;
	    		if(rect.width<gapH)
	    			gapH=rect.width;
	    	} 
	    	//������--��֤��������Ҫ��
	    	List<Rect> delWList=new ArrayList<Rect>();
	    	List<Rect> addWList=new ArrayList<Rect>();
	    	for(int i=0;i<areas.size();i++)
	    	{
	    		Rect rect=areas.get(i);
	    		int num=rect.width%gapH;
	    		if(num>1)
	    		{
	    			delWList.add(rect);
	    			int gap=rect.width/num;
	    			for(int j=0;j<num;j++)
	    			{
	    				Rect rec=new Rect();
	    				rec.x=rect.x+j*gap;
	    				rec.y=rect.y;
	    				rec.width=gap;
	    				rec.height=rect.height;
	    				addWList.add(rec);
	    			}
	    		}
	    	}
	    	for(int i=0;i<delWList.size();i++)
	    		areas.remove(delWList.get(i));
	    	for(int i=0;i<addWList.size();i++)
	    		areas.add(addWList.get(i));
	    	//�����ֽ���
	    	//������--��֤��������Ҫ��
	    	for(int i=1;i<areas.size();i++)
	    	{
	    		Rect rect=areas.get(i);
	    		if(rect.y>maxY) {
	    			i=areas.size();
	    		}else {
	    			if(rect.height<gapV)
	    				gapV=rect.height;
	    		}
	    	}
	    	
	    	List<Rect> delList=new ArrayList<Rect>();
	    	List<Rect> addList=new ArrayList<Rect>();
	    	for(int i=0;i<areas.size();i++)
	    	{
	    		Rect rect=areas.get(i);
	    		int num=rect.height%gapV;
	    		if(num>1)
	    		{
	    			delList.add(rect);
	    			int gap=rect.height/num;
	    			for(int j=0;j<num;j++)
	    			{
	    				Rect rec=new Rect();
	    				rec.x=rect.x;
	    				rec.y=(rect.y+j*gap-1);
	    				rec.height=gap;
	    				rect.width=rec.width;
	    				addList.add(rec);
	    			}
	    		}
	    	}
	    	for(int i=0;i<delList.size();i++)
	    		areas.remove(delList.get(i));
	    	for(int i=0;i<addList.size();i++)
	    		areas.add(addList.get(i));
	    	//�����ֽ���������X����
    	
	    	Collections.sort(areas,new Comparator<Rect>() {
	  			@Override
	  			public int compare(Rect a, Rect b) {
	  				// TODO Auto-generated method stub
	  			    if(a.x<b.x) return -1;
	  			    if(a.x==b.x) return 0;
	  			    if(a.x>b.x)return 1;
	  				return 0;
	  			}
	          });
    	}
    	return areas;
    }
    //����ÿ���𰸶�Ӧ�����ĵ�����
    public List<Point> getAreaCenter(List<Rect> areas)
    {
    	List<Point> centList=new ArrayList<Point>();
    	for(int i=0;i<areas.size();i++) {
    		Rect rect=areas.get(i);
    		centList.add(new Point(rect.x+rect.width/2,rect.y+rect.height));
    	}
    	return centList;
    }
    /**
     * ���ض�̬��
     */
    private static void loadLibraries() {

        try {
            String osName = System.getProperty("os.name");
            String opencvpath = System.getProperty("user.dir");

            //windows
            if(osName.startsWith("Windows")) {
                int bitness = Integer.parseInt(System.getProperty("sun.arch.data.model"));
                //32λϵͳ
                if(bitness == 32) {
                    opencvpath=opencvpath+"\\opencv\\x86\\Your path to .dll";
                }
                //64λϵͳ
                else if (bitness == 64) {
                    opencvpath=opencvpath+"\\opencv\\x64\\Your path to .dll";
                } else {
                    opencvpath=opencvpath+"\\opencv\\x86\\Your path to .dll";
                }
            }
            // mac os
            else if(osName.equals("Mac OS X")){
                opencvpath = "/usr/local/Cellar/opencv/3.4.0_1/share/OpenCV/java/libopencv_java340.dylib";
            }
            System.out.println(opencvpath);
            System.load(opencvpath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load opencv native library", e);
        }
    }
}
