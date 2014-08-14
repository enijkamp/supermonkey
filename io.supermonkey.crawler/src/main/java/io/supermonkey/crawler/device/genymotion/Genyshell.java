package io.supermonkey.crawler.device.genymotion;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 18.06.14
 */
public class Genyshell {

	private final String genyPath;
	private final String ip;

	public Genyshell(String genyPath, String ip) {
		this.genyPath = genyPath;
		this.ip = ip;
	}

	public void gpsStatusEnabled() {
		exec("gps setstatus enabled");
	}

	public void gpsSetPosition(double latitude, double longitude) {
		exec("gps setlatitude " + latitude);
		exec("gps setlongitude " + longitude);
	}

	public void exec(String command) {
		Process process = openShell(ip, command);

		InputStream in = process.getInputStream();
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		System.out.println(readLine(reader));
		System.out.println(readLine(reader));
		System.out.println(readLine(reader));
		System.out.println(readLine(reader));
	}

	private void waitFor(BufferedReader reader, String pattern) {
		long timeout = now() + TimeUnit.SECONDS.toMillis(20);

		while(now() < timeout) {
			String line = readLine(reader);
			System.out.println(line);
			if (line.contains(pattern)) {
				return;
			}
		}

		throw new IllegalStateException();
	}

	private String readLine(BufferedReader reader) {
		try {
			return reader.readLine();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void waitForDeviceSelected(BufferedReader reader) {
		long timeout = now() + TimeUnit.SECONDS.toMillis(20);

		while(now() < timeout) {
			String line = readLine(reader);
			System.out.println(line);
			if (line.contains("Genymotion virtual device selected")) {
				return;
			}
		}

		throw new IllegalStateException();
	}

	private void waitForPrompt(InputStream in) {
		long timeout = now() + TimeUnit.SECONDS.toMillis(20);

		try {

			while(now() < timeout) {
				byte[] bytes = new byte[in.available()];
				in.read(bytes);
				String line = new String(bytes);
				if(!line.isEmpty()) {
					System.out.println("waitForPrompt -> " + line);
				}

				if(line.contains("Genymotion Shell >")) {
					return;
				}
			}
		} catch(IOException e) {
			throw new RuntimeException(e);
		}

		throw new IllegalStateException();
	}

	private static long now() {
		return System.currentTimeMillis();
	}

	private Map<String, Integer> listDevices(BufferedReader reader, PrintWriter writer) {

		writer.println("devices list");
		writer.flush();

		// skip header
		waitFor(reader, "Available devices:");

		System.out.println(readLine(reader));
		System.out.println(readLine(reader));
		System.out.println(readLine(reader));

		// read body
		Pattern pattern = Pattern.compile("^(\\d+)\\|(.*)\\|(.*)\\|(.*)\\|(.*)\\|(.*)$");

		Map<String, Integer> devices = new HashMap<>();
		String line = readLine(reader).replaceAll(" ", "");
		while(pattern.matcher(line).matches()) {
			System.out.println(line);

			String[] groups = line.split("\\|");

			for(int i = 0; i < groups.length; i++) {
				System.out.println(i + " -> " + groups[i]);
			}

			int id = Integer.parseInt(groups[0]);
			String name = groups[5];

			devices.put(name, id);

			writer.println();
			writer.flush();

			line = readLine(reader).replaceAll(" ", "");
		}

		return devices;
	}

	public void execLocal(String command) {

		Process process = openShell();
		InputStream in = process.getInputStream();
		OutputStream out = process.getOutputStream();

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		PrintWriter writer = new PrintWriter(out);

		{
			writer.println();
			writer.flush();
			waitForPrompt(in);

			int deviceId = listDevices(reader, writer).get("geny_19");
			System.out.println("device-id is " + deviceId);

			writer.println();
			writer.flush();
			waitForPrompt(in);

			writer.println("devices select " + deviceId);
			writer.flush();
			String output = readLine(reader);
			System.out.println(output);
		}

		{
			writer.println();
			writer.flush();
			waitForPrompt(in);

			writer.println("gps setstatus enabled");
			writer.flush();
			String output = readLine(reader);
			System.out.println(output);
		}

		{
			writer.println();
			writer.flush();
			waitForPrompt(in);

			writer.println("gps setbearing 200");
			writer.flush();
			String output = readLine(reader);
			System.out.println(output);
		}
	}

	private Process openShell() {
		try {
			ProcessBuilder builder = new ProcessBuilder(genyPath + "/genyshell");
			Process process = builder.start();

			return process;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}

	private Process openShell(String ip, String command) {
		try {
			ProcessBuilder builder = new ProcessBuilder(genyPath + "/genyshell", "-r", ip, "-c", command);
			Process process = builder.start();

			return process;
		} catch(IOException e) {
			throw new RuntimeException(e);
		}
	}


	public static void main(String ... args) {
		Genyshell shell = new Genyshell("/home/enijkamp/Code/tools/genymotion", "192.168.56.101");
		shell.gpsStatusEnabled();
		shell.gpsSetPosition(52.5192, 13.4061);
	}
}