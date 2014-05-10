package unibo.ing.warp.core;

import org.json.JSONException;
import unibo.ing.warp.core.warpable.IWarpable;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.EnumSet;

/**
 * User: lorenzodonini
 * Date: 16/11/13
 * Time: 01:10
 */
public class WarpBeam implements IBeam{
    private Socket mSocket;
    private DataInputStream mInputStream;
    private DataOutputStream mOutputStream;
    private IWarpEngine mWarpDrive;
    private EnumSet<WarpFlag> mFlags;

    public WarpBeam(Socket socket, IWarpEngine engine, Collection<WarpFlag> flags) throws IOException
    {
        mSocket=socket;
        mInputStream = new DataInputStream(mSocket.getInputStream());
        mOutputStream = new DataOutputStream(mSocket.getOutputStream());
        mWarpDrive=engine;
        mFlags=(flags != null) ? EnumSet.copyOf(flags) : EnumSet.allOf(WarpFlag.class);
    }

    @Override
    public int beamWarpable(IWarpable object) throws IOException, JSONException {
        int sentBytes=0;

        if(mSocket!=null && !mSocket.isClosed())
        {
            sentBytes=object.warpTo(mOutputStream);
        }
        return sentBytes;
    }

    @Override
    public int receiveWarpable(IWarpable result) throws IOException, JSONException {
        int receivedBytes=0;

        if(mSocket!=null && !mSocket.isClosed())
        {
            receivedBytes=result.warpFrom(mInputStream);
        }
        return receivedBytes;
    }

    @Override
    public IWarpEngine getLocalWarpEngine()
    {
        return mWarpDrive;
    }

    /*############## FLAGS LOGIC ##############*/
    @Override
    public WarpFlag getFlag(String propertyName)
    {
        for(WarpFlag flag: mFlags)
        {
            if(flag.name().equals(propertyName))
            {
                return flag;
            }
        }
        return null;
    }

    @Override
    public WarpFlag[] getFlags()
    {
        return mFlags.toArray(new WarpFlag[mFlags.size()]);
    }

    @Override
    public void addFlag(WarpFlag flag)
    {
        WarpFlag oldFlag = getFlag(flag.name());
        if(oldFlag != null)
        {
            oldFlag.setValue(flag.getValue());
        }
        else
        {
            mFlags.add(flag);
        }
    }

    @Override
    public void removeFlag(String propertyName)
    {
        mFlags.remove(getFlag(propertyName));
    }

    @Override
    public void disable() throws IOException
    {
        mSocket.shutdownOutput();
        mSocket.close();
    }

    @Override
    public boolean isEnabled()
    {
        return mSocket!=null && !mSocket.isClosed();
    }
}
