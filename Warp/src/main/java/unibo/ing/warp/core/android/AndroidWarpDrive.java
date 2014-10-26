package unibo.ing.warp.core.android;

import android.content.Context;
import unibo.ing.warp.core.*;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.service.*;
import unibo.ing.warp.core.service.android.p2p.DirectWifiConnectService;
import unibo.ing.warp.core.service.android.p2p.DirectWifiDiscoverService;
import unibo.ing.warp.core.service.android.p2p.DirectWifiPingService;
import unibo.ing.warp.core.service.android.wifi.WifiConnectService;
import unibo.ing.warp.core.service.android.wifi.WifiDisconnectService;
import unibo.ing.warp.core.service.android.wifi.WifiScanService;
import unibo.ing.warp.core.service.base.*;
import unibo.ing.warp.core.service.launcher.IWarpServiceLauncher;
import unibo.ing.warp.core.service.launcher.WarpResourceLibrary;
import unibo.ing.warp.core.service.listener.DefaultEmptyWarpServiceListener;
import unibo.ing.warp.core.service.listener.DefaultWarpServiceListener;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.warpable.IWarpable;
import unibo.ing.warp.utils.WarpUtils;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cronic90 on 06/10/13.
 *
 * Basic implementation of the IWarpEngine interface. This implementation only suggests
 * the default behaviour when receiving Service requests, which need to be dispatched
 * to the correct IWarpService, contained by the AndroidWarpDrive itself in a data structure.
 *
 * This implementation considers the IWarpEngine to have a Map<Name,Service> of IWarpServices,
 * but the Services could also be stored elsewhere (list, tree, singleton class, etc).
 * The interface doesn't require a specific structure to contain the Services, since they
 * can be accessed outside the AndroidWarpDrive by returning them as a Collection.
 *
 * This default implementation makes use of Threads when calling a Push or Pull service,
 * since these operations are remote and should be done in background. This is automatically
 * done by the WarpHandshakeService and WarpTCPDispatcherService, which create a new Thread
 * for each incoming or outgoing request.
 *
 * NOTE: the basic Map<Name, Service> already contains an instance of each registered service.
 * IWarpServices are not Threads though, meaning that in case of multiple requests for a
 * single service, concurrency needs to be handled. Since IWarpServices may contain status,
 * the default behaviour is to instance a new service at runtime if the already mapped one
 * has ServiceStatus = RUNNING || LISTENING || COMPLETED. The new instance of the IWarpService
 * object will be bound to that specific request, and garbage collected after the result of
 * the service has been collected.
 */
public class AndroidWarpDrive implements IWarpEngine {
    private Context mContext;
    private IWarpServiceContainer mContainer;
    private Map<String, WarpServiceInfo> mDescriptors;
    private String mMasterKey;

    /**
     * This constructor should be called for remote WarpDrives, since it doesn't set a Context,
     * automatically sets a non local WarpLocation and doesn't start any background Service.
     * A remote node's AndroidWarpDrive should in fact have no services running at all. It should
     * be a shell containing only the remote DriveLocation, once it's set.
     */
    public AndroidWarpDrive(Context context, String masterKey)
    {
        if(context!=null)
        {
            mContext=context;
        }
        mMasterKey = masterKey;
        mContainer= new AndroidWarpServiceContainer(mMasterKey);
        mDescriptors = new HashMap<String, WarpServiceInfo>();

        //ADDING CORE SERVICES
        addWarpService(WarpTCPDispatcherService.class);
        addWarpService(WarpUDPDispatcherService.class);
        addWarpService(WarpHandshakeService.class);
        addWarpService(LookupService.class);
        addWarpService(PushObjectService.class);
        addWarpService(WarpBeaconService.class);
        //ADDING ADDITIONAL SERVICES
        addWarpService(WarpLighthouseService.class);
        addWarpService(WifiScanService.class);
        addWarpService(WifiConnectService.class);
        addWarpService(WifiDisconnectService.class);
        addWarpService(DirectWifiDiscoverService.class);
        addWarpService(DirectWifiConnectService.class);
        addWarpService(DirectWifiPingService.class);
        addWarpService(PushFileService.class);
    }

    @Override
    public Object getContext()
    {
        return mContext;
    }

    @Override
    public void addWarpService(Class<? extends IWarpService> serviceClass)
    {
        /* We are not overriding any listed Service by default. If someone tries
        to add a Service with the same name as an existing one, the new service
        just isn't added. To change this behaviour please subclass. */
        if(mContainer!=null)
        {
            WarpServiceInfo info = mContainer.registerWarpService(serviceClass);
            if(info != null)
            {
                mDescriptors.put(info.name(),info);
            }
        }
    }

    @Override
    public void callLocalService(String serviceName, IWarpServiceListener listener,
                                 Object [] params)
    {
        Class<? extends IWarpService> serviceClass = mContainer.getRegisteredWarpServiceByName(serviceName);
        if(serviceClass == null)
        {
            return;
        }
        WarpServiceInfo info = mDescriptors.get(serviceName);

        if(info == null)
        {
            info = WarpUtils.getWarpServiceInfo(serviceClass);
        }
        if(info != null && info.name().equals(serviceName) && info.type()== WarpServiceInfo.Type.LOCAL
                && info.protocol() == WarpServiceInfo.Protocol.NONE)
        {
            //Current found service is a Local Service
            mContainer.startLocalWarpService(serviceClass,info,this,listener,params);
        }
    }

