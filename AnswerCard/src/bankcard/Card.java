package bankcard;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;


/**
 * @author
 * @email
 */
public class Card {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) {
        // ��ȡͼ�񣬲���Ϊͼ��Ĵ洢·��
        Mat source = Imgcodecs.imread("D://bankcard.jpg");

        runCanny(source);

    }


    private static void runCanny(Mat srcImage) {

        // ��������
        // Mat dstImage = new Mat();
        Mat edge = new Mat();


        // ��ԭͼת���ɻҶ�ͼ
        Imgproc.cvtColor(srcImage, edge, Imgproc.COLOR_BGR2GRAY);

        // ʹ��3x3�ں�������
        Imgproc.blur(edge, edge, new Size(3, 3));

        // ����Canny����
        Imgproc.threshold(edge, edge, 30, 255, Imgproc.THRESH_BINARY);

        // 3*3ͼ��ʴ
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(3, 3));
        Imgproc.erode(edge, edge, element);

        //���½�
        int a = 0;
        int b = 0;

        int state = 0;

        for (int y = 0; y < edge.height(); y++) {
            // ��ֵ
            int count = 0;

            for (int x = 0; x < edge.width(); x++) {
                // �õ��������ص��ֵ
                byte[] data = new byte[1];
                edge.get(y, x, data);
                if (data[0] == 0){
                    count = count + 1;
                }

            }
            // ��δ����Ч��
            if (state == 0) {
                // �ҵ�����Ч��
                if (count >= 150) {
                    // ��Ч������ʮ�����ص������
                    a = y;
                    state = 1;
                }
            } else if (state == 1) {
                // �ҵ�����Ч��
                if (count <= 150) {

                    // ��Ч������ʮ�����ص������
                    b = y;
                    state = 2;
                }
            }
        }
        System.out.println("�����½�" + Integer.toString(a));
        System.out.println("�����Ͻ�" + Integer.toString(b));

        // ����,����X,����Y,��ͼ���,��ͼ����
        Rect roi = new Rect(0, a, edge.width(), b - a);

        Mat roi_img = new Mat(edge, roi);
        Mat tmp_img = new Mat();

        roi_img.copyTo(tmp_img);

        System.out.println(Imgcodecs.imwrite("D://blackto.jpg", roi_img));

    }
}
