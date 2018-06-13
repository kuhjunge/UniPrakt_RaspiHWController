package eu.selfhost.suxdorf.mqtt_alternative;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import eu.selfhost.suxdorf.MessageProcessor;
import eu.selfhost.suxdorf.NetworkMessenger;
import eu.selfhost.suxdorf.util.SslUtil;

public class MQTTAsyncChat implements NetworkMessenger {
	private static final Logger LOG = Logger.getLogger(MQTTAsyncChat.class.getName());
	private final MemoryPersistence persistence = new MemoryPersistence();
	private MqttAsyncClient client;
	private final MQTTChatReceiver cr = new MQTTChatReceiver();
	final MqttConnectOptions opts = new MqttConnectOptions();
	private final String username;

	public MQTTAsyncChat(final String username, final String pw, final String mqttServerAddress,
			final String pathToChert, final String testament, final String testamentTopic, final String clientId) {
		this.username = username;
		opts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
		opts.setCleanSession(true);
		opts.setUserName(username);
		opts.setPassword(pw.toCharArray());
		// Last Will setzen
		opts.setWill(testamentTopic, testament.getBytes(), 2, false);
		try {
			if (pathToChert.length() > 1 && new File(pathToChert + username + ".key").exists()) {
				opts.setSocketFactory(SslUtil.getSocketFactory(pathToChert + "ca.crt", pathToChert + username + ".crt",
						pathToChert + username + ".key", ""));
			} else {
				LOG.log(Level.WARNING, "No Encryption");
			}
			client = new MqttAsyncClient(mqttServerAddress, clientId, persistence);
			client.setCallback(cr);
		} catch (final Exception e) {
			LOG.log(Level.SEVERE, "Connection Error", e);
		}
	}

	@Override
	public void addNewMessageListener(final MessageProcessor pcl) {
		cr.addPropertyChangeListener(pcl);
	}

	private boolean checkConnected() {
		if (isConnected()) {
			return true;
		}
		LOG.log(Level.SEVERE, "Client not Connected");
		return false;
	}

	@Override
	public boolean connectClient() {
		if (!client.isConnected()) {
			try {
				final IMqttToken conToken = client.connect(opts);
				cr.setMessage("System", "Connecting...");
				while (conToken != null && conToken.isComplete() == false) {
					conToken.waitForCompletion();
				}
				while (!client.isConnected()) {
					Thread.sleep(100);
					cr.setMessage("System", ".");
				}
				return true;
			} catch (final Exception e) {
				LOG.log(Level.SEVERE, "Connection Error", e);
			}
		}
		return false;
	}

	@Override
	public void disconnect() {
		if (checkConnected()) {
			try {
				cr.setMessage("System", "Disconnectiong");
				client.disconnectForcibly();
			} catch (final MqttException e) {
				LOG.log(Level.SEVERE, "Disconnection Error", e);
			}
		}
		try {
			client.close(true);
			client = null;
		} catch (final MqttException e) {
			LOG.log(Level.SEVERE, "Shutdown Error", e);
		}
	}

	@Override
	public String getUserName() {
		return username;
	}

	@Override
	public boolean isConnected() {
		boolean res = false;
		if (client != null) {
			res = client.isConnected();
		}
		return res;
	}

	@Override
	public void openChannel(final String channel) {
		subscribe(channel, 2);
	}

	@Override
	public void openChannel(final String channel, final int qos) {
		subscribe(channel, qos);
	}

	private void publish(final String topicName, final int qos, final byte[] payload) throws MqttException {
		final MqttMessage message = new MqttMessage(payload);
		message.setQos(qos);
		client.publish(topicName, message);
	}

	public void publish(final String topic, final String message) {
		if (checkConnected()) {
			try {
				publish(topic, 2, message.getBytes("UTF-8"));
			} catch (final MqttException | UnsupportedEncodingException e) {
				LOG.log(Level.SEVERE, "Send Error", e);
			}
		}
	}

	@Override
	public void removeNewMessageListener(final MessageProcessor pcl) {
		cr.removePropertyChangeListener(pcl);
	}

	@Override
	public void sendMessage(final String channel, final String message) {
		publish(channel, message);
	}

	public void subscribe(final String topic, final int qos) {
		if (checkConnected()) {
			try {
				client.subscribe(topic, qos);
			} catch (final MqttException e) {
				LOG.log(Level.SEVERE, "Subscription Error", e);
			}
		} else {
			LOG.log(Level.SEVERE, "Subscription Error - MQTT not connected");
		}
	}
}
