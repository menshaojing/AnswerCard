package answercard;

import org.junit.Before;
import org.junit.Test;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.*;

import static org.opencv.core.CvType.CV_8UC3;
import static org.opencv.imgproc.Imgproc.*;

/**
 * @ClassName Test10
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/7/19 9:11
 * @Version 1.0
 */
public class Test10 {
    @Before
    public void init(){
        System.load("D:\\learn\\orc\\src\\main\\java\\x64\\opencv_java450.dll");

    }
    @Test
    public void test13(){
        Mat imgIn = Imgcodecs.imread("D:\\work\\AnswerCard\\img\\0802\\dst.png");
        Mat grayImage = new Mat();
        Imgproc.cvtColor(imgIn, grayImage, Imgproc.COLOR_RGB2GRAY);
        //大津法找到敏感区域
        Imgproc.threshold(grayImage,grayImage,100,255,Imgproc.THRESH_OTSU);
        Imgproc.threshold(grayImage,grayImage,100,255,Imgproc.THRESH_BINARY_INV);
        //形态学变化，确保目标区别都是连在一起的
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(1, 3));
        Imgproc.morphologyEx(grayImage,grayImage,Imgproc.MORPH_OPEN,kernel);
        Imgproc.dilate(grayImage, grayImage, kernel, new Point(-1, -1), 3, 1, new Scalar(1));
        Imgproc.erode(grayImage, grayImage, kernel, new Point(-1, -1), 3, 1, new Scalar(1));
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\img\\0802\\erode.png",grayImage);

    }


    @Test
    public void test12(){
        Mat source1 = Imgcodecs.imread("D:\\work\\AnswerCard\\AnswerCard\\img\\07221\\4.jpg");
        Mat source2 = new Mat();
                Imgproc.cvtColor(source1,source2, Imgproc.COLOR_RGB2GRAY);

        Point anchor01 = new Point(32,47);
        Point anchor02 = new Point(1220,42);
        Point anchor03 = new Point(1174,1738);
        Point anchor04 = new Point(63,1735);

        MatOfPoint mop = new MatOfPoint(anchor01, anchor02, anchor03, anchor04);
        MatOfPoint2f mat2f = new MatOfPoint2f();
        MatOfPoint2f refmat2f = new MatOfPoint2f();
        mop.convertTo(mat2f, CvType.CV_32FC1);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        contours.add(mop);
        Imgproc.polylines(source2, contours, true, new Scalar(0, 0, 255), 1);
        String destPath = "D:\\work\\AnswerCard\\AnswerCard\\img\\07221\\rect1.png";
        Imgcodecs.imwrite(destPath, source2);
        //-------------------根据四点截取图像-----start----------
        // 计算目标图像的尺寸 Retained Shallow
        Point p0 = anchor01;
        Point p1 = anchor02;
        Point p2 = anchor03;
        Point p3 =anchor04;
        double space0 = getSpacePointToPoint(p0, p1);
        double space1 = getSpacePointToPoint(p1, p2);
        double space2 = getSpacePointToPoint(p2, p3);
        double space3 = getSpacePointToPoint(p3, p0);
        //Add the perspective correction
        double paraFix1 = (space3 / space1) > 1 ? (space3 / space1) : (space1 / space3);
        double paraFix2 = (space2 / space0) > 1 ? (space2 / space0) : (space0 / space2);
        double imgWidth = space1 > space3 ? space1 : space3;
        double imgHeight = space0 > space2 ? space0 : space2;
       /* if(paraFix1>paraFix2){
            imgHeight=imgHeight * paraFix1;
        }else{
            imgWidth = imgWidth * paraFix2;
        }
        //p0p1 p2p3 宽
        //p1p2 p3p0 长
        imgWidth=Math.max(space0,space2);
        imgHeight=Math.max(space1,space3);*/
        double tmp = 0.0;
        if(imgHeight<imgWidth){
            tmp=imgHeight;
            imgHeight=imgWidth;
            imgWidth=tmp;
        }

        System.out.println("imgWidth :"+imgWidth);
        System.out.println("imgHeight :"+imgHeight);
        System.out.println("simgWidth :"+source1.width());
        System.out.println("simgHeight :"+source1.height());
        double paddingW = (source1.width() - imgWidth) / 2;
        System.out.println("paddingW :"+paddingW);

        double paddingH = (source1.height() - imgHeight) / 2;
        System.out.println("paddingH :"+paddingH);

        Point desPoint0 = new Point(paddingW, paddingH);
        Point desPoint1 = new Point(paddingW+imgWidth, paddingH);
        Point desPoint2 = new Point(paddingW+imgWidth, paddingH+imgHeight);
        Point desPoint3 = new Point(paddingW, paddingH+imgHeight);
        System.out.println("desPoint0 :"+desPoint0);
        System.out.println("desPoint1 :"+desPoint1);
        System.out.println("desPoint2 :"+desPoint2);
        System.out.println("desPoint3 :"+desPoint3);
        MatOfPoint desMatOfPoint = new MatOfPoint(desPoint0, desPoint1, desPoint2, desPoint3);
        MatOfPoint2f cornerMat = new MatOfPoint2f();
        desMatOfPoint.convertTo(cornerMat,CvType.CV_32FC1);
        Mat quad = Mat.zeros((int) imgHeight , (int) imgWidth , CvType.CV_32FC1);
       /* int paddingh = (source1.rows() - quad.rows()) / 2;
        int paddingw =  (source1.cols() - quad.cols()) / 2;*/
/*
        MatOfPoint2f quadMat = new MatOfPoint2f(
                new Point(paddingw, paddingh+imgHeight),
                new Point(paddingw, paddingh),
                new Point(paddingw+imgWidth, paddingh),
                new Point(paddingw+imgWidth, paddingh+imgHeight));*/

        // 提取图像
        Mat transmtx = Imgproc.getPerspectiveTransform(mat2f, cornerMat);


       // Mat warpMatrix = Imgproc.getPerspectiveTransform(mat2f, refmat2f);

        Mat dst = new Mat(source1.rows(), source1.cols(), source1.type());
        System.out.println(source1.rows() + " " + source1.cols());
        Imgproc.warpPerspective(source1, dst, transmtx, dst.size(), Imgproc.INTER_LINEAR, 0,
                new Scalar(255, 255, 255));
        List<MatOfPoint> contoursdes = new ArrayList<MatOfPoint>();
        contoursdes.add(desMatOfPoint);
        Imgproc.polylines(dst, contoursdes, true, new Scalar(0, 0, 255), 1);
        destPath =  "D:\\work\\AnswerCard\\AnswerCard\\img\\07221\\rect2.png";
        Imgcodecs.imwrite(destPath, dst);


    }
    // 点到点的距离
    private static double getSpacePointToPoint(Point p1, Point p2) {
        double a = p1.x - p2.x;
        double b = p1.y - p2.y;
        return Math.sqrt(a * a + b * b);
    }




    @Test
    public void test2(){
        Mat src = Imgcodecs.imread("D:\\work\\AnswerCard\\AnswerCard\\img\\mat8.jpg");
        //Mat src = Imgcodecs.imread("D:\\work\\AnswerCard\\AnswerCard\\img\\0722\\2.jpg");
        Mat gradMat = new Mat();
        Imgproc.cvtColor(src,gradMat,Imgproc.COLOR_RGB2GRAY);
        Imgproc.threshold(gradMat,gradMat,100,255,Imgproc.THRESH_OTSU);
        Imgproc.threshold(gradMat,gradMat,100,255,Imgproc.THRESH_BINARY_INV);
        //形态学变化，确保目标区别都是连在一起的
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(1, 1));
        //开操作 (去除一些噪点)
        Imgproc.morphologyEx(gradMat, gradMat, Imgproc.MORPH_OPEN, kernel);
        //闭操作 (连接一些连通域)
        Imgproc.morphologyEx(gradMat, gradMat, Imgproc.MORPH_CLOSE, kernel);
        Imgproc.dilate(gradMat, gradMat, kernel, new Point(-1, -1), 1, 1, new Scalar(1));
        Imgproc.erode(gradMat, gradMat, kernel, new Point(-1, -1), 1, 1, new Scalar(1));
        Imgproc.Canny(gradMat,gradMat,13,200,3);
        Mat lineMat=new Mat();
        Vector<Rect> rects=new Vector<>();
        Imgproc.HoughLines(gradMat, lineMat, 1, Math.PI / 180, 14, 0, 0, 4, 90);

        for (int x = 0; x < lineMat.rows(); x++) {
            double[] vec = lineMat.get(x, 0);

            double rho = vec[0]; //就是圆的半径r
            double theta = vec[1]; //就是直线的角度

            Point pt1 = new Point();
            Point pt2 = new Point();

            double a = Math.cos(theta);
            double b = Math.sin(theta);

            double x0 = a * rho;
            double y0 = b * rho;

            int lineLength =20;

            pt1.x = Math.round(x0 + lineLength * (-b));
            pt1.y = Math.round(y0 + lineLength * (a));
            pt2.x = Math.round(x0 - lineLength * (-b));
            pt2.y = Math.round(y0 - lineLength * (a));
            Rect rect = new Rect(pt1, pt2);

            if (theta >= 0) {
                if(rect.width==0){
                  continue;
                }
                if(rect.height==0){
                    continue;
                }
                if(rect.height<10){
                    continue;
                }
                if(rect.width<10){
                    continue;
                }
                rects.add(rect);
                System.out.println("w:"+rect.width + "h:"+rect.height);
              //  System.out.println("x:"+rect.x + "y:"+rect.y);
                Imgproc.line(src, pt1, pt2, new Scalar(255, 0, 0), 1, Imgproc.LINE_4, 0);
            }
        }

        System.out.println(rects.size());
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0722\\houghLinesP.jpg", src);
    }
    @Test
    public void test(){
        Mat src = Imgcodecs.imread("D:\\work\\AnswerCard\\AnswerCard\\img\\src.jpg");
        float totalScore =  subjectiveScore(src,
                1, new Rect(new Point(87, 1166), new Point(1156, 1190)),false,2f);
        System.out.println("得分："+totalScore);
    }
    /**
     * @Description 主观题得分（先阅后扫）
     * @param src 图像路径
     * @param fence 栏位
     * @param rect 分割图像矩形对象
     * @param flag false : 主观题模式识别  true : 填空题模式识别
     * @param score 题目总分（填空题模式识别所需参数）
     * @throws
     * @return int
     * @Author menshaojing
     * @Date  2021/7/20  17:43
     **/
    public static float subjectiveScore( Mat src, int fence,Rect rect,boolean flag,Float score ){
        //总分
        float totalScore=0f;
        src=src.submat(rect);
        List<Mat> matList=new ArrayList<>();
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\mat.jpg",src);
        //空格位
        List<Integer> space = Arrays.asList(10, 21);
        //初始值
        float initial = 9;
        int row=1;
        int col=22;
        if(flag){
            col=1;
            initial=score;
        }
        //分数 fraction
        Map<Mat,Float> fractionMap=new LinkedHashMap<>(col);

        if(fence==3 && !flag){
            col=15;
            initial=3;
            space=Arrays.asList(4, 14);
        }
        int xStep = src.cols()/col;
        int yStep =src.rows()/row;
        for (int i = 0; i < col; i++) {
            for (int j = 0; j < row; j++) {
                int x =  xStep*(i+1);
                int y = yStep*(j+1);
                //添加补差值（2,3），去除边框，尽量减少干扰
                Point p1 = new Point(x - xStep+3, y - yStep+2);
                Point p3 = new Point(x-2, y-3);
                Mat mat = src.submat(new Rect(p1, p3));
                matList.add(mat);
                if(initial==-1){
                    initial=9;
                }
                if(i==space.get(0) || i==space.get(1)){
                    fractionMap.put(mat,0f);
                }else{
                    fractionMap.put(mat,initial);
                    initial--;
                }
                Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\mat"+i+".jpg",src.submat(new Rect(p1,p3)));
            }

        }
        //1.使用HSV进行识别
        float[] hsvRes = HSVIdentify(matList, fractionMap);
        //2.HSV进行识别未成功，进行轮廓识别
        if(hsvRes[1]==0){
            totalScore= contourIdentify(matList, fractionMap);
        }else {
            totalScore=hsvRes[0];
        }



        return totalScore;
    }
   /**
    * @Description 使用HSV进行识别 （识别红色区域，相对准确些）
    * @param matList 分割图像对象
    * @param fractionMap  分割图像对应的分数
    * @throws
    * @return int[]
    * @Author menshaojing
    * @Date  2021/7/20  17:41
    **/
   public static float []  HSVIdentify(List<Mat> matList,Map<Mat,Float> fractionMap){
       //总分
       int totalScore=0;
       //识别是否成功
       float  contoursSize=0;
       Mat hsvMat = new Mat();
       for (Mat mat: matList) {
           Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(1, 1));
           //开操作 (去除一些噪点)
           Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_OPEN, kernel);
           //闭操作 (连接一些连通域)
           Imgproc.morphologyEx(mat, mat, Imgproc.MORPH_CLOSE, kernel);
           Imgproc.filter2D(mat,mat,-1,kernel);
           Imgproc.dilate(mat, mat, kernel, new Point(-1, -1), 1, 2, new Scalar(1));
           Imgproc.erode(mat, mat, kernel, new Point(-1, -1), 1, 2, new Scalar(1));
           Imgproc.cvtColor(mat, hsvMat, COLOR_BGR2HSV);
           Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\erode"+matList.indexOf(mat)+".jpg",hsvMat);
            Vector<Mat> hsvSplit=new Vector<>();
           //因为我们读取的是彩色图，直方图均衡化需要在HSV空间做
            Core.split(hsvMat,hsvSplit);
           Imgproc.equalizeHist(hsvSplit.get(2),hsvSplit.get(2));
           Core.merge(hsvSplit,hsvMat);
           double hMin = 0,hMax=10;
           double sMin=43,sMax=255;
           double vMin=46,vMax=255;
           double min = 0;
           double max = 59;
           Mat des = new Mat();
          // Core.inRange(hsvMat, new Scalar(hMin, sMin, vMin), new Scalar(hMax, sMax, vMax), des);
           Core.inRange(hsvMat, new Scalar(min, 90, 90), new Scalar(max, 255, 255), des);

           Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\des"+matList.indexOf(mat)+".jpg",des);

           //开操作 (去除一些噪点)
           Imgproc.morphologyEx(des, des, Imgproc.MORPH_OPEN, kernel);
           //闭操作 (连接一些连通域)
           Imgproc.morphologyEx(des, des, Imgproc.MORPH_CLOSE, kernel);
           //Imgproc.GaussianBlur(des,des,new Size(3,3),0,0);
           // Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\GaussianBlur"+matList.indexOf(mat)+".jpg",des);

           Imgproc.Canny(des,des,3,9,3);
           Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\Canny"+matList.indexOf(mat)+".jpg",des);

           List<MatOfPoint> contours=new ArrayList<>();
           Mat hierarchy=new Mat();
           Imgproc.findContours(des,contours,hierarchy,RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
           contoursSize+=contours.size();
           for (MatOfPoint matOfPoint:contours) {
               Rect rectVo = Imgproc.boundingRect(matOfPoint);
               Imgproc.rectangle(mat,rectVo.tl(),rectVo.br(),new Scalar(0,255,0),1,8);
           }
           if(contours.size()>0){
               totalScore+=fractionMap.get(mat);
           }

           // Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\HSV"+matList.indexOf(mat)+".jpg",mat);
       }
       return new float[]{totalScore,contoursSize};
   }



   @Test
           public void test11(){
       Mat src = Imgcodecs.imread("D:\\work\\AnswerCard\\AnswerCard\\img\\HSV17.jpg");
       distinguishColor(src);

   }
    //区分图像颜色
    void distinguishColor(Mat matSrc)
    {
        Map<String, List<Point>> colorMap=new HashMap<>();
        Mat matHsv=new Mat();
        Imgproc.cvtColor(matSrc, matHsv, COLOR_BGR2HSV);

        for (int i = 0; i < matSrc.rows(); i++)
        {
            for (int j = 0; j < matSrc.cols(); j++)
            {

                // 获取每个像素
                double[] pixel = matSrc.get(i, j);

                List<Point> pointList=new ArrayList<>();

                if ((pixel[0] >= 0 && pixel[0] <= 180) && (pixel[1] >= 0 && pixel[1] <= 255) && (pixel[2] >= 0 && pixel[2] <= 46))
                {
                     //黑色
                    if(!colorMap.containsKey("black")){

                        pointList.add(new Point(i,j));
                        colorMap.put("black",pointList);
                    }else{
                        pointList=colorMap.get("black");
                        pointList.add(new Point(i,j));
                        colorMap.put("black",pointList);
                    }
                }
                else if ((pixel[0] >= 0 && pixel[0] <= 180) && (pixel[1] >= 0 && pixel[1] <= 43) && (pixel[2] >= 46 && pixel[2] <= 220))
                {
                    //灰色 gray
                    if(!colorMap.containsKey("gray")){

                        pointList.add(new Point(i,j));
                        colorMap.put("gray",pointList);
                    }else{
                        pointList=colorMap.get("gray");
                        pointList.add(new Point(i,j));
                        colorMap.put("gray",pointList);
                    }
                }

                else if ((pixel[0] >= 0 && pixel[0] <= 180) && (pixel[1] >= 0 && pixel[1] <= 30) && (pixel[2] >= 221 && pixel[2] <= 255))
                {
                    //白色 white
                    if(!colorMap.containsKey("white")){

                        pointList.add(new Point(i,j));
                        colorMap.put("white",pointList);
                    }else{
                        pointList=colorMap.get("white");
                        pointList.add(new Point(i,j));
                        colorMap.put("white",pointList);
                    }
                }
                else if ((/*(pixel[0] >= 0 && pixel[0] <= 10) ||*/ (pixel[0] >= 156 && pixel[0] <= 180)) && (pixel[1] >= 43 && pixel[1] <= 255) && (pixel[2] >= 46 && pixel[2] <= 255))
                {
                    //红 red
                    System.out.println("h:"+pixel[0]+"s:"+pixel[1]+"v:"+pixel[2]);
                    if(!colorMap.containsKey("red")){

                        pointList.add(new Point(i,j));
                        colorMap.put("red",pointList);
                    }else{
                        pointList=colorMap.get("red");
                        pointList.add(new Point(i,j));
                        colorMap.put("red",pointList);
                    }
                }
                else if ((pixel[0] >= 11 && pixel[0] <= 25) && (pixel[1] >= 43 && pixel[1] <= 255) && (pixel[2] >= 46 && pixel[2] <= 255))
                {
                    //橙  orange
                    if(!colorMap.containsKey("orange")){

                        pointList.add(new Point(i,j));
                        colorMap.put("orange",pointList);
                    }else{
                        pointList=colorMap.get("orange");
                        pointList.add(new Point(i,j));
                        colorMap.put("orange",pointList);
                    }
                }
                else if ((pixel[0] >= 26 && pixel[0] <= 34) && (pixel[1] >= 43 && pixel[1] <= 255) && (pixel[2] >= 46 && pixel[2] <= 255))
                {
                    //黄 yellow
                    if(!colorMap.containsKey("yellow")){

                        pointList.add(new Point(i,j));
                        colorMap.put("yellow",pointList);
                    }else{
                        pointList=colorMap.get("yellow");
                        pointList.add(new Point(i,j));
                        colorMap.put("yellow",pointList);
                    }
                }
                else if ((pixel[0] >= 35 && pixel[0] <= 77) && (pixel[1] >= 43 && pixel[1] <= 255) && (pixel[2] >= 46 && pixel[2] <= 255))
                {
                     //绿 green
                    if(!colorMap.containsKey("green")){

                        pointList.add(new Point(i,j));
                        colorMap.put("green",pointList);
                    }else{
                        pointList=colorMap.get("green");
                        pointList.add(new Point(i,j));
                        colorMap.put("green",pointList);
                    }
                }
                else if ((pixel[0] >= 78 && pixel[0] <= 99) && (pixel[1] >= 43 && pixel[1] <= 255) && (pixel[2] >= 46 && pixel[2] <= 255))
                {
                    //青 blueness
                    if(!colorMap.containsKey("blueness")){

                        pointList.add(new Point(i,j));
                        colorMap.put("blueness",pointList);
                    }else{
                        pointList=colorMap.get("blueness");
                        pointList.add(new Point(i,j));
                        colorMap.put("blueness",pointList);
                    }
                }
                else if ((pixel[0] >= 100 && pixel[0] <= 124) && (pixel[1] >= 43 && pixel[1] <= 255) && (pixel[2] >= 46 && pixel[2] <= 255))
                {
                     //蓝 blue
                    if(!colorMap.containsKey("blue")){

                        pointList.add(new Point(i,j));
                        colorMap.put("blue",pointList);
                    }else{
                        pointList=colorMap.get("blue");
                        pointList.add(new Point(i,j));
                        colorMap.put("blue",pointList);
                    }
                }
                else if ((pixel[0] >= 125 && pixel[0] <= 155) && (pixel[1] >= 43 && pixel[1] <= 255) && (pixel[2] >= 46 && pixel[2] <= 255))
                {
                    //紫 purple
                    if(!colorMap.containsKey("purple")){

                        pointList.add(new Point(i,j));
                        colorMap.put("purple",pointList);
                    }else{
                        pointList=colorMap.get("purple");
                        pointList.add(new Point(i,j));
                        colorMap.put("purple",pointList);
                    }
                }
                else
                {
                   //未知 unknown
                    if(!colorMap.containsKey("unknown")){

                        pointList.add(new Point(i,j));
                        colorMap.put("unknown",pointList);
                    }else{
                        pointList=colorMap.get("unknown");
                        pointList.add(new Point(i,j));
                        colorMap.put("unknown",pointList);
                    }
                }
            }
        }
        double maxX = colorMap.get("red").stream().max(Comparator.comparing(point -> {
            return point.x;
        })).get().x;
        double maxY = colorMap.get("red").stream().max(Comparator.comparing(point -> {
            return point.y;
        })).get().y;
        double minX = colorMap.get("red").stream().min(Comparator.comparing(point -> {
            return point.x;
        })).get().x;
        double minY = colorMap.get("red").stream().min(Comparator.comparing(point -> {
            return point.y;
        })).get().y;
        System.out.println(new Rect(new Point(minX,minY),new Point(maxX,maxY)));
        Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\0726\\red,jpg",matSrc.submat(new Rect(new Point(minX,minY),new Point(maxX,maxY))));
        System.out.println(colorMap);
    }
   /**
    * @Description 使用轮廓进行识别 （识别划线轮廓，准确率低）
    * @param matList 分割图像对象
    * @param fractionMap  分割图像对应的分数
    * @throws
    * @return int
    * @Author menshaojing
    * @Date  2021/7/20  17:42
    **/
   public static float contourIdentify(List<Mat> matList,Map<Mat,Float> fractionMap){
       //总分
       float totalScore=0;
       for (Mat mat: matList) {
           Mat gradMat = new Mat();
           Imgproc.cvtColor(mat,gradMat,Imgproc.COLOR_RGB2GRAY);
           Imgproc.threshold(gradMat,gradMat,100,255,Imgproc.THRESH_OTSU);
           Imgproc.threshold(gradMat,gradMat,100,255,Imgproc.THRESH_BINARY_INV);
           //形态学变化，确保目标区别都是连在一起的
           Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(1, 1));
           //开操作 (去除一些噪点)
           Imgproc.morphologyEx(gradMat, gradMat, Imgproc.MORPH_OPEN, kernel);
           //闭操作 (连接一些连通域)
           Imgproc.morphologyEx(gradMat, gradMat, Imgproc.MORPH_CLOSE, kernel);
           Imgproc.dilate(gradMat, gradMat, kernel, new Point(-1, -1), 1, 1, new Scalar(1));
           Imgproc.erode(gradMat, gradMat, kernel, new Point(-1, -1), 1, 1, new Scalar(1));
           Imgproc.Canny(gradMat,gradMat,13,200,3);
           Mat lineMat=new Mat();
           Vector<Rect> rects=new Vector<>();
           Imgproc.HoughLines(gradMat, lineMat, 1, Math.PI / 180, 14, 0, 0, 4, 90);
           for (int x = 0; x < lineMat.rows(); x++) {
               double[] vec = lineMat.get(x, 0);

               double rho = vec[0]; //就是圆的半径r
               double theta = vec[1]; //就是直线的角度

               Point pt1 = new Point();
               Point pt2 = new Point();

               double a = Math.cos(theta);
               double b = Math.sin(theta);

               double x0 = a * rho;
               double y0 = b * rho;

               int lineLength = 20;

               pt1.x = Math.round(x0 + lineLength * (-b));
               pt1.y = Math.round(y0 + lineLength * (a));
               pt2.x = Math.round(x0 - lineLength * (-b));
               pt2.y = Math.round(y0 - lineLength * (a));
               Rect rect = new Rect(pt1, pt2);

               if (theta >= 0) {
                   if(rect.width==0){
                       continue;
                   }
                   if(rect.height==0){
                       continue;
                   }
                   if(rect.height<10){
                       continue;
                   }
                   if(rect.width<5){
                       continue;
                   }
                   rects.add(rect);
               }
           }
           if(rects.size()>0){
               totalScore+=fractionMap.get(mat);
           }
           // Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\contours.jpg",mat);
       }
       return totalScore;
   }
    @Test
    public void test1(){
        Mat src = Imgcodecs.imread("D:\\work\\AnswerCard\\AnswerCard\\img\\2_1.jpg");
        Mat hsvMat = new Mat();
        src=src.submat(new Rect(new Point(88,972),new Point(1154,1007)));
        List<Mat> matList=new ArrayList<>();
        //分数 fraction
        Map<Mat,Integer> fractionMap=new LinkedHashMap<>(22);
        //平分22个mat
        int row=1;
        int col=22;
        //初始值
        int initial = 9;
        int xStep = src.cols()/col;
        int yStep =src.rows()/row;
        for (int i = 0; i < col; i++) {
            for (int j = 0; j < row; j++) {
                int x =  xStep*(i+1);
                int y = yStep*(j+1);
                Point p1 = new Point(x - xStep, y - yStep);
                Point p3 = new Point(x, y);
                Mat mat = src.submat(new Rect(p1, p3));
                matList.add(mat);
                if(initial==-1){
                    initial=9;
                }
                if(i==10 || i==21){
                    fractionMap.put(mat,99);
                }else{
                    fractionMap.put(mat,initial);
                    initial--;
                }

                Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\mat"+i+".jpg",src.submat(new Rect(p1,p3)));
            }

        }
        //总分
        int totalScore=0;
        for (Mat mat: matList) {
            Imgproc.cvtColor(mat, hsvMat, COLOR_BGR2HSV);
            Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(1, 1));
            //开操作 (去除一些噪点)
            Imgproc.morphologyEx(hsvMat, hsvMat, Imgproc.MORPH_OPEN, kernel);
            //闭操作 (连接一些连通域)
            Imgproc.morphologyEx(hsvMat, hsvMat, Imgproc.MORPH_CLOSE, kernel);
            Imgproc.GaussianBlur(hsvMat,hsvMat,kernel.size(),0,0);
            Imgproc.dilate(hsvMat, hsvMat, kernel, new Point(-1, -1), 1, 2, new Scalar(1));
            Imgproc.erode(hsvMat, hsvMat, kernel, new Point(-1, -1), 1, 2, new Scalar(1));
            double min = 0;
            double max = 200;
            Mat des = new Mat();
            Core.inRange(hsvMat, new Scalar(min, 90, 90), new Scalar(max, 255, 255), des);
            //开操作 (去除一些噪点)
            Imgproc.morphologyEx(des, des, Imgproc.MORPH_OPEN, kernel);
            //闭操作 (连接一些连通域)
            Imgproc.morphologyEx(des, des, Imgproc.MORPH_CLOSE, kernel);
            Imgproc.GaussianBlur(des,des,new Size(3,3),0,0);
            Imgproc.Canny(des,des,3,9,3);
            List<MatOfPoint> contours=new ArrayList<>();
            Mat hierarchy=new Mat();
            Imgproc.findContours(des,contours,hierarchy,RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
            System.out.println(contours.size());
            for (MatOfPoint matOfPoint:contours) {
                Rect rect = Imgproc.boundingRect(matOfPoint);
                Imgproc.rectangle(mat,rect.tl(),rect.br(),new Scalar(0,255,0),1,8);
            }
            if(contours.size()>0){
                totalScore+=fractionMap.get(mat);
            }

            Imgcodecs.imwrite("D:\\work\\AnswerCard\\AnswerCard\\img\\HSV"+matList.indexOf(mat)+".jpg",mat);
        }
        System.out.println("totalScore="+totalScore);
    }

    @Test
    public void test14(){
        Mat src = Imgcodecs.imread("D:\\work\\AnswerCard\\AnswerCard\\img\\mat15.jpg");
        redColor(src);
    }

    public boolean redColor(Mat mat){
        Mat hsv_image = new Mat();
             Imgproc.cvtColor(mat,hsv_image,COLOR_BGR2HSV);
        int num_rows = hsv_image.rows();
        int num_col = hsv_image.cols();
        for (int i = 0; i < num_rows; i++) {
            for (int j = 0; j < num_col; j++) {
                // 获取每个像素
                double[] clone = hsv_image.get(i, j).clone();
                double hun = clone[0];
                // HSV hun
                if ((hun >= 0 && hun < 34) || (hun > 160 && hun < 200)) {
                    if (clone[1] > 25 && clone[1] < 240) {
                        if (clone[2] < 200 && clone[2] > 90) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
