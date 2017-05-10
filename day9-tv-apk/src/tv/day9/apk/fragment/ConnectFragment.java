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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import tv.day9.apk.R;
import tv.day9.apk.config.Constants;
import tv.day9.apk.manager.AppRequestManager;
import tv.day9.apk.receiver.C2DMReceiver;
import tv.day9.apk.service.AppService;
import tv.day9.apk.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Account selections activity - handles device registration and unregistration.
 */
public class ConnectFragment extends Fragment implements OnClickListener, AppRequestManager.OnRequestFinishedListener {

    private static final String TAG = ConnectFragment.class.getSimpleName();

    private static final String REGISTER = "Register";
    private static final String SKIP = "Skip";
    private static final String C2DM = "C2DM";
    
    private int requestId = -1;
    private int mAccountSelectedPosition = 0;

    private List<String> accounts = new ArrayList<String>();
    private ListView accountsListView;
    private ArrayAdapter<String> listAdapter;
    private ProgressDialog registerDialog;
    private SharedPreferences sharedPreferences;

    /**
     * @inheritDoc
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sharedPreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFS, 0);
    }

    /**
     * @inheritDoc
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_connect, container, false);

        listAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_account, accounts);
        accountsListView = (ListView) rootView.findViewById(R.id.select_account);
        accountsListView.setAdapter(listAdapter);
        accountsListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        accountsListView.setItemChecked(mAccountSelectedPosition, true);

        rootView.findViewById(R.id.connect).setOnClickListener(this);
        rootView.findViewById(R.id.exit).setOnClickListener(this);

        return rootView;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onStop() {
        if (registerDialog != null && registerDialog.isShowing()) {
            registerDialog.dismiss();
        }

        super.onStop();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onResume() {
        super.onResume();

        // Refresh google list accounts
        fillGoogleAccounts(accounts);
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }

        if (accounts.size() == 0) {
            // Show a dialog and invoke the "Add Account" activity if requested
            createAddAcountDialog();
        }
    }

    /**
     * Create the add account dialog.
     */
    private void createAddAcountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.needs_account);
        builder.setNegativeButton(R.string.btn_skip, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                UIUtils.removeC2dmPreferences(sharedPreferences);
                getActivity().finish();
            }
        });
        builder.setPositiveButton(R.string.add_account, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
        builder.setIcon(android.R.drawable.stat_sys_warning);
        builder.setTitle(R.string.attention);
        builder.show();
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.connect) {
            createRegisterDialog();

            mAccountSelectedPosition = accountsListView.getCheckedItemPosition();
            TextView account = (TextView) accountsListView.getChildAt(mAccountSelectedPosition);
            AppRequestManager.from(getActivity()).addOnRequestFinishedListener(this);
            requestId = AppRequestManager.from(getActivity()).c2dmRegister((String) account.getText(), true);
        } else if (i == R.id.exit) {
            UIUtils.removeC2dmPreferences(sharedPreferences);
            getActivity().finish();
        }
    }

    /**
     * Create register dialog.
     */
    private void createRegisterDialog() {
        registerDialog = new ProgressDialog(getActivity());
        registerDialog.setIcon(android.R.drawable.stat_sys_download);
        registerDialog.setMessage(getText(R.string.registering));
        registerDialog.setTitle(R.string.loading);
        registerDialog.setCancelable(true);
        registerDialog.show();
    }

    /**
     * Returns a list of registered Google account names. If no Google accounts
     * are registered on the device, a zero-length list is returned.
     *
     * @param accountsList The list of accounts from adapter.
     */
    private void fillGoogleAccounts(List<String> accountsList) {
        accountsList.clear();
        Account[] accounts = AccountManager.get(getActivity()).getAccounts();
        for (Account account : accounts) {
            if (account.type.equals(Constants.COM_GOOGLE)) {
                accountsList.add(account.name);
            }
        }
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
                if (registerDialog != null) {
                    registerDialog.dismiss();
                    registerDialog = null;
                }
                return;
            }

            if (registerDialog != null) {
                registerDialog.setMessage(getText(R.string.registering_secondstep));
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