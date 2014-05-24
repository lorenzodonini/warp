package unibo.ing.warp.core.service.base;

import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;

import java.io.InterruptedIOException;
import java.net.*;
import java.util.*;

/**
 * Created by Lorenzo Donini on 5/17/2014.
 */
@WarpServiceInfo(type = WarpServiceInfo.Type.LOCAL, label = "Beacon", execution =
        WarpServiceInfo.ServiceExecution.CONCURRENT, name = "beacon")
public class WarpBeaconService extends DefaultWarpService {
    private boolean bEnabled;
    private Map<InetAddress, String []> mResult;

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //TODO: TO TEST
        checkOptionalParameters(params,1);
        mResult = new HashMap<InetAddress, String[]>();
        long defaultInterval = (Long)params[0];
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(WarpLighthouseService.DEFAULT_SOCKET_TIMEOUT);
        InetAddress broadcastAddress = InetAddress.getByName(getBroadcastAddress());
        String [] availableServicesNames;
        byte [] data = new byte[WarpLighthouseService.PACKET_SIZE];
        boolean bChanged;
        data[0] = WarpLighthouseService.BEACON_PING;

        DatagramPacket packet = new DatagramPacket(data,data.length,
                broadcastAddress,WarpLighthouseService.LISTEN_PORT);

        //Starting service
        while(isEnabled())
        {
            socket.send(packet);
            bChanged=false;
            while(true)
            {
                try{
                    socket.receive(packet);
                    socket.receive(packet);
                    InetAddress senderAddress = packet.getAddress();
                    availableServicesNames = Arrays.toString(packet.getData()).split(";");
                    if(mResult.containsKey(senderAddress))
                    {
                        if(mResult.get(senderAddress).length != availableServicesNames.length)
                        {
                            mResult.put(senderAddress,availableServicesNames);
                            bChanged=true;
                        }
                    }
                    else
                    {
                        mResult.put(senderAddress,availableServicesNames);
                        bChanged=true;
                    }
                }
                catch (InterruptedIOException e)
                {
                    break;
                }
            }
            if(bChanged)
            {
                getWarpServiceHandler().onServiceProgressUpdate(this);
            }
            Thread.sleep(defaultInterval);
        }
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //Do nothing since it is a local service!!
    }

    private String getBroadcastAddress() throws SocketException
    {
        //TODO: MUTUATED CODE! CHECK IT
        System.setProperty("java.net.preferIPv4Stack", "true");
        for (Enumeration<NetworkInterface> niEnum = NetworkInterface.getNetworkInterfaces(); niEnum.hasMoreElements();)
        {
            NetworkInterface ni = niEnum.nextElement();
            if (!ni.isLoopback())
            {
                for (InterfaceAddress interfaceAddress : ni.getInterfaceAddresses())
                {
                    return interfaceAddress.getBroadcast().toString().substring(1);
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
