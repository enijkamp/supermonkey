package io.supermonkey.crawler.device.android;

import com.android.ddmlib.IDevice;
import org.testobject.capture.CaptureServer;
import org.testobject.capture.Settings;
import org.testobject.capture.sockets.CaptureSocketServerProxy;
import org.testobject.capture.vnc.Const;
import org.testobject.capture.vnc.VncCaptureServer;
import org.testobject.commons.bus.EventBus;
import org.testobject.commons.bus.EventEmitter;
import org.testobject.commons.bus.SimpleEventBus;
import org.testobject.commons.events.KeyEvent;
import org.testobject.commons.events.PointerClickEvent;
import org.testobject.commons.events.PointerEvent;
import org.testobject.commons.events.TimestampEvent;
import org.testobject.commons.net.sockets.Socket;
import org.testobject.commons.net.sockets.SocketServer;
import org.testobject.commons.util.config.Configuration;
import org.testobject.commons.util.config.Constants;
import org.testobject.commons.util.image.PixelFormat;
import org.testobject.kernel.inference.input.FrameBufferSetEvent;
import org.testobject.kernel.inference.input.FrameBufferUpdateEvent;
import org.testobject.kernel.inference.input.GetFramebuffer;
import org.testobject.net.sockets.java.JavaTcpServer;
import org.testobject.runtime.platform.linux.x11.X11;
import org.testobject.runtime.services.provisioning.Device;
import org.testobject.runtime.services.provisioning.DeviceState;
import org.testobject.runtime.services.provisioning.FrameBufferSource;
import org.testobject.runtime.services.provisioning.android.*;
import org.testobject.runtime.services.provisioning.linux.XShmCaptureGenymotionFactory;
import org.testobject.services.model.DeviceDescriptor;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.testobject.commons.route.RouteBuilder.route;

/**
 * @author Erik Nijkamp (erik.nijkamp@gmail.com)
 * @since 31.05.14
 */
public class Genymotion {

	public static IDevice createDevice() throws IOException {
		String sdkPath = Configuration.getProperty(Constants.android_sdk_location);
		AndroidDebugBridgeFactory androidDebugBridgeFactory = new AndroidDebugBridgeFactory(sdkPath);
//		AndroidDebugBridge androidDebugBridge = androidDebugBridgeFactory.createAndroidDebugBridge();
//		IDevice[] devices = androidDebugBridge.getDevices();
//		if(devices.length != 0) {
//			return devices[0];
//		} else {
		org.testobject.runtime.services.provisioning.android.AndroidDevice device = bootGenymotionDevice(androidDebugBridgeFactory);
		return device.getDevice();
//		}
	}

	public static org.testobject.runtime.services.provisioning.android.AndroidDevice bootGenymotionDevice(AndroidDebugBridgeFactory androidDebugBridgeFactory) throws IOException {

		String sdkPath = Configuration.getProperty(Constants.android_sdk_location);
		String workFolderPath = Configuration.getProperty(Constants.application_work_folder) + File.separator;
		String host = Configuration.getProperty(Constants.env_host_name);
		String genymotionLocation = Configuration.getProperty(Constants.genymotion_location);

		GenymotionDeviceFactory androidGenymotionDeviceFactory = new GenymotionDeviceFactory(getFrameBufferFactory(), androidDebugBridgeFactory,
				getPort(), sdkPath, host, workFolderPath, genymotionLocation);

		androidGenymotionDeviceFactory.open();
		openDisplayWithThreads(":0.0");

		EventBus bus = new SimpleEventBus();
		Settings settings = new Settings(PixelFormat.create32bppPixelFormat(false), Settings.Congestion.PushingWithDiffs, false, Const.Encoding.Hextile);
		final CaptureSocketServerProxy socketServer = new CaptureSocketServerProxy(new EventEmitter(bus), createSocketServerFactory(),
				createCaptureServerFactory(), settings);

		Device.InitVideoCallback initVideoCallback = new Device.InitVideoCallback() {

			@Override
			public void deviceInitVideo(final Device device) {
				// glue fbu
				route().event(FrameBufferSetEvent.class).from(device.getFrameBuffer()).to(socketServer).build();
				route().event(FrameBufferUpdateEvent.class).from(device.getFrameBuffer()).to(socketServer).build();

				// glue input
				socketServer.getEventSource().register(PointerEvent.class, new PointerEvent.Handler() {
					@Override
					public void pointerEvent(TimestampEvent.Timestamp timestamp, PointerClickEvent.ClickType clickType, int x, int y) {



						if (device.getState() == DeviceState.ONLINE) {
							if (clickType == PointerClickEvent.ClickType.DOWN) {
								device.getInputRobot().mouseDown(x, y);
							} else if (clickType == PointerClickEvent.ClickType.MOVE) {
								device.getInputRobot().mouseMove(x, y);
							} else {
								device.getInputRobot().mouseUp(x, y);
							}
						}
					}
				});

				socketServer.getEventSource().register(KeyEvent.class, new KeyEvent.Handler() {
					@Override
					public void keyEvent(TimestampEvent.Timestamp timestamp, int key, boolean controlKey, boolean downFlag) {
						if (device.getState() == DeviceState.ONLINE) {
							if (downFlag) {
								device.getInputRobot().keyDown(key);
							} else {
								device.getInputRobot().keyUp(key);
							}
						}
					}
				});
				// open
				socketServer.open(new InetSocketAddress(device.getVncPort()));
			}
		};

		org.testobject.runtime.services.provisioning.android.AndroidDevice device = androidGenymotionDeviceFactory.create(Device.DeviceRequest.from(getDescriptor()), initVideoCallback, Device.BootProcessListener.Factory.stub(), Device.CloseListener.Factory.stub(), Device.OrientationCallback.Factory.stub());

		device.open();
		device.waitFor(DeviceState.UI, 60 * 1000);
		device.getFrameBuffer().open();
		device.waitFor(DeviceState.ONLINE, 60 * 1000);

		System.out.println("adbDevice is ready on port " + device.getVncPort());

		return device;
	}


