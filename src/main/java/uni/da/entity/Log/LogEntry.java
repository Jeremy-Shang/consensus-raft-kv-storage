package uni.da.entity.Log;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class LogEntry implements Serializable {
    int term;

    int logIndex;

    Command body;
}
