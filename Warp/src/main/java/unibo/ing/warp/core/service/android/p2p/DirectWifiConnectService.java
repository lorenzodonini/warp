package unibo.ing.warp.core.service.android.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.*;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;

/**
 * Created by Lorenzo Donini on 5/23/2014.
 */
@WarpServiceInfo(type = WarpServiceInfo.Type.LOCAL, target = WarpServiceInfo.Target.ANDROID,
        completion = WarpServiceInfo.ServiceCompletion.EXPLICIT, name = "directWifiConnect", label="Connect")
public class DirectWifiConnectService extends DefaultWarpService {
    private IWarpDevice mWifiDirectDevice;
    private BroadcastReceiver mReceiver;
    private Channel mChannel;
    private boolean bConnected;
    public static final String CONNECTING = "Connecting...";
    public static final String CONNECTED = "Connected";
    public static final String FAILED = "Connection failed";

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        checkOptionalParameters(params,3);
        mWifiDirectDevice = (IWarpDevice)params[0];
        mChannel = (Channel)params[1];
        int groupOwnerPriority = (Integer)params[2];
        Context androidContext = (Context)context;
        setContext(context);

        WifiP2pManager manager = (WifiP2pManager) androidContext.getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pDevice device = (WifiP2pDevice)mWifiDirectDevice.getAbstractDevice();
        WifiP2pConfig p2pConfig = new WifiP2pConfig();
        p2pConfig.groupOwnerIntent = groupOwnerPriority;
        p2pConfig.deviceAddress = device.deviceAddress;
        if(device.wpsPbcSupported())
        {
            p2pConfig.wps.setup = WpsInfo.PBC;
        }
        else
        {
            //TODO: do something!
            throw new Exception("bla");
        }
        setupBroadcastReceiver();
        manager.connect(mChannel,p2pConfig,new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess()
            {
                bConnected=true;
                //Ignoring this since the broadcastReceiver will notify us
            }

            @Override
            public void onFailure(int reason)
            {
                bConnected=false;
                //TODO: handle error!
            }
        });
    }

    private void setupBroadcastReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                if(action == null)
                {
                    return;
                }
                if(action.equals(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION))
                {
                    NetworkInfo netInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                    if(netInfo == null)
                    {
                        return;
                    }
                    NetworkInfo.DetailedState state = netInfo.getDetailedState();
                    switch (state)
                    {
                        case CONNECTED:
                            break;
                        case OBTAINING_IPADDR:
                            break;
                        default:
                            break;
                    }
                }
            }
        };
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //Do nothing!
    }

    @Override
    public Object[] getResult()
    {
        return new Object [] {(bConnected) ? CONNECTED : FAILED};
    }

    @Override
    public Object[] getCurrentProgress()
    {
        return new Object[] {CONNECTING};
    }
}
