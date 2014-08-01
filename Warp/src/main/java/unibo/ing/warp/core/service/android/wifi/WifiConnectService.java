package unibo.ing.warp.core.service.android.wifi;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.launcher.android.WifiConnectLauncher;
import unibo.ing.warp.core.service.listener.android.WifiConnectServiceListener;
import unibo.ing.warp.utils.WarpUtils;
import unibo.ing.warp.view.DialogFragmentListener;
import unibo.ing.warp.view.IWarpDeviceViewAdapter;
import unibo.ing.warp.view.PasswordDialogFragment;
import java.util.List;

/**
 * User: lorenzodonini
 * Date: 10/11/13
 * Time: 18:27
 */
@WarpServiceInfo(type=WarpServiceInfo.Type.LOCAL,name="connectToAccessPoint", label = "Connect", target =
        WarpServiceInfo.Target.ANDROID, completion = WarpServiceInfo.ServiceCompletion.EXPLICIT,
        launcher = WifiConnectLauncher.class, callListener = WifiConnectServiceListener.class,
        protocol = WarpServiceInfo.Protocol.NONE)
public class WifiConnectService extends DefaultWarpService {
    private boolean bConnected;
    private boolean bCanCommunicateWith;
    private BroadcastReceiver mReceiver;
    private IWarpDevice mWifiAccessPoint;
    private WifiConfiguration mWifiConfiguration;
    private WifiManager mWifiManager;
    private int mNetworkId=-1;
    private final static String DIALOG_TAG = "PW_DIALOG";
    public static final String CONNECTING = "Connecting...";
    public static final String CONNECTED = "Connected";
    public static final String FAILED = "Connection failed";

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        checkOptionalParameters(params,1);
        Context androidContext = (Context)context;
        setContext(context);
        mWifiAccessPoint=(IWarpDevice)params[0];
        mWifiManager=(WifiManager)androidContext.getSystemService(Context.WIFI_SERVICE);

