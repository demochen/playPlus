package snails.common.util.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class EmptyUtil {
	public static boolean isEmpty(Object[] arr) {
		return isEmpty(arr, false);
	}

	public static boolean isEmpty(Object[] arr, boolean checkFirst) {
		return !isNotEmpty(arr, checkFirst);
	}

	public static boolean isNotEmpty(Object[] arr) {
		return isNotEmpty(arr, false);
	}

	public static boolean isNotEmpty(Object[] arr, boolean checkFirst) {
		return (arr != null) && !isEmpty(Arrays.asList(arr), checkFirst);
	}

	public static boolean isEmpty(Collection c, boolean checkFirst) {
		if (checkFirst) {
			return (c == null) || (c.size() < 1) || isEmpty(c.iterator().next() + "");
		}

		return (c == null) || (c.size() < 1);
	}

	public static boolean isNotEmpty(Collection c, boolean checkFirst) {
		return !isEmpty(c, checkFirst);
	}

	public static boolean isEmpty(Collection c) {
		return isEmpty(c, false);
	}

	public static boolean isNotEmpty(Collection c) {
		return isNotEmpty(c, false);
	}

	public static boolean isEmpty(String str) {
		return (str == null) || (str.trim().length() == 0) || str.equalsIgnoreCase("NULL")
				|| str.equalsIgnoreCase("NaN");
	}

	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	public static void main(String[] args) {
		List<String> emptyList = new ArrayList<String>();
		emptyList.add("");
		List<String> notEmptyList = new ArrayList<String>();
		notEmptyList.add("1");
		String[] emptyArray = new String[] { "" };
		String[] notEmptyArray = new String[] { "1" };

		System.out.println(isEmpty(emptyList, true));// true
		System.out.println(isNotEmpty(emptyList, true));// false

		System.out.println(isEmpty(notEmptyList, true));// false
		System.out.println(isNotEmpty(notEmptyList, true));// true

		System.out.println(isEmpty(emptyArray, true));// true
		System.out.println(isNotEmpty(emptyArray, true));// false

		System.out.println(isEmpty(notEmptyArray, true));// false
		System.out.println(isNotEmpty(notEmptyArray, true));// true
	}

}
