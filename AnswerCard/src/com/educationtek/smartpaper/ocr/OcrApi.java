package com.educationtek.smartpaper.ocr;

/**
 * @ClassName OcrApi
 * @Description 调用本地orc程序
 * @Author menshaojing
 * @Date 2021/12/30 10:10
 * @Version 1.0
 */
public class OcrApi {

    public static final String NATIVE_LIBRARY_OCRAPI= getNativeLibraryName();

    private static String getNativeLibraryName() { return "educationtek_OCR"; }

    /**
     * @Description 获取ocr识别文本信息（HTML形式，带有坐标）
     * @param imagePath
     * @param language
     * @throws
     * @return java.lang.String
     * @Author menshaojing
     * @Date  2021/12/30  10:16
     **/
    public static String orcHtext(String imagePath,String language){
        System.loadLibrary(NATIVE_LIBRARY_OCRAPI);
     return orcHtext0(imagePath,language);
    }
    private native static String orcHtext0(String imagePath,String language);

    public static void main(String[] args) {
        System.out.println(orcHtext("", "enm+num"));
    }
}
