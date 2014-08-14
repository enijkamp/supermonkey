package io.supermonkey.crawler.device.android;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

/**
* @author Erik Nijkamp
* @since 30.05.14
*/
public class JsonWire implements Closeable {

	private static final Log log = LogFactory.getLog(JsonWire.class);

	public static class CommandException extends RuntimeException {
		public CommandException(String msg) {
			super(msg);
		}

		public CommandException(String msg, Throwable cause) {
			super(msg, cause);
		}
	}

	public final BufferedReader reader;
	public final PrintWriter writer;

	private final Socket socket;

	public JsonWire(Socket socket) throws IOException {
		this.socket = socket;
		this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		this.writer = new PrintWriter(socket.getOutputStream(), true);
	}

	public String sendCommand(String cmd, String params) {

		sendRequest(cmd, params);

		String response = readResponse();

		return response;
	}

	public String sendCommand(String cmd) {

		sendRequest(cmd);

		String response = readResponse();

		return response;
	}

	private void sendRequest(String action, String params) {
		String request = "{\"cmd\" : \"action\", \"action\" : \"" + action + "\" , \"params\" : " + params + " }";
		log.info("Sending request: " + request);
		writer.println(request);
		writer.flush();
	}

	private void sendRequest(String action) {
		String request = "{\"cmd\" : \"action\", \"action\" : \"" + action + "\"}";
		log.info("Sending request: " + request);
		writer.println(request);
		writer.flush();
	}

	private String readResponse() {
		try {
			String json = reader.readLine();
			log.info("Received response: " + json);
			JSONObject response = new JSONObject(json);
			if(response.getInt("status") != 0) {
				throw new CommandException("Response: " + json);
			} else {
				return response.getString("value");
			}
		} catch(IOException e) {
			throw new CommandException("Cannot read response", e);
		} catch(JSONException e) {
			throw new CommandException("Cannot parse json response", e);
		}
	}

	@Override
	public void close() {
		try {
			this.reader.close();
		} catch (Throwable e) {
			// ignore
		}
		try {
			this.writer.close();
		} catch (Throwable e) {
			// ignore
		}
		try {
			this.socket.close();
		} catch (Throwable e) {
			// ignore
		}
	}

}
