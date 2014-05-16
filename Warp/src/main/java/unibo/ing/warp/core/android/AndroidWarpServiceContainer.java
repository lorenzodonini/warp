package unibo.ing.warp.core.android;

import android.os.Handler;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.IWarpBeamObserver;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.utils.WarpUtils;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.IWarpServiceContainer;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.base.WarpDispatcherService;
import unibo.ing.warp.core.service.base.WarpHandshakeService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Lorenzo Donini on 4/16/2014.
 */
public class AndroidWarpServiceContainer implements IWarpServiceContainer, IWarpBeamObserver {
    private Map<String, Class<? extends IWarpService>> mRegisteredServices;
    private static final int LIGHTWEIGHT_THREAD_NUM = 4;
    private ExecutorService mExecutor;
    private Handler mHandler;
    private Map<String, Collection<Runnable>> mRunningServices;
    private Map<IBeam, IWarpService> mActiveBeams;

    public AndroidWarpServiceContainer()
    {
        mRegisteredServices = new HashMap<String, Class<? extends IWarpService>>();
        mRunningServices = new HashMap<String, Collection<Runnable>>();
        mActiveBeams = new HashMap<IBeam, IWarpService>();
        mHandler = new Handler();
    }

    @Override
    public void startContainer()
    {
        if(mExecutor!=null)
        {
            mExecutor.shutdown();
        }
        mExecutor= Executors.newFixedThreadPool(LIGHTWEIGHT_THREAD_NUM);
    }

    @Override
    public void stopContainer()
    {
        if(mExecutor!=null)
        {
            mExecutor.shutdown();
            mExecutor=null;
        }
    }

    @Override
    public WarpServiceInfo registerWarpService(Class<? extends IWarpService> warpService)
    {
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(warpService);
        if(info != null && !mRegisteredServices.containsKey(info.name()))
        {
            mRegisteredServices.put(info.name(),warpService);
        }
        return info;
    }

    @Override
    public boolean unregisterWarpService(String serviceName)
    {
        return mRegisteredServices.remove(serviceName)!=null;
    }

    @Override
    public boolean isWarpServiceRegistered(String serviceName)
    {
        return mRegisteredServices.containsKey(serviceName);
    }

    @Override
    public Collection<Class<? extends IWarpService>> getRegisteredWarpServices()
    {
        return mRegisteredServices.values();
    }

    @Override
    public Collection<String> getRegisteredWarpServicesNames()
    {
        return mRegisteredServices.keySet();
    }

    @Override
    public Class<? extends IWarpService> getRegisteredWarpServiceByName(String serviceName)
    {
        return mRegisteredServices.get(serviceName);
    }

    @Override
    public boolean isWarpServiceRunning(String serviceName)
    {
        Collection<Runnable> runningServices = mRunningServices.get(serviceName);
        return runningServices!=null && runningServices.size()!=0;
    }

    @Override
    public int getWarpServiceRunningInstances(String serviceName)
    {
        Collection<Runnable> runningServices = mRunningServices.get(serviceName);
        if(runningServices!= null)
        {
            return runningServices.size();
        }
        return 0;
    }

    @Override
    public IWarpService.ServiceStatus getWarpServiceStatus(String serviceName)
    {
        //TODO: CORRECT?!?!
        return null;
    }

