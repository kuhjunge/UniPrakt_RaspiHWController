package eu.selfhost.suxdorf.mqtt;

public class G8Subscriber {

	public static void main(String[] args) throws Exception {
		
		// create instance of G8MqttClient
		G8MqttClient client = new G8MqttClient();
		
		// subscribe /chat/+
		client.subscribeTo("/chat/+");
	}

}
