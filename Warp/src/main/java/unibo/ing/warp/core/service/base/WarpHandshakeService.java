package unibo.ing.warp.core.service.base;

import org.json.JSONException;
import unibo.ing.warp.core.*;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.launcher.WarpHandshakeLauncher;
import unibo.ing.warp.core.warpable.*;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.EnumSet;

/**
 * User: lorenzodonini
 * Date: 16/11/13
 * Time: 01:34
 */
@WarpServiceInfo(type = WarpServiceInfo.Type.LOCAL, label="Handshake",
        target = WarpServiceInfo.Target.ALL, name="warpHandshake",
        launcher = WarpHandshakeLauncher.class)
public class WarpHandshakeService extends DefaultWarpService {
    private IBeam mWarpBeam =null;
    private String mErrorMessage = "TEST";
    private WarpServiceInfo mRemoteServiceDescriptor;
    private static final String HANDSHAKE_FAILED = "WarpHandshakeService.callService: Handshaking failed!";

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        /*Note: the params passed here are the actual parameters needed by the
        service that is going to be called by the Handshake service, plus some
         parameters needed in order to establish a connection to the remote node*/
        checkOptionalParameters(params,3);

        WarpLocation remoteWarpDeviceLocation = (WarpLocation)params[0];
        mRemoteServiceDescriptor = (WarpServiceInfo)params[1];
        IWarpable serviceParams [] = (IWarpable [])params[2];

        if(mRemoteServiceDescriptor == null)
        {
            throw new Exception(HANDSHAKE_FAILED);
        }

