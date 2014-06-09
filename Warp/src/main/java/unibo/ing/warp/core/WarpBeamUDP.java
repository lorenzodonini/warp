package unibo.ing.warp.core;

import org.json.JSONException;
import unibo.ing.warp.core.warpable.IWarpable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Created by Lorenzo Donini on 6/3/2014.
 */
public class WarpBeamUDP implements IBeam {
    private DatagramSocket mSocket;
    private EnumSet<WarpFlag> mFlags;
    private WarpLocation mRemoteLocation;

    public WarpBeamUDP(DatagramSocket socket, Collection<WarpFlag> flags)
    {
        //Server constructor
        mSocket=socket;
        mRemoteLocation = new WarpLocation();
        mFlags=(flags != null) ? EnumSet.copyOf(flags) : EnumSet.allOf(WarpFlag.class);
    }

    public WarpBeamUDP(DatagramSocket socket, WarpLocation remoteLocation,
                       Collection<WarpFlag> flags)
    {
        //Client constructor
        mSocket=socket;
        mRemoteLocation=remoteLocation;
        mFlags=(flags != null) ? EnumSet.copyOf(flags) : EnumSet.allOf(WarpFlag.class);
    }

    private int getDefaultMessageSize()
    {
        for(WarpFlag flag: mFlags)
        {
            if(flag == WarpFlag.MESSAGE_SIZE)
            {
                return (Integer)flag.getValue();
            }
        }
        return 0;
    }

    @Override
    public int beamWarpable(IWarpable object) throws IOException, JSONException
    {
        byte [] buf = object.warpTo();
        if(buf == null)
        {
            return -1;
        }
        DatagramPacket outgoingPacket = new DatagramPacket(buf,buf.length);
        outgoingPacket.setAddress(mRemoteLocation.getIPv4Address());
        outgoingPacket.setPort(mRemoteLocation.getPort());
        if(mSocket != null && !mSocket.isClosed())
        {
            mSocket.send(outgoingPacket);
            return buf.length;
        }
        return -1;
    }

    @Override
    public int receiveWarpable(IWarpable result) throws IOException, JSONException
    {

        int messageSize = getDefaultMessageSize();
        byte [] buf = new byte[messageSize];
        DatagramPacket incomingPacket = new DatagramPacket(buf,messageSize);
        if(mSocket != null && !mSocket.isClosed())
        {
            //TODO: set timeout properly!
            mSocket.setSoTimeout(1000);
            mSocket.receive(incomingPacket);
            mRemoteLocation.setIPv4Address(incomingPacket.getAddress());
            return result.warpFrom(incomingPacket.getData());
        }
        return -1;
    }

    @Override
    public WarpLocation getPeerWarpLocation()
    {
        return mRemoteLocation;
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
        mSocket.close();
        mSocket=null;
    }

    @Override
    public boolean isEnabled() {
        return mSocket != null && mSocket.isClosed();
    }
}
