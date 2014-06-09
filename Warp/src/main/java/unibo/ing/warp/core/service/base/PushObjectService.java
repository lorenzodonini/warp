package unibo.ing.warp.core.service.base;

import org.json.JSONException;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.launcher.PushObjectLauncher;
import unibo.ing.warp.core.warpable.IWarpable;
import unibo.ing.warp.core.warpable.WarpableInteger;
import unibo.ing.warp.core.warpable.WarpableString;
import java.io.IOException;

/**
 * User: lorenzodonini
 * Date: 12/11/13
 * Time: 16:06
 */
@WarpServiceInfo(type= WarpServiceInfo.Type.PUSH,name="warpObjectService",label = "Send Object",
        launcher = PushObjectLauncher.class)
public class PushObjectService extends DefaultWarpService {
    private int mTotalTransferredBytes;
    private int mCallTransferredBytes;
    private boolean bDataReceived=false;
    private IWarpable mReceivedObjects [];

    /*
    ------------------------ CLIENT LOGIC ------------------------
     */
    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        IWarpable objectsToSend [];
        IWarpable objectClass, objectNum;

        checkOptionalParameters(params,1);
        if(warpBeam == null)
        {
            throw new Exception("PushObjectService.callService: warpBeam is null!");
        }
        setContext(context);
        mCallTransferredBytes=0;
        objectsToSend=(IWarpable [])params[0];
        objectNum=new WarpableInteger(objectsToSend.length);

        try {
            mCallTransferredBytes=warpBeam.beamWarpable(objectNum);
            setPercentProgress(0);
            getWarpServiceHandler().onServiceProgressUpdate(this);

            for(int i=0; i<objectsToSend.length; i++)
            {
                objectClass=new WarpableString(objectsToSend[i].getClass().getName());
                mCallTransferredBytes+=warpBeam.beamWarpable(objectClass);
                mCallTransferredBytes+=warpBeam.beamWarpable(objectsToSend[i]);
                setPercentProgress(objectsToSend.length*100/(i+1));
                getWarpServiceHandler().onServiceProgressUpdate(this);
            }
        }
        catch(IOException e)
        {
            setPercentProgress(-1);
            return;
        }
        catch (JSONException e)
        {
            setPercentProgress(-1);
            return;
        }
        mTotalTransferredBytes+=mCallTransferredBytes;
    }

    /*
    ------------------------ SERVER LOGIC ------------------------
     */
    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //Takes no parameters in input
        IWarpable objectClass = new WarpableString();
        IWarpable objectNum = new WarpableInteger();

        if(warpBeam == null)
        {
            return;
        }
        setContext(context);
        mCallTransferredBytes=0;
        setPercentProgress(0);
        getWarpServiceHandler().onServiceProgressUpdate(this);

        mCallTransferredBytes=warpBeam.receiveWarpable(objectNum);
        if(objectNum.getValue()!=null)
        {
            mReceivedObjects= new IWarpable[(Integer)objectNum.getValue()];
            for(int i=0; i<mReceivedObjects.length; i++)
            {
                mCallTransferredBytes+=warpBeam.receiveWarpable(objectClass);
                mReceivedObjects[i]=(IWarpable)
                        Class.forName((String)objectClass.getValue()).newInstance();
                warpBeam.receiveWarpable(mReceivedObjects[i]);

                setPercentProgress(mReceivedObjects.length/100*(i+1));
                getWarpServiceHandler().onServiceProgressUpdate(this);
            }
            mTotalTransferredBytes+=mCallTransferredBytes;
        }
        mTotalTransferredBytes+=mCallTransferredBytes;
        bDataReceived=true;

        /*catch(IOException e)
        {
            //TCP FAIL
            setPercentProgress(-1);
            errorMessage=e.getMessage();
        }
        catch (JSONException e)
        {
            //WTF?
            setPercentProgress(-1);
            errorMessage=e.getMessage();
        }
        catch (ClassNotFoundException e)
        {
            //Failed to find an IWarpable class locally. Aborting!
            setPercentProgress(-1);
            errorMessage=e.getMessage();
        }
        catch (InstantiationException e)
        {
            //Error instantiating an object. Empty Constructor missing!
            setPercentProgress(-1);
            errorMessage=e.getMessage();
        }
        catch (IllegalAccessException e)
        {
            //Shouldn't be called
            setPercentProgress(-1);
            errorMessage=e.getMessage();
        }*/
    }

    @Override
    public Object[] getResult()
    {
        Object result [];

        if(bDataReceived)
        {
            result=new Object[3];
            result[0]=mReceivedObjects;
            result[1]=mCallTransferredBytes;
            result[2]=mTotalTransferredBytes;
        }
        else
        {
            result=new Object[2];
            result[0]=mCallTransferredBytes;
            result[1]=mTotalTransferredBytes;
        }
        return result;
    }

    @Override
    public Object[] getCurrentProgress()
    {
        Object result [] = new Object[1];
        //TODO: make it somewhat better
        result[0]=getCurrentPercentProgress();
        return result;
    }
}
