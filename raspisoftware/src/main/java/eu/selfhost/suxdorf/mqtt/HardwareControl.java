package eu.selfhost.suxdorf.mqtt;

import java.io.IOException;

import com.pi4j.gpio.extension.base.AdcGpioProvider;
import com.pi4j.gpio.extension.mcp.MCP3008GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP3008Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.spi.SpiChannel;

public class HardwareControl {
	// G8Controller
	G8Controller g8c;
	// create gpio controller
	private final GpioController gpio = GpioFactory.getInstance();

	// provision gpio pin #01 as an output pin and turn on
	private GpioPinDigitalOutput led_pin;
	private GpioPinDigitalInput photo_pin;

	public HardwareControl(final G8Controller g8c) throws Exception {
		this.g8c = g8c;
		try {
			initLed();
			initMCP3008();
		} catch (final Exception e) {
			shutdown();
			throw e;
		}
	}

	/**
	 * Analog to Lux
	 *
	 * @param analog
	 * @return
	 */
	private double analogToLux(final double analog) {
		final double uldr = analog * 3.3 / 1023;
		final double rldr = 4.7 * uldr / (3.3 - uldr);
		return Math.pow(rldr, -1.31022) * 210.91430;
	}

	public void initLed() {
		led_pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, "MyLED", PinState.HIGH);
		// set shutdown state for this pin
		led_pin.setShutdownOptions(true, PinState.LOW);
		// turn off gpio pin #01
		led_pin.setShutdownOptions(true, PinState.LOW);
	}

	public void initLightOnIfDark() {
		photo_pin = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, "MyPhotosensor");
	}

	public void initMCP3008() throws IOException, InterruptedException {
		// MCP3008 Analoger GPIO Provider
		final AdcGpioProvider provider = new MCP3008GpioProvider(SpiChannel.CS0);
		// Input Pin am MCP3008 festlegen
		final GpioPinAnalogInput inputs[] = {
				gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH0, "MyAnalogInput-CH0"), };
		// Schwellwert bevor Event ausgelöst wird
		provider.setEventThreshold(100, inputs);
		// Messgeschwindigkeit festlegen
		provider.setMonitorInterval(250);

		// Gibt alle Werte der Inputs aus
		for (final GpioPinAnalogInput input : inputs) {
			System.out.println("<INITIAL VALUE> [" + input.getName() + "] : RAW VALUE = " + input.getValue());
		}

		// Change Listener
		final GpioPinListenerAnalog listener = event -> {
			final double value = event.getValue();
			System.out.println("<CHANGED VALUE> [" + event.getPin().getName() + "] : RAW VALUE = " + value);

			g8c.newValueAvailable(analogToLux(value));
		};

		// Listener anhängen
		gpio.addListener(listener, inputs);
	}

	public void ledOff() {
		if (led_pin != null) {
			led_pin.setState(PinState.LOW);
		}
	}

	public void ledOn() {
		if (led_pin != null) {
			led_pin.setState(PinState.HIGH);
		}
	}

	public void lightOnIfDark() {
		if (led_pin != null && photo_pin != null && photo_pin.getState() == PinState.HIGH) {
			led_pin.setState(PinState.HIGH);
		} else {
			led_pin.setState(PinState.LOW);
		}
	}

	public void shutdown() {
		// stop all GPIO activity/threads by shutting down the GPIO controller
		// (this method will forcefully shutdown all GPIO monitoring threads and
		// scheduled tasks)
		gpio.shutdown();
	}
}
