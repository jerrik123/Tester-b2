/**
 * Copyright Mangocity Limited (c) 2010. All rights reserved.
 * This software is proprietary to and embodies the confidential
 * technology of Mangocity Limited.  Possession, use, or copying
 * of this software and media is authorized only pursuant to a
 * valid written license from Mangocity or an authorized sublicensor.
 */
package org.mangocube.corenut.commons.util;

import jregex.MatchResult;
import jregex.Replacer;
import jregex.Substitution;
import jregex.TextBuffer;

/**
 * TODO dengtailin: Change to the actual description of this class
 * @version   Revision History
 * <pre>
 * Author     Version       Date        Changes
 * Allen      1.0           2010-12-29     Created
 *
 * </pre>
 * @since 1.0
 */

public class StringUtils {
	public static String replaceAll(String original, String[] replacements) {
		if (replacements == null || replacements.length == 0
			|| original == null || original.length() == 0)
			return original;

		if (replacements.length % 2 != 0) {
			throw new IllegalArgumentException(
					"invalid replacements: the length of replacements should be even");
		}

		StringBuilder buffer = new StringBuilder(original);
		for (int i = 0; i < replacements.length - 1; i = i + 2) {
			String find = replacements[i];
			String replacement = replacements[i + 1];

			int idx = buffer.length();
			int offset = find.length();

			while ((idx = buffer.lastIndexOf(find, idx - 1)) > -1) {
				buffer.replace(idx, idx + offset, replacement);
			}
		}
		return buffer.toString();
	}
	
	public static String regReplaceAll(String original, String[] replacements) {
		if (replacements == null || replacements.length == 0
				|| original == null || original.length() == 0)
			return original;

		if (replacements.length % 2 != 0) {
			throw new IllegalArgumentException(
					"invalid replacements: the length of replacements should be even");
		}

		char[] orginChars = original.toCharArray();
		int capacity = orginChars.length;
		
		ArrayTextBuffer buffer = new ArrayTextBuffer(orginChars);
		buffer.expandCapacity(capacity / 5);
		
		//expand capacity
		ArrayTextBuffer backBuf = new ArrayTextBuffer(capacity + capacity / 5);

		for (int i = 0; i < replacements.length - 1; i = i + 2) {
			String find = replacements[i];
			String replacement = replacements[i + 1];

			replaceAllBytMatcher(buffer, backBuf, find, replacement);
            
			ArrayTextBuffer tmpBuf = backBuf;
			backBuf = buffer;
			buffer = tmpBuf;
		}

		return buffer.toString();
	}

	private static class DummySubstitution implements Substitution {
		String str;

		DummySubstitution(String s) {
			str = s;
		}

		public void appendSubstitution(MatchResult match, TextBuffer res) {
			if (str != null)
				res.append(str);
		}
	};

	static class ArrayTextBuffer implements TextBuffer {

		char value[];

		int count;
		
		static int EXPAND_UNIT = 100;
		
		ArrayTextBuffer(char[] value) {
		   this.value = value;
		   this.count = value.length;
		}
		
		ArrayTextBuffer(int len) {
		   this.value = new char[len];
		   this.count = 0;
		}
		 
		public void append(char c) {
			int newCount = count + 1;
			if (newCount > value.length) {
				expandCapacity(getNewCapcity(1));
			}
			value[count++] = c;
		}

		public void append(char[] chars, int start, int len) {
			int newCount = count + len;
			if (newCount > value.length) {
				expandCapacity(getNewCapcity(len));
			}
			System.arraycopy(chars, start, value, count, len);
			count = newCount;
		}
		
		public int getNewCapcity(int len) {
			return (len + value.length) / 5 + len;
		}

		public void expandCapacity(int newAddCapacity) {
			if (newAddCapacity < EXPAND_UNIT) {
				newAddCapacity = EXPAND_UNIT;
			}
			int newCapacity = value.length + newAddCapacity;
		 
			char newValue[] = new char[newCapacity];
			System.arraycopy(value, 0, newValue, 0, count);
			value = newValue;
		}

		public void append(String str) {
			if (str == null) {
				str = "null";
			}
			int len = str.length();
			if (len != 0) {
				int newCount = count + len;
				if (newCount > value.length) {
					expandCapacity(value.length / 5);
				}
				str.getChars(0, len, value, count);
				count = newCount;
			}
		}
		
		public void clear() {
			this.count = 0;
		}
		
		public int getCount() {
			return count;
		}

		public char[] toChars() {        
			return value;
		}
		
		public String toString() {
			return new String(value, 0, count);
		}
	};
	
	static class MangoReplacer extends Replacer {
		private Substitution substitution;
		private jregex.Pattern pattern;
		private ArrayTextBuffer distBuf;
		
		public MangoReplacer(ArrayTextBuffer backBuf, jregex.Pattern pattern, Substitution substitution) {
			super(pattern, substitution);
			this.pattern = pattern;
			this.substitution = substitution;
			this.distBuf = backBuf;
		}

		public ArrayTextBuffer replace(ArrayTextBuffer buffer, int offset) {
			distBuf.clear();
			
			replace(pattern.matcher(buffer.toChars(), offset,  buffer.getCount()), substitution, distBuf);			
			
			return distBuf;
		}
	}

	public static ArrayTextBuffer replaceAllBytMatcher(ArrayTextBuffer buffer, ArrayTextBuffer distBuf, String find, String replacement) {
		final jregex.Pattern p = new jregex.Pattern(find);
		MangoReplacer r = new MangoReplacer(distBuf, p, new DummySubstitution(replacement));
		return r.replace(buffer, 0);
	} 
}
