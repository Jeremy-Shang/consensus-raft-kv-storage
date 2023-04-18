package uni.da.node;

public interface RaftModule {
    void start() throws InterruptedException;

    void stop();
}
