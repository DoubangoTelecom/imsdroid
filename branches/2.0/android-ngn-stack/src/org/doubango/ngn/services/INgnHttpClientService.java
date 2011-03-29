package org.doubango.ngn.services;


public interface INgnHttpClientService extends INgnBaseService{
	String get(String uri);
	String post(String uri, String contentUTF8);
}
