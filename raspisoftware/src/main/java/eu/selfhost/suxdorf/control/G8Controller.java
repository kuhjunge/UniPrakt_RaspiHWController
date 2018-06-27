package eu.selfhost.suxdorf.control;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import eu.selfhost.suxdorf.MessageProcessor;
import eu.selfhost.suxdorf.NetworkMessenger;
import eu.selfhost.suxdorf.hardware.HardwareControl;
import eu.selfhost.suxdorf.mqtt_alternative.MQTTAsyncChat;
import eu.selfhost.suxdorf.util.Configuration;
import eu.selfhost.suxdorf.util.ListWithAverage;

public class G8Controller implements MessageProcessor {

	private final static String serverAddr = "ServerAddress";
	private final static String user = "user";
	private final static String pw = "pw";
	private final static String certPath = "cert";

	public static void main(final String[] args) throws Exception {
		new G8Controller(); // Starte
	}

	private static final Logger LOG = Logger.getLogger(G8Controller.class.getName());
	private HardwareControl hwc;
	private NetworkMessenger client;
	private ListWithAverage luxList = new ListWithAverage();
	private Configuration conf;

	public G8Controller() {
		try {
			loadConfig();
			// init hardware controller
			// reads ldr values
			hwc = new HardwareControl(this);
			// init mqtt client
			// sends and gets lux values
			client = new MQTTAsyncChat(conf.getValue(user), conf.getValue(pw), conf.getValue(serverAddr),
					conf.getValue(certPath), "{\"message\": \"" + conf.getValue(user) + " out!\"}",
					"/sensornetwork/" + conf.getValue(user) + "/status", conf.getValue(user) + Math.random());
			// set Communication interface
			client.addNewMessageListener(this);
			// connect
			if (!client.connectClient()) {
				LOG.log(Level.SEVERE, "Could not connect!");
			}
			// open Channel
			client.openChannel("/sensornetwork/+/sensor/brightness");
			client.openChannel("/sensornetwork/+/sensor/hall");
			// Polling
			Runnable task = () -> {
				while (true) {
					String threadName = Thread.currentThread().getName();
					LOG.log(Level.WARNING, threadName + " Polling");
					hwc.polling();
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						LOG.log(Level.SEVERE, "Error Thread", e);
					}
				}
			};
			Thread thread = new Thread(task);
			thread.start();
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "Could not start!", e);
			System.exit(1);
		}
	}

	// loads the config file
	public void loadConfig() {
		// Load Config
		conf = new Configuration("MQTTDataDealer", "/app2/");
		conf.setValue(serverAddr, "tcp://127.0.0.1:1883");
		conf.setValue(user, "admin");
		conf.setValue(pw, "");
		conf.setValue(certPath, "");
		conf.init();
		conf.save();
	}

	// gets called when a new mqtt message arrives
	private void messageIncoming(final String arg0, final String arg1) {
		if (!arg0.equals("System")) {
			try {
				// try to parse incoming message
				final JSONObject json = new JSONObject(arg1);
				// get lux value
				if ("Lux".equals((String) json.get("measurement_unit")) && luxList.addVal(json.get("value"))) {
					checkNewValue();
				} else {
					LOG.log(Level.WARNING, () -> "Could not Process Value:" + arg1 + " from:" + arg0);
				}
			} catch (final Exception e) {
				LOG.log(Level.WARNING, () -> "Could not Parse JSON:" + arg1 + " from:" + arg0);
			}
		} else {
			LOG.log(Level.WARNING, arg1);
		}
	}

	// is called if a new Value is processed and reacts
	private void checkNewValue() {
		// calculate average lux value
		final double average = luxList.getAvgVal();
		LOG.log(Level.WARNING, "DER MITTELWERT:" + average);
		// toggle led
		if (average > 50) {
			hwc.ledOff();
		} else {
			hwc.ledOn();
		}
	}

	@Override
	public void processMessageStringIn(final String topic, final String message) {
		LOG.log(Level.WARNING, "->" + topic + " MSG:" + message);
		messageIncoming(topic, message);
	}

	@Override
	public void processMessageDoubleOut(final String topic, final double val, final String unit) {
		final JSONObject dataset = new JSONObject();
		// put ldr value in lux in it
		dataset.put("value", val);
		dataset.put("measurement_unit", unit);
		// send lux value to mqtt broker
		client.sendMessage("/sensornetwork/8/sensor/" + topic, dataset.toString());
	}

	// TODO: Prüfen ob wirklich benötigt
	@Override
	public void processMessageStringOut(final String topic, final String message) {
		final JSONObject dataset = new JSONObject();
		// put ldr value in lux in it
		dataset.put("value", message);
		dataset.put("measurement_unit", topic);
		// send lux value to mqtt broker
		client.sendMessage("/sensornetwork/8/sensor", dataset.toString());
	}
}
