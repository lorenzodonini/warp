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
    public final static String INT_KEY="mInteger";

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
        mInteger = getJSONObject().getInt(INT_KEY);
        return mInteger;
    }

    @Override
    public void setValue(String key, Object value)
    {
        if(value != null && key != null && key.equals(INT_KEY))
        {
            mInteger = (Integer) value;
        }
    }

    @Override
    public Object getValue(String key) throws JSONException
    {
        if(key != null && key.equals(INT_KEY) && mInteger!=null)
        {
            return mInteger;
        }
        return super.getValue(key);
    }
}
