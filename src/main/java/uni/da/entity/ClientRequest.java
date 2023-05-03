package uni.da.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

@ToString
@Builder
@Data
@AllArgsConstructor
public class ClientRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    // 0: put 1: get
    int TYPE;

    int key;

    String val;
}
