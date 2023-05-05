package uni.da.app;

import uni.da.entity.Log.Command;
import uni.da.entity.Method;
import uni.da.entity.Result;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.RandomAccess;
import java.util.concurrent.locks.ReentrantLock;

public class StateMachine {
    private final Map<String, ValueAndLineNumber> cache;
    private final RandomAccessFile randomAccessFile;
    private int sequence;

    /**
     * @param id the identifier of the current server
     */
    public StateMachine(int id){
        cache = new HashMap<>();
        sequence = 1;
        try {
            File file  = new File("uni/da/data_"+id+".txt");
            file.createNewFile();       // just in case
            randomAccessFile =  new RandomAccessFile(file,"rw");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        load();
    }

    /**
     * Executes the given command and returns the result.
     *
     * @param command the command sent by the client.
     * @return the result of the execution.
     */
    public Result apply(Command command) throws IllegalAccessException {
        if (command.getMethod() == Method.GET){
            String value = get(command.getKey());

            if (value == null)
                return new Result(false);

            return new Result(true, value);
        } else if (command.getMethod() == Method.STORE){
            store(command.getKey(), command.getValue());
            return new Result(true);
        } else if (command.getMethod() == Method.DELETE) {
            boolean status = delete(command.getKey());
            return new Result(status);
        } else {
            throw new IllegalAccessException();
        }
    }

    /**
     * Loads data from file.
     */
    private void load(){
        String line;

        while (true) {
            try {
                line = randomAccessFile.readLine();
                if (line == null)
                    break;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            String[] keyValuePair = line.split(":", 2);
            if (keyValuePair.length == 2) {
                cache.put(keyValuePair[0], new ValueAndLineNumber(keyValuePair[1], sequence));
                sequence++;
            }
        }
    }

    /**
     * Returns the value associated with the given key.
     *
     * @param key the given key
     * @return the value associated with the key. If the given key does not exist, return null instead.
     */
    private String get(String key) {
        if (cache.containsKey(key))
            return cache.get(key).getValue();
        return null;
    }

    /**
     * Inserts a new key-value pair. If the given key already exists, the old value is then replaced with the new value.
     *
     * @param key an identifier
     * @param value the value associated with the key
     */
    private void store(String key, String value) {
        if (cache.containsKey(key)){        // an update operation
            cache.replace(key, new ValueAndLineNumber(value, cache.get(key).getLineNumber()));

            // a write-through policy is used here
            moveToCorrectPosition(cache.get(key).getLineNumber());

            // overwrites that line
            try {
                randomAccessFile.writeBytes((key+":"+value+"\n"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {    // an insertion operation
            try {
                cache.put(key,new ValueAndLineNumber(value,sequence));
                randomAccessFile.seek(randomAccessFile.length());
                randomAccessFile.writeBytes((key+":"+value+"\n"));
                sequence++;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Deletes the value associated with the given key.
     *
     * @param key an identifier
     * @return true if the value is successfully deleted and false otherwise.
     */
    private boolean delete(String key) {
        if (cache.containsKey(key)) {
            // replace the key-value pair in the file with a zombie key-value pair
            moveToCorrectPosition(cache.get(key).getLineNumber());
            try {
                String zombieKey = (char)0 + "_"+ cache.get(key).getLineNumber();
                randomAccessFile.writeBytes(zombieKey + ":" + (char)0 + "_" + "\n");
                cache.put(zombieKey, new ValueAndLineNumber((char)0 +"_", cache.get(key).getLineNumber()));
                cache.remove(key);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return true;
        }

        return false;
    }

    private void moveToCorrectPosition(int lineNumber){
        // moves the read/write head to the beginning of the file
        try {
            randomAccessFile.seek(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // moves the read/write head to the appropriate position
        for (int i=1;i<lineNumber;i++){
            try {
                randomAccessFile.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private class ValueAndLineNumber {
        private String value;
        private int lineNumber;

        private ValueAndLineNumber(String value, int lineNumber) {
            this.value = value;
            this.lineNumber = lineNumber;
        }

        private String getValue() {
            return value;
        }

        private int getLineNumber() {
            return lineNumber;
        }
    }

}