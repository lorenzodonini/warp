package unibo.ing.warp.core.device;

import unibo.ing.warp.core.DefaultWarpInteractiveDevice;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.IWarpService.ServiceOperation;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.base.LookupService;
import unibo.ing.warp.core.service.launcher.DefaultWarpServiceLauncher;
import unibo.ing.warp.core.service.launcher.IWarpServiceLauncher;
import unibo.ing.warp.core.service.launcher.WarpResourceLibrary;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.warpable.IWarpable;
import unibo.ing.warp.utils.WarpUtils;

/**
 * Created by Lorenzo Donini on 5/7/2014.
 */
public class WarpAccessManager implements IWarpDeviceRequestHandler {
    private IWarpLocalDevice mLocalDevice;
    private WarpDeviceManager mDeviceManager;
    private static String mAccessKey;
    private static WarpAccessManager mAccessManager;

    private WarpAccessManager(String accessKey)
    {
        mAccessKey=accessKey;
        mDeviceManager=new WarpDeviceManager();
    }

    public static WarpAccessManager getInstance(String accessKey)
    {
        if(mAccessManager == null)
        {
            if(accessKey != null)
            {
                mAccessManager = new WarpAccessManager(accessKey);
            }
        }
        return (accessKey != null && accessKey.equals(mAccessKey)) ? mAccessManager : null;
    }

    public void setLocalDevice(IWarpLocalDevice localDevice)
    {
        mLocalDevice=localDevice;
    }

    public IWarpLocalDevice getLocalDevice()
    {
        return mLocalDevice;
    }

    public WarpDeviceManager getDeviceManager()
    {
        return mDeviceManager;
    }

    public final void startWarpService(Class<? extends IWarpService> serviceClass, String accessKey,
                                 ServiceOperation operation, IWarpInteractiveDevice target)
    {
        WarpServiceInfo serviceDescriptor = WarpUtils.getWarpServiceInfo(serviceClass);
        if(serviceDescriptor == null)
        {
            return;
        }
        IWarpEngine warpDrive = mLocalDevice.getWarpEngine();
        IWarpServiceLauncher launcher = warpDrive.getLauncherForService(serviceDescriptor.name());
        launcher.initializeService(WarpResourceLibrary.getInstance(), accessKey, operation);
        IWarpServiceListener listener = warpDrive.getListenerForService(serviceDescriptor.name(),
                launcher.getServiceListenerParameters(target,operation),operation);
        if(serviceDescriptor.type() == WarpServiceInfo.Type.LOCAL)
        {
            warpDrive.callLocalService(serviceDescriptor.name(),listener,
                    launcher.getServiceParameters(target,operation));
        }
        else if(serviceDescriptor.type() == WarpServiceInfo.Type.PUSH)
        {
            warpDrive.callPushService(serviceDescriptor.name(), target.getWarpDevice(), listener, null,
                    launcher.getServiceParameters(target, operation), launcher.getServiceRemoteParameters());
        }
        else if(serviceDescriptor.type() == WarpServiceInfo.Type.PULL)
        {
            warpDrive.callPullService(serviceDescriptor.name(), target.getWarpDevice(), listener, null,
                    launcher.getServiceParameters(target, operation), launcher.getServiceRemoteParameters());
        }
    }

    public final void stopWarpService(Class<? extends IWarpService> serviceClass, String accessKey)
    {
        WarpServiceInfo serviceDescriptor = WarpUtils.getWarpServiceInfo(serviceClass);
        if(serviceDescriptor == null)
        {
            return;
        }
        IWarpEngine warpDrive = mLocalDevice.getWarpEngine();
        long [] serviceIds = warpDrive.getActiveServicesIdsByName(serviceDescriptor.name());
        if(serviceIds != null && serviceIds.length > 0)
        {
            warpDrive.stopService(serviceIds[0]);
        }
    }

    public final boolean isServiceActive(Class<? extends IWarpService> serviceClass, String accessKey)
    {
        WarpServiceInfo serviceDescriptor = WarpUtils.getWarpServiceInfo(serviceClass);
        if(serviceDescriptor == null)
        {
            return false;
        }
        IWarpEngine warpDrive = mLocalDevice.getWarpEngine();
        long [] serviceIds = warpDrive.getActiveServicesIdsByName(serviceDescriptor.name());
        return serviceIds != null && serviceIds.length > 0;
    }

