package org.doubango.ngn.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NgnListUtils {
	public static <T> List<T> filter(Collection<T>list, NgnPredicate<T> predicate) {
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
	
	public static <T> T getFirstOrDefault(Collection<T>list, NgnPredicate<T> predicate) {
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
