package eu.selfhost.suxdorf.mqtt;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.net.SocketFactory;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import eu.selfhost.suxdorf.util.SslUtil;

public class G8MqttClient extends MqttClient implements MqttCallback {
    
    // static configuration
    // TODO: Load configuration from file
    private static final String BROKER_URL = "ssl://141.83.175.234:9001";
    
    private static final String USERNAME = "group_8";
    private static final String PASSWORD = "nengausiem2AQueiph8U";
    private static final String CLIENT_ID = "G?: y1337";
    private static final String CERT_PUBLIC = "ca.crt";
    private static final String CERT_PRIVATE = "group_8.crt";
    private static final String CERT_KEY = "group_8.key";
    private static final String PATH = "\\home\\pi\\";
    
    private MqttConnectOptions connectOptions;
    
    private G8Controller g8c;
    
    // private constructor
    private G8MqttClient(String serverURI, String clientId) throws MqttException, UnrecoverableKeyException, KeyManagementException,
        KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
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
        String cert = path.getFile();
        
        path = G8MqttClient.class.getResource(CERT_PRIVATE);
        String groupcert = path.getFile();
        
        path = G8MqttClient.class.getResource(CERT_KEY);
        String groupkey = path.getFile();
        
        System.out.println("certificates loaded");
        
        // throws {UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException,
        //         CertificateException, IOException}exception if something in SslUtil went wrong
        connectOptions.setSocketFactory(SslUtil.getSocketFactory(
                cert,
                groupcert,
                groupkey,
                ""));
        
        System.out.println("certificates -> connect options");
        
        // this class should handle incoming messages and all the other interrupts
        this.setCallback(this);
        
        // try to connect to the broker
        this.connect(connectOptions);
                        
        System.out.println("connected to broker");
    }
    
    // private constructor
    private G8MqttClient(String serverURI, String clientId, String topic, String lastWill, int qos, boolean retained) throws MqttException, UnrecoverableKeyException, KeyManagementException,
        KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
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
        String cert = PATH + CERT_PUBLIC;
            
        path = G8MqttClient.class.getResource(CERT_PRIVATE);
        String groupcert = PATH + CERT_PRIVATE;
            
        path = G8MqttClient.class.getResource(CERT_KEY);
        String groupkey = PATH + CERT_KEY;
            
        System.out.println("certificates loaded");
            
        // throws {UnrecoverableKeyException, KeyManagementException, KeyStoreException, NoSuchAlgorithmException,
        //         CertificateException, IOException}exception if something in SslUtil went wrong
        connectOptions.setSocketFactory(SslUtil.getSocketFactory(
                cert,
                groupcert,
                groupkey,
                ""));
        
        System.out.println("certificates -> connect options");
            
        // this class should handle incoming messages and all the other interrupts
        this.setCallback(this);
        
        // try to connect to the broker with a last will message
        connectOptions.setWill(topic, lastWill.getBytes(), qos, retained);
        this.connect(connectOptions);
        System.out.println("connected to broker");
    }
    
    // public constructor, calls private constructor
    public G8MqttClient(G8Controller g8c) throws MqttException, UnrecoverableKeyException, KeyManagementException, KeyStoreException,
        NoSuchAlgorithmException, CertificateException, IOException {
        this(BROKER_URL, CLIENT_ID);
        this.g8c = g8c;
    }
    
    // public constructor with last will, calls private constructor
    public G8MqttClient(G8Controller g8c, String topic, String lastWill, int qos, boolean retained) throws MqttException, UnrecoverableKeyException, KeyManagementException, KeyStoreException,
        NoSuchAlgorithmException, CertificateException, IOException {
        this(BROKER_URL, CLIENT_ID, topic, lastWill, qos, retained);
        this.g8c = g8c;
    }
    
 // public constructor, calls private constructor
    public G8MqttClient() throws MqttException, UnrecoverableKeyException, KeyManagementException, KeyStoreException,
        NoSuchAlgorithmException, CertificateException, IOException {
        this(BROKER_URL, CLIENT_ID);
    }
    
    // public constructor with last will, calls private constructor
    public G8MqttClient(String topic, String lastWill, int qos, boolean retained) throws MqttException, UnrecoverableKeyException, KeyManagementException, KeyStoreException,
        NoSuchAlgorithmException, CertificateException, IOException {
        this(BROKER_URL, CLIENT_ID, topic, lastWill, qos, retained);
    }
    
    /**
     * Subscribes to a given topic with quality of service level
     * 
     * @param topic
     * @param qos
     * @throws MqttException
     */
    public void subscribeTo(String topic, int qos) throws MqttException {
        this.subscribe(topic, qos);
    }
    
    /**
     * Subscribes to a given topic
     * 
     * @param topic
     * @throws MqttException
     */
    public void subscribeTo(String topic) throws MqttException {
        this.subscribe(topic);
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
        // publishing a message is a blocking call, so this is called in a different thread
        new Thread() { public void run() {
            // instantiates a new mqtt message
            MqttMessage mqttmessage = new MqttMessage(message.getBytes());
            // sets the qos
            mqttmessage.setQos(qos);
            // sets the retained flag
                mqttmessage.setRetained(retained);
                // creates an delivery token where the return of the publish-call is set to
                MqttDeliveryToken token = null;
                // gets a mqtt topic object from a given topic as string
                MqttTopic mqtttopic = G8MqttClient.this.getTopic(topic);
                
                try {
                    // publish the message
                    token = mqtttopic.publish(mqttmessage);
                    // Waits for completion. This is the blocking part.
                    token.waitForCompletion();
            } catch (MqttException e) {
                System.out.println("Error while publishing message.");
                e.printStackTrace();
            }
                // starts the thread
        }}.start();
    }
    
    public void disconnect() {
        this.disconnect();
    }

    public void connectionLost(Throwable arg0) {
        System.out.println("Connection lost.");
        
    }

    public void deliveryComplete(IMqttDeliveryToken arg0) {
        System.out.println("Message delivered.");
        
    }

    public void messageArrived(String arg0, MqttMessage arg1) throws Exception {        
        if (g8c == null) {
    			g8c.messageIncoming(arg0, arg1.toString());
        } else {
        		System.out.println("Message arrived: <" + arg0 + ">, <" + arg1 + ">");
        }
    }
}
