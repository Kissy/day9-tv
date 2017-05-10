package tv.day9.apk.worker;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import com.foxykeep.datadroid.exception.RestClientException;
import org.json.JSONException;
import org.xml.sax.SAXException;
import tv.day9.apk.config.Constants;
import tv.day9.apk.util.UIUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;

public class DeviceRegisterWorker {
    private static final String TAG = DeviceRegisterWorker.class.getSimpleName();

    /**
     * Start the worker.
     *
     * @param context The context.
     * @param deviceRegistrationId    The device registration id.
     * @param register     Do we register or unregister ?
     * @throws IllegalStateException        Exception.
     * @throws java.io.IOException                  Exception.
     * @throws java.net.URISyntaxException           Exception.
     * @throws com.foxykeep.datadroid.exception.RestClientException          Exception.
     * @throws javax.xml.parsers.ParserConfigurationException Exception.
     * @throws org.xml.sax.SAXException                 Exception.
     * @throws org.json.JSONException                Exception.
     */
    public static void start(final Context context, final String deviceRegistrationId, final boolean register)
            throws IllegalStateException, IOException, URISyntaxException, RestClientException, ParserConfigurationException, SAXException, JSONException {

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        String accountName = sharedPreferences.getString(Constants.ACCOUNT_NAME, null);
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        String response;
        URL url = new URL(String.format(Constants.REMOTE_REGISTER_URL, deviceRegistrationId, accountName, deviceId, register));
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            response = urlConnection.getResponseMessage();
        } finally {
            urlConnection.disconnect();
        }

        if (response != null && !response.equals(Constants.RESPONSE_OK)) {
            // Not success
            throw new IllegalStateException();
        }

        if (register) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Constants.DEVICE_REGISTRATION_ID, deviceRegistrationId);
            editor.commit();
        } else {
            UIUtils.removeC2dmPreferences(sharedPreferences);
        }
    }
}
