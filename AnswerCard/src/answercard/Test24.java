package answercard;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

/**
 * @ClassName Test24
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/9/14 10:11
 * @Version 1.0
 */
public class Test24 {
    @Before
    public void init() {
        System.load("D:/work/AnswerCard/AnswerCard/libs/x64/opencv_java450.dll");
        // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    @Test
    public void test(){
        Mat img = Imgcodecs.imread("D:\\image\\card\\1.jpg");
        Mat gray=new Mat();
        Imgproc.cvtColor(img,gray,Imgproc.COLOR_RGB2GRAY);
        Imgproc.Canny(gray,gray,50,150,3);
        Mat lines=new Mat();
        Imgproc.HoughLines(gray,lines,1,Math.PI/180,0);
        for (int x = 0; x < lines.rows(); x++)
        {
            double[] vec = lines.get(x, 0);

            double rho = vec[0];
            double theta = vec[1];

            Point pt1 = new Point();
            Point pt2 = new Point();

            double a = Math.cos(theta);
            double b = Math.sin(theta);

            double x0 = a * rho;
            double y0 = b * rho;

            pt1.x = Math.round(x0 + 1000 * (-b));
            pt1.y = Math.round(y0 + 1000 * (a));
            pt2.x = Math.round(x0 - 1000 * (-b));
            pt2.y = Math.round(y0 - 1000 * (a));
            if(pt2.x == pt1.x || pt2.y==pt1.y){
                continue;
            }
            double t = (pt2.y - pt1.y) / (pt2.x - pt1.x);
            double rotate_angle = Math.toDegrees(Math.atan(t));
            if(rotate_angle>45){
                rotate_angle=-90+rotate_angle;

            }else if(rotate_angle<-45) {
                rotate_angle=90+rotate_angle;
            }
            Mat subMat = Imgproc.getRotationMatrix2D(new Point(img.width() / 2.00, img.height() / 2.00), rotate_angle, 1);
            Mat dst = new Mat();
            Imgproc.warpAffine(img, dst , subMat, img.size(), 1, 0,new Scalar(255,255,255));
            if (theta >= 0)
            {
                Imgproc.line(img, pt1, pt2, new Scalar(255, 255, 255, 255), 1, Imgproc.LINE_4, 0);
            }
            Imgcodecs.imwrite("D:\\image\\card\\2.jpg", dst);
        }

    }

    }

