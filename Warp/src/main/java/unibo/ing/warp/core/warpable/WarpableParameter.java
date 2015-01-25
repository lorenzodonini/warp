package unibo.ing.warp.core.warpable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lorenzo Donini on 6/4/2014.
 */
public class WarpableParameter extends DefaultWarpableObject {
    private IWarpable mParameter;
    private static final String CLASS_KEY = "mClass";
    public static final String PARAM_KEY = "mValue";

    public WarpableParameter()
    {
        super();
    }

    public WarpableParameter(IWarpable parameter)
    {
        mParameter=parameter;
    }

    public WarpableParameter(JSONObject jsonObject)
    {
        super(jsonObject);
    }

    @Override
    protected JSONObject toJSONObject() throws JSONException
    {
        JSONObject json = new JSONObject();
        //Passing IWarpable subclass, not object class
        json.put(CLASS_KEY, mParameter.getClass().getName());
        json.put(PARAM_KEY,mParameter.getJSONObject());
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
        JSONObject value = json.getJSONObject(PARAM_KEY);
        try{
            @SuppressWarnings("unchecked")
            Class<? extends IWarpable> warpableClass = (Class<? extends IWarpable>)
                    Class.forName(json.getString(CLASS_KEY));
            mParameter = warpableClass.newInstance();
            mParameter.setJSONObject(value);
        }
        catch(Exception e)
        {
            //TODO: handle exception
            return null;
        }
        return mParameter;
    }

    @Override
    public void setValue(String key, Object value)
    {
        if(key != null && value != null && key.equals(PARAM_KEY))
        {
            mParameter = (IWarpable) value;
        }
    }

    public Object getValue(String key) throws JSONException
    {
        if(key != null && mParameter != null && key.equals(PARAM_KEY))
        {
            return mParameter;
        }
        return super.getValue(key);
    }
}