	private static FrameBufferSource.Factory getFrameBufferFactory() {
		final String display = ":0.0";
		final ExecutorService executorService = Executors.newFixedThreadPool(1);
		return new XShmCaptureGenymotionFactory(display, executorService);
	}

	public static GetPort getPort() {
		final int consolePortEnd = Integer.parseInt(Configuration.getProperty(Constants.console_ports_end));
		final int consolePortStart = Integer.parseInt(Configuration.getProperty(Constants.console_ports_start));

		return new AndroidGetPortAdapter(consolePortStart, consolePortEnd);
	}

	private static X11.Display openDisplayWithThreads(String displayName) {
		// CAUTION: XInitThreads has to be invoked BEFORE all other X11 invocations, otherwise
		// XLockDisplay() and XUnlockDisplay() are non-functional, see X11 documentation (en)
		X11.INSTANCE.XInitThreads();

		// CAUTION: Error handlers have to be set BEFORE other X11 invocations (en)
		X11.INSTANCE.XSetErrorHandler(errorHandler);
		X11.INSTANCE.XSetIOErrorHandler(ioHandler);

		// CAUTION: Multi-threaded code where mutual exclusions are guarded by XLockDisplay() / XUnlockDisplay()
		// can only and only use a SINGLE display connection (en)
		return X11.INSTANCE.XOpenDisplay(displayName);
	}

	private static final X11.XErrorHandlerFunc errorHandler = new X11.XErrorHandlerFunc()
	{
		@Override
		public int callback(X11.Display display, X11.XErrorEvent error_event)
		{
			System.err.println("xerror: " + error_event.error_code + " -> " + error_event.minor_code);
			return 0;
		}
	};

	private static final X11.XErrorHandlerFunc ioHandler = new X11.XErrorHandlerFunc()
	{
		@Override
		public int callback(X11.Display display, X11.XErrorEvent error_event)
		{
			System.err.println("io xerror: " + error_event.error_code + " -> " + error_event.minor_code);
			return 0;
		}
	};

	private static SocketServer.Factory createSocketServerFactory() {
		return new SocketServer.Factory() {
			@Override
			public SocketServer create(InetSocketAddress address) {
				return new JavaTcpServer(address);
			}
		};
	}

	private static CaptureServer.CaptureFactory createCaptureServerFactory() {
		return new CaptureServer.CaptureFactory() {
			@Override
			public CaptureServer create(EventEmitter bus, Socket socket, GetFramebuffer framebuffer, Settings settings) {
				return new VncCaptureServer(bus, socket, framebuffer, settings);
			}
		};
	}

	private static DeviceDescriptor getDescriptor() {
		return new DeviceDescriptor(
				"geny_19",
				"geny_19",
				"geny_19",
				"AndroidDevice 4.4.2 (intel)",
				true,
				true,
				false,
				true,
				"Google APIs:19",
				"template",
				true,
				true,
				"armabi",
				true,
				true,
				"4.4.2",
				19,
				false,
				true,
				0,
				1080,
				1920,
				480,
				false,
				1024,
				512,
				2048,
				4,
				1500);
	}

}
