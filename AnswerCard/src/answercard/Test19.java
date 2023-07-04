package answercard;

import org.apache.commons.imaging.ImageInfo;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.junit.Before;
import org.junit.Test;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName Test19
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/6/2 17:12
 * @Version 1.0
 */
public class Test19 {


    @Before
    public void init() {
        System.load("D:/work/AnswerCard/AnswerCard/libs/x64/opencv_java450.dll");
        // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
  @Test
  public void test0(){
      Test19 test19=new Test19();
      //����
     // MatOfPoint point1 = test19.getLocationPoint("D:\\image\\test\\A.png", new CoordinateInfoDTO(129f, 80f, 108.00, 100.00));
      //����
     // MatOfPoint point2 = test19.getLocationPoint("D:\\image\\test\\A.png", new CoordinateInfoDTO(2269f, 50f, 126.00, 172.00));
      //����
    // MatOfPoint point3 = test19.getLocationPoint("D:\\image\\test\\A.png", new CoordinateInfoDTO(77f, 3338f, 161.00, 91.00));
      //����
      MatOfPoint point4= test19.getLocationPoint("D:\\image\\test\\A.png", new CoordinateInfoDTO(2295f, 3333f, 150.00, 78.00));

  }
    @Test
    public void test1(){
        try {   //ʶ�������δ���ѡ�� ����ѡ�� ��λ fence
       // squareAnswer("D:\\image\\test\\A.png",new CoordinateInfoDTO(206f, 805f, 376.00, 129.00),1,2,4,2)  ;
        //squareAnswer("D:\\image\\test\\A3.jpg",new CoordinateInfoDTO(100f, 619f, 196.00, 155.00),1,5,4,3)  ;
        squareAnswer("D:\\image\\test\\A3A150.jpg",new CoordinateInfoDTO(291f, 1343f, 302.00, 145.00),1,4,4,3)  ;
        //{
        //  "height": 154,
        //  "width": 183,
        //  "xcoord": 106,
        //  "ycoord": 676
        //}
        //squareAnswer("D:\\image\\test\\A3zhixue.jpg",new CoordinateInfoDTO(106f, 676f, 183.00, 154.00),1,5,4,3)  ;
        //{
        //  "height": 156,
        //  "width": 192,
        //  "xcoord": 102,
        //  "ycoord": 746
        //}

          //  squareAnswer("D:\\image\\test\\1.jpg",new CoordinateInfoDTO(102f, 746f, 192.00, 156.00),1,5,4,2)  ;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ImageReadException e) {
            e.printStackTrace();
        }
    }
/**
 * @Description //TODO
    * @param answersheetImg ͼƬ·��
 * @param coordinateInfoDTO �������
 * @param arrangement �ݺ��ʶ 1: ���� 2.����
 * @param col ����ѡ�����
 * @param row ��Ŀ����
 * @param size ֽ�Ŵ�С 2 A4
 * @throws
 * @return void
 * @Author menshaojing
 * @Date  2021/6/30  8:43
 **/
  public void   squareAnswer(String answersheetImg, CoordinateInfoDTO coordinateInfoDTO, int arrangement,int row,int col,int size) throws IOException, ImageReadException {
      ImageInfo imageInfo = Imaging.getImageInfo(new File(answersheetImg));
      System.out.println("dpiΪ"+imageInfo.getPhysicalHeightDpi());
      int dpi = imageInfo.getPhysicalHeightDpi();
      int iterations=1;
      List<Integer> dpis = Arrays.asList(96, 150, 300);
      //dpi��96-150֮��
      if(dpi>dpis.get(0) && dpi<=dpis.get(1)){
           iterations=size==2?10:0;
      //dpi��150��300֮��
      }else if(dpi>dpis.get(1) && dpi<=dpis.get(2)){
           iterations=size==2?10:5;
      }

      Map<Double,MatOfPoint> map = new HashMap(8);
      Mat imread = Imgcodecs.imread(answersheetImg);
      //��ɫͼ��
      Mat gray=new Mat();
      Imgproc.cvtColor(imread, gray, Imgproc.COLOR_RGB2GRAY);
      //Imgcodecs.imwrite("D:\\image\\test\\gray.png",gray);
      //����ҵ���������
      Imgproc.threshold(gray,gray, 0, 255,Imgproc.THRESH_OTSU );
      //ΪʲôҪת������Ϊ��ɫ�������ݵ�����,������Χ�ư�ɫ�����
      Imgproc.threshold(gray,gray, 0, 255,Imgproc.THRESH_BINARY_INV);
      Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0, 0);
       Imgcodecs.imwrite("D:\\image\\test\\GaussianBlur.png",gray);
      Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
      Imgproc.morphologyEx(gray,gray,Imgproc.MORPH_OPEN,kernel);
      Imgproc.Canny(gray,gray,30,60,7,false);
      Imgcodecs.imwrite("D:\\image\\test\\Canny.jpg",gray);
      Imgproc.dilate(gray, gray, kernel, new Point(-1, -1), iterations, 1, new Scalar(1));
      Imgcodecs.imwrite("D:\\image\\test\\dilate.jpg",gray);

      Imgproc.erode(gray, gray, kernel, new Point(-1, -1), iterations, 1, new Scalar(1));
      Imgcodecs.imwrite("D:\\image\\test\\erode.jpg",gray);
      List<MatOfPoint> contours = new ArrayList<>();
      Mat hierarchy = new Mat();
      Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
      Mat contourImg = imread.clone();
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
              //���бȶ�
              Rect rect = Imgproc.boundingRect(contour);
              boolean flag = isRectIntersect(rect, new Rect(coordinateInfoDTO.getxCoord().intValue(), coordinateInfoDTO.getyCoord().intValue(),
                                            coordinateInfoDTO.getWidth().intValue(), coordinateInfoDTO.getHeight().intValue()));
              if(!flag){
                  continue;
              }
              double maxCosine = 0;
              for (int j = 2; j < 5; j++) {
                  double cosine = Math.abs(getAngle(approxf1.toArray()[j % 4], approxf1.toArray()[j - 2], approxf1.toArray()[j - 1]));
                  maxCosine = Math.max(maxCosine, cosine);
              }
              // �Ƕȴ��72��
              if (maxCosine <= 0.5) {
                  MatOfPoint tmp = new MatOfPoint();
                  contourHull.convertTo(tmp, CvType.CV_32S);
                  map.put(Math.abs(Imgproc.contourArea(approx)),approxf1);
                  squares.add(approxf1);
                  hulls.add(tmp);
              }
          }
      }
      System.out.println(squares.size());
      //1.ȥ�����ڿ�ѡ���εľ���
     double marqueeRectangle=coordinateInfoDTO.getHeight()*coordinateInfoDTO.getWidth();
      map.forEach((aDouble, matOfPoint) -> {
          if(aDouble>marqueeRectangle){
              squares.remove(matOfPoint);
          }
      });
      //2.�ж�ѡ������Ƿ���ȷ���ܸ���
      int totalNumber = row * col;
      //3.�������ȣ�˵����ѡ�б���Ĵ���
     /* if(totalNumber<squares.size()){
          //�����ݹ��ѯɾ��
          removeSquare(squares,totalNumber,arrangement);
      }*/
      Scalar color=new Scalar(0, 255, 0);
      Imgproc.drawContours(contourImg, squares, -1, color, 2);
      Imgcodecs.imwrite("D:\\image\\test\\contourImg4.png",contourImg);



    }
     /**
      * @Description ��ȥ�����
      * @param squares ���ζ��󼯺�
      * @param totalNumber ѡ���ܸ���
      * @param arrangement ���ݱ�ʶ
      * @throws
      * @return void
      * @Author menshaojing
      * @Date  2021/6/30  9:29
      **/
       public   void removeSquare(List<MatOfPoint> squares,int totalNumber,int arrangement){
                      Map<Double,MatOfPoint> map=new HashMap<>();
                    if(squares.size()!=totalNumber){
                        //��ȡ��������
                        for (MatOfPoint matOfPoint: squares) {
                            MatOfPoint2f tmp = new MatOfPoint2f();
                            matOfPoint.convertTo(tmp, CvType.CV_32F);
                            //��ȡ���ĵ�λ
                            RotatedRect rect = Imgproc.minAreaRect(tmp);
                            map.put(arrangement==1?rect.center.x:rect.center.y,matOfPoint);
                        }
                        //ȥ��x��y����С�ľ��ζ���
                        List<Double> list = map.keySet().stream().sorted().collect(Collectors.toList());
                        squares.remove(map.get(list.get(0)));
                        removeSquare(squares,totalNumber,arrangement);
                    }

             }
    /**
     * @Description ��ȡ��λ��
     * @param answersheetImg ͼƬ·��
     * @param coordinateInfoDTO �������ݳ������
     * @throws
     * @return org.opencv.core.Mat
     * @Author menshaojing
     * @Date  2021/6/28  14:32
     **/
    public   MatOfPoint getLocationPoint(String answersheetImg, CoordinateInfoDTO coordinateInfoDTO){
        //ƥ��ľ��μ���
         Map<Double,MatOfPoint> map = new HashMap(1);
        Mat imread = Imgcodecs.imread(answersheetImg);
        //��ɫͼ��
        Mat gray=new Mat();
        Imgproc.cvtColor(imread, gray, Imgproc.COLOR_RGB2GRAY);
        //Imgcodecs.imwrite("D:\\image\\test\\gray.png",gray);
        //����ҵ���������
        Imgproc.threshold(gray,gray, 0, 255,Imgproc.THRESH_OTSU );
        //ΪʲôҪת������Ϊ��ɫ�������ݵ�����,������Χ�ư�ɫ�����
        Imgproc.threshold(gray,gray, 0, 255,Imgproc.THRESH_BINARY_INV);
        Imgproc.GaussianBlur(gray, gray, new Size(3, 3), 0, 0);
       // Imgcodecs.imwrite("D:\\image\\test\\GaussianBlur.png",gray);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(gray, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat contourImg = imread.clone();
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
            if (approx.rows() == 4 && //Math.abs(Imgproc.contourArea(approx)) > 600 && Math.abs(Imgproc.contourArea(approx)) <840&&
                    Imgproc.isContourConvex(approxf1)) {
                //���бȶ�
                Rect rect = Imgproc.boundingRect(contour);
                boolean flag = isRectIntersect(rect, new Rect(coordinateInfoDTO.getxCoord().intValue(), coordinateInfoDTO.getyCoord().intValue(), coordinateInfoDTO.getWidth().intValue(), coordinateInfoDTO.getHeight().intValue()));
                if(!flag){
                    continue;
                }
                double maxCosine = 0;
                for (int j = 2; j < 5; j++) {
                    double cosine = Math.abs(getAngle(approxf1.toArray()[j % 4], approxf1.toArray()[j - 2], approxf1.toArray()[j - 1]));
                    maxCosine = Math.max(maxCosine, cosine);
                }
                // �Ƕȴ��72��
                if (maxCosine <= 0.5) {
                    MatOfPoint tmp = new MatOfPoint();
                    contourHull.convertTo(tmp, CvType.CV_32S);
                    map.put(Math.abs(Imgproc.contourArea(approx)),approxf1);
                    squares.add(approxf1);
                    hulls.add(tmp);
                }
            }
        }
        Scalar color=new Scalar(0, 255, 0);
        Imgproc.drawContours(contourImg, squares, -1, color, 2);
        Imgcodecs.imwrite("D:\\image\\test\\contourImg3.png",contourImg);
        if(squares.size()>1){
            //���ĵ������¼
            List<Point> centers=new ArrayList<>();
            Map<Point,Point []> pointMap=new HashMap<>(4);
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
        }

        List<Double> mapList = map.keySet().stream().sorted().collect(Collectors.toList());
        return map.get(mapList.get(mapList.size()-1));
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
    /**
     * @Description �������Ƿ��ཻ
     * @param a ����
     * @param b Ŀ�����
     * @throws
     * @return boolean
     * @Author menshaojing
     * @Date  2021/6/28  14:13
     **/
    public  boolean isRectIntersect(Rect a,Rect b)
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
    // ��������������м��Ǹ���ļн�   pt1 pt0 pt2
    private double getAngle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);

    }
    @Test
    public void test(){
        String imgPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0601\\1000530001B.jpg";
        Mat src = Imgcodecs.imread(imgPath);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0601\\image_contrasted_1.jpg", imgCorrection(src));
    }
    public static Mat imgCorrection(Mat srcImage) {
        // ��ֵ��
        Mat binary = ImgBinarization(srcImage);
        // �����븯ʴ
        Mat preprocess = preprocess(binary);
        // ���Һ�ɸѡ��������
        List<RotatedRect> rects = findTextRegion(preprocess) ;
        //����ȡ���ľ��θ����������  �� �����������ظ���ȥ��
        Mat correction = correction(rects,srcImage);
        return correction;
    }
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
     * @author MaChao
     * @time 2019-9-29
     */
    public static Mat preprocess(Mat binary){
        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(20, 4));
        Mat element2 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
        Mat dilate1 = new Mat();
        Imgproc.dilate(binary, dilate1, element2);

        Mat erode1 = new Mat();
        Imgproc.erode(dilate1, erode1, element1);
        Mat dilate2 = new Mat();
        Imgproc.dilate(erode1, dilate2, element2);
        return dilate2;
    }

    /**
     * ��������
     * @author MaChao
     * @time 2019-12-3
     */
    public static List<RotatedRect> findTextRegion(Mat img)
    {
        List<RotatedRect> rects = new ArrayList<RotatedRect>();
        //1.��������
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(img, contours, hierarchy, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE, new Point(0, 0));
        int img_width = img.width();
        int img_height = img.height();
        int size = contours.size();
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
            rects.add(rect);
        }
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
     @Test
    public void test2(){
        //65-90
        // System.out.println(Integer.toString('A', 10));
        // System.out.println((char)65);
       Map<Integer, String> map = new HashMap(4);
       map.put(1,null);
       map.put(2,null);
       map.put(3,null);
         String asser = map.entrySet().stream().map(o -> o.getValue()).collect(Collectors.joining());
         System.out.println(asser);
     }
}
