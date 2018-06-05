package eu.selfhost.suxdorf.mqtt;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.json.JSONObject;

public class G8Publisher {

	public static void main(final String[] args) throws Exception {

		// create instance of G8MqttClient with last will
		final G8MqttClient client = new G8MqttClient("/chat/group8", "G8: Er ist tot, Jim.", 2, false);

		// buffered reader to get console input
		final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		// while loop to get input and send messages
		while (true) {
			final String input = br.readLine();

			// create JSON from input
			final JSONObject dataset = new JSONObject();
			dataset.put("from", "Group8");
			dataset.put("message", input);

			// publish message with retained = false and qos = 0
			client.publish("/chat/group8", dataset.toString(), 0, false);
		}

	}

}
