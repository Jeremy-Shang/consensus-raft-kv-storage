import lombok.extern.slf4j.Slf4j;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import uni.da.entity.Log.LogBody;
import uni.da.entity.Log.LogEntry;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class LogTest {
    private static final Logger logger = LogManager.getLogger(LogTest.class);
    public static void main(String[] args) {


        logger.info("info");
        logger.debug("debug");
        logger.error("error");
        logger.trace("trace");
        logger.warn("warn");

        Map<Integer, List<LogEntry>> records = new HashMap<>();

// 初始化id为1的记录
        List<LogEntry> entries1 = new ArrayList<>();
        entries1.add(new LogEntry(1, 1, new LogBody(1, "value1")));
        entries1.add(new LogEntry(1, 2, new LogBody(2, "value2")));
        entries1.add(new LogEntry(1, 3, new LogBody(3, "value3")));
        records.put(1, entries1);

// 初始化id为2的记录
        List<LogEntry> entries2 = new ArrayList<>();
        entries2.add(new LogEntry(2, 1, new LogBody(4, "value4")));
        entries2.add(new LogEntry(2, 2, new LogBody(5, "value5")));
        records.put(2, entries2);

// 初始化id为3的记录
        List<LogEntry> entries3 = new ArrayList<>();
        entries3.add(new LogEntry(3, 1, new LogBody(6, "value6")));
        entries3.add(new LogEntry(3, 2, new LogBody(7, "value7")));
        entries3.add(new LogEntry(3, 3, new LogBody(8, "value8")));
        records.put(3, entries3);

        printTable(records);
    }

    public static void printTable(Map<Integer, List<LogEntry>> records) {
        // 获取所有记录的logIndex集合
        Set<Integer> logIndexSet = records.values().stream()
                .flatMap(List::stream)
                .map(LogEntry::getLogIndex)
                .collect(Collectors.toSet());

        // 获取所有记录的id集合
        Set<Integer> idSet = records.keySet();

        // 计算每列的最大宽度
        Map<Integer, Integer> maxWidths = new HashMap<>();
        for (int logIndex : logIndexSet) {
            int maxWidth = records.values().stream()
                    .flatMap(List::stream)
                    .filter(entry -> entry.getLogIndex() == logIndex)
                    .mapToInt(entry -> entry.getBody().toString().length() + String.valueOf(entry.getTerm()).length())
                    .max()
                    .orElse(0);
            maxWidths.put(logIndex, Math.max(maxWidth, String.valueOf(logIndex).length() + 2));
        }

        // 输出表格头
        String header = "|     |" + logIndexSet.stream()
                .map(logIndex -> String.format(" %-" + maxWidths.get(logIndex) + "d|", logIndex))
                .collect(Collectors.joining());
        System.out.println(header);

        String line = "| --- |" + logIndexSet.stream()
                .map(logIndex -> String.join("", Collections.nCopies(maxWidths.get(logIndex), "-")) + "|")
                .collect(Collectors.joining());
        System.out.println(line);

        // 输出表格内容
        for (int id : idSet) {
            StringBuilder rowBuilder = new StringBuilder();
            rowBuilder.append(String.format("| id%-2d |", id));
            for (int logIndex : logIndexSet) {
                List<LogEntry> entryList = records.get(id);
                if (entryList == null) {
                    rowBuilder.append(" " + String.join("", Collections.nCopies(maxWidths.get(logIndex) - 1, " ")) + "|");
                    continue;
                }
                Optional<LogEntry> entry = entryList.stream()
                        .filter(e -> e.getLogIndex() == logIndex)
                        .findFirst();
                if (entry.isPresent()) {
                    LogEntry logEntry = entry.get();
                    String bodyTermStr = logEntry.getBody().toString() + logEntry.getTerm();
                    rowBuilder.append(String.format(" %-" + maxWidths.get(logIndex) + "s|", bodyTermStr));
                } else {
                    rowBuilder.append(" " + String.join("", Collections.nCopies(maxWidths.get(logIndex) - 1, " ")) + "|");
                }
            }
            System.out.println(rowBuilder);
        }
    }



}
