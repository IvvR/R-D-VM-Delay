package com.irv.vm_delay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

  public static final String PREF_INITIALISED = "initialized";
  public static final String TEL = "tel:";
  public static final String VOICE_NUMBER = "voice_number";
  public static final String DELAY_POS = "delay_pos";
  public static final String LOCK_SWITCH_CHECKED = "lock_switch_checked";
  public static final String USSD_ACCESS_CMD = "USSD_ACCESS_CMD";
  public static final String USSD_SET_CMD = "USSD_SET_CMD";
  public static final String USSD_SEND_CMD = "USSD_SEND_CMD";
  public static final String USSD_SETTINGS_REQUEST_CMD = "USSD_SETTINGS_REQUEST_CMD";

  private TextView settingsRequest;
  private TextView vmAccessCode;
  private TextView voiceNum;
  private TextView setCode;
  private TextView sendCode;
  private Spinner delaySpin;
  private Switch lockSwitch;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    settingsRequest = findViewById(R.id.edSettingsRequest);
    vmAccessCode = findViewById(R.id.edAccessCode);
    voiceNum = findViewById(R.id.edVoiceNum);
    setCode = findViewById(R.id.edSetCode);

    sendCode = findViewById(R.id.edSendCode);

    Button buCheck = findViewById(R.id.buCheck);
    buCheck.setOnClickListener((View v) -> {
          runDialCmd(settingsRequest.getText().toString());
        }
    );

    Button buSet = findViewById(R.id.buSet);
    buSet.setOnClickListener((View v) -> {
          saveUpdated();
          runDialCmd(getSetTimeCmd());
        }
    );

    delaySpin = findViewById(R.id.delaySpin);
    setUpSpinner();

    lockSwitch = findViewById(R.id.swLock);
    lockSwitch.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
      setEnabled(isChecked);
      updateSwitchPref();
    });

    updateFieldsWithPrefs();
    setEnabled(lockSwitch.isChecked());
  }

  private void setUpSpinner() {
    ArrayAdapter<CharSequence> staticAdapter = ArrayAdapter.
        createFromResource(this, R.array.delays, android.R.layout.simple_spinner_item);
    staticAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    delaySpin.setAdapter(staticAdapter);

  }


  private void runDialCmd(String cmd) {
    String telCmd = TEL + Uri.encode(cmd);
    Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(telCmd));
    try {
      startActivity(i);
    } catch (SecurityException e) {
      e.printStackTrace();
    }
  }

  /**
   * Koodo
   * **61*1 voice_number *11*xx_secs#
   * **61*+1xxxxxxxxxx*11*30#
   * to check
   * *#61#
   * T-Mobile
   * https://support.t-mobile.com/docs/DOC-4041
   * **61*18056377243**10#would be for a 10-second delay.)
   * <p>
   * vmAcessCode + vmNum + vmCommandCode + ringTime + sendCode
   *
   * @return
   */
  private String getSetTimeCmd() {
    return vmAccessCode.getText().toString() + voiceNum.getText().toString() + setCode.getText().toString()
        + String.valueOf(delaySpin.getSelectedItem()) + sendCode.getText().toString();
  }

  private void setEnabled(boolean readOnly) {
    settingsRequest.setFocusable(readOnly);
    settingsRequest.setEnabled(readOnly);
    vmAccessCode.setFocusable(readOnly);
    vmAccessCode.setEnabled(readOnly);
    setCode.setFocusable(readOnly);
    setCode.setEnabled(readOnly);
    sendCode.setFocusable(readOnly);
    sendCode.setEnabled(readOnly);
  }

  private void updateFieldsWithPrefs() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    if (!prefs.contains(PREF_INITIALISED)) {
      fillPref(prefs);
      setUpSpinnerDefault();
    } else {
      updateFields(prefs);
    }
  }

  private void setUpSpinnerDefault() {
    int pos = Integer.valueOf(getString(R.string.default_delay_pos));
    delaySpin.setSelection(pos);
  }

  /**
   * Init the DefShared with default value from resources
   *
   * @param prefs
   */
  private void fillPref(SharedPreferences prefs) {
    SharedPreferences.Editor ed = prefs.edit();
    ed.putString(PREF_INITIALISED, PREF_INITIALISED);
    ed.putString(VOICE_NUMBER, getString(R.string.voice_number));
    ed.putString(DELAY_POS, getString(R.string.default_delay_pos));
    ed.putString(LOCK_SWITCH_CHECKED, getString(R.string.lock_switch_checked));
    ed.putString(USSD_ACCESS_CMD, getString(R.string.USSD_ACCESS_CMD));
    ed.putString(USSD_SET_CMD, getString(R.string.USSD_SET_CMD));
    ed.putString(USSD_SEND_CMD, getString(R.string.USSD_SEND_CMD));
    ed.putString(USSD_SETTINGS_REQUEST_CMD, getString(R.string.USSD_SETTINGS_REQUEST_CMD));
    ed.apply();
  }

  /**
   * @param prefs
   */
  private void updateFields(SharedPreferences prefs) {
    voiceNum.setText(prefs.getString(VOICE_NUMBER, getString(R.string.voice_number)));
    delaySpin.setSelection(Integer.valueOf(prefs.getString(DELAY_POS, getString(R.string.default_delay_pos))));
    lockSwitch.setChecked(Boolean.valueOf(prefs.getString(LOCK_SWITCH_CHECKED, getString(R.string.lock_switch_checked))));
    vmAccessCode.setText(prefs.getString(USSD_ACCESS_CMD, getString(R.string.USSD_ACCESS_CMD)));
    setCode.setText(prefs.getString(USSD_SET_CMD, getString(R.string.USSD_SET_CMD)));
    sendCode.setText(prefs.getString(USSD_SEND_CMD, getString(R.string.USSD_SEND_CMD)));
    settingsRequest.setText(prefs.getString(USSD_SETTINGS_REQUEST_CMD, getString(R.string.USSD_SETTINGS_REQUEST_CMD)));
  }

  /**
   */
  private void saveUpdated() {

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor ed = prefs.edit();
    ed.putString(VOICE_NUMBER, voiceNum.getText().toString());
    ed.putString(DELAY_POS, String.valueOf(delaySpin.getSelectedItemPosition()));
    ed.putString(LOCK_SWITCH_CHECKED, String.valueOf(lockSwitch.isChecked()));
    if (lockSwitch.isChecked()) {
      ed.putString(USSD_ACCESS_CMD, vmAccessCode.getText().toString());
      ed.putString(USSD_SET_CMD, setCode.getText().toString());
      ed.putString(USSD_SEND_CMD, sendCode.getText().toString());
      ed.putString(USSD_SETTINGS_REQUEST_CMD, settingsRequest.getText().toString());
    }
    ed.apply();
  }

  private void updateSwitchPref() {
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    SharedPreferences.Editor ed = prefs.edit();
    ed.putString(LOCK_SWITCH_CHECKED, String.valueOf(lockSwitch.isChecked()));
    ed.apply();
  }
}
