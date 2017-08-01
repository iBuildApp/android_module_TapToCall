/****************************************************************************
 *                                                                           *
 *  Copyright (C) 2014-2015 iBuildApp, Inc. ( http://ibuildapp.com )         *
 *                                                                           *
 *  This file is part of iBuildApp.                                          *
 *                                                                           *
 *  This Source Code Form is subject to the terms of the iBuildApp License.  *
 *  You can obtain one at http://ibuildapp.com/license/                      *
 *                                                                           *
 ****************************************************************************/
package com.ibuildapp.romanblack.CallPlugin;

import android.Manifest;
import android.R.drawable;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.appbuilder.sdk.android.AppBuilderModuleMain;
import com.appbuilder.sdk.android.StartUpActivity;
import com.appbuilder.sdk.android.Utils;
import com.appbuilder.sdk.android.Widget;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.ByteArrayInputStream;

/**
 * Main module class. Module entry point. Represents Call widget.
 */
@StartUpActivity(moduleName = "Call")
public class CallPlugin extends AppBuilderModuleMain {

    private boolean flurryStarted = false;
    private String flurryId = "";
    final private CallPlugin mCallPlugin = this;
    private String mPhoneNumber;

    /**
     * Calls given number
     *
     * @param callNumber number to call
     */
    private void callNumber(String callNumber) {
        call("tel:" + callNumber);
    }

    /**
     * Starts stanadrt device calling app
     *
     * @param callString initialization call string
     */
    private void call(String callString) {
        try {
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse(callString));
      /*     if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                return;
            }*/
          //  startActivityForResult(callIntent, 1);
             startActivity(callIntent);
            overridePendingTransition(R.anim.activity_open_translate, R.anim.activity_close_scale);

        } catch (ActivityNotFoundException activityException) {
            Log.d("CallerPlugin", "ActivityNotFoundException", activityException);
        }
    }

    /**
     * Parses module xml data and returns phone number.
     *
     * @param xmlData
     * @return configured phone number
     * @throws RuntimeException
     */
    private String getNumber(String xmlData) throws RuntimeException {
        try {//ErrorLogging
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setCoalescing(true);
            DocumentBuilder builder;
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e1) {
                throw new RuntimeException();
            }
            Document document = null;
            try {
                document = builder.parse(new ByteArrayInputStream(xmlData.getBytes()));
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException();
            }

            String phone = null;
            try {
                NodeList phoneList = document.getElementsByTagName("phone");
                NodeList findPhoneList = phoneList.item(0).getChildNodes();
                phone = (findPhoneList.item(0).getNodeValue());
            } catch (Exception e) {
                throw new RuntimeException();
            }
            return phone;


        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void create() {
        try {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.romanblack_call_main);
            Intent activityIntent = getIntent();
            Widget widget = (Widget) activityIntent.getSerializableExtra("Widget");

            String xmlData = widget.getPluginXmlData().length() == 0
                    ? Utils.readXmlFromFile(widget.getPathToXmlFile())
                    : widget.getPluginXmlData();

            if (xmlData == null) {
                finish();
            }
            try {
                mPhoneNumber = getNumber(xmlData);
            } catch (RuntimeException e) {
                finish();
            }

            if (widget.getTitle() != null && widget.getTitle().length() != 0) {
                setTopBarTitle(widget.getTitle());
            } else {
                setTopBarTitle(getResources().getString(R.string.romanblack_call_call));
            }

            boolean showSideBar = ((Boolean) getIntent().getExtras().getSerializable("showSideBar")).booleanValue();
            if (!showSideBar) {
                setTopBarLeftButtonText(getResources().getString(R.string.common_home_upper), true, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });
            }
            hideTopBar();

            flurryId = getIntent().getExtras().getString("flurry_id");
            showDialog(0);


        } catch (Exception e) {
        }
    }

    /**
     * Creates call dialog with Call and Cancel buttons.
     * @param id
     * @return created dialog
     */
    protected Dialog onCreateDialog(int id) {
        return new AlertDialog.Builder(this)
                .setTitle(mPhoneNumber)
                .setNegativeButton(getString(R.string.romanblack_call_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        })
                .setPositiveButton(getString(R.string.romanblack_call_call), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mCallPlugin.callNumber(mPhoneNumber);
            }
        })
                .setIcon(drawable.ic_menu_call)
                .setCancelable(false)
                .create();
    }

    @Override
    public void start() {
        if (flurryId != null) {
            if (!flurryId.equals("") && !flurryId.equals("FFLLuuRRRRyy")
                    && !flurryStarted) {
                try {
                    flurryStarted = true;
                } catch (Exception e) {
                    Log.e("", "");
                }
            }
        }
    }

    @Override
    public void stop() {
        if (flurryStarted) {
            try {
                flurryStarted = false;
            } catch (Exception e) {
                Log.e("", "");
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}