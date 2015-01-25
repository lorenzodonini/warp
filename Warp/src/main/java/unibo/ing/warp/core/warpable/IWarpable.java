package unibo.ing.warp.core.warpable;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * User: lorenzodonini
 * Date: 12/11/13
 * Time: 14:33
 *
 * I'm still not overly fond of this solution. May try another way.
 */
public interface IWarpable {
    public int warpTo(DataOutputStream outputStream) throws IOException, JSONException;
    public int warpFrom(DataInputStream inputStream) throws IOException, JSONException;
    public byte [] warpTo() throws JSONException;
    public int warpFrom(byte [] buffer) throws JSONException;
    public Object getValue(String key) throws JSONException;
    public void setValue(String key, Object value);
    public JSONObject getJSONObject();
    public void setJSONObject(JSONObject value);
}
