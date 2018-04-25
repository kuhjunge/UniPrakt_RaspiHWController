package raspisoftware;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

public class Controller {

	public static void main(String[] args) {
		 // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #01 as an output pin and turn on
        final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "MyLED", PinState.HIGH);
        //pin.high();
        pin.setState(PinState.HIGH);
        //pin.setState(true);
        // set shutdown state for this pin
        //pin.setShutdownOptions(true, PinState.LOW);
        

        try {

	        Thread.sleep(4000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // turn off gpio pin #01
        

        // stop all GPIO activity/threads by shutting down the GPIO controller
        // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        pin.setShutdownOptions(true, PinState.LOW);
        gpio.shutdown();

        System.out.println("Exiting ControlGpioExample");
    }
}
