package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.nfc.NfcAdapter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.content.pm.ShortcutInfoCompat;
import android.support.v4.content.pm.ShortcutManagerCompat;
import android.support.v4.graphics.drawable.IconCompat;
import android.support.v7.app.AlertDialog;
import android.telephony.PhoneNumberUtils;
import android.text.format.DateFormat;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import static android.content.Context.POWER_SERVICE;

public class DataWrapper {

    public final Context context;
    //private boolean forGUI = false;
    private boolean monochrome = false;
    private int monochromeValue = 0xFF;

    boolean profileListFilled = false;
    boolean eventListFilled = false;
    final List<Profile> profileList = Collections.synchronizedList(new ArrayList<Profile>());
    final List<Event> eventList = Collections.synchronizedList(new ArrayList<Event>());

    //static final String EXTRA_INTERACTIVE = "interactive";

    DataWrapper(Context c,
                        //boolean fgui,
                        boolean mono,
                        int monoVal)
    {
        context = c;

        setParameters(/*fgui, */mono, monoVal);
    }

    void setParameters(
            //boolean fgui,
            boolean mono,
            int monoVal)
    {
        //forGUI = fgui;
        monochrome = mono;
        monochromeValue = monoVal;
    }

    void fillProfileList(boolean generateIcons, boolean generateIndicators)
    {
        synchronized (profileList) {
            if (!profileListFilled)
            {
                profileList.addAll(getNewProfileList(generateIcons, generateIndicators));
                profileListFilled = true;
            }
        }
    }

