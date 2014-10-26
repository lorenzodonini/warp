package unibo.ing.warp.core.android;

import android.util.Log;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.IWarpServiceObserver;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.IBeamHandler;
import unibo.ing.warp.core.IHandler;
import unibo.ing.warp.utils.WarpUtils;

import java.io.IOException;

/**
 * Created by Lorenzo Donini on 4/17/2014.
 */
public abstract class AndroidHandler implements IHandler, IBeamHandler {
    private IWarpServiceListener mListener;
    private IWarpServiceObserver mContainer;
    private boolean bCreated;
    private IWarpService.ServiceStatus mStatus;
    private long mId;

    public AndroidHandler(IWarpServiceListener listener, IWarpServiceObserver observer, long id)
    {
        mListener=listener;
        mContainer=observer;
        bCreated=false;
        mStatus= IWarpService.ServiceStatus.INACTIVE;
        mId=id;
    }

    @Override
    public void onBeamCreate(IBeam warpBeam, IWarpService service)
    {
        if(!bCreated)
        {
            bCreated=true;
            if(warpBeam!=null && warpBeam.isEnabled())
            {
                mContainer.onBeamCreate(warpBeam,service);
            }
        }
    }

    @Override
    public void completeService(IBeam warpBeam, IWarpService servant)
    {
        if(mStatus != IWarpService.ServiceStatus.COMPLETED)
        {
            setHandledServiceStatus(IWarpService.ServiceStatus.COMPLETED);
            if(warpBeam != null && warpBeam.isEnabled())
            {
                try {
                    warpBeam.disable();
                    mContainer.onBeamDestroy(warpBeam,servant);
                    mContainer.onServiceDestroy(servant);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            onServiceCompletedOperation(servant);
        }
    }

    @Override
    public void abortService(IBeam warpBeam, IWarpService servant, String message)
    {
        if(mStatus != IWarpService.ServiceStatus.ABORTED)
        {
            setHandledServiceStatus(IWarpService.ServiceStatus.ABORTED);
            if(warpBeam != null && warpBeam.isEnabled())
            {
                try {
                    warpBeam.disable();
                    mContainer.onBeamDestroy(warpBeam,servant);
                    mContainer.onServiceDestroy(servant);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            onServiceAbortOperation(message);
        }
    }

    @Override
    public synchronized IWarpService.ServiceStatus getHandledServiceStatus()
    {
        return (mStatus!=null)? mStatus : IWarpService.ServiceStatus.UNKNOWN;
    }

    public synchronized void setHandledServiceStatus(IWarpService.ServiceStatus status)
    {
        mStatus=status;
    }

    protected IWarpServiceListener getServiceListener()
    {
        return mListener;
    }

    @Override
    public void onServiceCompleted(IWarpService servant)
    {
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(servant.getClass());
        if(info.completion() == WarpServiceInfo.ServiceCompletion.EXPLICIT)
        {
            completeService(null,servant);
        }
    }

    @Override
    public void onServiceAbort(IWarpService servant, String message)
    {
        WarpServiceInfo info = WarpUtils.getWarpServiceInfo(servant.getClass());
        if(info.completion() == WarpServiceInfo.ServiceCompletion.EXPLICIT)
        {
            abortService(null,servant,message);
        }
    }

    @Override
    public long getHandledServiceId()
    {
        return mId;
    }

    protected abstract void onServiceCompletedOperation(final IWarpService servant);
    protected abstract void onServiceAbortOperation(final String message);
}
