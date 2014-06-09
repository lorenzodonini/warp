package unibo.ing.warp.core;

import unibo.ing.warp.core.device.DefaultWarpDevice;
import unibo.ing.warp.core.device.IWarpDeviceRequestHandler;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.utils.WarpUtils;
import unibo.ing.warp.view.IViewObserver;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by Lorenzo Donini on 5/16/2014.
 */
public abstract class DefaultWarpInteractiveDevice implements IWarpInteractiveDevice {
    private Collection<WarpServiceInfo> mServices;
    private IWarpDeviceRequestHandler mRequestManager;
    private IViewObserver mViewObserver;
    private WarpDeviceStatus mDeviceStatus;
    private int mOperationProgress;
    private String mCurrentLabel;

    public DefaultWarpInteractiveDevice(IWarpDeviceRequestHandler requestManager)
    {
        mRequestManager=requestManager;
        mDeviceStatus=WarpDeviceStatus.DISCONNECTED;
        mServices = new LinkedList<WarpServiceInfo>();
    }

    public DefaultWarpInteractiveDevice(IWarpDeviceRequestHandler requestManager, WarpDeviceStatus status)
    {
        mRequestManager=requestManager;
        mDeviceStatus=status;
        mServices = new LinkedList<WarpServiceInfo>();
    }

    public void setViewObserver(IViewObserver observer)
    {
        mViewObserver = observer;
    }

    @Override
    public synchronized Collection<WarpServiceInfo> getAvailableServices(IWarpServiceListener listener)
    {
        if(mDeviceStatus == WarpDeviceStatus.CONNECTED)
        {
            if(mServices.size()==0)
            {
                //Return only connect service
                WarpServiceInfo info = WarpUtils.getWarpServiceInfo(((DefaultWarpDevice)
                        getWarpDevice()).getDisconnectServiceClass());
                if(info != null)
                {
                    mServices.add(info);
                }
                if(mRequestManager != null)
                {
                    mRequestManager.onServicesLookupRequest(this,listener);
                }
            }
        }
        else if(mDeviceStatus == WarpDeviceStatus.DISCONNECTED || mDeviceStatus == WarpDeviceStatus.FAILED)
        {
            if(mServices.size()==0)
            {
                //Return only connect service
                WarpServiceInfo info = WarpUtils.getWarpServiceInfo(((DefaultWarpDevice)
                        getWarpDevice()).getConnectServiceClass());
                if(info != null)
                {
                    mServices.add(info);
                }
            }
        }
        return mServices;
    }

    @Override
    public synchronized void addAvailableServices(Collection<WarpServiceInfo> services)
    {
        mServices.addAll(services);
    }

    @Override
    public void connect()
    {
        if(mRequestManager != null)
        {
            mRequestManager.onConnectRequest(this);
        }
    }

    @Override
    public void disconnect()
    {
        if(mRequestManager != null)
        {
            mRequestManager.onDisconnectRequest(this);
        }
    }

    @Override
    public void callPushService(WarpServiceInfo serviceDescriptor)
    {
        if(mRequestManager != null)
        {
            mRequestManager.onPushServiceRequest(this,serviceDescriptor);
        }
    }

    @Override
    public void callPullService(WarpServiceInfo serviceDescriptor)
    {
        if(mRequestManager != null)
        {
            mRequestManager.onPullServiceRequest(this,serviceDescriptor);
        }

    }

    @Override
    public void updateDeviceData(IWarpInteractiveDevice newDevice)
    {
        getWarpDevice().updateAbstractDevice(newDevice.getWarpDevice().getAbstractDevice());
        /* NOT SURE IF THIS IS A GOOD IDEA
        setDeviceStatus(newDevice.getDeviceStatus());
        setOperationProgress(newDevice.getDeviceOperationProgress());
        setOperationLabel(newDevice.getDeviceOperationLabel());*/
    }

    @Override
    public synchronized WarpDeviceStatus getDeviceStatus()
    {
        return mDeviceStatus;
    }

    //DEFAULT DEVICE METHODS ONLY

    public synchronized void setDeviceStatus(WarpDeviceStatus status)
    {
        boolean changed = mDeviceStatus != status;
        mDeviceStatus=status;
        if(changed)
        {
            mServices.clear();
            if(mViewObserver != null)
            {
                mViewObserver.onWarpDeviceStatusChanged(this);
            }
        }
    }

    public synchronized void setOperationProgress(int operationProgress)
    {
        boolean changed = mOperationProgress != operationProgress;
        mOperationProgress = operationProgress;
        if(changed && mViewObserver != null)
        {
            mViewObserver.onWarpDeviceOperationProgressChanged(this);
        }
    }

    public synchronized void setOperationLabel(String operationLabel)
    {
        boolean changed = (mCurrentLabel != null && !mCurrentLabel.equals(operationLabel));
        mCurrentLabel = operationLabel;
        if(changed && mViewObserver != null)
        {
            mViewObserver.onWarpDeviceOperationLabelChanged(this);
        }
    }

    @Override
    public synchronized int getDeviceOperationProgress()
    {
        return mOperationProgress;
    }

    @Override
    public synchronized String getDeviceOperationLabel()
    {
        return mCurrentLabel;
    }
}
