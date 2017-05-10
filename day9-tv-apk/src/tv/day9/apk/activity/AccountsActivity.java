package tv.day9.apk.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import tv.day9.apk.config.Constants;
import tv.day9.apk.fragment.ConnectFragment;
import tv.day9.apk.fragment.DisconnectFragment;

/**
 * The AccountsActivity
 */
public class AccountsActivity extends BaseSinglePaneActivity {

    /**
     * @inheritDoc
     */
    @Override
    protected Fragment onCreatePane() {
        SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARED_PREFS, 0);
        String deviceRegistrationID = sharedPreferences.getString(Constants.DEVICE_REGISTRATION_ID, null);
        if (deviceRegistrationID == null || deviceRegistrationID.length() == 0) {
            // Show the 'connect' screen if we are not connected
            return new ConnectFragment();
        } else {
            // Show the 'disconnect' screen if we are connected
            return new DisconnectFragment();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getActivityHelper().setupSubActivity();
    }

}
