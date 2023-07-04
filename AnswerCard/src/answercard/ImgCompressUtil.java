package answercard;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.awt.*;
import com.sun.imageio.plugins.jpeg.*;
import java.awt.image.BufferedImage;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

import org.w3c.dom.Element;


/**
 * @author zhouqz
 */
public class ImgCompressUtil {

    /**
     * ͼƬѹ������
     * @param args
     */
    public static void main(String args[]){
        //���ļ��������ļ�����ѹ������
//        String f = "/var/upload_bak/";
//        File file = new File(f);
//
//        if(file.exists())
//        {
//            File[] filelist = file.listFiles();
//            for(int i = 0;i<filelist.length;i++)
//            {
//                String n = filelist[i].getName();
//                Tosmallerpic(f,filelist[i],"_middle",n,185,160,(float)0.7);
//                Tosmallerpic(f,filelist[i],"_small",n,45,45,(float)0.7);
//                Tosmallerpic(f,filelist[i],"_smaller",n,116,100,(float)0.7);
//            }
//        }

        //��һ���ļ�����ѹ������
        String url = "D:\\image\\test\\";
        String name  =  "1.jpg";
        img_change(url,name);


    }


    public static void img_change(String url,String name)
    {
        Tosmallerpic(url,new File(url+name),"_middle",name,188,165,(float)0.7);
        Tosmallerpic(url,new File(url+name),"_small",name,45,45,(float)0.7);
        Tosmallerpic(url,new File(url+name),"_smaller",name,116,100,(float)0.7);
    }

    /**
     *
     * @param f ͼƬ���ڵ��ļ���·��
     * @param file ͼƬ·��
     * @param ext ��չ��
     * @param n ͼƬ��
     * @param w Ŀ���
     * @param h Ŀ���
     * @param per �ٷֱ�
     */
    private static void  Tosmallerpic(String f,File file,String ext,String n,int w,int h,float per){
        Image src;
        try {
            src  =  javax.imageio.ImageIO.read(file); //����Image����

            String img_midname  =  f+n.substring(0,n.indexOf("."))+ext+n.substring(n.indexOf("."));
            int old_w = src.getWidth(null); //�õ�Դͼ��
            int old_h = src.getHeight(null);
            int new_w = 0;
            int new_h = 0; //�õ�Դͼ��

            double w2 = (old_w*1.00)/(w*1.00);
            double h2 = (old_h*1.00)/(h*1.00);

            //ͼƬ���ݳ������ף���һ��������ͼ��
            BufferedImage oldpic;
            if(old_w>old_h)
            {
                oldpic = new BufferedImage(old_w,old_w,BufferedImage.TYPE_INT_RGB);
            }else{if(old_w<old_h){
                oldpic = new BufferedImage(old_h,old_h,BufferedImage.TYPE_INT_RGB);
            }else{
                oldpic = new BufferedImage(old_w,old_h,BufferedImage.TYPE_INT_RGB);
            }
            }
            Graphics2D g  =  oldpic.createGraphics();
            g.setColor(Color.white);
            if(old_w>old_h)
            {
                g.fillRect(0, 0, old_w, old_w);
                g.drawImage(src, 0, (old_w - old_h) / 2, old_w, old_h, Color.white, null);
            }else{
                if(old_w<old_h){
                    g.fillRect(0,0,old_h,old_h);
                    g.drawImage(src, (old_h - old_w) / 2, 0, old_w, old_h, Color.white, null);
                }else{
                    //g.fillRect(0,0,old_h,old_h);
                    g.drawImage(src.getScaledInstance(old_w, old_h,  Image.SCALE_SMOOTH), 0,0,null);
                }
            }
            g.dispose();
            src  =  oldpic;
            //ͼƬ����Ϊ���ν���
            if(old_w>w)
                new_w = (int)Math.round(old_w/w2);
            else
                new_w = old_w;
            if(old_h>h)
                new_h = (int)Math.round(old_h/h2);//������ͼ����
            else
                new_h = old_h;
            BufferedImage image_to_save  =  new BufferedImage(new_w,new_h,BufferedImage.TYPE_INT_RGB);
            image_to_save.getGraphics().drawImage(src.getScaledInstance(new_w, new_h,  Image.SCALE_SMOOTH), 0,0,null);
            FileOutputStream fos = new FileOutputStream(img_midname); //������ļ���

            //�ɵ�ʹ�� jpeg classes���д���ķ���
//               JPEGImageEncoder encoder  =  JPEGCodec.createJPEGEncoder(fos);
//               JPEGEncodeParam jep = JPEGCodec.getDefaultJPEGEncodeParam(image_to_save);
            /* ѹ������ */
//               jep.setQuality(per, true);
//               encoder.encode(image_to_save, jep);

            //�µķ���
            saveAsJPEG(100, image_to_save, per, fos);

            fos.close();
        } catch (IOException ex) {
            ex.getStackTrace();
        }
    }


    /**
     * ��JPEG���뱣��ͼƬ
     * @param dpi  �ֱ���
     * @param image_to_save  Ҫ�����ͼ��ͼƬ
     * @param JPEGcompression  ѹ����
     * @param fos �ļ������
     * @throws IOException
     */
    public static void saveAsJPEG(Integer dpi ,BufferedImage image_to_save, float JPEGcompression, FileOutputStream fos) throws IOException {

        //useful documentation at
        //useful example program at ;to output JPEG data

        //old jpeg class
        //com.sun.image.codec.jpeg.JPEGImageEncoder jpegEncoder  =  com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(fos);
        //com.sun.image.codec.jpeg.JPEGEncodeParam jpegEncodeParam  =  jpegEncoder.getDefaultJPEGEncodeParam(image_to_save);

        // Image writer
        JPEGImageWriter imageWriter  =  (JPEGImageWriter) ImageIO.getImageWritersBySuffix("jpg").next();
        ImageOutputStream ios  =  ImageIO.createImageOutputStream(fos);
        imageWriter.setOutput(ios);
        //and metadata
        IIOMetadata imageMetaData  =  imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(image_to_save), null);


        if(dpi !=  null && !dpi.equals("")){

            //old metadata
            //jpegEncodeParam.setDensityUnit(com.sun.image.codec.jpeg.JPEGEncodeParam.DENSITY_UNIT_DOTS_INCH);
            //jpegEncodeParam.setXDensity(dpi);
            //jpegEncodeParam.setYDensity(dpi);

            //new metadata
            Element tree  =  (Element) imageMetaData.getAsTree("javax_imageio_jpeg_image_1.0");
            Element jfif  =  (Element)tree.getElementsByTagName("app0JFIF").item(0);
            jfif.setAttribute("Xdensity", Integer.toString(dpi) );
            jfif.setAttribute("Ydensity", Integer.toString(dpi));

        }


        if(JPEGcompression >= 0 && JPEGcompression <= 1f){

            //old compression
            //jpegEncodeParam.setQuality(JPEGcompression,false);

            // new Compression
            JPEGImageWriteParam jpegParams  =  (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
            jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(JPEGcompression);

        }

        //old write and clean
        //jpegEncoder.encode(image_to_save, jpegEncodeParam);

        //new Write and clean up
        imageWriter.write(imageMetaData, new IIOImage(image_to_save, null, null), null);
        ios.close();
        imageWriter.dispose();

    }

}

