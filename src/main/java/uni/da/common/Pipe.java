package uni.da.common;


import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

@Data
@Slf4j
@Builder
public class Pipe {

    private String name;
    private final PipedOutputStream outputStream;

    private final PipedInputStream inputStream;

    /*
       双端管道, 用来监听
     */
    public Pipe(String name) throws IOException {
        this.name = name;
        this.outputStream = new PipedOutputStream();
        this.inputStream = new PipedInputStream(outputStream);
    }
}
