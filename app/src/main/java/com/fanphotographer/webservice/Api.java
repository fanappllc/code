package com.fanphotographer.webservice;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
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

    @GET("user")
    Call<ResponseBody> getuser();


    @Multipart
    @POST("update-profile")
    Call<ResponseBody> updateProfile(@Header("access-token") String accessToken,
                                     @PartMap Map<String, RequestBody> map,
                                     @Part("first_name") RequestBody first_name,
                                     @Part("last_name") RequestBody last_name,
                                     @Part("email") RequestBody email,
                                     @Part("mobile_model") RequestBody mobile_modal,
                                     @Part("is_terms_condition_accepted") RequestBody is_terms_condition_accepted,
                                     @Part("zip_code") RequestBody zip_code,
                                     @Part("address") RequestBody addressst);

    @POST("update-ssn")
    Call<ResponseBody> updatessn(@Header("access-token") String accessToken, @Body Map<String, String> fields);

    @Multipart
    @POST("update-driving-licence-image")
    Call<ResponseBody> updatedrivinglicence(@Header("access-token") String accessToken, @PartMap Map<String, RequestBody> map);

    @POST("add-bank-account")
    Call<ResponseBody> addaccount(@Body Map<String, String> fields);


    @POST("photographer-registration-payment")
    Call<ResponseBody> registration_payment(@Body Map<String, String> fields);



    @POST("change-user-availability")
    Call<ResponseBody> user_availability(@Header("access-token") String accessToken, @Body Map<String, String> fields);

    @POST("logout")
    Call<ResponseBody> logout(@Header("access-token") String accessToken);


    @POST("update-location")
    Call<ResponseBody> updateLocationAPI(@Header("access-token") String accessToken, @Body Map<String, String> fields);

    @GET("order-request-list")
    Call<ResponseBody> getorderapi(@Header("access-token") String accessToken);

    @GET("accept-request/{order_id}")
    Call<ResponseBody> acceptRequest(@Header("access-token") String accessToken, @Path("order_id") int order_id);

    @POST("start-time-approve")
    Call<ResponseBody> starttimeApproveapi(@Header("access-token") String accessToken, @Body Map<String, String> fields);

    @DELETE("cancel-session-by-photographer/{order_id}/{cancel_reasons_id}")
    Call<ResponseBody> cancel_session_by_photographer_api(@Header("access-token") String accessToken, @Path("order_id") String order_id,@Path("cancel_reasons_id") String cancel_reasons_id);

    @GET("get-cancel-resons")
    Call<ResponseBody> getCancelresons();

    @GET("my-orders")
    Call<ResponseBody> myOrder(@Header("access-token") String accessToken, @Query("page") int pageNumber);

    @GET("notifications")
    Call<ResponseBody> notificationListing(@Header("access-token") String accessToken, @Query("page") int pageNumber);

    @DELETE("notification/{notification_id}")
    Call<ResponseBody> notificationDeleteAPI(@Header("access-token") String accessToken, @Path("notification_id") String notification_id);

    @Multipart
    @POST("upload-photo")
    Call<ResponseBody> uploadimage(@Header("access-token") String accessToken,
                                     @PartMap Map<String, RequestBody> map,
                                     @Part("order_id") RequestBody order_id,
                                     @Part("order_slot_id") RequestBody order_slot_id);


    @DELETE("delete-all-notification")
    Call<ResponseBody> allNotificationAPI(@Header("access-token") String accessToken);

    @GET("photographer-terms-and-condition")
    Call<ResponseBody> getTermsAndCondition();

    @GET("photographer-registration-fee")
    Call<ResponseBody> getRegistrationfee();

    @GET("settings")
    Call<ResponseBody> getSettings();

    @GET("customer-stripe-key")
    Call<ResponseBody> getstripekey();

    @GET("photographer-faq")
    Call<ResponseBody> getFaq();

    @GET("account-status")
    Call<ResponseBody> getStatus(@Header("access-token") String accessToken);


    @POST("save-message")
    Call<ResponseBody> save_message(@Header("access-token") String accessToken, @Body Map<String, String> fields);

    @GET("messages/{order_id}")
    Call<ResponseBody> get_msg(@Header("access-token") String accessToken, @Path("order_id") int order_id);

}
