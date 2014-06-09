package unibo.ing.warp.view;

import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.DefaultWarpDevice;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.IWarpService.ServiceOperation;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.base.LookupService;
import unibo.ing.warp.core.service.launcher.DefaultWarpServiceLauncher;
import unibo.ing.warp.core.service.launcher.IWarpServiceLauncher;
import unibo.ing.warp.core.service.launcher.WarpResourceLibrary;
import unibo.ing.warp.utils.WarpUtils;

import java.io.CharArrayReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lorenzo Donini on 5/21/2014.
 */
public class AndroidRemoteServicesPopup extends PopupMenu {
    private IWarpInteractiveDevice mDevice;
    private IWarpEngine mWarpDrive;
    private Map<String, WarpServiceInfo> mAvailableServices; //LABEL - INFO
    private WarpServiceInfo lookupService;
    private String mMasterKey;

    public AndroidRemoteServicesPopup(Context context, View anchor, IWarpEngine engine,
                                      String accessKey, IWarpInteractiveDevice device)
    {
        super(context, anchor);
        mDevice=device;
        mWarpDrive=engine;
        mAvailableServices=new HashMap<String, WarpServiceInfo>();
        lookupService = WarpUtils.getWarpServiceInfo(LookupService.class);
        mMasterKey = accessKey;
        populateInnerStructure();
    }

    public int getServicesAmount()
    {
        return mAvailableServices.size();
    }

    public void callService(String serviceLabel)
    {
        final WarpServiceInfo serviceInfo = mAvailableServices.get(serviceLabel);

        if(serviceInfo.type() == WarpServiceInfo.Type.LOCAL)
        {
            callLocalService(serviceInfo);
        }
        else
        {
            if(serviceInfo.type() == WarpServiceInfo.Type.PUSH)
            {
                mDevice.callPushService(serviceInfo);
            }
            else if(serviceInfo.type() == WarpServiceInfo.Type.PULL)
            {
                mDevice.callPullService(serviceInfo);
            }
        }
    }

    private void callLocalService(final WarpServiceInfo serviceInfo)
    {
        DefaultWarpDevice device = (DefaultWarpDevice)mDevice.getWarpDevice();
        final WarpServiceInfo connectService = WarpUtils.getWarpServiceInfo(device.getConnectServiceClass());
        final WarpServiceInfo disconnectService = WarpUtils.getWarpServiceInfo(device.getDisconnectServiceClass());

        if(connectService.name().equals(serviceInfo.name()))
        {
            mDevice.connect();
        }
        else if(disconnectService.name().equals(serviceInfo.name()))
        {
            mDevice.disconnect();
        }
        else
        {
            //TODO: should not be done!! right?!
            final DefaultWarpServiceLauncher launcher = (DefaultWarpServiceLauncher)
                    mWarpDrive.getLauncherForService(serviceInfo.name());
            launcher.setInitializationListener(new IWarpServiceLauncher.IWarpServiceInitializationListener() {
                @Override
                public void onInitializationCompleted()
                {
                    mWarpDrive.callLocalService(serviceInfo.name(), mWarpDrive.getListenerForService(
                            serviceInfo.name(), launcher.getServiceListenerParameters(mDevice,
                            ServiceOperation.CALL), ServiceOperation.CALL),
                            launcher.getServiceParameters(mDevice, ServiceOperation.CALL));
                }
            });
            WarpResourceLibrary library = WarpResourceLibrary.getInstance();
            String userKey = (String)library.getResource(mMasterKey,
                    WarpResourceLibrary.RES_USER_PERMISSION_KEY);
            launcher.initializeService(library,userKey, ServiceOperation.CALL);
        }
    }

    public void addService(WarpServiceInfo serviceDescriptor)
    {
        if(serviceDescriptor != null)
        {
            mAvailableServices.put(serviceDescriptor.label(),serviceDescriptor);
        }
    }

    private void populateInnerStructure()
    {
        IWarpServiceLauncher lookupLauncher = mWarpDrive.getLauncherForService(lookupService.name());
        if(lookupLauncher == null)
        {
            return;
        }
        //TODO: remember to add a callListener in case the service code was modified
        lookupLauncher.initializeService(WarpResourceLibrary.getInstance(), mMasterKey, ServiceOperation.CALL);
        Object [] handlerParameters = lookupLauncher.getServiceListenerParameters(mDevice, ServiceOperation.CALL);
        Collection<WarpServiceInfo> services = mDevice.getAvailableServices(mWarpDrive.
                getListenerForService(lookupService.name(), handlerParameters, ServiceOperation.CALL));
        Menu innerMenu = getMenu();
        for(WarpServiceInfo info : services)
        {
            mAvailableServices.put(info.label(),info);
            innerMenu.add(info.label());
        }
    }
}
