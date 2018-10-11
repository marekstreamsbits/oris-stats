package oris.extractor.response;

public class ResultDTO {

    private String classDesc;

    private String place;

    private String regNo;

    private String time;

    private String loss;

    public String getClassDesc() {
        return classDesc;
    }

    public String getPlace() {
        return place;
    }

    public String getRegNo() {
        return regNo;
    }

    public String getTime() {
        return time;
    }

    public String getLoss() {
        return loss;
    }

    public void setClassDesc(String classDesc) {
        this.classDesc = classDesc;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLoss(String loss) {
        this.loss = loss;
    }
}