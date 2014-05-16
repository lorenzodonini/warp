package unibo.ing.warp.core.service.android.wifi;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import java.util.List;

import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.view.DialogFragmentListener;
import unibo.ing.warp.view.PasswordDialogFragment;

/**
 * Created by cronic90 on 08/10/13.
 */
@WarpServiceInfo(type=WarpServiceInfo.Type.LOCAL,name="wifiConnectionSetup", target = WarpServiceInfo.Target.ANDROID)
public class ConnectionSetupService extends DefaultWarpService {
    private IWarpDevice mWifiAccessPoint;
    private WifiConfiguration mWifiConfiguration;
    private WifiManager mWifiManager;
    private int mNetworkId=-1;
    private final static String DIALOG_TAG = "PW_DIALOG";

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        checkOptionalParameters(params,1);
        Context androidContext = (Context)context;
        setContext(context);
        mWifiAccessPoint=(IWarpDevice)params[0];
        mWifiManager=(WifiManager)androidContext.getSystemService(Context.WIFI_SERVICE);

        //Calling the actual custom service
        buildWifiConfiguration();
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //DO NOTHING SINCE THR SERVICE IS LOCAL
    }

    @Override
    public Object[] getResult()
    {
        return new Object[] {mNetworkId,mWifiConfiguration};
    }

    @Override
    public Object[] getCurrentProgress()
    {
        return null;
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
        PasswordDialogFragment fragment = new PasswordDialogFragment(keyType);
        fragment.setDialogFragmentListener(new DialogFragmentListener() {
            @Override
            public void onDialogPositiveClick(PasswordDialogFragment dialog) {
                setPassword(dialog.getPassword(),dialog.getKeyType());
            }

            @Override
            public void onDialogNegativeClick(PasswordDialogFragment dialog) {
                onFinishedConfigurationHandler();
                //TODO: Or perhaps we should just return and do nothing?!
            }
        });
        fragment.show(((Activity)getContext()).getFragmentManager(),DIALOG_TAG);
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
        WifiManager manager = (WifiManager)((Context)getContext()).getSystemService(Context.WIFI_SERVICE);

        mNetworkId=manager.addNetwork(mWifiConfiguration);
        manager.saveConfiguration();

        if(mNetworkId < 0)
        {
            Log.d("WIFI_CONFIG_BUILD", "Couldn't add network " + mWifiConfiguration.SSID);
        }
        /* From now on the PushDevice will be able to connect to the configured network.
        In case a connect() is called on the device even if no scan() was invoked before to
        make sure the device is reachable, then the device will perform a scan() automatically,
        following the AndroidWifiHotspot procedure. From the time this method is called, the
        device is considered as configured and ready to connect to.
         */
    }
}