    List<Profile> getNewProfileList(boolean generateIcons, boolean generateIndicators) {
        List<Profile> newProfileList = DatabaseHandler.getInstance(context).getAllProfiles();

        //if (forGUI)
        //{
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = newProfileList.iterator(); it.hasNext();) {
                Profile profile = it.next();
                if (generateIcons)
                    profile.generateIconBitmap(context, monochrome, monochromeValue);
                if (generateIndicators)
                    profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            }
        //}
        return newProfileList;
    }

    void setProfileList(List<Profile> sourceProfileList)
    {
        synchronized (profileList) {
            if (profileListFilled)
                profileList.clear();
            profileList.addAll(sourceProfileList);
            profileListFilled = true;
        }
    }

    void copyProfileList(DataWrapper fromDataWrapper)
    {
        synchronized (profileList) {
            if (profileListFilled) {
                profileList.clear();
                profileListFilled = false;
            }
            if (fromDataWrapper.profileListFilled) {
                profileList.addAll(fromDataWrapper.profileList);
                profileListFilled = true;
            }
        }
    }

    static Profile getNonInitializedProfile(String name, String icon, int order)
    {
        return new Profile(
                name,
                icon + Profile.defaultValuesString.get("prf_pref_profileIcon_withoutIcon"),
                false,
                order,
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_volumeRingerMode")),
                Profile.defaultValuesString.get("prf_pref_volumeRingtone"),
                Profile.defaultValuesString.get("prf_pref_volumeNotification"),
                Profile.defaultValuesString.get("prf_pref_volumeMedia"),
                Profile.defaultValuesString.get("prf_pref_volumeAlarm"),
                Profile.defaultValuesString.get("prf_pref_volumeSystem"),
                Profile.defaultValuesString.get("prf_pref_volumeVoice"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_soundRingtoneChange")),
                Settings.System.DEFAULT_RINGTONE_URI.toString(),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_soundNotificationChange")),
                Settings.System.DEFAULT_NOTIFICATION_URI.toString(),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_soundAlarmChange")),
                Settings.System.DEFAULT_ALARM_ALERT_URI.toString(),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceAirplaneMode")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWiFi")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceBluetooth")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceScreenTimeout")),
                Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET + Profile.defaultValuesString.get("prf_pref_deviceBrightness_withoutLevel"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWallpaperChange")),
                Profile.defaultValuesString.get("prf_pref_deviceWallpaper"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceMobileData")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceMobileDataPrefs")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceGPS")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceRunApplicationChange")),
                Profile.defaultValuesString.get("prf_pref_deviceRunApplicationPackageName"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceAutosync")),
                Profile.defaultValuesBoolean.get("prf_pref_showInActivator_notShow"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceAutoRotation")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceLocationServicePrefs")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_volumeSpeakerPhone")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceNFC")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_duration")),
                  Profile.AFTERDURATIONDO_RESTARTEVENTS,
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_volumeZenMode")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceKeyguard")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_vibrationOnTouch")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWiFiAP")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_devicePowerSaveMode")),
                Profile.defaultValuesBoolean.get("prf_pref_askForDuration"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceNetworkType")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_notificationLed")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_vibrateWhenRinging")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWallpaperFor")),
                Profile.defaultValuesBoolean.get("prf_pref_hideStatusBarIcon"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_lockDevice")),
                Profile.defaultValuesString.get("prf_pref_deviceConnectToSSID"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_applicationDisableWifiScanning")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_applicationDisableBluetoothScanning")),
                Profile.defaultValuesString.get("prf_pref_durationNotificationSound"),
                Profile.defaultValuesBoolean.get("prf_pref_durationNotificationVibrate"),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceWiFiAPPrefs")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_applicationDisableLocationScanning")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_applicationDisableMobileCellScanning")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_applicationDisableOrientationScanning")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_headsUpNotifications")),
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceForceStopApplicationChange")),
                Profile.defaultValuesString.get("prf_pref_deviceForceStopApplicationPackageName"),
                0,
                Integer.valueOf(Profile.defaultValuesString.get("prf_pref_deviceNetworkTypePrefs"))
            );
    }

    private String getVolumeLevelString(int percentage, int maxValue)
    {
        Double dValue = maxValue / 100.0 * percentage;
        return String.valueOf(dValue.intValue());
    }

    Profile getPredefinedProfile(int index, boolean saveToDB) {
        AudioManager audioManager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        int maximumValueRing = 7;
        int maximumValueNotification = 7;
        int maximumValueMusic = 15;
        int maximumValueAlarm = 7;
        //int	maximumValueSystem = 7;
        //int	maximumValueVoiceCall = 7;
        if (audioManager != null) {
            maximumValueRing = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
            maximumValueNotification = audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
            maximumValueMusic = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            maximumValueAlarm = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            //maximumValueSystem = audioManager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
            //maximumValueVoiceCall = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        }

        Profile profile;

        switch (index) {
            case 0:
                profile = getNonInitializedProfile(context.getString(R.string.default_profile_name_home), "ic_profile_home_2", 1);
                profile._showInActivator = true;
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else
                            profile._volumeRingerMode = 1;
                    } else
                        profile._volumeRingerMode = 1;
                //} else
                //    profile._volumeRingerMode = 1;
                profile._volumeRingtone = getVolumeLevelString(71, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(86, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 1;
                //profile._deviceBrightness = "60|0|0|0";
                break;
            case 1:
                profile = getNonInitializedProfile(context.getString(R.string.default_profile_name_outdoor), "ic_profile_outdoors_1", 2);
                profile._showInActivator = true;
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 4; // ALL with vibration
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 4; // ALL with vibration
                        } else
                            profile._volumeRingerMode = 2;
                    } else
                        profile._volumeRingerMode = 2;
                //} else
                //    profile._volumeRingerMode = 2;
                profile._volumeRingtone = getVolumeLevelString(100, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(100, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(93, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 2;
                //profile._deviceBrightness = "255|0|0|0";
                break;
            case 2:
                profile = getNonInitializedProfile(context.getString(R.string.default_profile_name_work), "ic_profile_work_5", 3);
                profile._showInActivator = true;
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 1; // ALL
                        } else
                            profile._volumeRingerMode = 1;
                    } else
                        profile._volumeRingerMode = 1;
                //} else
                //    profile._volumeRingerMode = 1;
                profile._volumeRingtone = getVolumeLevelString(57, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(71, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(57, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 2;
                //profile._deviceBrightness = "60|0|0|0";
                break;
            case 3:
                profile = getNonInitializedProfile(context.getString(R.string.default_profile_name_meeting), "ic_profile_meeting_2", 4);
                profile._showInActivator = true;
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 3; // NONE
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 3; // NONE
                        } else
                            profile._volumeRingerMode = 4;
                    } else
                        profile._volumeRingerMode = 4;
                //} else
                //    profile._volumeRingerMode = 4;
                profile._volumeRingtone = getVolumeLevelString(57, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(71, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(57, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 0;
                //profile._deviceBrightness = Profile.BRIGHTNESS_ADAPTIVE_BRIGHTNESS_NOT_SET+"|1|1|0";
                break;
            case 4:
                profile = getNonInitializedProfile(context.getString(R.string.default_profile_name_sleep), "ic_profile_sleep", 5);
                profile._showInActivator = true;
                //if (android.os.Build.VERSION.SDK_INT >= 18) {
                    if (ActivateProfileHelper.canChangeZenMode(context, true)) {
                        if (android.os.Build.VERSION.SDK_INT >= 23) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 6; // ALARMS
                        } else if (android.os.Build.VERSION.SDK_INT >= 21) {
                            profile._volumeRingerMode = 5;
                            profile._volumeZenMode = 3; // NONE
                        } else
                            profile._volumeRingerMode = 4;
                    } else
                        profile._volumeRingerMode = 4;
                //} else
                //    profile._volumeRingerMode = 4;
                profile._volumeRingtone = getVolumeLevelString(71, maximumValueRing) + "|0|0";
                profile._volumeNotification = getVolumeLevelString(86, maximumValueNotification) + "|0|0";
                profile._volumeAlarm = getVolumeLevelString(100, maximumValueAlarm) + "|0|0";
                profile._volumeMedia = getVolumeLevelString(80, maximumValueMusic) + "|0|0";
                profile._deviceWiFi = 0;
                //profile._deviceBrightness = "10|0|0|0";
                break;
            case 5:
                profile = getNonInitializedProfile(context.getString(R.string.default_profile_name_battery_low), "ic_profile_battery_1", 6);
                profile._showInActivator = false;
                profile._deviceAutoSync = 2;
                profile._deviceMobileData = 2;
                profile._deviceWiFi = 2;
                profile._deviceBluetooth = 2;
                profile._deviceGPS = 2;
                break;
            default:
                profile = null;
        }

        if (profile != null) {
            if (saveToDB)
                DatabaseHandler.getInstance(context).addProfile(profile, false);
        }

        return profile;
    }

    void fillPredefinedProfileList(boolean generateIcons, boolean generateIndicators)
    {
        synchronized (profileList) {
            invalidateProfileList();
            DatabaseHandler.getInstance(context).deleteAllProfiles();

            for (int index = 0; index < 6; index++)
                getPredefinedProfile(index, true);

            fillProfileList(generateIcons, generateIndicators);
        }
    }

    void invalidateProfileList()
    {
        synchronized (profileList) {
            if (profileListFilled)
            {
                for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                    Profile profile = it.next();
                    profile.releaseIconBitmap();
                    profile.releasePreferencesIndicator();
                    it.remove();
                }
            }
            profileListFilled = false;
        }
    }

    Profile getActivatedProfileFromDB(boolean generateIcon, boolean generateIndicators)
    {
        Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
        if (/*forGUI &&*/ (profile != null))
        {
            if (generateIcon)
                profile.generateIconBitmap(context, monochrome, monochromeValue);
            if (generateIndicators)
                profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
        }
        return profile;
    }

    public Profile getActivatedProfile(boolean generateIcon, boolean generateIndicators)
    {
        synchronized (profileList) {
            if (!profileListFilled) {
                return getActivatedProfileFromDB(generateIcon, generateIndicators);
            } else {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                    Profile profile = it.next();
                    if (profile._checked) {
                        return profile;
                    }
                }
                // when profile not found, get profile from db
                return getActivatedProfileFromDB(generateIcon, generateIndicators);
            }
        }
    }

    public Profile getActivatedProfile(List<Profile> profileList) {
        if (profileList == null) {
            return null;
        } else {
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = profileList.iterator(); it.hasNext();) {
                Profile profile = it.next();
                if (profile._checked)
                    return profile;
            }
            return null;
        }
    }

    void setProfileActive(Profile profile)
    {
        synchronized (profileList) {
            if (!profileListFilled)
                return;

            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Profile> it = profileList.iterator(); it.hasNext();) {
                Profile _profile = it.next();
                _profile._checked = false;
            }

            if (profile != null)
                profile._checked = true;
        }
    }

    void activateProfileFromEvent(long profile_id, /*boolean interactive,*/ boolean manual,
                                         boolean merged)
    {
        int startupSource = PPApplication.STARTUP_SOURCE_SERVICE;
        if (manual)
            startupSource = PPApplication.STARTUP_SOURCE_SERVICE_MANUAL;
        Profile profile = getProfileById(profile_id, false, false, merged);
        if (Permissions.grantProfilePermissions(context, profile, merged, true,
                /*false, monochrome, monochromeValue,*/
                startupSource, /*interactive,*/ null, true)) {
            _activateProfile(profile, merged, startupSource);
        }
    }

    void updateNotificationAndWidgets()
    {
        if (PhoneProfilesService.instance != null)
            PhoneProfilesService.instance.showProfileNotification(this);
        ActivateProfileHelper.updateGUI(context, true);
    }

    private Profile getProfileByIdFromDB(long id, boolean generateIcon, boolean generateIndicators, boolean merged)
    {
        Profile profile = DatabaseHandler.getInstance(context).getProfile(id, merged);
        if (/*forGUI &&*/ (profile != null))
        {
            if (generateIcon)
                profile.generateIconBitmap(context, monochrome, monochromeValue);
            if (generateIndicators)
                profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
        }
        return profile;
    }

    public Profile getProfileById(long id, boolean generateIcon, boolean generateIndicators, boolean merged)
    {
        synchronized (profileList) {
            if ((!profileListFilled) || merged) {
                return getProfileByIdFromDB(id, generateIcon, generateIndicators, merged);
            } else {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                    Profile profile = it.next();
                    if (profile._id == id)
                        return profile;
                }
                // when filter is set and profile not found, get profile from db
                return getProfileByIdFromDB(id, generateIcon, generateIndicators, false);
            }
        }
    }

    void updateProfile(Profile profile)
    {
        if (profile != null)
        {
            Profile origProfile = getProfileById(profile._id, false, false, false);
            if (origProfile != null)
                origProfile.copyProfile(profile);
        }
    }

    void deleteProfile(Profile profile)
    {
        if (profile == null)
            return;

        synchronized (profileList) {
            profileList.remove(profile);
        }
        synchronized (eventList) {
            fillEventList();
            // unlink profile from events
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                if (event._fkProfileStart == profile._id)
                    event._fkProfileStart = 0;
                if (event._fkProfileEnd == profile._id)
                    event._fkProfileEnd = Profile.PROFILE_NO_ACTIVATE;

                String oldFkProfiles = event._startWhenActivatedProfile;
                if (!oldFkProfiles.isEmpty()) {
                    String[] splits = oldFkProfiles.split("\\|");
                    StringBuilder newFkProfiles = new StringBuilder();
                    for (String split : splits) {
                        long fkProfile = Long.valueOf(split);
                        if (fkProfile != profile._id) {
                            if (newFkProfiles.length() > 0)
                                newFkProfiles.append("|");
                            newFkProfiles.append(split);
                        }
                    }
                    event._startWhenActivatedProfile = newFkProfiles.toString();
                }
            }
        }
        // unlink profile from Background profile
        if (Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(context)) == profile._id)
        {
            ApplicationPreferences.getSharedPreferences(context);
            Editor editor = ApplicationPreferences.preferences.edit();
            editor.putString(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE));
            editor.apply();
        }
    }

    void deleteAllProfiles()
    {
        synchronized (profileList) {
            profileList.clear();
        }
        synchronized (eventList) {
            fillEventList();
            // unlink profiles from events
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                event._fkProfileStart = 0;
                event._fkProfileEnd = Profile.PROFILE_NO_ACTIVATE;
                event._startWhenActivatedProfile = "";
            }
        }
        // unlink profiles from Background profile
        ApplicationPreferences.getSharedPreferences(context);
        Editor editor = ApplicationPreferences.preferences.edit();
        editor.putString(ApplicationPreferences.PREF_APPLICATION_BACKGROUND_PROFILE, String.valueOf(Profile.PROFILE_NO_ACTIVATE));
        editor.apply();
    }

    void refreshProfileIcon(Profile profile, boolean generateIcon, boolean generateIndicators) {
        if (profile != null) {
            boolean isIconResourceID = profile.getIsIconResourceID();
            String iconIdentifier = profile.getIconIdentifier();
            DatabaseHandler.getInstance(context).getProfileIcon(profile);
            if (isIconResourceID && iconIdentifier.equals("ic_profile_default") && (!profile.getIsIconResourceID())) {
                if (generateIcon)
                    profile.generateIconBitmap(context, monochrome, monochromeValue);
                if (generateIndicators)
                    profile.generatePreferencesIndicator(context, monochrome, monochromeValue);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private ShortcutInfo createShortcutInfo(Profile profile, boolean restartEvents) {
        boolean isIconResourceID;
        String iconIdentifier;
        Bitmap profileBitmap;
        boolean useCustomColor;

        Intent shortcutIntent;

        isIconResourceID = profile.getIsIconResourceID();
        iconIdentifier = profile.getIconIdentifier();
        useCustomColor = profile.getUseCustomColorForIcon();

        if (isIconResourceID) {
            //noinspection ConstantConditions
            if (profile._iconBitmap != null)
                profileBitmap = profile._iconBitmap;
            else {
                int iconResource = context.getResources().getIdentifier(iconIdentifier, "drawable", context.getPackageName());
                profileBitmap = BitmapFactory.decodeResource(context.getResources(), iconResource);
            }
        } else {
            Resources resources = context.getResources();
            int height = (int) resources.getDimension(android.R.dimen.app_icon_size);
            int width = (int) resources.getDimension(android.R.dimen.app_icon_size);
            //Log.d("---- ShortcutCreatorListFragment.generateIconBitmap","resampleBitmapUri");
            profileBitmap = BitmapManipulator.resampleBitmapUri(iconIdentifier, width, height, context.getApplicationContext());
            if (profileBitmap == null) {
                int iconResource = R.drawable.ic_profile_default;
                profileBitmap = BitmapFactory.decodeResource(context.getResources(), iconResource);
            }
        }

        if (ApplicationPreferences.applicationWidgetIconColor(context).equals("1")) {
            int monochromeValue = 0xFF;
            String applicationWidgetIconLightness = ApplicationPreferences.applicationWidgetIconLightness(context);
            if (applicationWidgetIconLightness.equals("0")) monochromeValue = 0x00;
            if (applicationWidgetIconLightness.equals("25")) monochromeValue = 0x40;
            if (applicationWidgetIconLightness.equals("50")) monochromeValue = 0x80;
            if (applicationWidgetIconLightness.equals("75")) monochromeValue = 0xC0;
            if (applicationWidgetIconLightness.equals("100")) monochromeValue = 0xFF;

            if (isIconResourceID || useCustomColor) {
                // icon is from resource or colored by custom color
                profileBitmap = BitmapManipulator.monochromeBitmap(profileBitmap, monochromeValue/*, getActivity().getBaseContext()*/);
            } else
                profileBitmap = BitmapManipulator.grayScaleBitmap(profileBitmap);
        }

        if (restartEvents) {
            shortcutIntent = new Intent(context.getApplicationContext(), ActionForExternalApplicationActivity.class);
            shortcutIntent.setAction(ActionForExternalApplicationActivity.ACTION_RESTART_EVENTS);
        }
        else {
            shortcutIntent = new Intent(context.getApplicationContext(), BackgroundActivateProfileActivity.class);
            shortcutIntent.setAction(Intent.ACTION_MAIN);
            shortcutIntent.putExtra(PPApplication.EXTRA_STARTUP_SOURCE, PPApplication.STARTUP_SOURCE_SHORTCUT);
            //noinspection ConstantConditions
            shortcutIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
        }

        String id;
        String profileName;
        String longLabel;

        if (restartEvents) {
            id = "restart_events";
            profileName = context.getString(R.string.menu_restart_events);
            longLabel = context.getString(R.string.shortcut_activate_profile) + profileName;
        }
        else {
            id = "profile_" + profile._id;
            profileName = profile._name;
            longLabel = context.getString(R.string.shortcut_activate_profile) + profileName;
        }

        return new ShortcutInfo.Builder(context, id)
                .setShortLabel(profileName)
                .setLongLabel(longLabel)
                .setIcon(Icon.createWithBitmap(profileBitmap))
                .setIntent(shortcutIntent)
                .build();
    }

    void setDynamicLauncherShortcuts() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {
            ShortcutManager shortcutManager = context.getSystemService(ShortcutManager.class);

            if (shortcutManager != null) {
                List<Profile> countedProfiles = DatabaseHandler.getInstance(context).getProfilesForDynamicShortcuts(true);
                List<Profile> notCountedProfiles = DatabaseHandler.getInstance(context).getProfilesForDynamicShortcuts(false);

                ArrayList<ShortcutInfo> shortcuts = new ArrayList<>();

                for (Profile profile : countedProfiles) {
                    PPApplication.logE("DataWrapper.setDynamicLauncherShortcuts", "countedProfile=" + profile._name);
                    profile.generateIconBitmap(context, monochrome, monochromeValue);
                    shortcuts.add(0, createShortcutInfo(profile, false));
                }

                int shortcutsCount = countedProfiles.size();
                if (shortcutsCount < 3) {
                    for (Profile profile : notCountedProfiles) {
                        PPApplication.logE("DataWrapper.setDynamicLauncherShortcuts", "notCountedProfile=" + profile._name);
                        profile.generateIconBitmap(context, monochrome, monochromeValue);
                        shortcuts.add(0, createShortcutInfo(profile, false));

                        ++shortcutsCount;
                        if (shortcutsCount == 3)
                            break;
                    }
                }

                Profile profile = DataWrapper.getNonInitializedProfile(context.getString(R.string.menu_restart_events),
                        context.getResources().getResourceEntryName(R.drawable.ic_action_events_restart_color)+"|1|0|0", 0);
                profile.generateIconBitmap(context, monochrome, monochromeValue);
                shortcuts.add(0, createShortcutInfo(profile, true));

                shortcutManager.setDynamicShortcuts(shortcuts);
            }
        }
    }

//---------------------------------------------------

    void fillEventList()
    {
        synchronized (eventList) {
            if (!eventListFilled) {
                eventList.addAll(DatabaseHandler.getInstance(context).getAllEvents());
                eventListFilled = true;
            }
        }
    }

    /*
    void setEventList(List<Event> sourceEventList) {
        synchronized (eventList) {
            if (eventListFilled)
                eventList.clear();
            eventList.addAll(sourceEventList);
            eventListFilled = true;
        }
    }
    */

    void copyEventList(DataWrapper fromDataWrapper) {
        synchronized (eventList) {
            if (eventListFilled) {
                eventList.clear();
                eventListFilled = false;
            }
            if (fromDataWrapper.eventListFilled) {
                eventList.addAll(fromDataWrapper.eventList);
                eventListFilled = true;
            }
        }
    }

    void invalidateEventList()
    {
        synchronized (eventList) {
            if (eventListFilled)
                eventList.clear();
            eventListFilled = false;
        }
    }

    void sortEventsByStartOrderAsc()
    {
        class PriorityComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                int res = 0;
                if ((lhs != null) && (rhs != null))
                    res =  lhs._startOrder - rhs._startOrder;
                return res;
            }
        }

        synchronized (eventList) {
            fillEventList();
            Collections.sort(eventList, new PriorityComparator());
        }
    }

    void sortEventsByStartOrderDesc()
    {
        class PriorityComparator implements Comparator<Event> {
            public int compare(Event lhs, Event rhs) {
                int res = 0;
                if ((lhs != null) && (rhs != null))
                    res =  rhs._startOrder - lhs._startOrder;
                return res;
            }
        }

        synchronized (eventList) {
            fillEventList();
            Collections.sort(eventList, new PriorityComparator());
        }
    }

    Event getEventById(long id)
    {
        synchronized (eventList) {
            if (!eventListFilled) {
                return DatabaseHandler.getInstance(context).getEvent(id);
            } else {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    if (event._id == id)
                        return event;
                }

                // when filter is set and profile not found, get profile from db
                return DatabaseHandler.getInstance(context).getEvent(id);
            }
        }
    }

    void updateEvent(Event event)
    {
        if (event != null)
        {
            Event origEvent = getEventById(event._id);
            origEvent.copyEvent(event);
        }
    }

    // stops all events associated with profile
    private void stopEventsForProfile(Profile profile, boolean alsoUnlink/*, boolean saveEventStatus*/)
    {
        List<EventTimeline> eventTimelineList = getEventTimelineList();

        synchronized (eventList) {
            fillEventList();
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                //if ((event.getStatusFromDB(this) == Event.ESTATUS_RUNNING) &&
                //	(event._fkProfileStart == profile._id))
                if (event._fkProfileStart == profile._id)
                    event.stopEvent(this, eventTimelineList, false, true, true/*saveEventStatus*/, false);
            }
        }
        if (alsoUnlink) {
            unlinkEventsFromProfile(profile);
            DatabaseHandler.getInstance(context).unlinkEventsFromProfile(profile);
        }
        PPApplication.logE("$$$ restartEvents", "from DataWrapper.stopEventsForProfile");
        restartEvents(false, true/*, false*/);
    }

    void stopEventsForProfileFromMainThread(final Profile profile, final boolean alsoUnlink) {
        final DataWrapper dataWrapper = new DataWrapper(context, monochrome, monochromeValue);
        synchronized (profileList) {
            dataWrapper.copyProfileList(this);
        }
        synchronized (eventList) {
            dataWrapper.copyEventList(this);
        }

        PPApplication.startHandlerThread();
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DataWrapper.stopEventsForProfileFromMainThread");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                dataWrapper.stopEventsForProfile(profile, alsoUnlink);

                if ((wakeLock != null) && wakeLock.isHeld())
                    wakeLock.release();
            }
        });
    }

    // pauses all events
    void pauseAllEvents(boolean noSetSystemEvent, boolean blockEvents/*, boolean activateReturnProfile*/)
    {
        List<EventTimeline> eventTimelineList = getEventTimelineList();

        synchronized (eventList) {
            PPApplication.logE("DataWrapper.pauseAllEvents", "eventListFilled="+eventListFilled);
            fillEventList();
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                if (event != null) {
                    int status = event.getStatusFromDB(this);
                    PPApplication.logE("DataWrapper.pauseAllEvents", "event._name=" + event._name);
                    PPApplication.logE("DataWrapper.pauseAllEvents", "status=" + status);

                    if (status == Event.ESTATUS_RUNNING) {
                        if (!(event._forceRun && event._noPauseByManualActivation)) {
                            event.pauseEvent(this, eventTimelineList, false, true, noSetSystemEvent, true, null, false);
                        }
                    }

                    setEventBlocked(event, false);
                    if (blockEvents && (status == Event.ESTATUS_RUNNING) && event._forceRun) {
                        // block only running forceRun events
                        if (!event._noPauseByManualActivation)
                            setEventBlocked(event, true);
                    }

                    if (!(event._forceRun && event._noPauseByManualActivation)) {
                        // for "push" events, set startTime to 0
                        event._eventPreferencesSMS._startTime = 0;
                        DatabaseHandler.getInstance(context).updateSMSStartTime(event);
                        //event._eventPreferencesNotification._startTime = 0;
                        //DatabaseHandler.getInstance(context).updateNotificationStartTime(event);
                        event._eventPreferencesNFC._startTime = 0;
                        DatabaseHandler.getInstance(context).updateNFCStartTime(event);
                        event._eventPreferencesCall._startTime = 0;
                        DatabaseHandler.getInstance(context).updateCallStartTime(event);
                    }
                }
            }
        }

        // blockEvents == true -> manual profile activation is set
        Event.setEventsBlocked(context, blockEvents);
    }

    private void pauseAllEventsFromMainThread(final boolean noSetSystemEvent, final boolean blockEvents) {
        final DataWrapper dataWrapper = new DataWrapper(context, monochrome, monochromeValue);
        synchronized (profileList) {
            dataWrapper.copyProfileList(this);
        }
        synchronized (eventList) {
            dataWrapper.copyEventList(this);
        }

        PPApplication.startHandlerThread();
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DataWrapper.pauseAllEventsFromMainThread");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                dataWrapper.pauseAllEvents(noSetSystemEvent, blockEvents);

                if ((wakeLock != null) && wakeLock.isHeld())
                    wakeLock.release();
            }
        });
    }

    // stops all events
    void stopAllEvents(boolean saveEventStatus, boolean alsoDelete/*, boolean activateReturnProfile*/)
    {
        List<EventTimeline> eventTimelineList = getEventTimelineList();

        for (int i = eventTimelineList.size()-1; i >= 0; i--)
        {
            EventTimeline eventTimeline = eventTimelineList.get(i);
            if (eventTimeline != null)
            {
                long eventId = eventTimeline._fkEvent;
                Event event = getEventById(eventId);
                if (event != null)
                {
                //if (event.getStatusFromDB(this) != Event.ESTATUS_STOP)
                    event.stopEvent(this, eventTimelineList, false/*activateReturnProfile*/,
                            true, saveEventStatus, false);
                }
            }
        }
        if (alsoDelete) {
            unlinkAllEvents();
            DatabaseHandler.getInstance(context).deleteAllEvents();
        }
    }

    void stopAllEventsFromMainThread(final boolean saveEventStatus, final boolean alsoDelete) {
        final DataWrapper dataWrapper = new DataWrapper(context, monochrome, monochromeValue);
        synchronized (profileList) {
            dataWrapper.copyProfileList(this);
        }
        synchronized (eventList) {
            dataWrapper.copyEventList(this);
        }

        PPApplication.startHandlerThread();
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {

                PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DataWrapper.stopAllEventsFromMainThread");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                dataWrapper.stopAllEvents(saveEventStatus, alsoDelete);

                if ((wakeLock != null) && wakeLock.isHeld())
                    wakeLock.release();
            }
        });
    }

    private void unlinkEventsFromProfile(Profile profile)
    {
        synchronized (eventList) {
            fillEventList();
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                if (event._fkProfileStart == profile._id)
                    event._fkProfileStart = 0;
                if (event._fkProfileEnd == profile._id)
                    event._fkProfileEnd = Profile.PROFILE_NO_ACTIVATE;
            }
        }
    }

    private void unlinkAllEvents()
    {
        synchronized (eventList) {
            fillEventList();
            //noinspection ForLoopReplaceableByForEach
            for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                Event event = it.next();
                event._fkProfileStart = 0;
                event._fkProfileEnd = Profile.PROFILE_NO_ACTIVATE;
            }
        }
    }

    void activateProfileOnBoot()
    {
        if (ApplicationPreferences.applicationActivate(context))
        {
            Profile profile = DatabaseHandler.getInstance(context).getActivatedProfile();
            long profileId;
            if (profile != null)
                profileId = profile._id;
            else
            {
                profileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(context));
                if (profileId == Profile.PROFILE_NO_ACTIVATE)
                    profileId = 0;
            }
            activateProfile(profileId, PPApplication.STARTUP_SOURCE_BOOT, null/*, ""*/);
        }
        else
            activateProfile(0, PPApplication.STARTUP_SOURCE_BOOT, null/*, ""*/);
    }

    private void startEventsOnBoot(boolean startedFromService)
    {
        if (startedFromService) {
            if (ApplicationPreferences.applicationActivate(context) &&
                    ApplicationPreferences.applicationStartEvents(context)) {
                restartEvents(false, false/*, false*/);
            }
            else {
                Event.setGlobalEventsRunning(context, false);
                activateProfileOnBoot();
            }
        }
        else {
            restartEvents(false, false/*, false*/);
        }
    }

    // this is called in boot or first start application
    void firstStartEvents(boolean startedFromService)
    {
        PPApplication.logE("DataWrapper.firstStartEvents", "startedFromService="+startedFromService);

        if (startedFromService)
            invalidateEventList();  // force load form db

        if (!startedFromService) {
            Event.setEventsBlocked(context, false);
            synchronized (eventList) {
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    if (event != null)
                        event._blocked = false;
                }
            }
            DatabaseHandler.getInstance(context).unblockAllEvents();
            Event.setForceRunEventRunning(context, false);
        }

        resetAllEventsInDelayStart(true);
        resetAllEventsInDelayEnd(true);

        if (!getIsManualProfileActivation()) {
            PPApplication.logE("DataWrapper.firstStartEvents", "no manual profile activation, restart events");
            startEventsOnBoot(startedFromService);
        }
        else
        {
            PPApplication.logE("DataWrapper.firstStartEvents", "manual profile activation, activate profile");
            activateProfileOnBoot();
        }
    }

    Event getNonInitializedEvent(String name, int startOrder)
    {
        return new Event(name,
                startOrder,
                0,
                Profile.PROFILE_NO_ACTIVATE,
                Event.ESTATUS_STOP,
                "",
                false,
                false,
                Event.EPRIORITY_MEDIUM,
                0,
                false,
                Event.EATENDDO_RESTART_EVENTS,
                false,
                "",
                0,
                false,
                0,
                0,
                false,
                false,
                false,
                15
         );
    }

    private long getProfileIdByName(String name)
    {
        if (!profileListFilled)
        {
            return DatabaseHandler.getInstance(context).getProfileIdByName(name);
        }
        else
        {
            synchronized (profileList) {
                Profile profile;
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Profile> it = profileList.iterator(); it.hasNext(); ) {
                    profile = it.next();
                    if (profile._name.equals(name))
                        return profile._id;
                }
            }
            return 0;
        }
    }

    Event getPredefinedEvent(int index, boolean saveToDB) {
        Event event;

        switch (index) {
            case 0:
                event = getNonInitializedEvent(context.getString(R.string.default_event_name_during_the_week), 5);
                event._fkProfileStart = getProfileIdByName(context.getString(R.string.default_profile_name_home));
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_NONE;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._monday = true;
                event._eventPreferencesTime._tuesday = true;
                event._eventPreferencesTime._wednesday = true;
                event._eventPreferencesTime._thursday = true;
                event._eventPreferencesTime._friday = true;
                event._eventPreferencesTime._startTime = 8 * 60;
                event._eventPreferencesTime._endTime = 23 * 60;
                //event._eventPreferencesTime._useEndTime = true;
                break;
            case 1:
                event = getNonInitializedEvent(context.getString(R.string.default_event_name_weekend), 5);
                event._fkProfileStart = getProfileIdByName(context.getString(R.string.default_profile_name_home));
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_NONE;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._saturday = true;
                event._eventPreferencesTime._sunday = true;
                event._eventPreferencesTime._startTime = 8 * 60;
                event._eventPreferencesTime._endTime = 23 * 60;
                //event._eventPreferencesTime._useEndTime = true;
                break;
            case 2:
                event = getNonInitializedEvent(context.getString(R.string.default_event_name_during_the_work), 8);
                event._fkProfileStart = getProfileIdByName(context.getString(R.string.default_profile_name_work));
                //event._undoneProfile = true;
                event._atEndDo = Event.EATENDDO_NONE;
                event._priority = Event.EPRIORITY_HIGHER;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._monday = true;
                event._eventPreferencesTime._tuesday = true;
                event._eventPreferencesTime._wednesday = true;
                event._eventPreferencesTime._thursday = true;
                event._eventPreferencesTime._friday = true;
                event._eventPreferencesTime._startTime = 9 * 60 + 30;
                event._eventPreferencesTime._endTime = 17 * 60 + 30;
                //event._eventPreferencesTime._useEndTime = true;
                break;
            case 3:
                event = getNonInitializedEvent(context.getString(R.string.default_event_name_overnight), 5);
                event._fkProfileStart = getProfileIdByName(context.getString(R.string.default_profile_name_sleep));
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_UNDONE_PROFILE;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._monday = true;
                event._eventPreferencesTime._tuesday = true;
                event._eventPreferencesTime._wednesday = true;
                event._eventPreferencesTime._thursday = true;
                event._eventPreferencesTime._friday = true;
                event._eventPreferencesTime._saturday = true;
                event._eventPreferencesTime._sunday = true;
                event._eventPreferencesTime._startTime = 23 * 60;
                event._eventPreferencesTime._endTime = 8 * 60;
                //event._eventPreferencesTime._useEndTime = true;
                break;
            case 4:
                event = getNonInitializedEvent(context.getString(R.string.default_event_name_night_call), 10);
                event._fkProfileStart = getProfileIdByName(context.getString(R.string.default_profile_name_home));
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_UNDONE_PROFILE;
                event._priority = Event.EPRIORITY_HIGHEST;
                event._forceRun = true;
                event._noPauseByManualActivation = false;
                event._eventPreferencesTime._enabled = true;
                event._eventPreferencesTime._monday = true;
                event._eventPreferencesTime._tuesday = true;
                event._eventPreferencesTime._wednesday = true;
                event._eventPreferencesTime._thursday = true;
                event._eventPreferencesTime._friday = true;
                event._eventPreferencesTime._saturday = true;
                event._eventPreferencesTime._sunday = true;
                event._eventPreferencesTime._startTime = 23 * 60;
                event._eventPreferencesTime._endTime = 8 * 60;
                //event._eventPreferencesTime._useEndTime = true;
                event._eventPreferencesCall._enabled = true;
                event._eventPreferencesCall._callEvent = EventPreferencesCall.CALL_EVENT_RINGING;
                event._eventPreferencesCall._contactListType = EventPreferencesCall.CONTACT_LIST_TYPE_WHITE_LIST;
                break;
            case 5:
                event = getNonInitializedEvent(context.getString(R.string.default_event_name_low_battery), 10);
                event._fkProfileStart = getProfileIdByName(context.getString(R.string.default_profile_name_battery_low));
                //event._undoneProfile = false;
                event._atEndDo = Event.EATENDDO_RESTART_EVENTS;
                event._priority = Event.EPRIORITY_HIGHEST;
                event._forceRun = true;
                event._noPauseByManualActivation = false;
                event._eventPreferencesBattery._enabled = true;
                if (Build.VERSION.SDK_INT >= 21) {
                    event._eventPreferencesBattery._levelLow = 0;
                    event._eventPreferencesBattery._levelHight = 100;
                    event._eventPreferencesBattery._powerSaveMode = true;
                }
                else {
                    event._eventPreferencesBattery._levelLow = 0;
                    event._eventPreferencesBattery._levelHight = 10;
                    event._eventPreferencesBattery._powerSaveMode = false;
                }
                event._eventPreferencesBattery._charging = 0;
                break;
            default:
                event = null;
        }

        if (event != null) {
            if (saveToDB)
                DatabaseHandler.getInstance(context).addEvent(event);
        }

        return event;
    }

    void generatePredefinedEventList()
    {
        invalidateEventList();
        DatabaseHandler.getInstance(context).deleteAllEvents();

        for (int index = 0; index < 5; index++)
            getPredefinedEvent(index, true);
    }


