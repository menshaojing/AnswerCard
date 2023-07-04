package answercard;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.opencv.imgproc.Imgproc.MORPH_RECT;

/**
 * @ClassName Test17
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/5/25 13:55
 * @Version 1.0
 */
public class Test17 {


    @Before
    public void init() {
        System.load("D:/work/AnswerCard/AnswerCard/libs/x64/opencv_java450.dll");
        // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Test
    public void  test3(){
        String imgPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0524\\A40524_150-1-110.jpeg";
        Mat src = Imgcodecs.imread(imgPath);

    }
    @Test
    public void   test41() throws ParseException {
        String imgPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0524\\0526003.jpg";
        String destPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0524\\A40524_150-1-110-01.jpeg";
        Mat src = Imgcodecs.imread(imgPath);

        List<MatOfPoint> list = answerCard(src);
        List<Point> centers=new ArrayList<>();
        //获取所有中心点位信息
        for (MatOfPoint matOfPoint: list) {
            MatOfPoint2f tmp = new MatOfPoint2f();
            matOfPoint.convertTo(tmp, CvType.CV_32F);
            //获取中心点位
            RotatedRect rect = Imgproc.minAreaRect(tmp);
            // 获取矩形的四个顶点
            Point[] rectPoint = new Point[4];
            rect.points(rectPoint);
            centers.add(rect.center);
        }
        Mat grayImage = new Mat();
        Imgproc.cvtColor(src, grayImage, Imgproc.COLOR_RGB2GRAY);
        //大津法找到敏感区域
        Imgproc.threshold(grayImage, grayImage, 100, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU);
        //形态学变化，确保目标区别都是连在一起的
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
        Imgproc.morphologyEx(grayImage,grayImage,Imgproc.MORPH_OPEN,kernel);
        Imgproc.dilate(grayImage, grayImage, kernel, new Point(-1, -1), 2, 1, new Scalar(1));
        Imgproc.erode(grayImage, grayImage, kernel, new Point(-1, -1), 2, 1, new Scalar(1));
        int x_step = grayImage.cols()/4;
        int y_step =grayImage.rows()/5;
        for (int i = 0; i < 4; i++) {
            Imgproc.line(grayImage,new Point(x_step*i,0),new Point(x_step*i,grayImage.rows()),new Scalar(255));
        }
        for (int i = 0; i < 5; i++) {
            Imgproc.line(grayImage,new Point(0,i*y_step),new Point(grayImage.cols(),i*y_step),new Scalar(255));

        }
        //结果
        Map<Integer, String> resultMap=new HashMap<>();
        //初始化题目答案
        resultMap.put(1,null);
        resultMap.put(2,null);
        resultMap.put(3,null);
        resultMap.put(4,null);
        resultMap.put(5,null);
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 5; j++) {
                int x =  x_step*(i+1);
                int y = y_step*(j+1);
                Point p1 = new Point(x - x_step, y - y_step);
                Point p2 = new Point(x, y - y_step);
                Point p3 = new Point(x, y);
                Point p4 = new Point(x - x_step, y);
                System.out.println(i+"行"+j+"列："+"("+p1.x+","+p1.y+"),"+"("+p2.x+","+p2.y+"),"+"("+p3.x+","+p3.y+"),"+"("+p4.x+","+p4.y+"),");
                for (Point centerPoint:centers ) {

                    if(IsPointInMatrix(  p1,   p2,   p3,  p4,  centerPoint)){
                        //答案
                        String resultValue = null;
                        switch(x/x_step){
                            case 1 :
                                resultValue="A";
                                break;
                            case 2 :
                                resultValue="B";
                                break;
                            case 3 :
                                resultValue="C";
                                break;
                            case 4 :
                                resultValue="D";
                                break;
                        }
                        resultMap.put(j+1,resultValue);
                        centers.remove(centerPoint);
                        break;
                    }
                }
            }

        }
        resultMap.forEach((key,value) ->{
            System.out.println("resultMap:"+key+"-"+value);
        });

    }
    // 计算 |p1 p2| X |p1 p|
    public double GetCross( Point p1, Point p2,  Point p) {
        return (p2.x - p1.x) * (p.y - p1.y) - (p.x - p1.x) * (p2.y - p1.y);
    }
    //判断点p是否在p1p2p3p4的正方形内
    boolean IsPointInMatrix( Point p1,  Point p2,  Point p3, Point p4,  Point p) {
        boolean isPointIn = GetCross(p1, p2, p) * GetCross(p3, p4, p) >= 0 && GetCross(p2, p3, p) * GetCross(p4, p1, p) >= 0;
        return isPointIn;
    }

    public List<MatOfPoint> answerCard(Mat imgIn) {
         Mat grayImg = new Mat();
        Imgproc.cvtColor(imgIn, grayImg, Imgproc.COLOR_RGB2GRAY);
        //大津法找到敏感区域
        double threshold1 = Imgproc.threshold(grayImg, grayImg, 100, 255, Imgproc.THRESH_OTSU);
        System.out.println(threshold1);
        //为什么要转换，因为白色是有数据的区域,轮廓是围绕白色区域的
        double threshold2 = Imgproc.threshold(grayImg, grayImg, 100, 255, Imgproc.THRESH_BINARY_INV);
        System.out.println(threshold2);
        //形态学变化，确保目标区别都是连在一起的
        Mat element = Imgproc.getStructuringElement(MORPH_RECT, new Size(3, 3));
        Imgproc.morphologyEx(grayImg,grayImg,Imgproc.MORPH_OPEN,element);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0524\\grayImg.jpg", grayImg);
        int ksize =3;
        double sigma = 0.3 * ((ksize - 1) * 0.5 - 1) + 0.8;
        Mat gaussianKernel = Imgproc.getGaussianKernel(ksize, sigma);
        Imgproc.GaussianBlur(grayImg, grayImg, gaussianKernel.size(), 0, 0);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0524\\GaussianBlur.jpg", grayImg);
        //解析
        return answerCardParse(grayImg,threshold1,threshold2,imgIn);
    }

    /**
     * @Description 答题卡解析
     * @Date  2021/5/18  18:03
     * @param imgIn
     * @throws
     * @return int
     * @Author menshaojing
     * @Date  2021/5/18  18:03
     **/
    List<MatOfPoint> answerCardParse(Mat imgIn, double threshold1,double threshold2,Mat src) {
        Mat cannyImg = new Mat();
        Imgproc.Canny(imgIn, cannyImg, threshold1, threshold2, 3);
        //矩形：MORPH_RECT; 交叉形：MORPH_CROSS;椭圆形：MORPH_ELLIPSE;
        Mat kernel =Imgproc.getStructuringElement(MORPH_RECT, new Size(3, 3));
        Imgproc.dilate(cannyImg, cannyImg, kernel, new Point(-1, -1), 1, 1, new Scalar(1));
        Imgproc.erode(cannyImg, cannyImg, kernel, new Point(-1, -1), 1, 1, new Scalar(1));
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        // 找出轮廓对应凸包的四边形拟合
        List<MatOfPoint> squares = new ArrayList<>();
        List<MatOfPoint> hulls = new ArrayList<>();
        MatOfInt hull = new MatOfInt();
        MatOfPoint2f approx = new MatOfPoint2f();
        approx.convertTo(approx, CvType.CV_32F);
        for (MatOfPoint contour : contours) {
            // 边框的凸包
            Imgproc.convexHull(contour, hull);

            // 用凸包计算出新的轮廓点
            Point[] contourPoints = contour.toArray();
            int[] indices = hull.toArray();
            List<Point> newPoints = new ArrayList<>();
            for (int index : indices) {
                newPoints.add(contourPoints[index]);
            }
            MatOfPoint2f contourHull = new MatOfPoint2f();
            contourHull.fromList(newPoints);

            // 多边形拟合凸包边框(此时的拟合的精度较低)
            Imgproc.approxPolyDP(contourHull, approx, Imgproc.arcLength(contourHull, true) * 0.1, true);

            // 筛选出面积大于某一阈值的，且四边形的各个角度都接近直角的凸四边形
            MatOfPoint approxf1 = new MatOfPoint();
            approx.convertTo(approxf1, CvType.CV_32S);
            if (approx.rows() == 4 && Math.abs(Imgproc.contourArea(approx)) >200 && Math.abs(Imgproc.contourArea(approx)) <800&&
                    Imgproc.isContourConvex(approxf1)) {
                double maxCosine = 0;
                for (int j = 2; j < 5; j++) {
                    double cosine = Math.abs(getAngle(approxf1.toArray()[j % 4], approxf1.toArray()[j - 2], approxf1.toArray()[j - 1]));
                    maxCosine = Math.max(maxCosine, cosine);
                }
                // 角度大概72度
                if (maxCosine <= 0.5) {
                    MatOfPoint tmp = new MatOfPoint();
                    contourHull.convertTo(tmp, CvType.CV_32S);
                    squares.add(approxf1);
                    hulls.add(tmp);
                }
            }
        }
        Scalar color=new Scalar(0, 255, 0);
        Imgproc.drawContours(src, squares, -1, color, 2);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0524\\drawContours.jpg", src);
        return  squares;
    }

    // 根据三个点计算中间那个点的夹角   pt1 pt0 pt2
    private static double getAngle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);

    }
}
