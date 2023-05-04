package uni.da.util;

import uni.da.entity.Log.LogEntry;
import uni.da.node.ConsensusState;

import java.io.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class StateManager {
    public void saveState(ConsensusState consensusState) {
        PersistentState persistentState = new PersistentState(consensusState.getTerm().get(), consensusState.getVotedFor(), consensusState.getLogModule().getLogEntries().stream().toList());
        try {
            File myObj = new File("uni/da/state_"+consensusState.getId()+".txt");
            myObj.createNewFile();
            FileOutputStream fileOutputStream = new FileOutputStream(myObj);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(persistentState);
            System.out.println("成功保存状态");
            fileOutputStream.close();
            objectOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void restoreState(ConsensusState consensusState) {

        try {
            FileInputStream fileInputStream = new FileInputStream("uni/da/state_"+consensusState.getId()+".txt");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            PersistentState persistentState = (PersistentState) objectInputStream.readObject();
            consensusState.setTerm(new AtomicInteger(persistentState.currentTerm));
            consensusState.setVotedFor(persistentState.votedFor);
            consensusState.getLogModule().setLogEntries(new CopyOnWriteArrayList<LogEntry>(persistentState.logEntries));
            System.out.println("成功恢复状态");
            fileInputStream.close();
            objectInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("没找到日志文件");
        }
    }

    private class PersistentState implements Serializable {
        private final int currentTerm;
        private final int votedFor;
        private final List<LogEntry> logEntries;
        @Serial
        private static final long serialVersionUID = 1L;

        private PersistentState(int currentTerm, int votedFor, List<LogEntry> logEntries) {
            this.currentTerm = currentTerm;
            this.votedFor = votedFor;
            this.logEntries = logEntries;
        }
    }

}