    @Override
    public void startLocalWarpService(Class<? extends IWarpService> serviceClass, IWarpEngine callerEngine,
                                      IWarpServiceListener listener, Object [] params)
    {
        //LOCAL SERVICE
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(serviceClass);
        if(info != null)
        {
            //Start service directly, without handshake policy
            try {
                IWarpService service = mRegisteredServices.get(info.name()).newInstance();
                if(info.execution()== WarpServiceInfo.ServiceExecution.DEFAULT
                        || info.execution()== WarpServiceInfo.ServiceExecution.SEQUENTIAL)
                {
                    //EXECUTE ON CURRENT THREAD
                    AndroidHandler taskHandler = new AndroidSequentialHandler(listener,this);
                    try {
                        service.setWarpServiceHandler(taskHandler);
                        //Service is from now on Running
                        taskHandler.setHandledServiceStatus(IWarpService.ServiceStatus.RUNNING);
                        service.callService(null,callerEngine.getContext(),params);
                        taskHandler.completeService(null,service);
                    }
                    catch (Exception e)
                    {
                        taskHandler.abortService(null,service,e.getMessage());
                    }
                }
                else
                {
                    LocalWarpServiceRunnable thread = new LocalWarpServiceRunnable(
                            service,callerEngine,listener,params);
                    mExecutor.execute(thread);
                }
            }
            //TODO: need to handle errors properly
            catch (InstantiationException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }
    }

    private IWarpService initializeWarpServiceToStart(Class<? extends IWarpService> serviceClass)
            throws IllegalAccessException, InstantiationException {
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(serviceClass);
        if(info != null)
        {
            IWarpService service = mRegisteredServices.get(info.name()).newInstance();
            if(info.execution()== WarpServiceInfo.ServiceExecution.DEFAULT
                    || info.execution()== WarpServiceInfo.ServiceExecution.CONCURRENT)
            {
                return service;
            }
        }
        return null;
    }

    @Override
    public void startClientRemoteWarpService(Class<? extends IWarpService> serviceClass, IWarpEngine callerEngine,
                        IWarpDevice target, IWarpServiceListener listener, Object [] params, Object [] remoteParams)
    {
        try {
            IWarpService service = initializeWarpServiceToStart(serviceClass);
            if(service != null)
            {
                ClientRemoteWarpServiceRunnable thread = new ClientRemoteWarpServiceRunnable(service,
                        callerEngine,target,listener,params,remoteParams);
                mExecutor.execute(thread);
            }
            else
            {
                //TODO: Throw some sort of error!
            }
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void startServerRemoteWarpService(Class<? extends IWarpService> serviceClass, IBeam warpBeam,
                        IWarpServiceListener listener, Object [] params)
    {
        try {
            IWarpService service = initializeWarpServiceToStart(serviceClass);
            if(service != null)
            {
                ServerRemoteWarpServiceRunnable thread = new ServerRemoteWarpServiceRunnable(warpBeam,
                        service,listener,params);
                mExecutor.execute(thread);
            }
            else
            {
                //TODO: Throw some sort of error!
            }
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
    }

    /*############### ACTIVE BEAMS LOGIC ###############*/
    @Override
    public void onBeamCreate(IBeam warpBeam, IWarpService service)
    {
        if(!mActiveBeams.containsKey(warpBeam))
        {
            mActiveBeams.put(warpBeam,service);
        }
    }

    @Override
    public void onBeamDestroy(IBeam warpBeam, IWarpService service)
    {
        if(mActiveBeams.containsKey(warpBeam))
        {
            mActiveBeams.remove(warpBeam);
        }
    }

    private class LocalWarpServiceRunnable implements Runnable {
        private IWarpService toRun;
        private Object [] mParams;
        private IWarpEngine mLocalEngine;
        private AndroidHandler taskHandler;

        public LocalWarpServiceRunnable(IWarpService service, IWarpEngine localEngine,
                                        IWarpServiceListener listener, Object [] params)
        {
            taskHandler = new AndroidConcurrentHandler(listener,AndroidWarpServiceContainer.this,mHandler);
            toRun=service;
            mLocalEngine=localEngine;
            mParams=params;
        }

        @Override
        public void run()
        {
            toRun.setWarpServiceHandler(taskHandler);
            try {
                //Service is from now on Running
                taskHandler.setHandledServiceStatus(IWarpService.ServiceStatus.RUNNING);
                toRun.callService(null,mLocalEngine.getContext(),mParams);
                taskHandler.completeService(null,toRun);
            }
            catch (Exception e)
            {
                taskHandler.abortService(null,toRun,e.getMessage());
            }
        }
    }

    private class ClientRemoteWarpServiceRunnable implements Runnable {
        private IWarpService toRun;
        private IWarpEngine localEngine;
        private IWarpDevice remoteDevice;
        private Object [] mParams;
        private Object [] mRemoteParams;
        private AndroidHandler taskHandler;

        public ClientRemoteWarpServiceRunnable(IWarpService service, IWarpEngine local, IWarpDevice remote,
                                        IWarpServiceListener listener, Object [] params, Object [] remoteParams)
        {
            taskHandler = new AndroidConcurrentHandler(listener,AndroidWarpServiceContainer.this,mHandler);
            localEngine=local;
            toRun=service;
            remoteDevice=remote;
            mParams=params;
            mRemoteParams=remoteParams;
        }

        @Override
        public void run()
        {
            WarpServiceInfo info = WarpUtils.getWarpServiceInfo(WarpHandshakeService.class);
            IWarpService handshakeService = null;
            IBeam warpBeam = null;
            if(info != null && getRegisteredWarpServiceByName(info.name())!=null)
            {
                try {
                    handshakeService = WarpHandshakeService.class.newInstance();
                }
                catch (InstantiationException e)
                {
                    //TODO: HANDLING
                    return;
                }
                catch (IllegalAccessException e)
                {
                    //TODO: HANDLING
                    return;
                }

                if(handshakeService == null)
                {
                    //TODO: HANDLING
                    return;
                }

                try {
                    //Parameters to send to the handshake service
                    Object [] handshakeServiceParams = new Object[4];
                    handshakeServiceParams[0]=localEngine;
                    handshakeServiceParams[1]=remoteDevice.getDeviceLocation();
                    handshakeServiceParams[2]=toRun;
                    handshakeServiceParams[3]=mRemoteParams;
                    handshakeService.setWarpServiceHandler(taskHandler);
                    //Service is from now Handshaking
                    taskHandler.setHandledServiceStatus(IWarpService.ServiceStatus.HANDSHAKING);
                    handshakeService.callService(null,null,handshakeServiceParams);
                    Object [] result = handshakeService.getResult();
                    if(result == null || !(result[0] instanceof IBeam))
                    {
                        //TODO: handle error!
                    }
                    else
                    {
                        warpBeam = (IBeam) result[0];
                        taskHandler.onBeamCreate(warpBeam,toRun);
                        toRun.setWarpServiceHandler(taskHandler);
                        //Service is from now on Running
                        taskHandler.setHandledServiceStatus(IWarpService.ServiceStatus.RUNNING);
                        toRun.callService(warpBeam,localEngine.getContext(),mParams);
                        taskHandler.completeService(warpBeam,toRun);
                    }
                }
                catch(Exception e)
                {
                    taskHandler.abortService(warpBeam,toRun,e.getMessage());
                }
            }
        }
    }

    private class ServerRemoteWarpServiceRunnable implements Runnable {
        private IWarpService toRun;
        private IBeam mWarpBeam;
        private Object [] mParams;
        private AndroidHandler taskHandler;

        public ServerRemoteWarpServiceRunnable(IBeam warpBeam, IWarpService service,
                                        IWarpServiceListener listener, Object [] params)
        {
            taskHandler=new AndroidConcurrentHandler(listener,AndroidWarpServiceContainer.this,mHandler);
            mWarpBeam=warpBeam;
            toRun=service;
            mParams=params;
            taskHandler.onBeamCreate(mWarpBeam,toRun);
        }

        @Override
        public void run()
        {
            toRun.setWarpServiceHandler(taskHandler);
            try {
                //Service is from now on Running
                taskHandler.setHandledServiceStatus(IWarpService.ServiceStatus.RUNNING);
                toRun.provideService(mWarpBeam, mWarpBeam.getLocalWarpEngine().getContext(), mParams);
                taskHandler.completeService(mWarpBeam,toRun);
            }
            catch (Exception e)
            {
                taskHandler.abortService(mWarpBeam,toRun,e.getMessage());
            }
        }
    }
}
