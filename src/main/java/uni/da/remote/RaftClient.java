package uni.da.remote;

public interface RaftClient {
    public void put(int key, String value);

    public String get(int key);
}
