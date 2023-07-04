package imagehandle;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

/**
 * @ClassName ImageHandle
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/5/27 10:05
 * @Version 1.0
 */
public class ImageHandle {
    private static Mat imgOut;
    private static Mat Hw,h;
    public static void main(String[] args) {

    }
    public static void calcPSF(Mat outputImg, Size filterSize,int R){
        h=new Mat(filterSize, CvType.CV_32F,new Scalar(0));
        Point point = new Point(filterSize.width / 2, filterSize.height / 2);
        Imgproc.circle(h,point,R,new Scalar(255),-1,8);
        Scalar scalar = new Scalar(h.channels());
        double oup = h.channels() / scalar.val[0];

    }
}
