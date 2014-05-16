package unibo.ing.warp.core.service.android.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;

/**
 * User: lorenzodonini
 * Date: 10/11/13
 * Time: 18:27
 */
@WarpServiceInfo(type=WarpServiceInfo.Type.LOCAL,name="connectToAccessPoint", target = WarpServiceInfo.Target.ANDROID)
public class WifiConnectService extends DefaultWarpService {
    private boolean bConnected;
    private BroadcastReceiver mReceiver;
    private WifiConfiguration mTargetConfiguration;

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        WifiManager wifiManager;
        WifiConfiguration wifiConfiguration;
        int networkId;

        checkOptionalParameters(params,2);
        Context androidContext = (Context)context;
        setContext(context);

        mTargetConfiguration=(WifiConfiguration)params[0];
        networkId=(Integer)params[1];
        wifiManager=(WifiManager)androidContext.getSystemService(Context.WIFI_SERVICE);

        setPercentProgress(0);
        getWarpServiceHandler().onServiceProgressUpdate(this);
        bConnected=performConnect(wifiManager,mTargetConfiguration,networkId);
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //NO IMPLEMENTATION SINCE SERVICE IS LOCAL
    }

    private boolean performConnect(WifiManager manager, WifiConfiguration configuration, int networkId)
    {
        WifiInfo wifiInfo = manager.getConnectionInfo();

        if(wifiInfo!=null)
        {
            if(wifiInfo.getSSID().equals(configuration.SSID) &&
                    wifiInfo.getNetworkId()==networkId)
            {
                return false;
            }
        }
        setupBroadcastReceiver();
        manager.setWifiEnabled(true);
        return manager.enableNetwork(networkId,true);
    }

    private void setupBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);

        mReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if(action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION))
                {
                    bConnected=intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED,false);
                    onConnectedHandler();
                }
            }
        };
        ((Context)getContext()).registerReceiver(mReceiver,intentFilter);
    }

    private void onConnectedHandler()
    {
        Context context = (Context)getContext();
        context.unregisterReceiver(mReceiver);
        mReceiver=null; //Since the Service may stay referenced, the receiver should stay too --> Deallocate

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        bConnected = info.getNetworkId() == mTargetConfiguration.networkId;
        setPercentProgress(100);
        getWarpServiceHandler().onServiceProgressUpdate(this);
    }

    @Override
    public Object[] getResult()
    {
        return new Object [] {(bConnected) ? "Connected" : "Connection refused"};
    }

    @Override
    public Object[] getCurrentProgress()
    {
        if(getPercentProgress()==100)
        {
            return getResult();
        }
        else
        {
            return new Object [] {"Connecting..."};
        }
    }
}
