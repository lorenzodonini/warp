package unibo.ing.warp.core.service.android.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.view.IWarpDeviceViewAdapter;

/**
 * Created by Lorenzo Donini on 5/22/2014.
 */
@WarpServiceInfo(type=WarpServiceInfo.Type.LOCAL,name="disconnectFromAccessPoint", label = "Disconnect",
        target = WarpServiceInfo.Target.ANDROID, completion = WarpServiceInfo.ServiceCompletion.EXPLICIT)
public class WifiDisconnectService extends DefaultWarpService {
    private BroadcastReceiver mReceiver;
    private boolean bDisconnected;
    public static final String DISCONNECTED = "Disconnected";
    public static final String DISCONNECTING = "Disconnecting";
    public static final String FAILED = "Disconnection failed";

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        Context androidContext = (Context)context;
        setContext(context);

        WifiManager wifiManager = (WifiManager)androidContext.getSystemService(Context.WIFI_SERVICE);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                if(action != null && action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
                {
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if(info != null)
                    {
                        NetworkInfo.DetailedState state = info.getDetailedState();
                        if(state == NetworkInfo.DetailedState.DISCONNECTED)
                        {
                            onDisconnectedHandler();
                        }
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        androidContext.registerReceiver(mReceiver, filter);
        setPercentProgress(IWarpDeviceViewAdapter.PROGRESS_INDETERMINATE);
        getWarpServiceHandler().onServiceProgressUpdate(this);
        bDisconnected = wifiManager.disconnect();
        //TODO: disableNetwork in case the wifi manager tries to reconnect to the same network
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //Do nothing!
    }

    private void onDisconnectedHandler()
    {
        Context context = (Context)getContext();
        context.unregisterReceiver(mReceiver);
        mReceiver=null;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if(info != null)
        {
            bDisconnected = info.getNetworkId() < 0;
        }
        if(bDisconnected)
        {
            setPercentProgress(IWarpDeviceViewAdapter.PROGRESS_MAX);
        }
        else
        {
            setPercentProgress(IWarpDeviceViewAdapter.PROGRESS_FAILED);
        }
        getWarpServiceHandler().onServiceCompleted(this);
    }

    @Override
    public Object[] getResult()
    {
        return new Object[] {(bDisconnected)? DISCONNECTED : FAILED};
    }

    @Override
    public Object[] getCurrentProgress()
    {
        return new Object[] {DISCONNECTING};
    }
}