    @Override
    public Collection<Class<? extends IWarpService>> getServiceList()
    {
        //Just returning the classes Collection.
        return mContainer.getRegisteredWarpServices();
    }

    @Override
    public long [] getActiveServicesIdsByName(String serviceName)
    {
        return mContainer.getRunningServicesIdsByName(serviceName);
    }

    @Override
    public Collection<String> getServicesNames()
    {
        return mContainer.getRegisteredWarpServicesNames();
    }

    @Override
    public Class<? extends IWarpService> getServiceByName(String name)
    {
        return mContainer.getRegisteredWarpServiceByName(name);
    }

    @Override
    public void callPushService(String serviceName, IWarpDevice to, IWarpServiceListener listener,
                                IBeam warpBeam, Object [] params, IWarpable[] remoteParams)
    {
        Class<? extends IWarpService> serviceClass = mContainer.getRegisteredWarpServiceByName(serviceName);
        if(serviceClass==null)
        {
            return;
        }
        WarpServiceInfo info = mDescriptors.get(serviceName);
        if(info == null)
        {
            info = WarpUtils.getWarpServiceInfo(serviceClass);
        }
        if(info != null && info.name().equals(serviceName) && info.type()== WarpServiceInfo.Type.PUSH
                && info.protocol() != WarpServiceInfo.Protocol.NONE)
        {
            if(warpBeam != null && !(Boolean)warpBeam.getFlag(WarpFlag.MASTER.name()).getValue())
            {
                mContainer.startServerRemoteWarpService(serviceClass, info, warpBeam, listener, this, params);
            }
            else
            {
                mContainer.startClientRemoteWarpService(serviceClass,info,this,to,listener,params,remoteParams);
            }
        }
    }

    @Override
    public void callPullService(String serviceName, IWarpDevice from, IWarpServiceListener listener,
                                IBeam warpBeam, Object [] params, IWarpable [] remoteParams)
    {
        Class<? extends IWarpService> serviceClass = mContainer.getRegisteredWarpServiceByName(serviceName);
        if(serviceClass==null)
        {
            return;
        }
        WarpServiceInfo info = mDescriptors.get(serviceName);
        if(info == null)
        {
            info = WarpUtils.getWarpServiceInfo(serviceClass);
        }
        if(info != null && info.name().equals(serviceName) && info.type()== WarpServiceInfo.Type.PULL
                && info.protocol() != WarpServiceInfo.Protocol.NONE)
        {
            if(warpBeam != null && !(Boolean)warpBeam.getFlag(WarpFlag.MASTER.name()).getValue())
            {
                mContainer.startServerRemoteWarpService(serviceClass, info, warpBeam, listener, this, params);
            }
            else
            {
                mContainer.startClientRemoteWarpService(serviceClass,info,this,from,listener,params,remoteParams);
            }
        }
    }

    @Override
    public void stopService(long serviceId)
    {
        mContainer.stopService(serviceId);
    }

    @Override
    public void startEngine()
    {
        mContainer.startContainer();
        /*Request Dispatcher Service is called on startup, and works as a Daemon.
        If the container Activity is garbaged though, the background Thread will be killed,
        instead of acting as a System service. */
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(WarpTCPDispatcherService.class);
        IWarpServiceLauncher launcher = getLauncherForService(info.name());
        launcher.initializeService(WarpResourceLibrary.getInstance(),mMasterKey, IWarpService.ServiceOperation.CALL);
        callLocalService(info.name(), null, launcher.getServiceParameters(null, IWarpService.ServiceOperation.CALL));
        info = WarpUtils.getWarpServiceInfo(WarpUDPDispatcherService.class);
        callLocalService(info.name(), null, launcher.getServiceParameters(null, IWarpService.ServiceOperation.CALL));
    }

    @Override
    public void stopEngine()
    {
        //TODO: to implement
    }

    @Override
    public IWarpServiceListener getListenerForService(String serviceName, Object [] values,
                                                      IWarpService.ServiceOperation operation)
    {
        DefaultWarpServiceListener listener = null;
        WarpServiceInfo info = mDescriptors.get(serviceName);
        if(info == null)
        {
            info = WarpUtils.getWarpServiceInfo(mContainer.getRegisteredWarpServiceByName(serviceName));
        }
        if(info != null)
        {
            if(info.callListener() != DefaultEmptyWarpServiceListener.class)
            {
                try {
                    listener = (DefaultWarpServiceListener) info.callListener().newInstance();
                    listener.putDefaultValues(values);
                }
                catch (Exception e)
                {
                    //TODO: handle error!
                }
            }
        }
        return listener;
    }

    @Override
    public IWarpServiceLauncher getLauncherForService(String serviceName)
    {
        WarpServiceInfo info = mDescriptors.get(serviceName);
        IWarpServiceLauncher launcher = null;
        if(info == null)
        {
            info = WarpUtils.getWarpServiceInfo(mContainer.getRegisteredWarpServiceByName(serviceName));
        }
        if(info != null)
        {
            try {
                launcher = info.launcher().newInstance();
            }
            catch (Exception e)
            {
                //TODO: handle error!
            }
        }
        return launcher;
    }
}
