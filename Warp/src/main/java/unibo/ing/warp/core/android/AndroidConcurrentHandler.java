package unibo.ing.warp.core.android;

import android.os.Handler;
import unibo.ing.warp.core.IWarpBeamObserver;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;

/**
 * Created by Lorenzo Donini on 4/17/2014.
 */
public class AndroidConcurrentHandler extends AndroidHandler {
    private Handler mConcurrentHandler;

    public AndroidConcurrentHandler(IWarpServiceListener listener, IWarpBeamObserver observer, Handler handler)
    {
        super(listener,observer);
        mConcurrentHandler=handler;
    }

    @Override
    protected void onServiceCompleted(final IWarpService servant)
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
    protected void onServiceAbort(final String message)
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
