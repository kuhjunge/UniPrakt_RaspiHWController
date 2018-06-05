package eu.selfhost.suxdorf.mqtt;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import eu.selfhost.suxdorf.MessageProcessor;
import eu.selfhost.suxdorf.NetworkMessenger;
import eu.selfhost.suxdorf.util.SslUtil;

public class G8MqttClient extends MqttClient implements MqttCallback, NetworkMessenger {

	// static configuration
	// TODO: Load configuration from file
	private static final String BROKER_URL = "ssl://141.83.175.234:9001";
	private static final String USERNAME = "group_8";
	private static final String PASSWORD = "nengausiem2AQueiph8U";
	private static final String CLIENT_ID = "G?: y1337";
	private static final String CERT_PUBLIC = "ca.crt";
	private static final String CERT_PRIVATE = "group_8.crt";
	private static final String CERT_KEY = "group_8.key";
	private static final String PATH = "/home/pi/";

	private final MqttConnectOptions connectOptions;

	private MessageProcessor mp;

	// public constructor, calls private constructor
	public G8MqttClient() throws MqttException, UnrecoverableKeyException, KeyManagementException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		this(BROKER_URL, CLIENT_ID);
	}

	// public constructor, calls private constructor
	public G8MqttClient(final MessageProcessor g8c) throws MqttException, UnrecoverableKeyException,
			KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		this(BROKER_URL, CLIENT_ID);
		mp = g8c;
	}

	// public constructor with last will, calls private constructor
	public G8MqttClient(final MessageProcessor g8c, final String topic, final String lastWill, final int qos,
			final boolean retained) throws MqttException, UnrecoverableKeyException, KeyManagementException,
			KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		this(BROKER_URL, CLIENT_ID, topic, lastWill, qos, retained);
		mp = g8c;
	}

	// private constructor
	private G8MqttClient(final String serverURI, final String clientId) throws MqttException, UnrecoverableKeyException,
			KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		// create an MqttClient (MqttClient constructor)
		super(serverURI, clientId);

		System.out.println("super ini");

		// set connection options
		connectOptions = new MqttConnectOptions();
		connectOptions.setCleanSession(true);
		connectOptions.setKeepAliveInterval(30);
		connectOptions.setUserName(USERNAME);
		connectOptions.setPassword(PASSWORD.toCharArray());

		System.out.println("connect options set");

		// load certificate files from class location
		// throws IOException if any of them isn't available
		URL path = G8MqttClient.class.getResource(CERT_PUBLIC);
		final String cert = PATH + CERT_PUBLIC;

		path = G8MqttClient.class.getResource(CERT_PRIVATE);
		final String groupcert = PATH + CERT_PRIVATE;

		path = G8MqttClient.class.getResource(CERT_KEY);
		final String groupkey = PATH + CERT_KEY;

		System.out.println("certificates loaded");

		// throws {UnrecoverableKeyException, KeyManagementException, KeyStoreException,
		// NoSuchAlgorithmException,
		// CertificateException, IOException}exception if something in SslUtil went
		// wrong
		connectOptions.setSocketFactory(SslUtil.getSocketFactory(cert, groupcert, groupkey, ""));

		System.out.println("certificates -> connect options");

		// this class should handle incoming messages and all the other interrupts
		setCallback(this);

		// try to connect to the broker
		this.connect(connectOptions);

		System.out.println("connected to broker");
	}

	// public constructor with last will, calls private constructor
	public G8MqttClient(final String topic, final String lastWill, final int qos, final boolean retained)
			throws MqttException, UnrecoverableKeyException, KeyManagementException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException {
		this(BROKER_URL, CLIENT_ID, topic, lastWill, qos, retained);
	}

	// private constructor
	private G8MqttClient(final String serverURI, final String clientId, final String topic, final String lastWill,
			final int qos, final boolean retained) throws MqttException, UnrecoverableKeyException,
			KeyManagementException, KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
		// create an MqttClient (MqttClient constructor)
		super(serverURI, clientId);

		System.out.println("super ini");

		// set connection options
		connectOptions = new MqttConnectOptions();
		connectOptions.setCleanSession(true);
		connectOptions.setKeepAliveInterval(30);
		connectOptions.setUserName(USERNAME);
		connectOptions.setPassword(PASSWORD.toCharArray());

		System.out.println("connect options set");

		// load certificate files from class location
		// throws IOException if any of them isn't available
		URL path = G8MqttClient.class.getResource(CERT_PUBLIC);
		final String cert = PATH + CERT_PUBLIC;

		path = G8MqttClient.class.getResource(CERT_PRIVATE);
		final String groupcert = PATH + CERT_PRIVATE;

		path = G8MqttClient.class.getResource(CERT_KEY);
		final String groupkey = PATH + CERT_KEY;

		System.out.println("certificates loaded");

		// throws {UnrecoverableKeyException, KeyManagementException, KeyStoreException,
		// NoSuchAlgorithmException,
		// CertificateException, IOException}exception if something in SslUtil went
		// wrong
		connectOptions.setSocketFactory(SslUtil.getSocketFactory(cert, groupcert, groupkey, ""));

		System.out.println("certificates -> connect options");

		// this class should handle incoming messages and all the other interrupts
		setCallback(this);

		// try to connect to the broker with a last will message
		connectOptions.setWill(topic, lastWill.getBytes(), qos, retained);
		this.connect(connectOptions);
		System.out.println("connected to broker");
	}

	@Override
	public void addNewMessageListener(final MessageProcessor pcl) {
		mp = pcl;
	}

	@Override
	public boolean connectClient() {
		// TODO: richtige Connect Funktion implementieren
		return true;
	}

	@Override
	public void connectionLost(final Throwable arg0) {
		System.out.println("Connection lost.");

	}

	@Override
	public void deliveryComplete(final IMqttDeliveryToken arg0) {
		System.out.println("Message delivered.");

	}

	@Override
	public void disconnect() {
		this.disconnect();
	}

	@Override
	public String getUserName() {
		return USERNAME;
	}

	@Override
	public void messageArrived(final String arg0, final MqttMessage arg1) throws Exception {
		if (mp != null) {
			mp.processMessageStringIn(arg0, arg1.toString());
		} else {
			System.out.println("Message arrived: <" + arg0 + ">, <" + arg1 + ">");
		}
	}

	@Override
	public void openChannel(final String channel) {
		openChannel(channel, 1);
	}

	@Override
	public void openChannel(final String channel, final int qos) {
		try {
			subscribeTo(channel, qos);
		} catch (final MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Publishes a message to a given topic with QoS level.
	 *
	 * @param topic
	 * @param message
	 * @param qos
	 * @param retained
	 */
	public void publish(final String topic, final String message, final int qos, final boolean retained) {
		// publishing a message is a blocking call, so this is called in a different
		// thread
		new Thread() {
			@Override
			public void run() {
				// instantiates a new mqtt message
				final MqttMessage mqttmessage = new MqttMessage(message.getBytes());
				// sets the qos
				mqttmessage.setQos(qos);
				// sets the retained flag
				mqttmessage.setRetained(retained);
				// creates an delivery token where the return of the publish-call is set to
				MqttDeliveryToken token = null;
				// gets a mqtt topic object from a given topic as string
				final MqttTopic mqtttopic = G8MqttClient.this.getTopic(topic);

				try {
					// publish the message
					token = mqtttopic.publish(mqttmessage);
					// Waits for completion. This is the blocking part.
					token.waitForCompletion();
				} catch (final MqttException e) {
					System.out.println("Error while publishing message.");
					e.printStackTrace();
				}
				// starts the thread
			}
		}.start();
	}

	@Override
	public void removeNewMessageListener(final MessageProcessor pcl) {
		mp = null;
	}

	@Override
	public void sendMessage(final String channel, final String message) {
		publish(channel, message, 2, true);

	}

	/**
	 * Subscribes to a given topic
	 *
	 * @param topic
	 * @throws MqttException
	 */
	public void subscribeTo(final String topic) throws MqttException {
		this.subscribe(topic);
	}

	/**
	 * Subscribes to a given topic with quality of service level
	 *
	 * @param topic
	 * @param qos
	 * @throws MqttException
	 */
	public void subscribeTo(final String topic, final int qos) throws MqttException {
		this.subscribe(topic, qos);
	}
}
