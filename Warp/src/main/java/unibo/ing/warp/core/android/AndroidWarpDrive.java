package unibo.ing.warp.core.android;

import android.content.Context;
import unibo.ing.warp.core.*;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.service.*;
import unibo.ing.warp.core.service.base.LookupService;
import unibo.ing.warp.core.service.base.PushObjectService;
import unibo.ing.warp.core.service.listener.DefaultWarpServiceListener;
import unibo.ing.warp.core.service.listener.android.AndroidWarpServiceListenerFactory;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.service.listener.IWarpServiceListenerFactory;
import unibo.ing.warp.utils.WarpUtils;
import java.util.Collection;

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
 * done by the WarpHandshakeService and WarpDispatcherService, which create a new Thread
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
    private IWarpServiceListenerFactory mListenerFactory;

    /**
     * This constructor should be called for remote WarpDrives, since it doesn't set a Context,
     * automatically sets a non local WarpLocation and doesn't start any background Service.
     * A remote node's AndroidWarpDrive should in fact have no services running at all. It should
     * be a shell containing only the remote DriveLocation, once it's set.
     */
    public AndroidWarpDrive(Context context)
    {
        if(context!=null)
        {
            mContext=context;
        }
        mContainer= new AndroidWarpServiceContainer();
        mListenerFactory = new AndroidWarpServiceListenerFactory();

        //ADDING CORE SERVICES
        addWarpService(AndroidWarpServiceContainer.DISPATCHER_SERVICE_CLASS);
        addWarpService(AndroidWarpServiceContainer.HANDSHAKE_SERVICE_CLASS);
        addWarpService(LookupService.class);
        addWarpService(PushObjectService.class);
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
            Class<? extends DefaultWarpServiceListener> listenerClass =
                    AndroidListenerMapping.getListenerClass(serviceClass);
            if(listenerClass != null)
            {
                mListenerFactory.addWarpServiceListenerMapping(info.name(),listenerClass);
            }
        }
    }

    @Override
    public void callLocalService(String serviceName, IWarpServiceListener listener,
                                 Object [] params)
    {
        Class<? extends IWarpService> serviceClass;
        WarpServiceInfo info;

        serviceClass=mContainer.getRegisteredWarpServiceByName(serviceName);
        if(serviceClass!=null)
        {
            info = WarpUtils.getWarpServiceInfo(serviceClass);
            if(info.name().equals(serviceName) &&
                    info.type()== WarpServiceInfo.Type.LOCAL)
            {
                //Current found service is a Local Service
                mContainer.startLocalWarpService(serviceClass,this,listener,params);
            }
        }
    }

    @Override
    public Collection<Class<? extends IWarpService>> getServiceList()
    {
        //Just returning the classes Collection.
        return mContainer.getRegisteredWarpServices();
    }

    public Collection<IWarpService> getActiveServices()
    {
        return null; //TODO: IMPLEMENT
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
                                IBeam warpBeam, Object [] params, Object [] remoteParams)
    {
        Class<? extends IWarpService> serviceClass = mContainer.getRegisteredWarpServiceByName(serviceName);
        if(serviceClass==null)
        {
            return;
        }
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(serviceClass);
        if(info != null && info.name().equals(serviceName) && info.type()== WarpServiceInfo.Type.PUSH)
        {
            if(warpBeam != null && !(Boolean)warpBeam.getFlag(WarpFlag.MASTER.name()).getValue())
            {
                mContainer.startServerRemoteWarpService(serviceClass,warpBeam,listener,params);
            }
            else
            {
                mContainer.startClientRemoteWarpService(serviceClass,this,to,listener,params,remoteParams);
            }
        }
    }

    @Override
    public void callPullService(String serviceName, IWarpDevice from, IWarpServiceListener listener,
                                IBeam warpBeam, Object [] params, Object [] remoteParams)
    {
        Class<? extends IWarpService> serviceClass = mContainer.getRegisteredWarpServiceByName(serviceName);
        if(serviceClass==null)
        {
            return;
        }
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(serviceClass);
        if(info != null && info.name().equals(serviceName) && info.type()== WarpServiceInfo.Type.PULL)
        {
            if(warpBeam != null && !(Boolean)warpBeam.getFlag(WarpFlag.MASTER.name()).getValue())
            {
                mContainer.startServerRemoteWarpService(serviceClass,warpBeam,listener,params);
            }
            else
            {
                mContainer.startClientRemoteWarpService(serviceClass,this,from,listener,params,remoteParams);
            }
        }
    }

    @Override
    public void startEngine()
    {
        mContainer.startContainer();
        /*Request Dispatcher Service is called on startup, and works as a Daemon.
        If the container Activity is garbaged though, the background Thread will be killed,
        instead of acting as a System service. */
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(AndroidWarpServiceContainer.DISPATCHER_SERVICE_CLASS);
        callLocalService(info.name(),null,new Object [] {this});
    }

    @Override
    public void stopEngine()
    {
        //TODO: to implement
    }

    @Override
    public IWarpServiceListener getDefaultListenerForService(String serviceName, Object [] values)
    {
        return mListenerFactory.createWarpServiceListener(serviceName,values);
    }
}
