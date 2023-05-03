package uni.da.entity;


import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@ToString
@Builder
@Data
public class ClientRequest {
    // 0: put 1: get
    int TYPE;

    int key;

    String val;
}
