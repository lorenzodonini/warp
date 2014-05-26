package unibo.ing.warp.view;

import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.DefaultWarpDevice;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.base.LookupService;
import unibo.ing.warp.core.service.handler.IWarpServiceResourcesHandler;
import unibo.ing.warp.core.warpable.IWarpable;
import unibo.ing.warp.utils.WarpUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lorenzo Donini on 5/21/2014.
 */
public class AndroidServicesPopup extends PopupMenu {
    private IWarpInteractiveDevice mDevice;
    private IWarpEngine mWarpDrive;
    private Map<String, WarpServiceInfo> mAvailableServices; //LABEL - INFO
    private WarpServiceInfo lookupService;

    public AndroidServicesPopup(Context context, View anchor, IWarpEngine engine, IWarpInteractiveDevice device)
    {
        super(context, anchor);
        mDevice=device;
        mWarpDrive=engine;
        mAvailableServices=new HashMap<String, WarpServiceInfo>();
        lookupService = WarpUtils.getWarpServiceInfo(LookupService.class);
        populateInnerStructure();
    }

    public int getServicesAmount()
    {
        return mAvailableServices.size();
    }

    public void callService(String serviceLabel)
    {
        WarpServiceInfo serviceInfo = mAvailableServices.get(serviceLabel);

        if(serviceInfo.type() == WarpServiceInfo.Type.LOCAL)
        {
            callLocalService(serviceInfo);
        }
        else
        {
            IWarpServiceResourcesHandler serviceHandler = mWarpDrive.getDefaultHandlerForService(serviceInfo.name());
            Object [] listenerParams = serviceHandler.getServiceListenerParameters(mDevice);
            Object [] params = serviceHandler.getServiceParameters(mDevice);
            IWarpable [] remoteParams = serviceHandler.getServiceRemoteParameters();
            if(serviceInfo.type() == WarpServiceInfo.Type.PUSH)
            {
                mWarpDrive.callPushService(serviceInfo.name(),mDevice.getWarpDevice(),
                        mWarpDrive.getDefaultListenerForService(serviceInfo.name(),listenerParams),
                        null,params,remoteParams);
            }
            else
            {
                mWarpDrive.callPullService(serviceInfo.name(),mDevice.getWarpDevice(),
                        mWarpDrive.getDefaultListenerForService(serviceInfo.name(),listenerParams),
                        null,params,remoteParams);
            }
        }
    }

    private void callLocalService(WarpServiceInfo serviceInfo)
    {
        DefaultWarpDevice device = (DefaultWarpDevice)mDevice.getWarpDevice();
        WarpServiceInfo connectService = WarpUtils.getWarpServiceInfo(device.getConnectServiceClass());
        WarpServiceInfo disconnectService = WarpUtils.getWarpServiceInfo(device.getDisconnectServiceClass());
        IWarpServiceResourcesHandler serviceHandler = mWarpDrive.getDefaultHandlerForService(serviceInfo.name());
        if(connectService.name().equals(serviceInfo.name()))
        {
            mDevice.connect(mWarpDrive.getDefaultListenerForService(connectService.name(),
                    serviceHandler.getServiceListenerParameters(mDevice)));
        }
        else if(disconnectService.name().equals(serviceInfo.name()))
        {
            mDevice.disconnect(mWarpDrive.getDefaultListenerForService(disconnectService.name(),
                    serviceHandler.getServiceListenerParameters(mDevice)));
        }
        else
        {
            mWarpDrive.callLocalService(serviceInfo.name(),mWarpDrive.getDefaultListenerForService(
                    serviceInfo.name(),serviceHandler.getServiceListenerParameters(mDevice)),
                    serviceHandler.getServiceParameters(mDevice));
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
        IWarpServiceResourcesHandler lookupHandler = mWarpDrive.getDefaultHandlerForService(lookupService.name());
        Object [] handlerParameters = (lookupHandler != null) ?
                lookupHandler.getServiceListenerParameters(mDevice) : null;
        Collection<WarpServiceInfo> services = mDevice.getAvailableServices(mWarpDrive.
                getDefaultListenerForService(lookupService.name(), handlerParameters));
        Menu innerMenu = getMenu();
        for(WarpServiceInfo info : services)
        {
            mAvailableServices.put(info.label(),info);
            innerMenu.add(info.label());
        }
    }
}
