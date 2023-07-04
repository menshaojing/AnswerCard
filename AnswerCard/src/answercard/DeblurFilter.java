package answercard;

/**
 * @ClassName DeblurFilter
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/6/7 11:14
 * @Version 1.0
 */

    import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import java.util.ArrayList;
import java.util.List;

    class DeblurFilter{
        public void run(String[] args){
            String strInFileName = "../data/lena.jpg";
            int snr = 5200;
            int R = 53;
            Mat imgIn = new Mat();
            imgIn = Imgcodecs.imread(strInFileName);
            if(imgIn.empty()){
                System.out.println("ERROR : Image cannot be loaded..!!");
                System.exit(-1);
            }
            Mat imgOut = new Mat();
            Rect roi = new Rect(0,0,imgIn.cols()&-2,imgIn.rows()&-2);

            Mat Hw = new Mat(), h = new Mat();
            calcPSF(h, roi.size(), R);
            calcWnrFilter(h, Hw, 1.0/snr);
            filter2DFreq(new Mat(imgIn, roi), imgOut, Hw);
            imgOut.convertTo(imgOut, CvType.CV_8U);
            Core.normalize(imgOut, imgOut, 0, 255,Core.NORM_MINMAX);
            HighGui.imshow("Original",imgIn);
            HighGui.imshow("Deblurred",imgOut);

        }

        void calcPSF(Mat outputImg, Size filterSize, int R){
            Mat h = new Mat(filterSize, CvType.CV_32F, new Scalar(0));
            Point point = new Point(filterSize.width / 2, filterSize.height / 2);
            Imgproc.circle(h, point, R, new Scalar(255), -1, 8);
            Scalar summa = Core.sumElems(h);
            Core.divide(h, summa, outputImg);

        }

        void fftshift( Mat inputImg, Mat outputImg){
            outputImg = inputImg.clone();
            int cx = outputImg.cols() / 2;
            int cy = outputImg.rows() / 2;
            Mat q0 = new Mat(outputImg, new Rect(0, 0, cx, cy));
            Mat q1 = new Mat(outputImg, new Rect(cx, 0, cx, cy));
            Mat q2 = new Mat(outputImg, new Rect(0, cy, cx, cy));
            Mat q3 = new Mat(outputImg, new Rect(cx, cy, cx, cy));

            Mat tmp = new Mat();
            q0.copyTo(tmp);
            q3.copyTo(q0);
            tmp.copyTo(q3);
            q1.copyTo(tmp);
            q2.copyTo(q1);
            tmp.copyTo(q2);
        }

        void filter2DFreq(Mat inputImg, Mat outputImg,  Mat H){

            List<Mat> planes = new ArrayList<Mat>();
            inputImg.convertTo(inputImg, CvType.CV_32F);
            planes.add(inputImg);
            planes.add(Mat.zeros(inputImg.size(), CvType.CV_32F));
            Mat complexI = new Mat();
            Core.merge(planes, complexI);
            Core.dft(complexI, complexI, Core.DFT_SCALE);
            // System.out.println(planes.size());

            List<Mat>planesH = new ArrayList<Mat>();
            H.convertTo(H, CvType.CV_32F);
            planesH.add(H);
            planesH.add(Mat.zeros(H.size(), CvType.CV_32F));
            Mat complexH = new Mat(), complexIH = new Mat();
            Core.merge(planesH, complexH);
            Core.mulSpectrums(complexI, complexH, complexIH, 0);

            Core.idft(complexIH, complexIH);
            Core.split(complexIH, planes);
            outputImg = planes.get(0);

        }

        void calcWnrFilter( Mat input_h_PSF, Mat output_G, double nsr) {
            Mat h_PSF_shifted = new Mat();
            fftshift(input_h_PSF, h_PSF_shifted);
            //System.out.println(h_PSF_shifted.size().height);
            ArrayList<Mat> planes = new ArrayList<Mat>();
            h_PSF_shifted.convertTo(h_PSF_shifted, CvType.CV_32F);
            planes.add(h_PSF_shifted);
            planes.add(Mat.zeros(h_PSF_shifted.size(), CvType.CV_32F));
        }
}
