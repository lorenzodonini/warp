package unibo.ing.warp.core.service.base;

import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.WarpFlag;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.warpable.IWarpable;
import unibo.ing.warp.core.warpable.WarpableBoolean;
import unibo.ing.warp.core.warpable.WarpableFile;
import unibo.ing.warp.core.warpable.WarpableString;

import java.io.File;

/**
 * Created by Lorenzo Donini on 5/7/2014.
 */
@WarpServiceInfo(type = WarpServiceInfo.Type.PUSH, target = WarpServiceInfo.Target.ALL, name = "pushFile")
public class PushFileService extends DefaultWarpService {
    private File mResultFile; //Available to receiver only (provideService)
    private long mTransferredBytes = 0;

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        checkOptionalParameters(params,1);
        File file = (File) params[0];

        WarpFlag bufferSizeFlag = warpBeam.getFlag(WarpFlag.BUFFER_SIZE.name());
        WarpableString fileName = new WarpableString(file.getName());
        WarpableFile fileToSend = (bufferSizeFlag != null) ? new WarpableFile((Integer) bufferSizeFlag.getValue(),
                new FileWarpProgressUpdater()) : new WarpableFile(new FileWarpProgressUpdater());

        //Network operation
        warpBeam.beamWarpable(fileName);
        mTransferredBytes = warpBeam.beamWarpable(fileToSend);
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        WarpFlag bufferSizeFlag = warpBeam.getFlag(WarpFlag.BUFFER_SIZE.name());
        WarpableString fileName = new WarpableString();
        warpBeam.receiveWarpable(fileName);
        if(fileName.getValue() == null)
        {
            throw new Exception("PushFileService.provideService: didn't receive valid file name!");
        }
        mResultFile = new File((String)fileName.getValue());
        WarpableFile fileToReceive = (bufferSizeFlag != null) ? new WarpableFile((Integer)bufferSizeFlag.getValue(),
                new FileWarpProgressUpdater()) : new WarpableFile(new FileWarpProgressUpdater());
        mTransferredBytes = warpBeam.receiveWarpable(fileToReceive);
    }

    @Override
    public Object[] getResult()
    {
        return (mResultFile != null) ? new Object[] {mResultFile} : new Object[] {mTransferredBytes};
    }

    @Override
    public Object[] getCurrentProgress()
    {
        return new Object[] {mTransferredBytes};
    }

    public class FileWarpProgressUpdater {
        public FileWarpProgressUpdater() {
        }

        public void notifyProgress(int currentPercentage, long transferredBytes)
        {
            mTransferredBytes=transferredBytes;
            setPercentProgress(currentPercentage);
            getWarpServiceHandler().onServiceProgressUpdate(PushFileService.this);
        }
    }
}