    //Single Device Request Handlers
    @Override
    public void onConnectRequest(DefaultWarpInteractiveDevice device)
    {
        DefaultWarpDevice warpDevice = (DefaultWarpDevice) device.getWarpDevice();
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(warpDevice.getConnectServiceClass());
        if(info != null)
        {
            IWarpEngine warpDrive = mLocalDevice.getWarpEngine();
            IWarpServiceLauncher launcher = warpDrive.getLauncherForService(info.name());
            launcher.initializeService(WarpResourceLibrary.getInstance(),mAccessKey, ServiceOperation.CALL);
            IWarpServiceListener listener = warpDrive.getListenerForService(info.name(),
                    launcher.getServiceListenerParameters(device, ServiceOperation.CALL),ServiceOperation.CALL);
            warpDrive.callLocalService(info.name(),listener,launcher.getServiceParameters(device,
                    ServiceOperation.CALL));
        }
    }

    @Override
    public void onDisconnectRequest(DefaultWarpInteractiveDevice device)
    {
        DefaultWarpDevice warpDevice = (DefaultWarpDevice) device.getWarpDevice();
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(warpDevice.getDisconnectServiceClass());
        if(info != null)
        {
            IWarpEngine warpDrive = mLocalDevice.getWarpEngine();
            IWarpServiceLauncher launcher = warpDrive.getLauncherForService(info.name());
            launcher.initializeService(WarpResourceLibrary.getInstance(),mAccessKey, ServiceOperation.CALL);
            IWarpServiceListener listener = warpDrive.getListenerForService(info.name(),
                    launcher.getServiceListenerParameters(device, ServiceOperation.CALL), ServiceOperation.CALL);
            warpDrive.callLocalService(info.name(),listener,launcher.getServiceParameters(device,
                    ServiceOperation.CALL));
        }
    }

    @Override
    public void onPushServiceRequest(final DefaultWarpInteractiveDevice device, final WarpServiceInfo serviceDescriptor)
    {
        final DefaultWarpServiceLauncher launcher = (DefaultWarpServiceLauncher)
                mLocalDevice.getWarpEngine().getLauncherForService(serviceDescriptor.name());
        launcher.setInitializationListener(new IWarpServiceLauncher.IWarpServiceInitializationListener() {
            @Override
            public void onInitializationCompleted()
            {
                Object [] listenerParams = launcher.getServiceListenerParameters(device, ServiceOperation.CALL);
                Object [] params = launcher.getServiceParameters(device, ServiceOperation.CALL);
                IWarpable[] remoteParams = launcher.getServiceRemoteParameters();
                mLocalDevice.getWarpEngine().callPushService(serviceDescriptor.name(), device.getWarpDevice(),
                        mLocalDevice.getWarpEngine().getListenerForService(
                        serviceDescriptor.name(), listenerParams, ServiceOperation.CALL),
                        null, params, remoteParams);
            }
        });
        initializeService(launcher);
    }

    @Override
    public void onPullServiceRequest(final DefaultWarpInteractiveDevice device, final WarpServiceInfo serviceDescriptor)
    {
        final DefaultWarpServiceLauncher launcher = (DefaultWarpServiceLauncher)
                mLocalDevice.getWarpEngine().getLauncherForService(serviceDescriptor.name());
        launcher.setInitializationListener(new IWarpServiceLauncher.IWarpServiceInitializationListener() {
            @Override
            public void onInitializationCompleted()
            {
                Object [] listenerParams = launcher.getServiceListenerParameters(device, ServiceOperation.CALL);
                Object [] params = launcher.getServiceParameters(device, ServiceOperation.CALL);
                IWarpable[] remoteParams = launcher.getServiceRemoteParameters();
                mLocalDevice.getWarpEngine().callPullService(serviceDescriptor.name(), device.getWarpDevice(),
                        mLocalDevice.getWarpEngine().getListenerForService(
                        serviceDescriptor.name(), listenerParams, ServiceOperation.CALL),
                        null, params, remoteParams);
            }
        });
        initializeService(launcher);
    }

    private void initializeService(DefaultWarpServiceLauncher launcher)
    {
        WarpResourceLibrary library = WarpResourceLibrary.getInstance();
        String userKey = (String)library.getResource(mAccessKey,
                WarpResourceLibrary.RES_USER_PERMISSION_KEY);
        launcher.initializeService(library,userKey,ServiceOperation.CALL);
    }

    @Override
    public void onServicesLookupRequest(DefaultWarpInteractiveDevice device, IWarpServiceListener listener)
    {
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(LookupService.class);
        if(info != null)
        {
            //TODO: take a look here!
            //mLocalDevice.getWarpEngine().callPullService(info.name(),device,callListener,null,null,null);
        }
    }
}
