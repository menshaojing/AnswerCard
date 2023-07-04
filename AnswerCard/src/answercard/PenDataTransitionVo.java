package answercard;

import java.io.Serializable;


public class PenDataTransitionVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String book;

    private Integer page;

    private String coordinates = "";

    private Long brid;

    private Long time;

    //学生或批阅老师ID
    private Integer userid = 0;

    //教学平台pageid(页码)
    private Integer pageid;
    //笔迹主键ID update:menshaojing 2022年4月18日09:35:22
    private String id;
    //作业ID update:menshaojing 2022年4月18日09:35:22
    private Long actionid;

    /**
     * @Description 批次号(用于每个学生或者每个异常作业批阅的批次)

     * @Author menshaojing
     * @Date  2022/4/24  11:55
     **/
    private String batchNo;

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

    public String getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(String coordinates) {
        this.coordinates = coordinates;
    }

    public Long getBrid() {
        return brid;
    }

    public void setBrid(Long brid) {
        this.brid = brid;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public Integer getPageid() {
        return pageid;
    }

    public void setPageid(Integer pageid) {
        this.pageid = pageid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getActionid() {
        return actionid;
    }

    public void setActionid(Long actionid) {
        this.actionid = actionid;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }
}