        buildWifiConfiguration();
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //NO IMPLEMENTATION SINCE SERVICE IS LOCAL
    }

    /**
     * Builder method, called by the callService() as the actual service. Builds the internal
     * WifiConfiguration object, based on the parameters of the local IWarpEngine source caller,
     * which represents the AndroidWifiHotspot. This AndroidWifiHotspot holds a ScanResult object, needed for
     * this builder method.
     * If appropriate parameters are found in the ScanResult object, the method dispatches
     * and obtainKey() call, given on the key type the user will have to enter in order
     * to complete the network configuration.
     */
    private void buildWifiConfiguration()
    {
        List<WifiConfiguration> configurationList;
        ScanResult scanResult=(ScanResult)mWifiAccessPoint.getAbstractDevice();
        String SSID;

        SSID="\""+scanResult.SSID+"\"";
        configurationList=mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration config:configurationList)
        {
            if(checkConfigurationEquality(config, scanResult))
            {
                config.priority=1;
                mWifiConfiguration=config;
                onFinishedConfigurationHandler();
                return;
            }

        }
        mWifiConfiguration = new WifiConfiguration();
        mWifiConfiguration.SSID=SSID;
        //mWifiConfiguration.BSSID="\""+scanResult.BSSID+"\"";
        mWifiConfiguration.status=WifiConfiguration.Status.DISABLED;
        mWifiConfiguration.priority=1;

        if(scanResult.capabilities.contains("WPA"))
        {
            obtainKey(PasswordDialogFragment.WPA_KEY);
        }
        else if(scanResult.capabilities.contains("WPA2"))
        {
            obtainKey(PasswordDialogFragment.WPA2_KEY);
        }
        else if(scanResult.capabilities.contains("WEP"))
        {
            obtainKey(PasswordDialogFragment.WEP_KEY);
        }
        else
        {
            mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            mWifiConfiguration.allowedAuthAlgorithms.clear();
            onFinishedConfigurationHandler();
        }
    }

    /**
     * This private methods checks whether an access point found by the WifiManager's scan() method
     * is the same access point as the WifiConfiguration passed as a parameter.
     * The device may be connected to a different IEEE 802.11 access point and still have discovered
     * other known access points. The check is performed by confronting SSID and the BSSID.
     * Needed to avoid reconfiguration of an already configured hotspot.
     *
     * @param oldConf  The known configuration to be confronted to
     * @param newConf  The ScanResult object, representing an in-range access point
     * @return  Returns true if the two parameters refer to the same network, false otherwise
     */
    private boolean checkConfigurationEquality(WifiConfiguration oldConf, ScanResult newConf)
    {
        boolean result=false;

        if(oldConf.BSSID!=null)
        {
            result = oldConf.BSSID.equals("\""+newConf.BSSID+"\"");
        }
        if(oldConf.SSID!=null)
        {
            result = oldConf.SSID.equals("\""+newConf.SSID+"\"");
        }

        return result;
    }

    /**
     * Internal call performed by the buildWifiConfiguration() method in case the
     * network the service is trying to access is password protected. The method creates
     * a new PasswordDialogFragment window and shows it to the user, awaiting a callback.
     *
     * @param keyType  The key type that needs to be displayed by the Dialog window and
     *                 needs to be passed on to the callback methods, in order to build
     *                 a proper wifi configuration
     */
    private void obtainKey(String keyType)
    {
        final PasswordDialogFragment fragment = new PasswordDialogFragment(keyType);
        fragment.setDialogFragmentListener(new DialogFragmentListener() {
            @Override
            public void onDialogPositiveClick(PasswordDialogFragment dialog) {
                setPassword(dialog.getPassword(),dialog.getKeyType());
                fragment.dismiss();
            }

            @Override
            public void onDialogNegativeClick(PasswordDialogFragment dialog) {
                bConnected=false;
                getWarpServiceHandler().onServiceAbort(WifiConnectService.this,null);
                fragment.dismiss();
            }
        });
        FragmentManager fragmentManager = ((Activity)getContext()).getFragmentManager();
        fragment.show(fragmentManager,DIALOG_TAG);
    }

    /**
     * Callback by a PasswordDialogFragment, in case the required password was set correctly.
     * Once the Dialog is closed, the password is set and the wifiConfiguration is
     * completed. In the end the method will just call the onFinishedConfigurationHandler.
     *
     * @param key  The password inserted by the user that needs to be set inside the config
     * @param keyType  The key type, necessary to build the correct configuration and set
     *                 the parameters according to the specific key type
     */
    private void setPassword(String key, String keyType)
    {
        ScanResult scanResult = (ScanResult)mWifiAccessPoint.getAbstractDevice();
        key="\""+key+"\"";

        if(keyType.equals(PasswordDialogFragment.WPA_KEY) ||
                keyType.equals(PasswordDialogFragment.WPA2_KEY))
        {
            setWpaConfiguration(scanResult, key);
        }
        else if(keyType.equals(PasswordDialogFragment.WEP_KEY))
        {
            setWepConfiguration(key);
        }
        /*Calling the handler which will then automatically call a connect() on the PushDevice
        itself.
         */
        onFinishedConfigurationHandler();
    }

    private void setWpaConfiguration(ScanResult scanResult, String key)
    {
        boolean found = false;
        mWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        mWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        mWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        mWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        if(scanResult.capabilities.contains("PSK"))
        {
            mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            found=true;
        }
        if(scanResult.capabilities.contains("EAP"))
        {
            mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
            found=true;
        }
        if(!found)
        {
            mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }

        mWifiConfiguration.preSharedKey=key;
    }

    private void setWepConfiguration(String key)
    {
        mWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        mWifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        mWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
        mWifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
        mWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        mWifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
        mWifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
        mWifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

        mWifiConfiguration.wepTxKeyIndex=0;
        mWifiConfiguration.wepKeys[0]="\""+key+"\"";
    }

    /**
     * Handler TO COMMENT
     */
    private void onFinishedConfigurationHandler()
    {
        mNetworkId=mWifiManager.addNetwork(mWifiConfiguration);

        if(mNetworkId < 0)
        {
            bConnected=false;
            getWarpServiceHandler().onServiceCompleted(this); //TODO: or maybe aborted?!
            return;
        }

        mWifiManager.saveConfiguration();
        bConnected = performConnect();
        if(!bConnected)
        {
            if(mReceiver != null)
            {
                Context context = (Context) getContext();
                context.unregisterReceiver(mReceiver);
                mReceiver=null;
            }
            setPercentProgress(IWarpDeviceViewAdapter.PROGRESS_FAILED);
            getWarpServiceHandler().onServiceCompleted(this);
        }
    }

    private boolean performConnect()
    {
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();

        if(wifiInfo!=null)
        {
            if(("\""+wifiInfo.getSSID()+"\"").equals(mWifiConfiguration.SSID) &&
                    wifiInfo.getNetworkId()==mNetworkId)
            {
                return false; //Already connected!!
            }
        }
        setupBroadcastReceiver();
        mWifiManager.setWifiEnabled(true);
        setPercentProgress(IWarpDeviceViewAdapter.PROGRESS_INDETERMINATE);
        getWarpServiceHandler().onServiceProgressUpdate(this);
        return mWifiManager.enableNetwork(mNetworkId,true);
    }

    private void setupBroadcastReceiver()
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        mReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if(action == null)
                {
                    return;
                }
                if(action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION))
                {
                    NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                    if(info != null)
                    {
                        NetworkInfo.DetailedState state = info.getDetailedState();
                        if(state == NetworkInfo.DetailedState.CONNECTED ||
                                state == NetworkInfo.DetailedState.FAILED)
                        {
                            onConnectedHandler();
                        }
                    }
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
        WarpUtils.checkNetworkInterfaces(); //TODO: NOT NEEDED!

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        if(info != null)
        {
            bConnected = info.getNetworkId() == mNetworkId;
        }
        if(bConnected)
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
        //What about IP discovery?
        return new Object [] {(bConnected) ? CONNECTED : FAILED, bCanCommunicateWith};
    }

    @Override
    public Object[] getCurrentProgress()
    {
        return new Object [] {CONNECTING};
    }
}
