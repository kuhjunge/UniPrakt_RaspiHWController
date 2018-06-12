package eu.selfhost.suxdorf.util;

import java.util.LinkedList;
import java.util.List;

public class ListWithAverage {
	private List<Double> luxList = new LinkedList<>();

	public boolean addVal(Object val) {
		boolean ret = true;
		if (val instanceof Double) {
			luxList.add((Double) val);
		} else if (val instanceof String) {
			luxList.add(Double.parseDouble((String) val));
		} else {
			ret = false;
		}
		// remove first lux value if list is greater 20
		if (luxList.size() > 20) {
			luxList.remove(0);
		}
		return ret;
	}

	public double getAvgVal() {
		double average = 0.0;
		for (final double v : luxList) {
			average += v;
		}
		return average > 0 ? average / luxList.size() : 0;
	}
}
