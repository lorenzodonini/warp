package unibo.ing.warp.core.android;

import android.os.Handler;
import unibo.ing.warp.core.IWarpServiceObserver;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;

/**
 * Created by Lorenzo Donini on 4/17/2014.
 */
public class AndroidConcurrentHandler extends AndroidHandler {
    private Handler mConcurrentHandler;

    public AndroidConcurrentHandler(IWarpServiceListener listener, IWarpServiceObserver observer,
                                    long id, Handler handler)
    {
        super(listener,observer,id);
        mConcurrentHandler=handler;
    }

    @Override
    protected void onServiceCompletedOperation(final IWarpService servant)
    {
        final IWarpServiceListener listener = getServiceListener();
        if(listener!=null)
        {
            mConcurrentHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onServiceCompleted(servant);
                }
            });
        }
    }

    @Override
    protected void onServiceAbortOperation(final String message)
    {
        final IWarpServiceListener listener = getServiceListener();
        if(listener!=null)
        {
            mConcurrentHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onServiceAbort(message);
                }
            });
        }
    }

    @Override
    public void onServiceProgressUpdate(final IWarpService servant)
    {
        final IWarpServiceListener listener = getServiceListener();
        if(listener!=null)
        {
            mConcurrentHandler.post(new Runnable() {
                @Override
                public void run() {
                    listener.onServiceProgressUpdate(servant);
                }
            });
        }
    }

    @Override
    public void onServiceStatusChanged(IWarpService servant)
    {
        //TODO: THINK OF SOMETHING
    }
}
