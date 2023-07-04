package answercard.util;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import javax.imageio.*;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @ClassName PicFlip
 * @Description 图片矫正
 * @Author menshaojing
 * @Date 2021/5/22 16:48
 * @Version 1.0
 */

public class PicFlip {
    /**
     * @Description
     *      A4像素尺寸 96 分辨率  像素宽高：794 1123
     *      300分辨率 像素宽高： 2480 3508
     *      200分辨率 像素宽高： 1652 2338
     *      150分辨率 像素宽高： 1240 1754
     *      100分辨率 像素宽高： 827  1098
     *      120分辨率 像素宽高： 992  1403
     * @Date  2021/6/2  11:40
     * @Author menshaojing
     * @Date  2021/6/2  11:40
     **/
   private final Size A4_SIZE = new Size(1240,1754);
   private final Size A3_SIZE = new Size(2480,1754);
    private static final double INCH_2_CM = 2.54d;
   /**
    * @Description 定位点矩形上边距 上下是8mm
    * @Author menshaojing
    * @Date  2021/6/2  11:41
    **/
    private  final int LOCATION_POINT_TOP_MARGIN=8;
    /**
     * @Description 定位点矩形上边距 左右 9mm
     * @Author menshaojing
     * @Date  2021/6/2  11:41
     **/
    private final int LOCATION_POINT_LEFT_MARGIN=9;
    /**
     * @Description 300 分辨率 像素比： 11.8
     *      200 分辨率 像素比：7.88
     *      150 分辨率 像素比：5.90
     *      100 分辨率 像素比：3.97
     *      120 分辨率 像素比：4.72
     * @Author menshaojing
     * @Date  2021/6/2  11:42
     **/
    private final double pixel = 5.90;
    Mat grayImg = new Mat();
    Mat tmpImg = new Mat();
    Mat tmpImg2 = new Mat();
    Mat cannyImg = new Mat();
    List<Point> list=new ArrayList<>();
    public void init() {
         System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    /**
     * @Description 矫正图片
     * @Date  2021/6/2  12:27
     * @param img
     * @param size
     * @throws
     * @return org.opencv.core.Mat
     * @Author menshaojing
     * @Date  2021/6/2  12:27
     **/
    public Mat flip(String img, Integer size){
        Mat quad =null;
        Mat transmtx=null;
        Mat currentMat=null;
        Mat range=null;
        MatOfPoint2f cornerMat=null;
        MatOfPoint2f quadMat=null;
        try {
            init();
            //-------------------图像预处理-------start------------
          
            System.out.println("图像预处理开始");
            imagePreprocessing(img);
            System.out.println("图像预处理结束");
            //-------------------图像预处理-------end------------

            //-------------------获取四点坐标-----start----------
            System.out.println("获取四点坐标开始");
            getFourPoint(size);
            System.out.println("获取四点坐标结束");
            //-------------------获取四点坐标-----end----------

            //-------------------根据四点截取图像-----start----------
            System.out.println("根据四点截取图像开始");
            // 计算目标图像的尺寸
            Point p0 = list.get(0);
            Point p1 = list.get(1);
            Point p2 = list.get(2);
            Point p3 = list.get(3);
            double space0 = getSpacePointToPoint(p0, p1);
            double space1 = getSpacePointToPoint(p1, p2);
            double space2 = getSpacePointToPoint(p2, p3);
            double space3 = getSpacePointToPoint(p3, p0);
            //Add the perspective correction
       /* double paraFix1 = (space3 / space1) > 1 ? (space3 / space1) : (space1 / space3);
        double paraFix2 = (space2 / space0) > 1 ? (space2 / space0) : (space0 / space2);*/
            double imgWidth = space1 > space3 ? space1 : space3;
            double imgHeight = space0 > space2 ? space0 : space2;
            double tmp = 0.0;
            if(size==2){
                //A4纸张是高大于宽
                if(imgHeight<imgWidth){
                    tmp=imgHeight;
                    imgHeight=imgWidth;
                    imgWidth=tmp;
                }
            }else{
                //A3纸张是宽大于高
                if(imgHeight>imgWidth){
                    tmp=imgHeight;
                    imgHeight=imgWidth;
                    imgWidth=tmp;
                }
            }

             cornerMat = new MatOfPoint2f(p0, p1, p2, p3);

             quad = Mat.zeros((int) imgHeight , (int) imgWidth , CvType.CV_8UC3);

             quadMat = new MatOfPoint2f(
                    new Point(0, quad.rows()),
                    new Point(0, 0),
                    new Point(quad.cols(), 0),
                    new Point(quad.cols(), quad.rows()));

            // 提取图像
             transmtx = Imgproc.getPerspectiveTransform(cornerMat, quadMat,Imgproc.INTER_LINEAR);
            Imgproc.warpPerspective(tmpImg, quad, transmtx, quad.size());
            System.out.println("根据四点截取图像结束");
            //-------------------根据四点截取图像-----end----------

            //-------------------还原图像-----------start--------
            System.out.println("还原图像开始");
            Mat mat = null;
            if(size==2) {
                mat = new Mat(A4_SIZE, quad.type(), new Scalar(255,255,255));
            } else {
                mat = new Mat(A3_SIZE, quad.type(), new Scalar(255,255,255));
            }
            //设置实际内容中心尺寸  W : 原图像尺寸宽  - 定点左边距*2   H：原图像尺寸宽  - 定点上边距*2
            double width =mat.width() - LOCATION_POINT_LEFT_MARGIN * pixel * 2;
            double height = mat.height()-LOCATION_POINT_TOP_MARGIN * pixel * 2;
             currentMat = new Mat(new Size(width, height), quad.type());
            Imgproc.resize(quad, currentMat, currentMat.size(), 0, 0, Imgproc.INTER_CUBIC);
            int left =(int) (LOCATION_POINT_LEFT_MARGIN * pixel );
            int top =(int) (LOCATION_POINT_TOP_MARGIN * pixel );
             range=mat.colRange(left,currentMat.cols() +left);
            range=range.rowRange(top,currentMat.rows()+top);
            currentMat.copyTo(range);
            System.out.println("还原图像结束");
            return mat;

            //-------------------还原图像-----------end--------
        }finally {
            if(null!=grayImg){
                grayImg.release();
            }
            if(null!=tmpImg){
                tmpImg.release();
            }
            if(null!=tmpImg2){
                tmpImg2.release();
            }
            if(null!=cannyImg){
                cannyImg.release();
            }
            if(null!=quad){
                quad.release();
            }
            if(null!=transmtx){
                transmtx.release();
            }
            if(null!=currentMat){
                currentMat.release();
            }
            if(null!=range){
                range.release();
            }
            if(null!=cornerMat){
                cornerMat.release();
            }
            if(null!=quadMat){
                quadMat.release();
            }
            /*if(!CollectionUtils.isEmpty(contours)){
                for (MatOfPoint matOfPoint:contours){
                    matOfPoint.release();
                }
            }*/

        }

    }
    List<MatOfPoint> contours = new ArrayList<>();

    /**
   * @Description 获取四个极点坐标
   * @param size 答题卡尺寸
   * @throws
   * @return void
   * @Author menshaojing
   * @Date  2021/7/1  10:58
   **/
   public void   getFourPoint(Integer size){
       Mat hierarchy = new Mat();
       try {
           Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

           // 找出轮廓对应凸包的四边形拟合
           List<MatOfPoint> squares = new ArrayList<>();
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
               //300分辨率 定位矩形面积 3347.42
               //96分辨率 定位矩形面积    350
               // 200 分辨率 定位矩形面积 1490.27
               // 150 分辨率 定位矩形面积 840
               // 100 分辨率 定位矩形面积 384
               // 120 分辨率 定位矩形面积 532
               if (approx.rows() == 4 && Math.abs(Imgproc.contourArea(approx)) > 500 && Math.abs(Imgproc.contourArea(approx)) <840&&
                       Imgproc.isContourConvex(approxf1)) {
                   double maxCosine = 0;
                   for (int j = 2; j < 5; j++) {
                       double cosine = Math.abs(getAngle(approxf1.toArray()[j % 4], approxf1.toArray()[j - 2], approxf1.toArray()[j - 1]));
                       maxCosine = Math.max(maxCosine, cosine);
                   }
                   // 角度大概72度
                   if (maxCosine <= 0.5) {
                       squares.add(approxf1);
                   }
               }
               //释放对象
               if(null!=contourHull){
                   contourHull.release();
               }

           }
           // Scalar color=new Scalar(0, 255, 0);
           // Imgproc.drawContours(contourImg, squares, -1, color, 2);
           // Imgcodecs.imwrite("D:\\image\\card\\14\\2\\"+contourImg.nativeObj+".jpg",contourImg);
           //定位点不足四个异常抛出
           if(squares.size()<4){
               System.out.println("ExceptionEnum.ANCHOR_POINT_MISSING.getMsg()");
              // throw new BaseException(ExceptionEnum.ANCHOR_POINT_MISSING.getCode(),ExceptionEnum.ANCHOR_POINT_MISSING.getMsg());
           }
           doGetFourPoint( squares,size);
       }finally {
           if(null!=hierarchy){
               hierarchy.release();
           }

       }

    }
    /**
     * @Description  获取四个极点坐标
     * @Date  2021/6/2  12:00
     * @param squares
     * @throws
     * @return void
     * @Author menshaojing
     * @Date  2021/6/2  12:00
     **/
   private void doGetFourPoint(List<MatOfPoint> squares,Integer size){
       //中心点坐标记录
       List<Point> centers=new ArrayList<>();
       Map<Point,Point []> pointMap=new HashMap<>(4);
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
       Point p00 = rectPoint0[rectPointSumMinIndex];
       //右上
       Point[] rectPoint1 = pointMap.get(point1);
       List<Double> rectPointDiff = diff(Arrays.asList(rectPoint1));
       int rectPointDiffMinIndex = rectPointDiff.indexOf(rectPointDiff.stream().min(Double::compareTo).get());
       Point p11 = rectPoint1[rectPointDiffMinIndex];
       //右下
       Point[] rectPoint2 = pointMap.get(point2);
       List<Double> rectPointSum1 = sum(Arrays.asList(rectPoint2));
       int rectPointSumMaxIndex = rectPointSum1.indexOf(rectPointSum1.stream().max(Double::compareTo).get());
       Point p22 = rectPoint2[rectPointSumMaxIndex];
       //左下
       Point[] rectPoint3 = pointMap.get(point3);
       List<Double> rectPointDiff1 = diff(Arrays.asList(rectPoint3));
       int rectPointDiffMaxIndex = rectPointDiff1.indexOf(rectPointDiff1.stream().max(Double::compareTo).get());
       Point p33= rectPoint3[rectPointDiffMaxIndex];
       list.add(p33);
       list.add(p00);
       list.add(p11);
       list.add(p22);
       //验证是否存在定位点缺失
       if(!isPointMissing( list,size)){
           System.out.println("验证是否存在定位点缺失");
          // throw new BaseException(ExceptionEnum.ANCHOR_POINT_MISSING.getCode(),ExceptionEnum.ANCHOR_POINT_MISSING.getMsg());
       }
   }


