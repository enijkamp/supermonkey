package org.testobject.android.crawler.commands;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import org.testobject.android.crawler.Command;
import static org.testobject.android.crawler.Json.toJson;

import java.util.List;

/**
 * @author Erik Nijkamp
 * @since 28.05.14
 */
public class CurrentActivity implements Command {

	private final Context applicationContext;

	public CurrentActivity(Context applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public Command.Result call() throws Exception {

		ActivityManager am = (ActivityManager) applicationContext.getSystemService(Context.ACTIVITY_SERVICE);

		List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);

		for (ActivityManager.RunningTaskInfo task : tasks) {
			ComponentName topActivity = task.topActivity;
			String response = "{ \"package\":\"" + topActivity.getPackageName() + "\", \"class\":\"" + topActivity.getClassName() + "\" }";
			return Command.Result.success(toJson(response));
		}

		return Command.Result.failure("No tasks running");
	}

}
