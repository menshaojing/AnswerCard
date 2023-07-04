package answercard;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName Test
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/5/10 13:23
 * @Version 1.0
 */
public class Test {
    public static void main(String[] args) throws IOException {
        //顺时针旋转90度
        BufferedImage src = ImageIO.read(new File("D:/work/AnswerCard/AnswerCard/img/A3.jpg"));
        //根据坐标算出倾斜角度
BufferedImage des1 = RotateImage.Rotate(src, 23);
        try {
            ImageIO.write(des1, "jpg", new File("D:/23.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        edge_detection();
    }

public static void edge_detection(){
    System.load("D:/work/AnswerCard/AnswerCard/libs/x64/opencv_java450.dll");
    String path="D:/work/AnswerCard/AnswerCard/img/fapiao.jpg";

    Mat srcImage = Imgcodecs.imread(path);
    int ratio = srcImage.height() / 500;
    Mat orig = srcImage.clone();
    Mat image=new Mat();
    Imgproc.resize(orig,image,new Size(orig.width(),orig.height()));
    Mat gray=new Mat();
    Imgproc.cvtColor(image,gray,Imgproc.COLOR_RGB2GRAY);
    Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/gray.jpg", gray);
    Mat  blur=new Mat();
    Imgproc.GaussianBlur(gray,blur,new Size(5,5),0);
    Mat edged=new Mat();
    Imgproc.Canny(gray,edged,75,200);
    Mat hierarchy=new Mat();
    List<MatOfPoint> contours=new ArrayList<MatOfPoint>();
    Imgproc.findContours(edged.clone(),contours,hierarchy,Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);
    Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/edged.jpg", edged);
    //排序
    if (contours.size() <= 0) {
        throw new RuntimeException("未找到图像轮廓");
    } else {
        // 对contours进行了排序，按递增顺序
        contours.sort(new Comparator<MatOfPoint>() {
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
    }
   /* MatOfInt hull = new MatOfInt();*/

   /* approx.convertTo(approx, CvType.CV_32F);
    org.opencv.core.Point[] contourPoints=null;
    List<MatOfPoint> contour=new ArrayList<MatOfPoint>();
    List<Point> newPoints = new ArrayList<>();*/

    MatOfPoint matOfPoint = contours.get(contours.size() - 1);
    MatOfPoint2f approx = new MatOfPoint2f(matOfPoint.toArray());
    /*contour.add(matOfPoint);
    Imgproc.convexHull(matOfPoint, hull);*/
   /* contourPoints =matOfPoint.toArray();*/
   /* // 用凸包计算出新的轮廓点
    int[] indices = hull.toArray();
    newPoints = new ArrayList<>();
    for (int index : indices) {
        newPoints.add(contourPoints[index]);
    }
    MatOfPoint2f contourHull = new MatOfPoint2f();
    contourHull.fromList(newPoints);
    //获取输入坐标点
    MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
    matOfPoint2f.fromList(newPoints);
    double peri = Imgproc.arcLength(matOfPoint2f, true);
    MatOfPoint2f approxCurve=new MatOfPoint2f();
    Imgproc.approxPolyDP(matOfPoint2f,approxCurve,peri*0.02,true);*/
    RotatedRect rect = Imgproc.minAreaRect(approx);
    Scalar color=new Scalar(0, 255, 0);
   // Imgproc.drawContours(srcImage,contour,-1,color,2);
    Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cest2.jpg", srcImage);

     Point[] rectPoint = new Point[4];
    rect.points(rectPoint);

    List<Point> listPoint = Arrays.asList(rectPoint);

    Point tl = rectPoint[0];

    Point tr = rectPoint[1];

    Point br = rectPoint[2];

    Point bl = rectPoint[3];

    double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
    double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));
    double maxWidth = Math.max(widthA, widthB);
    System.out.println("maxWidth:"+maxWidth);
    double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
    double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));
    double maxHeight = Math.max(heightA, heightB);

    Point[] trans=new Point[4];
    trans[0]=new Point(0,0);
    trans[1]=new Point(maxWidth-1,0);
    trans[2]=new Point(maxWidth-1,maxHeight-1);
    trans[3]=new Point(0,maxHeight-1);

    Mat convertersMat = Converters.vector_Point2f_to_Mat(listPoint);
    for (int i = 0; i < listPoint.size(); i++) {
        trans[i]=listPoint.get(i);
    }
    Mat transtersMat = Converters.vector_Point2f_to_Mat(Arrays.asList(trans));
    System.out.println("convertersMat: row:"+convertersMat.rows());
    Mat dist=new Mat();

    srcImage.copyTo(dist);
    Mat perspectiveMmat=Imgproc.getPerspectiveTransform(convertersMat,transtersMat);
    //计算变换结果
    Mat warpedGray=new Mat();
    Imgproc.warpPerspective(image,warpedGray ,perspectiveMmat,image.size(),Imgproc.INTER_LINEAR);
    Mat thresh=new Mat();
    Imgproc.threshold(warpedGray,thresh,100,225,Imgproc.THRESH_BINARY);
    Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/warpedGray.jpg", warpedGray);



}
/*    public static Mat normalProcess(Mat img){
        Mat threshImg = Thresholding.InvertImageColor(img);
        Thresholding.gridDetection(threshImg);
        Mat mat = Mat.zeros(4,2,CvType.CV_32F);
        mat.put(0,0,0); mat.put(0,1,512);
        mat.put(1,0,0); mat.put(1,1,0);
        mat.put(2,0,512); mat.put(2,1,0);
        mat.put(3,0,512); mat.put(3,1,512);

        mat = Imgproc.getPerspectiveTransform(Thresholding.grid,mat);

        Mat M = new Mat();

        Imgproc.warpPerspective(threshImg,M,mat, new Size(512,512));
        return Thresholding.InvertImageColor(M);
    }*/

   /* public static Mat adaptativeProcess(Mat img){
        Mat im = new Mat();
        Imgproc.threshold(img,im,120,255,Imgproc.THRESH_TRUNC);
        im = Thresholding.adaptativeThresholding(im);
        Imgproc.medianBlur(im,im,7);
        Mat threshImg = Thresholding.InvertImageColor(im);
        Thresholding.gridDetection(threshImg);

        Mat mat = Mat.zeros(4,2,CvType.CV_32F);
        mat.put(0,0,0); mat.put(0,1,512);
        mat.put(1,0,0); mat.put(1,1,0);
        mat.put(2,0,512); mat.put(2,1,0);
        mat.put(3,0,512); mat.put(3,1,512);

        mat = Imgproc.getPerspectiveTransform(Thresholding.grid,mat);

        Mat M = new Mat();

        Imgproc.warpPerspective(threshImg,M,mat, new Size(512,512));

        Imgproc.medianBlur(M,M,3);
        Imgproc.threshold(M,M,254,255,Imgproc.THRESH_BINARY);

        return Thresholding.InvertImageColor(M);
    }*/
}
