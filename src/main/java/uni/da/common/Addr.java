package uni.da.common;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Addr implements Serializable {

    public String ip;

    public int port;
}
