package unibo.ing.warp.core;

import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.warpable.IWarpable;

import java.util.Collection;

/**
 * Created by Lorenzo Donini on 4/15/2014.
 */
public interface IWarpServiceContainer {
    public void startContainer();
    public void stopContainer();
    public WarpServiceInfo registerWarpService(Class<? extends IWarpService> warpService);
    public boolean unregisterWarpService(String serviceName);
    public boolean isWarpServiceRegistered(String serviceName);
    public Collection<Class<? extends IWarpService>> getRegisteredWarpServices();
    public Collection<String> getRegisteredWarpServicesNames();
    public Class<? extends IWarpService> getRegisteredWarpServiceByName(String serviceName);
    public boolean isWarpServiceRunning(String serviceName);
    public Collection<IWarpService> getWarpServiceRunningInstances();
    public IWarpService getRunningServiceInstanceById(long serviceId);
    public long [] getRunningServicesIdsByName(String serviceName);
    public Collection<IWarpService> getWarpRunningServiceInstancesByName(String serviceName);
    public IWarpService.ServiceStatus getWarpServiceStatus(long serviceId);
    public void startLocalWarpService(Class<? extends IWarpService> serviceClass, WarpServiceInfo serviceInfo,
                    IWarpEngine callerEngine, IWarpServiceListener listener, Object[] params);
    public void startClientRemoteWarpService(Class<? extends IWarpService> serviceClass,
                    WarpServiceInfo serviceInfo, IWarpEngine callerEngine, IWarpDevice target,
                    IWarpServiceListener listener, Object[] params, IWarpable[] remoteParams);
    public void startServerRemoteWarpService(Class<? extends IWarpService> serviceClass,
                    WarpServiceInfo serviceInfo, IBeam warpBeam, IWarpServiceListener listener, Object[] params);
    public void stopService(long serviceId);
}
