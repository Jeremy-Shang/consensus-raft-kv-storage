package uni.da.entity.Log;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class LogBody implements Serializable {
    int key;
    String value;
}
