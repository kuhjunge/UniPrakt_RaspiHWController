package eu.selfhost.suxdorf.mqtt;

public class G8Subscriber {

	public static void main(final String[] args) throws Exception {
		// create instance of G8MqttClient
		final G8MqttClient client = new G8MqttClient();
		// subscribe /chat/+
		client.subscribeTo("/chat/+");
	}
}
