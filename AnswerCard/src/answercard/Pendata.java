package answercard;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author RenYinkui
 * @since 2021-07-27
 */
public class Pendata implements Serializable {

    private static final long serialVersionUID = 1L;


    private Long pdid;


    private Long pdaid;


    private String book;


    private Integer page;

    private Integer x;


    private Integer y;

    private Integer xf;


    private Integer yf;

    private Long ts;


    private String type;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer status;
    private String coordinates = "";

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public Long getPdid() {
        return pdid;
    }

    public void setPdid(Long pdid) {
        this.pdid = pdid;
    }

    public Long getPdaid() {
        return pdaid;
    }

    public void setPdaid(Long pdaid) {
        this.pdaid = pdaid;
    }

    public String getBook() {
        return book;
    }

    public void setBook(String book) {
        this.book = book;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getX() {
        return x;
    }

    public void setX(Integer x) {
        this.x = x;
    }

    public Integer getY() {
        return y;
    }

    public void setY(Integer y) {
        this.y = y;
    }

    public Integer getXf() {
        return xf;
    }

    public void setXf(Integer xf) {
        this.xf = xf;
    }

    public Integer getYf() {
        return yf;
    }

    public void setYf(Integer yf) {
        this.yf = yf;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
