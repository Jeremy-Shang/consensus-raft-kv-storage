import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
@Slf4j
public class LogTest {
    private static final Logger logger = LogManager.getLogger(LogTest.class);
    public static void main(String[] args) {


        logger.info("info");
        logger.debug("debug");
        logger.error("error");
        logger.trace("trace");
        logger.warn("warn");
    }
}
