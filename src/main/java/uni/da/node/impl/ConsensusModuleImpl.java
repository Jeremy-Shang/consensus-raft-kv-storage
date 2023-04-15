package uni.da.node.impl;

import lombok.extern.slf4j.Slf4j;
import uni.da.entity.RequestVoteRespond;
import uni.da.node.ConsensusModule;


@Slf4j
public class ConsensusModuleImpl implements ConsensusModule {


    @Override
    public void start() {
    }

    @Override
    public void stop() {

    }

    @Override
    public RequestVoteRespond requestVote() {
        return null;
    }

    @Override
    public void respondVote(RequestVoteRespond requestVoteRespond) {

    }

    @Override
    public String rpcTest() {
        return "hello rpc call";
    }
}
