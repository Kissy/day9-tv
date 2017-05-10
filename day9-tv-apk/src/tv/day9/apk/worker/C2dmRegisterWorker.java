package tv.day9.apk.worker;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import com.foxykeep.datadroid.exception.RestClientException;
import com.google.android.c2dm.C2DMessaging;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.xml.sax.SAXException;
import tv.day9.apk.config.Constants;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class C2dmRegisterWorker {
    private static final String TAG = C2dmRegisterWorker.class.getSimpleName();

    private static final String AUTH_COOKIE_NAME = "SACSID";

    /**
     * Start the worker.
     *
     * @param context The context.
     * @param accountName     The account name.
     * @param register Register or unregister ?
     * @throws IllegalStateException        Exception.
     * @throws java.io.IOException                  Exception.
     * @throws java.net.URISyntaxException           Exception.
     * @throws com.foxykeep.datadroid.exception.RestClientException          Exception.
     * @throws javax.xml.parsers.ParserConfigurationException Exception.
     * @throws org.xml.sax.SAXException                 Exception.
     * @throws org.json.JSONException                Exception.
     */
    public static void start(final Context context, final String accountName, final boolean register)
            throws IllegalStateException, IOException, URISyntaxException, RestClientException, ParserConfigurationException, SAXException, JSONException {

        if (register) {
            doRegister(context, accountName);
        } else {
            doUnregister(context);
        }
    }

    /**
     * Do unregister.
     *
     * @param context The context.
     */
    private static void doUnregister(Context context) {
        C2DMessaging.unregister(context);
    }

    /**
     * Do register.
     * 
     * @param context The context.
     * @param accountName The accountName.
     */
    private static void doRegister(Context context, String accountName) {
        // Store the account name in shared preferences
        final SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFS, 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.ACCOUNT_NAME, accountName);
        editor.putString(Constants.AUTH_COOKIE, null);
        editor.commit();

        // Obtain an auth token and register
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(Constants.COM_GOOGLE);
        for (Account account : accounts) {
            if (account.name.equals(accountName)) {
                // Get the auth token from the AccountManager and convert
                // it into a cookie for the appengine server
                try {
                    String authToken = accountManager.blockingGetAuthToken(account, Constants.AUTH_TOKEN_TYPE, false);
                    String authCookie = getAuthCookie(authToken);
                    sharedPreferences.edit().putString(Constants.AUTH_COOKIE, authCookie).commit();
                    C2DMessaging.register(context, Constants.SENDER_ID);
                    return;
                } catch (AuthenticatorException e) {
                    Log.w(TAG, "AuthenticatorException " + e);
                } catch (IOException e) {
                    Log.w(TAG, "IOException " + e);
                } catch (OperationCanceledException e) {
                    Log.w(TAG, "OperationCanceledException " + e);
                }
                break;
            }
        }

        throw new IllegalStateException();
    }

    /**
     * Retrieves the authorization cookie associated with the given token. This
     * method should only be used when running against a production appengine
     * backend (as opposed to a dev mode server).
     *
     * @param authToken The auth token.
     * @return The auth cookie.
     */
    private static String getAuthCookie(String authToken) {
        try {
            // Get SACSID cookie
            DefaultHttpClient client = new DefaultHttpClient();
            URI uri = new URI(String.format(Constants.REMOTE_AUTH_URL, URLEncoder.encode(Constants.PROD_URL, "UTF-8"), authToken));
            HttpGet method = new HttpGet(uri);
            final HttpParams getParams = new BasicHttpParams();
            HttpClientParams.setRedirecting(getParams, false);
            method.setParams(getParams);

            HttpResponse res = client.execute(method);
            Header[] headers = res.getHeaders("Set-Cookie");
            if (res.getStatusLine().getStatusCode() != 302 || headers.length == 0) {
                return null;
            }

            for (Cookie cookie : client.getCookieStore().getCookies()) {
                if (AUTH_COOKIE_NAME.equals(cookie.getName())) {
                    return AUTH_COOKIE_NAME + "=" + cookie.getValue();
                }
            }
        } catch (IOException e) {
            Log.w(TAG, "IOException " + e);
        } catch (URISyntaxException e) {
            Log.w(TAG, "URISyntaxException " + e);
        }

        return null;
    }

}
