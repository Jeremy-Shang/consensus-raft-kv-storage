package uni.da.entity.Log;


import lombok.*;

import java.io.Serializable;


@Getter
@Setter
@AllArgsConstructor
public class LogBody implements Serializable {
    int key;
    String value;

    @Override
    public String toString() {
        return key + "->" + value + " ";
    }
}
