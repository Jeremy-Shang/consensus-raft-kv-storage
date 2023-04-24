package uni.da.node;

import java.io.IOException;

public interface RaftModule {
    void start() throws InterruptedException, IOException;

    void stop();
}
