package unibo.ing.warp.core.warpable;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: lorenzodonini
 * Date: 23/11/13
 * Time: 19:30
 */
public class WarpableLong extends DefaultWarpableObject {
    private Long mLong;
    private final static String LONG_KEY="mLong";

    public WarpableLong()
    {
        super();
    }

    public WarpableLong(JSONObject jsonObject)
    {
        super(jsonObject);
    }

    public WarpableLong(long l)
    {
        mLong=l;
    }

    @Override
    protected JSONObject toJSONObject() throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put(LONG_KEY,mLong);
        return obj;
    }

    @Override
    protected Object fromJSONObject() throws JSONException
    {
        if(getJSONObject()==null)
        {
            return null;
        }
        return ((Double)getJSONObject().getDouble(LONG_KEY)).longValue();
    }

    @Override
    public void setValue(Object value)
    {
        mLong=(Long)value;
    }

    @Override
    public Object getValue() throws JSONException
    {
        if(mLong!=null)
        {
            return mLong;
        }
        return super.getValue();
    }
}
