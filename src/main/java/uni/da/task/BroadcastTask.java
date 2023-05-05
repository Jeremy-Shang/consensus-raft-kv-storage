package uni.da.task;

import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;
import uni.da.entity.AppendEntryResponse;
import uni.da.entity.Log.LogEntry;
import uni.da.node.ConsensusState;
import uni.da.entity.AppendEntryRequest;
import uni.da.remote.RaftRpcService;
import uni.da.statetransfer.fsm.component.EventType;
import uni.da.util.LogUtil;

@Slf4j
public class BroadcastTask extends AbstractRaftTask {

    private LogEntry logEntry = null;


    public BroadcastTask(ConsensusState consensusState) {
        super(consensusState);
    }

    /**
     * HeartBeat broadcast and AppendEntry broadcast
     * @return
     * @throws Exception
     */
    @Override
    public EventType call() throws Exception {

        LogUtil.printBoxedMessage(consensusState.getCharacter() + " " + consensusState.getName() + " broadcast msg !");

        CopyOnWriteArrayList<AppendEntryResponse> responses = new CopyOnWriteArrayList<>();

        AtomicInteger rel = new AtomicInteger(1);
        CountDownLatch latch = new CountDownLatch(consensusState.getClusterSize());


        Map<Integer, RaftRpcService> otherNodesService = new HashMap<>(this.consensusState.getRemoteServiceMap());
        otherNodesService.remove(consensusState.getId());

        /**
         * HeartBeat interval = election_timeout / 10;
         * TODO: distinguish broadcast type?
         */
        Thread.sleep(consensusState.getTimeout() / 10);


        // broadcast message
        for(Integer sid: otherNodesService.keySet()) {
            consensusState.getNodeExecutorService().execute(new Runnable() {
                @Override
                public void run() {
                    AppendEntryRequest request = null;
                    /**
                     * If last log index ≥ nextIndex for a follower: send
                     * AppendEntries RPC with log entries starting at nextIndex
                     */
                    int nextLogIndex = consensusState.getNextIndex().get(sid);
                    int prevLogIndex = consensusState.getLogModule().getPrevLogIndex();
                    int preLogTerm = consensusState.getLogModule().getPrevLogTerm();

                    if (consensusState.getLogModule().getLastLogIndex() > nextLogIndex) {

                        LogEntry logEntry = consensusState.getLogModule().getEntryByIndex(nextLogIndex);
                        request = AppendEntryRequest.builder()
                                .term(consensusState.getCurrTerm().get())
                                .leaderId(consensusState.getId())
                                .prevLogIndex(prevLogIndex)
                                .preLogTerm(preLogTerm)
                                .logEntry(logEntry)
                                .leaderCommit(consensusState.getCommitIndex().get())
                                .build();

                        try {
                            AppendEntryResponse response = otherNodesService
                                    .get(sid)
                                    .appendEntry(request);
                            /**
                             * 1. If successful: update nextIndex and matchIndex for
                             *      follower (§5.3)
                             * 2. If AppendEntries fails because of log inconsistency:
                             *      decrement nextIndex and retry (§5.3)
                             *
                             * TODO: check if update correct
                             */
                            if (response.isSuccess()) {
                                consensusState.getMatchIndex().put(sid, nextLogIndex);
                                consensusState.getNextIndex().put(sid, nextLogIndex + 1);

                                rel.incrementAndGet();
                            } else {
                                consensusState.getNextIndex().put(sid, consensusState.getNextIndex().get(sid) - 1);
                            }
                        } catch (RemoteException e) {
                            log.error("[SEND MESSAGE FAIL] from {} to {}. ", consensusState.getId(), sid);
                        } finally {
                            latch.countDown();
                        }

                    }
                }
            });
        }

        /**
         * TODO: If there exists an N such that N > commitIndex, a majority
         *          of matchIndex[i] ≥ N, and log[N].term == currentTerm:
         *          set commitIndex = N (§5.3, §5.4).
         *
         * Current: if more than half nodes replicated log, commit it and set commit index
         */

        latch.await();

        int[] matches = consensusState.getMatchIndex().values().stream().mapToInt(e -> e).toArray();

        // Condition 1: N > commitIndex and log[N].term == currentTerm
        int[] N = IntStream.range(0, Arrays.stream(matches).max().getAsInt())
                .filter(n -> consensusState.getLogModule().getEntryByIndex(n).getTerm() == consensusState.getCurrTerm().get() && n > consensusState.getCommitIndex().get())
                .sorted()
                .toArray();

        // Condition 2: a majority of matchIndex[i] ≥ N
        Map<Integer, Integer> map = Arrays.stream(N)
                .boxed()
                .collect(Collectors.toMap(Function.identity(), n -> 0));

        Arrays.stream(N).mapToObj(e -> (Integer) e)
                .flatMap(n -> Arrays.stream(matches)
                                .filter(m -> m > n)
                                .mapToObj(m -> Map.entry(m, 1)))
                .collect(Collectors.groupingBy(Map.Entry::getKey))
                .forEach((k, v) -> map.put(k, v.stream().mapToInt(Map.Entry::getValue).sum()));

        N = map.entrySet()
                .stream()
                .filter(entry -> entry.getValue() > (consensusState.getClusterSize() / 2) + 1)
                .map(e -> e.getKey())
                .mapToInt(e -> e)
                .sorted()
                .toArray();

        int newCommitIndex = N[N.length - 1];

        log.info("[SATISFIED N] {} commitIndex {} ", Arrays.toString(N), newCommitIndex);

        consensusState.setCommitAndApply(newCommitIndex);

        return EventType.SUCCESS;
    }
}
