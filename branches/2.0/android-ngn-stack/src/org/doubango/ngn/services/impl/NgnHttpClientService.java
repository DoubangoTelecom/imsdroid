package org.doubango.ngn.services.impl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.doubango.ngn.services.INgnHttpClientService;

import android.util.Log;

/**@page NgnHttpClientService_page HTTP/HTTPS Service
 * The HTTP/HTTPS service is used to send and retrieve data to/from remote server using HTTP/HTTPS protocol.
 */

/**
 * HTTP/HTTPS service.
 */
public class NgnHttpClientService extends NgnBaseService implements INgnHttpClientService{
	private static final String TAG = NgnHttpClientService.class.getCanonicalName();
	
	private static final int sTimeoutConnection = 3000;
	private static final int sTimeoutSocket = 5000;

	private HttpClient mClient;
	
	public NgnHttpClientService(){
		super();
	}
	
	@Override
	public boolean start() {
		Log.d(TAG, "Starting...");
		
		if(mClient == null){
			mClient = new DefaultHttpClient();
			final HttpParams params = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(params, sTimeoutConnection);
			HttpConnectionParams.setSoTimeout(params, sTimeoutSocket);
			((DefaultHttpClient)mClient).setParams(params);
			return true;
		}
		Log.e(TAG, "Already started");
		return false;
	}

	@Override
	public boolean stop() {
		if(mClient != null){
			mClient.getConnectionManager().shutdown();
		}
		mClient = null;
		return true;
	}

	@Override
	public String get(String uri) {
		try{
			HttpGet getRequest = new HttpGet(uri);
			final HttpResponse resp = mClient.execute(getRequest);
			if(resp != null){
				return getResponseAsString(resp);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	@Override
	public String post(String uri, String contentUTF8, String contentType){
		String result = null;
		try{
			HttpPost postRequest = new HttpPost(uri);
			final StringEntity entity = new StringEntity(contentUTF8,"UTF-8");
			if(contentType != null){
				entity.setContentType(contentType);
			}
			postRequest.setEntity(entity);
			final HttpResponse resp = mClient.execute(postRequest);
			if(resp != null){
				return getResponseAsString(resp);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	
	public static String getResponseAsString(HttpResponse resp){
        String result = "";
        try{
            InputStream in = resp.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line = null;
            while((line = reader.readLine()) != null){
                str.append(line + "\n");
            }
            in.close();
            result = str.toString();
        }catch(Exception ex){
            result = null;
        }
        return result;
    }
}
