package eu.selfhost.suxdorf.control;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import eu.selfhost.suxdorf.MessageProcessor;
import eu.selfhost.suxdorf.NetworkMessenger;
import eu.selfhost.suxdorf.hardware.HardwareControl;
import eu.selfhost.suxdorf.mqtt_alternative.MQTTAsyncChat;
import eu.selfhost.suxdorf.util.Configuration;

public class G8Controller implements MessageProcessor {

	private final static String SERVERADDR = "ServerAddress";
	private final static String USER = "user";
	private final static String PW = "pw";
	private final static String CERTPATH = "cert";
	private final static String DEBUGFEATURES = "debug";

	private static final Logger LOG = Logger.getLogger(G8Controller.class.getName());
	private static final String CONFIGFILE = "/app/"; // "/app/"

	public static void main(final String[] args) throws Exception {
		new G8Controller(); // Starte
	}

	private HardwareControl hwc;
	private NetworkMessenger client;
	private Configuration conf;
	private final ValueProcessor valProc = new ValueProcessor(this, "/actuatornetwork/8/actuator/display");

	public G8Controller() {
		try {
			loadConfig();
			// reads ldr values
			hwc = new HardwareControl(this);
			// init mqtt client
			// sends and gets lux values
			client = new MQTTAsyncChat(conf.getValue(USER), conf.getValue(PW), conf.getValue(SERVERADDR),
					conf.getValue(CERTPATH), "{\"message\": \"" + conf.getValue(USER) + " out!\"}",
					"/sensornetwork/" + conf.getValue(USER) + "/status", conf.getValue(USER) + Math.random());
			// set Communication interface
			client.addNewMessageListener(this);
			// connect
			if (!client.connectClient()) {
				LOG.log(Level.SEVERE, "Could not connect!");
			}
			// open Channel
			valProc.registrerData("Lux");
			valProc.registrerData("Hall");
			valProc.registrerData("Celsius");
			valProc.registrerData("Percent");
			client.openChannel("/sensornetwork/+/sensor/brightness");
			client.openChannel("/sensornetwork/+/sensor/hall");
			client.openChannel("/sensornetwork/3/sensor/indoor/temperature");
			client.openChannel("/sensornetwork/3/sensor/indoor/humidity");
			// DEBUG Feature um Werte zu simulieren
			if (Boolean.getBoolean(conf.getValue(DEBUGFEATURES))) {
				final Runnable task = () -> {
					final Random random = new Random();
					while (true) {
						final String threadName = Thread.currentThread().getName();
						LOG.log(Level.WARNING, threadName + " Polling");
						// hwc.polling();
						processMessageDoubleOut("/sensornetwork/3/sensor/indoor/temperature", random.nextInt(30) + 5,
								"Celsius");
						processMessageDoubleOut("/sensornetwork/3/sensor/indoor/humidity", random.nextInt(30) + 40,
								"Percent");
						processMessageDoubleOut("/actuatornetwork/8/actuator/display", "new Dataset", "Text");
						try {
							Thread.sleep(10000);

						} catch (final InterruptedException e) {
							LOG.log(Level.SEVERE, "Error Thread", e);
						}
					}
				};
				final Thread thread = new Thread(task);
				thread.start();
			}
			// Ende Debug Teil
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "Could not start!", e);
			System.exit(1);
		}
	}

	// is called if a new Value is processed and reacts
	private void checkNewValue() {
		// calculate average lux value
		final double average = valProc.getList("Lux").getAvgVal();
		LOG.log(Level.WARNING, "DER MITTELWERT:" + average);
		// toggle led
		if (average > 50) {
			hwc.ledOff();
		} else {
			hwc.ledOn();
		}
	}

	// loads the config file
	public void loadConfig() {
		// Load Config
		conf = new Configuration("MQTTDataDealer", CONFIGFILE);
		conf.setValue(SERVERADDR, "tcp://127.0.0.1:1883");
		conf.setValue(USER, "admin");
		conf.setValue(PW, "");
		conf.setValue(CERTPATH, "");
		conf.setValue(DEBUGFEATURES, Boolean.toString(false));
		conf.init();
		conf.save();
	}

	// gets called when a new mqtt message arrives
	private void messageIncoming(final String arg0, final String arg1) {
		if (!arg0.equals("System")) {
			try {
				// try to parse incoming message
				final JSONObject json = new JSONObject(arg1);
				valProc.processValue(arg0, json);
				checkNewValue();
			} catch (final Exception e) {
				LOG.log(Level.WARNING, () -> "Could not Parse JSON:" + arg1 + " from:" + arg0);
			}
		} else {
			LOG.log(Level.WARNING, arg1);
		}
	}

	@Override
	public void processMessageDoubleOut(final String topic, final Object val, final String unit) {
		final JSONObject dataset = new JSONObject();
		// put ldr value in lux in it
		dataset.put("value", val);
		dataset.put("measurement_unit", unit);
		// send lux value to mqtt broker
		client.sendMessage(topic, dataset.toString());
	}

	@Override
	public void processMessageStringIn(final String topic, final String message) {
		LOG.log(Level.WARNING, "->" + topic + " MSG:" + message);
		messageIncoming(topic, message);
	}
}
