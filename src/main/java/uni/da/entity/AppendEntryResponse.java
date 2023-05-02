package uni.da.entity;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;


@Builder
@ToString
@Data
public class AppendEntryResponse implements Serializable {
    int term;

    boolean isSuccess;
}
