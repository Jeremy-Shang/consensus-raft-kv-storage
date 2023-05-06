package uni.da.common;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

@Data
@Slf4j
public class Timer {

    private static Timer timer = null;

    private String name;
    private final PipedOutputStream outputStream;

    private final PipedInputStream inputStream;

    private Timer() throws IOException {
        this.outputStream = new PipedOutputStream();
        this.inputStream = new PipedInputStream(outputStream);
    }

    public synchronized static Timer getInstance() throws IOException {
        if (timer == null) {
            timer = new Timer();
        }
        return timer;
    }



}
