package answercard;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.opencv.imgproc.Imgproc.MORPH_RECT;

/**
 * @ClassName Test23
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/8/17 14:39
 * @Version 1.0
 */
public class Test23 {
    @Before
    public void init() {
        System.load("D:/work/AnswerCard/AnswerCard/libs/x64/opencv_java450.dll");
        // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    @Test
    public void test(){
        Mat imgIn = Imgcodecs.imread("D:\\image\\card\\1.jpg");
        Mat grayImg = new Mat();
        Imgproc.cvtColor(imgIn, grayImg, Imgproc.COLOR_RGB2GRAY);
        //����ҵ���������
        Imgproc.threshold(grayImg, grayImg, 0, 255, Imgproc.THRESH_OTSU);
        //ΪʲôҪת������Ϊ��ɫ�������ݵ�����,������Χ�ư�ɫ�����
        Imgproc.threshold(grayImg, grayImg, 0, 255, Imgproc.THRESH_BINARY_INV);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0901\\threshold2.jpg", grayImg);
        Mat kernel =Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(1, 1));
        Imgproc.dilate(grayImg, grayImg, kernel, new Point(0, 0), 20, 1, new Scalar(1));
        Imgproc.erode(grayImg, grayImg, kernel, new Point(0, 0), 1, 1, new Scalar(1));
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0901\\dilate.jpg", grayImg);

        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(grayImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        // �ҳ�������Ӧ͹�����ı������
        List<MatOfPoint> squares = new ArrayList<>();
        MatOfInt hull = new MatOfInt();
        MatOfPoint2f approx = new MatOfPoint2f();
        approx.convertTo(approx, CvType.CV_32F);
        for (MatOfPoint contour : contours) {
            Rect rect = Imgproc.boundingRect(contour);
            if(rect.height>100){
                    Imgproc.rectangle(imgIn,rect.br(),rect.tl(),new Scalar(0, 255, 0),1);
            }
            System.out.println("area:"+rect.area()+"   width:  "+rect.width+ "height :"+rect.height);
       /*     // �߿��͹��
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
            if (approx.rows() == 4 &&
                    Imgproc.isContourConvex(approxf1)) {
                double maxCosine = 0;
                for (int j = 2; j < 5; j++) {
                    double cosine = Math.abs(getAngle(approxf1.toArray()[j % 4], approxf1.toArray()[j - 2], approxf1.toArray()[j - 1]));
                    maxCosine = Math.max(maxCosine, cosine);
                }
                // �Ƕȴ��72��
                if (maxCosine <= 0.5) {
                    squares.add(approxf1);
                }
            }*/
        }

     /*   int maxIndex = findLargestSquare(squares);
        MatOfPoint matOfpoint = squares.get(maxIndex);
        Rect rect = Imgproc.boundingRect(matOfpoint);
        Mat subMat = imgIn.submat(rect);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0901\\subMat.jpg", subMat);
        Scalar color=new Scalar(0, 255, 0);
        Imgproc.drawContours(imgIn, contours, -1, color, 2);*/
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0901\\drawContours.jpg", imgIn);


    }
    // �ҵ���������������
    private  int findLargestSquare(List<MatOfPoint> squares) {
        if (squares.size() == 0)
        { return -1;
        }
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
    // ��������������м��Ǹ���ļн�   pt1 pt0 pt2
    private static double getAngle(Point pt1, Point pt2, Point pt0) {
        double dx1 = pt1.x - pt0.x;
        double dy1 = pt1.y - pt0.y;
        double dx2 = pt2.x - pt0.x;
        double dy2 = pt2.y - pt0.y;
        return (dx1 * dx2 + dy1 * dy2) / Math.sqrt((dx1 * dx1 + dy1 * dy1) * (dx2 * dx2 + dy2 * dy2) + 1e-10);

    }
    @Test
    public void test3(){
        String str="1.png";
        List<String> list=new ArrayList<>();
         Map<Integer, String> map=new HashMap<>();
         map.put(2,"2.png");
         map.put(1,"1.png");
         map.put(9,"9.png");
         map.put(6,"6.png");
        System.out.println(str.substring(0,str.lastIndexOf(".")));
      /*   list=map.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(Map.Entry::getValue).collect(Collectors.toList());
        System.out.println(list);*/
    }
}
