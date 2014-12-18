package unibo.ing.warp.core.android;

import android.os.Handler;
import android.util.Log;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.IWarpServiceObserver;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.service.launcher.WarpHandshakeLauncher;
import unibo.ing.warp.core.service.launcher.WarpResourceLibrary;
import unibo.ing.warp.core.warpable.IWarpable;
import unibo.ing.warp.utils.WarpUtils;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.IWarpServiceContainer;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.base.WarpHandshakeService;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by Lorenzo Donini on 4/16/2014.
 */
public class AndroidWarpServiceContainer implements IWarpServiceContainer, IWarpServiceObserver {
    private Map<String, Class<? extends IWarpService>> mRegisteredServices;
    private static final int THREAD_CORE_NUM = 8;
    private static final int THREAD_MAX_NUM = 8;
    private static final int QUEUE_MAX = 5;
    private static final int THREAD_KEEP_ALIVE_TIME = 100;
    private ThreadPoolExecutor mExecutor;
    private Handler mHandler;
    private long mBaseId;
    private String mMasterKey;
    private Map<String, Collection<Long>> mServicesIds;
    private Map<Long, Runnable> mActiveTasks;
    private Map<Long, IWarpService> mRunningServices;
    private Map<IBeam, IWarpService> mActiveBeams;

    public AndroidWarpServiceContainer(String masterKey)
    {
        mRegisteredServices = new HashMap<String, Class<? extends IWarpService>>();
        mServicesIds = new HashMap<String, Collection<Long>>();
        mActiveTasks = new HashMap<Long, Runnable>();
        mRunningServices = new HashMap<Long, IWarpService>();
        mActiveBeams = new HashMap<IBeam, IWarpService>();
        mHandler = new Handler();
        mMasterKey = masterKey;
        mBaseId = 123; //TODO: change this somehow
    }

