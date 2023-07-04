package answercard;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGEncodeParam;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @ClassName Test20
 * @Description TODO
 * @Author menshaojing
 * @Date 2021/5/26 15:52
 * @Version 1.0
 */
public class DPIHandleHelper {
    private static int DPI = 300;

    public static void main(String[] args) {
        String path = "D:\\image\\mat.jpg";

        File file = new File(path);

        handleDpi(file, 300, 300);

    }

    /**

     * 改变图片DPI

     *  处理

     * @param file

     * @param xDensity

     * @param yDensity

     */

    public static void handleDpi(File file, int xDensity, int yDensity) {
        try {
            BufferedImage image = ImageIO.read(file);

            JPEGImageEncoder jpegEncoder = JPEGCodec.createJPEGEncoder(new FileOutputStream(file));

            JPEGEncodeParam jpegEncodeParam = jpegEncoder.getDefaultJPEGEncodeParam(image);

            jpegEncodeParam.setDensityUnit(JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);

            jpegEncoder.setJPEGEncodeParam(jpegEncodeParam);

            jpegEncodeParam.setQuality(0.75f, false);

            jpegEncodeParam.setXDensity(xDensity);

            jpegEncodeParam.setYDensity(yDensity);

            jpegEncoder.encode(image, jpegEncodeParam);

            image.flush();

        } catch (IOException e) {
            e.printStackTrace();

        }

    }

}