        if(mRemoteServiceDescriptor.protocol() == WarpServiceInfo.Protocol.TCP)
        {
            buildWarpBeamTCP(remoteWarpDeviceLocation, serviceParams);
        }
        else if(mRemoteServiceDescriptor.protocol() == WarpServiceInfo.Protocol.UDP)
        {
            buildWarpBeamUDP(remoteWarpDeviceLocation, serviceParams);
        }
    }

    private void buildWarpBeamTCP(WarpLocation remoteWarpDeviceLocation, IWarpable [] serviceParams)
            throws Exception
    {
        Socket socket;
        InetAddress address = remoteWarpDeviceLocation.getIPv4Address();
        if(address == null)
        {
            String stringAddress = remoteWarpDeviceLocation.getStringIPv4Address();
            byte [] rawAddress = remoteWarpDeviceLocation.getRawIPv4Address();
            if(stringAddress != null)
            {
                address = InetAddress.getByName(stringAddress);
            }
            else if(rawAddress != null)
            {
                address = InetAddress.getByAddress(rawAddress);
            }
            else
            {
                //TODO: needs to be a specific exception
                throw new Exception("WarpHandshakeService.buildWarpBeamTCP: no IP address is set!");
            }
        }

        socket = new Socket(address, WarpTCPDispatcherService.LISTEN_PORT);

        EnumSet<WarpFlag> flags = EnumSet.noneOf(WarpFlag.class);
        WarpFlag master = WarpFlag.MASTER;
        master.setValue(true);
        flags.add(master);

        mWarpBeam = new WarpBeamTCP(socket,flags);

        //Handshaking
        if(performTCPServiceRequest())
        {
            //Passing service parameters!
            if(!provideTCPExtraParameters(serviceParams))
            {
                throw new Exception(HANDSHAKE_FAILED);
            }
        }
    }

    private void buildWarpBeamUDP(WarpLocation remoteWarpDeviceLocation, IWarpable [] serviceParams)
            throws Exception
    {
        InetAddress address = InetAddress.getLocalHost();
        if(address == null)
        {
            //TODO: needs to be a specific exception
            throw new Exception("WarpHandshakeService.buildWarpBeamUDP: no localhost address found!");
        }

        DatagramSocket socket = new DatagramSocket(WarpTCPDispatcherService.LISTEN_PORT,
                remoteWarpDeviceLocation.getIPv4Address());

        EnumSet<WarpFlag> flags = EnumSet.noneOf(WarpFlag.class);
        WarpFlag master = WarpFlag.MASTER;
        master.setValue(true);
        flags.add(master);
        flags.add(WarpFlag.MESSAGE_SIZE);
        flags.add(WarpFlag.BUFFER_SIZE);
        flags.add(WarpFlag.TIMEOUT);

        mWarpBeam = new WarpBeamUDP(socket,remoteWarpDeviceLocation,flags);
        //Handshaking
        if(performUDPServiceRequest())
        {
            if(!provideUDPExtraParameters(serviceParams))
            {
                throw new Exception(HANDSHAKE_FAILED);
            }
        }
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //NOTHING TO BE IMPLEMENTED SINCE THIS IS A LOCAL SERVICE
    }

    @Override
    public Object[] getResult()
    {
        return (mWarpBeam != null)? new Object[] {mWarpBeam} : new Object[] {mErrorMessage};
    }

    @Override
    public Object[] getCurrentProgress()
    {
        return null;
    }

    /* ############## TCP LOGIC ############## */
    private boolean performTCPServiceRequest() throws IOException, JSONException
    {
        IWarpable serviceName, response;

        serviceName = new WarpableString(mRemoteServiceDescriptor.name());
        mWarpBeam.beamWarpable(serviceName);
        response=new WarpableString();
        mWarpBeam.receiveWarpable(response);
        return (response.getValue().equals(WarpTCPDispatcherService.SERVICE_REQUEST_REFUSED));
    }

    private boolean provideTCPExtraParameters(IWarpable params [])
            throws IOException, JSONException {
        IWarpable paramClass = new WarpableString();
        IWarpable paramNum = new WarpableInteger();
        IWarpable extra = new WarpableString();
        IWarpable object, response;

        if(params==null || params.length==0)
        {
            //NO PARAMETERS
            extra.setValue(WarpTCPDispatcherService.NO_EXTRA_PARAMETERS);
            mWarpBeam.beamWarpable(extra);
        }
        else
        {
            //EXTRA PARAMETERS TO BE SENT
            extra.setValue(WarpTCPDispatcherService.EXTRA_PARAMETERS);
            mWarpBeam.beamWarpable(extra);

            paramNum.setValue(params.length);
            mWarpBeam.beamWarpable(paramNum);

            for (IWarpable param : params)
            {
                object = param;
                paramClass.setValue(object.getValue().getClass().getName()); //TODO: HANDLE CLASS NAME
                mWarpBeam.beamWarpable(paramClass);
                mWarpBeam.beamWarpable(object);
            }
        }

        response = new WarpableString();
        mWarpBeam.receiveWarpable(response);
        return(response.getValue()!=null &&
                (response.getValue()).equals(WarpTCPDispatcherService.SERVICE_REQUEST_ACCEPTED));
    }

    /* ############## UDP LOGIC ############## */
    private boolean performUDPServiceRequest() throws IOException, JSONException
    {
        IWarpable serviceName = new WarpableString(mRemoteServiceDescriptor.name());
        WarpableUDPResponse response;

        mWarpBeam.beamWarpable(serviceName);
        response = new WarpableUDPResponse();
        mWarpBeam.receiveWarpable(response);
        String message = response.getMessage();
        if(message.equals(WarpTCPDispatcherService.SERVICE_REQUEST_REFUSED))
        {
            return false;
        }
        mWarpBeam.getPeerWarpLocation().setPort((Integer)response.getValue());
        return true;
    }

    private boolean provideUDPExtraParameters(IWarpable params []) throws IOException, JSONException
    {
        WarpableUDPRequest extra = new WarpableUDPRequest(params.length);
        IWarpable response = new WarpableString();

        if(params != null && params.length==0)
        {
            //PARAMETERS
            for(IWarpable param : params)
            {
                extra.setValue(new WarpableParameter(param));
            }
        }
        mWarpBeam.beamWarpable(extra);
        mWarpBeam.receiveWarpable(response);
        return(response.getValue()!=null &&
                (response.getValue()).equals(WarpTCPDispatcherService.SERVICE_REQUEST_ACCEPTED));
    }

}
