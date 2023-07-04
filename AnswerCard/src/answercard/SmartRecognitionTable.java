package answercard;


import org.junit.Test;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.opencv.imgproc.Imgproc.RETR_LIST;

/**
 * @Author : alexliu
 * @Description : opencv 测试
 * @Date : Create at 下午3:12 2018/1/26
 */
public class SmartRecognitionTable {

    String test_file_path = System.getProperty("user.dir") + File.separator + "img"+File.separator;
    List<Rect> rectList = new ArrayList<Rect>();
    static {
        //加载动态链接库时，不使用System.loadLibrary(xxx);。 而是使用 绝对路径加载：System.load(xxx);

        /*
         * 加载动态库
         *
         * 第一种方式 --------------System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
         * loadLibrary(Core.NATIVE_LIBRARY_NAME); //使用这种方式加载，需要在 IDE 中配置参数.
         * Eclipse 配置：http://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html#set-up-opencv-for-java-in-eclipse
         * IDEA 配置 ：http://opencv-java-tutorials.readthedocs.io/en/latest/01-installing-opencv-for-java.html#set-up-opencv-for-java-in-other-ides-experimental
         *
         * 第二种方式 --------------System.load(path of lib);
         * System.load(your path of lib) ,方式比较灵活，可根据环境的系统，位数，决定加载内容
         */
       // loadLibraries();
    	System.out.println("Welcome to OpenCV VH " + Core.VERSION);
    	//String root=System.getProperty("user.dir");
       // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        System.load("D:\\work\\AnswerCard\\AnswerCard\\libs\\x64\\opencv_java450.dll");
    }
     public static void main(String[] args)
     {
    	 SmartRecognitionTable  srtable=new SmartRecognitionTable();
         List<Rect> list = srtable.readTable();
         list=  getContainRect(new Rect((int)96,(int)744,(int)198,(int)166),list);
         List<Integer> xList=new ArrayList();
         List<Integer> yList=new ArrayList();
         list.forEach(rect -> {
             xList.add(rect.x);
             xList.add(rect.x+ rect.width);
             yList.add(rect.y);
             yList.add(rect.y+ rect.height);
         });
         //获取最大X,Y 和最小 x,Y
         int maxX = xList.stream().max(Integer::compareTo).get();
         int maxY = yList.stream().max(Integer::compareTo).get();
         int minX = xList.stream().min(Integer::compareTo).get();
         int minY = yList.stream().min(Integer::compareTo).get();
         Mat source_image = Imgcodecs.imread("D:\\image\\test\\1.jpg");
         Mat clone = source_image.clone();
         //获取准考证号范围矩形
         Rect maxRect = new Rect( new Point(minX,minY),new Point(maxX,maxY));

         Imgproc.rectangle(source_image, maxRect.tl(), maxRect.br(), new Scalar(0, 255, 0), 1, 8, 0);
         Mat maxRectMat = source_image.submat(maxRect);
         Imgcodecs.imwrite("D:\\image\\card\\maxRect.jpg",source_image);
         Imgcodecs.imwrite("D:\\image\\card\\maxRectMat.jpg",maxRectMat);
         Mat maxMat = source_image.submat(maxRect);
         //网格化
         int row = 5;
         int col = 4;
         int xStep = maxMat.cols()/col;
         int yStep =maxMat.rows()/row;
         for (int i = 0; i < col; i++) {
             Imgproc.line(maxMat,new Point(xStep*i,0),new Point(xStep*i,source_image.rows()),new Scalar(0, 255, 0));
         }
         for (int i = 0; i < row; i++) {
             Imgproc.line(maxMat,new Point(0,i*yStep),new Point(source_image.cols(),i*yStep),new Scalar(0, 255, 0));

         }
         List<Rect> resultList=new ArrayList<>();
         for (int i = 0; i < col; i++) {
             for (int j = 0; j < row; j++) {
                 int x =  xStep*(i+1);
                 int y = yStep*(j+1);
                 Point p1 = new Point((x - xStep)+maxRect.x, (y - yStep)+maxRect.y);
                 //  Point p2 = new Point(x, y - yStep);
                 Point p3 = new Point(x+maxRect.x, y+maxRect.y);
                 // Point p4 = new Point(x - xStep, y);
                 resultList.add(new Rect(p1,p3));
                 Imgproc.rectangle(clone, new Rect(p1,p3).tl(), new Rect(p1,p3).br(), new Scalar(255, 0, 0), 1, 8, 0);
             }

         }
         Imgcodecs.imwrite("D:\\image\\card\\line2.jpg",clone);

         resultList=resultList.stream().sorted(Comparator.comparing(rect -> {
             return  rect.y;
         })).collect(Collectors.toList());
         System.out.println(resultList);
         Imgcodecs.imwrite("D:\\image\\card\\line.jpg",maxMat);

     }
    public Rect getMaxRect(String fileUrl){
        Mat imgIn = Imgcodecs.imread(fileUrl);
        Mat grayImg = new Mat();
        Mat maxMat=new Mat();
        Imgproc.resize(imgIn,maxMat,new Size(imgIn.width(),imgIn.height()));
        Imgproc.cvtColor(maxMat, grayImg, Imgproc.COLOR_RGB2GRAY);
        // C 负数，取反色，超过阈值的为黑色，其他为白色
        Imgproc.adaptiveThreshold(grayImg, grayImg,255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,7,-2);

        //形态学变化，确保目标区别都是连在一起的
        Mat element = Imgproc.getStructuringElement(RETR_LIST, new Size(3, 3));
        Imgproc.morphologyEx(grayImg,grayImg,Imgproc.MORPH_CLOSE,element);

        Mat cannyImg = new Mat();
        int ksize =3;
        double sigma = 0.3 * ((ksize - 1) * 0.5 - 1) + 0.8;
        Mat gaussianKernel = Imgproc.getGaussianKernel(ksize, sigma);
        Imgproc.GaussianBlur(grayImg, grayImg, gaussianKernel.size(), 0, 0);
        Imgproc.Canny(grayImg, cannyImg, 35, 35*3, 3);



        Imgcodecs.imwrite("D:\\image\\test1\\cannyImg.png",cannyImg);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.RETR_LIST, new Size(3, 3));
        Imgproc.dilate(cannyImg, cannyImg, kernel, new Point(-1, -1), 98, 1, new Scalar(1));
        Imgcodecs.imwrite("D:\\image\\test1\\dilate.png",cannyImg);
        Imgproc.erode(cannyImg, cannyImg, kernel, new Point(-1, -1), 10, 1, new Scalar(1));
        Imgcodecs.imwrite("D:\\image\\test1\\erode.png",cannyImg);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyImg, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
        // 找出轮廓对应凸包的四边形拟合
        List<MatOfPoint> squares = new ArrayList<>();
        List<MatOfPoint> hulls = new ArrayList<>();
        MatOfInt hull = new MatOfInt();
        MatOfPoint2f approx = new MatOfPoint2f();
        approx.convertTo(approx, CvType.CV_32F);
        for (MatOfPoint contour : contours) {
            // 边框的凸包
            Imgproc.convexHull(contour, hull);

            // 用凸包计算出新的轮廓点
            Point[] contourPoints = contour.toArray();
            int[] indices = hull.toArray();
            List<Point> newPoints = new ArrayList<>();
            for (int index : indices) {
                newPoints.add(contourPoints[index]);
            }
            MatOfPoint2f contourHull = new MatOfPoint2f();
            contourHull.fromList(newPoints);

            // 多边形拟合凸包边框(此时的拟合的精度较低)
            Imgproc.approxPolyDP(contourHull, approx, Imgproc.arcLength(contourHull, true) * 0.1, true);

            // 筛选出面积大于某一阈值的，且四边形的各个角度都接近直角的凸四边形
            MatOfPoint approxf1 = new MatOfPoint();
            approx.convertTo(approxf1, CvType.CV_32S);
            if (approx.rows() == 4 && Imgproc.isContourConvex(approxf1)) {
                MatOfPoint tmp = new MatOfPoint();
                contourHull.convertTo(tmp, CvType.CV_32S);
                squares.add(approxf1);
                hulls.add(tmp);
            }
        }

