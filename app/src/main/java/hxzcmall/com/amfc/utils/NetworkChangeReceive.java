package hxzcmall.com.amfc.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/////////////监听网络状态变化的广播接收器
public class NetworkChangeReceive extends BroadcastReceiver {

    Activity activity;

    public NetworkChangeReceive(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager connectivityManager = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
            if(netInfo != null && netInfo.isAvailable()) {
                /////////////网络连接
                String name = netInfo.getTypeName();
                Log.v("DEBUG", name);

                if(netInfo.getType()== ConnectivityManager.TYPE_WIFI){
                    /////WiFi网络
                }else if(netInfo.getType()== ConnectivityManager.TYPE_ETHERNET){
                    /////有线网络
                }else if(netInfo.getType()== ConnectivityManager.TYPE_MOBILE){
                    /////////3g网络
                }
                if (null != delegate) {
                    delegate.networkEnable(true);
                }
            }
            else {
                if (null != delegate) {
                    delegate.networkEnable(false);
                }
            }
        }
    }

    public interface NetworkStatusDelegate {
        void networkEnable(boolean enable);
    };

    private NetworkStatusDelegate delegate;
    public void setNetworkStatusDelegate(NetworkStatusDelegate delegate) {
        this.delegate = delegate;
    }
};
