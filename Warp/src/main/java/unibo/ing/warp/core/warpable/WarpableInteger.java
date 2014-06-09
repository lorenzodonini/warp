package unibo.ing.warp.core.warpable;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * User: lorenzodonini
 * Date: 12/11/13
 * Time: 15:41
 */
public class WarpableInteger extends DefaultWarpableObject{
    private Integer mInteger;
    private final static String INT_KEY="mInteger";

    public WarpableInteger()
    {
        super();
    }

    public WarpableInteger(JSONObject jsonObject)
    {
        super(jsonObject);
    }

    public WarpableInteger(int integer)
    {
        mInteger=integer;
    }

    @Override
    protected JSONObject toJSONObject() throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put(INT_KEY,mInteger);
        return obj;
    }

    @Override
    protected Object fromJSONObject() throws JSONException
    {
        if(getJSONObject()==null)
        {
            return null;
        }
        return getJSONObject().getInt(INT_KEY);
    }

    @Override
    public void setValue(Object value)
    {
        mInteger=(Integer)value;
    }

    @Override
    public Object getValue() throws JSONException
    {
        if(mInteger!=null)
        {
            return mInteger;
        }
        return super.getValue();
    }
}
