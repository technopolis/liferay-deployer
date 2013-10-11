package org.jenkinsci.plugins.liferay.helpers;

import hudson.model.BuildListener;

import java.io.File;

import net.sf.json.JSONObject;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;

public class PostFileUtil extends AbstractHelper {

	public PostFileUtil(BuildListener listener, boolean verbose) {

		super(listener, verbose);
	}

	@SuppressWarnings("deprecation")
	public JSONObject post(File file, String url, String contentType, String username, String password)
		throws Exception {

		HttpClient httpclient = new DefaultHttpClient();
		httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		HttpPost httppost = new HttpPost(url);
		String encoding = Base64.encodeBase64URLSafeString((username + ":" + password).getBytes());
		httppost.setHeader("Authorization", "Basic " + encoding);
		MultipartEntity mpEntity = new MultipartEntity();
		ContentBody cbFile = new FileBody(file, contentType);
		mpEntity.addPart("userfile", cbFile);

		httppost.setEntity(mpEntity);
		log("EXECUTING REQUEST " + httppost.getRequestLine() + " ...");
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity resEntity = response.getEntity();

		log("RESPONSE STATUS: " + response.getStatusLine());
		String responseText = null;
		if (resEntity != null) {
			responseText = EntityUtils.toString(resEntity);
		}
		log("RESPONSE JSON: " + responseText);
		if (resEntity != null) {
			resEntity.consumeContent();
		}
		httpclient.getConnectionManager().shutdown();
		
		return JSONObject.fromObject(responseText);
	}
}
