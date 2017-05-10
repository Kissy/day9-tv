/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package tv.day9.apk.fragment;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import tv.day9.apk.R;
import tv.day9.apk.config.Constants;
import tv.day9.apk.manager.AppRequestManager;
import tv.day9.apk.receiver.C2DMReceiver;
import tv.day9.apk.service.AppService;

/**
 * Account selections activity - handles device registration and unregistration.
 */
public class DisconnectFragment extends Fragment implements OnClickListener, AppRequestManager.OnRequestFinishedListener {

    private static final String TAG = DisconnectFragment.class.getSimpleName();

    private static final String C2DM = "C2DM";
    private static final String CLOSE = "Close";
    private static final String UNREGISTER = "Unregister";

    private int requestId = -1;
    private ProgressDialog unregisterDialog;

    /**
     * @inheritDoc
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_disconnect, container, false);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFS, 0);
        String accountName = sharedPreferences.getString(Constants.ACCOUNT_NAME, Constants.STRING_EMPTY);

        // Format the disconnect message with the currently connected account name
        TextView disconnectText = (TextView) rootView.findViewById(R.id.disconnect_text);
        String message = getResources().getString(R.string.disconnect_text);
        String formatted = String.format(message, accountName);
        disconnectText.setText(formatted);

        rootView.findViewById(R.id.disconnect).setOnClickListener(this);
        rootView.findViewById(R.id.exit).setOnClickListener(this);

        return rootView;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.disconnect) {
            createUnregisterDialog();

            AppRequestManager.from(getActivity()).addOnRequestFinishedListener(this);
            requestId = AppRequestManager.from(getActivity()).c2dmRegister(null, false);
        } else if (i == R.id.exit) {
            getActivity().finish();
        }
    }

    /**
     * Create the unregister dialog.
     */
    private void createUnregisterDialog() {
        unregisterDialog = new ProgressDialog(getActivity());
        unregisterDialog.setIcon(android.R.drawable.stat_sys_download);
        unregisterDialog.setMessage(getText(R.string.unregistering));
        unregisterDialog.setTitle(R.string.loading);
        unregisterDialog.setCancelable(true);
        unregisterDialog.show();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onStop() {
        if (unregisterDialog != null && unregisterDialog.isShowing()) {
            unregisterDialog.dismiss();
        }

        super.onStop();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onRequestFinished(int requestId, int resultCode, Bundle payload) {
        if (this.requestId == requestId) {
            this.requestId = -1;

            if (resultCode == AppService.ERROR_CODE) {
                Toast.makeText(getActivity().getApplicationContext(), getActivity().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                if (unregisterDialog != null) {
                    unregisterDialog.dismiss();
                    unregisterDialog = null;
                }
                return;
            }

            if (unregisterDialog != null) {
                unregisterDialog.setMessage(getText(R.string.unregistering_secondstep));
            }
            // Do NOT remove request finished listener, because there is still one query running.
        } else if (C2DMReceiver.getRequestId() == requestId) {
            C2DMReceiver.setRequestId(-1);

            AppRequestManager.from(getActivity()).removeOnRequestFinishedListener(this);

            if (resultCode == AppService.ERROR_CODE) {
                Toast.makeText(getActivity().getApplicationContext(), getActivity().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
            }

            getActivity().finish();
        }
    }
}