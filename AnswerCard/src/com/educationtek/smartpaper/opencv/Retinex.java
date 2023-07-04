package com.educationtek.smartpaper.opencv;

import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName
 * @Description Retinex 算法
 * @Author menshaojing
 * @Date 2021/12/8 11:04
 * @Version 1.0
 */
public class Retinex {

    public static final String NATIVE_LIBRARY_RETINEX = getNativeLibraryName();

    private static String getNativeLibraryName() { return "dynamicRetinex"; }

   public static void main(String[] args) {

      System.loadLibrary(NATIVE_LIBRARY_RETINEX);
      System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
      List<Double> weight=new ArrayList<>() ;
      List<Double> sigmas=new ArrayList<>();
      for(int i = 0; i < 3; i++)
      {
         weight.add(0.3333343);
      }
      sigmas.add(30.0);
      sigmas.add(150.0);
      sigmas.add(300.0);

      int dynamic = 1;
      Mat image = Imgcodecs.imread("D:\\work\\AnswerCard\\AnswerCard\\img\\dst_873487520.png");
      Mat imageDst=new Mat();
      long start= System.currentTimeMillis();
      // imageDst=Retinex.msrcr_GIMP(image,weight,sigmas,dynamic);
      imageDst=Retinex.msrcr_GIMP(image,dynamic);
      candidateNumber(imageDst);
      long end= System.currentTimeMillis();
      System.out.println("消耗了"+((end-start)/ 1000)+"秒");
      Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\image_1.png",imageDst);
   }

   public static   Mat msrcr_GIMP(Mat src, List<Double> weight, List<Double>sigmas, int dynamic){
      double[] weightI=weight.stream().mapToDouble(Double::valueOf).toArray();
      double[] sigmasI=sigmas.stream().mapToDouble(Double::valueOf).toArray();
      return new Mat(dmsrcr_GIMP0(src.getNativeObjAddr(),weightI,sigmasI,dynamic));
   }
   public static   Mat msrcr_GIMP(Mat src, int dynamic){
      return new Mat(dmsrcr_GIMP1(src.getNativeObjAddr(),dynamic));
   }
   public static native long dmsrcr_GIMP0(long src,  double[] weight, double[] sigmas, int dynamic);
   public static native long dmsrcr_GIMP1(long src,int dynamic);
   public static native void dmsrcr_GIMP2(String imagePath,String desPath);


