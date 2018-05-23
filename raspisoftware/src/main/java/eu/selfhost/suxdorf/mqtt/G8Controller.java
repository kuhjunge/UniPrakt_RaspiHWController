package eu.selfhost.suxdorf.mqtt;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class G8Controller {
	
	public static void main(String[] args) throws Exception {
		G8Controller g8c = new G8Controller();
	}
    
    HardwareControl hwc;
    G8MqttClient client;
    
    List<Double> luxList;
    
    public G8Controller() {
        try {
            // init hardware controller
            // reads ldr values
            hwc = new HardwareControl(this);
            // init mqtt client
            // sends and gets lux values
            client = new G8MqttClient(this);
            client.subscribeTo("/sensornetwork/#", 1);
            // lux value list
            luxList = new ArrayList<Double>();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    // is called when HardwareControl gets a new value from the ldr
    public void newValueAvailable(double value) {
        // create json object
        JSONObject dataset = new JSONObject();
        // put ldr value in lux in it
        dataset.put("value", value);
        dataset.put("measurement_unit", "Lux");
        // send lux value to mqtt broker
        client.publish("/sensornetwork/group8/sensor/brightness", dataset.toString(), 2, true);
    }
    
    // calculates the average lux value above the last 20 lux values
    private double averageLux() {
        double average = 0;
        
        for (double v: luxList) {
            average += v;
        }
        
        average = average / luxList.size();
        return average;
    }
    
    // gets called when a new mqtt message arrives
    public void messageIncoming(String arg0, String arg1) {
        try {
            // try to parse incoming message
            JSONObject json = new JSONObject(arg1);
            // get lux value
            double value = (Double) json.get("value");
            luxList.add(value);
            // remove first lux value if list is greater 20
            if (luxList.size() > 20) {
                luxList.remove(0);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        
        // calculate average lux value
        double average = averageLux();
        System.out.println("DER FUCKING MITTELWERT:" + average);
        
        // toggle led
        if (average > 50) {
            hwc.ledOff();
        } else {
            hwc.ledOn();
        }
        
        // print average lux to console
        System.out.println(average);
    }

}
