package org.doubango.imsdroid.Utils;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {
	public static <T> List<T> filter(List<T>list, Predicate<T> predicate) {
		List<T> result = new ArrayList<T>();
		if(list != null){
		    for (T element: list) {
		        if (predicate.apply(element)) {
		            result.add(element);
		        }
		    }
		}
	    return result;
	}
	
	public static <T> T getFirstOrDefault(List<T>list, Predicate<T> predicate) {
		if(list != null){
		    for (T element: list) {
		        if (predicate.apply(element)) {
		            return element;
		        }
		    }
		}
		return null;
	}
}
