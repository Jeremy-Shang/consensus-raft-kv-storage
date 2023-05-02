package uni.da.entity.Log;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LogBody {
    int key;
    String value;
}
