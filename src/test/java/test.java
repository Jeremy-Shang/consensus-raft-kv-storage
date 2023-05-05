import uni.da.app.StateMachine;
import uni.da.entity.Log.Command;
import uni.da.entity.Method;
import uni.da.entity.Result;

import java.util.Scanner;

public class test {


    public static void main(String[] args) throws IllegalAccessException {
        StateMachine stateMachine = new StateMachine(1);
        while (true){
            Scanner scanner = new Scanner(System.in);
            String method = scanner.next();
            String key = scanner.next();
            String value = scanner.next();
            Command command = new Command(Method.valueOf(method), key, value);
            Result result= stateMachine.apply(command);
            System.out.println(result.getStatus() + " "+ result.getValue());
        }
    }
}
