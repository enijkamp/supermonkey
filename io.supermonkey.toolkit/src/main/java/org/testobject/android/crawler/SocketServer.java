package org.testobject.android.crawler;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;
import org.testobject.android.crawler.commands.CurrentActivity;
import org.testobject.android.crawler.commands.GotoHome;
import org.testobject.android.crawler.commands.Ping;
import org.testobject.android.crawler.exceptions.SocketServerException;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Erik Nijkamp
 * @since 28.05.14
 */
public class SocketServer implements Closeable {

	private final Map<String, Command> protocol;
	private final ServerSocket server;

	private boolean keepListening = true;

	public SocketServer(final int port, final Context applicationContext) throws SocketServerException {
		try {
			server = new ServerSocket(port);
			protocol = createProtocol(applicationContext);
			Logger.info("Socket opened on port " + port);
		} catch (IOException e) {
			throw new SocketServerException(
					"Could not start socket server listening on " + port);
		}
	}

	public static Map<String, Command> createProtocol(Context applicationContext) {
		Map<String, Command> protocol = new HashMap<String, Command>();
		{
			protocol.put("ping", new Ping());
			protocol.put("current_activity", new CurrentActivity(applicationContext));
			protocol.put("goto_home", new GotoHome(applicationContext));
		}
		return protocol;
	}

	public void open() throws SocketServerException {
		Logger.info("Socket Server Ready");

		while(keepListening) {
			try {
				Socket client = server.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				PrintWriter out = new PrintWriter(client.getOutputStream(), true);
				try {
					Logger.info("Client connected");
					String input;
					while ((input = readString(in)) != null) {
						handleClientData(input, out);
					}
				} finally {
					Logger.info("Client disconnected");
					in.close();
					out.close();
					client.close();
				}
			} catch (IOException e) {
				throw new SocketServerException("Error when client was trying to connect");
			}
		}
	}

	public void close() {
		keepListening = false;
	}

	private void handleClientData(String input, PrintWriter out) {
		Logger.info("Got data from client: " + input);
		Command.Result res = runCommand(input);
		out.println(res.toString());
		out.flush();
	}

	private Command.Result runCommand(String input) {
		try {
			Command cmd = resolveCommand(input);
			Logger.info("Got command");
			Command.Result res = cmd.call();
			Logger.info("Returning result: " + res);
			return res;
		} catch (JSONException e) {
			Logger.error(e.getMessage());
			return Command.Result.failure("Error running and parsing command");
		} catch(Exception e) {
			Logger.error(e.getMessage());
			return Command.Result.failure("Error while running command");
		}
	}

	private String readString(BufferedReader in) throws IOException {
		return in.readLine();
	}


	private Command resolveCommand(final String data) throws JSONException {
		JSONObject json = new JSONObject(data);
		String cmd = json.getString("action");

		return protocol.get(cmd);
	}

}
