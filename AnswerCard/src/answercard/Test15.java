package answercard;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

/**
 * @ClassName Test15
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/5/22 16:48
 * @Version 1.0
 */
public class Test15 {
    @Before
    public void init() {
        System.load("D:/work/AnswerCard/AnswerCard/libs/x64/opencv_java450.dll");
        // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    @Test
    public void test7(){
        //-------------------ͼ��Ԥ����-------start------------
        Mat grayImg = new Mat();
        Mat tmpImg = new Mat();
        Mat tmpImg2 = new Mat();
        String imgPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0729\\3.jpg";
        String destPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\0729\\3_1.jpg";
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
         Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cannyImg.jpg", cannyImg);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
        Imgproc.dilate(cannyImg, cannyImg, kernel, new Point(-1, -1), 3, 1, new Scalar(1));
        Imgproc.erode(cannyImg, cannyImg, kernel, new Point(-1, -1), 3, 1, new Scalar(1));
        Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/dilate.jpg", cannyImg);
         Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/erode.jpg", cannyImg);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat contourImg = tmpImg.clone();
         Imgcodecs.imwrite("D:/work/AnswerCard/AnswerCard/img/cannyImg.jpg", cannyImg);
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
                System.out.println("�����"+Imgproc.contourArea(approx));
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
        System.out.println(squares.size());
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
        Rect rect = new Rect(p00, p22);
        Imgproc.rectangle(src,rect.tl(),rect.br(),new Scalar(0,255,0),2);
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0729\\rectangle.jpg",src);
        //140007.0  3988980  3988980.0
        //2403
        //1660
        System.out.println(rect.area());
        System.out.println(rect.width);
        System.out.println(rect.height);
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
      /* final Size A4_SIZE=new Size(1240,1754);
        Mat mat = new Mat(A4_SIZE, quad.type(), new Scalar(255,255,255));*/
         final Size    A3_SIZE=new Size(2480,1754);
         Mat mat = new Mat(A3_SIZE, quad.type(), new Scalar(255,255,255));
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
       /* int left =(int) (LOCATION_POINT_LEFT_MARGIN * pixel );
        int top =(int) (LOCATION_POINT_TOP_MARGIN * pixel );*/
        int left = (mat.width() - currentMat.width()) / 2;
		int top = (mat.height() - currentMat.height()) / 2;
        Mat range=mat.colRange(left,currentMat.cols() +left);
        range=range.rowRange(top,currentMat.rows()+top);
        currentMat.copyTo(range);
        Imgcodecs.imwrite(destPath, mat);
        Imgcodecs.imwrite(destPath, mat);


        //-------------------��ԭͼ��-----------end--------

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
}
