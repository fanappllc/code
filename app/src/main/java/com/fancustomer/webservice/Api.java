package com.fancustomer.webservice;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Dheeraj Giri on 29/06/2017.
 */
public interface Api {


    @POST("send-otp")
    Call<ResponseBody> sendOTP(@Body Map<String, String> fields);

    @POST("otp-verify")
    Call<ResponseBody> verifyOTP(@Body Map<String, String> fields);


    @POST("add-card")
    Call<ResponseBody> addCardApi(@Header("access-token") String accessToken, @Body Map<String, String> fields);

    //Stripe add card api//


    @Multipart
    @POST("update-profile")
    Call<ResponseBody> updateProfile(@Header("access-token") String accessToken,
                                     @PartMap Map<String, RequestBody> map);

    @GET("user")
    Call<ResponseBody> getUserApi(@Header("access-token") String accessToken);


    @GET("messages/{order_id}")
    Call<ResponseBody> getMessage(@Header("access-token") String accessToken, @Path("order_id") String order_id);


    @POST("save-message")
    Call<ResponseBody> sendMessage(@Header("access-token") String accessToken, @Body Map<String, String> fields);


    @GET("get-my-cards")
    Call<ResponseBody> getMyCard(@Header("access-token") String accessToken);


    @DELETE("delete-card/{card_id}")
    Call<ResponseBody> getDeleteCardApi(@Header("access-token") String accessToken, @Path("card_id") String cardId);


    @POST("photographers")
    Call<ResponseBody> getNearPhoto(@Header("access-token") String accessToken, @Body Map<String, String> fields);


    @POST("update-location")
    Call<ResponseBody> updateLocationAPI(@Header("access-token") String accessToken, @Body Map<String, String> fields);


    @GET("slots")
    Call<ResponseBody> getSlots();

    @GET("customer-faq")
    Call<ResponseBody> getFaq();

    @POST("send-request")
    Call<ResponseBody> sendRequestApi(@Header("access-token") String accessToken, @Body Map<String, String> fields);


    @POST("logout")
    Call<ResponseBody> logOutApi(@Header("access-token") String accessToken);

    @GET("photographer-profile/{photographer_id}/{order_id}")
    Call<ResponseBody> getPhotographerProfile(@Header("access-token") String accessToken, @Path("photographer_id") String photographer_id, @Path("order_id") String order_id);

    @GET("customer-stripe-key")
    Call<ResponseBody> getStripeKey();


    @GET("proceed-or-cancel-request/{order_id}/{flag}")
    Call<ResponseBody> getProceedCancelApi(@Header("access-token") String accessToken, @Path("order_id") String order_id, @Path("flag") String flag);


    @GET("settings")
    Call<ResponseBody> getSettings();


    @GET("notifications")
    Call<ResponseBody> notificationListing(@Header("access-token") String accessToken, @Query("page") int pageNumber);

    @GET("complain-header")
    Call<ResponseBody> getComplain(@Header("access-token") String accessToken);


    @POST("start-time-request")
    Call<ResponseBody> StartTimeApi(@Header("access-token") String accessToken, @Body Map<String, String> fields);


    @DELETE("cancel-photography-session/{order_id}")
    Call<ResponseBody> cancelPhoto(@Header("access-token") String accessToken, @Path("order_id") String order_id);


    @GET("end-sessoin/{order_id}")
    Call<ResponseBody> getEndSessoinApi(@Header("access-token") String accessToken, @Path("order_id") String order_id);

    @POST("renew-start-time-request")
    Call<ResponseBody> renewTimeRequest(@Header("access-token") String accessToken, @Body Map<String, String> fields);


    @POST("rating-user")
    Call<ResponseBody> ratingUser(@Header("access-token") String accessToken, @Body Map<String, String> fields);

    @DELETE("notification/{notification_id}")
    Call<ResponseBody> notificationDeleteAPI(@Header("access-token") String accessToken, @Path("notification_id") String notification_id);


    @GET("my-orders")
    Call<ResponseBody> myOrder(@Header("access-token") String accessToken, @Query("page") int pageNumber);

    @DELETE("delete-all-notification")
    Call<ResponseBody> allNotificationAPI(@Header("access-token") String accessToken);

    @GET("get-photos")
    Call<ResponseBody> getPhoto(@Header("access-token") String accessToken);


    @GET("customer-terms-and-condition")
    Call<ResponseBody> getTermsAndCondition();

    @POST("complain")
    Call<ResponseBody> complainAPI(@Header("access-token") String accessToken, @Body Map<String, String> fields);


    @POST("use-promocode")
    Call<ResponseBody> userPromoCodeAPi(@Header("access-token") String accessToken, @Body Map<String, String> fields);


    @GET("discount/{slot_price}")
    Call<ResponseBody> getDiscountAPI(@Header("access-token") String accessToken, @Path("slot_price") String slot_price);


}
