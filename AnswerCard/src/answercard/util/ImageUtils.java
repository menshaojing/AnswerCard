package answercard.util;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

/**
 * @ClassName ImageUtils
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/5/12 11:38
 * @Version 1.0
 */
public class ImageUtils {
    private Mat grayImg = new Mat();
    private Mat tmpImg = new Mat();
    private Mat tmpImg2 = new Mat();
    /**
     * @Description 根据最大轮廓（矩形）进行分割
     * @Date  2021/5/18  13:49
     * @param imgIn
     * @throws
     * @return int
     * @Author menshaojing
     * @Date  2021/5/18  13:49
     **/
    public Mat RectSegmentation(Mat imgIn) {
        Imgproc.resize(imgIn, tmpImg, new Size(imgIn.width(), imgIn.height()), 0.25, 0.25, 1);
        tmpImg2 = tmpImg.clone();
        PreOperation(tmpImg2);
        return objectiveQuestion(grayImg);
    }
     /**
      * @Description 四个点位获取
      * @Date  2021/5/19  13:13
      * @param mat
      * @throws
      * @return java.util.List<org.opencv.core.Point>
      * @Author menshaojing
      * @Date  2021/5/19  13:13
      **/
     public    List<Point>  fourPoints(Mat mat){
         List<Point> list=new ArrayList<>();
         Imgproc.resize(mat, tmpImg, new Size(mat.width(), mat.height()), 0, 0, 1);
         tmpImg2 = tmpImg.clone();
         PreOperation(tmpImg2);
         return fourPointsParse(grayImg);
    }
    /**
     * @Description  四个点位获取解析
     * @Date  2021/5/19  13:16
     * @param imgIn
     * @throws
     * @return java.util.List<org.opencv.core.Point>
     * @Author menshaojing
     * @Date  2021/5/19  13:16
     **/
    List<Point> fourPointsParse(Mat imgIn){
        List<Point> list=new ArrayList<>();
        Mat cannyImg = new Mat();
        int threshLow =35;
        Imgproc.Canny(imgIn, cannyImg, threshLow, 3 * threshLow, 3);
         Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cannyImg.jpg", cannyImg);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
        Imgproc.dilate(cannyImg, cannyImg, kernel, new Point(-1, -1), 3, 1, new Scalar(1));
        Imgproc.erode(cannyImg, cannyImg, kernel, new Point(-1, -1), 3, 1, new Scalar(1));
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/dilate.jpg", cannyImg);
         Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/erode.jpg", cannyImg);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat contourImg = tmpImg.clone();
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cannyImg.jpg", cannyImg);
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
            //300分辨率 3347.42
            //96分辨率 250 350
            // 200 分辨率 1490.27
            // 150 分辨率 840
            // 100 分辨率  384
            // 120 分辨率  532
            if (approx.rows() == 4 && Math.abs(Imgproc.contourArea(approx)) > 700 && Math.abs(Imgproc.contourArea(approx)) <840&&
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
        Imgproc.drawContours(contourImg, squares, -1, color, 2);
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/drawContours.jpg", contourImg);
        //中心点坐标记录
        List<Point> centers=new ArrayList<>();
       Map <Point,Point []> pointMap=new HashMap<>();
        //获取所有中心点位信息
        for (MatOfPoint matOfPoint: squares) {
            MatOfPoint2f tmp = new MatOfPoint2f();
            matOfPoint.convertTo(tmp, CvType.CV_32F);
            //获取中心点位
            RotatedRect rect = Imgproc.minAreaRect(tmp);
            // 获取矩形的四个顶点
            Point[] rectPoint = new Point[4];
            rect.points(rectPoint);
            centers.add(rect.center);
            pointMap.put(rect.center, rectPoint);
           /* double angle = rect.angle;
            Point center = rect.center;
            System.out.println(angle);*/

        }
        List<Double> listDiff = diff(centers);
        List<Double> listSum = sum(centers);
        int sumMinIndex = listSum.indexOf(listSum.stream().min(Double::compareTo).get());
        int sumMaxIndex = listSum.indexOf(listSum.stream().max(Double::compareTo).get());
        int diffMaxIndex = listDiff.indexOf(listDiff.stream().max(Double::compareTo).get());
        int diffMinIndex = listDiff.indexOf(listDiff.stream().min(Double::compareTo).get());
        //按顺序找到对应的坐标0123 分别是左上，右上，右下，左下
        Point point0 = centers.get(sumMinIndex);
        Point point1 = centers.get(diffMinIndex);
        Point point2 = centers.get(sumMaxIndex);
        Point point3 = centers.get(diffMaxIndex);
        //计算最大坐标
        //左上
        Point[] rectPoint0 = pointMap.get(point0);
        List<Double> rectPointSum = sum(Arrays.asList(rectPoint0));
        int  rectPointSumMinIndex = rectPointSum.indexOf(rectPointSum.stream().min(Double::compareTo).get());
        Point p0 = rectPoint0[rectPointSumMinIndex];
        //右上
        Point[] rectPoint1 = pointMap.get(point1);
        List<Double> rectPointDiff = diff(Arrays.asList(rectPoint1));
        int rectPointDiffMinIndex = rectPointDiff.indexOf(rectPointDiff.stream().min(Double::compareTo).get());
        Point p1 = rectPoint1[rectPointDiffMinIndex];
        //右下
        Point[] rectPoint2 = pointMap.get(point2);
        List<Double> rectPointSum1 = sum(Arrays.asList(rectPoint2));
        int rectPointSumMaxIndex = rectPointSum1.indexOf(rectPointSum1.stream().max(Double::compareTo).get());
        Point p2 = rectPoint2[rectPointSumMaxIndex];
        //左下
        Point[] rectPoint3 = pointMap.get(point3);
        List<Double> rectPointDiff1 = diff(Arrays.asList(rectPoint3));
        int rectPointDiffMaxIndex = rectPointDiff1.indexOf(rectPointDiff1.stream().max(Double::compareTo).get());
        Point p3= rectPoint3[rectPointDiffMaxIndex];

       /* list.add(point3);
        list.add(point0);
        list.add(point1);
        list.add(point2);*/
        list.add(p3);
        list.add(p0);
        list.add(p1);
        list.add(p2);
        //计算中心与水平线的夹角
        MatOfPoint   centerMatOfPoint=new MatOfPoint();
        centerMatOfPoint.fromArray(p3,p0,p1,p2);
        MatOfPoint2f centerTmp = new MatOfPoint2f();
        centerMatOfPoint.convertTo(centerTmp, CvType.CV_32F);
        //获取中心点位
        RotatedRect rect = Imgproc.minAreaRect(centerTmp);
        System.err.println("夹角："+rect.angle);
        System.err.println("中心点位："+rect.center.x+","+rect.center.y);
        return list;
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
    public  List<MatOfPoint> answerCard(Mat imgIn) {
        Imgproc.resize(imgIn, tmpImg, new Size(imgIn.width(), imgIn.height()), 0, 0, 1);
        tmpImg2 = tmpImg.clone();
        PreOperation(tmpImg2);
        //解析
        return answerCardParse(grayImg);
    }
    public int answerCard1(Mat imgIn) {
        Imgproc.resize(imgIn, tmpImg, new Size(imgIn.width(), imgIn.height()), 0, 0, 1);
        tmpImg2 = tmpImg.clone();
        PreOperation(tmpImg2);
        //解析
        answerCardRectangleParse(grayImg);
        return 0;
    }
    public  List<MatOfPoint> objectiveGuestionBlock(Mat imgIn,Mat src) {
        Mat cannyImg = new Mat();
        int threshLow =35;
        Imgproc.Canny(imgIn, cannyImg, threshLow, 3 * threshLow, 3);
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cannyImg.jpg", cannyImg);
      //  Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(1062, 177));
        Imgproc.dilate(cannyImg, cannyImg, new Mat(), new Point(-1, -1), 50, 1, new Scalar(1));
        Imgproc.erode(cannyImg, cannyImg,  new Mat(), new Point(-1, -1), 50, 1, new Scalar(1));
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/dilate.jpg", cannyImg);
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/erode.jpg", cannyImg);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat contourImg = src.clone();
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cannyImg.jpg", cannyImg);
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
            if (approx.rows() == 4 && //Math.abs(Imgproc.contourArea(approx)) >180000 && Math.abs(Imgproc.contourArea(approx)) <188100&&
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
        Imgproc.drawContours(contourImg, squares, -1, color, 2);
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/drawContours.jpg", contourImg);
        return  squares;
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
    List<MatOfPoint> answerCardParse(Mat imgIn) {
        Mat cannyImg = new Mat();
        int threshLow =30;
        Imgproc.Canny(imgIn, cannyImg, threshLow, 3 * threshLow, 3);
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cannyImg.jpg", cannyImg);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
        Imgproc.dilate(cannyImg, cannyImg, kernel, new Point(-1, -1), 1, 1, new Scalar(1));
        Imgproc.erode(cannyImg, cannyImg, kernel, new Point(-1, -1), 1, 1, new Scalar(1));
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/dilate.jpg", cannyImg);
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/erode.jpg", cannyImg);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat contourImg = tmpImg.clone();
       Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cannyImg.jpg", cannyImg);
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
        Imgproc.drawContours(contourImg, squares, -1, color, 2);
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/drawContours.jpg", contourImg);
        return  squares;
    }
    /**
     * @Description 获取答题卡矩形图
     * @Date  2021/5/19  10:01
     * @param imgIn
     * @throws
     * @return int
     * @Author menshaojing
     * @Date  2021/5/19  10:01
     **/
     int answerCardRectangleParse(Mat imgIn) {
        Mat cannyImg = new Mat();
        int threshLow =35;
        Imgproc.Canny(imgIn, cannyImg, threshLow, 3 * threshLow, 3);
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cannyImg.jpg", cannyImg);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
        Imgproc.dilate(cannyImg, cannyImg, kernel, new Point(-1, -1), 12, 1, new Scalar(1));
        Imgproc.erode(cannyImg, cannyImg, kernel, new Point(-1, -1), 12, 1, new Scalar(1));
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/dilate.jpg", cannyImg);
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/erode.jpg", cannyImg);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat contourImg = tmpImg.clone();
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cannyImg.jpg", cannyImg);
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
            if (approx.rows() == 4 && Math.abs(Imgproc.contourArea(approx)) > 20000 && Math.abs(Imgproc.contourArea(approx)) <45000&&
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
        Imgproc.drawContours(contourImg, squares, -1, color, 2);
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/drawContours.jpg", contourImg);
        return  0;
    }
    /**
     * @Description 根据四个极点进行分割
     * @Date  2021/5/18  13:48
     * @param imgIn
     * @param corners
     * @throws
     * @return org.opencv.core.Mat
     * @Author menshaojing
     * @Date  2021/5/18  13:48
     **/
    public Mat RectSegmentation(Mat imgIn,List<Point> corners) {
        Imgproc.resize(imgIn, tmpImg, new Size(imgIn.width(), imgIn.height()), 0, 0, 1);
        return  rectExtract(corners);
    }

    public int PreOperation(Mat imgIn) {

        Imgproc.cvtColor(imgIn, grayImg, Imgproc.COLOR_RGB2GRAY);
        //大津法找到敏感区域
        /*Imgproc.threshold(grayImg,grayImg, 0, 255,Imgproc.THRESH_OTSU );
        //为什么要转换，因为白色是有数据的区域,轮廓是围绕白色区域的
        Imgproc.threshold(grayImg,grayImg, 0, 255,Imgproc.THRESH_BINARY_INV);
        //形态学变化，确保目标区别都是连在一起的
        Imgproc.morphologyEx(grayImg,grayImg,Imgproc.MORPH_OPEN,new Mat());*/
       // Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/grayImg.jpg", grayImg);
        Imgproc.GaussianBlur(grayImg, grayImg, new Size(3, 3), 0, 0);
        //Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/GaussianBlur.jpg", grayImg);
        return 0;
    }

    Mat InContours(Mat imgIn) {
        Mat cannyImg = new Mat();
        int threshLow = 35;
        Imgproc.Canny(imgIn, cannyImg, threshLow, 3 * threshLow, 3);
       Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cannyImg.jpg", cannyImg);
        Imgproc.dilate(cannyImg, cannyImg, new Mat(), new Point(-1, -1), 3, 1, new Scalar(1));Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/dilate.jpg", cannyImg);
       Imgproc.erode(cannyImg, cannyImg, new Mat(), new Point(-1, -1), 3, 1, new Scalar(1));
       Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/erode.jpg", cannyImg);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat contourImg = tmpImg.clone();

      //  Imgproc.drawContours(contourImg, contours, -1, new Scalar(0, 0, 205), 2, 8, hierarchy);
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
        Imgproc.drawContours(contourImg, squares, -1, new Scalar(0, 255, 0), 2);
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/drawContours.jpg", contourImg);
        // 找出外接矩形最大的四边形
        int index = findLargestSquare(squares);
        if (squares.size() == 0 || index == -1) {
            return imgIn;
        }

        // 找到这个最大的四边形对应的凸边框，再次进行多边形拟合，此次精度较高，拟合的结果可能是大于4条边的多边形

        MatOfPoint largest_square = squares.get(index);
        MatOfPoint2f tmp = new MatOfPoint2f();
        largest_square.convertTo(tmp, CvType.CV_32F);
        Imgproc.approxPolyDP(tmp, approx, 3, true);
        List<Point> newPointList = new ArrayList<>();
        double maxL = Imgproc.arcLength(approx, true) * 0.02;

        // 找到高精度拟合时得到的顶点中 距离小于低精度拟合得到的四个顶点maxL的顶点，排除部分顶点的干扰
        for (Point p : approx.toArray()) {
            if (!(getSpacePointToPoint(p, largest_square.toList().get(0)) > maxL &&
                    getSpacePointToPoint(p, largest_square.toList().get(1)) > maxL &&
                    getSpacePointToPoint(p, largest_square.toList().get(2)) > maxL &&
                    getSpacePointToPoint(p, largest_square.toList().get(3)) > maxL)) {
                newPointList.add(p);
            }
        }

        // 找到剩余顶点连线中，边长大于 2 * maxL的四条边作为四边形物体的四条边
        List<double[]> lines = new ArrayList<>();
        for (int i = 0; i < newPointList.size(); i++) {
            Point p1 = newPointList.get(i);
            Point p2 = newPointList.get((i + 1) % newPointList.size());
            if (getSpacePointToPoint(p1, p2) > 2 * maxL) {
                lines.add(new double[]{p1.x, p1.y, p2.x, p2.y});
                Imgproc.line(grayImg,p1,p2,new Scalar(225),2,8);
            }
        }

// 计算出这四条边中 相邻两条边的交点，即物体的四个顶点
        List<Point> corners = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            Point corner = computeIntersect(lines.get(i), lines.get((i + 1) % lines.size()));
            corners.add(corner);
        }
        for (int i = 0; i < corners.size(); i++) {
            Imgproc.circle(tmpImg2,corners.get(i),5,new Scalar(255,255,255),-1);
            Imgproc.line(tmpImg2,corners.get(i),corners.get(((i+1) % corners.size())),new Scalar(255, 0, 0),1,8);
        }
       // Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/tmpImg2.jpg", tmpImg2);

        // 对顶点顺时针排序
        sortCorners(corners);

        return  rectExtract(corners);
    }

    Mat objectiveQuestion(Mat imgIn) {
        Mat cannyImg = new Mat();
        int threshLow = 35;
        Imgproc.Canny(imgIn, cannyImg, threshLow, 3 * threshLow, 3);
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cannyImg.jpg", cannyImg);
        Imgproc.dilate(cannyImg, cannyImg, new Mat(), new Point(-1, -1), 10, 1, new Scalar(1));Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/dilate.jpg", cannyImg);
        Imgproc.erode(cannyImg, cannyImg, new Mat(), new Point(-1, -1), 10, 1, new Scalar(1));
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/erode.jpg", cannyImg);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat contourImg = tmpImg.clone();

        //  Imgproc.drawContours(contourImg, contours, -1, new Scalar(0, 0, 205), 2, 8, hierarchy);
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
        Imgproc.drawContours(contourImg, squares, -1, new Scalar(0, 255, 0), 2);
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/drawContours.jpg", contourImg);
        // 找出外接矩形最大的四边形
        int index = findLargestSquare(squares);
        if (squares.size() == 0 || index == -1) {
            return imgIn;
        }

        // 找到这个最大的四边形对应的凸边框，再次进行多边形拟合，此次精度较高，拟合的结果可能是大于4条边的多边形

        MatOfPoint largest_square = squares.get(index);
        MatOfPoint2f tmp = new MatOfPoint2f();
        largest_square.convertTo(tmp, CvType.CV_32F);
        Imgproc.approxPolyDP(tmp, approx, 3, true);
        List<Point> newPointList = new ArrayList<>();
        double maxL = Imgproc.arcLength(approx, true) * 0.02;

        // 找到高精度拟合时得到的顶点中 距离小于低精度拟合得到的四个顶点maxL的顶点，排除部分顶点的干扰
        for (Point p : approx.toArray()) {
            if (!(getSpacePointToPoint(p, largest_square.toList().get(0)) > maxL &&
                    getSpacePointToPoint(p, largest_square.toList().get(1)) > maxL &&
                    getSpacePointToPoint(p, largest_square.toList().get(2)) > maxL &&
                    getSpacePointToPoint(p, largest_square.toList().get(3)) > maxL)) {
                newPointList.add(p);
            }
        }

        // 找到剩余顶点连线中，边长大于 2 * maxL的四条边作为四边形物体的四条边
        List<double[]> lines = new ArrayList<>();
        for (int i = 0; i < newPointList.size(); i++) {
            Point p1 = newPointList.get(i);
            Point p2 = newPointList.get((i + 1) % newPointList.size());
            if (getSpacePointToPoint(p1, p2) > 2 * maxL) {
                lines.add(new double[]{p1.x, p1.y, p2.x, p2.y});
                Imgproc.line(grayImg,p1,p2,new Scalar(225),2,8);
            }
        }

// 计算出这四条边中 相邻两条边的交点，即物体的四个顶点
        List<Point> corners = new ArrayList<>();


        for (int i = 0; i < lines.size(); i++) {
            Point corner = computeIntersect(lines.get(i), lines.get((i + 1) % lines.size()));
            corners.add(corner);
        }
        for (int i = 0; i < corners.size(); i++) {
            Imgproc.circle(tmpImg2,corners.get(i),5,new Scalar(255,255,255),-1);
            Imgproc.line(tmpImg2,corners.get(i),corners.get(((i+1) % corners.size())),new Scalar(255, 0, 0),1,8);
        }
        // Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/tmpImg2.jpg", tmpImg2);
        //进行排序
        // 对顶点顺时针排序
        List<Double> listDiff = diff(corners);
        List<Double> listSum = sum(corners);
        int sumMinIndex = listSum.indexOf(listSum.stream().min(Double::compareTo).get());
        int sumMaxIndex = listSum.indexOf(listSum.stream().max(Double::compareTo).get());
        int diffMaxIndex = listDiff.indexOf(listDiff.stream().max(Double::compareTo).get());
        int diffMinIndex = listDiff.indexOf(listDiff.stream().min(Double::compareTo).get());
        //按顺序找到对应的坐标0123 分别是左上，右上，右下，左下
        Point point0 = corners.get(sumMinIndex);
        Point point1 = corners.get(diffMinIndex);
        Point point2 = corners.get(sumMaxIndex);
        Point point3 = corners.get(diffMaxIndex);
        corners.add(0,point3);
        corners.add(1,point0);
        corners.add(2,point1);
        corners.add(3,point2);
       // sortCorners(corners);

        return  objectiveQuestionRectExtract(corners);
    }
    private Mat objectiveQuestionRectExtract(List<Point> corners) {
        // sortCorners(corners);
// 计算目标图像的尺寸
        Point p0 = corners.get(0);
        Point p1 = corners.get(1);
        Point p2 = corners.get(2);
        Point p3 = corners.get(3);
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


/*
// 如果提取出的图片宽小于高，则旋转90度
        if (imgWidth < imgHeight) {
            double temp = imgWidth;
            imgWidth = imgHeight;
            imgHeight = temp;
            Point tempPoint = p0.clone();
            p0 = p1.clone();
            p1 = p2.clone();
            p2 = p3.clone();
            //rotate the Image
            p3 = tempPoint.clone();
        }
*/

        MatOfPoint2f cornerMat = new MatOfPoint2f(p0, p1, p2, p3);

        Mat quad = Mat.zeros((int) imgHeight , (int) imgWidth , CvType.CV_8UC3);

        MatOfPoint2f quadMat = new MatOfPoint2f(
                new Point(6, quad.rows()),
                new Point(6, 0),
                new Point(quad.cols(), 0),
                new Point(quad.cols(), quad.rows()));

// 提取图像
        Mat transmtx = Imgproc.getPerspectiveTransform(cornerMat, quadMat);
        Imgproc.warpPerspective(tmpImg, quad, transmtx, quad.size());
        return quad;
    }
    private Mat rectExtract(List<Point> corners) {
       // sortCorners(corners);
// 计算目标图像的尺寸
        Point p0 = corners.get(0);
        Point p1 = corners.get(1);
        Point p2 = corners.get(2);
        Point p3 = corners.get(3);
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


/*// 如果提取出的图片宽小于高，则旋转90度
        if (imgWidth < imgHeight) {
            double temp = imgWidth;
            imgWidth = imgHeight;
            imgHeight = temp;
            Point tempPoint = p0.clone();
            p0 = p1.clone();
            p1 = p2.clone();
            p2 = p3.clone();
            //rotate the Image
            p3 = tempPoint.clone();
        }*/

        MatOfPoint2f cornerMat = new MatOfPoint2f(p0, p1, p2, p3);

        Mat quad = Mat.zeros((int) imgHeight , (int) imgWidth , CvType.CV_8UC3);

        MatOfPoint2f quadMat = new MatOfPoint2f(
                new Point(0, quad.rows()),
                new Point(0, 0),
                new Point(quad.cols(), 0),
                new Point(quad.cols(), quad.rows()));

// 提取图像
        Mat transmtx = Imgproc.getPerspectiveTransform(cornerMat, quadMat);
        Imgproc.warpPerspective(tmpImg, quad, transmtx, quad.size());
        return quad;
    }
    /**
     * @Description 2D空间旋转
     * @Date  2021/5/19  12:56
     * @param mat
     * @param matOfPoint2f
     * @throws
     * @return org.opencv.core.Mat
     * @Author menshaojing
     * @Date  2021/5/19  12:56
     **/
    Mat twoDimensionalRotation(Mat mat ,MatOfPoint2f matOfPoint2f){
        RotatedRect rect = Imgproc.minAreaRect(matOfPoint2f);
        // 获取矩形的四个顶点
        Point[] rectPoint = new Point[4];
        rect.points(rectPoint);
        double angle = rect.angle;
        Point center = rect.center;

        System.out.println(angle);
        Mat CorrectImg = new Mat(mat.size(), mat.type());
        mat.copyTo(CorrectImg);

        // 得到旋转矩阵算子
        Mat matrix = Imgproc.getRotationMatrix2D(center, angle, 0.9);

        Imgproc.warpAffine(CorrectImg, CorrectImg, matrix, CorrectImg.size(), 1, 0, new Scalar(0, 0, 0));

        return CorrectImg;
    }

    // 对多个点按顺时针排序
    private static void sortCorners(List<Point> corners) {
        if (corners.size() == 0) return;
        Point p1 = corners.get(0);
        int index = 0;
        for (int i = 1; i < corners.size(); i++) {
            Point point = corners.get(i);
            if (p1.x > point.x) {
                p1 = point;
                index = i;
            }
        }

        corners.set(index, corners.get(0));
        corners.set(0, p1);

        Point lp = corners.get(0);
        for (int i = 1; i < corners.size(); i++) {
            for (int j = i + 1; j < corners.size(); j++) {
                Point point1 = corners.get(i);
                Point point2 = corners.get(j);
                if ((point1.y - lp.y * 1.0) / (point1.x - lp.x) > (point2.y - lp.y * 1.0) / (point2.x - lp.x)) {
                    Point temp = point1.clone();
                    corners.set(i, corners.get(j));
                    corners.set(j, temp);
                }
            }
        }
    }

    // 两直线的交点
    private static Point computeIntersect(double[] a, double[] b) {
        if (a.length != 4 || b.length != 4)
            throw new ClassFormatError();
        double x1 = a[0], y1 = a[1], x2 = a[2], y2 = a[3], x3 = b[0], y3 = b[1], x4 = b[2], y4 = b[3];
        double d = ((x1 - x2) * (y3 - y4)) - ((y1 - y2) * (x3 - x4));
        if (d != 0) {
            Point pt = new Point();
            pt.x = ((x1 * y2 - y1 * x2) * (x3 - x4) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
            pt.y = ((x1 * y2 - y1 * x2) * (y3 - y4) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
            return pt;
        } else {
            return new Point(-1, -1);
        }
    }

    // 点到点的距离
    private static double getSpacePointToPoint(Point p1, Point p2) {
        double a = p1.x - p2.x;
        double b = p1.y - p2.y;
        return Math.sqrt(a * a + b * b);
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
}
