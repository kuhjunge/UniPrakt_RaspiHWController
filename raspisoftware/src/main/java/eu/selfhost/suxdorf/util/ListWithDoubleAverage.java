package eu.selfhost.suxdorf.util;

import java.util.LinkedList;
import java.util.List;

public class ListWithDoubleAverage {
	private final List<Double> dataList = new LinkedList<>();
	private final Object lock = new Object();
	private final String unit;

	public ListWithDoubleAverage(final String unit) {
		this.unit = unit;
	}

	public boolean addVal(final Object val) {
		boolean ret = true;
		synchronized (lock) {
			if (val instanceof Double) {
				dataList.add((Double) val);
			} else if (val instanceof Integer) {
				dataList.add((Integer) val + 0.0);
			} else if (val instanceof String) {
				dataList.add(Double.parseDouble((String) val));
			} else {
				ret = false;
			}
			// remove first lux value if list is greater 20
			if (dataList.size() > 20) {
				dataList.remove(0);
			}
		}
		return ret;
	}

	public double getAvgVal() {
		double average = 0.0;
		synchronized (lock) {
			for (final double v : dataList) {
				average += v;
			}
			return average > 0 ? average / dataList.size() : 0;
		}
	}

	public String getUnit() {
		return unit;
	}
}
