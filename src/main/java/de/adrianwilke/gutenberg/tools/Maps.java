package de.adrianwilke.gutenberg.tools;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Maps {

	/**
	 * @see https://www.programcreek.com/2013/03/java-sort-map-by-value/
	 */
	public static TreeMap<String, Integer> sortMapByValue(Map<String, Integer> map) {
		class ValueComparator implements Comparator<String> {
			HashMap<String, Integer> map = new HashMap<String, Integer>();

			public ValueComparator(Map<String, Integer> map) {
				this.map.putAll(map);
			}

			@Override
			public int compare(String s1, String s2) {
				if (map.get(s1) >= map.get(s2)) {
					return -1;
				} else {
					return 1;
				}
			}
		}
		Comparator<String> comparator = new ValueComparator(map);
		TreeMap<String, Integer> result = new TreeMap<String, Integer>(comparator);
		result.putAll(map);
		return result;
	}
}
