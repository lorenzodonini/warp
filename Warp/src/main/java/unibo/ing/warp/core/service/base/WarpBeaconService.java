package unibo.ing.warp.core.service.base;

import android.util.Log;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.launcher.WarpBeaconLauncher;
import unibo.ing.warp.core.service.listener.WarpBeaconServiceListener;
import unibo.ing.warp.utils.WarpUtils;
import java.io.InterruptedIOException;
import java.net.*;
import java.util.*;

/**
 * Created by Lorenzo Donini on 5/17/2014.
 */
@WarpServiceInfo(type = WarpServiceInfo.Type.LOCAL, label = "Beacon", name = "beaconService",
        execution = WarpServiceInfo.ServiceExecution.CONCURRENT, launcher = WarpBeaconLauncher.class,
        protocol = WarpServiceInfo.Protocol.NONE, callListener = WarpBeaconServiceListener.class)
public class WarpBeaconService extends DefaultWarpService {
    private boolean bEnabled;
    private Map<InetAddress, String []> mResult;
    private Map<InetAddress, Long> mResultTimeout;

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        checkOptionalParameters(params,2);
        setEnabled(true);
        mResult = new HashMap<InetAddress, String[]>();
        mResultTimeout = new HashMap<InetAddress, Long>();
        long defaultInterval = (Long)params[0];
        int rawIpAddress = (Integer)params[1];
        long currentInterval;
        long deviceTimeout;
        long startTime;
        boolean bChanged;

        DatagramSocket socket = new DatagramSocket();
        InetAddress ipAddress = InetAddress.getByAddress(WarpUtils.getRawIPv4AddressFromInt(rawIpAddress));
        InetAddress reverseIp = InetAddress.getByAddress(WarpUtils.getRawIPv4AddressFromIntReversed(rawIpAddress));
        String broadcast = getBroadcastAddress(ipAddress.getHostAddress(),reverseIp.getHostAddress());
        InetAddress broadcastAddress = InetAddress.getByName(broadcast);
        String [] availableServicesNames;
        byte [] ping = new byte[] {WarpLighthouseService.BEACON_PING}; //Broadcast packet
        byte [] data = new byte[WarpLighthouseService.PACKET_SIZE]; //Unicast packet

        //Sending only a ping for broadcast, then expecting results
        DatagramPacket broadcastPacket = new DatagramPacket(ping, ping.length,
                broadcastAddress,WarpLighthouseService.LISTEN_PORT);
        DatagramPacket unicastPacket = new DatagramPacket(data,data.length);

        //Starting service
        while(isEnabled())
        {
            startTime = System.currentTimeMillis();
            currentInterval = defaultInterval;
            socket.send(broadcastPacket);  //Broadcast PING
            Log.d("WARP.DEBUG","WarpBaconService: Broadcast Sent");
            while(isEnabled())
            {
                try{
                    currentInterval = currentInterval - (System.currentTimeMillis() - startTime);
                    socket.setSoTimeout((int)currentInterval);
                    unicastPacket.setData(data); //TODO: UNNECESSARY?
                    socket.receive(unicastPacket); //Receive Unicast response
                    InetAddress senderAddress = unicastPacket.getAddress();
                    Log.d("WARP.DEBUG","WarpBaconService: Response received by "+senderAddress.getHostAddress());
                    String payload = new String(unicastPacket.getData(),"UTF-8");
                    availableServicesNames = payload.split("#")[0].split(";");
                    //Updating result
                    if(!mResult.containsKey(senderAddress))
                    {
                        mResult.put(senderAddress,availableServicesNames);
                        mResultTimeout.put(senderAddress,defaultInterval*3);
                        getWarpServiceHandler().onServiceProgressUpdate(this);
                    }
                    else
                    {
                        mResultTimeout.put(senderAddress,defaultInterval*3);
                    }
                }
                catch (InterruptedIOException e)
                {
                    break;
                }
            }
            //Decreasing time in which devices have last been seen
            bChanged=false;
            for(InetAddress address: mResult.keySet())
            {
                deviceTimeout = mResultTimeout.get(address);
                deviceTimeout-= defaultInterval;
                //Removing elements that aren't reachable anymore
                if(deviceTimeout <= 0)
                {
                    mResult.remove(address);
                    mResultTimeout.remove(address);
                    bChanged=true;
                }
                else
                {
                    mResultTimeout.put(address,deviceTimeout);
                }
            }
            if(bChanged)
            {
                getWarpServiceHandler().onServiceProgressUpdate(this);
            }
        }
        socket.close();
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //Do nothing since it is a local service!!
    }

    private String getBroadcastAddress(String ipAddress, String reverseIpAddress) throws SocketException
    {
        System.setProperty("java.net.preferIPv4Stack", "true");
        for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements();)
        {
            NetworkInterface ni = niEnum.nextElement();
            if (!ni.isLoopback())
            {
                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses())
                {
                    //The found interface must be the one relative to the passed IP address
                    String address = interfaceAddress.getAddress().getHostAddress();
                    if(address.equals(ipAddress) || address.equals(reverseIpAddress))
                    {
                        InetAddress broadcast = interfaceAddress.getBroadcast();
                        if(broadcast != null)
                        {
                            return broadcast.toString().substring(1);
                        }
                    }
                }
            }
        }
        return null;
    }

    private synchronized boolean isEnabled()
    {
        return bEnabled;
    }

    private synchronized void setEnabled(boolean enabled)
    {
        bEnabled=enabled;
    }

    public void stopService()
    {
        setEnabled(false);
    }

    @Override
    public Object[] getResult()
    {
        return null;
    }

    @Override
    public Object[] getCurrentProgress()
    {
        return new Object[] {mResult};
    }
}
