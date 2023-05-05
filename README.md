# Consensus-Raft

A KV database implemented in Java based on the Raft consensus algorithm.

*Reference*
> D. Ongaro and J. Ousterhout. In search of an understandable consensus algorithm. In USENIX ATC, June 2014.

## Brief introduction about raft

The Raft consensus algorithm is a distributed consensus algorithm used for maintaining consistency across a cluster of computers. It was designed to be easy to understand, making it popular among developers who are building distributed systems.

The algorithm works by electing a leader among the cluster nodes. The leader is responsible for managing the cluster state and ensuring that all other nodes agree on it. To do so, the leader sends periodic heartbeat messages to all other nodes to inform them of its existence. In the event of a leader failure, a new leader is elected using a leader election process.

The Raft algorithm provides several advantages over other consensus algorithms such as Paxos. It's easier to understand and implement, and it separates the leader election process from the consensus process, making it more fault-tolerant.

The Raft algorithm has been used in various systems, including distributed databases, distributed file systems, and key-value stores. It has proven to be an effective algorithm for building fault-tolerant distributed systems.


## About this project

The project is implemented in Java and follows the modules and steps defined in the Raft paper. Additionally, there is a client that can communicate with the cluster to some extent. The project currently implements the following features:

Leader Election: The algorithm elects a leader among the nodes using a consensus-based process.

Log Replication: Once the leader is elected, it is responsible for managing the log replication across all nodes in the cluster.

Through communication with the client, the project can demonstrate the cluster's consistency in data. 

Project structure:

```java
├── pom.xml
├── src
│   ├── main
│   │   ├── java
│   │   │   └── uni
│   │   │       └── da
│   │   │           ├── RaftClusterApp.java                             // Main Class
│   │   │           ├── common
│   │   │           ├── entity                                          // Define RPC entity and Log
│   │   │           │   ├── AppendEntryRequest.java
│   │   │           │   ├── AppendEntryResponse.java
│   │   │           │   ├── ClientRequest.java
│   │   │           │   ├── ClientResponse.java
│   │   │           │   ├── Log
│   │   │           │   │   ├── LogBody.java
│   │   │           │   │   └── LogEntry.java
│   │   │           │   ├── RequestVoteRequest.java
│   │   │           │   └── RequestVoteResponse.java
│   │   │           ├── node                                            // Define node in raft cluster: Log module, state machine, consensus state and etc
│   │   │           │   ├── Character.java
│   │   │           │   ├── ConsensusState.java
│   │   │           │   ├── LogModule.java
│   │   │           │   ├── Node.java
│   │   │           │   ├── RaftModule.java
│   │   │           │   ├── StateMachineModule.java
│   │   │           │   └── impl
│   │   │           │       ├── LogModuleImpl.java
│   │   │           │       ├── NodeModuleImpl.java
│   │   │           │       └── StateMachineImpl.java
│   │   │           ├── remote                                          // Define rpc remote service: the way of communicatig with other nodes
│   │   │           │   ├── RaftClient.java
│   │   │           │   ├── RaftRpcService.java
│   │   │           │   └── impl
│   │   │           │       ├── RaftClientImpl.java
│   │   │           │       └── RaftRpcServiceImpl.java
│   │   │           ├── statetransfer                                   // Implementation of "Leader-Follower-Candidate" state transfer via multithread task
│   │   │           │   ├── ServerStateTransfer.java
│   │   │           │   └── fsm
│   │   │           │       ├── StateMachine.java
│   │   │           │       ├── Transition.java
│   │   │           │       ├── component
│   │   │           │       │   ├── Context.java
│   │   │           │       │   ├── Event.java
│   │   │           │       │   ├── EventType.java
│   │   │           │       │   └── RaftState.java
│   │   │           │       └── impl
│   │   │           │           └── StateMachineFactory.java
│   │   │           ├── task                                             // Define the task in raft thread
│   │   │           │   ├── AbstractRaftTask.java
│   │   │           │   ├── BroadcastTask.java
│   │   │           │   ├── ListeningTask.java
│   │   │           │   └── election
│   │   │           │       ├── ElectionTask.java
│   │   │           │       └── WaitForVoteTask.java
│   │   │           └── util
│   │   │               └── LogUtil.java
│   │   └── resources
│   │       ├── META-INF
│   │       │   └── MANIFEST.MF
│   │       ├── log4j2.xml
│   │       └── raft.yaml
│   └── test
│       └── java
│           ├── ClientTest.java
│           └── LogTest.java
└── target
    ├── Consensus-Raft-1.0-SNAPSHOT-shaded.jar                                             // Running jar after packaging
    ├── Consensus-Raft-1.0-SNAPSHOT.jar

```

## Usage

Using `maven package` to get a runnable jar `Consensus-Raft-1.0-SNAPSHOT-shaded.jar` in `target/` . By default, you need to define 4 nodes to 

start a raft cluster. e.g.

Parameter:
- id: Unique id of node in cluster
- port: Port
- timeout: For testing purposes, you can specify a timeout duration (although the implementation in the paper uses randomized timeout durations).
```shell
  

 7319  java -Did=1 -Dport=6666 -Dtimeout=3000 -jar Consensus-Raft-1.0-SNAPSHOT-shaded.jar
 7320  java -Did=3 -Dport=6669 -Dtimeout=6524 -jar Consensus-Raft-1.0-SNAPSHOT-shaded.jar
 7321  java -Did=2 -Dport=6669 -Dtimeout=5983 -jar Consensus-Raft-1.0-SNAPSHOT-shaded.jar
 7322  java -Did=4 -Dport=6668 -Dtimeout=6987 -jar Consensus-Raft-1.0-SNAPSHOT-shaded.jar
```

## Interact with Raft cluster (TODO: still in progress)


To test the Raft cluster and interact with it, you can run the `ClientPrompt()` method in `uni.da.remote.RaftClient.`













