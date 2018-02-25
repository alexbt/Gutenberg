package de.adrianwilke.gutenberg.content.re;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import de.adrianwilke.gutenberg.exceptions.TextRootRuntimeException;

/**
 * Represents an abstract text.
 * 
 * @author Adrian Wilke
 */
public abstract class Txt implements Comparable<Txt> {

	static public final int DEFAULT_LENGTH_FILENAME = 30;
	static public final int DEFAULT_LENGTH_LINE_NUMBER = 5;
	static public final int DEFAULT_LENGTH_PART = 4;
	static public boolean EXECUTE = true;

	protected SortedSet<Integer> lineIndexes;
	protected String name;
	protected final Txt parent;
	protected SortedMap<Integer, List<Txt>> sections;

	/**
	 * Creates new text.
	 * 
	 * @param parent
	 *            null, if is root
	 */
	protected Txt(Txt parent) {
		this.parent = parent;
	}

	/**
	 * {@link Comparable} implemented as comparison of first line indexes.
	 */
	public int compareTo(Txt t) {
		int comparison;
		comparison = Integer.compare(getLineIndexes().first(), t.getLineIndexes().first());
		if (0 != comparison) {
			return comparison;
		}
		comparison = Integer.compare(getLineIndexes().last(), t.getLineIndexes().last());
		if (0 != comparison) {
			return comparison;
		}
		return getName().compareTo(t.getName());
	}

