package answercard;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

import static org.opencv.imgproc.Imgproc.RETR_LIST;

/**
 * @ClassName Test9
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/7/7 17:08
 * @Version 1.0
 */
public class Test9 {
    @Before
    public void init(){
        System.load("D:\\learn\\orc\\src\\main\\java\\x64\\opencv_java450.dll");

    }
    @Test
    public void test6(){
        List<String> list=new ArrayList<>();
        list.add("a");
    }

    @Test
    public void test5(){
        Map<Integer, String> optionsMap=new HashMap<>(26);
        int index=26;
        //��ʼֵ
        int initialValue = 65;
        for (int i = 0; i < index; i++) {
            optionsMap.put(i+1,String.valueOf((char)(initialValue++)));
        }
        optionsMap.forEach((key,value)->{
          //  System.out.println("key:"+key+"    value:"+value);
            System.out.println(value.codePointAt(0));
        });
    }
    @Test
    public void test4(){
        candidateNumber(Imgcodecs.imread("D:\\image\\test1\\dst.png"));
    }

    /**
     * @Description ��ȡ׼��֤��
     * @param imgIn ͼƬ����
     * @throws
     * @return java.util.List<org.opencv.core.MatOfPoint>
     * @Author menshaojing
     * @Date  2021/6/30  20:19
     **/
    public List<MatOfPoint> candidateNumber(Mat imgIn) {
        Mat grayImage = new Mat();
        Imgproc.cvtColor(imgIn, grayImage, Imgproc.COLOR_RGB2GRAY);
        //����ҵ���������
        Imgproc.threshold(grayImage, grayImage, 100, 255, Imgproc.THRESH_BINARY_INV+Imgproc.THRESH_OTSU);
        //��̬ѧ�仯��ȷ��Ŀ������������һ���
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(1, 1));
        Imgproc.morphologyEx(grayImage,grayImage,Imgproc.MORPH_OPEN,kernel);
        Imgproc.dilate(grayImage, grayImage, kernel, new Point(-1, -1), 1, 1, new Scalar(1));
        Imgproc.erode(grayImage, grayImage, kernel, new Point(-1, -1), 1, 1, new Scalar(1));
        Imgcodecs.imwrite("D:\\image\\card\\erode.png",grayImage);
        //����
        return candidateNumberParse(grayImage,imgIn);
    }
    /**
     * @Description ����׼��֤��
     * @param imgIn ͼƬ����
     * @throws
     * @return java.util.List<org.opencv.core.MatOfPoint>
     * @Author menshaojing
     * @Date  2021/6/30  20:20
     **/
    List<MatOfPoint> candidateNumberParse(Mat imgIn,Mat src) {
        //ѡ�����23 ��12  6mm*3mm ����150�ֱ���������Ϊ   35px*18px=630
        //Ϊ�����ʶ���� ȡ�������֮һ 35/3=12 �ߵ�����֮һ 18/3=6  ���������֮һ 630/3=210
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(imgIn, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_L1);
        for (MatOfPoint matOfPoint: contours) {
            Rect rect = Imgproc.boundingRect(matOfPoint);
            Imgproc.rectangle(src,rect.br(),rect.tl(),new Scalar(0, 0, 205));
            if(rect.width>12 && rect.height>6 && rect.area()> 210){

            }


        }
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\squares.png",src);
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
            if (Math.abs(Imgproc.contourArea(approx)) >80 && Math.abs(Imgproc.contourArea(approx)) <1000) {
                MatOfPoint tmp = new MatOfPoint();
                contourHull.convertTo(tmp, CvType.CV_32S);
                squares.add(approxf1);
                hulls.add(tmp);
            }
        }
        Imgproc.drawContours(src,squares,2,new Scalar(0,255,0));
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\drawContours.jpg",src);
        return  squares;
    }

    @Test
    public void test(){
        examNumberRecognition("D:\\image\\test1\\1.jpg", new CoordinateInfoDTO());
    }
    @Test
    public void test1(){
        examNumberRecognition("D:\\image\\test1\\1.jpg", new CoordinateInfoDTO());
    }
    @Test
    public void test2(){
        templateMatching("D:\\image\\test1\\0.jpg","D:\\image\\test1\\A3.jpg");
    }

    //ģ��ƥ��
   public void templateMatching(String templateUrl,String imageUrl){
       Mat dst = Imgcodecs.imread(imageUrl);
       Mat templateMat = Imgcodecs.imread(templateUrl);
       Mat result=new Mat();
       /*int intMatchingMethod;
       switch (this.matchingMethod) {
           case MM_CORELLATION_COEFF:
               intMatchingMethod = Imgproc.TM_CCOEFF_NORMED;
               break;
           case MM_CROSS_CORELLATION:
               intMatchingMethod = Imgproc.TM_CCORR_NORMED;
               break;
           default:
               intMatchingMethod = Imgproc.TM_SQDIFF_NORMED;
       }*/
       Imgproc.matchTemplate(dst,templateMat,result,Imgproc.TM_SQDIFF_NORMED);
       Core.MinMaxLocResult minMaxLocRes = Core.minMaxLoc(result);

       double accuracy = 0;
       Point location = null;
       accuracy = minMaxLocRes.maxVal;
       location = minMaxLocRes.maxLoc;
/*
       if (this.matchingMethod == MatchingMethod.MM_SQUARE_DIFFERENCE) {
           accuracy = 1 - minMaxLocRes.minVal;
           location = minMaxLocRes.minLoc;
       } else {

       }*/

      /* if (accuracy < desiredAccuracy) {
           throw new ImageNotFoundException(
                   String.format(
                           "Failed to find template image in the source image. The accuracy was %.2f and the desired accuracy was %.2f",
                           accuracy,
                           desiredAccuracy),
                   new Rectangle((int) location.x, (int) location.y, templateMat.width(), templateMat.height()),
                   accuracy);
       }*/

      /* if (!minMaxLocResultIsValid(minMaxLocRes)) {
           throw new ImageNotFoundException(
                   "Image find result (MinMaxLocResult) was invalid. This usually happens when the source image is covered in one solid color.",
                   null,
                   null);
       }*/

       Rect foundRect = new Rect(
               (int) location.x,
               (int) location.y,
               templateMat.width(),
               templateMat.height());
       System.out.println(foundRect);

       Imgproc.rectangle(dst,foundRect,new Scalar(0, 255, 0));
       Imgcodecs.imwrite("D:\\image\\test1\\template.jpg",dst);

    }
    //����ʶ��
    public List<Rect> examNumberRecognition(String answersheetImg, CoordinateInfoDTO coordinateInfoDTO){
        Mat dst = Imgcodecs.imread(answersheetImg);
        Mat grayImg = new Mat();
        Imgproc.cvtColor(dst, grayImg, Imgproc.COLOR_RGB2GRAY);
        //����ҵ���������
        double threshold1 = Imgproc.threshold(grayImg, grayImg, 0, 255, Imgproc.THRESH_OTSU);
        //ΪʲôҪת������Ϊ��ɫ�������ݵ�����,������Χ�ư�ɫ�����
        double threshold2 =Imgproc.threshold(grayImg,grayImg, 0, 255,Imgproc.THRESH_BINARY_INV);
        //��̬ѧ�仯��ȷ��Ŀ������������һ���
        Mat element = Imgproc.getStructuringElement(RETR_LIST, new Size(3, 3));
        Imgproc.morphologyEx(grayImg,grayImg,Imgproc.MORPH_OPEN,element);
        int ksize =3;
        double sigma = 0.3 * ((ksize - 1) * 0.5 - 1) + 0.8;
        Mat gaussianKernel = Imgproc.getGaussianKernel(ksize, sigma);

        Imgproc.GaussianBlur(grayImg, grayImg, gaussianKernel.size(), 0.25, 0.25);
        Mat cannyImg = new Mat();
        Imgproc.Canny(grayImg, cannyImg, 30, 30*3, 7);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
        Imgproc.dilate(cannyImg, cannyImg, kernel, new Point(-1, -1), 3, 1, new Scalar(1));
        Imgproc.erode(cannyImg, cannyImg, kernel, new Point(-1, -1), 3, 1, new Scalar(1));
        Imgcodecs.imwrite("D:\\image\\test1\\erode.jpg",cannyImg);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat contourImg = dst.clone();
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
            System.out.println(Math.abs(Imgproc.contourArea(approx)));
            if (approx.rows() == 4 && Imgproc.isContourConvex(approxf1)) {
                //���бȶ�

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
        //��ȡ���ĵ�λ
        Imgcodecs.imwrite("D:\\image\\test1\\binaryMat.jpg",contourImg);
        return null;
    }
    /**
     * @Description  ��������������м��Ǹ���ļн�   pt1 pt0 pt2
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
}
