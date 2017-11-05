package com.example.plugin;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;

public class SmartPos extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray data, CallbackContext callbackContext) throws JSONException {

        if (action.equals("test")) {

            String name = data.getString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);
            android.widget.Toast.makeText(cordova.getActivity(), message,android.widget.Toast.LENGTH_LONG).show();  
            return true;
        }else if (action.equals("print")) {

            String name = data.getString(0);
            String message = "Hello, " + name;
            callbackContext.success(message);
            return true;
        } else {            
            return false;

        }
    }
}
