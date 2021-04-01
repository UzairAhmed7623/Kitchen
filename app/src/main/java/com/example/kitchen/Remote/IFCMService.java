package com.example.kitchen.Remote;

import com.example.kitchen.modelclasses.FCMResponse;
import com.example.kitchen.modelclasses.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAZf4rA1M:APA91bHhfDIUOkoU40DcfB4Cwov1ACfVj_aS8MPILy9p9u8UYHksFSJPuExNyfj3ESJE4M0qVn4YVc-dPZaJPOzHcZhCxC9dn1c33JvP297Zcsgx4ZhPkHuvymFa0TMOLYdK5seSjJDh"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}
