package org.testobject.android.crawler;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import org.testobject.android.crawler.exceptions.SocketServerException;

/**
 * @author Erik Nijkamp
 * @since 28.05.14
 */
public class Main extends IntentService {

    private static String NAME = "crawler";

	public Main() {
		super(NAME);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		Runnable startServer = new Runnable() {
			@Override
			public void run() {
				try {
					startServer();
				} catch (SocketServerException e) {
					Logger.error(e.getMessage());
				}
			}
		};

		new Thread(startServer).start();

		return START_STICKY;
	}

	@Override
	protected void onHandleIntent(Intent intent) {

	}

	private void startServer() throws SocketServerException {
		Context applicationContext = this.getApplicationContext();
		SocketServer server = new SocketServer(4824, applicationContext);
		server.open();
	}
}

