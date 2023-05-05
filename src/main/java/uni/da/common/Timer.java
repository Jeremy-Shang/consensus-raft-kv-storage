package uni.da.common;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

@Data
@Slf4j
public class Timer {
    private String name;
    private final PipedOutputStream outputStream;

    private final PipedInputStream inputStream;

    public Timer(String name) throws IOException {
        this.name = name;
        this.outputStream = new PipedOutputStream();
        this.inputStream = new PipedInputStream(outputStream);
    }
}
