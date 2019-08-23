package uz.molochniydomik.molochka.network;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import uz.molochniydomik.molochka.entities.Token;

public interface ApiService {
    @POST("login")
    @FormUrlEncoded
    Call<Token> login(@Field("username") String username, @Field("password") String password);

    @POST("accounting")
    @FormUrlEncoded
    Call<String> postAccounting(@Field("field_1") String field_1,
                                @Field("field_2") String field_2,
                                @Field("field_3") String field_3,
                                @Field("field_4") String field_4);

    @POST("refresh")
    @FormUrlEncoded
    Call<Token> refresh(@Field("refresh_token") String refreshToken);

    @GET("user")
    Call<String> getUserName();

    @GET("isAccounting")
    Call<String> getIsAccounting();

    @GET("isOrder")
    Call<String> getIsOrder();
}
