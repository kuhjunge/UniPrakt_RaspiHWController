package eu.selfhost.suxdorf.hardware;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.gpio.extension.base.AdcGpioProvider;
import com.pi4j.gpio.extension.mcp.MCP3008GpioProvider;
import com.pi4j.gpio.extension.mcp.MCP3008Pin;
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinAnalogInput;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinAnalogValueChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerAnalog;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioUtil;

import eu.selfhost.suxdorf.MessageProcessor;

public class HardwareControl {
	private static final Logger LOG = Logger.getLogger(HardwareControl.class.getName());

	public static String PIN0 = "MyAnalogInput-CH0";
	public static String PIN1 = "MyAnalogInput-CH1";
	public static int dht22pin = 7;
	public Pin ledpin = RaspiPin.GPIO_22;

	// G8Controller
	MessageProcessor mp;
	// create gpio controller
	private final GpioController gpio = GpioFactory.getInstance();

	// provision gpio pin #01 as an output pin and turn on
	private GpioPinDigitalOutput led_pin;
	private GpioPinDigitalInput photo_pin;

	public HardwareControl(final MessageProcessor mp) throws Exception {
		if (Gpio.wiringPiSetup() == -1) {
			LOG.log(Level.SEVERE, "Die dumme Kuh initialisiert nicht");
		}
		this.mp = mp;
		try {
			initLed();
			initMCP3008();
			// TODO Dokumentieren was das macht
			GpioUtil.export(dht22pin, GpioUtil.DIRECTION_OUT);
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
		led_pin = gpio.provisionDigitalOutputPin(ledpin, "MyLED", PinState.HIGH);
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
		final GpioPinAnalogInput inputs[] = { gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH0, PIN0),
				gpio.provisionAnalogInputPin(provider, MCP3008Pin.CH1, PIN1) };
		// Schwellwert bevor Event ausgelöst wird
		provider.setEventThreshold(100, inputs);
		// Messgeschwindigkeit festlegen
		provider.setMonitorInterval(250);

		// Change Listener
		final GpioPinListenerAnalog listener = event -> sendMessage(event);

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

	private void sendMessage(final GpioPinAnalogValueChangeEvent event) {
		if (event.getPin().toString().contains(PIN0)) {
			final double value = event.getValue();
			mp.processMessageDoubleOut("/sensornetwork/8/sensor/" + "brightness", analogToLux(value), "Lux");
		} else if (event.getPin().toString().contains(PIN1)) {
			final double value = event.getValue();
			mp.processMessageDoubleOut("/sensornetwork/8/sensor/" + "hall", value, "Hall");
		}
	}

	public void shutdown() {
		// stop all GPIO activity/threads by shutting down the GPIO controller
		// (this method will forcefully shutdown all GPIO monitoring threads and
		// scheduled tasks)
		gpio.shutdown();
	}
}
