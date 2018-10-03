package net.tanozin.digiary.scheduler;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;

public class OnAlarmReceiver extends BroadcastReceiver {

	private static final String TAG = ComponentInfo.class.getCanonicalName(); 
	
	
	@Override	
	public void onReceive(Context context, Intent intent) {
		long rowid = intent.getExtras().getLong(ScheduleDbAdapter.KEY_ROWID);
		
		WakeReminderIntentService.acquireStaticLock(context);
		
		Intent i = new Intent(context, ScheduleService.class);
		i.putExtra(ScheduleDbAdapter.KEY_ROWID, rowid);
		context.startService(i);
		 
	}
}
