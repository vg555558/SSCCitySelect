package cordova.plugin.sscity;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

import com.nc.rac.cities.citylist.CityList;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.app.Activity.RESULT_OK;

/**
 * This class echoes a string called from JavaScript.
 */
public class SSCCity extends CordovaPlugin {
    private CallbackContext callbackContext;
    
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        if (action.equals("startSelectCity")) {
            try {
                JSONObject message = args.getJSONObject(0);
                this.intentCityList(message, callbackContext);
            }catch (Exception e){
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }
    
    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
    
    private void intentCityList(JSONObject city, CallbackContext callbackContext) {
        try {
            Intent intent = new Intent(this.cordova.getActivity(), CityList.class);
            intent.putExtra("city", city.getString("city"));
            intent.putExtra("resourceType", city.getString("resourceType"));
            //  cordova.setActivityResultCallback(this);
            this.cordova.startActivityForResult(this,intent, 2);
        }catch (Exception e){
            e.printStackTrace();
        }
        this.callbackContext = callbackContext;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            if (data != null) {
                if (!TextUtils.isEmpty(data.getStringExtra("cityResult"))) {
                    try {
                        String cityResult = data.getStringExtra("city");
                        JSONObject jsonObject=new JSONObject();
                        jsonObject.put("city",cityResult);
                        callbackContext.success(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    
                }
            }
        }
    }
}
