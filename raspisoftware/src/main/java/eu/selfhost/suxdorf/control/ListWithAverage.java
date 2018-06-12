package eu.selfhost.suxdorf.control;

import java.util.LinkedList;
import java.util.List;

public class ListWithAverage {
	private List<Double> luxList = new LinkedList<>();

	public void addVal(Double val) {
		luxList.add(val);
		// remove first lux value if list is greater 20
		if (luxList.size() > 20) {
			luxList.remove(0);
		}
	}

	public double getAvgVal() {
		double average = 0.0;
		for (final double v : luxList) {
			average += v;
		}
		return average > 0 ? average / luxList.size() : 0;
	}
}