   /**
    * @Description 获取准考证号
    * @param imgIn 图片对象
    * @throws
    * @return java.util.List<org.opencv.core.MatOfPoint>
    * @Author menshaojing
    * @Date  2021/6/30  20:19
    **/
   public static List<Rect> candidateNumber(Mat imgIn) {
      Mat grayImage = new Mat();
      Imgproc.cvtColor(imgIn, grayImage, Imgproc.COLOR_RGB2GRAY);
      //形态学变化，确保目标区别都是连在一起的
      int ksize =2;
      //高斯模糊
      Imgproc.GaussianBlur(grayImage, grayImage, new Size(2 * ksize + 1, 2 * ksize + 1), 0, 0);
      //二值化
      int thresh =176;
      Imgproc.threshold(grayImage, grayImage, thresh, 255, Imgproc.THRESH_BINARY_INV);
      //开运算
      int size = 7;
      Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT,  new Size(2 * size + 1, 2 * size + 1));
      Imgproc.morphologyEx(grayImage,grayImage,Imgproc.MORPH_OPEN,kernel);
      //腐蚀
      size = 1;
      Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2 * size + 1, 2 * size + 1));
      Imgproc.erode(grayImage, grayImage, element);
      Imgproc.Canny(grayImage, grayImage,  0,  150);
      HighGui.imshow("grayImage",grayImage);
      HighGui.waitKey();
     //Imgcodecs.imwrite("D:\\image\\card\\11\\"+grayImage.nativeObj+".png",grayImage);
      //解析
      try {
         return candidateNumberParse(grayImage,imgIn);

      }finally {
         grayImage.release();
         kernel.release();
         element.release();
         for (MatOfPoint matOfPoint: contours) {
            matOfPoint.release();
         }
      }
   }
   static List<MatOfPoint>  contours = new ArrayList<>();
   /**
    * @Description 解析准考证号
    * @param imgIn 图片对象
    * @throws
    * @return java.util.List<org.opencv.core.MatOfPoint>
    * @Author menshaojing
    * @Date  2021/6/30  20:20
    **/
   static List<Rect> candidateNumberParse(Mat imgIn, Mat src) {
      //选择项宽23 高12  6mm*3mm 按照150分辨率则像素为   35px*18px=630
      //为了提高识别率 取宽的三分之一 35/3=12 高的三分之一 18/3=6  面积的三分之一 630/3=210  630/2 +630 =1150
      List<Rect> rectList=new ArrayList<>();

      Mat hierarchy = new Mat();

      Imgproc.findContours(imgIn, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_L1);
      //convexHull( contours, src);
      for (MatOfPoint matOfPoint: contours) {
         Rect rect = Imgproc.boundingRect(matOfPoint);

         if( rect.area()>=100 && rect.width>5 ){
            Imgproc.rectangle(src,rect.br(),rect.tl(),new Scalar(0, 0, 205));
            rectList.add(rect);
         }
      }
      // Imgcodecs.imwrite("D:\\image\\card\\11\\"+src.nativeObj+"1.png",src);
      //进行二次筛选，将获取的矩形大于两个的答案的区域再次识别
      List<Rect> secondRectList=new ArrayList<>();
      rectList.forEach(rect -> {
         if(rect.area()/630>=2){
            secondRectList.add(rect);
         }
      });
      for (Rect rect: secondRectList) {
         hierarchy = new Mat();
         contours = new ArrayList<>();
         //让截取的图像更全宽高增加3像素
         int x = (rect.x - 3)<0?0:(rect.x - 3);
         int y=(rect.y-3)<0?0:(rect.y-3);
         int width = (rect.width + 6+x)>src.width()?src.width()-x:rect.width + 6;
         int height=(rect.height+6+y)>src.height()?src.height()-y:rect.height+6;
         Rect maxRect = new Rect(x, y, width, height);
         Mat subMat = src.submat(maxRect);

         Imgproc.resize(subMat,subMat,new Size(subMat.width()*3,subMat.height()*3));
         // Mat clone = subMat.clone();
         // Imgcodecs.imwrite("D:\\image\\card\\11\\"+subMat.nativeObj+".png",subMat);
         Imgproc.cvtColor(subMat,subMat,Imgproc.COLOR_BGR2GRAY);
         Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(1, 3));
         Imgproc.morphologyEx(subMat,subMat,Imgproc.MORPH_OPEN,kernel);
         Imgproc.dilate(subMat, subMat, kernel, new Point(-1, -1), 1, 1, new Scalar(1));
         Imgproc.erode(subMat, subMat, kernel, new Point(-1, -1), 1, 1, new Scalar(1));
         Imgproc.Canny(subMat, subMat,  35,  35*3);
         //  Imgcodecs.imwrite("D:\\image\\card\\11\\"+subMat.nativeObj+"Canny.png",subMat);
         Imgproc.findContours(subMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_L1);
         for (MatOfPoint matOfPoint: contours) {
            Rect secondRect = Imgproc.boundingRect(matOfPoint);
            int w = secondRect.width /3;
            int h = secondRect.height / 3;
            int area = w * h;
            if(w>=12 && h>=6*3 && area>=210 && area<1150){
               secondRect.x=rect.x+ secondRect.x/3;
               secondRect.y=rect.y+ secondRect.y/3;
               secondRect.height=secondRect.height/3;
               secondRect.width=secondRect.width/3;
               rectList.add(secondRect);
            }
         }
         //  Imgcodecs.imwrite("D:\\image\\card\\11\\"+clone.nativeObj+".png",clone);
         rectList.remove(rect);
         subMat.release();
         kernel.release();
      }
       /* rectList.forEach(rect -> {
            Imgproc.rectangle(src,rect.br(),rect.tl(),new Scalar(0, 0, 205));
        });
        Imgcodecs.imwrite("D:\\image\\card\\11\\"+src.nativeObj+".png",src);*/

      try {
         return  rectList;
      }finally {
         hierarchy.release();
      }

   }
}
