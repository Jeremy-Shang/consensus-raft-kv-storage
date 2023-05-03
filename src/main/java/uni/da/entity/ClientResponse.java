package uni.da.entity;


import lombok.*;

import java.io.Serializable;

@Data
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ClientResponse<T>  implements Serializable {
    T data;

    String msg;

    public static <T> ClientResponse<T> success(T data) {

        ClientResponse<T> response = new ClientResponse<>();

        response.data = data;

        return response;
    }


    public static <T> ClientResponse<T> error(String msg) {
        ClientResponse<T> response = new ClientResponse<>();

        response.msg = msg;

        return response;
    }


    @Override
    public String toString() {
        return super.toString();
    }
}
