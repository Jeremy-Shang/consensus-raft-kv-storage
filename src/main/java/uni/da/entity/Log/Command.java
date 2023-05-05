package uni.da.entity.Log;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uni.da.entity.Method;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class Command implements Serializable {
    Method method;
    String key;
    String value;
}
