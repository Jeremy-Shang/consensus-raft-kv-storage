package uni.da.util;

import lombok.extern.slf4j.Slf4j;
import uni.da.entity.Log.LogEntry;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class LogUtil {

    public static void printBoxedMessage(String message) {
        int length = message.length() + 20;
        String border = String.format("%0" + length + "d", 0).replace("0", "#");
        log.info(border);
        log.info("# " + message + " #");
        log.info(border);
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
        log.info(header);

        String line = "| --- |" + logIndexSet.stream()
                .map(logIndex -> String.join("", Collections.nCopies(maxWidths.get(logIndex), "-")) + "|")
                .collect(Collectors.joining());
        log.info(line);

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
            log.info(String.valueOf(rowBuilder));
        }
    }

}
