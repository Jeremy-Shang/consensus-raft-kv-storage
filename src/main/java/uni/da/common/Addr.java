package uni.da.common;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Addr {

    public String ip;

    public int port;
}
