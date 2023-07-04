package answercard;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * @ClassName ImageConrrection1
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/5/10 11:36
 * @Version 1.0·
 */
public class ImageConrrection1 {

    /**
     * 逆时针旋转,但是图片宽高不用变,此方法与rotateLeft和rotateRight不兼容
     *
     * @param src    源
     * @param angele 旋转的角度
     * @return 旋转后的对象
     */
    public static Mat rotate(Mat src, double angele) {
        Mat dst = src.clone();
        Point center = new Point(src.width() / 2.0, src.height() / 2.0);
        Mat affineTrans = Imgproc.getRotationMatrix2D(center, angele, 1.0);
        Imgproc.warpAffine(src, dst, affineTrans, dst.size(), Imgproc.INTER_NEAREST);
        return dst;
    }

    /**
     * 图像整体向左旋转90度
     *
     * @param src Mat
     * @return 旋转后的Mat
     */
    public static Mat rotateLeft(Mat src) {
        Mat tmp = new Mat();
        // 此函数是转置、（即将图像逆时针旋转90度，然后再关于x轴对称）
        Core.transpose(src, tmp);
        Mat result = new Mat();
        // flipCode = 0 绕x轴旋转180， 也就是关于x轴对称
        // flipCode = 1 绕y轴旋转180， 也就是关于y轴对称
        // flipCode = -1 此函数关于原点对称
        Core.flip(tmp, result, 0);
        return result;
    }

    /**
     * 图像整体向左旋转90度
     *
     * @param src Mat
     * @return 旋转后的Mat
     */
    public static Mat rotateRight(Mat src) {
        Mat tmp = new Mat();
        // 此函数是转置、（即将图像逆时针旋转90度，然后再关于x轴对称）
        Core.transpose(src, tmp);
        Mat result = new Mat();
        // flipCode = 0 绕x轴旋转180， 也就是关于x轴对称
        // flipCode = 1 绕y轴旋转180， 也就是关于y轴对称
        // flipCode = -1 此函数关于原点对称
        Core.flip(tmp, result, 1);
        return result;
    }
    @Test
    public void testRotate() {
        Mat dog = Imgcodecs.imread("/tmp/dog.jpg");
        Mat result = rotate(dog, 30);
        showImg(result);
    }

    @Test
    public void testRotateLeft() {
        Mat dog = Imgcodecs.imread("/tmp/dog.jpg");
        Mat result = rotateLeft(dog);
        showImg(result);
    }

    @Test
    public void testRotateRight() {
        Mat dog = Imgcodecs.imread("D:/work/AnswerCard/AnswerCard/img/A3.jpg");
        Mat result = rotateRight(dog);
        showImg(result);
    }

    private void showImg(Mat mat) {
        HighGui.imshow("结果", mat);
        HighGui.waitKey();
    }

}
