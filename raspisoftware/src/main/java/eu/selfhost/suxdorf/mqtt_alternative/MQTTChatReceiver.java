package eu.selfhost.suxdorf.mqtt_alternative;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import eu.selfhost.suxdorf.MessageProcessor;

public class MQTTChatReceiver implements MqttCallback {
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private MessageProcessor mp;

	public MQTTChatReceiver() {
	}

	public void addPropertyChangeListener(final MessageProcessor pcl) {
		mp = pcl;
	}

	@Override
	public void connectionLost(final Throwable arg0) {
		setMessage("System", "Connection lost");
	}

	@Override
	public void deliveryComplete(final IMqttDeliveryToken arg0) {
		setMessage("System", "Message delivered");
	}

	@Override
	public void messageArrived(final String arg0, final MqttMessage arg1) throws Exception {
		setMessage(arg0, new String(arg1.getPayload(), StandardCharsets.UTF_8));
	}

	public void removePropertyChangeListener(final MessageProcessor pcl) {
		mp = null;
	}

	public void setMessage(final String topic, final String value) {
		mp.processMessageStringIn(topic, value);
	}
}
