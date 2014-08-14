package org.testobject.android.crawler.commands;

import android.content.Context;
import android.content.Intent;
import org.testobject.android.crawler.Command;

/**
 * @author Erik Nijkamp
 * @since 28.05.14
 */
public class GotoHome implements Command {

	private final Context applicationContext;

	public GotoHome(Context applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public Result call() throws Exception {

		Intent startMain = new Intent(Intent.ACTION_MAIN);
		startMain.addCategory(Intent.CATEGORY_HOME);
		startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		applicationContext.startActivity(startMain);

		return Result.success("ok");
	}

}
