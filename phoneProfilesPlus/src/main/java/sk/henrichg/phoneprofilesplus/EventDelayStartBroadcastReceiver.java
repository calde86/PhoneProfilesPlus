package sk.henrichg.phoneprofilesplus;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class EventDelayStartBroadcastReceiver extends WakefulBroadcastReceiver {

    public static final String BROADCAST_RECEIVER_TYPE = "eventDelayStart";

    @Override
    public void onReceive(Context context, Intent intent) {

        //Thread.setDefaultUncaughtExceptionHandler(new TopExceptionHandler());

        PPApplication.logE("##### EventDelayStartBroadcastReceiver.onReceive", "xxx");

        Context appContext = context.getApplicationContext();

        if (!PPApplication.getApplicationStarted(appContext, true))
            // application is not started
            return;

        //PPApplication.loadPreferences(appContext);

        if (Event.getGlobalEventsRuning(appContext))
        {
            PPApplication.logE("@@@ EventDelayStartBroadcastReceiver.onReceive","xxx");

            // start service
            Intent eventsServiceIntent = new Intent(appContext, EventsService.class);
            eventsServiceIntent.putExtra(EventsService.EXTRA_BROADCAST_RECEIVER_TYPE, BROADCAST_RECEIVER_TYPE);
            startWakefulService(appContext, eventsServiceIntent);
        }

    }

}