        Imgproc.drawContours(maxMat, squares, -1, new Scalar(0, 0, 205), 2);
        Imgcodecs.imwrite("D:\\image\\test1\\imgIn.jpg",maxMat);
        MatOfPoint matOf = squares.stream().max(Comparator.comparing(matOfPoint -> {
            Rect rect = Imgproc.boundingRect(matOfPoint);
            return rect.area();
        })).get();
        return Imgproc.boundingRect(matOf);
    }
     //取包含数据
     public static List<Rect>   getContainRect(Rect a,List<Rect> list){
         List<Rect> listRect=new ArrayList<>();
         for (Rect rect:list) {
             boolean flag = containRectangle(a, rect);
             if(flag){
                 listRect.add(rect);
             }
         }
         return listRect;
    }
    /**
     * @Description 两矩形是否相交
     * @param a 矩形
     * @param b 目标矩形
     * @throws
     * @return boolean
     * @Author menshaojing
     * @Date  2021/6/28  14:13
     **/
    public  boolean isRectIntersect(Rect a, Rect b)
    {
        int x01=a.x;
        int x02=a.x+a.width;
        int y01=a.y;
        int y02=a.y+a.height;
        int x11=b.x;
        int x12=b.x+b.width;
        int y11=b.y;
        int y12=b.y+b.height;
        int zx = Math.abs(x01 + x02 -x11 - x12);
        int x  = Math.abs(x01 - x02) + Math.abs(x11 - x12);
        int zy = Math.abs(y01 + y02 - y11 - y12);
        int y  = Math.abs(y01 - y02) + Math.abs(y11 - y12);
        if(zx <= x && zy <= y)
        { //去除相交的
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * @Description 两矩形是否包含
     * @param a  矩形
     * @param b 目标矩形
     * @throws
     * @return boolean
     * @Author menshaojing
     * @Date  2021/7/7  8:51
     **/
    public static boolean  containRectangle(Rect a, Rect b){
        Point pointA0=new Point(a.x,a.y);
        Point pointA1=new Point(a.x+a.width,a.y);
        Point pointA2=new Point(a.x+a.width,a.y+a.height);
        Point pointA3=new Point(a.x,a.y+a.height);
        Point pointB0 = new Point(b.x, b.y);
        Point pointB1 = new Point(b.x+b.width, b.y);
        Point pointB2 = new Point(b.x+b.width, b.y+b.height);
        Point pointB3 = new Point(b.x, b.y+b.height);
        if(isPointInMatrix(pointA0,pointA1,pointA2,pointA3,pointB0) &&
                isPointInMatrix(pointA0,pointA1,pointA2,pointA3,pointB1) &&
                isPointInMatrix(pointA0,pointA1,pointA2,pointA3,pointB2) &&
                isPointInMatrix(pointA0,pointA1,pointA2,pointA3,pointB3)
        ){
            return true;
        }
        return false;
    }
    /**
     * @Description 判断点p是否在p1p2p3p4的正方形内
     * @Date  2021/6/2  11:25
     * @param p1 矩形四角第一个点位
     * @param p2 矩形四角第二个点位
     * @param p3 矩形四角第三个点位
     * @param p4 矩形四角第四个点位
     * @param p  判断点位
     * @throws
     * @return boolean
     * @Author menshaojing
     * @Date  2021/6/2  11:25
     **/
    static boolean  isPointInMatrix( Point p1,  Point p2,  Point p3, Point p4,  Point p) {
        boolean isPointIn = getCross(p1, p2, p) * getCross(p3, p4, p) >= 0 && getCross(p2, p3, p) * getCross(p4, p1, p) >= 0;
        return isPointIn;
    }
    /**
     * @Description  计算 |p1 p2| X |p1 p|
     * @Date  2021/6/2  11:25
     * @param p1
     * @param p2
     * @param p
     * @throws
     * @return double
     * @Author menshaojing
     * @Date  2021/6/2  11:25
     **/
    public static double getCross( Point p1, Point p2,  Point p) {
        return (p2.x - p1.x) * (p.y - p1.y) - (p.x - p1.x) * (p2.y - p1.y);
    }

    @Test
    public void test(){
        readTable();
    }
    /**
     * 读取 table
     */
    public  List<Rect>  readTable(){

        Mat source_image = Imgcodecs.imread("D:\\work\\AnswerCard\\img\\0802\\dst.png");
        //灰度处理
        Mat gray_image = new Mat();
        Imgproc.cvtColor(source_image,gray_image,Imgproc.COLOR_RGB2GRAY);

        //二值化
        Mat thresh_image = new Mat(source_image.height(), source_image.width(), CvType.CV_8UC1);
        // C 负数，取反色，超过阈值的为黑色，其他为白色
        Imgproc.adaptiveThreshold(gray_image, thresh_image,255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,3,-1);
       // this.saveImage("out-table/1-thresh.png",thresh_image);

        //克隆一个 Mat，用于提取水平线
        Mat horizontal_image = thresh_image.clone();

        //克隆一个 Mat，用于提取垂直线
        Mat vertical_image = thresh_image.clone();

        /*
         * 求水平线
         * 1. 根据页面的列数（可以理解为宽度），将页面化成若干的扫描区域
         * 2. 根据扫描区域的宽度，创建一根水平线
         * 3. 通过腐蚀、膨胀，将满足条件的区域，用水平线勾画出来
         *
         * scale 越大，识别的线越多，因为，越大，页面划定的区域越小，在腐蚀后，多行文字会形成一个块，那么就会有一条线
         * 在识别表格时，我们可以理解线是从页面左边 到 页面右边的，那么划定的区域越小，满足的条件越少，线条也更准确
         */
        int scale = horizontal_image.cols();
        int horizontalsize = horizontal_image.cols() / scale;
        // 为了获取横向的表格线，设置腐蚀和膨胀的操作区域为一个比较大的横向直条
        Mat horizontalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(horizontalsize, 1));
        // 先腐蚀再膨胀 new Point(-1, -1) 以中心原点开始
        // iterations 最后一个参数，迭代次数，越多，线越多。在页面清晰的情况下1次即可。
        Imgproc.erode(horizontal_image, horizontal_image, horizontalStructure, new Point(-1, -1),1);
        Imgproc.dilate(horizontal_image, horizontal_image, horizontalStructure, new Point(-1, -1),1);
        //this.saveImage("out-table/2-horizontal.png",horizontal_image);

        // 求垂直线
        scale = vertical_image.rows();
        int verticalsize = vertical_image.rows() / scale;
        Mat verticalStructure = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, verticalsize));
        Imgproc.erode(vertical_image, vertical_image, verticalStructure, new Point(-1, -1),1);
        Imgproc.dilate(vertical_image, vertical_image, verticalStructure, new Point(-1, -1),1);
       // this.saveImage("out-table/3-vertical.png",vertical_image);

        /*
         * 合并线条
         * 将垂直线，水平线合并为一张图
         */
        Mat mask_image = new Mat();
        Core.add(horizontal_image,vertical_image,mask_image);
       // this.saveImage("out-table/4-mask.png",mask_image);

        /*
         * 通过 bitwise_and 定位横线、垂直线交汇的点
         */
        Mat points_image = new Mat();
        Core.bitwise_and(horizontal_image, vertical_image, points_image);
        //this.saveImage("out-table/5-points.png",points_image);

        /*
         * 通过 findContours 找轮廓
         *
         * 第一个参数，是输入图像，图像的格式是8位单通道的图像，并且被解析为二值图像（即图中的所有非零像素之间都是相等的）。
         * 第二个参数，是一个 MatOfPoint 数组，在多数实际的操作中即是STL vectors的STL vector，这里将使用找到的轮廓的列表进行填充（即，这将是一个contours的vector,其中contours[i]表示一个特定的轮廓，这样，contours[i][j]将表示contour[i]的一个特定的端点）。
         * 第三个参数，hierarchy，这个参数可以指定，也可以不指定。如果指定的话，输出hierarchy，将会描述输出轮廓树的结构信息。0号元素表示下一个轮廓（同一层级）；1号元素表示前一个轮廓（同一层级）；2号元素表示第一个子轮廓（下一层级）；3号元素表示父轮廓（上一层级）
         * 第四个参数，轮廓的模式，将会告诉OpenCV你想用何种方式来对轮廓进行提取，有四个可选的值：
         *      RETR_EXTERNAL （0）：表示只提取最外面的轮廓；
         *      RETR_LIST （1）：表示提取所有轮廓并将其放入列表；
         *      RETR_CCOMP （2）:表示提取所有轮廓并将组织成一个两层结构，其中顶层轮廓是外部轮廓，第二层轮廓是“洞”的轮廓；
         *      RETR_TREE （3）：表示提取所有轮廓并组织成轮廓嵌套的完整层级结构。
         * 第五个参数，见识方法，即轮廓如何呈现的方法，有三种可选的方法：
         *      CHAIN_APPROX_NONE （1）：将轮廓中的所有点的编码转换成点；
         *      CHAIN_APPROX_SIMPLE （2）：压缩水平、垂直和对角直线段，仅保留它们的端点；
         *      CHAIN_APPROX_TC89_L1  （3）or CV_CHAIN_APPROX_TC89_KCOS（4）：应用Teh-Chin链近似算法中的一种风格
         * 第六个参数，偏移，可选，如果是定，那么返回的轮廓中的所有点均作指定量的偏移
         */

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(mask_image,contours,hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_L1,new Point(0,0));


        List<MatOfPoint> contours_poly = contours;
        Rect[] boundRect = new Rect[contours.size()];

        LinkedList<Mat> tables = new LinkedList<Mat>();
        //START去重，主要是1)包含在外层矩形中，且面积大于500  
        // 2)包含在内层的小矩形
        //没有效果，抛弃，建议从结果集中获取

        List<MatOfPoint> myContours = new ArrayList<MatOfPoint>();
        List<MatOfPoint> smallContours = new ArrayList<MatOfPoint>();

        List<MatOfPoint> repContours = new ArrayList<MatOfPoint>();;
        List<RectComp> RectCompList = new ArrayList<>();
        List<RectComp> smallCompList = new ArrayList<>();
        List<MatOfPoint> repSmallContours = new ArrayList<MatOfPoint>();;
        for (int i = 0; i < contours.size(); i++) {
            Rect rm = Imgproc.boundingRect(contours.get(i));
            RectComp ti = new RectComp(rm);
            //把轮廓宽度区间在 50 - 80 范围内的轮廓装进矩形集合
            if (ti.rm.width > 20 ) {
                RectCompList.add(ti);
                myContours.add(contours.get(i));
            }else {
            	smallContours.add(contours.get(i));
            	smallCompList.add(ti);
            }
        }

        List<RectComp> aList=RectCompList;
        for(int i=0;i<RectCompList.size();i++)
        {
        	RectComp rc=RectCompList.get(i);
        	
        	for(int j=i+1;j<aList.size();j++)
        	{
        		RectComp point = aList.get(j);
        		if(rc.rm.x<point.rm.x&&rc.rm.y<point.rm.y)
        		{
        			if(rc.rm.contains(new Point(point.rm.x,point.rm.y)))
        			if(rc.rm.width>=point.rm.width&&rc.rm.height>=point.rm.height)
        			{
        				repContours.add(myContours.get(j));
        			}
        		}
        	}
        }
        for(int i=0;i<repContours.size();i++)
        {
        	contours.remove(repContours.get(i));
        }
        //小面积去重
        aList=smallCompList;
        for(int i=0;i<smallCompList.size();i++)
        {
        	RectComp rc=smallCompList.get(i);
        	
        	for(int j=i+1;j<aList.size();j++)
        	{
        		RectComp point = aList.get(j);
        		if(rc.rm.x<point.rm.x&&rc.rm.y<point.rm.y)
        		{
        			if(rc.rm.contains(new Point(point.rm.x,point.rm.y)))
        			if(rc.rm.width>=point.rm.width&&rc.rm.height>=point.rm.height)
        			{
        				repSmallContours.add(smallContours.get(j));
        			}
        		}
        	}
        }
        for(int i=0;i<repSmallContours.size();i++)
        {
        	contours.remove(repSmallContours.get(i));
        }
        //
        //END去重
       //循环所有找到的轮廓-点
        for(int i=0 ; i< contours.size(); i++){

            MatOfPoint point = contours.get(i);
            MatOfPoint contours_poly_point = contours_poly.get(i);

            /*
             * 获取区域的面积
             * 第一个参数，InputArray contour：输入的点，一般是图像的轮廓点
             * 第二个参数，bool oriented = false:表示某一个方向上轮廓的的面积值，顺时针或者逆时针，一般选择默认false
             */
            double area = Imgproc.contourArea(contours.get(i));
            //如果小于某个值就忽略，代表是杂线不是表格
            //特表注意宽不小于18 ，高不小于18 面积不小于324，为了兼容性在此设置为324
            //原因：最大字体为四号 四号字体宽高为18px
            //单位:  像素
            //目的：去掉对字母的选择框
            if(area < 325){
                continue;
            }

            /*
             * approxPolyDP 函数用来逼近区域成为一个形状，true值表示产生的区域为闭合区域。比如一个带点幅度的曲线，变成折线
             *
             * MatOfPoint2f curve：像素点的数组数据。
             * MatOfPoint2f approxCurve：输出像素点转换后数组数据。
             * double epsilon：判断点到相对应的line segment 的距离的阈值。（距离大于此阈值则舍弃，小于此阈值则保留，epsilon越小，折线的形状越“接近”曲线。）
             * bool closed：曲线是否闭合的标志位。
             */
            Imgproc.approxPolyDP(new MatOfPoint2f(point.toArray()),new MatOfPoint2f(contours_poly_point.toArray()),3,true);

            //为将这片区域转化为矩形，此矩形包含输入的形状
            boundRect[i] = Imgproc.boundingRect(contours_poly.get(i));

            // 找到交汇处的的表区域对象
            Mat table_image = points_image.submat(boundRect[i]);

            List<MatOfPoint> table_contours = new ArrayList<MatOfPoint>();
            Mat joint_mat = new Mat();
            Imgproc.findContours(table_image, table_contours,joint_mat, Imgproc.RETR_CCOMP, Imgproc.CHAIN_APPROX_SIMPLE);
            //从表格的特性看，如果这片区域的点数小于4，那就代表没有一个完整的表格，忽略掉
           /* if (table_contours.size() < 4)
                continue;*/

            RotatedRect rect = Imgproc.minAreaRect(new MatOfPoint2f(contours.get(i).toArray()));
            //计算高和宽
            int m_width = rect.boundingRect().width;
            int m_height = rect.boundingRect().height;
            if(m_width>300||m_height>300)
            {
            	 continue;
            }
            //保存图片
            tables.addFirst(source_image.submat(boundRect[i]).clone());

            //将矩形画在原图上
            Imgproc.rectangle(source_image, boundRect[i].tl(), boundRect[i].br(), new Scalar(0, 255, 0), 1, 8, 0);
            rectList.add(boundRect[i]);
        }


        Collections.sort(rectList,new Comparator<Rect>() {
			@Override
			public int compare(Rect a, Rect b) {
				// TODO Auto-generated method stub
			    if(a.x<b.x) return -1;
			    if(a.x==b.x) return 0;
			    if(a.x>b.x)return 1;
				return 0;
			}
        });

        for(int i=0;i<rectList.size();i++)
        {
        	if(rectList.get(i).y>460) {
        	System.out.println(rectList.get(i));
        	}
        }
        this.saveImage("out-table/7-source.png",source_image);
        return rectList;
    }

    private void saveImage(String path,Mat image){

        String outPath = this.test_file_path + File.separator + path;

        File file = new File(outPath);
        //目录是否存在
        this.dirIsExist(file.getParent());

        Imgcodecs.imwrite(outPath,image);

    }

    private void dirIsExist(String dirPath){
        File dir = new File(dirPath);
        if(!dir.exists()){
            dir.mkdirs();
        }
    }
    /**
     * @param area--客观题总区域大小[非必要，扩展用]
     * @param NoVH--题号横竖排列[非必要，扩展用]
     * @param ItemVH--选项横竖排列[非必要，扩展用]
     * @param hasNo--序号[非必要，扩展用]
     * @param rows--行数[非必要，扩展用]
     * @param cols--列数[非必要，扩展用]
     * @param areas--客观题区域列表 * 必须填写项，
     * TODO:特别注意Areas 区域坐标不能包含第一行/第一列的题号列（无论横向和竖向排列）
     */
    public List<Rect>  FormatAreas(int noVH,int itemVH,int rows,int cols,List<Rect> areas)
    {
    	//1) 获取x最小，最大值
    	//2）获取Y的最小，最大值
    	//3）计算两行答案垂直方向上间距的最小值
    	//4）根据排列方向自动计算每一个题号和答案区域坐标
    	int minX=0,minY=0,maxX=0,maxY=0;
    	int gapV=0,gapH=0;//水平宽度和垂直高度的最小值
    	int width=0;//一道题的宽度计算
    	if(areas.size()>1) {
    		minX=areas.get(0).x;
    		minY=areas.get(0).y;
    		maxX=minX;
    		maxY=minY;
    		gapV=areas.get(0).height;
    		gapH=areas.get(0).width;
	    	for(int i=1;i<areas.size();i++)
	    	{
	    		Rect rect=areas.get(i);
	    		if(rect.x<minX)
	    			minX=rect.x;
	    		if(rect.x>maxX)
	    			maxX=rect.x;
	    		if(rect.y<minY)
	    			minY=rect.x;
	    		if(rect.y>maxY)
	    			maxY=rect.y;
	    		if(rect.width<gapH)
	    			gapH=rect.width;
	    	} 
	    	//横向拆分--保证列数符合要求
	    	List<Rect> delWList=new ArrayList<Rect>();
	    	List<Rect> addWList=new ArrayList<Rect>();
	    	for(int i=0;i<areas.size();i++)
	    	{
	    		Rect rect=areas.get(i);
	    		int num=rect.width%gapH;
	    		if(num>1)
	    		{
	    			delWList.add(rect);
	    			int gap=rect.width/num;
	    			for(int j=0;j<num;j++)
	    			{
	    				Rect rec=new Rect();
	    				rec.x=rect.x+j*gap;
	    				rec.y=rect.y;
	    				rec.width=gap;
	    				rec.height=rect.height;
	    				addWList.add(rec);
	    			}
	    		}
	    	}
	    	for(int i=0;i<delWList.size();i++)
	    		areas.remove(delWList.get(i));
	    	for(int i=0;i<addWList.size();i++)
	    		areas.add(addWList.get(i));
	    	//横向拆分结束
	    	//竖向拆分--保证行数符合要求
	    	for(int i=1;i<areas.size();i++)
	    	{
	    		Rect rect=areas.get(i);
	    		if(rect.y>maxY) {
	    			i=areas.size();
	    		}else {
	    			if(rect.height<gapV)
	    				gapV=rect.height;
	    		}
	    	}
	    	
	    	List<Rect> delList=new ArrayList<Rect>();
	    	List<Rect> addList=new ArrayList<Rect>();
	    	for(int i=0;i<areas.size();i++)
	    	{
	    		Rect rect=areas.get(i);
	    		int num=rect.height%gapV;
	    		if(num>1)
	    		{
	    			delList.add(rect);
	    			int gap=rect.height/num;
	    			for(int j=0;j<num;j++)
	    			{
	    				Rect rec=new Rect();
	    				rec.x=rect.x;
	    				rec.y=(rect.y+j*gap-1);
	    				rec.height=gap;
	    				rect.width=rec.width;
	    				addList.add(rec);
	    			}
	    		}
	    	}
	    	for(int i=0;i<delList.size();i++)
	    		areas.remove(delList.get(i));
	    	for(int i=0;i<addList.size();i++)
	    		areas.add(addList.get(i));
	    	//竖向拆分结束，按照X排序
    	
	    	Collections.sort(areas,new Comparator<Rect>() {
	  			@Override
	  			public int compare(Rect a, Rect b) {
	  				// TODO Auto-generated method stub
	  			    if(a.x<b.x) return -1;
	  			    if(a.x==b.x) return 0;
	  			    if(a.x>b.x)return 1;
	  				return 0;
	  			}
	          });
    	}
    	return areas;
    }
    //返回每个答案对应的中心点坐标
    public List<Point> getAreaCenter(List<Rect> areas)
    {
    	List<Point> centList=new ArrayList<Point>();
    	for(int i=0;i<areas.size();i++) {
    		Rect rect=areas.get(i);
    		centList.add(new Point(rect.x+rect.width/2,rect.y+rect.height));
    	}
    	return centList;
    }
    /**
     * 加载动态库
     */
    private static void loadLibraries() {

        try {
            String osName = System.getProperty("os.name");
            String opencvpath = System.getProperty("user.dir");

            //windows
            if(osName.startsWith("Windows")) {
                int bitness = Integer.parseInt(System.getProperty("sun.arch.data.model"));
                //32位系统
                if(bitness == 32) {
                    opencvpath=opencvpath+"\\opencv\\x86\\Your path to .dll";
                }
                //64位系统
                else if (bitness == 64) {
                    opencvpath=opencvpath+"\\opencv\\x64\\Your path to .dll";
                } else {
                    opencvpath=opencvpath+"\\opencv\\x86\\Your path to .dll";
                }
            }
            // mac os
            else if(osName.equals("Mac OS X")){
                opencvpath = "/usr/local/Cellar/opencv/3.4.0_1/share/OpenCV/java/libopencv_java340.dylib";
            }
            System.out.println(opencvpath);
            System.load(opencvpath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load opencv native library", e);
        }
    }
}
