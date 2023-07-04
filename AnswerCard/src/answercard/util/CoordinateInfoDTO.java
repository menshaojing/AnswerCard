package answercard.util;


/**
 * @author Wangzy
 * @Description: 坐标数据抽象对象
 * @date 17:09 2021/1/20
 */
public class CoordinateInfoDTO {

    /** 左上角横坐标 */
    private Float xCoord;

    /** 左上角纵坐标 */
    private Float yCoord;

    /** 宽度 */
    private Double width;

    /** 高度 */
    private Double height;

    public CoordinateInfoDTO(){}

    public CoordinateInfoDTO(Float xCoord, Float yCoord, Double width, Double height) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.width = width;
        this.height = height;
    }

    public Float getxCoord() {
        return xCoord;
    }

    public void setxCoord(Float xCoord) {
        this.xCoord = xCoord;
    }

    public Float getyCoord() {
        return yCoord;
    }

    public void setyCoord(Float yCoord) {
        this.yCoord = yCoord;
    }

    public Double getWidth() {
        return width;
    }

    public void setWidth(Double width) {
        this.width = width;
    }

    public Double getHeight() {
        return height;
    }

    public void setHeight(Double height) {
        this.height = height;
    }
}
