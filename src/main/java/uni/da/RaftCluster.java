package uni.da;

public class RaftCluster {
    // 集群配置相关

    public RaftCluster() {
        String path = RaftCluster.class.getResource("raft.yaml").getPath();

    }

    public void getCommand() {

    }

    public void getConfig() {

    }

    // TODO 拉起多端进程
    public static void main(String[] args) {

        System.out.println("Hello world!");

    }

}