    /**
     * @Description 根据差值宽，高，是否存在定位点缺失
     * @param list 定位点集合
     * @param size 答题卡尺寸 A4 A3
     * @throws       
     * @return boolean
     * @Author menshaojing
     * @Date  2021/7/1  10:54
     **/
    private  boolean isPointMissing( List<Point> list,int size){

        //判断定位点是否异常（左上，右下构成的矩形）
        Rect rect = new Rect(list.get(1), list.get(3));


        //上差值宽，高
        double upperDifferW = size==2?A4_SIZE.width-LOCATION_POINT_LEFT_MARGIN/2*2*pixel:A3_SIZE.width-
                LOCATION_POINT_LEFT_MARGIN/2*2*pixel;
        double upperDifferH = size==2?A4_SIZE.height-LOCATION_POINT_TOP_MARGIN/2*2*pixel:A3_SIZE.height-
                LOCATION_POINT_TOP_MARGIN/2*2*pixel;
        //下差值宽，高
        double lowerDifferW=size==2?A4_SIZE.width-(LOCATION_POINT_LEFT_MARGIN+14)*2*pixel:A3_SIZE.width-
                (LOCATION_POINT_LEFT_MARGIN+14)*2*pixel;
        double  lowerDifferH=size==2?A4_SIZE.height-(LOCATION_POINT_TOP_MARGIN+12)*2*pixel:A3_SIZE.height-
                (LOCATION_POINT_TOP_MARGIN+12)*2*pixel;
        if((rect.width<upperDifferW && rect.width> lowerDifferW) &&
                (rect.height<upperDifferH && rect.height>lowerDifferH)
        ){
            //判断定位点是否异常（左下，右上构成的矩形）
             rect = new Rect(list.get(0), list.get(2));

            if((rect.width<upperDifferW && rect.width> lowerDifferW) &&
                    (rect.height<upperDifferH && rect.height>lowerDifferH)
            ){

                return true;
            }
        }
        return false;
    }
    /**
     * @Description 图像预处理
     * @Date  2021/6/2  11:48
     * @param img
     * @throws
     * @return org.opencv.core.Mat
     * @Author menshaojing
     * @Date  2021/6/2  11:48
     **/
    public void imagePreprocessing(String img){
        String imgPath = img;
        Mat src = Imgcodecs.imread(imgPath);
        try {
            Imgproc.resize(src, tmpImg, new Size(src.width(), src.height()), 0, 0, 1);
            tmpImg2 = tmpImg.clone();
            Imgproc.cvtColor(tmpImg2, grayImg, Imgproc.COLOR_RGB2GRAY);
            //大津法找到敏感区域
            Imgproc.threshold(grayImg,grayImg, 0, 255,Imgproc.THRESH_OTSU );
            //为什么要转换，因为白色是有数据的区域,轮廓是围绕白色区域的
            Imgproc.threshold(grayImg,grayImg, 0, 255,Imgproc.THRESH_BINARY_INV);
            Imgproc.GaussianBlur(grayImg, grayImg, new Size(3, 3), 0, 0);
            int threshLow =35;
            Imgproc.Canny(grayImg, cannyImg, threshLow, 3 * threshLow, 3);
            Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
            Imgproc.dilate(cannyImg, cannyImg, kernel, new Point(-1, -1), 3, 1, new Scalar(1));
            Imgproc.erode(cannyImg, cannyImg, kernel, new Point(-1, -1), 3, 1, new Scalar(1));
        }finally {
            if(null!=src){
                src.release();
            }

        }

    }
   /**
    * @Description  点到点的距离
    * @Date  2021/6/2  11:36
    * @param p1
    * @param p2
    * @throws
    * @return double
    * @Author menshaojing
    * @Date  2021/6/2  11:36
    **/
    private static double getSpacePointToPoint(Point p1, Point p2) {
        double a = p1.x - p2.x;
        double b = p1.y - p2.y;
        return Math.sqrt(a * a + b * b);
    }

