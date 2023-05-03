package uni.da.node;

import java.io.IOException;
import java.io.Serializable;

public interface RaftModule extends Serializable {
    void start() throws InterruptedException, IOException;

    void stop();
}
