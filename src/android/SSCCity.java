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
            String message = args.getString(0);
            this.intentCityList(message, callbackContext);
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
    
    private void intentCityList(String city, CallbackContext callbackContext) {
        this.callbackContext = callbackContext;
        Intent intent = new Intent(this.cordova.getActivity(), CityList.class);
        intent.putExtra("city", city);
        //  cordova.setActivityResultCallback(this);
        this.cordova.startActivityForResult(this,intent, 2);
        
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
                        jsonObject.put("city",jsonObject);
                        callbackContext.success(jsonObject);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    
                }
            }
        }
    }
}
