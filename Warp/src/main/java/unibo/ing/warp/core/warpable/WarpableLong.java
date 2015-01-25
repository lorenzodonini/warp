package unibo.ing.warp.core.warpable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created with IntelliJ IDEA.
 * User: lorenzodonini
 * Date: 23/11/13
 * Time: 19:30
 */
public class WarpableLong extends DefaultWarpableObject {
    private Long mLong;
    public final static String LONG_KEY="mLong";

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
        mLong = ((Double)getJSONObject().getDouble(LONG_KEY)).longValue();
        return mLong;
    }

    @Override
    public void setValue(String key, Object value)
    {
        if(key != null && value != null && key.equals(LONG_KEY))
        {
            mLong = (Long) value;
        }
    }

    @Override
    public Object getValue(String key) throws JSONException
    {
        if(key != null && mLong != null && key.equals(LONG_KEY))
        {
            return mLong;
        }
        return super.getValue(key);
    }

    @Override
    public Object getValue() throws JSONException
    {
        return mLong;
    }
}
