package uni.da.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogUtil {

    public static void printBoxedMessage(String message) {
        int length = message.length() + 20;
        String border = String.format("%0" + length + "d", 0).replace("0", "#");
        log.info(border);
        log.info("# " + message + " #");
        log.info(border);
    }
}