	/**
	 * Returns the given line and lines before and after. Includes line numbers.
	 * 
	 * @param lineNumber
	 *            The line number to display (NOT the index)
	 * @param range
	 *            The range above and below the line of interest
	 */
	public String getContext(int lineNumber, int range) {
		StringBuilder sb = new StringBuilder();
		int lineIndex = lineNumber - 1;

		int startIndex = lineIndex - range;
		if (startIndex < 0) {
			startIndex = 0;
		}
		int endIndex = lineIndex + range;
		if (endIndex > getRoot().getLineIndexes().last()) {
			endIndex = getRoot().getLineIndexes().last();
		}

		int endLineNumberLength = String.valueOf(endIndex).length();
		for (int i = startIndex; i <= endIndex; i++) {
			for (int j = 0; j < endLineNumberLength - String.valueOf(i + 1).length(); j++) {
				sb.append(" ");
			}
			sb.append(i + 1);
			if (i == lineIndex) {
				sb.append(" >");
			} else {
				sb.append("  ");
			}
			sb.append(" ");
			sb.append(getLine(i));
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	};

	/**
	 * Gets line with related index.
	 */
	public abstract String getLine(int index);

	/**
	 * Gets indexes of text.
	 */
	public abstract SortedSet<Integer> getLineIndexes();

	/**
	 * Gets simplified line with related index. Lines are in lower case and only
	 * consist of letters, numbers and spaces.
	 */
	public abstract String getLineSimplified(int index);

	/**
	 * Gets name of text
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets parent element.
	 * 
	 * @throws TextRootRuntimeException
	 *             if is root and has no parent element
	 */
	protected Txt getParent() {
		if (parent == null) {
			throw new TextRootRuntimeException();
		} else {
			return parent;
		}
	}

	/**
	 * Gets the text root element.
	 */
	protected Txt getRoot() {
		Txt text = this;
		while (text.hasParent()) {
			text = text.getParent();
		}
		return text;
	}

	/**
	 * Returns text-parts divides by empty lines.
	 */
	public SortedMap<Integer, List<Txt>> getSections() {
		if (sections == null) {
			sections = new TreeMap<Integer, List<Txt>>();

			// First iteration: Get distances (a.k.a. sequences of empty lines), which occur
			// in text
			SortedSet<Integer> distances = new TreeSet<Integer>();
			int emptyLinesCounter = 0;

			// Iterate over all relevant lines/indexes
			for (int i = getLineIndexes().first(); i <= getLineIndexes().last(); i++) {
				if (getLine(i).trim().isEmpty()) {
					// Current line is empty -> Check if there are more
					emptyLinesCounter++;

				} else {
					// Current line contains text -> Add distance before text
					if (emptyLinesCounter > 0) {
						distances.add(emptyLinesCounter);
					}
					emptyLinesCounter = 0;
				}
			}

			// Second iteration: Split text dependent of number of empty lines (a.k.a.
			// distances)
			Map<Integer, Integer> distancesToStartIndex = new HashMap<Integer, Integer>();
			for (Integer distance : distances) {
				sections.put(distance, new LinkedList<Txt>());
				distancesToStartIndex.put(distance, -1);
			}
			emptyLinesCounter = 0;
			int lastTextLineIndex = -1;

			// Iterate over all relevant lines/indexes
			for (int lineIndex = getLineIndexes().first(); lineIndex <= getLineIndexes().last(); lineIndex++) {

				if (getLine(lineIndex).trim().isEmpty()) {

					// Current line is empty
					emptyLinesCounter++;

					// For all real distances
					for (int distance = 1; distance <= emptyLinesCounter; distance++) {
						if (!distances.contains(distance)) {
							continue;
						}

						if (!distancesToStartIndex.get(distance).equals(-1)) {
							String name = "distance" + distance + "-index" + sections.get(distance).size();
							sections.get(distance).add(
									(new TxtPart(this, name, distancesToStartIndex.get(distance), lastTextLineIndex)));
							distancesToStartIndex.put(distance, -1);
						}
					}

				} else {
					// First line after empty lines -> Remember the line index

					for (Integer distance : distances) {
						if (distancesToStartIndex.get(distance).equals(-1)) {
							distancesToStartIndex.put(distance, lineIndex);
						}
					}
					lastTextLineIndex = lineIndex;
					emptyLinesCounter = 0;
				}

			}

			// Add end parts
			for (Integer distance : distances) {
				if (!distancesToStartIndex.get(distance).equals(-1)) {
					String name = "distance" + distance + "-index" + sections.get(distance).size();
					sections.get(distance)
							.add((new TxtPart(this, name, distancesToStartIndex.get(distance), lastTextLineIndex)));
				}
			}
		}
		return sections;
	}

	/**
	 * Returns, if is not root
	 */
	protected abstract boolean hasParent();

	/**
	 * Tries to remove index and returns result. The source text line will not be
	 * removed.
	 */
	public boolean remove(int index) {
		return getLineIndexes().remove(index);
	}

	/**
	 * Sets name of text
	 */
	public void setName(String name, boolean parentAsPrefix) {
		if (parentAsPrefix && hasParent()) {
			this.name = getParent().getName() + "/" + name;
		} else {
			this.name = name;
		}
	}

	/**
	 * Returns string representation of text
	 */
	@Override
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();

		stringBuilder.append(getName());
		for (int i = getName().length(); i < DEFAULT_LENGTH_FILENAME; i++) {
			stringBuilder.append(" ");
		}
		stringBuilder.append(" ");

		if (sections != null) {
			for (int i = String.valueOf(getSections().get(1).size()).length(); i < DEFAULT_LENGTH_PART; i++) {
				stringBuilder.append(" ");
			}
			stringBuilder.append(" ");
			stringBuilder.append(getSections().get(1).size());
			stringBuilder.append(" parts ");
		} else {
			for (int i = 0; i < DEFAULT_LENGTH_PART + 8; i++) {
				stringBuilder.append(" ");
			}
		}

		if (lineIndexes != null) {
			for (int i = String.valueOf(lineIndexes.size()).length(); i < DEFAULT_LENGTH_LINE_NUMBER; i++) {
				stringBuilder.append(" ");
			}
			stringBuilder.append(" ");
			stringBuilder.append(lineIndexes.size());
			stringBuilder.append(" lines ");
		} else {
			for (int i = 0; i < DEFAULT_LENGTH_LINE_NUMBER + 8; i++) {
				stringBuilder.append(" ");
			}
		}

		if (lineIndexes != null) {
			stringBuilder.append(" [");
			stringBuilder.append(lineIndexes.first());
			stringBuilder.append(",");
			stringBuilder.append(lineIndexes.last());
			stringBuilder.append("]");
			stringBuilder.append(" ");
		}

		return stringBuilder.toString();
	}

	/**
	 * Returns a string containing all lines with line number prefixes.
	 */
	public String toStringAllLines(boolean addLineNumberPrefix) {
		return toStringBuilder(new StringBuilder(), addLineNumberPrefix).toString();
	}

	/**
	 * Puts all contained lines to the string builder.
	 */
	public StringBuilder toStringBuilder(StringBuilder stringBuilder, boolean putLinePrefix) {
		for (Integer lineIndex : getLineIndexes()) {
			if (putLinePrefix) {
				stringBuilder.append(lineIndex + 1);
				for (int i = String.valueOf(lineIndex + 1).length(); i < DEFAULT_LENGTH_LINE_NUMBER; i++) {
					stringBuilder.append(" ");
				}
				stringBuilder.append(" ");
			}
			stringBuilder.append(getLine(lineIndex));
			stringBuilder.append(System.lineSeparator());
		}
		return stringBuilder;
	}
}