package svs.meeting.service;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

/**
 * Created by Mersens on 2016/9/28.
 */

public interface ServiceStore {

    @FormUrlEncoded
    @POST("/do_login")
    Observable<ResponseBody> do_login(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/query")
    Observable<ResponseBody> do_query(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/create_notes")
    Observable<ResponseBody> create_notes(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/execute")
    Observable<ResponseBody> delNotesById(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/execute")
    Observable<ResponseBody> startVoteBallot(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/execute")
    Observable<ResponseBody> setMeetingStatu(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/png_check")
    Observable<ResponseBody> png_check(@FieldMap Map<String, String> params);

    @FormUrlEncoded
    @POST("/saveDoc")
    Observable<ResponseBody> saveDoc(@FieldMap Map<String, String> params);

    @GET
    Observable<ResponseBody> download(@Url String fileUrl);

}
