package uni.da.entity;



public class ClientResponse<T> {




    public static <T> ClientResponse<T> success(T data) {

        return null;
    }


    public static <T> ClientResponse<T> error(T msg) {

        return null;
    }


    @Override
    public String toString() {
        return super.toString();
    }
}
