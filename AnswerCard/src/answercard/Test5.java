package answercard;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Vector;

/**
 * @ClassName Test5
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/8/30 15:17
 * @Version 1.0
 */
public class Test5 {
    @Before
    public void init(){
        System.load("D:\\learn\\orc\\src\\main\\java\\x64\\opencv_java452.dll");

    }
    @Test
    public void test(){
        Mat src = Imgcodecs.imread("D:\\image\\card\\20210830155410.jpg");
        Mat mask = Imgcodecs.imread("D:\\image\\card\\20210830155410.jpg");
        System.out.println( "T".codePointAt(0));
    }

    @Test
    public void test1(){
        Mat src = Imgcodecs.imread("D:\\image\\card\\12\\group1\\M01\\14\\2.jpg");
        Rect rect = new Rect(new Point(131, 207), new Point(1157, 1597));
        Mat submat = src.submat(rect);
  Imgcodecs.imwrite("D:\\image\\card\\12\\group1\\M01\\14\\16T.jpg",submat);
    }
    @Test
    public void test2(){
        Mat src = Imgcodecs.imread("D:\\image\\0704\\20230704095738.jpg");
        Mat tmpImg =src.clone();
        // 计算目标图像的尺寸
        Point p0 = new Point(70,3);
        Point p1 = new Point(1215,82);
        Point p2 = new Point(1221,1639);
        Point p3 = new Point(85,1688);
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

        // 提取图像
        Mat transmtx = Imgproc.getPerspectiveTransform(cornerMat, quadMat);
        Imgproc.warpPerspective(tmpImg, quad, transmtx, quad.size());
        Imgcodecs.imwrite("D:\\image\\0704\\quad.jpg",quad);
    }
    // 点到点的距离
    private static double getSpacePointToPoint(Point p1, Point p2) {
        double a = p1.x - p2.x;
        double b = p1.y - p2.y;
        return Math.sqrt(a * a + b * b);
    }
}