   /**
    * @Description  根据三个点计算中间那个点的夹角   pt1 pt0 pt2
    * @Date  2021/6/2  11:36
    * @param pt1
    * @param pt2
    * @param pt0
    * @throws
    * @return double
    * @Author menshaojing
    * @Date  2021/6/2  11:36
    **/
    private static double getAngle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);

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
 /*   *//**
     * 改变图片DPI参数
     *
     * @param file
     * @param xDensity
     * @param yDensity
     *//*
    public  void handleDpi(File file, int xDensity, int yDensity) {
        try {
            BufferedImage image = ImageIO.read(file);
            JPEGImageEncoder jpegEncoder =  JPEGCodec.createJPEGEncoder(new FileOutputStream(file));
            JPEGEncodeParam jpegEncodeParam =  jpegEncoder.getDefaultJPEGEncodeParam(image);
            jpegEncodeParam.setDensityUnit(JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);
            jpegEncoder.setJPEGEncodeParam(jpegEncodeParam);
            jpegEncodeParam.setXDensity(xDensity);
            jpegEncodeParam.setYDensity(yDensity);
            jpegEncoder.encode(image, jpegEncodeParam);
            image.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
    /**
     * @Description 透视矫正：用于第三方答题卡
        * @param imageUrl
     * @param list
     * @param size
     * @param desList 返回矫正后的定位点信息
     * @throws
     * @return org.opencv.core.Mat
     * @Author menshaojing
     * @Date  2021/7/6  11:04
     **/
    public  void perspectiveCorrection(String imageUrl,String desImageUrl,List<Point> list, int size,List<Point> desList){
        Mat src = Imgcodecs.imread(imageUrl);
        // 计算目标图像的尺寸
        Point p0 = list.get(0);
        Point p1 = list.get(1);
        Point p2 = list.get(2);
        Point p3 = list.get(3);
        double space0 = getSpacePointToPoint(p0, p1);
        double space1 = getSpacePointToPoint(p1, p2);
        double space2 = getSpacePointToPoint(p2, p3);
        double space3 = getSpacePointToPoint(p3, p0);
       /* //Add the perspective correction
        double paraFix1 = (space3 / space1) > 1 ? (space3 / space1) : (space1 / space3);
        double paraFix2 = (space2 / space0) > 1 ? (space2 / space0) : (space0 / space2);*/
        double imgWidth = space1 > space3 ? space1 : space3;
        double imgHeight = space0 > space2 ? space0 : space2;
      /*  if(paraFix1>paraFix2){
            imgHeight=imgHeight * paraFix1;
        }else{
            imgWidth = imgWidth * paraFix2;
        }
*/
        double tmp = 0.0;
        if(size==2){
            //A4纸张是高大于宽
            if(imgHeight<imgWidth){
                tmp=imgHeight;
                imgHeight=imgWidth;
                imgWidth=tmp;
            }
        }else{
            //A3纸张是宽大于高
            if(imgHeight>imgWidth){
                tmp=imgHeight;
                imgHeight=imgWidth;
                imgWidth=tmp;
            }
        }
        //将四点位进行转化
        MatOfPoint cornerMatOfPoint = new MatOfPoint(p0, p1, p2, p3);
        MatOfPoint2f cornerMatOfPoint2f = new MatOfPoint2f();
        cornerMatOfPoint.convertTo(cornerMatOfPoint2f,CvType.CV_32FC1);
        //计算四点位矫正后对应的点位信息 1.计算页边距 2.计算点位
        double leftMargins = (src.width() - imgWidth) / 2;
        double topMargins = (src.height() - imgHeight) / 2;
        //传入的点位顺序是从左下角开始，顺时针
        Point desPoint1 = new Point(leftMargins,topMargins+imgHeight);
        Point desPoint2 = new Point(leftMargins,topMargins);
        Point desPoint3 = new Point(leftMargins+imgWidth,topMargins);
        Point desPoint4 = new Point(leftMargins+imgWidth,topMargins+imgHeight);
        desList.add(desPoint1);
        desList.add(desPoint2);
        desList.add(desPoint3);
        desList.add(desPoint4);
        MatOfPoint desMatOfPoint = new MatOfPoint(desPoint1,desPoint2,desPoint3,desPoint4);
        MatOfPoint2f desMatOfPoint2f = new MatOfPoint2f();
        desMatOfPoint.convertTo(desMatOfPoint2f,CvType.CV_32FC1);
        Mat dst = new Mat(src.rows(), src.cols(), src.type());
        // 提取图像
        Mat transMtx = Imgproc.getPerspectiveTransform(cornerMatOfPoint2f, desMatOfPoint2f);
        Imgproc.warpPerspective(src, dst, transMtx, dst.size(),Imgproc.INTER_LINEAR, 0,
                new Scalar(255, 255, 255));

        //-------------------根据四点截取图像-----end----------
        Imgcodecs.imwrite(desImageUrl,dst);
    }

    /**
     * @Description 获取定位点
     * @param answersheetImg 图片路径
     * @param coordinateInfoDTO 坐标数据抽象对象
     * @param fourCood 答题卡四个角落位置信息(左上1，左下3，右上2，右下4)
     * @throws
     * @return org.opencv.core.Mat
     * @Author menshaojing
     * @Date  2021/6/28  14:32
     **/
    public Rect getLocationPoint(String answersheetImg, CoordinateInfoDTO coordinateInfoDTO, int fourCood){
        //匹配的矩形集合
        Mat dst = Imgcodecs.imread(answersheetImg);
        //灰色图像
        Mat grayImage = new Mat();
        Imgproc.cvtColor(dst, grayImage, Imgproc.COLOR_RGB2GRAY);
        //大津法找到敏感区域
        Imgproc.threshold(grayImage, grayImage, 100, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU);
        //形态学变化，确保目标区别都是连在一起的
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
        Imgproc.morphologyEx(grayImage,grayImage,Imgproc.MORPH_OPEN,kernel);
        Imgproc.dilate(grayImage, grayImage, kernel, new Point(-1, -1), 2, 1, new Scalar(1));
        Imgproc.erode(grayImage, grayImage, kernel, new Point(-1, -1), 2, 1, new Scalar(1));
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(grayImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat contourImg = dst.clone();
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
            if (approx.rows() == 4 && Imgproc.isContourConvex(approxf1)) {
                //进行比对
                Rect rect = Imgproc.boundingRect(contour);
               boolean flag = isRectIntersect(rect, new Rect(coordinateInfoDTO.getxCoord().intValue(),
                coordinateInfoDTO.getyCoord().intValue(),
                 coordinateInfoDTO.getWidth().intValue(),
                 coordinateInfoDTO.getHeight().intValue()));
                if(!flag){
                    continue;
                }
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

        if(!squares.isEmpty()){
            //中心点坐标记录
            MatOfPoint point=null;
            //答题卡四个角落位置信息(左上1，左下3，右上2，右下4)
            if(fourCood==1 || fourCood==3){
                //左上左下获取x最小对象
                 point = squares.stream().min(Comparator.comparing(matOfPoint -> {
                    MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
                    matOfPoint.convertTo(matOfPoint2f, CvType.CV_32F);
                    //获取中心点位
                    RotatedRect  rect= Imgproc.minAreaRect(matOfPoint2f);
                    // 获取矩形的四个顶点
                    Point[] rectPoint = new Point[4];
                    rect.points(rectPoint);
                    return rect.center.x;
                })).get();
            }else if(fourCood==2 || fourCood==4) {
                //右上右下获取x最大对象
                 point = squares.stream().max(Comparator.comparing(matOfPoint -> {
                    MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
                    matOfPoint.convertTo(matOfPoint2f, CvType.CV_32F);
                    //获取中心点位
                    RotatedRect  rect= Imgproc.minAreaRect(matOfPoint2f);
                    // 获取矩形的四个顶点
                    Point[] rectPoint = new Point[4];
                    rect.points(rectPoint);
                    return rect.center.x;
                })).get();
            }
            MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
            point.convertTo(matOfPoint2f, CvType.CV_32F);
            //获取中心点位
            RotatedRect  rotatedRect= Imgproc.minAreaRect(matOfPoint2f);
            // 获取矩形的四个顶点
            Point[] rectPoint = new Point[4];
            rotatedRect.points(rectPoint);
            List<Point> centerList = Arrays.asList(rectPoint);
            List<Double> listSum = sum(centerList);
            int sumMinIndex = listSum.indexOf(listSum.stream().min(Double::compareTo).get());
            //按顺序找到对应的坐标0123 分别是左上，右上，右下，左下
            Point point0 = centerList.get(sumMinIndex);
            Rect rect=new Rect(point0,rotatedRect.size);

            boolean flag= containRectangle(new Rect(coordinateInfoDTO.getxCoord().intValue(),
                    coordinateInfoDTO.getyCoord().intValue(),
                    coordinateInfoDTO.getWidth().intValue(),
                    coordinateInfoDTO.getHeight().intValue()),rect);
            if(!flag){
                return null;
            }
            return rect;
        }

        return null;
    }

   /**
    * @Description 获取矫正后定位点
    * @param answersheetImg 图片路径
    * @param maxArea 面积
    * @param minArea 面积
    * @throws
    * @return java.util.List<org.opencv.core.Rect>
    * @Author menshaojing
    * @Date  2021/7/6  13:37
    **/
  public List<Rect> getCorrectLocationPoint(String answersheetImg,double maxArea,double minArea){
        Mat dst = Imgcodecs.imread(answersheetImg);
        //灰色图像
        Mat grayImage = new Mat();
        Imgproc.cvtColor(dst, grayImage, Imgproc.COLOR_RGB2GRAY);
        //大津法找到敏感区域
        Imgproc.threshold(grayImage, grayImage, 100, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU);
        //形态学变化，确保目标区别都是连在一起的
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
        Imgproc.morphologyEx(grayImage,grayImage,Imgproc.MORPH_OPEN,kernel);
        Imgproc.dilate(grayImage, grayImage, kernel, new Point(-1, -1), 2, 1, new Scalar(1));
        Imgproc.erode(grayImage, grayImage, kernel, new Point(-1, -1), 2, 1, new Scalar(1));
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(grayImage, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
       Mat contourImg = dst.clone();
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
           if (approx.rows() == 4 && Imgproc.isContourConvex(approxf1)&& Math.abs(Imgproc.contourArea(approx))>minArea-500&&Math.abs(Imgproc.contourArea(approx))<maxArea+500) {
               //进行比对

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
       //获取中心点位
      List<Point> centerList=new ArrayList<>();
      Map<Point,Rect> centerBoundingRect=new HashMap<>(10);
       for (MatOfPoint matOfPoint:squares) {
           MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
           matOfPoint.convertTo(matOfPoint2f, CvType.CV_32F);
           //获取中心点位
           RotatedRect  rect= Imgproc.minAreaRect(matOfPoint2f);
           Rect boundingRect = Imgproc.boundingRect(matOfPoint);
           centerList.add(rect.center);
           centerBoundingRect.put(rect.center,boundingRect);

       }
       List<Double> listDiff = diff(centerList);
       List<Double> listSum = sum(centerList);
       int sumMinIndex = listSum.indexOf(listSum.stream().min(Double::compareTo).get());
       int sumMaxIndex = listSum.indexOf(listSum.stream().max(Double::compareTo).get());
       int diffMaxIndex = listDiff.indexOf(listDiff.stream().max(Double::compareTo).get());
       int diffMinIndex = listDiff.indexOf(listDiff.stream().min(Double::compareTo).get());
       //按顺序找到对应的坐标0123 分别是左上，右上，右下，左下
       Point point0 = centerList.get(sumMinIndex);
       Point point1 = centerList.get(diffMinIndex);
       Point point2 = centerList.get(sumMaxIndex);
       Point point3 = centerList.get(diffMaxIndex);
       return Arrays.asList(new Rect[]
               {centerBoundingRect.get(point0),
                centerBoundingRect.get(point1),
                centerBoundingRect.get(point2),
                centerBoundingRect.get(point3)});
    }
    /**
     * @Description 两矩形是否相交
     * @param a 矩形
     * @param b 目标矩形
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
        {   return true;}
        else{
            return false;}
    }
    /**
     * @Description 两矩形是否包含
     * @param a  矩形
     * @param b 目标矩形
     * @throws
     * @return boolean
     * @Author menshaojing
     * @Date  2021/7/7  8:51
     **/
  public boolean  containRectangle(Rect a, Rect b){
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
     * @Description 判断点p是否在p1p2p3p4的正方形内
     * @Date  2021/6/2  11:25
     * @param p1 矩形四角第一个点位
     * @param p2 矩形四角第二个点位
     * @param p3 矩形四角第三个点位
     * @param p4 矩形四角第四个点位
     * @param p  判断点位
     * @throws
     * @return boolean
     * @Author menshaojing
     * @Date  2021/6/2  11:25
     **/
    boolean isPointInMatrix( Point p1,  Point p2,  Point p3, Point p4,  Point p) {
        boolean isPointIn = getCross(p1, p2, p) * getCross(p3, p4, p) >= 0 && getCross(p2, p3, p) * getCross(p4, p1, p) >= 0;
        return isPointIn;
    }
    /**
     * @Description  计算 |p1 p2| X |p1 p|
     * @Date  2021/6/2  11:25
     * @param p1
     * @param p2
     * @param p
     * @throws
     * @return double
     * @Author menshaojing
     * @Date  2021/6/2  11:25
     **/
    public double getCross( Point p1, Point p2,  Point p) {
        return (p2.x - p1.x) * (p.y - p1.y) - (p.x - p1.x) * (p2.y - p1.y);
    }
    /**
     * 处理图片，设置图片DPI值
     * @param image
     * @param dpi dot per inch
     * @return
     * @throws IOException
     */
    public String process(BufferedImage image, int dpi) throws  IOException {
        String fileName = System.getProperty("user.dir") + File.separator + UUID.randomUUID().toString() + ".jpg";
        for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName("png"); iw.hasNext(); ) {
            ImageWriter writer = iw.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                continue;
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageOutputStream imageOutputStream = null;
            ImageOutputStream stream = null;
            try {
                setDPI(metadata, dpi);
                stream = ImageIO.createImageOutputStream(output);
                writer.setOutput(stream);
                writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
                File outputFile = new File(fileName);
                imageOutputStream = ImageIO.createImageOutputStream(outputFile);
                imageOutputStream.write(output.toByteArray());

            } finally {
                stream.close();
                imageOutputStream.close();
            }


        }
        return fileName;
    }



    /**
     * 设置图片的DPI值
     * @param metadata
     * @param dpi
     * @throws IIOInvalidTreeException
     * @author menshaojing
     * @date 2021年7月7日19:34:16
     * @return void
     */
    public void setDPI(IIOMetadata metadata, int dpi) throws IIOInvalidTreeException {
        // for PMG, it's dots per millimeter
        double dotsPerMilli = 1.0 * dpi / 10 / INCH_2_CM;
        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);
        metadata.mergeTree("javax_imageio_1.0", root);
    }
}