//---------------------------------------------------

    List<EventTimeline> getEventTimelineList()
    {
        return DatabaseHandler.getInstance(context).getAllEventTimelines();
    }

    public void invalidateDataWrapper()
    {
        invalidateProfileList();
        invalidateEventList();
    }

//----- Activate profile ---------------------------------------------------------------------------------------------

    private void _activateProfile(Profile _profile, boolean merged, int startupSource
                                    /*,final boolean _interactive,*/)
    {
        // remove last configured profile duration alarm
        ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
        Profile.setActivatedProfileForDuration(context, 0);

        final Profile profile = Profile.getMappedProfile(_profile, context);
        //profile = filterProfileWithBatteryEvents(profile);

        if (profile != null)
            PPApplication.logE("$$$ DataWrapper._activateProfile","profileName="+profile._name);
        else
            PPApplication.logE("$$$ DataWrapper._activateProfile","profile=null");

        PPApplication.logE("$$$ DataWrapper._activateProfile","startupSource="+startupSource);
        PPApplication.logE("$$$ DataWrapper._activateProfile","merged="+merged);

        //boolean interactive = _interactive;
        //final Activity activity = _activity;

        // get currently activated profile
        Profile activatedProfile = getActivatedProfile(false, false);

        if ((startupSource != PPApplication.STARTUP_SOURCE_SERVICE) &&
            //(startupSource != PPApplication.STARTUP_SOURCE_BOOT) &&  // on boot must set as manual activation
            (startupSource != PPApplication.STARTUP_SOURCE_LAUNCHER_START))
        {
            // manual profile activation

            ActivateProfileHelper.lockRefresh = true;

            // pause all events
            // for forceRun events set system events and block all events
            pauseAllEvents(false, true/*, true*/);

            ActivateProfileHelper.lockRefresh = false;
        }

        DatabaseHandler.getInstance(context).activateProfile(_profile);
        setProfileActive(_profile);

        String profileIcon = "";
        int profileDuration = 0;
        if (profile != null)
        {
            profileIcon = profile._icon;

            if ((profile._afterDurationDo != Profile.AFTERDURATIONDO_NOTHING) &&
                    (profile._duration > 0))
                profileDuration = profile._duration;

            // activation with duration
            if ((startupSource != PPApplication.STARTUP_SOURCE_SERVICE) &&
                (startupSource != PPApplication.STARTUP_SOURCE_BOOT) &&
                (startupSource != PPApplication.STARTUP_SOURCE_LAUNCHER_START))
            {
                // manual profile activation
                PPApplication.logE("$$$ DataWrapper._activateProfile","manual profile activation");

                //// set profile duration alarm

                // save before activated profile
                if (activatedProfile != null) {
                    long profileId = activatedProfile._id;
                    PPApplication.logE("$$$ DataWrapper._activateProfile", "setActivatedProfileForDuration profileId=" + profileId);
                    PPApplication.logE("$$$ DataWrapper._activateProfile", "setActivatedProfileForDuration duration=" + profileDuration);
                    Profile.setActivatedProfileForDuration(context, profileId);
                }
                else
                    Profile.setActivatedProfileForDuration(context, 0);

                ProfileDurationAlarmBroadcastReceiver.setAlarm(profile, context);
                ///////////
            }
            else {
                PPApplication.logE("$$$ DataWrapper._activateProfile","NO manual profile activation");
                profileDuration = 0;
            }
        }

        if (PhoneProfilesService.instance != null)
            PhoneProfilesService.instance.showProfileNotification(this);
        ActivateProfileHelper.updateGUI(context, true);

        if (profile != null)
            ActivateProfileHelper.execute(context, profile);

        if ((profile != null) && (!merged)) {
            addActivityLog(DatabaseHandler.ALTYPE_PROFILEACTIVATION, null,
                    getProfileNameWithManualIndicator(profile, true, profileDuration > 0, false, this),
                    profileIcon, profileDuration);
        }

        if (profile != null)
        {
            if (ApplicationPreferences.notificationsToast(context) && (!ActivateProfileHelper.lockRefresh))
            {
                // toast notification
                if (PPApplication.toastHandler != null) {
                    //final Profile __profile = profile;
                    PPApplication.toastHandler.post(new Runnable() {
                        public void run() {
                            showToastAfterActivation(profile);
                        }
                    });
                }// else
                //    showToastAfterActivation(profile);
            }
        }
    }

    void activateProfileFromMainThread(final Profile _profile, final boolean merged, final int startupSource,
                                    final Activity _activity)
    {
        PPApplication.logE("DataWrapper.activateProfileFromMainThread", "start");
        final DataWrapper dataWrapper = new DataWrapper(context, monochrome, monochromeValue);
        synchronized (profileList) {
            dataWrapper.copyProfileList(this);
        }
        synchronized (eventList) {
            dataWrapper.copyEventList(this);
        }

        PPApplication.startHandlerThread();
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DataWrapper.activateProfileFromMainThread");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                PPApplication.logE("DataWrapper.activateProfileFromMainThread", "start in handler");
                dataWrapper._activateProfile(_profile, merged, startupSource);
                if (_activity != null) {
                    DatabaseHandler.getInstance(context).increaseActivationByUserCount(_profile);
                    dataWrapper.setDynamicLauncherShortcuts();
                }
                PPApplication.logE("DataWrapper.activateProfileFromMainThread", "end in handler");

                if ((wakeLock != null) && wakeLock.isHeld())
                    wakeLock.release();

            }
        });

        // for startActivityForResult
        if (_activity != null)
        {
            final Profile profile = Profile.getMappedProfile(_profile, context);

            Intent returnIntent = new Intent();
            if (profile == null)
                returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, 0);
            else
                returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile._id);
            returnIntent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
            _activity.setResult(Activity.RESULT_OK,returnIntent);
        }

        finishActivity(startupSource, true, _activity);

    }

    private void showToastAfterActivation(Profile profile)
    {
        //Log.d("DataWrapper.showToastAfterActivation", "xxx");
        try {
            String profileName = getProfileNameWithManualIndicator(profile, true, false, false, this);
            Toast msg = Toast.makeText(context,
                    context.getResources().getString(R.string.toast_profile_activated_0) + ": " + profileName + " " +
                            context.getResources().getString(R.string.toast_profile_activated_1),
                    Toast.LENGTH_SHORT);
            msg.show();
        }
        catch (Exception ignored) {
        }
        //Log.d("DataWrapper.showToastAfterActivation", "-- end");
    }

    private void activateProfileWithAlert(Profile profile, int startupSource, /*final boolean interactive,*/
                                            Activity activity)
    {
        if (/*interactive &&*/ (ApplicationPreferences.applicationActivateWithAlert(context) ||
                            (startupSource == PPApplication.STARTUP_SOURCE_EDITOR)))
        {
            // set theme and language for dialog alert ;-)
            // not working on Android 2.3.x
            GlobalGUIRoutines.setTheme(activity, true, false, false);
            GlobalGUIRoutines.setLanguage(activity.getBaseContext());

            final Profile _profile = profile;
            //final boolean _interactive = interactive;
            final int _startupSource = startupSource;
            final Activity _activity = activity;
            final DataWrapper _dataWrapper = this;

            if (profile._askForDuration) {
                FastAccessDurationDialog dlg = new FastAccessDurationDialog(_activity, _profile, _dataWrapper,
                        /*monochrome, monochromeValue,*/ _startupSource);
                dlg.show();
            }
            else {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
                dialogBuilder.setTitle(activity.getResources().getString(R.string.profile_string_0) + ": " + profile._name);
                dialogBuilder.setMessage(activity.getResources().getString(R.string.activate_profile_alert_message));
                //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (Permissions.grantProfilePermissions(context, _profile, false, false,
                                /*false, monochrome, monochromeValue,*/
                                _startupSource, /*true,*/ _activity, true))
                            _dataWrapper.activateProfileFromMainThread(_profile, false, _startupSource, /*true,*/ _activity);
                        else {
                            Intent returnIntent = new Intent();
                            _activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                            finishActivity(_startupSource, false, _activity);
                        }
                        }
                });
                dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // for startActivityForResult
                        Intent returnIntent = new Intent();
                        _activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                        finishActivity(_startupSource, false, _activity);
                    }
                });
                dialogBuilder.setOnCancelListener(new DialogInterface.OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        // for startActivityForResult
                        Intent returnIntent = new Intent();
                        _activity.setResult(Activity.RESULT_CANCELED, returnIntent);

                        finishActivity(_startupSource, false, _activity);
                    }
                });
                dialogBuilder.show();
            }
        }
        else
        {
            if (profile._askForDuration/* && interactive*/) {
                FastAccessDurationDialog dlg = new FastAccessDurationDialog(activity, profile, this,
                        /*monochrome, monochromeValue,*/ startupSource);
                dlg.show();
            }
            else {
                boolean granted;
                //if (interactive) {
                    // set theme and language for dialog alert ;-)
                    // not working on Android 2.3.x
                    GlobalGUIRoutines.setTheme(activity, true, false, false);
                    GlobalGUIRoutines.setLanguage(activity.getBaseContext());

                    granted = Permissions.grantProfilePermissions(context, profile, false, false,
                            /*false, monochrome, monochromeValue,*/
                            startupSource, /*true,*/ activity, true);
                /*}
                else
                    granted = Permissions.grantProfilePermissions(context, profile, false, true,
                            forGUI, monochrome, monochromeValue,
                            startupSource, false, null, true);*/
                if (granted)
                    activateProfileFromMainThread(profile, false, startupSource, /*interactive,*/ activity);
            }
        }
    }

    void finishActivity(int startupSource, boolean afterActivation, Activity _activity)
    {
        if (_activity == null)
            return;

        //final Activity activity = _activity;

        boolean finish = true;

        if (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR)
        {
            finish = false;
            if (ApplicationPreferences.applicationClose(context))
            {
                // close of activity after profile activation is enabled
                if (PPApplication.getApplicationStarted(context, false))
                    // application is already started and is possible to close activity
                    finish = afterActivation;
            }
        }
        else
        if (startupSource == PPApplication.STARTUP_SOURCE_EDITOR)
        {
            finish = false;
        }

        if (finish)
            _activity.finish();
    }

    public void activateProfile(final long profile_id, final int startupSource, final Activity activity)
    {
        Profile profile;

        // for activated profile is recommended update of activity
        profile = getActivatedProfile(false, false);

        boolean actProfile = false;
        //boolean interactive = false;
        if ((startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT) ||
            (startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_EDITOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SERVICE) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SERVICE_MANUAL) ||
            (startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER))
        {
            // activation is invoked from shortcut, widget, Activator, Editor, service,
            // do profile activation
            actProfile = true;
            //interactive = ((startupSource != PPApplication.STARTUP_SOURCE_SERVICE));
        }
        else
        if (startupSource == PPApplication.STARTUP_SOURCE_BOOT)
        {
            // activation is invoked during device boot

            ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
            Profile.setActivatedProfileForDuration(context, 0);

            if (ApplicationPreferences.applicationActivate(context))
            {
                actProfile = true;
            }

            if (profile_id == 0)
                profile = null;
        }
        else
        if (startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER_START)
        {
            // activation is invoked from launcher

            ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
            Profile.setActivatedProfileForDuration(context, 0);

            if (ApplicationPreferences.applicationActivate(context))
            {
                actProfile = true;
            }

            if (profile_id == 0)
                profile = null;
        }

        if ((startupSource == PPApplication.STARTUP_SOURCE_SHORTCUT) ||
            (startupSource == PPApplication.STARTUP_SOURCE_WIDGET) ||
            (startupSource == PPApplication.STARTUP_SOURCE_ACTIVATOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_EDITOR) ||
            (startupSource == PPApplication.STARTUP_SOURCE_SERVICE) ||
            (startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER_START) ||
            (startupSource == PPApplication.STARTUP_SOURCE_LAUNCHER))
        {
            if (profile_id == 0)
                profile = null;
            else
                profile = getProfileById(profile_id, false, false, false);
        }


        if (actProfile && (profile != null))
        {
            // profile activation
            if (startupSource == PPApplication.STARTUP_SOURCE_BOOT)
                activateProfileFromMainThread(profile, false, PPApplication.STARTUP_SOURCE_BOOT,
                                        /*boolean _interactive,*/ null);
            else
                activateProfileWithAlert(profile, startupSource, /*interactive,*/ activity);
        }
        else
        {
            DatabaseHandler.getInstance(context).activateProfile(profile);
            setProfileActive(profile);

            if (PhoneProfilesService.instance != null)
                PhoneProfilesService.instance.showProfileNotification(this);
            ActivateProfileHelper.updateGUI(context, true);

            // for startActivityForResult
            if (activity != null)
            {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(PPApplication.EXTRA_PROFILE_ID, profile_id);
                returnIntent.getIntExtra(PPApplication.EXTRA_STARTUP_SOURCE, startupSource);
                activity.setResult(Activity.RESULT_OK,returnIntent);
            }

            finishActivity(startupSource, true, activity);
        }
    }

    void activateProfileAfterDuration(long profile_id)
    {
        int startupSource = PPApplication.STARTUP_SOURCE_SERVICE_MANUAL;
        Profile profile = getProfileById(profile_id, false, false, false);
        PPApplication.logE("DataWrapper.activateProfileAfterDuration", "profile="+profile);
        if (profile == null) {
            PPApplication.logE("DataWrapper.activateProfileAfterDuration", "no activate");
            ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
            Profile.setActivatedProfileForDuration(context, 0);
            if (PhoneProfilesService.instance != null)
                PhoneProfilesService.instance.showProfileNotification(this);
            ActivateProfileHelper.updateGUI(context, true);
            return;
        }
        if (Permissions.grantProfilePermissions(context, profile, false, true,
                /*false, monochrome, monochromeValue,*/
                startupSource, /*true,*/ null, true)) {
            // activateProfileAfterDuration is already called from handlerThread
            PPApplication.logE("DataWrapper.activateProfileAfterDuration", "activate");
            _activateProfile(profile, false, startupSource);
        }
    }

    @SuppressLint({ "NewApi", "SimpleDateFormat" })
    void doHandleEvents(Event event, boolean statePause,
                                    boolean restartEvent, /*boolean interactive,*/
                                    boolean forDelayStartAlarm, boolean forDelayEndAlarm,
                                    boolean reactivate, Profile mergedProfile,
                                    String sensorType)
    {
        if (!Permissions.grantEventPermissions(context, event, true))
            return;

        int newEventStatus;// = Event.ESTATUS_NONE;

        boolean ignoreTime = false;
        boolean ignoreBattery = false;
        boolean ignoreCall = false;
        boolean ignorePeripheral = false;
        boolean ignoreCalendar = false;
        boolean ignoreWifi = false;
        boolean ignoreScreen = false;
        boolean ignoreBluetooth = false;
        boolean ignoreSms = false;
        boolean ignoreNotification = false;
        boolean ignoreApplication = false;
        boolean ignoreLocation = false;
        boolean ignoreOrientation = false;
        boolean ignoreMobileCell = false;
        boolean ignoreNfc = false;
        boolean ignoreRadioSwitch = false;

        boolean timePassed = true;
        boolean batteryPassed = true;
        boolean callPassed = true;
        boolean peripheralPassed = true;
        boolean calendarPassed = true;
        boolean wifiPassed = true;
        boolean screenPassed = true;
        boolean bluetoothPassed = true;
        boolean smsPassed = true;
        boolean notificationPassed = true;
        boolean applicationPassed = true;
        boolean locationPassed = true;
        boolean orientationPassed = true;
        boolean mobileCellPassed = true;
        boolean nfcPassed = true;
        boolean radioSwitchPassed = true;

        PPApplication.logE("%%%%%%% DataWrapper.doHandleEvents","--- start --------------------------");
        PPApplication.logE("%%%%%%% DataWrapper.doHandleEvents","------- event._id="+event._id);
        PPApplication.logE("%%%%%%% DataWrapper.doHandleEvents","------- event._name="+event._name);
        PPApplication.logE("%%%%%%% DataWrapper.doHandleEvents","------- sensorType="+sensorType);

        if (event._eventPreferencesTime._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesTime.PREF_EVENT_TIME_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            // compute start datetime
            long startAlarmTime;
            long endAlarmTime;

            startAlarmTime = event._eventPreferencesTime.computeAlarm(true);

            String alarmTimeS = DateFormat.getDateFormat(context).format(startAlarmTime) +
                                " " + DateFormat.getTimeFormat(context).format(startAlarmTime);
            PPApplication.logE("%%% DataWrapper.doHandleEvents","startAlarmTime="+alarmTimeS);

            endAlarmTime = event._eventPreferencesTime.computeAlarm(false);

            alarmTimeS = DateFormat.getDateFormat(context).format(endAlarmTime) +
                         " " + DateFormat.getTimeFormat(context).format(endAlarmTime);
            PPApplication.logE("%%% DataWrapper.doHandleEvents","endAlarmTime="+alarmTimeS);

            Calendar now = Calendar.getInstance();
            long nowAlarmTime = now.getTimeInMillis();
            alarmTimeS = DateFormat.getDateFormat(context).format(nowAlarmTime) +
                 " " + DateFormat.getTimeFormat(context).format(nowAlarmTime);
            PPApplication.logE("%%% DataWrapper.doHandleEvents","nowAlarmTime="+alarmTimeS);

            timePassed = ((nowAlarmTime >= startAlarmTime) && (nowAlarmTime < endAlarmTime));

            PPApplication.logE("%%% DataWrapper.doHandleEvents","timePassed="+timePassed);

            //eventStart = eventStart && timePassed;
        }

        if (event._eventPreferencesBattery._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesBattery.PREF_EVENT_BATTERY_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            boolean isPowerSaveMode = isPowerSaveMode(context);
            PPApplication.logE("*** DataWrapper.doHandleEvents", "isPowerSaveMode=" + isPowerSaveMode);

            boolean isCharging;
            int batteryPct;

            // get battery status
            Intent batteryStatus = null;
            try { // Huawei devices: java.lang.IllegalArgumentException: regist too many Broadcast Receivers
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                batteryStatus = context.registerReceiver(null, filter);
            } catch (Exception ignored) {}

            if (batteryStatus != null) {
                batteryPassed = false;

                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                PPApplication.logE("*** DataWrapper.doHandleEvents", "status=" + status);
                isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                        status == BatteryManager.BATTERY_STATUS_FULL;
                PPApplication.logE("*** DataWrapper.doHandleEvents", "isCharging=" + isCharging);

                int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                PPApplication.logE("*** DataWrapper.doHandleEvents", "level=" + level);
                PPApplication.logE("*** DataWrapper.doHandleEvents", "scale=" + scale);

                batteryPct = Math.round(level / (float) scale * 100);
                PPApplication.logE("*** DataWrapper.doHandleEvents", "batteryPct=" + batteryPct);

                if ((batteryPct >= event._eventPreferencesBattery._levelLow) &&
                        (batteryPct <= event._eventPreferencesBattery._levelHight))
                    batteryPassed = true;

                if (event._eventPreferencesBattery._charging > 0) {
                    if (event._eventPreferencesBattery._charging == 1)
                        batteryPassed = batteryPassed && isCharging;
                    else
                        batteryPassed = batteryPassed && (!isCharging);
                }
                else
                if (event._eventPreferencesBattery._powerSaveMode)
                    batteryPassed = batteryPassed && isPowerSaveMode;
            }
            else
                ignoreBattery = true;
        }

        if ((event._eventPreferencesCall._enabled)  &&
                (Event.isEventPreferenceAllowed(EventPreferencesCall.PREF_EVENT_CALL_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED)&&
                Permissions.checkEventCallContacts(context, event, null) &&
                Permissions.checkEventPhoneBroadcast(context, event, null))
        {
            ApplicationPreferences.getSharedPreferences(context);
            int callEventType = ApplicationPreferences.preferences.getInt(PhoneCallBroadcastReceiver.PREF_EVENT_CALL_EVENT_TYPE, PhoneCallBroadcastReceiver.CALL_EVENT_UNDEFINED);
            String phoneNumber = ApplicationPreferences.preferences.getString(PhoneCallBroadcastReceiver.PREF_EVENT_CALL_PHONE_NUMBER, "");

            PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "callEventType="+callEventType);
            PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "phoneNumber="+phoneNumber);

            boolean phoneNumberFound = false;

            if (callEventType != PhoneCallBroadcastReceiver.CALL_EVENT_UNDEFINED)
            {
                if (event._eventPreferencesCall._contactListType != EventPreferencesCall.CONTACT_LIST_TYPE_NOT_USE)
                {
                    PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "search in gropus");
                    // find phone number in groups
                    String[] splits = event._eventPreferencesCall._contactGroups.split("\\|");
                    for (String split : splits) {
                        String[] projection = new String[]{ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID};
                        String selection = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "=? AND "
                                + ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='"
                                + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'";
                        String[] selectionArgs = new String[]{split};
                        Cursor mCursor = context.getContentResolver().query(ContactsContract.Data.CONTENT_URI, projection, selection, selectionArgs, null);
                        if (mCursor != null) {
                            while (mCursor.moveToNext()) {
                                String contactId = mCursor.getString(mCursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
                                String[] projection2 = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                                String selection2 = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?" + " and " +
                                        ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "=1";
                                String[] selection2Args = new String[]{contactId};
                                Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection2, selection2, selection2Args, null);
                                if (phones != null) {
                                    while (phones.moveToNext()) {
                                        String _phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                        if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                            phoneNumberFound = true;
                                            break;
                                        }
                                    }
                                    phones.close();
                                }
                                if (phoneNumberFound)
                                    break;
                            }
                            mCursor.close();
                        }
                        if (phoneNumberFound)
                            break;
                    }

                    if (!phoneNumberFound) {
                        PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "search in contacts");
                        // find phone number in contacts
                        splits = event._eventPreferencesCall._contacts.split("\\|");
                        for (String split : splits) {
                            //PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "split="+split);
                            String[] splits2 = split.split("#");

                            // get phone number from contacts
                            String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.HAS_PHONE_NUMBER};
                            String selection = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1' and " + ContactsContract.Contacts._ID + "=?";
                            String[] selectionArgs = new String[]{splits2[0]};
                            Cursor mCursor = context.getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, projection, selection, selectionArgs, null);
                            if (mCursor != null) {
                                while (mCursor.moveToNext()) {
                                    String[] projection2 = new String[]{ContactsContract.CommonDataKinds.Phone._ID, ContactsContract.CommonDataKinds.Phone.NUMBER};
                                    String selection2 = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?" + " and " + ContactsContract.CommonDataKinds.Phone._ID + "=?";
                                    String[] selection2Args = new String[]{splits2[0], splits2[1]};
                                    Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection2, selection2, selection2Args, null);
                                    if (phones != null) {
                                        while (phones.moveToNext()) {
                                            String _phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                            PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "_phoneNumber="+_phoneNumber);
                                            if (PhoneNumberUtils.compare(_phoneNumber, phoneNumber)) {
                                                phoneNumberFound = true;
                                                break;
                                            }
                                        }
                                        phones.close();
                                    }
                                    if (phoneNumberFound)
                                        break;
                                }
                                mCursor.close();
                            }
                            if (phoneNumberFound)
                                break;
                        }
                    }

                    if (event._eventPreferencesCall._contactListType == EventPreferencesCall.CONTACT_LIST_TYPE_BLACK_LIST)
                        phoneNumberFound = !phoneNumberFound;
                }
                else
                    phoneNumberFound = true;

                PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "phoneNumberFound="+phoneNumberFound);

                if (phoneNumberFound)
                {
                    if (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_RINGING)
                    {
                        //noinspection StatementWithEmptyBody
                        if ((callEventType == PhoneCallBroadcastReceiver.CALL_EVENT_INCOMING_CALL_RINGING) ||
                            ((callEventType == PhoneCallBroadcastReceiver.CALL_EVENT_INCOMING_CALL_ANSWERED)))
                            ;//eventStart = eventStart && true;
                        else
                            callPassed = false;
                    }
                    else
                    if (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_INCOMING_CALL_ANSWERED)
                    {
                        //noinspection StatementWithEmptyBody
                        if (callEventType == PhoneCallBroadcastReceiver.CALL_EVENT_INCOMING_CALL_ANSWERED)
                            ;//eventStart = eventStart && true;
                        else
                            callPassed = false;
                    }
                    else
                    if (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_OUTGOING_CALL_STARTED)
                    {
                        //noinspection StatementWithEmptyBody
                        if (callEventType == PhoneCallBroadcastReceiver.CALL_EVENT_OUTGOING_CALL_ANSWERED)
                            ;//eventStart = eventStart && true;
                        else
                            callPassed = false;
                    }
                    else
                    if (event._eventPreferencesCall._callEvent == EventPreferencesCall.CALL_EVENT_MISSED_CALL)
                    {
                        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                        long startTime = event._eventPreferencesCall._startTime - gmtOffset;

                        if (PPApplication.logEnabled()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                            String alarmTimeS = sdf.format(startTime);
                            PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "startTime=" + alarmTimeS);
                        }

                        // compute end datetime
                        long endAlarmTime = event._eventPreferencesCall.computeAlarm();
                        if (PPApplication.logEnabled()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                            String alarmTimeS = sdf.format(endAlarmTime);
                            PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                        }

                        Calendar now = Calendar.getInstance();
                        long nowAlarmTime = now.getTimeInMillis();
                        if (PPApplication.logEnabled()) {
                            SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                            String alarmTimeS = sdf.format(nowAlarmTime);
                            PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "nowAlarmTime=" + alarmTimeS);
                        }

                        if (sensorType.equals(EventsHandler.SENSOR_TYPE_PHONE_CALL)) {
                            //noinspection StatementWithEmptyBody
                            if (callEventType == PhoneCallBroadcastReceiver.CALL_EVENT_MISSED_CALL)
                                ;//eventStart = eventStart && true;
                            else
                                callPassed = false;
                        }
                        else if (!event._eventPreferencesCall._permanentRun) {
                            if (sensorType.equals(EventsHandler.SENSOR_TYPE_PHONE_CALL_EVENT_END))
                                callPassed = false;
                            else
                                callPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                        }
                        else {
                            callPassed = nowAlarmTime >= startTime;
                        }
                    }

                    if ((callEventType == PhoneCallBroadcastReceiver.CALL_EVENT_INCOMING_CALL_ENDED) ||
                        (callEventType == PhoneCallBroadcastReceiver.CALL_EVENT_OUTGOING_CALL_ENDED))
                    {
                        //callPassed = true;
                        //eventStart = eventStart && false;
                        callPassed = false;
                    }
                }
                else
                    callPassed = false;

            }
            else
                callPassed = false;

            PPApplication.logE("[CALL] DataWrapper.doHandleEvents", "callPassed="+callPassed);

            if (!callPassed) {
                event._eventPreferencesCall._startTime = 0;
                DatabaseHandler.getInstance(context).updateCallStartTime(event);
            }

        }

        if (event._eventPreferencesPeripherals._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesPeripherals.PREF_EVENT_PERIPHERAL_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_DESK_DOCK) ||
                (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_CAR_DOCK))
            {
                // get dock status
                IntentFilter iFilter = new IntentFilter(Intent.ACTION_DOCK_EVENT);
                Intent dockStatus = context.registerReceiver(null, iFilter);

                if (dockStatus != null)
                {
                    int dockState = dockStatus.getIntExtra(Intent.EXTRA_DOCK_STATE, -1);
                    boolean isDocked = dockState != Intent.EXTRA_DOCK_STATE_UNDOCKED;
                    boolean isCar = dockState == Intent.EXTRA_DOCK_STATE_CAR;
                    boolean isDesk = dockState == Intent.EXTRA_DOCK_STATE_DESK ||
                                     dockState == Intent.EXTRA_DOCK_STATE_LE_DESK ||
                                     dockState == Intent.EXTRA_DOCK_STATE_HE_DESK;

                    if (isDocked)
                    {
                        if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_DESK_DOCK)
                                && isDesk)
                            peripheralPassed = true;
                        else
                            peripheralPassed = (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_CAR_DOCK)
                                    && isCar;
                    }
                    else
                        peripheralPassed = false;
                    //eventStart = eventStart && peripheralPassed;
                }
                else
                    ignorePeripheral = true;
            }
            else
            if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_WIRED_HEADSET) ||
                (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_BLUETOOTH_HEADSET) ||
                (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_HEADPHONES))
            {
                ApplicationPreferences.getSharedPreferences(context);
                boolean headsetConnected = ApplicationPreferences.preferences.getBoolean(HeadsetConnectionBroadcastReceiver.PREF_EVENT_HEADSET_CONNECTED, false);
                boolean headsetMicrophone = ApplicationPreferences.preferences.getBoolean(HeadsetConnectionBroadcastReceiver.PREF_EVENT_HEADSET_MICROPHONE, false);
                boolean bluetoothHeadset = ApplicationPreferences.preferences.getBoolean(HeadsetConnectionBroadcastReceiver.PREF_EVENT_HEADSET_BLUETOOTH, false);

                if (headsetConnected)
                {
                    if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_WIRED_HEADSET)
                            && headsetMicrophone && (!bluetoothHeadset))
                        peripheralPassed = true;
                    else
                    if ((event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_BLUETOOTH_HEADSET)
                            && headsetMicrophone && bluetoothHeadset)
                        peripheralPassed = true;
                    else
                        peripheralPassed = (event._eventPreferencesPeripherals._peripheralType == EventPreferencesPeripherals.PERIPHERAL_TYPE_HEADPHONES)
                                && (!headsetMicrophone) && (!bluetoothHeadset);
                }
                else
                    peripheralPassed = false;
                //eventStart = eventStart && peripheralPassed;
            }
        }

        if ((event._eventPreferencesCalendar._enabled) &&
                (Event.isEventPreferenceAllowed(EventPreferencesCalendar.PREF_EVENT_CALENDAR_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED) &&
                (Permissions.checkEventCalendar(context, event, null)))
        {
            // compute start datetime
            long startAlarmTime;
            long endAlarmTime;

            if (event._eventPreferencesCalendar._eventFound)
            {
                startAlarmTime = event._eventPreferencesCalendar.computeAlarm(true);

                String alarmTimeS = DateFormat.getDateFormat(context).format(startAlarmTime) +
                                    " " + DateFormat.getTimeFormat(context).format(startAlarmTime);
                PPApplication.logE("DataWrapper.doHandleEvents","startAlarmTime="+alarmTimeS);

                endAlarmTime = event._eventPreferencesCalendar.computeAlarm(false);

                alarmTimeS = DateFormat.getDateFormat(context).format(endAlarmTime) +
                             " " + DateFormat.getTimeFormat(context).format(endAlarmTime);
                PPApplication.logE("DataWrapper.doHandleEvents","endAlarmTime="+alarmTimeS);

                Calendar now = Calendar.getInstance();
                long nowAlarmTime = now.getTimeInMillis();
                alarmTimeS = DateFormat.getDateFormat(context).format(nowAlarmTime) +
                     " " + DateFormat.getTimeFormat(context).format(nowAlarmTime);
                PPApplication.logE("DataWrapper.doHandleEvents","nowAlarmTime="+alarmTimeS);

                calendarPassed = ((nowAlarmTime >= startAlarmTime) && (nowAlarmTime < endAlarmTime));
            }
            else
                calendarPassed = false;

            //eventStart = eventStart && calendarPassed;
        }
        

        if (event._eventPreferencesWifi._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesWifi.PREF_EVENT_WIFI_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED)
                && Permissions.checkEventLocation(context, event, null))
        {
            PPApplication.logE("----- DataWrapper.doHandleEvents","-------- eventSSID="+event._eventPreferencesWifi._SSID);

            wifiPassed = false;

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            boolean isWifiEnabled = wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;

            List<WifiSSIDData> wifiConfigurationList = WifiScanJob.getWifiConfigurationList(context);

            boolean done = false;

            if (isWifiEnabled)
            {
                PPApplication.logE("----- DataWrapper.doHandleEvents","wifiStateEnabled=true");

                //PPApplication.logE("----- DataWrapper.doHandleEvents","-- eventSSID="+event._eventPreferencesWifi._SSID);

                WifiInfo wifiInfo = wifiManager.getConnectionInfo();

                boolean wifiConnected = false;

                ConnectivityManager connManager = null;
                try {
                    connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                } catch (Exception ignored) {
                    // java.lang.NullPointerException: missing IConnectivityManager
                    // Dual SIM?? Bug in Android ???
                }
                if (connManager != null) {
                    if (android.os.Build.VERSION.SDK_INT >= 21) {
                        Network[] networks = connManager.getAllNetworks();
                        if ((networks != null) && (networks.length > 0)) {
                            for (Network ntk : networks) {
                                try {
                                    NetworkInfo ntkInfo = connManager.getNetworkInfo(ntk);
                                    if (ntkInfo != null) {
                                        if (ntkInfo.getType() == ConnectivityManager.TYPE_WIFI && ntkInfo.isConnected()) {
                                            if (wifiInfo != null) {
                                                wifiConnected = true;
                                                break;
                                            }
                                        }
                                    }
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    } else {
                        NetworkInfo ntkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        wifiConnected = (ntkInfo != null) && ntkInfo.isConnected();
                    }
                }

                if (wifiConnected)
                {
                    PPApplication.logE("----- DataWrapper.doHandleEvents","wifi connected");

                    PPApplication.logE("----- DataWrapper.doHandleEvents","wifiSSID="+ WifiScanJob.getSSID(wifiInfo, wifiConfigurationList));
                    PPApplication.logE("----- DataWrapper.doHandleEvents","wifiBSSID="+wifiInfo.getBSSID());

                    //PPApplication.logE("----- DataWrapper.doHandleEvents","SSID="+event._eventPreferencesWifi._SSID);

                    String[] splits = event._eventPreferencesWifi._SSID.split("\\|");
                    for (String _ssid : splits) {
                        if (_ssid.equals(EventPreferencesWifi.ALL_SSIDS_VALUE)){
                            wifiPassed = true;
                        }
                        else
                        if (_ssid.equals(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE)){
                            for (WifiSSIDData data : wifiConfigurationList) {
                                wifiPassed = WifiScanJob.compareSSID(wifiInfo, data.ssid.replace("\"", ""), wifiConfigurationList);
                                if (wifiPassed)
                                    break;
                            }
                        }
                        else
                            wifiPassed = WifiScanJob.compareSSID(wifiInfo, _ssid, wifiConfigurationList);
                        if (wifiPassed)
                            break;
                    }

                    //PPApplication.logE("----- DataWrapper.doHandleEvents","wifiPassed="+wifiPassed);

                    if (wifiPassed)
                    {
                        // event SSID is connected
                        done = true;

                        if ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTCONNECTED) ||
                            (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTINFRONT))
                            // for this connectionTypes, wifi must not be connected to event SSID
                            wifiPassed = false;
                        //PPApplication.logE("----- DataWrapper.doHandleEvents","wifiPassed="+wifiPassed);
                    }
                    else {
                        if (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTCONNECTED) {
                            // for this connectionTypes, wifi must not be connected to event SSID
                            done = true;
                            wifiPassed = true;
                        }
                    }
                }
                else
                {
                    PPApplication.logE("----- DataWrapper.doHandleEvents", "wifi not connected");

                    if (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTCONNECTED) {
                        // for this connectionTypes, wifi must not be connected to event SSID
                        done = true;
                        wifiPassed = true;
                    }
                }

            }
            else {
                PPApplication.logE("----- DataWrapper.doHandleEvents", "wifiStateEnabled=false");
                if ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_CONNECTED) ||
                    (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTCONNECTED)) {
                    // for this connectionTypes, wifi must not be connected to event SSID
                    done = true;
                    wifiPassed = (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTCONNECTED);
                }
            }

            PPApplication.logE("----- DataWrapper.doHandleEvents","wifiPassed="+wifiPassed);

            if ((event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_INFRONT) ||
                (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTINFRONT))
            {
                if (!done) {
                    if (!ApplicationPreferences.applicationEventWifiEnableScannig(context)) {
                        // ignore for disabled scanning
                        ignoreWifi = true;
                    } else {
                        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                        if (!pm.isScreenOn() && ApplicationPreferences.applicationEventWifiScanOnlyWhenScreenIsOn(context)) {
                            // ignore for screen Off
                            ignoreWifi = true;
                        } else {

                            wifiPassed = false;

                            List<WifiSSIDData> scanResults = WifiScanJob.getScanResults(context);

                            //PPApplication.logE("----- DataWrapper.doHandleEvents","scanResults="+scanResults);

                            if (scanResults != null) {
                                PPApplication.logE("----- DataWrapper.doHandleEvents", "scanResults != null");
                                PPApplication.logE("----- DataWrapper.doHandleEvents", "scanResults.size=" + scanResults.size());
                                //PPApplication.logE("----- DataWrapper.doHandleEvents","-- eventSSID="+event._eventPreferencesWifi._SSID);

                                for (WifiSSIDData result : scanResults) {
                                    PPApplication.logE("----- DataWrapper.doHandleEvents", "scanSSID=" + result.ssid);
                                    PPApplication.logE("----- DataWrapper.doHandleEvents", "scanBSSID=" + result.bssid);
                                    String[] splits = event._eventPreferencesWifi._SSID.split("\\|");
                                    for (String _ssid : splits) {
                                        if (_ssid.equals(EventPreferencesWifi.ALL_SSIDS_VALUE)) {
                                            PPApplication.logE("----- DataWrapper.doHandleEvents", "all ssids");
                                            wifiPassed = true;
                                            break;
                                        } else if (_ssid.equals(EventPreferencesWifi.CONFIGURED_SSIDS_VALUE)) {
                                            PPApplication.logE("----- DataWrapper.doHandleEvents", "configured ssids");
                                            for (WifiSSIDData data : wifiConfigurationList) {
                                                PPApplication.logE("----- DataWrapper.doHandleEvents", "configured SSID=" + data.ssid.replace("\"", ""));
                                                if (WifiScanJob.compareSSID(result, data.ssid.replace("\"", ""), wifiConfigurationList)) {
                                                    PPApplication.logE("----- DataWrapper.doHandleEvents", "wifi found");
                                                    wifiPassed = true;
                                                    break;
                                                }
                                            }
                                            if (wifiPassed)
                                                break;
                                        } else {
                                            PPApplication.logE("----- DataWrapper.doHandleEvents", "event SSID=" + event._eventPreferencesWifi._SSID);
                                            if (WifiScanJob.compareSSID(result, _ssid, wifiConfigurationList)) {
                                                PPApplication.logE("----- DataWrapper.doHandleEvents", "wifi found");
                                                wifiPassed = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (wifiPassed)
                                        break;
                                }

                                PPApplication.logE("----- DataWrapper.doHandleEvents", "wifiPassed=" + wifiPassed);

                                if (event._eventPreferencesWifi._connectionType == EventPreferencesWifi.CTYPE_NOTINFRONT)
                                    // if wifi is not in front of event SSID, then passed
                                    wifiPassed = !wifiPassed;

                                PPApplication.logE("----- DataWrapper.doHandleEvents", "wifiPassed=" + wifiPassed);

                            } else
                                PPApplication.logE("----- DataWrapper.doHandleEvents", "scanResults == null");
                        }
                    }
                }
            }

            PPApplication.logE("----- DataWrapper.doHandleEvents","------- wifiPassed="+wifiPassed);

            //eventStart = eventStart && wifiPassed;
        }


        if (event._eventPreferencesScreen._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesScreen.PREF_EVENT_SCREEN_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            boolean isScreenOn;
            //if (android.os.Build.VERSION.SDK_INT >= 20)
            //{
            //	Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
            //	isScreenOn = display.getState() != Display.STATE_OFF;
            //}
            //else
            //{
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                isScreenOn = pm.isScreenOn();
            //}
            boolean keyguardShowing = false;

            if (event._eventPreferencesScreen._whenUnlocked)
            {
                KeyguardManager kgMgr = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
                keyguardShowing = kgMgr.isKeyguardLocked();
            }

            if (event._eventPreferencesScreen._eventType == EventPreferencesScreen.ETYPE_SCREENON)
            {
                if (event._eventPreferencesScreen._whenUnlocked)
                    // passed if screen is on and unlocked => start only when unlocked
                    screenPassed = isScreenOn && (!keyguardShowing);
                else
                    screenPassed = isScreenOn;
            }
            else
            {
                if (event._eventPreferencesScreen._whenUnlocked)
                    // passed if screen is off or locked => locked is the same as screen off
                    screenPassed = (!isScreenOn) || keyguardShowing;
                else
                    screenPassed = !isScreenOn;
            }

            //eventStart = eventStart && screenPassed;
        }


        if (event._eventPreferencesBluetooth._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesBluetooth.PREF_EVENT_BLUETOOTH_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED)
                && Permissions.checkEventLocation(context, event, null))
        {
            bluetoothPassed = false;

            List<BluetoothDeviceData> boundedDevicesList = BluetoothScanJob.getBoundedDevicesList(context);

            boolean done = false;

            BluetoothAdapter bluetooth = BluetoothScanJob.getBluetoothAdapter(context);
            if (bluetooth != null) {
                boolean isBluetoothEnabled = bluetooth.isEnabled();

                if (isBluetoothEnabled) {
                    PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "bluetoothEnabled=true");

                    PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "-- eventAdapterName=" + event._eventPreferencesBluetooth._adapterName);


                    //List<BluetoothDeviceData> connectedDevices = BluetoothConnectedDevices.getConnectedDevices(context);
                    BluetoothConnectionBroadcastReceiver.getConnectedDevices(context);

                    if (BluetoothConnectionBroadcastReceiver.isBluetoothConnected(null, "")) {
                    //if (BluetoothConnectedDevices.isBluetoothConnected(connectedDevices,null, "")) {

                        PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "any device connected");

                        boolean connected = false;
                        String[] splits = event._eventPreferencesBluetooth._adapterName.split("\\|");
                        for (String _bluetoothName : splits) {
                            if (_bluetoothName.equals(EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE)) {
                                PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "any device connected");
                                connected = true;
                                break;
                            } else if (_bluetoothName.equals(EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE)) {
                                for (BluetoothDeviceData data : boundedDevicesList) {
                                    PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "boundedDevice.name="+data.getName());
                                    PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "boundedDevice.address="+data.getAddress());
                                    connected = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(data, "");
                                    //connected = BluetoothConnectedDevices.isBluetoothConnected(connectedDevices, data, "");
                                    if (connected)
                                        break;
                                }
                                PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "paired device connected="+connected);
                            } else {
                                connected = BluetoothConnectionBroadcastReceiver.isBluetoothConnected(null, _bluetoothName);
                                //connected = BluetoothConnectedDevices.isBluetoothConnected(connectedDevices,null, _bluetoothName);
                                PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "event sensor device connected="+connected);
                            }
                            if (connected)
                                break;
                        }

                        if (connected) {
                            // event BT adapter is connected
                            done = true;

                            bluetoothPassed = !((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTCONNECTED) ||
                                    (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTINFRONT));
                        } else {
                            if (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTCONNECTED) {
                                // for this connectionTypes, BT must not be connected to event BT adapter
                                done = true;
                                bluetoothPassed = true;
                            }
                        }
                    } else {
                        PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "not any device connected");

                        if (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTCONNECTED) {
                            // for this connectionTypes, BT must not be connected to event BT adapter
                            done = true;
                            bluetoothPassed = true;
                        }
                    }
                } else {
                    PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "bluetoothEnabled=true");

                    if ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_CONNECTED) ||
                            (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTCONNECTED)) {
                        // for this connectionTypes, BT must not be connected to event BT adapter
                        done = true;
                        bluetoothPassed = (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTCONNECTED);
                    }
                }
            }

            PPApplication.logE("[BTScan] DataWrapper.doHandleEvents","bluetoothPassed="+bluetoothPassed);

            if ((event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_INFRONT) ||
                (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTINFRONT))
            {
                if (!done) {
                    if (!ApplicationPreferences.applicationEventBluetoothEnableScannig(context)) {
                        // ignore for disabled scanning
                        ignoreBluetooth = true;
                    }
                    else {
                        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                        if (!pm.isScreenOn() && ApplicationPreferences.applicationEventBluetoothScanOnlyWhenScreenIsOn(context)) {
                            // ignore for screen Off
                            ignoreBluetooth = true;
                        } else {
                            bluetoothPassed = false;

                            List<BluetoothDeviceData> scanResults = BluetoothScanJob.getScanResults(context);

                            if (scanResults != null) {
                                //PPApplication.logE("@@@ DataWrapper.doHandleEvents","-- eventAdapterName="+event._eventPreferencesBluetooth._adapterName);

                                for (BluetoothDeviceData device : scanResults) {
                                    String[] splits = event._eventPreferencesBluetooth._adapterName.split("\\|");
                                    for (String _bluetoothName : splits) {
                                        if (_bluetoothName.equals(EventPreferencesBluetooth.ALL_BLUETOOTH_NAMES_VALUE)) {
                                            bluetoothPassed = true;
                                            break;
                                        } else if (_bluetoothName.equals(EventPreferencesBluetooth.CONFIGURED_BLUETOOTH_NAMES_VALUE)) {
                                            for (BluetoothDeviceData data : boundedDevicesList) {
                                                String _device = device.getName().toUpperCase();
                                                String _adapterName = data.getName().toUpperCase();
                                                if (Wildcard.match(_device, _adapterName, '_', '%', true)) {
                                                    PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "bluetooth found");
                                                    //PPApplication.logE("@@@ DataWrapper.doHandleEvents","bluetoothAdapterName="+device.getName());
                                                    //PPApplication.logE("@@@ DataWrapper.doHandleEvents","bluetoothAddress="+device.getAddress());
                                                    bluetoothPassed = true;
                                                    break;
                                                }
                                            }
                                            if (bluetoothPassed)
                                                break;
                                        } else {
                                            String _device = device.getName().toUpperCase();
                                            if ((device.getName() == null) || device.getName().isEmpty()) {
                                                // scanned device has not name (hidden BT?)
                                                if ((device.getAddress() != null) && (!device.getAddress().isEmpty())) {
                                                    // device has address
                                                    for (BluetoothDeviceData data : boundedDevicesList) {
                                                        if ((data.getAddress() != null) && data.getAddress().equals(device.getAddress())) {
                                                            PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "bluetooth found");
                                                            //PPApplication.logE("@@@ DataWrapper.doHandleEvents","bluetoothAdapterName="+device.getName());
                                                            //PPApplication.logE("@@@ DataWrapper.doHandleEvents","bluetoothAddress="+device.getAddress());
                                                            bluetoothPassed = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            } else {
                                                String _adapterName = _bluetoothName.toUpperCase();
                                                if (Wildcard.match(_device, _adapterName, '_', '%', true)) {
                                                    PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "bluetooth found");
                                                    //PPApplication.logE("@@@ DataWrapper.doHandleEvents","bluetoothAdapterName="+device.getName());
                                                    //PPApplication.logE("@@@ DataWrapper.doHandleEvents","bluetoothAddress="+device.getAddress());
                                                    bluetoothPassed = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (bluetoothPassed)
                                        break;
                                }

                                if (!bluetoothPassed)
                                    PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "bluetooth not found");

                                if (event._eventPreferencesBluetooth._connectionType == EventPreferencesBluetooth.CTYPE_NOTINFRONT)
                                    // if bluetooth is not in front of event BT adapter name, then passed
                                    bluetoothPassed = !bluetoothPassed;
                            } else
                                PPApplication.logE("[BTScan] DataWrapper.doHandleEvents", "scanResults == null");

                        }
                    }
                }
            }

            PPApplication.logE("[BTScan] DataWrapper.doHandleEvents","bluetoothPassed="+bluetoothPassed);

            //eventStart = eventStart && bluetoothPassed;
        }

        if ((event._eventPreferencesSMS._enabled) &&
                (Event.isEventPreferenceAllowed(EventPreferencesSMS.PREF_EVENT_SMS_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED)
                && Permissions.checkEventSMSContacts(context, event, null) &&
                Permissions.checkEventSMSBroadcast(context, event, null))
        {
            // compute start time

            if (event._eventPreferencesSMS._startTime > 0) {
                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                long startTime = event._eventPreferencesSMS._startTime - gmtOffset;

                if (PPApplication.logEnabled()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String alarmTimeS = sdf.format(startTime);
                    PPApplication.logE("DataWrapper.doHandleEvents", "startTime=" + alarmTimeS);
                }

                // compute end datetime
                long endAlarmTime = event._eventPreferencesSMS.computeAlarm();
                if (PPApplication.logEnabled()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String alarmTimeS = sdf.format(endAlarmTime);
                    PPApplication.logE("DataWrapper.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                }

                Calendar now = Calendar.getInstance();
                long nowAlarmTime = now.getTimeInMillis();
                if (PPApplication.logEnabled()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String alarmTimeS = sdf.format(nowAlarmTime);
                    PPApplication.logE("DataWrapper.doHandleEvents", "nowAlarmTime=" + alarmTimeS);
                }

                if (sensorType.equals(EventsHandler.SENSOR_TYPE_SMS))
                    smsPassed = true;
                else if (!event._eventPreferencesSMS._permanentRun) {
                    if (sensorType.equals(EventsHandler.SENSOR_TYPE_SMS_EVENT_END))
                        smsPassed = false;
                    else
                        smsPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                }
                else {
                    smsPassed = nowAlarmTime >= startTime;
                }
            }
            else
                smsPassed = false;

            if (!smsPassed) {
                event._eventPreferencesSMS._startTime = 0;
                DatabaseHandler.getInstance(context).updateSMSStartTime(event);
            }
        }

        if (event._eventPreferencesNotification._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesNotification.PREF_EVENT_NOTIFICATION_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                /*if (!event._eventPreferencesNotification._endWhenRemoved) {

                    if (event._eventPreferencesNotification._startTime > 0) {
                        // compute start time
                        int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                        long startTime = event._eventPreferencesNotification._startTime - gmtOffset;

                        SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                        String alarmTimeS = sdf.format(startTime);
                        PPApplication.logE("[NOTIF] DataWrapper.doHandleEvents", "startTime=" + alarmTimeS);

                        // compute end datetime
                        long endAlarmTime = event._eventPreferencesNotification.computeAlarm();
                        alarmTimeS = sdf.format(endAlarmTime);
                        PPApplication.logE("[NOTIF] DataWrapper.doHandleEvents", "endAlarmTime=" + alarmTimeS);

                        Calendar now = Calendar.getInstance();
                        long nowAlarmTime = now.getTimeInMillis();
                        alarmTimeS = sdf.format(nowAlarmTime);
                        PPApplication.logE("[NOTIF] DataWrapper.doHandleEvents", "nowAlarmTime=" + alarmTimeS);

                        if (sensorType.equals(EventsHandler.SENSOR_TYPE_NOTIFICATION))
                            notificationPassed = true;
                        else if (!event._eventPreferencesNotification._permanentRun) {
                            if (sensorType.equals(EventsHandler.SENSOR_TYPE_NOTIFICATION_EVENT_END)) {
                                notificationPassed = false;
                            }
                            else
                                notificationPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                        }
                        else
                            notificationPassed = nowAlarmTime >= startTime;
                    } else
                        notificationPassed = false;
                } else {*/
                    notificationPassed = event._eventPreferencesNotification.isNotificationVisible(this);
                //}

                PPApplication.logE("[NOTIF] DataWrapper.doHandleEvents", "notificationPassed=" + notificationPassed);

                /*if (!notificationPassed) {
                    event._eventPreferencesNotification._startTime = 0;
                    DatabaseHandler.getInstance(context).updateNotificationStartTime(event);
                }*/
            /*}
            else {
                ignoreNotification = true;
            }*/
        }


        if (event._eventPreferencesApplication._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesApplication.PREF_EVENT_APPLICATION_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            applicationPassed = false;

            if (AccessibilityServiceBroadcastReceiver.isEnabled(context.getApplicationContext())) {
                String foregroundApplication = AccessibilityServiceBroadcastReceiver.getApplicationInForeground(context);

                if (!foregroundApplication.isEmpty()) {
                    String[] splits = event._eventPreferencesApplication._applications.split("\\|");
                    for (String split : splits) {
                        String packageName = ApplicationsCache.getPackageName(split);

                        if (foregroundApplication.equals(packageName)) {
                            applicationPassed = true;
                            break;
                        }
                    }
                } else
                    ignoreApplication = true;
            }
            else
                ignoreApplication = true;
        }

        if (event._eventPreferencesLocation._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesLocation.PREF_EVENT_LOCATION_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED)
                && Permissions.checkEventLocation(context, event, null))
        {
            if (!ApplicationPreferences.applicationEventLocationEnableScannig(context)) {
                // ignore for disabled location scanner
                PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "ignore for disabled scanner");
                ignoreLocation = true;
            }
            else{
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (!pm.isScreenOn() && ApplicationPreferences.applicationEventLocationScanOnlyWhenScreenIsOn(context)) {
                    // ignore for screen Off
                    PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "ignore for screen off");
                    ignoreLocation = true;
                } else {
                    synchronized (PPApplication.geofenceScannerMutex) {
                        if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isGeofenceScannerStarted() &&
                                PhoneProfilesService.getGeofencesScanner().mTransitionsUpdated) {
                            locationPassed = false;

                            String[] splits = event._eventPreferencesLocation._geofences.split("\\|");
                            PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "--------");
                            PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "_eventPreferencesLocation._geofences=" + event._eventPreferencesLocation._geofences);
                            for (String _geofence : splits) {
                                if (!_geofence.isEmpty()) {
                                    PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "geofence=" + DatabaseHandler.getInstance(context).getGeofenceName(Long.valueOf(_geofence)));

                                    int geofenceTransition = DatabaseHandler.getInstance(context).getGeofenceTransition(Long.valueOf(_geofence));
                                    if (geofenceTransition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER)
                                        PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "transitionType=GEOFENCE_TRANSITION_ENTER");
                                    else
                                        PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "transitionType=GEOFENCE_TRANSITION_EXIT");

                                    if (geofenceTransition == com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER) {
                                        locationPassed = true;
                                        break;
                                    }
                                }
                            }
                            PPApplication.logE("[GeoSensor] DataWrapper.doHandleEvents", "locationPassed=" + locationPassed);

                            if (event._eventPreferencesLocation._whenOutside)
                                locationPassed = !locationPassed;
                        } else {
                            ignoreLocation = true;
                        }
                    }
                }
            }
        }

        if (event._eventPreferencesOrientation._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesOrientation.PREF_EVENT_ORIENTATION_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            ApplicationPreferences.getSharedPreferences(context);
            int callEventType = ApplicationPreferences.preferences.getInt(PhoneCallBroadcastReceiver.PREF_EVENT_CALL_EVENT_TYPE, PhoneCallBroadcastReceiver.CALL_EVENT_UNDEFINED);

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (/*Permissions.checkEventPhoneBroadcast(context, event) &&*/
                (callEventType != PhoneCallBroadcastReceiver.CALL_EVENT_UNDEFINED) &&
                (callEventType != PhoneCallBroadcastReceiver.CALL_EVENT_INCOMING_CALL_ENDED) &&
                (callEventType != PhoneCallBroadcastReceiver.CALL_EVENT_OUTGOING_CALL_ENDED)) {
                // ignore changes during call
                ignoreOrientation = true;
            }
            else
            if (!ApplicationPreferences.applicationEventOrientationEnableScannig(context)) {
                ignoreOrientation = true;
            }
            else
            if (!pm.isScreenOn() && ApplicationPreferences.applicationEventOrientationScanOnlyWhenScreenIsOn(context)) {
                    // ignore for screen Off
                    ignoreOrientation = true;
            }
            else
            {
                synchronized (PPApplication.orientationScannerMutex) {
                    if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isOrientationScannerStarted()) {
                        boolean lApplicationPassed = false;
                        if (AccessibilityServiceBroadcastReceiver.isEnabled(context.getApplicationContext())) {
                            String foregroundApplication = AccessibilityServiceBroadcastReceiver.getApplicationInForeground(context);
                            if (!foregroundApplication.isEmpty()) {
                                String[] splits = event._eventPreferencesOrientation._ignoredApplications.split("\\|");
                                for (String split : splits) {
                                    String packageName = ApplicationsCache.getPackageName(split);

                                    if (foregroundApplication.equals(packageName)) {
                                        lApplicationPassed = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!lApplicationPassed) {

                            boolean lDisplayPassed = true;
                            boolean lSidePassed = true;

                            boolean enabledAccelerometer = PhoneProfilesService.getAccelerometerSensor(context) != null;
                            boolean enabledMagneticField = PhoneProfilesService.getMagneticFieldSensor(context) != null;
                            boolean enabledAll = (enabledAccelerometer) && (enabledMagneticField);
                            if (enabledAccelerometer) {
                                if (!event._eventPreferencesOrientation._display.isEmpty()) {
                                    String[] splits = event._eventPreferencesOrientation._display.split("\\|");
                                    if (splits.length > 0) {
                                        lDisplayPassed = false;
                                        for (String split : splits) {
                                            try {
                                                int side = Integer.valueOf(split);
                                                if (side == PhoneProfilesService.mDisplayUp) {
                                                    lDisplayPassed = true;
                                                    break;
                                                }
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }
                                }
                            }

                            if (enabledAll) {
                                if (!event._eventPreferencesOrientation._sides.isEmpty()) {
                                    String[] splits = event._eventPreferencesOrientation._sides.split("\\|");
                                    if (splits.length > 0) {
                                        lSidePassed = false;
                                        for (String split : splits) {
                                            try {
                                                int side = Integer.valueOf(split);
                                                if (side == PhoneProfilesService.DEVICE_ORIENTATION_HORIZONTAL) {
                                                    if (PhoneProfilesService.mSideUp == PhoneProfilesService.mDisplayUp) {
                                                        lSidePassed = true;
                                                        break;
                                                    }
                                                } else {
                                                    if (side == PhoneProfilesService.mSideUp) {
                                                        lSidePassed = true;
                                                        break;
                                                    }
                                                }
                                            } catch (Exception ignored) {
                                            }
                                        }
                                    }
                                }
                            }

                            boolean lDistancePassed = true;
                            boolean enabled = PhoneProfilesService.getProximitySensor(context) != null;
                            if (enabled) {
                                if (event._eventPreferencesOrientation._distance != 0) {
                                    lDistancePassed = event._eventPreferencesOrientation._distance == PhoneProfilesService.mDeviceDistance;
                                }
                            }

                            //Log.d("**** DataWrapper.doHandleEvents","lDisplayPassed="+lDisplayPassed);
                            //Log.d("**** DataWrapper.doHandleEvents","lSidePassed="+lSidePassed);
                            //Log.d("**** DataWrapper.doHandleEvents","lDistancePassed="+lDistancePassed);


                            orientationPassed = lDisplayPassed && lSidePassed && lDistancePassed;
                        } else {
                            ignoreOrientation = true;
                        }
                    } else {
                        ignoreOrientation = true;
                    }
                }
            }
        }

        if (event._eventPreferencesMobileCells._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesMobileCells.PREF_EVENT_MOBILE_CELLS_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED)
                && Permissions.checkEventLocation(context, event, null))
        {
            if (!ApplicationPreferences.applicationEventMobileCellEnableScannig(context)) {
                ignoreMobileCell = true;
            }
            else {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                if (!pm.isScreenOn() && ApplicationPreferences.applicationEventMobileCellScanOnlyWhenScreenIsOn(context)) {
                    // ignore for screen Off
                    ignoreMobileCell = true;
                } else {
                    synchronized (PPApplication.phoneStateScannerMutex) {
                        if ((PhoneProfilesService.instance != null) && PhoneProfilesService.isPhoneStateScannerStarted()) {
                            String[] splits = event._eventPreferencesMobileCells._cells.split("\\|");
                            if (PhoneProfilesService.phoneStateScanner.registeredCell != Integer.MAX_VALUE) {
                                String registeredCell = Integer.toString(PhoneProfilesService.phoneStateScanner.registeredCell);
                                boolean found = false;
                                for (String cell : splits) {
                                    if (cell.equals(registeredCell)) {
                                        found = true;
                                        break;
                                    }
                                }
                                mobileCellPassed = found;
                            } else
                                ignoreMobileCell = true;

                            if (event._eventPreferencesMobileCells._whenOutside)
                                mobileCellPassed = !mobileCellPassed;
                        } else
                            ignoreMobileCell = true;
                    }
                }
            }
        }

        if (event._eventPreferencesNFC._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesNFC.PREF_EVENT_NFC_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            // compute start time

            if (event._eventPreferencesNFC._startTime > 0) {
                int gmtOffset = 0; //TimeZone.getDefault().getRawOffset();
                long startTime = event._eventPreferencesNFC._startTime - gmtOffset;

                if (PPApplication.logEnabled()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String alarmTimeS = sdf.format(startTime);
                    PPApplication.logE("DataWrapper.doHandleEvents", "startTime=" + alarmTimeS);
                }

                // compute end datetime
                long endAlarmTime = event._eventPreferencesNFC.computeAlarm();
                if (PPApplication.logEnabled()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String alarmTimeS = sdf.format(endAlarmTime);
                    PPApplication.logE("DataWrapper.doHandleEvents", "endAlarmTime=" + alarmTimeS);
                }

                Calendar now = Calendar.getInstance();
                long nowAlarmTime = now.getTimeInMillis();
                if (PPApplication.logEnabled()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("EE d.MM.yyyy HH:mm:ss:S");
                    String alarmTimeS = sdf.format(nowAlarmTime);
                    PPApplication.logE("DataWrapper.doHandleEvents", "nowAlarmTime=" + alarmTimeS);
                }

                if (sensorType.equals(EventsHandler.SENSOR_TYPE_NFC_TAG))
                    nfcPassed = true;
                else if (!event._eventPreferencesNFC._permanentRun) {
                    if (sensorType.equals(EventsHandler.SENSOR_TYPE_NFC_EVENT_END))
                        nfcPassed = false;
                    else
                        nfcPassed = ((nowAlarmTime >= startTime) && (nowAlarmTime < endAlarmTime));
                }
                else
                    nfcPassed = nowAlarmTime >= startTime;
            }
            else
                nfcPassed = false;

            if (!nfcPassed) {
                event._eventPreferencesNFC._startTime = 0;
                DatabaseHandler.getInstance(context).updateNFCStartTime(event);
            }
        }

        if (event._eventPreferencesRadioSwitch._enabled &&
                (Event.isEventPreferenceAllowed(EventPreferencesRadioSwitch.PREF_EVENT_RADIO_SWITCH_ENABLED, context) == PPApplication.PREFERENCE_ALLOWED))
        {
            radioSwitchPassed = true;
            boolean tested = false;

            if ((event._eventPreferencesRadioSwitch._wifi == 1 || event._eventPreferencesRadioSwitch._wifi == 2)
                    && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_WIFI)) {

                if (!((WifiScanJob.getScanRequest(context)) ||
                        (WifiScanJob.getWaitForResults(context)) ||
                        (WifiScanJob.getWifiEnabledForScan(context)))) {
                    // ignore for wifi scanning

                    WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    int wifiState = wifiManager.getWifiState();
                    boolean enabled = ((wifiState == WifiManager.WIFI_STATE_ENABLED) || (wifiState == WifiManager.WIFI_STATE_ENABLING));
                    PPApplication.logE("-###- DataWrapper.doHandleEvents", "wifiState=" + enabled);
                    tested = true;
                    if (event._eventPreferencesRadioSwitch._wifi == 1)
                        radioSwitchPassed = radioSwitchPassed && enabled;
                    else
                        radioSwitchPassed = radioSwitchPassed && !enabled;
                }
                else
                    ignoreRadioSwitch = true;
            }

            if ((event._eventPreferencesRadioSwitch._bluetooth == 1 || event._eventPreferencesRadioSwitch._bluetooth == 2)
                    && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_BLUETOOTH)) {

                if (!((BluetoothScanJob.getScanRequest(context)) ||
                        (BluetoothScanJob.getLEScanRequest(context)) ||
                        (BluetoothScanJob.getWaitForResults(context)) ||
                        (BluetoothScanJob.getWaitForLEResults(context)) ||
                        (BluetoothScanJob.getBluetoothEnabledForScan(context)))) {
                    // ignore for bluetooth scanning


                    BluetoothAdapter bluetoothAdapter = BluetoothScanJob.getBluetoothAdapter(context);
                    if (bluetoothAdapter != null) {
                        boolean enabled = bluetoothAdapter.isEnabled();
                        PPApplication.logE("-###- DataWrapper.doHandleEvents", "bluetoothState=" + enabled);
                        tested = true;
                        if (event._eventPreferencesRadioSwitch._bluetooth == 1)
                            radioSwitchPassed = radioSwitchPassed && enabled;
                        else
                            radioSwitchPassed = radioSwitchPassed && !enabled;
                    }
                }
                else
                    ignoreRadioSwitch = true;
            }

            if ((event._eventPreferencesRadioSwitch._mobileData == 1 || event._eventPreferencesRadioSwitch._mobileData == 2)
                    && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_TELEPHONY)) {

                boolean enabled = ActivateProfileHelper.isMobileData(context);
                PPApplication.logE("-###- DataWrapper.doHandleEvents", "mobileDataState=" + enabled);
                tested = true;
                if (event._eventPreferencesRadioSwitch._mobileData == 1)
                    radioSwitchPassed = radioSwitchPassed && enabled;
                else
                    radioSwitchPassed = radioSwitchPassed && !enabled;
            }

            if ((event._eventPreferencesRadioSwitch._gps == 1 || event._eventPreferencesRadioSwitch._gps == 2)
                    && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_LOCATION_GPS)) {

                boolean enabled;
                /*if (android.os.Build.VERSION.SDK_INT < 19)
                    enabled = Settings.Secure.isLocationProviderEnabled(context.getContentResolver(), LocationManager.GPS_PROVIDER);
                else {*/
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                //}
                PPApplication.logE("-###- DataWrapper.doHandleEvents", "gpsState=" + enabled);
                tested = true;
                if (event._eventPreferencesRadioSwitch._gps == 1)
                    radioSwitchPassed = radioSwitchPassed && enabled;
                else
                    radioSwitchPassed = radioSwitchPassed && !enabled;
            }

            if ((event._eventPreferencesRadioSwitch._nfc == 1 || event._eventPreferencesRadioSwitch._nfc == 2)
                    && PPApplication.hasSystemFeature(context, PackageManager.FEATURE_NFC)) {

                NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
                if (nfcAdapter != null) {
                    boolean enabled = nfcAdapter.isEnabled();
                    PPApplication.logE("-###- DataWrapper.doHandleEvents", "nfcState=" + enabled);
                    tested = true;
                    if (event._eventPreferencesRadioSwitch._nfc == 1)
                        radioSwitchPassed = radioSwitchPassed && enabled;
                    else
                        radioSwitchPassed = radioSwitchPassed && !enabled;
                }
            }

            if (event._eventPreferencesRadioSwitch._airplaneMode == 1 || event._eventPreferencesRadioSwitch._airplaneMode == 2) {

                boolean enabled = ActivateProfileHelper.isAirplaneMode(context);
                PPApplication.logE("-###- DataWrapper.doHandleEvents", "airplaneModeState=" + enabled);
                tested = true;
                if (event._eventPreferencesRadioSwitch._airplaneMode == 1)
                    radioSwitchPassed = radioSwitchPassed && enabled;
                else
                    radioSwitchPassed = radioSwitchPassed && !enabled;
            }

            radioSwitchPassed = radioSwitchPassed && tested;
        }

        PPApplication.logE("DataWrapper.doHandleEvents","timePassed="+timePassed);
        PPApplication.logE("DataWrapper.doHandleEvents","batteryPassed="+batteryPassed);
        PPApplication.logE("DataWrapper.doHandleEvents","callPassed="+callPassed);
        PPApplication.logE("DataWrapper.doHandleEvents","peripheralPassed="+peripheralPassed);
        PPApplication.logE("DataWrapper.doHandleEvents","calendarPassed="+calendarPassed);
        PPApplication.logE("DataWrapper.doHandleEvents","wifiPassed="+wifiPassed);
        PPApplication.logE("DataWrapper.doHandleEvents","screenPassed="+screenPassed);
        PPApplication.logE("DataWrapper.doHandleEvents","bluetoothPassed="+bluetoothPassed);
        PPApplication.logE("DataWrapper.doHandleEvents","smsPassed="+smsPassed);
        PPApplication.logE("DataWrapper.doHandleEvents","notificationPassed="+notificationPassed);
        PPApplication.logE("DataWrapper.doHandleEvents","applicationPassed="+applicationPassed);
        PPApplication.logE("DataWrapper.doHandleEvents","locationPassed="+locationPassed);
        PPApplication.logE("DataWrapper.doHandleEvents","orientationPassed="+orientationPassed);
        PPApplication.logE("DataWrapper.doHandleEvents","mobileCellPassed="+mobileCellPassed);
        PPApplication.logE("DataWrapper.doHandleEvents","nfcPassed="+nfcPassed);
        PPApplication.logE("DataWrapper.doHandleEvents","radioSwitchPassed="+radioSwitchPassed);

        PPApplication.logE("DataWrapper.doHandleEvents","ignoreTime="+ignoreTime);
        PPApplication.logE("DataWrapper.doHandleEvents","ignoreBattery="+ignoreBattery);
        PPApplication.logE("DataWrapper.doHandleEvents","ignoreCall="+ignoreCall);
        PPApplication.logE("DataWrapper.doHandleEvents","ignorePeripheral="+ignorePeripheral);
        PPApplication.logE("DataWrapper.doHandleEvents","ignoreCalendar="+ignoreCalendar);
        PPApplication.logE("DataWrapper.doHandleEvents","ignoreWifi="+ignoreWifi);
        PPApplication.logE("DataWrapper.doHandleEvents","ignoreScreen="+ignoreScreen);
        PPApplication.logE("DataWrapper.doHandleEvents","ignoreBluetooth="+ignoreBluetooth);
        PPApplication.logE("DataWrapper.doHandleEvents","ignoreSms="+ignoreSms);
        PPApplication.logE("DataWrapper.doHandleEvents","ignoreNotification="+ignoreNotification);
        PPApplication.logE("DataWrapper.doHandleEvents","ignoreApplication="+ignoreApplication);
        PPApplication.logE("DataWrapper.doHandleEvents","ignoreLocation="+ignoreLocation);
        PPApplication.logE("DataWrapper.doHandleEvents","ignoreOrientation="+ignoreOrientation);
        PPApplication.logE("DataWrapper.doHandleEvents","ignoreMobileCell="+ignoreMobileCell);
        PPApplication.logE("DataWrapper.doHandleEvents","ignoreNfc="+ignoreNfc);
        PPApplication.logE("DataWrapper.doHandleEvents","ignoreRadioSwitch="+ignoreRadioSwitch);

        //PPApplication.logE("DataWrapper.doHandleEvents","eventStart="+eventStart);
        PPApplication.logE("DataWrapper.doHandleEvents","restartEvent="+restartEvent);
        PPApplication.logE("DataWrapper.doHandleEvents","statePause="+statePause);

        List<EventTimeline> eventTimelineList = getEventTimelineList();

        if (!(ignoreTime ||
              ignoreBattery ||
              ignoreCall ||
              ignorePeripheral ||
              ignoreCalendar ||
              ignoreWifi ||
              ignoreScreen ||
              ignoreBluetooth ||
              ignoreSms ||
              ignoreNotification ||
              ignoreApplication ||
              ignoreLocation ||
              ignoreOrientation ||
              ignoreMobileCell ||
              ignoreNfc ||
              ignoreRadioSwitch)) {
            // if all sensors are not ignored, do event start/pause

            if (timePassed &&
                batteryPassed &&
                callPassed &&
                peripheralPassed &&
                calendarPassed &&
                wifiPassed &&
                screenPassed &&
                bluetoothPassed &&
                smsPassed &&
                notificationPassed &&
                applicationPassed &&
                locationPassed &&
                orientationPassed &&
                mobileCellPassed &&
                nfcPassed &&
                radioSwitchPassed ) {
                // all sensors are passed

                //if (eventStart)
                newEventStatus = Event.ESTATUS_RUNNING;
                //else
                //    newEventStatus = Event.ESTATUS_PAUSE;

            } else
                newEventStatus = Event.ESTATUS_PAUSE;

            PPApplication.logE("[***] DataWrapper.doHandleEvents", "event.getStatus()=" + event.getStatus());
            PPApplication.logE("[***] DataWrapper.doHandleEvents", "newEventStatus=" + newEventStatus);

            //PPApplication.logE("@@@ DataWrapper.doHandleEvents","restartEvent="+restartEvent);

            if ((event.getStatus() != newEventStatus) || restartEvent || event._isInDelayStart || event._isInDelayEnd) {
                PPApplication.logE("[***] DataWrapper.doHandleEvents", " do new event status");

                if ((newEventStatus == Event.ESTATUS_RUNNING) && (!statePause)) {
                    PPApplication.logE("[***] DataWrapper.doHandleEvents", "start event");
                    PPApplication.logE("[***] DataWrapper.doHandleEvents", "event._name=" + event._name);

                    if (event._isInDelayEnd)
                        event.removeDelayEndAlarm(this);
                    else {
                        if (!forDelayStartAlarm) {
                            // called not for delay alarm
                            /*if (restartEvent) {
                                event._isInDelayStart = false;
                            } else*/ {
                                if (!event._isInDelayStart) {
                                    // if not delay alarm is set, set it
                                    event.setDelayStartAlarm(this); // for start delay
                                }
                                if (event._isInDelayStart) {
                                    // if delay expires, start event
                                    event.checkDelayStart(/*this*/);
                                }
                            }
                            PPApplication.logE("[***] DataWrapper.doHandleEvents", "event._isInDelayStart=" + event._isInDelayStart);
                            if (!event._isInDelayStart) {
                                // no delay alarm is set
                                // start event
                                event.startEvent(this, eventTimelineList, /*interactive,*/ reactivate, mergedProfile);
                                PPApplication.logE("[***] DataWrapper.doHandleEvents", "mergedProfile._id=" + mergedProfile._id);
                            }
                        }

                        if (forDelayStartAlarm && event._isInDelayStart) {
                            // called for delay alarm
                            // start event
                            event.startEvent(this, eventTimelineList, /*interactive,*/ reactivate, mergedProfile);
                        }
                    }
                } else if (((newEventStatus == Event.ESTATUS_PAUSE) || restartEvent) && statePause) {
                    // when pausing and it is for restart events, force pause

                    PPApplication.logE("[***] DataWrapper.doHandleEvents", "pause event");
                    PPApplication.logE("[***] DataWrapper.doHandleEvents", "event._name=" + event._name);

                    if (event._isInDelayStart) {
                        PPApplication.logE("[***] DataWrapper.doHandleEvents", "isInDelayStart");
                        event.removeDelayStartAlarm(this);
                    }
                    else {
                        if (!forDelayEndAlarm) {
                            PPApplication.logE("[***] DataWrapper.doHandleEvents", "!forDelayEndAlarm");
                            // called not for delay alarm
                            if (restartEvent) {
                                event._isInDelayEnd = false;
                            } else {
                                if (!event._isInDelayEnd) {
                                    // if not delay alarm is set, set it
                                    event.setDelayEndAlarm(this); // for end delay
                                }
                                if (event._isInDelayEnd) {
                                    // if delay expires, pause event
                                    event.checkDelayEnd(/*this*/);
                                }
                            }
                            if (!event._isInDelayEnd) {
                                // no delay alarm is set
                                // pause event
                                event.pauseEvent(this, eventTimelineList, true, false,
                                        false, true, mergedProfile, !restartEvent);
                            }
                        }

                        if (forDelayEndAlarm && event._isInDelayEnd) {
                            // called for delay alarm
                            // pause event
                            event.pauseEvent(this, eventTimelineList, true, false,
                                    false, true, mergedProfile, !restartEvent);
                        }
                    }
                }
            }
        }

        PPApplication.logE("%%% DataWrapper.doHandleEvents","--- end --------------------------");
    }

    void restartEvents(boolean unblockEventsRun, boolean keepActivatedProfile/*, final boolean interactive*/)
    {
        if (!Event.getGlobalEventsRunning(context))
            // events are globally stopped
            return;

        PPApplication.logE("$$$ restartEvents", "in DataWrapper.restartEvents");

        if (Event.getEventsBlocked(context) && (!unblockEventsRun)) {
            //EventsHandlerJob.startForSensor(context, EventsHandler.SENSOR_TYPE_START_EVENTS_SERVICE);
            final Context appContext = context.getApplicationContext();
            PPApplication.startHandlerThread();
            final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                    PowerManager.WakeLock wakeLock = null;
                    if (powerManager != null) {
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DataWrapper.restartEvents.1");
                        wakeLock.acquire(10 * 60 * 1000);
                    }

                    EventsHandler eventsHandler = new EventsHandler(appContext);
                    eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_START_EVENTS_SERVICE/*, false*/);

                    if ((wakeLock != null) && wakeLock.isHeld())
                        wakeLock.release();
                }
            });
            return;
        }

        PPApplication.logE("DataWrapper.restartEvents", "events are not blocked");

        //Profile activatedProfile = getActivatedProfile();

        if (unblockEventsRun)
        {
            // remove alarm for profile duration
            ProfileDurationAlarmBroadcastReceiver.removeAlarm(context);
            Profile.setActivatedProfileForDuration(context, 0);

            Event.setEventsBlocked(context, false);
            synchronized (eventList) {
                fillEventList();
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    if (event != null)
                        event._blocked = false;
                }
            }
            DatabaseHandler.getInstance(context).unblockAllEvents();
            Event.setForceRunEventRunning(context, false);
        }

        if (!keepActivatedProfile) {
            DatabaseHandler.getInstance(context).deactivateProfile();
            setProfileActive(null);
        }

        //EventsHandlerJob.startForRestartEvents(context, interactive);
        final Context appContext = context.getApplicationContext();
        PPApplication.startHandlerThread();
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                PowerManager powerManager = (PowerManager) appContext.getSystemService(POWER_SERVICE);
                PowerManager.WakeLock wakeLock = null;
                if (powerManager != null) {
                    wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DataWrapper.restartEvents.2");
                    wakeLock.acquire(10 * 60 * 1000);
                }

                EventsHandler eventsHandler = new EventsHandler(appContext);
                eventsHandler.handleEvents(EventsHandler.SENSOR_TYPE_RESTART_EVENTS/*, interactive*/);

                if ((wakeLock != null) && wakeLock.isHeld())
                    wakeLock.release();
            }
        });
    }

    void restartEventsWithRescan(/*boolean showToast, boolean interactive*/)
    {
        PPApplication.logE("$$$ DataWrapper.restartEventsWithRescan","xxx");

        // remove all event delay alarms
        resetAllEventsInDelayStart(false);
        resetAllEventsInDelayEnd(false);
        // ignore manual profile activation
        // and unblock forceRun events
        restartEvents(true, true/*, true/*interactive*/);

        if (ApplicationPreferences.applicationEventWifiRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS))
        {
            PPApplication.restartWifiScanner(context, false);
        }
        if (ApplicationPreferences.applicationEventBluetoothRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS))
        {
            PPApplication.restartBluetoothScanner(context, false);
        }
        if (ApplicationPreferences.applicationEventLocationRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS))
        {
            PPApplication.restartGeofenceScanner(context, false);
        }
        if (ApplicationPreferences.applicationEventMobileCellsRescan(context).equals(PPApplication.RESCAN_TYPE_SCREEN_ON_RESTART_EVENTS))
        {
            PPApplication.restartPhoneStateScanner(context, false);
        }


        //if (showToast)
        //{
            Toast msg = Toast.makeText(context,
                    context.getResources().getString(R.string.toast_events_restarted),
                    Toast.LENGTH_SHORT);
            msg.show();
        //}
    }

    void restartEventsWithAlert(Activity activity)
    {
        if (!Event.getGlobalEventsRunning(context))
            // events are globally stopped
            return;

        /*
        if (!PPApplication.getEventsBlocked(context))
            return;
        */

        PPApplication.logE("DataWrapper.restartEventsWithAlert", "xxx");

        if (ApplicationPreferences.applicationRestartEventsWithAlert(context) || (activity instanceof EditorProfilesActivity))
        {
            final Activity _activity = activity;

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activity);
            dialogBuilder.setTitle(R.string.restart_events_alert_title);
            dialogBuilder.setMessage(R.string.restart_events_alert_message);
            //dialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
            dialogBuilder.setPositiveButton(R.string.alert_button_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    PPApplication.logE("DataWrapper.restartEventsWithAlert", "restart");

                    boolean finish;
                    if (_activity instanceof ActivateProfileActivity)
                        finish = ApplicationPreferences.applicationClose(context);
                    else
                    //noinspection RedundantIfStatement
                    if (_activity instanceof RestartEventsFromNotificationActivity)
                        finish = true;
                    else
                        finish = false;
                    if (finish)
                        _activity.finish();

                    restartEventsWithRescan();

                    /*final Handler handler = new Handler(context.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            restartEventsWithRescan(true, true);
                        }
                    }, 3000);*/
                }
            });
            dialogBuilder.setNegativeButton(R.string.alert_button_no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    boolean finish = (!(_activity instanceof ActivateProfileActivity)) &&
                                     (!(_activity instanceof EditorProfilesActivity));

                    if (finish)
                        _activity.finish();
                }
            });
            dialogBuilder.show();
        }
        else
        {
            PPApplication.logE("DataWrapper.restartEventsWithAlert", "restart");

            boolean finish;
            if (activity instanceof ActivateProfileActivity)
                finish = ApplicationPreferences.applicationClose(context);
            else
            //noinspection RedundantIfStatement
            if (activity instanceof RestartEventsFromNotificationActivity) {
                finish = true;
            }
            else
                finish = false;
            PPApplication.logE("DataWrapper.restartEventsWithAlert", "finish="+finish);
            if (finish)
                activity.finish();

            restartEventsWithRescan();

            /*final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    restartEventsWithRescan(true, true);
                }
            }, 3000);*/
        }
    }

    @SuppressLint("NewApi")
    // delay is in seconds, max 5
    void restartEventsWithDelay(int delay, boolean unblockEventsRun/*, boolean interactive*/)
    {
        PPApplication.logE("DataWrapper.restartEventsWithDelay","xxx");

        final boolean _unblockEventsRun = unblockEventsRun;
        //final boolean _interactive = false/*interactive*/;

        PPApplication.startHandlerThread();
        final Handler handler = new Handler(PPApplication.handlerThread.getLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                PPApplication.logE("DataWrapper.restartEventsWithDelay","restart");
                restartEvents(_unblockEventsRun, true/*, _interactive*/);
            }
        }, delay * 1000);
    }

    void setEventBlocked(Event event, boolean blocked)
    {
        event._blocked = blocked;
        DatabaseHandler.getInstance(context).updateEventBlocked(event);
    }

    // returns true if:
    // 1. events are blocked = any profile is activated manually
    // 2. no any forceRun event is running
    boolean getIsManualProfileActivation()
    {
        if (!Event.getEventsBlocked(context))
            return false;
        else
            return !Event.getForceRunEventRunning(context);
    }

    static private String getProfileNameWithManualIndicator(Profile profile, List<EventTimeline> eventTimelineList,
                                                            boolean addIndicators, boolean addDuration, boolean multiLine,
                                                            DataWrapper dataWrapper)
    {
        if (profile == null)
            return "";

        String name;
        if (addDuration)
            name = profile.getProfileNameWithDuration(multiLine, dataWrapper.context);
        else
            name = profile._name;

        if (Event.getEventsBlocked(dataWrapper.context))
        {
            if (addIndicators)
            {
                if (Event.getForceRunEventRunning(dataWrapper.context))
                {
                    name = "[\u00BB] " + name;
                }
                else
                {
                    name = "[M] " + name;
                }
            }
        }

        if (addIndicators)
        {
            String eventName = getLastStartedEventName(eventTimelineList, dataWrapper);
            if (!eventName.isEmpty())
                name = name + " [" + eventName + "]";
        }

        return name;
    }

    static String getProfileNameWithManualIndicator(Profile profile, boolean addIndicators, boolean addDuration, boolean multiLine,
                                                    DataWrapper dataWrapper) {
        List<EventTimeline> eventTimelineList = dataWrapper.getEventTimelineList();

        return getProfileNameWithManualIndicator(profile, eventTimelineList, addIndicators, addDuration, multiLine, dataWrapper);
    }

    static private String getLastStartedEventName(List<EventTimeline> eventTimelineList, DataWrapper dataWrapper)
    {

        if (Event.getGlobalEventsRunning(dataWrapper.context) && PPApplication.getApplicationStarted(dataWrapper.context, false))
        {
            if (eventTimelineList.size() > 0)
            {
                EventTimeline eventTimeLine = eventTimelineList.get(eventTimelineList.size()-1);
                long event_id = eventTimeLine._fkEvent;
                Event event = dataWrapper.getEventById(event_id);
                if (event != null)
                {
                    if ((!Event.getEventsBlocked(dataWrapper.context)) || (event._forceRun))
                    {
                        Profile profile = dataWrapper.getActivatedProfile(false, false);
                        if ((profile != null) && (event._fkProfileStart == profile._id))
                            // last started event activates activated profile
                            return event._name;
                        else
                            return "";
                    }
                    else
                        return "";
                }
                else
                    return "";
            }
            else
            {
                long profileId = Long.valueOf(ApplicationPreferences.applicationBackgroundProfile(dataWrapper.context));
                if ((!Event.getEventsBlocked(dataWrapper.context)) && (profileId != Profile.PROFILE_NO_ACTIVATE))
                {
                    Profile profile = dataWrapper.getActivatedProfile(false, false);
                    if ((profile != null) && (profile._id == profileId))
                        return dataWrapper.context.getString(R.string.event_name_background_profile);
                    else
                        return "";
                }
                else
                    return "";
            }
        }
        else
            return "";
    }

    private void resetAllEventsInDelayStart(boolean onlyFromDb)
    {
        if (!onlyFromDb) {
            synchronized (eventList) {
                fillEventList();
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    event.removeDelayStartAlarm(this);
                    event.removeDelayStartAlarm(this);
                }
            }
        }
        DatabaseHandler.getInstance(context).resetAllEventsInDelayStart();
    }

    private void resetAllEventsInDelayEnd(boolean onlyFromDb)
    {
        if (!onlyFromDb) {
            synchronized (eventList) {
                fillEventList();
                //noinspection ForLoopReplaceableByForEach
                for (Iterator<Event> it = eventList.iterator(); it.hasNext(); ) {
                    Event event = it.next();
                    event.removeDelayEndAlarm(this);
                    event.removeDelayEndAlarm(this);
                }
            }
        }
        DatabaseHandler.getInstance(context).resetAllEventsInDelayStart();
    }

    public void addActivityLog(int logType, String eventName, String profileName, String profileIcon,
                               int durationDelay) {
        if (PPApplication.getActivityLogEnabled(context)) {
            //if (ApplicationPreferences.preferences == null)
            //    ApplicationPreferences.preferences = context.getSharedPreferences(PPApplication.APPLICATION_PREFS_NAME, Context.MODE_PRIVATE);
            //ApplicationPreferences.setApplicationDeleteOldActivityLogs(context, Integer.valueOf(preferences.getString(ApplicationPreferences.PREF_APPLICATION_DELETE_OLD_ACTIVITY_LOGS, "7")));
            DatabaseHandler.getInstance(context).addActivityLog(ApplicationPreferences.applicationDeleteOldActivityLogs(context),
                                    logType, eventName, profileName, profileIcon, durationDelay);
        }
    }

    void runStopEvents() {
        if (Event.getGlobalEventsRunning(context))
        {
            //noinspection ConstantConditions
            addActivityLog(DatabaseHandler.ALTYPE_RUNEVENTS_DISABLE, null, null, null, 0);

            // no setup for next start
            resetAllEventsInDelayStart(false);
            resetAllEventsInDelayEnd(false);
            // no set system events, unblock all events, no activate return profile
            pauseAllEventsFromMainThread(true, false/*, false*/);
            Event.setGlobalEventsRunning(context, false);

            Intent serviceIntent = new Intent(context, PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_UNREGISTER_RECEIVERS_AND_JOBS, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            PPApplication.startPPService(context, serviceIntent);
        }
        else
        {
            //noinspection ConstantConditions
            addActivityLog(DatabaseHandler.ALTYPE_RUNEVENTS_ENABLE, null, null, null, 0);

            Event.setGlobalEventsRunning(context, true);

            Intent serviceIntent = new Intent(context, PhoneProfilesService.class);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_REGISTER_RECEIVERS_AND_JOBS, true);
            serviceIntent.putExtra(PhoneProfilesService.EXTRA_ONLY_START, false);
            PPApplication.startPPService(context, serviceIntent);

            // setup for next start
            firstStartEvents(false);
        }
    }

    static boolean isPowerSaveMode(Context context) {
        boolean isCharging = false;
        int batteryPct = -100;
        boolean isPowerSaveMode = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null)
                isPowerSaveMode = powerManager.isPowerSaveMode();
        }

        Intent batteryStatus = null;
        try { // Huawei devices: java.lang.IllegalArgumentException: regist too many Broadcast Receivers
            IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            batteryStatus = context.registerReceiver(null, filter);
        } catch (Exception ignored) {}
        if (batteryStatus != null) {
            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            PPApplication.logE("DataWrapper.isPowerSaveMode", "status=" + status);
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                         status == BatteryManager.BATTERY_STATUS_FULL;
            PPApplication.logE("DataWrapper.isPowerSaveMode", "isCharging=" + isCharging);

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            PPApplication.logE("DataWrapper.isPowerSaveMode", "level=" + level);
            PPApplication.logE("DataWrapper.isPowerSaveMode", "scale=" + scale);

            batteryPct = Math.round(level / (float) scale * 100);
            PPApplication.logE("DataWrapper.isPowerSaveMode", "batteryPct=" + batteryPct);
        }

        if (ApplicationPreferences.applicationPowerSaveModeInternal(context).equals("1") && (batteryPct <= 5) && (!isCharging))
            return true;
        if (ApplicationPreferences.applicationPowerSaveModeInternal(context).equals("2") && (batteryPct <= 15) && (!isCharging))
            return true;
        if (ApplicationPreferences.applicationPowerSaveModeInternal(context).equals("3"))
            return isPowerSaveMode;

        return false;
    }

}
