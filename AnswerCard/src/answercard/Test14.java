package answercard;

import answercard.util.GeneralUtils;
import answercard.util.ImageOpencvUtils;
import answercard.util.ImageUtils;
import org.junit.Before;
import org.junit.Test;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.text.ParseException;
import java.util.*;

/**
 * @ClassName Test14
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/5/12 10:31
 * @Version 1.0
 */
public class Test14 {
    @Before
    public void init() {
        System.load("D:/work/AnswerCard/AnswerCard/libs/x64/opencv_java450.dll");
       // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    @Test
    public void testCut(){
        String imgPath = "D:/work/AnswerCard/AnswerCard/img/correct.jpg";
        String destPath = "D:/work/AnswerCard/AnswerCard/img/";

        Mat src = ImageOpencvUtils.matFactory(imgPath);

        src = ImageOpencvUtils.correct(src);

        GeneralUtils.saveImg(src , destPath + "correctm.jpg");

         }
        @Test
         public  void testImage(){
            String imgPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0524\\A40524_150-1-110.jpeg";
            Mat src = ImageOpencvUtils.matFactory(imgPath);
            ImageUtils imageUtils=new ImageUtils();
            imageUtils.answerCard(src);
         }
         @Test
         public void test3(){
             String imgPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0603\\1000530002A_1.jpg";
             String destPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0603\\1000530002A_2.jpg";
             Mat src = ImageOpencvUtils.matFactory(imgPath);
          /*   //����͹�������λ��
             double left=15*5.9;
             double top=124*5.9;
             double w = 180*5.9;
             double h =30*5.9;
             int colStart = (int) left;
             int colEnd = (int) (left + w);
             Mat range = src.colRange(colStart,colEnd);
             int rowStart = (int) top;
             int rowEnd = (int) (top + h);
             range=range.rowRange(rowStart,rowEnd);*/
            //  Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/range.jpg", range);
             ImageUtils imageUtils=new ImageUtils();
             //�͹���
            Mat current1 = imageUtils.RectSegmentation(src);
             Imgcodecs.imwrite(destPath, current1);
             /*int x_step =(int) (35*5.9);
             int y_step =(int) (30*5.9);

             for (int i = 0; i < 5; i++) {
                 Imgproc.line(range,new Point(x_step*i,0),new Point(x_step*i,range.rows()),new Scalar(255));
             }
             for (int i = 0; i <h/y_step; i++) {
                 Imgproc.line(range,new Point(0,i*y_step),new Point(range.cols(),i*y_step),new Scalar(255));

             }
             //�͹���С��
             *//*Map <Integer, Mat> map=*//*
             for (int i = 0; i < h/y_step; i++) {
                 for (int j = 0; j < 5; j++) {
                     int x =  x_step*(i+1);
                     int y = y_step*(j+1);

                 }
             }*/
           //  Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/current1.jpg", range);
             //��ȡ
          /*

             Mat grayImg=new Mat();
             Imgproc.cvtColor(range, grayImg, Imgproc.COLOR_RGB2GRAY);*/
             /*//����ҵ���������
             Imgproc.threshold(grayImg,grayImg, 0, 255,Imgproc.THRESH_OTSU );
             //ΪʲôҪת������Ϊ��ɫ�������ݵ�����,������Χ�ư�ɫ�����
             Imgproc.threshold(grayImg,grayImg, 0, 255,Imgproc.THRESH_BINARY_INV);*/
             //��̬ѧ�仯��ȷ��Ŀ������������һ���
           /*  Imgproc.morphologyEx(grayImg,grayImg,Imgproc.MORPH_OPEN,new Mat());
              Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/grayImg.jpg", grayImg);
             Imgproc.GaussianBlur(grayImg, grayImg, new Size(3, 3), 0, 0);

             //��ȡ�͹�������
             List<MatOfPoint> list = imageUtils.objectiveGuestionBlock(grayImg,range);

             //����
             if (list.size() <= 0) {
                 throw new RuntimeException("δ�ҵ�ͼ������");
             } else {
                 // ��contours���������򣬰�����˳��
                 list.sort(new Comparator<MatOfPoint>() {
                     @Override
                     public int compare(MatOfPoint o1, MatOfPoint o2) {
                         MatOfPoint2f mat1 = new MatOfPoint2f(o1.toArray());
                         RotatedRect rect1 = Imgproc.minAreaRect(mat1);
                         Rect r1 = rect1.boundingRect();

                         MatOfPoint2f mat2 = new MatOfPoint2f(o2.toArray());
                         RotatedRect rect2 = Imgproc.minAreaRect(mat2);
                         Rect r2 = rect2.boundingRect();

                         return (int) (r1.area() - r2.area());
                     }
                 });
             }*/

           /*  MatOfPoint matOfPoint = list.get(list.size() - 1);
             MatOfPoint2f approx = new MatOfPoint2f(matOfPoint.toArray());
             RotatedRect rect = Imgproc.minAreaRect(approx);
             Point[] rectPoint = new Point[4];
             rect.points(rectPoint);
             List<Point> centers = Arrays.asList(rectPoint);
             List<Double> listDiff = diff(Arrays.asList(rectPoint));
             List<Double> listSum = sum(Arrays.asList(rectPoint));
             int sumMinIndex = listSum.indexOf(listSum.stream().min(Double::compareTo).get());
             int sumMaxIndex = listSum.indexOf(listSum.stream().max(Double::compareTo).get());
             int diffMaxIndex = listDiff.indexOf(listDiff.stream().max(Double::compareTo).get());
             int diffMinIndex = listDiff.indexOf(listDiff.stream().min(Double::compareTo).get());
             //��˳���ҵ���Ӧ������0123 �ֱ������ϣ����ϣ����£�����
             Point point0 = centers.get(sumMinIndex);
             Point point1 = centers.get(diffMinIndex);
             Point point2 = centers.get(sumMaxIndex);
             Point point3 = centers.get(diffMaxIndex);
             List<Point> corner= Arrays.asList(new Point[]{point3,point0,point1,point2});
             Mat current = imageUtils.RectSegmentation(src, corner);
             Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/current.jpg", current);*/
         }
       @Test
      public void   test4() throws ParseException {
           String imgPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0524\\A40524_150-1-1.jpeg";
           String destPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0524\\A40524_150-1-11.jpeg";
           Mat src = ImageOpencvUtils.matFactory(imgPath);
           ImageUtils imageUtils=new ImageUtils();
           //�ĸ���λ��ȡ
           //�ײ� bottom-left top-left  top-right bottom-right
           List<Point> corners= Arrays.asList(new Point[]{new Point(140,854),new Point(140,794),new Point(287,749),new Point(287,854)});
           Mat current = imageUtils.RectSegmentation(src, corners);//250,1038  2356,132 2938,1369 717,2288
          ImageOpencvUtils.saveImg(current,destPath);

       }
    @Test
    public void   test41() throws ParseException {
        String imgPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0524\\0526003.jpg";
        String destPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0524\\A40524_150-1-110-01.jpeg";
        Mat src = Imgcodecs.imread(imgPath);
        ImageUtils imageUtils=new ImageUtils();
        List<MatOfPoint> list = imageUtils.answerCard(src);
       List<Point> centers=new ArrayList<>();
        //��ȡ�������ĵ�λ��Ϣ
        for (MatOfPoint matOfPoint: list) {
            MatOfPoint2f tmp = new MatOfPoint2f();
            matOfPoint.convertTo(tmp, CvType.CV_32F);
            //��ȡ���ĵ�λ
            RotatedRect rect = Imgproc.minAreaRect(tmp);
            // ��ȡ���ε��ĸ�����
            Point[] rectPoint = new Point[4];
            rect.points(rectPoint);
            centers.add(rect.center);
        }
        Mat grayImg = new Mat();
        Imgproc.cvtColor(src, grayImg, Imgproc.COLOR_RGB2GRAY);
        //����ҵ���������
        Imgproc.threshold(grayImg, grayImg, 100, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU);
        //��̬ѧ�仯��ȷ��Ŀ������������һ���
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
        Imgproc.morphologyEx(grayImg,grayImg,Imgproc.MORPH_OPEN,kernel);

        Imgproc.dilate(grayImg, grayImg, kernel, new Point(-1, -1), 2, 1, new Scalar(1));
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0524\\dilate.jpg", grayImg);
        Imgproc.erode(grayImg, grayImg, kernel, new Point(-1, -1), 2, 1, new Scalar(1));
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0524\\erode.jpg", grayImg);

       int x_step = grayImg.cols()/4;
       int y_step =grayImg.rows()/5;
        for (int i = 0; i < 4; i++) {
            Imgproc.line(grayImg,new Point(x_step*i,0),new Point(x_step*i,grayImg.rows()),new Scalar(255));
        }
        for (int i = 0; i < 5; i++) {
            Imgproc.line(grayImg,new Point(0,i*y_step),new Point(grayImg.cols(),i*y_step),new Scalar(255));

        }
        //���
        Map<Integer, String> resultMap=new HashMap<>();
        //��ʼ����Ŀ��
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
                System.out.println(i+"��"+j+"�У�"+"("+p1.x+","+p1.y+"),"+"("+p2.x+","+p2.y+"),"+"("+p3.x+","+p3.y+"),"+"("+p4.x+","+p4.y+"),");
                for (Point centerPoint:centers ) {

                   if(IsPointInMatrix(  p1,   p2,   p3,  p4,  centerPoint)){
                       //��
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
        ImageOpencvUtils.saveImg(grayImg,destPath);

    }
    // ���� |p1 p2| X |p1 p|
    public double GetCross( Point p1, Point p2,  Point p) {
        return (p2.x - p1.x) * (p.y - p1.y) - (p.x - p1.x) * (p2.y - p1.y);
    }
    //�жϵ�p�Ƿ���p1p2p3p4����������
    boolean IsPointInMatrix( Point p1,  Point p2,  Point p3, Point p4,  Point p) {
        boolean isPointIn = GetCross(p1, p2, p) * GetCross(p3, p4, p) >= 0 && GetCross(p2, p3, p) * GetCross(p4, p1, p) >= 0;
        return isPointIn;
    }
    @Test
       public void test5(){
        String destPath = "D:/work/AnswerCard/AnswerCard/img/zeros.jpg";
        Mat src = ImageOpencvUtils.matFactory(destPath);
       }
       /**
        * @Description ������ԭ
        * @Date  2021/5/20  11:27
        * @param
        * @throws
        * @return void
        * @Author menshaojing
        * @Date  2021/5/20  11:27
        **/
       @Test
       public void test6(){
           String imgPath = "D:\\image\\test\\6-1.jpg";
           String destPath = "D:\\image\\test\\6-1-1-3.jpg";
           Mat src = ImageOpencvUtils.matFactory(imgPath);
           ImageUtils imageUtils=new ImageUtils();
           //�ĸ���λ��ȡ
           List<Point> corners= imageUtils.fourPoints(src);
           //�ײ� bottom-left top-left  top-right bottom-right
          // corners=Arrays.asList(new Point[]{new Point(0,1752),new Point(0,0),new Point(861,0),new Point(861,1752)});
          // corners=Arrays.asList(new Point[]{new Point(861,1752),new Point(861,0),new Point(1616,0),new Point(1616,1752)});
          //corners=Arrays.asList(new Point[]{new Point(1616,1752),new Point(1616,0),new Point(2477,0),new Point(2477,1752)});
           Mat current = src.submat(new Rect(new Point(873, 118), new Size(732, 77)));
        //  corners=Arrays.asList(new Point[]{new Point(65,1637),new Point(65,1148),new Point(797,1148),new Point(797,1637)});
          // Mat current= imageUtils. RectSegmentation(src,corners);
           ImageOpencvUtils.saveImg(current,destPath);
          // Mat  A4Mat=ImageOpencvUtils.restoreImage(src,current);
          // ImageOpencvUtils.saveImg(A4Mat,destPath);
       }
       @Test
      public void test7(){
           //-------------------ͼ��Ԥ����-------start------------
            Mat grayImg = new Mat();
            Mat tmpImg = new Mat();
            Mat tmpImg2 = new Mat();
           String imgPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\2.jpg";
           String destPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\2_1.jpg";
           Mat src = Imgcodecs.imread(imgPath);
           Imgproc.resize(src, tmpImg, new Size(src.width(), src.height()), 0, 0, 1);
           tmpImg2 = tmpImg.clone();
           Imgproc.cvtColor(tmpImg2, grayImg, Imgproc.COLOR_RGB2GRAY);
           //����ҵ���������
           Imgproc.threshold(grayImg,grayImg, 0, 255,Imgproc.THRESH_OTSU );
           //ΪʲôҪת������Ϊ��ɫ�������ݵ�����,������Χ�ư�ɫ�����
           Imgproc.threshold(grayImg,grayImg, 0, 255,Imgproc.THRESH_BINARY_INV);
           //��̬ѧ�仯��ȷ��Ŀ������������һ���
           Imgproc.morphologyEx(grayImg,grayImg,Imgproc.MORPH_OPEN,new Mat());
           // Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/grayImg.jpg", grayImg);
           Imgproc.GaussianBlur(grayImg, grayImg, new Size(3, 3), 0, 0);
           //Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/GaussianBlur.jpg", grayImg);
           //-------------------ͼ��Ԥ����-------end------------


           //-------------------��ȡ�ĵ�����-----start----------
           List<Point> list=new ArrayList<>();
               Mat cannyImg = new Mat();
               int threshLow =35;
               Imgproc.Canny(grayImg, cannyImg, threshLow, 3 * threshLow, 3);
              // Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cannyImg.jpg", cannyImg);
               Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
               Imgproc.dilate(cannyImg, cannyImg, kernel, new Point(-1, -1), 3, 1, new Scalar(1));
               Imgproc.erode(cannyImg, cannyImg, kernel, new Point(-1, -1), 3, 1, new Scalar(1));
              // Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/dilate.jpg", cannyImg);
              // Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/erode.jpg", cannyImg);
               List<MatOfPoint> contours = new ArrayList<>();
               Mat hierarchy = new Mat();
               Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
               Mat contourImg = tmpImg.clone();
              // Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cannyImg.jpg", cannyImg);
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
                   //300�ֱ��� ��λ������� 3347.42
                   //96�ֱ��� ��λ�������    350
                   // 200 �ֱ��� ��λ������� 1490.27
                   // 150 �ֱ��� ��λ������� 840
                   // 100 �ֱ��� ��λ������� 384
                   // 120 �ֱ��� ��λ������� 532
                   if (approx.rows() == 4 && Math.abs(Imgproc.contourArea(approx)) > 600 && Math.abs(Imgproc.contourArea(approx)) <840&&
                           Imgproc.isContourConvex(approxf1)) {
                       double maxCosine = 0;
                       for (int j = 2; j < 5; j++) {
                           double cosine = Math.abs(getAngle(approxf1.toArray()[j % 4], approxf1.toArray()[j - 2], approxf1.toArray()[j - 1]));
                           maxCosine = Math.max(maxCosine, cosine);
                       }
                       // �Ƕȴ��72��
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
               //���ĵ������¼
               List<Point> centers=new ArrayList<>();
               Map<Point,Point []> pointMap=new HashMap<>();
               //��ȡ�������ĵ�λ��Ϣ
               for (MatOfPoint matOfPoint: squares) {
                   MatOfPoint2f tmp = new MatOfPoint2f();
                   matOfPoint.convertTo(tmp, CvType.CV_32F);
                   //��ȡ���ĵ�λ
                   RotatedRect rect = Imgproc.minAreaRect(tmp);
                   // ��ȡ���ε��ĸ�����
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
               //��˳���ҵ���Ӧ������0123 �ֱ������ϣ����ϣ����£�����
               Point point0 = centers.get(sumMinIndex);
               Point point1 = centers.get(diffMinIndex);
               Point point2 = centers.get(sumMaxIndex);
               Point point3 = centers.get(diffMaxIndex);
               //�����������
               //����
               Point[] rectPoint0 = pointMap.get(point0);
               List<Double> rectPointSum = sum(Arrays.asList(rectPoint0));
               int  rectPointSumMinIndex = rectPointSum.indexOf(rectPointSum.stream().min(Double::compareTo).get());
               Point p00 = rectPoint0[rectPointSumMinIndex];
               //����
               Point[] rectPoint1 = pointMap.get(point1);
               List<Double> rectPointDiff = diff(Arrays.asList(rectPoint1));
               int rectPointDiffMinIndex = rectPointDiff.indexOf(rectPointDiff.stream().min(Double::compareTo).get());
               Point p11 = rectPoint1[rectPointDiffMinIndex];
               //����
               Point[] rectPoint2 = pointMap.get(point2);
               List<Double> rectPointSum1 = sum(Arrays.asList(rectPoint2));
               int rectPointSumMaxIndex = rectPointSum1.indexOf(rectPointSum1.stream().max(Double::compareTo).get());
               Point p22 = rectPoint2[rectPointSumMaxIndex];
               //����
               Point[] rectPoint3 = pointMap.get(point3);
               List<Double> rectPointDiff1 = diff(Arrays.asList(rectPoint3));
               int rectPointDiffMaxIndex = rectPointDiff1.indexOf(rectPointDiff1.stream().max(Double::compareTo).get());
               Point p33= rectPoint3[rectPointDiffMaxIndex];
               list.add(p33);
               list.add(p00);
               list.add(p11);
               list.add(p22);
           System.out.println(isParallelogram(list,3));
           //-------------------��ȡ�ĵ�����-----end----------

          //-------------------�����ĵ��ȡͼ��-----start----------
           // ����Ŀ��ͼ��ĳߴ�
           Point p0 = list.get(0);
           Point p1 = list.get(1);
           Point p2 = list.get(2);
           Point p3 = list.get(3);
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

           Mat quad = Mat.zeros((int) imgHeight , (int) imgWidth , CvType.CV_8UC3);

           MatOfPoint2f quadMat = new MatOfPoint2f(
                   new Point(0, quad.rows()),
                   new Point(0, 0),
                   new Point(quad.cols(), 0),
                   new Point(quad.cols(), quad.rows()));

           // ��ȡͼ��
           Mat transmtx = Imgproc.getPerspectiveTransform(cornerMat, quadMat);
           Imgproc.warpPerspective(tmpImg, quad, transmtx, quad.size());

           //-------------------�����ĵ��ȡͼ��-----end----------

           //-------------------��ԭͼ��-----------start--------
           //A4���سߴ� 96 �ֱ���  ���ؿ�ߣ�794 1123
           // 300�ֱ��� ���ؿ�ߣ� 2480 3508
           // 200�ֱ��� ���ؿ�ߣ� 1652 2338
           // 150�ֱ��� ���ؿ�ߣ� 1240 1754
           // 100�ֱ��� ���ؿ�ߣ� 827  1098
           // 120�ֱ��� ���ؿ�ߣ� 992  1403

           Mat mat = new Mat(new Size(2480,1754), quad.type(), new Scalar(255,255,255));
           //����ʵ���������ĳߴ�  W : ԭͼ��ߴ��  - ������߾�*2   H��ԭͼ��ߴ��  - �����ϱ߾�*2
           //��λ������ϱ߾� ������8mm
             final int LOCATION_POINT_TOP_MARGIN=8;
           //��λ������ϱ߾� ���� 9mm
             final int LOCATION_POINT_LEFT_MARGIN=9;
           //300 �ֱ��� ���رȣ� 11.8
           // 200 �ֱ��� ���رȣ�7.88
           // 150 �ֱ��� ���رȣ�5.90
           // 100 �ֱ��� ���رȣ�3.97
           // 120 �ֱ��� ���رȣ�4.72
            double pixel = 5.90;
           double width =mat.width() - LOCATION_POINT_LEFT_MARGIN * pixel * 2;
           double height = mat.height()-LOCATION_POINT_TOP_MARGIN * pixel * 2;
           Mat currentMat = new Mat(new Size(width, height), quad.type());
           Imgproc.resize(quad, currentMat, currentMat.size(), 0, 0, Imgproc.INTER_CUBIC);
           int left =(int) (LOCATION_POINT_LEFT_MARGIN * pixel );
           int top =(int) (LOCATION_POINT_TOP_MARGIN * pixel );
           Mat range=mat.colRange(left,currentMat.cols() +left);
           range=range.rowRange(top,currentMat.rows()+top);
           currentMat.copyTo(range);
           Imgcodecs.imwrite(destPath, mat);
           //-------------------��ԭͼ��-----------end--------

       }


       //���ݶԽ��߳���ֵ���Ƿ���ڶ�λ��ȱʧ
    private static boolean isParallelogram( List<Point> list,int size){
           //�Խ��߷�Χֵ
         double []  diagonal=(size==2? new double[]{1900.00, 2050.00} :new double[]{2800,2910.00});
        double p0p2 = getSpacePointToPoint(list.get(1), list.get(3));
        double p1p3 = getSpacePointToPoint(list.get(2), list.get(0));
        //ƽ��ֵ
        double average = (p0p2 + p1p3)/2;
        if ((diagonal[0]<average) && (diagonal[1]>average)){
           return  true;
       }
       return false;
    }
    // �㵽��ľ���
    private static double getSpacePointToPoint(Point p1, Point p2) {
        double a = p1.x - p2.x;
        double b = p1.y - p2.y;
        return Math.sqrt(a * a + b * b);
    }

    // ��������������м��Ǹ���ļн�   pt1 pt0 pt2
    private static double getAngle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);

    }
    /**
     * @Description ����������Ԫ�صļ���
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
     * @Description ����������Ԫ�صļӷ�
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


    @Test
    public void test1234(){
        int cases;
        int x01=1 , x02=4 , y01=4 , y02=4;
        int x11=2 , x12=3 , y11=3 , y12=3;


            /*scanf("%d %d %d %d" ,&x01 ,&y01 ,&x02 , &y02);
            scanf("%d %d %d %d" ,&x11 , &y11 , &x12 , &y12);*/
            int zx = ab(x01+x02-x11-x12); //��������������x���ϵľ��������
            int x = ab(x01-x02)+ab(x11-x12); //��������x����ı߳��ĺ�
            int zy = ab(y01+y02-y11-y12); //������y���Ͼ��������
            int y = ab(y01-y02)+ab(y11-y12); //y����߳��ĺ�
            if(zx <= x && zy <= y)
                System.out.println("YES\n");
            else
                System.out.println("NO\n");

    }
    int ab(int n)
    {
        if(n >= 0)
            return n;
        else
            return -n;
    }
}
