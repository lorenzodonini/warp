package unibo.ing.warp.core.warpable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lorenzo Donini on 6/4/2014.
 */
public class WarpableUDPRequest extends DefaultWarpableObject {
    private static final String PARAM_NUM = "mLength";
    private static final String PARAM_ARRAY = "mParameters";
    public static final String PARAM_KEY = "mParam";
    private WarpableParameter [] mParameters;
    private int mInnerIndex=0;

    public WarpableUDPRequest()
    {
        super();
    }

    public WarpableUDPRequest(int parameterNum)
    {
        mParameters = (parameterNum >= 0) ? new WarpableParameter[parameterNum] : new WarpableParameter[0];
    }

    @Override
    protected JSONObject toJSONObject() throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put(PARAM_NUM,mParameters.length);

        JSONArray array = new JSONArray();
        for(WarpableParameter parameter: mParameters)
        {
            array.put(parameter.getJSONObject());
        }
        json.put(PARAM_ARRAY,array);
        return json;
    }

    @Override
    protected Object fromJSONObject() throws JSONException
    {
        JSONObject json = getJSONObject();
        if(json == null)
        {
            return null;
        }
        int paramNum = json.getInt(PARAM_NUM);
        mParameters = new WarpableParameter[paramNum];
        JSONArray array = json.getJSONArray(PARAM_ARRAY);
        for(int i=0; i<array.length(); i++)
        {
            WarpableParameter parameter = new WarpableParameter(array.getJSONObject(i));
            mParameters[i] = parameter;
        }
        //BE CAREFUL! RETURNS WARPABLE_PARAMETERS!!
        return mParameters;
    }

    @Override
    public void setValue(String key, Object value)
    {
        if(key != null && value != null && mInnerIndex < mParameters.length && key.equals(PARAM_KEY))
        {
            mParameters[mInnerIndex++]=(WarpableParameter)value;
        }
    }

    @Override
    public Object getValue(String key) throws JSONException
    {
        if(key != null && key.equals(PARAM_KEY) && mParameters != null)
        {
            return mParameters;
        }
        return super.getValue(key);
    }
}
