package raspisoftware;

import java.io.IOException;

public class Controller {

	public static void main(String[] args) {
		System.out.println("Start");
		HardwareControl hwc = new HardwareControl();
		hwc.initLed();
		try {
			// hwc.test();
			hwc.initMCP3008();
			long t = System.currentTimeMillis();
			long end = t + 30000;
			while (System.currentTimeMillis() < end) {
				// pause to avoid churning
				Thread.sleep(200);
			}
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Fatal Error");
		}
		hwc.shutdown();
		System.out.println("Exiting ControlGpioExample");
	}

}
