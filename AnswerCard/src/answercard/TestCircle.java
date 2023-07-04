package answercard;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * @ClassName TestCircle
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/5/11 12:41
 * @Version 1.0
 */
public class TestCircle {
    public static void main(String[] args) {
        System.load("D:/work/AnswerCard/AnswerCard/libs/x64/opencv_java450.dll");
        Mat src = Imgcodecs.imread("D:/work/AnswerCard/AnswerCard/img/1000530001A.jpg");
        Mat dst = src.clone();
        Imgproc.cvtColor(src, dst, Imgproc.COLOR_BGR2GRAY);

        Mat circles = new Mat();
       //核心代码
        Imgproc.HoughCircles(dst, circles, Imgproc.HOUGH_GRADIENT, 1, 100, 100, 100, 0, 0);
        System.out.println(circles.cols());
        Point[] rectPoint = new Point[4];
        for (int i = 0; i < circles.cols(); i++){
            double[] vCircle = circles.get(0, i);

            Point center = new Point(vCircle[0], vCircle[1]);
            rectPoint[i]=center;
            int radius = (int) Math.round(vCircle[2]);

            // circle center
            Imgproc.circle(src, center, 3, new Scalar(0, 255, 0), -1, 8, 0);
            // circle outline
            Imgproc.circle(src, center, radius, new Scalar(0, 255, 255), 3, 8, 0);
        }

        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cest19.jpg", src);
        int[] roi = cutRectHelp(rectPoint);
        Mat temp = new Mat(src, new Rect(roi[0], roi[1], roi[2], roi[3]));
        Mat t = new Mat();
        temp.copyTo(t);
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cest5.jpg", t);
    }
    /**
     * 把矫正后的图像切割出来--辅助函数(修复)
     *
     * @param rectPoint
     *            矩形的四个点
     * @return int[startLeft , startUp , width , height]
     */
    public static int[] cutRectHelp(Point[] rectPoint) {
        double minX = rectPoint[0].x;
        double maxX = rectPoint[0].x;
        double minY = rectPoint[0].y;
        double maxY = rectPoint[0].y;
        for (int i = 1; i < rectPoint.length; i++) {
            minX = rectPoint[i].x < minX ? rectPoint[i].x : minX;
            maxX = rectPoint[i].x > maxX ? rectPoint[i].x : maxX;
            minY = rectPoint[i].y < minY ? rectPoint[i].y : minY;
            maxY = rectPoint[i].y > maxY ? rectPoint[i].y : maxY;
        }
        int[] roi = { (int) Math.abs(minX), (int) Math.abs(minY), (int) Math.abs(maxX - minX),
                (int) Math.abs(maxY - minY) };
        return roi;
    }
    /**
     * 旋转矩形
     *
     * @param cannyMat
     *            mat矩阵
     * @param rect
     *            矩形
     * @return
     */

    public static Mat rotation(Mat cannyMat, RotatedRect rect) {
        // 获取矩形的四个顶点
        Point[] rectPoint = new Point[4];
        rect.points(rectPoint);

        int a = 0;

        if (cannyMat.height() > cannyMat.width()) {
            a = 90;
        }

        double angle = rect.angle + 90 + a;

        Point center = rect.center;

        Mat CorrectImg = new Mat(cannyMat.size(), cannyMat.type());

        cannyMat.copyTo(CorrectImg);

        // 得到旋转矩阵算子
        Mat matrix = Imgproc.getRotationMatrix2D(center, angle, 0.8);

        Imgproc.warpAffine(CorrectImg, CorrectImg, matrix, CorrectImg.size(), 1, 0, new Scalar(0, 0, 0));

        return CorrectImg;
    }

    /**
     * canny算法，边缘检测
     *
     * @param src
     * @return
     */
    public static Mat canny(Mat src) {
        Mat mat = src.clone();
        Imgproc.Canny(src, mat, 60, 200);
        // saveImg(mat, "C:/Users/admin/Desktop/opencv/open/x/canny.jpg");
        return mat;
    }
}
