package uni.da.statemachine.task;


import lombok.extern.slf4j.Slf4j;
import uni.da.node.Character;
import uni.da.node.NodeParam;
import uni.da.statemachine.fsm.component.EventType;

@Slf4j
public class HeartBeatListenTask extends AbstractRaftTask{

    public HeartBeatListenTask(NodeParam nodeParam) {
        super(nodeParam);
    }

    /**
     * 以写入管道的方式来判断是否有心跳到达
     * @return
     * @throws Exception
     */
    @Override
    public EventType call() throws Exception {
        try {
            int signal = this.nodeParam.getPipe().getInputStream().read();
            // 监听到心跳，角色变为Follower
            nodeParam.setCharacter(Character.Follower);
            return EventType.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return EventType.FAIL;
        }
    }
}
