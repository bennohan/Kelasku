package com.bennohan.kelasku.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bennohan.kelasku.R
import com.bennohan.kelasku.api.ApiService
import com.bennohan.kelasku.data.Session
import com.bennohan.kelasku.data.constant.Const
import com.bennohan.kelasku.ui.detailFriends.DetailFriendsActivity
import com.crocodic.core.model.AppNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import org.greenrobot.eventbus.EventBus
import timber.log.Timber

class FirebaseMsgService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("firebase-token", token)
        /*session.setValue(Session.TOKEN_FCM, token)

        session.getString(Session.TOKEN_FCM)*/
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val context: Context = applicationContext

        Log.d("fcmServis", "messageData:${message.data}")
        Log.d("fcmServis", "message:${message.notification}")
        Timber.d("firebase_receive_message_title : ${message.data["user_id"]}")
        Timber.d("firebase_receive_message_title : ${message.data["title"]}")
        Timber.d("firebase_receive_message_message : ${message.data["message"]}")


        if (message.notification != null) {
            showNotification(
                context,
                message.data["title"] ?:return,
                message.data["body"] ?:return,
//                message.data["title"] ?:return,
//                message.notification!!.title!!,
//                message.notification!!.body!!
                //todo:title mengambil titlenya, body itu messagenya
            )
        }

    }

}

private fun sendRegistrationToServer(token: String?) {
    // TODO: Implement this method to send token to your app server.
    //todo: untuk mengirim device tokennya, tapi home sudah ngambil dan mengirim...ini hanya untuk mengeceknya
//    Log.d(TAG, "sendRegistrationTokenToServer($token)")
}

//todo: untuk edit notifikasinya, notifasi manager sudah ada di android
fun showNotification(context: Context, title: String, message: String) {
    //todo:Notification Manager
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    //todo: Notification for Oreo >
    // untuk android 13 meminta notifikasi untuk notifikasi wajib untuk android 13 keatas
    // untuk menyeting notifikasinya
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            "CHANNEL_ID", //untuk android 13
            "My Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "Mhm"
        channel.enableLights(true)
        channel.enableVibration(true)
        notificationManager.createNotificationChannel(channel)
    }

    //todo: untuk edit titile, masage, logo
    // todo:Builder
    val resultIntent = Intent(context, DetailFriendsActivity::class.java)
    var resultPendingIntent: PendingIntent? = PendingIntent.getActivity(context, 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    val builder = NotificationCompat.Builder(context,"CHANNEL_ID")
        .setSmallIcon(R.drawable.person_vector)
//        .setContentInfo(user_id)
        .setContentIntent(resultPendingIntent)
        .setAutoCancel(true)
        .setContentTitle(title)
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)

    // todo:Show Notification
    notificationManager.notify(1, builder.build())
}

//    override fun onNewToken(token: String) {
//        super.onNewToken(token)
//        Log.d("firebasetoken",token)
//
//    }
//
//    override fun onMessageReceived(message: RemoteMessage) {
//        super.onMessageReceived(message)
//
//        val context: Context = applicationContext
//
//        Log.d("notification", "${message.data}")
//
//
//        val title:String? = message.data["title"]
//        val content : String? = message.data["content"]
//
//        val appNotification = AppNotification(
//            title = title,
//            content = content
//        )
//        EventBus.getDefault().post(appNotification)
//
//    }
