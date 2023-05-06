package uni.da.entity;


import lombok.*;

import java.io.Serializable;

@ToString
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ClientRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type{
        PUT,
        GET
    }

    @NonNull Type type;

    @NonNull int key;

    String val;

}
