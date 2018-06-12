package eu.selfhost.suxdorf.control;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;
import eu.selfhost.suxdorf.MessageProcessor;
import eu.selfhost.suxdorf.NetworkMessenger;
import eu.selfhost.suxdorf.hardware.HardwareControl;
import eu.selfhost.suxdorf.mqtt.G8MqttClient;

public class G8Controller implements MessageProcessor {

	public static void main(final String[] args) throws Exception {
		new G8Controller(); // Starte 
	}

	private static final Logger LOG = Logger.getLogger(G8Controller.class.getName());
	private HardwareControl hwc;
	private NetworkMessenger client;
	private List<Double> luxList;

	public G8Controller() {
		try {
			// init hardware controller
			// reads ldr values
			hwc = new HardwareControl(this);
			// init mqtt client
			// sends and gets lux values
			client = new G8MqttClient(this);
			client.addNewMessageListener(this);
			client.openChannel("/sensornetwork/+/sensor/brightness");
			// lux value list
			luxList = new ArrayList<>();
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "Could not start!", e);
			System.exit(1);
		}
	}

	// calculates the average lux value above the last 20 lux values
	private double averageLux() {
		double average = 0.0;
		for (final double v : luxList) {
			average += v;
		}
		average = average / luxList.size();
		return average;
	}

	// gets called when a new mqtt message arrives
	public void messageIncoming(final String arg0, final String arg1) {
		try {
			// try to parse incoming message
			final JSONObject json = new JSONObject(arg1);
			// get lux value
			final double value = (Double) json.get("value");
			luxList.add(value);
			// remove first lux value if list is greater 20
			if (luxList.size() > 20) {
				luxList.remove(0);
			}
		} catch (final Exception e) {
			LOG.log(Level.WARNING, "Could not Parse XML", e);
		}

		// calculate average lux value
		final double average = averageLux();
		LOG.log(Level.WARNING, "DER MITTELWERT:" + average);
		// toggle led
		if (average > 50) {
			hwc.ledOff();
		} else {
			hwc.ledOn();
		}
	}

	// is called when HardwareControl gets a new value from the ldr
	public void newValueAvailable(final double value, final String unit, final String topic) {
		// create json object
		final JSONObject dataset = new JSONObject();
		// put ldr value in lux in it
		dataset.put("value", value);
		dataset.put("measurement_unit", unit);
		// send lux value to mqtt broker
		client.sendMessage("/sensornetwork/group8/sensor/"+topic, dataset.toString());
	}

	@Override
	public void processMessageDoubleOut(final String topic, final double val, final String unit) {
		newValueAvailable(val, unit, topic);
	}

	@Override
	public void processMessageStringIn(final String topic, final String message) {
		messageIncoming(topic, message);
	}

	@Override
	public void processMessageStringOut(final String topic, final String message) {
		final JSONObject dataset = new JSONObject();
		// put ldr value in lux in it
		dataset.put("value", message);
		dataset.put("measurement_unit", topic);
		// send lux value to mqtt broker
		client.sendMessage("/sensornetwork/group8/sensor", dataset.toString());
	}
}
