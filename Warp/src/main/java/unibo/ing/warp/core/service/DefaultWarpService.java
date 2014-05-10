package unibo.ing.warp.core.service;

import unibo.ing.warp.core.IHandler;

/**
 * User: lorenzodonini
 * Date: 17/11/13
 * Time: 10:59
 */
public abstract class DefaultWarpService implements IWarpService {
    private Object mContext;
    private int percentProgress;
    private IHandler mHandler;

    @Override
    public ServiceStatus getServiceStatus()
    {
        return (mHandler != null) ? mHandler.getHandledServiceStatus() : ServiceStatus.UNKNOWN;
    }

    @Override
    public void setWarpServiceHandler(IHandler handler)
    {
        mHandler=handler;
    }

    public IHandler getWarpServiceHandler()
    {
        return mHandler;
    }

    protected Object getContext()
    {
        return mContext;
    }

    protected void setContext(Object context)
    {
        mContext=context;
    }

    protected void setPercentProgress(int progress)
    {
        if(progress <= 100 && progress >= 0)
        {
            percentProgress=progress;
        }
    }

    protected int getPercentProgress()
    {
        return percentProgress;
    }

    protected void checkOptionalParameters(Object [] params, int size) throws Exception
    {
        if((params == null && size > 0) || (params != null && size != params.length))
        {
            throw new Exception("Invalid parameters!");
        }
    }

    public String toString()
    {
        return getClass().getSimpleName();
    }
}
