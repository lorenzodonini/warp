package unibo.ing.warp.core.service.base;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import org.json.JSONException;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.WarpLocation;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.launcher.WarpBeaconLauncher;
import unibo.ing.warp.core.service.listener.WarpBeaconServiceListener;
import unibo.ing.warp.core.warpable.WarpablePingObject;
import unibo.ing.warp.utils.WarpUtils;
import java.io.IOException;
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
    private static final int LISTEN_PORT=13838;
    //private static final int DEFAULT_SOCKET_TIMEOUT=60000;
    private static final byte BEACON_PING = 33;
    private static final int PACKET_SIZE = 4096;
    private static final byte JOIN_PING = 32;
    private static final byte RESPONSE_PING = 34;
    private static final String MULTICAST_LOCK_TAG = "warp.unibo.it";
    private boolean bEnabled;
    private Map<WarpLocation, String []> mResult;
    private Set<String> mNeighbors;
    private Map<WarpLocation, Long> mResultTimeout;
    private MulticastSocket mBroadcastSocket;

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        checkOptionalParameters(params,3); //TODO: ADJUST IN LAUNCHER
        setEnabled(true);
        setContext(context);
        mResult = new HashMap<WarpLocation, String[]>();
        mResultTimeout = new HashMap<WarpLocation, Long>();
        mNeighbors = new HashSet<String>();
        long defaultInterval = (Long)params[0];
        int rawIpAddress = (Integer)params[1];
        IWarpEngine warpDrive = (IWarpEngine)params[2];
        long currentInterval, startTime;

        mBroadcastSocket = new MulticastSocket(LISTEN_PORT);
        InetAddress ipAddress = InetAddress.getByAddress(WarpUtils.getRawIPv4AddressFromInt(rawIpAddress));
        InetAddress reverseIp = InetAddress.getByAddress(WarpUtils.getRawIPv4AddressFromIntReversed(rawIpAddress));
        String broadcast = getBroadcastAddress(ipAddress.getHostAddress(),reverseIp.getHostAddress());
        InetAddress broadcastAddress = InetAddress.getByName(broadcast);
        String [] servicesData = buildData(warpDrive);

        //Configuring multicast lock
        WifiManager wifi = (WifiManager) ((Context)getContext()).getSystemService(Context.WIFI_SERVICE);
        WifiManager.MulticastLock lock = wifi.createMulticastLock(MULTICAST_LOCK_TAG);
        String myMacAddress = wifi.getConnectionInfo().getMacAddress();

        //Putting local data into the Ping Object
        WarpablePingObject pingObject = new WarpablePingObject();
        pingObject.setValue(servicesData);
        pingObject.setValue(JOIN_PING);
        pingObject.setValue(myMacAddress);

        byte [] buffer = new byte[PACKET_SIZE]; //Common 4 KB buffer for Datagrams
        byte [] data = pingObject.warpTo(); //Broadcast Ping data

        //Sending a ping for broadcast, then expecting results or pings from other devices
        DatagramPacket defaultPacket = new DatagramPacket(buffer, buffer.length,broadcastAddress,LISTEN_PORT);
        DatagramPacket servicesPacket = new DatagramPacket(data,data.length,broadcastAddress,LISTEN_PORT);

        //Starting service
        lock.acquire();
        if(isEnabled())
        {
            mBroadcastSocket.send(servicesPacket); //Broadcast JOIN PING
            Log.d("WARP.DEBUG","WarpBaconService: Initial Broadcast Sent");
        }

        while(isEnabled())
        {
            startTime = System.currentTimeMillis();
            currentInterval = defaultInterval;
            while(isEnabled())
            {
                try{
                    currentInterval = currentInterval - (System.currentTimeMillis() - startTime);
                    if(currentInterval < 0)
                    {
                        break;
                    }
                    mBroadcastSocket.setSoTimeout((int)currentInterval);
                    defaultPacket.setData(buffer); //TODO: UNNECESSARY?!
                    mBroadcastSocket.receive(defaultPacket); //Receive either Unicast response or Broadcast ping

                    //Creating corresponding WarpLocation Object
                    InetAddress senderAddress = defaultPacket.getAddress();
                    if(senderAddress.getHostAddress().equals(ipAddress.getHostAddress())
                            || senderAddress.getHostAddress().equals(reverseIp.getHostAddress()))
                    {
                        //We want to process only external packets, not our own!
                        continue;
                    }
                    int read = pingObject.warpFrom(defaultPacket.getData());
                    int code = (Integer) pingObject.getValue();
                    switch(code) {
                        case JOIN_PING:
                            //We don't know the device and want to add it, but also respond to the ping
                            /*addDevice(senderAddress, pingObject.getMacAddress(),
                                    pingObject.getServices(), defaultInterval);
                            respondToPeer(servicesPacket, senderAddress, pingObject, servicesData,
                                    myMacAddress); //Unicast RESPONSE PING*/
                            Log.d("WARP.DEBUG","WarpBeaconService: Received JOIN PING from "+senderAddress.getHostAddress());
                            break;
                        case BEACON_PING:
                            //Just a regular ping
                            Log.d("WARP.DEBUG","WarpBeaconService: Received BEACON PING from "+senderAddress.getHostAddress());
                            String mac = pingObject.getMacAddress();
                            if (mNeighbors.contains(mac)) {
                                //We know the device already, updating timeout
                                WarpLocation location = new WarpLocation(senderAddress);
                                location.setIPv4Address(senderAddress.getHostAddress());
                                mResultTimeout.put(location, defaultInterval * 3);
                            } else {
                                /*We somehow don't know the device (either due to an error or because we
                                didn't receive the JOIN_PING). Therefore we must add the device */
                                /*addDevice(senderAddress, pingObject.getMacAddress(),
                                        pingObject.getServices(), defaultInterval);
                                respondToPeer(servicesPacket, senderAddress, pingObject, servicesData,
                                        myMacAddress); //Unicast RESPONSE PING*/
                            }
                            break;
                        case RESPONSE_PING:
                            //We don't know the device and want to add it
                            /*addDevice(senderAddress, pingObject.getMacAddress(),
                                    pingObject.getServices(), defaultInterval);*/
                            Log.d("WARP.DEBUG", "WarpBaconService: Response received by "
                                    + senderAddress.getHostAddress());
                            break;
                        default:
                            break;
                    }
                }
                catch(InterruptedIOException e)
                {
                    break;
                }
                catch (Exception e)
                {
                    Log.d("WARP.DEBUG","WarpBeaconService: exception occurred: "+e.getMessage());
                }
            }

            if(isEnabled())
            {
                //Updating timeouts of currently stored devices
                updateTimeouts(defaultInterval);

                //Performing a standard beacon ping
                pingObject.setValue(new String[0]); //Set services data
                pingObject.setValue(BEACON_PING);
                pingObject.setValue(myMacAddress);
                servicesPacket.setData(pingObject.warpTo());
                servicesPacket.setAddress(broadcastAddress);
                servicesPacket.setPort(LISTEN_PORT);
                mBroadcastSocket.send(servicesPacket);
                Log.d("WARP.DEBUG","WarpBeaconService: Broadcast Sent");
            }
        }
        lock.release();
        if(!mBroadcastSocket.isClosed())
        {
            mBroadcastSocket.close();
        }
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //Do nothing since it is a local service!!
    }

    private String [] buildData(IWarpEngine warpDrive)
    {
        Collection<String> activeServiceNames = warpDrive.getServicesNames();
        return activeServiceNames.toArray(new String[activeServiceNames.size()]);
    }

    private void updateTimeouts(long defaultInterval)
    {
        long deviceTimeout;
        boolean bChanged = false;
        for(WarpLocation location: mResult.keySet())
        {
            //Decreasing time in which devices have last been seen
            deviceTimeout = mResultTimeout.get(location);
            deviceTimeout -= defaultInterval;
            if(deviceTimeout <= 0)
            {
                //Removing elements that aren't reachable anymore
                mResult.remove(location);
                mResultTimeout.remove(location);
                mNeighbors.remove(location.getPhyisicalAddress());
                Log.d("WARP.DEBUG","WarpBeaconService: "+location.getStringIPv4Address()+" removed");
                bChanged = true;
            }
            else
            {
                mResultTimeout.put(location,deviceTimeout);
            }
        }
        if(bChanged)
        {
            getWarpServiceHandler().onServiceProgressUpdate(this);
        }
    }

    private void addDevice(InetAddress senderAddress, String senderMac, String [] services, long interval)
    {
        WarpLocation peerLocation = new WarpLocation(senderAddress);
        peerLocation.setIPv4Address(senderAddress.getHostAddress());
        peerLocation.setPhyisicalAddress(senderMac);
        if(services != null)
        {
            mResult.put(peerLocation, services);
        }
        else
        {
            mResult.put(peerLocation, new String[0]);
        }
        mResultTimeout.put(peerLocation,interval*3);
        mNeighbors.add(senderMac);
        Log.d("WARP.DEBUG","WarpBeaconService: "+peerLocation.getStringIPv4Address()+" added");
    }

    private void respondToPeer(DatagramPacket packet, InetAddress sender, WarpablePingObject pingObject,
                               String [] servicesData, String macAddress) throws JSONException, IOException
    {
        pingObject.setValue(servicesData);
        pingObject.setValue(RESPONSE_PING);
        pingObject.setValue(macAddress);
        packet.setAddress(sender);
        packet.setPort(LISTEN_PORT);
        packet.setData(pingObject.warpTo());
        mBroadcastSocket.send(packet); //Unicast RESPONSE PING
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
        mBroadcastSocket.close();
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
