package eu.selfhost.suxdorf.control;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import eu.selfhost.suxdorf.MessageProcessor;
import eu.selfhost.suxdorf.util.ListWithDoubleAverage;

public class ValueProcessor {
	private static final Logger LOG = Logger.getLogger(ValueProcessor.class.getName());
	private final MessageProcessor mp;
	private final Map<String, ListWithDoubleAverage> data = new HashMap<>();
	private final String target;

	public ValueProcessor(final MessageProcessor mp, final String target) {
		this.mp = mp;
		this.target = target;
	}

	public ListWithDoubleAverage getList(final String unit) {
		return data.get(unit);
	}

	public void processValue(final String topic, final JSONObject val) {
		final ListWithDoubleAverage l = data.get(val.get("measurement_unit"));
		if (l != null) {
			l.addVal(val.get("value"));
			mp.processMessageDoubleOut(target, l.getAvgVal(), l.getUnit());
		} else {
			LOG.log(Level.WARNING, () -> "Could not Process Value:" + val.toString() + " from:" + topic);
		}
	}

	public void registrerData(final String unit) {
		data.put(unit, new ListWithDoubleAverage(unit));
	}
}
