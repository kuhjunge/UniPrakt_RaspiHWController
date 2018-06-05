package eu.selfhost.suxdorf.control;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import eu.selfhost.suxdorf.MessageProcessor;
import eu.selfhost.suxdorf.NetworkMessenger;
import eu.selfhost.suxdorf.hardware.HardwareControl;
import eu.selfhost.suxdorf.mqtt.G8MqttClient;

public class G8Controller implements MessageProcessor {

	public static void main(final String[] args) throws Exception {
		final G8Controller g8c = new G8Controller();
	}

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
			e.printStackTrace();
			System.exit(1);
		}
	}

	// calculates the average lux value above the last 20 lux values
	private double averageLux() {
		double average = 0;
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
			System.out.println(e);
		}

		// calculate average lux value
		final double average = averageLux();
		System.out.println("DER MITTELWERT:" + average);
		// toggle led
		if (average > 50) {
			hwc.ledOff();
		} else {
			hwc.ledOn();
		}
		// print average lux to console
		System.out.println(average);
	}

	// is called when HardwareControl gets a new value from the ldr
	public void newValueAvailable(final double value, final String unit) {
		// create json object
		final JSONObject dataset = new JSONObject();
		// put ldr value in lux in it
		dataset.put("value", value);
		dataset.put("measurement_unit", unit);
		// send lux value to mqtt broker
		client.sendMessage("/sensornetwork/group8/sensor/brightness", dataset.toString());
	}

	@Override
	public void processMessageDoubleOut(final String topic, final double val) {
		newValueAvailable(val, topic);
	}

	@Override
	public void processMessageStringIn(final String topic, final String message) {
		messageIncoming(topic, message);
	}

	@Override
	public void processMessageStringOut(final String topic, final String message) {
		// TODO Auto-generated method stub
	}

}
