package org.doubango.ngn.utils;

public interface NgnCallbackFunc<T> {
	void callback(T object, Object[]... args);
}