    @Override
    public void startContainer()
    {
        if(mExecutor!=null)
        {
            mExecutor.shutdown();
        }
        mExecutor = new ThreadPoolExecutor(THREAD_CORE_NUM,THREAD_MAX_NUM,THREAD_KEEP_ALIVE_TIME,
                TimeUnit.MILLISECONDS,new ArrayBlockingQueue<Runnable>(QUEUE_MAX));
        Log.d("WARP.DEBUG","AndroidWarpServiceContainer.startContainer");
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
    public synchronized WarpServiceInfo registerWarpService(Class<? extends IWarpService> warpService)
    {
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(warpService);
        if(info != null && !mRegisteredServices.containsKey(info.name()))
        {
            mRegisteredServices.put(info.name(),warpService);
        }
        return info;
    }

    @Override
    public synchronized boolean unregisterWarpService(String serviceName)
    {
        return mRegisteredServices.remove(serviceName)!=null;
    }

    @Override
    public synchronized boolean isWarpServiceRegistered(String serviceName)
    {
        return mRegisteredServices.containsKey(serviceName);
    }

    @Override
    public synchronized Collection<Class<? extends IWarpService>> getRegisteredWarpServices()
    {
        return mRegisteredServices.values();
    }

    @Override
    public synchronized Collection<String> getRegisteredWarpServicesNames()
    {
        return mRegisteredServices.keySet();
    }

    @Override
    public synchronized Class<? extends IWarpService> getRegisteredWarpServiceByName(String serviceName)
    {
        return mRegisteredServices.get(serviceName);
    }

    @Override
    public synchronized boolean isWarpServiceRunning(String serviceName)
    {
        return mServicesIds.containsKey(serviceName) && mServicesIds.get(serviceName).size() > 0;
    }

    @Override
    public synchronized Collection<IWarpService> getWarpServiceRunningInstances()
    {
        return mRunningServices.values();
    }

    @Override
    public synchronized IWarpService getRunningServiceInstanceById(long serviceId)
    {
        return mRunningServices.get(serviceId);
    }

    @Override
    public synchronized long [] getRunningServicesIdsByName(String serviceName)
    {
        Collection<Long> ids = mServicesIds.get(serviceName);
        if(ids == null)
        {
            return null;
        }
        long result [] = new long[ids.size()];
        int i=0;
        for(Long id: ids)
        {
            result[i++]=id;
        }
        return result;
    }

    @Override
    public synchronized Collection<IWarpService> getWarpRunningServiceInstancesByName(String serviceName)
    {
        Collection<Long> ids = mServicesIds.get(serviceName);
        if(ids == null)
        {
            return null;
        }
        List<IWarpService> result = new ArrayList<IWarpService>(ids.size());
        for(long id: ids)
        {
            result.add(mRunningServices.get(id));
        }
        return result;
    }

    @Override
    public synchronized void stopService(long serviceId)
    {
        IWarpService serviceToStop = mRunningServices.get(serviceId);
        if(serviceToStop != null)
        {
            serviceToStop.stopService();
            onServiceDestroy(serviceToStop);
        }
        //TODO: maybe return a boolean?!
    }

    @Override
    public synchronized IWarpService.ServiceStatus getWarpServiceStatus(long serviceId)
    {
        IWarpService service = mRunningServices.get(serviceId);
        return (service != null) ? service.getServiceStatus() : null;
    }

    @Override
    public synchronized void startLocalWarpService(Class<? extends IWarpService> serviceClass, WarpServiceInfo serviceInfo,
                                      IWarpEngine callerEngine, IWarpServiceListener listener, Object [] params)
    {
        //LOCAL SERVICE
        long serviceId;
        if(serviceInfo != null)
        {
            //Start service directly, without handshake policy
            try {
                IWarpService service = mRegisteredServices.get(serviceInfo.name()).newInstance();
                if(serviceInfo.execution()== WarpServiceInfo.ServiceExecution.DEFAULT
                        || serviceInfo.execution()== WarpServiceInfo.ServiceExecution.SEQUENTIAL)
                {
                    //EXECUTE ON CURRENT THREAD
                    serviceId = generateServiceId();
                    AndroidHandler taskHandler = new AndroidSequentialHandler(listener,this,serviceId);
                    try {
                        service.setWarpServiceHandler(taskHandler);
                        //Service is from now on Running
                        taskHandler.setHandledServiceStatus(IWarpService.ServiceStatus.RUNNING);
                        onServiceCreate(service,serviceInfo,serviceId,null); //Adding service to active list
                        service.callService(null,callerEngine.getContext(),params);
                        /*By default local services contain asynchronous policies, so we should
                        give the service programmer the possibility to decide whether a service
                        should be completed automatically or directly inside the service code.
                         */
                        if(serviceInfo.completion() == WarpServiceInfo.ServiceCompletion.IMPLICIT)
                        {
                            taskHandler.completeService(null, service);
                        }
                    }
                    catch (Exception e)
                    {
                        taskHandler.abortService(null,service,e.getMessage());
                    }
                }
                else
                {
                    LocalWarpServiceRunnable thread = new LocalWarpServiceRunnable(
                            service,serviceInfo,callerEngine,listener,params);
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

    private synchronized long generateServiceId()
    {
        return mBaseId++;
    }

    private synchronized IWarpService initializeWarpServiceToStart(WarpServiceInfo serviceInfo)
            throws IllegalAccessException, InstantiationException {
        if(serviceInfo != null)
        {
            IWarpService service = mRegisteredServices.get(serviceInfo.name()).newInstance();
            if(serviceInfo.execution()== WarpServiceInfo.ServiceExecution.DEFAULT
                    || serviceInfo.execution()== WarpServiceInfo.ServiceExecution.CONCURRENT)
            {
                return service;
            }
        }
        return null;
    }

    @Override
    public synchronized void startClientRemoteWarpService(Class<? extends IWarpService> serviceClass, WarpServiceInfo serviceInfo,
                        IWarpEngine callerEngine,IWarpDevice target, IWarpServiceListener listener,
                        Object [] params, IWarpable[] remoteParams)
    {
        try {
            IWarpService service = initializeWarpServiceToStart(serviceInfo);
            if(service != null)
            {
                ClientRemoteWarpServiceRunnable thread = new ClientRemoteWarpServiceRunnable(service, serviceInfo,
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
    public synchronized void startServerRemoteWarpService(Class<? extends IWarpService> serviceClass, WarpServiceInfo serviceInfo,
                        IBeam warpBeam, IWarpServiceListener listener, IWarpEngine callerEngine, Object [] params)
    {
        try {
            IWarpService service = initializeWarpServiceToStart(serviceInfo);
            if(service != null)
            {
                ServerRemoteWarpServiceRunnable thread = new ServerRemoteWarpServiceRunnable(warpBeam,
                        service,serviceInfo,listener,callerEngine,params);
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

    /*############### ACTIVE SERVICES LOGIC ###############*/
    @Override
    public synchronized void onBeamCreate(IBeam warpBeam, IWarpService service)
    {
        if(!mActiveBeams.containsKey(warpBeam))
        {
            mActiveBeams.put(warpBeam,service);
        }
    }

    @Override
    public synchronized void onBeamDestroy(IBeam warpBeam, IWarpService service)
    {
        if(mActiveBeams.containsKey(warpBeam))
        {
            mActiveBeams.remove(warpBeam);
        }
    }

    @Override
    public synchronized void onServiceDestroy(IWarpService service)
    {
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(service.getClass());
        long id = service.getWarpServiceHandler().getHandledServiceId();
        mRunningServices.remove(id);
        mActiveTasks.remove(id);
        mServicesIds.get(info.name()).remove(id);
        if(mServicesIds.get(info.name()).size()==0)
        {
            mServicesIds.remove(info.name());
        }
    }

    private synchronized void onServiceCreate(IWarpService service, WarpServiceInfo info,
                                              long serviceId, Runnable task)
    {
        if(mServicesIds.containsKey(info.name()))
        {
            mServicesIds.get(info.name()).add(serviceId);
        }
        else
        {
            Collection<Long> idList = new LinkedList<Long>();
            idList.add(serviceId);
            mServicesIds.put(info.name(),idList);
        }
        mRunningServices.put(serviceId, service);
        if(task != null)
        {
            mActiveTasks.put(serviceId,task);
        }
    }

    //LOCAL SERVICE DEDICATED TASK
    private class LocalWarpServiceRunnable implements Runnable {
        private IWarpService toRun;
        private Object [] mParams;
        private IWarpEngine mLocalEngine;
        private AndroidHandler taskHandler;
        private WarpServiceInfo mInfo;

        public LocalWarpServiceRunnable(IWarpService service, WarpServiceInfo info, IWarpEngine localEngine,
                                        IWarpServiceListener listener, Object [] params)
        {
            taskHandler = new AndroidConcurrentHandler(listener,AndroidWarpServiceContainer.this,
                    generateServiceId(),mHandler);
            toRun=service;
            mInfo=info;
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
                Log.d("WARP.DEBUG",Thread.currentThread().getName()+" - "+mInfo.name()+
                        ": "+taskHandler.getHandledServiceStatus().name());
                onServiceCreate(toRun,mInfo,taskHandler.getHandledServiceId(),this); //Adding service to active list
                toRun.callService(null,mLocalEngine.getContext(),mParams);
                if(mInfo.completion() == WarpServiceInfo.ServiceCompletion.IMPLICIT)
                {
                    taskHandler.completeService(null,toRun);
                    Log.d("WARP.DEBUG",Thread.currentThread().getName()+" - "+mInfo.name()+
                            ": "+taskHandler.getHandledServiceStatus().name());
                }
            }
            catch (Exception e)
            {
                taskHandler.abortService(null,toRun,e.getMessage());
                Log.d("WARP.DEBUG",Thread.currentThread().getName()+" - "+mInfo.name()+
                        ": "+taskHandler.getHandledServiceStatus().name()+ " # "+e.getMessage()
                        +" - "+e.getLocalizedMessage());
            }
        }
    }

    //REMOTE CLIENT SERVICE DEDICATED TASK
    private class ClientRemoteWarpServiceRunnable implements Runnable {
        private IWarpService toRun;
        private WarpServiceInfo mInfo;
        private IWarpEngine localEngine;
        private IWarpDevice remoteDevice;
        private Object [] mParams;
        private IWarpable [] mRemoteParams;
        private AndroidHandler taskHandler;

        public ClientRemoteWarpServiceRunnable(IWarpService service, WarpServiceInfo info, IWarpEngine local,
                    IWarpDevice remote, IWarpServiceListener listener, Object [] params, IWarpable [] remoteParams)
        {
            taskHandler = new AndroidConcurrentHandler(listener,AndroidWarpServiceContainer.this,
                    generateServiceId(),mHandler);
            localEngine=local;
            toRun=service;
            mInfo=info;
            remoteDevice=remote;
            mParams=params;
            mRemoteParams=remoteParams;
        }

        @Override
        public void run()
        {
            WarpServiceInfo info = WarpUtils.getWarpServiceInfo(WarpHandshakeService.class);
            IWarpService handshakeService;
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
                    WarpHandshakeLauncher launcher = (WarpHandshakeLauncher)
                            localEngine.getLauncherForService(info.name());
                    //Setting parameters to send to the handshake service
                    launcher.setRemoteDeviceLocation(remoteDevice.getDeviceLocation());
                    launcher.setRemoteParameters(mRemoteParams);
                    launcher.setServiceToLaunchDescriptor(mInfo);
                    launcher.initializeService(WarpResourceLibrary.getInstance(),mMasterKey,
                            IWarpService.ServiceOperation.CALL);

                    handshakeService.setWarpServiceHandler(taskHandler);
                    //Service is from now Handshaking
                    taskHandler.setHandledServiceStatus(IWarpService.ServiceStatus.HANDSHAKING);
                    handshakeService.callService(null,null,launcher.getServiceParameters(
                            null, IWarpService.ServiceOperation.CALL));
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
                        onServiceCreate(toRun,mInfo,taskHandler.getHandledServiceId(),this);
                        toRun.callService(warpBeam,localEngine.getContext(),mParams);
                        if(mInfo.completion() == WarpServiceInfo.ServiceCompletion.IMPLICIT)
                        {
                            taskHandler.completeService(warpBeam, toRun);
                        }
                    }
                }
                catch(Exception e)
                {
                    taskHandler.abortService(warpBeam,toRun,e.getMessage());
                }
            }
        }
    }

    //REMOTE SERVER SERVICE DEDICATED TASK
    private class ServerRemoteWarpServiceRunnable implements Runnable {
        private IWarpService toRun;
        private IBeam mWarpBeam;
        private Object [] mParams;
        private WarpServiceInfo mInfo;
        private AndroidHandler taskHandler;
        private IWarpEngine mLocalEngine;

        public ServerRemoteWarpServiceRunnable(IBeam warpBeam, IWarpService service, WarpServiceInfo info,
                                        IWarpServiceListener listener, IWarpEngine local, Object [] params)
        {
            taskHandler=new AndroidConcurrentHandler(listener,AndroidWarpServiceContainer.this,
                    generateServiceId(),mHandler);
            mWarpBeam=warpBeam;
            toRun=service;
            mInfo=info;
            mLocalEngine=local;
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
                onServiceCreate(toRun,mInfo,taskHandler.getHandledServiceId(),this);
                toRun.provideService(mWarpBeam, mLocalEngine.getContext(), mParams);
                //In this case nobody asks for service completion, so the service just gets completed
                taskHandler.completeService(mWarpBeam,toRun);
            }
            catch (Exception e)
            {
                taskHandler.abortService(mWarpBeam,toRun,e.getMessage());
            }
        }
    }
}
