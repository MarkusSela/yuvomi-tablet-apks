package it.marukoshi.yuvomiwrapper;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

/** Native tablet controls. This screen is deliberately outside the Yuvomi WebView. */
public final class TabletSettingsActivity extends Activity {
    private static final int BRIGHTNESS_MAX = 255;
    private static final long NEVER_TIMEOUT = Integer.MAX_VALUE;
    private static final long[] TIMEOUTS = {
            60_000L, 120_000L, 300_000L, 600_000L, 900_000L, 1_800_000L, NEVER_TIMEOUT
    };
    private static final String[] TIMEOUT_LABELS = {
            "1 minute", "2 minutes", "5 minutes", "10 minutes", "15 minutes", "30 minutes", "Never"
    };

    private SeekBar brightnessBar;
    private Switch automaticBrightnessSwitch;
    private Spinner orientationSpinner;
    private Spinner timeoutSpinner;
    private TextView permissionStatus;
    private TextView timeoutStatus;
    private TextView screensaverStatus;
    private View settingsRoot;
    private AudioManager resolvedAudioManager;
    private boolean bindingState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        resolvedAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(0xfff5f3ed);

        LinearLayout root = new LinearLayout(this);
        settingsRoot = root;
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(24), dp(18), dp(28));
        scrollView.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.WRAP_CONTENT));

        TextView title = text("Tablet settings", 28, 0xff191816);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        root.addView(title, marginParams(0, 0, 0, 8));
        root.addView(text("These controls modify Android directly. They are not Yuvomi calendar settings.", 15, 0xff5c5954), marginParams(0, 0, 0, 18));

        root.addView(buildPermissionCard());
        root.addView(buildBrightnessCard(), marginParams(0, 14, 0, 0));
        root.addView(buildAudioCard(), marginParams(0, 14, 0, 0));
        root.addView(buildOrientationCard(), marginParams(0, 14, 0, 0));
        root.addView(buildTimeoutCard(), marginParams(0, 14, 0, 0));
        root.addView(buildScreensaverCard(), marginParams(0, 14, 0, 0));

        setContentView(scrollView);
        refreshState();
    }

    private LinearLayout buildPermissionCard() {
        LinearLayout card = card();
        card.addView(sectionTitle("Android permission"));
        permissionStatus = text("Checking permission…", 14, 0xff5c5954);
        card.addView(permissionStatus, marginParams(0, 6, 0, 10));
        Button button = button("Open settings modification permission");
        button.setOnClickListener(view -> openSystemSettings("write-settings"));
        card.addView(button);
        return card;
    }

    private LinearLayout buildBrightnessCard() {
        LinearLayout card = card();
        card.addView(sectionTitle("Brightness"));
        card.addView(text("Adjust the tablet display.", 14, 0xff5c5954), marginParams(0, 6, 0, 10));
        brightnessBar = new SeekBar(this);
        brightnessBar.setMax(100);
        brightnessBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                if (!bindingState) applyBrightness();
            }
        });
        card.addView(brightnessBar, marginParams(0, 0, 0, 4));
        automaticBrightnessSwitch = new Switch(this);
        automaticBrightnessSwitch.setText("Automatic brightness");
        automaticBrightnessSwitch.setTextSize(16);
        automaticBrightnessSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
            if (!bindingState) applyBrightness();
        });
        card.addView(automaticBrightnessSwitch);
        return card;
    }

    private LinearLayout buildAudioCard() {
        LinearLayout card = card();
        card.addView(sectionTitle("Audio"));
        card.addView(text("Volumes are modified directly by Android.", 14, 0xff5c5954), marginParams(0, 6, 0, 8));
        addVolume(card, "Media volume", AudioManager.STREAM_MUSIC);
        addVolume(card, "Alarms and reminders", AudioManager.STREAM_ALARM);
        addVolume(card, "Notifications", AudioManager.STREAM_NOTIFICATION);
        addVolume(card, "Ringtone", AudioManager.STREAM_RING);
        addVolume(card, "System and keys", AudioManager.STREAM_SYSTEM);
        Switch touchSounds = new Switch(this);
        touchSounds.setText("Touch sounds");
        touchSounds.setTextSize(16);
        touchSounds.setTag("touch-sounds");
        touchSounds.setOnCheckedChangeListener((buttonView, checked) -> {
            if (!bindingState) applyTouchSounds(checked);
        });
        card.addView(touchSounds, marginParams(0, 8, 0, 0));
        return card;
    }

    private void addVolume(LinearLayout card, String label, int stream) {
        TextView title = text(label, 16, 0xff191816);
        title.setTag("volume-label-" + stream);
        card.addView(title, marginParams(0, 8, 0, 0));
        SeekBar bar = new SeekBar(this);
        bar.setMax(100);
        bar.setTag(stream);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }
            @Override public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override public void onStopTrackingTouch(SeekBar seekBar) {
                if (!bindingState) applyVolume(stream, seekBar.getProgress());
            }
        });
        card.addView(bar);
    }

    private LinearLayout buildOrientationCard() {
        LinearLayout card = card();
        card.addView(sectionTitle("Global orientation"));
        card.addView(text("Try to change the global rotation of the Android tablet.", 14, 0xff5c5954), marginParams(0, 6, 0, 8));
        orientationSpinner = spinner(new String[]{"Automatic", "Portrait", "Landscape"});
        orientationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!bindingState) applyOrientation(position == 0 ? "auto" : position == 1 ? "portrait" : "landscape");
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        card.addView(orientationSpinner);
        return card;
    }

    private LinearLayout buildTimeoutCard() {
        LinearLayout card = card();
        card.addView(sectionTitle("Display inactivity"));
        card.addView(text("Set when Android may turn off the display.", 14, 0xff5c5954), marginParams(0, 6, 0, 8));
        timeoutSpinner = spinner(TIMEOUT_LABELS);
        timeoutSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!bindingState) applyScreenTimeout(TIMEOUTS[position]);
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        card.addView(timeoutSpinner);
        timeoutStatus = text("", 13, 0xff5c5954);
        card.addView(timeoutStatus, marginParams(0, 6, 0, 0));
        return card;
    }

    private LinearLayout buildScreensaverCard() {
        LinearLayout card = card();
        card.addView(sectionTitle("Android screensaver"));
        card.addView(text("The state is read from the system. Android may require changes in its protected settings screen.", 14, 0xff5c5954), marginParams(0, 6, 0, 8));
        screensaverStatus = text("Checking status…", 14, 0xff5c5954);
        card.addView(screensaverStatus, marginParams(0, 0, 0, 10));
        Button button = button("Open Android screensaver settings");
        button.setOnClickListener(view -> openSystemSettings("dream"));
        card.addView(button);
        return card;
    }

    private void refreshState() {
        bindingState = true;
        try {
            boolean writable = canWriteSettings();
            permissionStatus.setText(writable
                    ? "Settings modification permission: granted"
                    : "Settings modification permission: not granted yet");
            permissionStatus.setTextColor(writable ? 0xff246b3a : 0xff9b3d24);

            int brightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, BRIGHTNESS_MAX);
            brightnessBar.setProgress(Math.max(0, Math.min(100, Math.round((brightness * 100f) / BRIGHTNESS_MAX))));
            boolean automatic = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL) == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC;
            automaticBrightnessSwitch.setChecked(automatic);

            for (int stream : new int[]{AudioManager.STREAM_MUSIC, AudioManager.STREAM_ALARM, AudioManager.STREAM_NOTIFICATION, AudioManager.STREAM_RING, AudioManager.STREAM_SYSTEM}) {
                View view = findVolumeBar(settingsRoot, stream);
                if (view instanceof SeekBar) ((SeekBar) view).setProgress(volumePercent(stream));
            }
            View touch = settingsRoot.findViewWithTag("touch-sounds");
            if (touch instanceof Switch) {
                ((Switch) touch).setChecked(Settings.System.getInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, 1) != 0);
            }

            String orientation = globalOrientation();
            orientationSpinner.setSelection("auto".equals(orientation) ? 0 : "portrait".equals(orientation) ? 1 : 2);
            long timeout = Settings.System.getLong(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, NEVER_TIMEOUT);
            timeoutSpinner.setSelection(timeoutIndex(timeout));
            timeoutStatus.setText(String.format(Locale.ROOT, "Current Android value: %s", timeoutLabel(timeout)));

            int screensaverEnabled = Settings.Secure.getInt(getContentResolver(), Settings.Secure.SCREENSAVER_ENABLED, 0);
            String component = Settings.Secure.getString(getContentResolver(), Settings.Secure.SCREENSAVER_COMPONENTS);
            screensaverStatus.setText(screensaverEnabled != 0
                    ? "Enabled" + (component == null || component.length() == 0 ? "" : " — configuration present")
                    : "Disabled");
        } catch (Exception error) {
            Toast.makeText(this, "Tablet state unavailable", Toast.LENGTH_SHORT).show();
        } finally {
            bindingState = false;
        }
    }

    private View findVolumeBar(View root, int stream) {
        if (!(root instanceof android.view.ViewGroup)) return null;
        android.view.ViewGroup group = (android.view.ViewGroup) root;
        for (int i = 0; i < group.getChildCount(); i++) {
            View child = group.getChildAt(i);
            if (child instanceof SeekBar && Integer.valueOf(stream).equals(child.getTag())) return child;
            View nested = findVolumeBar(child, stream);
            if (nested != null) return nested;
        }
        return null;
    }

    private void applyBrightness() {
        if (!requireWriteSettings()) return;
        try {
            boolean automatic = automaticBrightnessSwitch.isChecked();
            boolean ok;
            if (automatic) {
                ok = Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            } else {
                int raw = Math.round((brightnessBar.getProgress() / 100f) * BRIGHTNESS_MAX);
                ok = Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                ok &= Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, raw);
            }
            if (!ok) throw new SecurityException();
            refreshState();
        } catch (SecurityException error) {
            showPermissionError();
        }
    }

    private void applyVolume(int stream, int percent) {
        if (resolvedAudioManager == null) return;
        int max = resolvedAudioManager.getStreamMaxVolume(stream);
        if (max <= 0) return;
        try {
            resolvedAudioManager.setStreamVolume(stream, Math.round((percent / 100f) * max), 0);
            refreshState();
        } catch (Exception error) {
            Toast.makeText(this, "Volume cannot be changed", Toast.LENGTH_SHORT).show();
        }
    }

    private void applyTouchSounds(boolean enabled) {
        if (!requireWriteSettings()) return;
        try {
            if (!Settings.System.putInt(getContentResolver(), Settings.System.SOUND_EFFECTS_ENABLED, enabled ? 1 : 0)) {
                throw new SecurityException();
            }
            refreshState();
        } catch (SecurityException error) {
            showPermissionError();
        }
    }

    private void applyOrientation(String mode) {
        if (!requireWriteSettings()) return;
        try {
            boolean ok;
            if ("auto".equals(mode)) {
                ok = Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
            } else {
                ok = Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);
                ok &= Settings.System.putInt(getContentResolver(), Settings.System.USER_ROTATION,
                        "portrait".equals(mode) ? Surface.ROTATION_0 : Surface.ROTATION_90);
            }
            if (!ok || !mode.equals(globalOrientation())) throw new SecurityException();
            setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            refreshState();
        } catch (SecurityException error) {
            showPermissionError();
        }
    }

    private void applyScreenTimeout(long timeout) {
        if (!requireWriteSettings()) return;
        try {
            if (!Settings.System.putLong(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, timeout)) {
                throw new SecurityException();
            }
            refreshState();
        } catch (SecurityException error) {
            showPermissionError();
        }
    }

    private boolean requireWriteSettings() {
        if (canWriteSettings()) return true;
        showPermissionError();
        return false;
    }

    private void showPermissionError() {
        Toast.makeText(this, "Grant Android permission to modify settings", Toast.LENGTH_LONG).show();
    }

    private void openSystemSettings(String action) {
        Intent intent;
        if ("write-settings".equals(action) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                    android.net.Uri.parse("package:" + getPackageName()));
        } else if ("dream".equals(action)) {
            intent = new Intent(Settings.ACTION_DREAM_SETTINGS);
        } else {
            intent = new Intent(Settings.ACTION_DISPLAY_SETTINGS);
        }
        try {
            startActivity(intent);
        } catch (Exception error) {
            Toast.makeText(this, "Android settings unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean canWriteSettings() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.System.canWrite(this);
    }

    private String globalOrientation() {
        int automatic = Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);
        if (automatic != 0) return "auto";
        int rotation = Settings.System.getInt(getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) return "portrait";
        if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) return "landscape";
        return "unsupported";
    }

    private int volumePercent(int stream) {
        if (resolvedAudioManager == null) return 0;
        int max = resolvedAudioManager.getStreamMaxVolume(stream);
        return max <= 0 ? 0 : Math.max(0, Math.min(100,
                Math.round((resolvedAudioManager.getStreamVolume(stream) * 100f) / max)));
    }

    private int timeoutIndex(long timeout) {
        for (int i = 0; i < TIMEOUTS.length; i++) if (TIMEOUTS[i] == timeout) return i;
        return TIMEOUTS.length - 1;
    }

    private String timeoutLabel(long timeout) {
        for (int i = 0; i < TIMEOUTS.length; i++) if (TIMEOUTS[i] == timeout) return TIMEOUT_LABELS[i];
        return "custom";
    }

    private Spinner spinner(String[] values) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        return spinner;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.setBackgroundColor(0xffffffff);
        return card;
    }

    private TextView sectionTitle(String value) {
        TextView title = text(value, 20, 0xff191816);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        return title;
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private Button button(String value) {
        Button button = new Button(this);
        button.setText(value);
        button.setTextSize(14);
        return button;
    }

    private LinearLayout.LayoutParams marginParams(int left, int top, int right, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(dp(left), dp(top), dp(right), dp(bottom));
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionStatus != null) refreshState();
    }
}
