package unibo.ing.warp.core.service.android.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.*;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.launcher.WarpResourceLibrary;
import unibo.ing.warp.core.service.launcher.android.DirectWifiConnectLauncher;
import unibo.ing.warp.core.service.launcher.android.DirectWifiPingLauncher;
import unibo.ing.warp.core.service.listener.android.DirectWifiConnectServiceListener;
import unibo.ing.warp.view.IWarpDeviceViewAdapter;

/**
 * Created by Lorenzo Donini on 5/23/2014.
 */
@WarpServiceInfo(type = WarpServiceInfo.Type.LOCAL, target = WarpServiceInfo.Target.ANDROID,
        completion = WarpServiceInfo.ServiceCompletion.EXPLICIT, name = "directWifiConnect", label="Connect",
        launcher = DirectWifiConnectLauncher.class, callListener = DirectWifiConnectServiceListener.class)
public class DirectWifiConnectService extends DefaultWarpService {
    private IWarpDevice mWifiDirectDevice;
    private BroadcastReceiver mReceiver;
    private Channel mChannel;
    private boolean bConnected;
    private boolean bCanCommunicateWith;
    private Looper mLooper;
    private Handler mHandler;
    private boolean bStartsChainedService;
    private String mPermissionKey;
    private static final int PING_TIMEOUT = 5000;
    public static final int PING_RECEIVED = 27;
    public static final String CONNECTING = "Connecting...";
    public static final String CONNECTED = "Connected";
    public static final String FAILED = "Connection failed";
    //TODO: NEEDS DirectWifiPingService DEPENDENCY

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        checkOptionalParameters(params,5);
        mWifiDirectDevice = (IWarpDevice)params[0];
        mChannel = (Channel)params[1];
        int groupOwnerPriority = (Integer)params[2];
        bStartsChainedService = (Boolean)params[3];
        mPermissionKey = (String)params[4];
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
        //Updating view
        setPercentProgress(IWarpDeviceViewAdapter.PROGRESS_INDETERMINATE);
        getWarpServiceHandler().onServiceProgressUpdate(this);

        if(bStartsChainedService)
        {
            preparePeerPingLooper();
            WarpResourceLibrary.getInstance().setResource(mPermissionKey,
                    DirectWifiPingLauncher.RES_P2P_CONNECT_SERVICE_HANDLER, mHandler);
        }
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
                getWarpServiceHandler().onServiceAbort(DirectWifiConnectService.this,"Error!");
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
                    if(state == NetworkInfo.DetailedState.CONNECTED)
                    {
                        WifiP2pManager manager = (WifiP2pManager)((Context)getContext())
                                .getSystemService(Context.WIFI_P2P_SERVICE);
                        manager.requestGroupInfo(mChannel,new WifiP2pManager.GroupInfoListener() {
                            @Override
                            public void onGroupInfoAvailable(WifiP2pGroup group)
                            {
                                onConnectedHandler(group);
                            }
                        });
                    }
                    else if(state == NetworkInfo.DetailedState.FAILED)
                    {
                        bConnected=false;
                        setPercentProgress(IWarpDeviceViewAdapter.PROGRESS_FAILED);
                        ((Context)getContext()).unregisterReceiver(mReceiver);
                        mReceiver=null; //Since the Service may stay referenced, the receiver should stay too --> Deallocate
                        getWarpServiceHandler().onServiceCompleted(DirectWifiConnectService.this);
                    }
                }
            }
        };
        ((Context)getContext()).registerReceiver(mReceiver,filter);
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //Do nothing!
    }

    private void onConnectedHandler(WifiP2pGroup group)
    {
        Context context = (Context)getContext();
        context.unregisterReceiver(mReceiver);
        mReceiver=null; //Since the Service may stay referenced, the receiver should stay too --> Deallocate

        WifiP2pDevice device = (WifiP2pDevice) mWifiDirectDevice.getAbstractDevice();
        if(!group.isGroupOwner())
        {
            WifiP2pDevice ownerDevice = group.getOwner();
            bConnected = device.deviceName.equals(ownerDevice.deviceName)
                    && device.deviceAddress.equals(ownerDevice.deviceAddress);
        }
        else
        {
            bConnected=false;
            for(WifiP2pDevice otherDevice : group.getClientList())
            {
                if(otherDevice.deviceName.equals(device.deviceName)
                        && otherDevice.deviceAddress.equals(device.deviceAddress))
                {
                    bConnected=true;
                    break;
                }
            }
            if(bStartsChainedService)
            {
                mHandler.postDelayed(new OnPingTimeoutTask(),PING_TIMEOUT);
                Looper.loop();
                return;
            }
        }
        bCanCommunicateWith=bConnected;
        if(bConnected)
        {
            setPercentProgress(IWarpDeviceViewAdapter.PROGRESS_MAX);
        }
        else
        {
            setPercentProgress(IWarpDeviceViewAdapter.PROGRESS_FAILED);
        }
        WarpResourceLibrary.getInstance().setResource(DirectWifiPingLauncher.
                RES_P2P_CONNECT_SERVICE_HANDLER,mPermissionKey,null);
        getWarpServiceHandler().onServiceCompleted(this);
    }

    private void preparePeerPingLooper()
    {
        Looper.prepare();
        mLooper = Looper.myLooper();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message inputMessage)
            {
                if(inputMessage.what == PING_RECEIVED)
                {
                    onPingReceived();
                }
            }
        };
    }

    public void onPingReceived()
    {
        bCanCommunicateWith = mWifiDirectDevice.getDeviceLocation().getIPv4Address() != null;
        if(bCanCommunicateWith)
        {
            setPercentProgress(IWarpDeviceViewAdapter.PROGRESS_MAX);
            mLooper.quit();
            WarpResourceLibrary.getInstance().setResource(DirectWifiPingLauncher.
                    RES_P2P_CONNECT_SERVICE_HANDLER,mPermissionKey,null);
        }
    }

    private class OnPingTimeoutTask implements Runnable {

        @Override
        public void run()
        {
            bCanCommunicateWith = mWifiDirectDevice.getDeviceLocation().getIPv4Address() != null;
            if(bCanCommunicateWith)
            {
                setPercentProgress(IWarpDeviceViewAdapter.PROGRESS_MAX);
            }
            else
            {
                setPercentProgress(IWarpDeviceViewAdapter.PROGRESS_FAILED);
            }
            mLooper.quit();
            WarpResourceLibrary.getInstance().setResource(DirectWifiPingLauncher.
                    RES_P2P_CONNECT_SERVICE_HANDLER,mPermissionKey,null);
        }
    }

    @Override
    public Object[] getResult()
    {
        return new Object [] {(bConnected) ? CONNECTED : FAILED, bCanCommunicateWith};
    }

    @Override
    public Object[] getCurrentProgress()
    {
        return new Object[] {CONNECTING};
    }
}
