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
    private static final String BOOL_KEY="mBoolean";

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
        return (getJSONObject() != null) ? getJSONObject().getBoolean(BOOL_KEY) : null;
    }

    @Override
    public void setValue(Object value)
    {
        mBoolean=(Boolean)value;
    }

    @Override
    public Object getValue() throws JSONException
    {
        return (mBoolean != null) ? mBoolean : super.getValue();
    }
}
