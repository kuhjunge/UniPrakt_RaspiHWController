package raspisoftware;

import java.io.IOException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;

public class HardwareControl {
	// create gpio controller
	private final GpioController gpio = GpioFactory.getInstance();

	// provision gpio pin #01 as an output pin and turn on
	private GpioPinDigitalOutput led_pin;
	private GpioPinDigitalInput photo_pin;

	// SPI
	private SpiDevice spi = null;
	// ADC channel count
    public static short ADC_CHANNEL_COUNT = 8;  // MCP3004=4, MCP3008=8
	
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

	public void lightOnIfDark() {
		if (led_pin != null && photo_pin != null && photo_pin.getState() == PinState.HIGH) {
			led_pin.setState(PinState.HIGH);
		} else {
			led_pin.setState(PinState.LOW);
		}
	}

	public void initSpi() throws IOException {
		System.out.println("init spi");
		spi = SpiFactory.getInstance(SpiChannel.CS0,
                SpiDevice.DEFAULT_SPI_SPEED, // default spi speed 1 MHz
                SpiDevice.DEFAULT_SPI_MODE); // default spi mode 0
		System.out.println("init spi finished");
	}
	
    /**
     * Read data via SPI bus from MCP3002 chip.
     * @throws IOException
     */
    public void read() throws IOException, InterruptedException {
    	System.out.println("read:");
    	int conversion_value = getConversionValue((short)0);
    	//for(short channel = 0; channel < ADC_CHANNEL_COUNT; channel++){
         //   int conversion_value = getConversionValue(channel);
         //   System.out.println(String.format(" | %04d", conversion_value)); // print 4 digits with leading zeros
       // }
    	 System.out.println(String.format(" | %04d", conversion_value)); // print 4 digits with leading zeros
        System.out.println(" |\r");
        Thread.sleep(250);
    }
    
    /**
     * Communicate to the ADC chip via SPI to get single-ended conversion value for a specified channel.
     * @param channel analog input channel on ADC chip
     * @return conversion value for specified analog input channel
     * @throws IOException
     */
    public int getConversionValue(short channel) throws IOException {

        // create a data buffer and initialize a conversion request payload
        byte data[] = new byte[] {
                (byte) 0b00000001,                              // first byte, start bit
                (byte)(0b10000000 |( ((channel & 7) << 4))),    // second byte transmitted -> (SGL/DIF = 1, D2=D1=D0=0)
                (byte) 0b00000000                               // third byte transmitted....don't care
        };

        // send conversion request to ADC chip via SPI channel
        byte[] result = spi.write(data);

        // calculate and return conversion value from result bytes
        int value = (result[1]<< 8) & 0b1100000000; //merge data[1] & data[2] to get 10-bit result
        value |=  (result[2] & 0xff);
        return value;
    }
	
	public void shutdown() {
		// stop all GPIO activity/threads by shutting down the GPIO controller
		// (this method will forcefully shutdown all GPIO monitoring threads and
		// scheduled tasks)
		gpio.shutdown();
	}
}
