package unibo.ing.warp.core.warpable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: lorenzodonini
 * Date: 19/11/13
 * Time: 16:52
 */
public class WarpableBoolean extends DefaultWarpableObject {
    private Boolean mBoolean;
    public static final String BOOL_KEY="mBoolean";

    public WarpableBoolean()
    {
        super();
    }

    public WarpableBoolean(JSONObject jsonObject)
    {
        super(jsonObject);
    }

    public WarpableBoolean(boolean b)
    {
        mBoolean=b;
    }

    @Override
    protected JSONObject toJSONObject() throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put(BOOL_KEY,mBoolean);
        return obj;
    }

    @Override
    protected Object fromJSONObject() throws JSONException
    {
        if(getJSONObject() == null)
        {
            return null;
        }
        mBoolean = getJSONObject().getBoolean(BOOL_KEY);
        return mBoolean;
    }

    @Override
    public void setValue(String key, Object value)
    {
        if(value != null && key != null && key.equals(BOOL_KEY))
        {
            mBoolean = (Boolean) value;
        }
    }

    @Override
    public Object getValue(String key) throws JSONException
    {
        if(key != null && key.equals(BOOL_KEY) && mBoolean != null)
        {
            return mBoolean;
        }
        return super.getValue(key);
    }

    @Override
    public Object getValue() throws JSONException
    {
        return mBoolean;
    }
}
