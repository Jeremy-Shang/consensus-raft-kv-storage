package uni.da.entity;

public class Result {
    private final boolean status;
    private String value;

    public Result(boolean status, String value) {
        this.status = status;
        this.value = value;
    }

    public Result(boolean status){
        this.status = status;
    }

    public boolean getStatus() {
        return status;
    }

    public String getValue() {
        return value;
    }
}
