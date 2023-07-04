package answercard;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.opencv.core.Core.*;
import static org.opencv.core.CvType.*;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;

/**
 * @ClassName Test18
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/6/1 16:17
 * @Version 1.0
 */
public class Test18 {
    private static final int GRAY_THRESH =150 ;
    // 直线上点的个数
    private static final int HOUGH_VOTE  =50 ;

    @Before
    public void init() {
        System.load("D:/work/AnswerCard/AnswerCard/libs/x64/opencv_java450.dll");
        // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    @Test
    public void test(){
        String imgPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0601\\image_contrasted.jpg";
        Mat src = Imgcodecs.imread(imgPath);
        Mat images = src.clone();
        Mat grad=new Mat();
        Imgproc.cvtColor(src,grad,Imgproc.COLOR_RGB2GRAY);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0601\\grad.jpg",grad);
        double threshold = Imgproc.threshold(grad, grad, 0, 255, Imgproc.THRESH_BINARY_INV + Imgproc.THRESH_OTSU);
        Imgproc.GaussianBlur(grad, grad, new Size(3,3), 2, 2);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0601\\GaussianBlur.jpg",grad);


        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0601\\threshold.jpg",grad);

        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
        Imgproc.morphologyEx(grad,grad,Imgproc.MORPH_OPEN,kernel);
        Imgproc.Canny(grad,grad,20,60,3,false);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0601\\Canny.jpg",grad);
        Imgproc.dilate(grad, grad, kernel, new Point(-1, -1), 400, 1, new Scalar(1));
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0601\\dilate.jpg",grad);

        Imgproc.erode(grad, grad, kernel, new Point(-1, -1), 400, 1, new Scalar(1));
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0601\\erode.jpg",grad);
        List<MatOfPoint> contours = new ArrayList<>();


        Mat hierarchy = new Mat();
        Imgproc.findContours(grad, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0601\\findContours.jpg",grad);
        System.out.println(contours.size());
        Scalar color=new Scalar(0, 255, 0);
        Imgproc.drawContours(src, contours, -1, color, 2);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0601\\drawContours.jpg", src);

        MatOfPoint2f tmp = new MatOfPoint2f();
        contours.get(0).convertTo(tmp, CvType.CV_32F);
        //获取中心点位
        RotatedRect rect = Imgproc.minAreaRect(tmp);
        // 获取矩形的四个顶点
        Point[] rectPoint = new Point[4];
        rect.points(rectPoint);
        List<Point> centers = Arrays.asList(rectPoint);
        List<Double> listDiff = diff(Arrays.asList(rectPoint));
        List<Double> listSum = sum(Arrays.asList(rectPoint));
        int sumMinIndex = listSum.indexOf(listSum.stream().min(Double::compareTo).get());
        int sumMaxIndex = listSum.indexOf(listSum.stream().max(Double::compareTo).get());
        int diffMaxIndex = listDiff.indexOf(listDiff.stream().max(Double::compareTo).get());
        int diffMinIndex = listDiff.indexOf(listDiff.stream().min(Double::compareTo).get());
        //按顺序找到对应的坐标0123 分别是左上，右上，右下，左下
        Point point0 = centers.get(sumMinIndex);
        Point point1 = centers.get(diffMinIndex);
        Point point2 = centers.get(sumMaxIndex);
        Point point3 = centers.get(diffMaxIndex);
      // 计算目标图像的尺寸
        Point p0 = point3;
        Point p1 = point0;
        Point p2 = point1;
        Point p3 = point2;
        double space0 = getSpacePointToPoint(p0, p1);
        double space1 = getSpacePointToPoint(p1, p2);
        double space2 = getSpacePointToPoint(p2, p3);
        double space3 = getSpacePointToPoint(p3, p0);
        //Add the perspective correction
        double paraFix1 = (space3 / space1) > 1 ? (space3 / space1) : (space1 / space3);
        double paraFix2 = (space2 / space0) > 1 ? (space2 / space0) : (space0 / space2);
        double imgWidth = space1 > space3 ? space1 : space3;
        double imgHeight = space0 > space2 ? space0 : space2;
        if(paraFix1>paraFix2){
            imgHeight=imgHeight * paraFix1;
        }else{
            imgWidth = imgWidth * paraFix2;
        }
        MatOfPoint2f cornerMat = new MatOfPoint2f(p0, p1, p2, p3);

        Mat quad = Mat.zeros((int) imgHeight , (int) imgWidth , CV_8UC3);

        MatOfPoint2f quadMat = new MatOfPoint2f(
                new Point(0, quad.rows()),
                new Point(0, 0),
                new Point(quad.cols(), 0),
                new Point(quad.cols(), quad.rows()));

// 提取图像
        Mat transmtx = Imgproc.getPerspectiveTransform(cornerMat, quadMat);
        Imgproc.warpPerspective(images, quad, transmtx, quad.size());
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0601\\quad.jpg", quad);

    }
    @Test
    public void test1(){
        int scale =1;
        int delta =5;
        int ddepth = CV_16S;
        String imgPath = "D:\\image\\test\\1.jpg";
        Mat src = Imgcodecs.imread(imgPath);
        Mat cloneMat = src.clone();
       // Imgproc.filter2D(src, dst, -1, kernel, new Point(-1, -1), 0, Core.BORDER_DEFAULT);
        Imgproc.GaussianBlur(src, src, new Size(51,51), 0, 0);
       Mat gray=src;
       // Imgproc.cvtColor(src,gray,COLOR_BGR2GRAY);
        Mat grad_x = new Mat(), grad_y = new Mat();
        Mat abs_grad_x = new Mat(), abs_grad_y = new Mat();
        Imgproc.Sobel(gray,grad_x, ddepth, 1, 0, 3, scale, delta, BORDER_REPLICATE);
        Imgproc.Sobel(gray,grad_y, ddepth, 0, 1, 3, scale, delta, BORDER_REPLICATE);
        Core.convertScaleAbs(grad_x,abs_grad_x);
        Core.convertScaleAbs(grad_y,abs_grad_y);
        Mat weighted = new Mat();
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));

        Core.addWeighted(abs_grad_x,0.5,abs_grad_y,0.5,0,weighted);
        Imgproc.dilate(weighted, weighted, kernel, new Point(-1, -1), 1, BORDER_REPLICATE, new Scalar(1));
        Imgproc.erode(weighted, weighted, kernel, new Point(-1, -1), 1, 1, new Scalar(1));
         Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0603\\1000530002A_3.jpg",weighted);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(weighted, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
         // Imgproc.drawContours(cloneMat, contours, -1, new Scalar(0, 0, 205), 2, 10, hierarchy);
       // Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0603\\1000530002A_2.jpg",weighted);
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
            Imgproc.approxPolyDP(contourHull, approx, Imgproc.arcLength(contourHull, true) * 0.02, true);

            // 筛选出面积大于某一阈值的，且四边形的各个角度都接近直角的凸四边形
            MatOfPoint approxf1 = new MatOfPoint();
            approx.convertTo(approxf1, CvType.CV_32S);
            if (approx.rows() == 4 && Math.abs(Imgproc.contourArea(approx)) > 40000 &&
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
        Imgproc.drawContours(cloneMat, squares, -1, new Scalar(0, 0, 205), 2);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0603\\1000530002A_1.jpg",weighted);

        System.out.println(squares.size());
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0603\\1000530002A_2.jpg",cloneMat);
    }
    // 找到最大的正方形轮廓
    private static int findLargestSquare(List<MatOfPoint> squares) {
        if (squares.size() == 0)
            return -1;
        int max_width = 0;
        int max_height = 0;
        int max_square_idx = 0;
        int currentIndex = 0;
        for (MatOfPoint square : squares) {
            Rect rectangle = Imgproc.boundingRect(square);
            if (rectangle.width >= max_width && rectangle.height >= max_height) {
                max_width = rectangle.width;
                max_height = rectangle.height;
                max_square_idx = currentIndex;
            }
            currentIndex++;
        }
        return max_square_idx;
    }
    // 根据三个点计算中间那个点的夹角   pt1 pt0 pt2
    private static double getAngle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);

    }
    @Test
    public void test2(){
        int scale = 1;
        int delta = 0;
        int ddepth = CV_16S;
        String imgPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0603\\1000530002A.jpg";
        Mat src = Imgcodecs.imread(imgPath);
        Mat des=new Mat();
        switch (src.channels()){
            case 1:
                Core.normalize(src,des,3,255,NORM_MINMAX,CV_8UC1);
                break;
            case 3 :
                Core.normalize(src,des,3,255,NORM_MINMAX,CV_8UC3);
                break;
            default:
             //   des=src;
                break;
        }

        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0603\\1000530002A_1.jpg",des);
    }
    @Test
    public void test4(){
        String imgPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0603\\1000530002A.jpg";
        Mat src = Imgcodecs.imread(imgPath);
        Mat gray=new Mat();
        Imgproc.cvtColor(src,gray,COLOR_BGR2GRAY);
        Point center = new Point(gray.cols() / 2, gray.rows() / 2);
        Mat padded=new Mat();
        int opWidth = getOptimalDFTSize(gray.rows());
        int opHeight = getOptimalDFTSize(gray.cols());
        Core.copyMakeBorder(gray,padded,0,opWidth-gray.rows(),0,opHeight-gray.cols(),BORDER_CONSTANT,Scalar.all(0));
        Mat real = new Mat(padded.size(), CV_32F);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0603\\1000530002A_3.jpg",padded);

        List<Mat> planes=new ArrayList<>();
        padded.convertTo(padded, CvType.CV_32F);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0603\\1000530002A_4.jpg",padded);

        planes.add(padded);
       planes.add(new Mat(padded.size(),CV_32F));
       Mat comImg = new Mat();
        Core.merge(planes,comImg);
        Core.dft(comImg,comImg);
        Core.split(comImg,planes);
        Core.magnitude(planes.get(0),planes.get(1),planes.get(0));
        Mat magMat=planes.get(0);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0603\\1000530002A_2.jpg",magMat);

        Rect rect = new Rect(0, 0, magMat.cols() - 2, magMat.rows() - 2);
        magMat=magMat.submat(rect);
        int cx=magMat.cols() /2;
        int cy=magMat.rows() /2;
        Mat q0 = magMat.submat(new Rect(0, 0, cx, cy));
        Mat q1 = magMat.submat(new Rect(0, cy, cx, cy));
        Mat q2 = magMat.submat(new Rect(cx, cy, cx, cy));
        Mat q3 = magMat.submat(new Rect(cx, 0, cx, cy));
        Mat tmp=new Mat();
        q0.copyTo(tmp);
        q2.copyTo(q0);
        tmp.copyTo(q2);
        Core.normalize(magMat,magMat,0,1,NORM_MINMAX);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0603\\1000530002A_5.jpg",magMat);

        Mat magImg = new Mat(magMat.size(), CV_8UC1);
        magMat.convertTo(magImg,CV_8UC1);
        Imgproc.threshold(magImg,magImg,GRAY_THRESH,255,THRESH_BINARY);
        double pi180 = Math.PI / 180;
        double pi2 = Math.PI / 2;
        Mat linImg=new Mat(magImg.size(),CV_8SC3);
        Mat lines=new Mat();
        Imgproc.HoughLinesP(magImg,lines,1,pi180,HOUGH_VOTE);
        int numLines=lines.rows();
        double theta=0;
        for (int i = 0; i <numLines ; i++) {
            double[] vec = lines.get(i, 0);


            double  x1 = vec[0],
                    y1 = vec[1],
                    x2 = vec[2],
                    y2 = vec[3];
            Imgproc.line(linImg,new Point(x1,y1),new Point(x2,y2),new Scalar(255, 0, 0), 3, 8, 0);

            if(x2-x1==0){
                continue;
            }else {
                theta=((y2-y1) / (x2 -x1));
            }
            if(Math.abs(theta) < pi180 || Math.abs(theta-pi2)<pi180){
                continue;
            }else {
                System.out.println(theta);
            }

        }
        double angle = Math.atan(theta);
        System.out.println(angle);
        angle=angle*(180/Math.PI);
        System.out.println(angle);
        angle=(angle-90)/(src.width()/src.height());
        System.out.println(angle);
        Point point = new Point(src.width() / 2, src.height() / 2);
        Mat rotation=Imgproc.getRotationMatrix2D(point,angle,1.0);
        Mat dstImag=new Mat(src.size(),CV_8UC3);
        Imgproc.warpAffine(src,dstImag,rotation,src.size());
       Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0603\\1000530002A_1.jpg",dstImag);


    }
    /**
     * @Description 做了数组中元素的减法
     * @Date  2021/5/19  14:52
     * @param points
     * @throws
     * @return java.util.List<java.lang.Double>
     * @Author menshaojing
     * @Date  2021/5/19  14:52
     **/
    public List<Double> diff( List<Point> points){
        List<Double> list=new ArrayList<>();
        points.forEach(point -> {
            list.add(point.y-point.x);
        });
        return list;
    }
    /**
     * @Description 做了数组中元素的加法
     * @Date  2021/5/19  14:56
     * @param points
     * @throws
     * @return java.util.List<java.lang.Double>
     * @Author menshaojing
     * @Date  2021/5/19  14:56
     **/
    public List<Double> sum( List<Point> points){
        List<Double> list=new ArrayList<>();
        points.forEach(point -> {
            list.add(point.y+point.x);
        });
        return list;
    }
    // 点到点的距离
    private static double getSpacePointToPoint(Point p1, Point p2) {
        double a = p1.x - p2.x;
        double b = p1.y - p2.y;
        return Math.sqrt(a * a + b * b);
    }
}
