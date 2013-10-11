
package org.jenkinsci.plugins.liferay.helpers;

import com.thoughtworks.xstream.core.util.Base64Encoder;

import hudson.model.BuildListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.jenkinsci.plugins.liferay.LiferayServerMap;
import org.jenkinsci.plugins.liferay.exceptions.APIException;
import org.jenkinsci.plugins.liferay.helpers.json.Output;
import org.jenkinsci.plugins.liferay.helpers.json.Plugin;
import org.jenkinsci.plugins.liferay.helpers.json.Plugins;
import org.jfree.util.Log;
import org.springframework.web.bind.annotation.RequestMethod;

public class LiferayServerHelper extends AbstractHelper {

	private static final Logger logger = Logger.getLogger(LiferayServerHelper.class.getName());

	private final PostFileUtil postFileUtil;
	
	
	public PostFileUtil getPostFileUtil() {
	
		return postFileUtil;
	}

	public LiferayServerHelper(LiferayServerMap liferayServer) {

		this(null, liferayServer, false);
	}
	
	public LiferayServerHelper(BuildListener listener, LiferayServerMap liferayServer) {

		this(listener, liferayServer, false);
	}
	
	public LiferayServerHelper(BuildListener listener, LiferayServerMap liferayServer, boolean verbose) {

		super(listener, verbose);
		postFileUtil = new PostFileUtil(listener, verbose);
		this.host = liferayServer.getLiferayServerHost();
		this.port = liferayServer.getLiferayServerPort();
		this.username = liferayServer.getLiferayUserName();
		this.password = liferayServer.getLiferayUserPassword();
		this.managerContextPath = this.host + ":" + this.port + "/server-manager-web";
	}

	private final String host;
	private final String port;
	private final String username;
	private final String password;
	private HttpClient httpClient;
	private final String managerContextPath;

	/**
	 * Checks if the remote liferay server has the "Remote IDE Connector" plugin
	 * installed using the following url: http://[liferay
	 * server]/server-manager-web/plugins/
	 * 
	 * @return true if the plugin is instaled and answers
	 * @throws IOException
	 * @throws JSONException
	 */
	public boolean hasRemoteIdeConnectorInstalled(LiferayServerMap liferayServerMap) {

		if (liferayServerMap == null) {
			log("No liferay server found for given name...");
			return false;
		}

		String liferayServerUrlPrefix = liferayServerMap.getLiferayServerUrl();
		logger.info("CHECKING CONNECTION WITH LIFERAY IN " + liferayServerUrlPrefix);
		try {
			JSONObject jsonObject = readJsonFromUrl(
				liferayServerUrlPrefix + "/server-manager-web/plugins", liferayServerMap.getLiferayUserName(), liferayServerMap.getLiferayUserPassword(),
				RequestMethod.GET);
			
			Plugins plugins = (Plugins) JSONObject.toBean(jsonObject, Plugins.class);
			if (plugins.getError() != null && !plugins.getError().equals("")) {
				return false;
			}
		}
		catch (JSONException e) {
			logger.log(Level.INFO, e.getMessage(), e);
			log("No JSON result for " + liferayServerUrlPrefix + "/server-manager-web/plugins");
		}
		catch (IOException e) {
			logger.log(Level.INFO, e.getMessage(), e);
			log("Could not connect to " + liferayServerUrlPrefix);
		}

		return true;
	}

	public boolean hasPluginInstalled(LiferayServerMap liferayServerMap, String pluginName) {

		if (liferayServerMap == null) {
			log("No liferay server found for given name...");
			return false;
		}
		if (pluginName == null || pluginName.equals("")) {
			log("No plugin name has been set...");
			return false;
		}

		String liferayServerUrlPrefix = liferayServerMap.getLiferayServerUrl();
		// int counter = 30;
		try {
			// while(counter > 0) {
			// logger.info("TRY:" + counter);
			JSONObject jsonObject =
				readJsonFromUrl(
					liferayServerUrlPrefix + "/server-manager-web/plugins/" + pluginName, liferayServerMap.getLiferayUserName(),
					liferayServerMap.getLiferayUserPassword(), RequestMethod.POST);

			Plugin plugins = (Plugin) JSONObject.toBean(jsonObject, Plugin.class);
			logger.info("  GOT:" + plugins);
			logger.info("  SLEEPING FOR ONE SECOND...");
			// Thread.sleep(1000);
			// counter--;
			// }
		}
		catch (JSONException e) {
			logger.log(Level.INFO, e.getMessage(), e);
			log("No JSON result for " + liferayServerUrlPrefix + "/server-manager-web/plugins");
		}
		catch (IOException e) {
			logger.log(Level.INFO, e.getMessage(), e);
			log("Could not connect to " + liferayServerUrlPrefix);
		}
		return false;
	}

	public Plugin uploadPlugin(LiferayServerMap liferayServer, String fileName, String pluginName) {

		String url = liferayServer.getLiferayServerUrl() + "/server-manager-web/plugins/" + pluginName;
		File fileToUpload = new File(fileName);
		try {
			
			return (Plugin) JSONObject.toBean(
				getPostFileUtil().post(
					fileToUpload, 
					url, 
					"multipart/form-data", 
					liferayServer.getLiferayUserName(), 
					liferayServer.getLiferayUserPassword()), Plugin.class);
		}
		catch (Exception e) {
			Log.error(e.getMessage(), e);
		}
		
		return null;
	}

//	public JSONObject uploadPlugin(LiferayServerMap liferayServer, String fileName, String pluginName)
//		throws IOException {
//
//		String urlToConnect = liferayServer.getLiferayServerUrl() + "/server-manager-web/plugins/" + pluginName;
//		logger.info("CALLING: " + urlToConnect);
//
//		// String paramToSend = "fubar";
//		File fileToUpload = new File(fileName);
//		String boundary = Long.toHexString(System.currentTimeMillis());
//		PrintWriter writer = null;
//		URLConnection connection = null;
//		try {
//			connection = new URL(urlToConnect).openConnection();
//			Base64Encoder encoder = new Base64Encoder();
//			String encoding = encoder.encode((liferayServer.getLiferayUserName() + ":" + liferayServer.getLiferayUserPassword()).getBytes());
//			// connection.setRequestMethod("POST");
//			connection.setRequestProperty("Authorization", "Basic " + encoding);
//			connection.setDoOutput(true); // This sets request method to POST.
//			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
//			writer = new PrintWriter(new OutputStreamWriter(connection.getOutputStream(), "UTF-8"));
//
//			// writer.println("--" + boundary);
//			// writer.println("Content-Disposition: form-data; name=\"paramToSend\"");
//			// writer.println("Content-Type: text/plain; charset=UTF-8");
//			// writer.println();
//			// writer.println(paramToSend);
//
//			writer.println("\r\n--" + boundary);
//			writer.println("\r\nContent-Disposition: form-data; name=\"fileToUpload\"; filename=\"ostores.war\"\r\n");
//			writer.println("\r\nContent-Type: text/plain; charset=UTF-8\r\n\r\n");
//			writer.println();
//			BufferedReader reader = null;
//			try {
//				reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileToUpload), "UTF-8"));
//				for (String line; (line = reader.readLine()) != null;) {
//					writer.println(line);
//				}
//			}
//			catch (UnsupportedEncodingException e) {
//				logger.log(Level.INFO, e.getMessage(), e);
//			}
//			catch (FileNotFoundException e) {
//				logger.log(Level.INFO, e.getMessage(), e);
//			}
//			catch (IOException e) {
//				logger.log(Level.INFO, e.getMessage(), e);
//			}
//			finally {
//				if (reader != null)
//					try {
//						reader.close();
//					}
//					catch (IOException logOrIgnore) {
//					}
//			}
//
//			writer.println("\r\n--" + boundary + "--");
//		}
//		catch (UnsupportedEncodingException e) {
//			logger.log(Level.INFO, e.getMessage(), e);
//		}
//		catch (IOException e) {
//			logger.log(Level.INFO, e.getMessage(), e);
//		}
//		finally {
//			if (writer != null)
//				writer.close();
//		}
//
//		// Connection is lazily executed whenever you request any status.
//		int responseCode = ((HttpURLConnection) connection).getResponseCode();
//		logger.info("GOT HTTP STATUS: " + responseCode); // Should be 200
//
//		InputStream content = (InputStream) connection.getInputStream();
//		BufferedReader in = new BufferedReader(new InputStreamReader(content));
//		String jsonText = in.readLine();
//		logger.info("JSON:" + jsonText);
//		return JSONObject.fromObject(jsonText);
//	}

	public Plugin checkForPlugin(LiferayServerMap liferayServer, String pluginName) {

		try {
			Map classMap = new HashMap();
			classMap.put("output", Output.class);

			return (Plugin) JSONObject.toBean(
				readJsonFromUrl(
					liferayServer.getLiferayServerUrlForPlugin(pluginName), 
					liferayServer.getLiferayUserName(), 
					liferayServer.getLiferayUserPassword(),
					RequestMethod.GET), 
				Plugin.class, 
				classMap);

		}
		catch (JSONException e) {
			logger.log(Level.INFO, e.getMessage(), e);
		}
		catch (IOException e) {
			logger.log(Level.INFO, e.getMessage(), e);
		}
		return null;
	}

	public JSONObject readJsonFromUrl(String liferayUrl, String username, String password, RequestMethod requestMethod)
		throws IOException, JSONException {

		try {
			logger.info("CALLING:" + liferayUrl);
			URL url = new URL(liferayUrl);
			Base64Encoder encoder = new Base64Encoder();
			String encoding = encoder.encode((username + ":" + password).getBytes());

			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(requestMethod.toString());
			connection.setDoOutput(true);
			connection.setRequestProperty("Authorization", "Basic " + encoding);

			InputStream content = (InputStream) connection.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(content));
			String jsonText = in.readLine();
			JSONObject json = JSONObject.fromObject(jsonText);
			logger.info("RETUNING: " + json);
			return json;
		}
		catch (Exception e) {
			logger.log(Level.INFO, e.getMessage(), e);
		}
		return null;
	}

	public Object installApplication(String absolutePath, String appName) {

		try {
			FileBody fileBody = new FileBody(new File(absolutePath));

			MultipartEntity entity = new MultipartEntity();
			entity.addPart("deployWar", fileBody); //$NON-NLS-1$

			HttpPost httpPost = new HttpPost();
			httpPost.setEntity(entity);

			Object response = httpJSONAPI(httpPost, getDeployURI(appName));

			if (response instanceof JSONObject) {
				JSONObject json = (JSONObject) response;

				if (isSuccess(json)) {
					System.out.println("installApplication: Sucess.\n\n"); //$NON-NLS-1$
				}
				else {
					if (isError(json)) {
						return json.getString("error"); //$NON-NLS-1$
					}
					else {
						return "installApplication error " + getDeployURI(appName); //$NON-NLS-1$
					}
				}
			}

			httpPost.releaseConnection();
		}
		catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}

		return null;
	}

	private boolean isSuccess(JSONObject jsonObject)
		throws JSONException {

		String success = jsonObject.getString("status"); //$NON-NLS-1$
		return "0".equals(success); //$NON-NLS-1$
	}

	public boolean isError(JSONObject jsonObject) {

		try {
			final String error = jsonObject.getString("error"); //$NON-NLS-1$

			return !CoreUtil.isNullOrEmpty(error);
		}
		catch (JSONException e) {
		}

		return false;
	}

	private String getPluginsAPI() {

		return managerContextPath + "/plugins"; //$NON-NLS-1$
	}

	private String getDeployURI(String appName) {

		return getPluginsAPI() + "/" + appName; //$NON-NLS-1$
	}

	private String getFMDebugPasswordAPI() {

		return managerContextPath + "/server/freemarker/debug-password"; //$NON-NLS-1$
	}

	private String getFMDebugPortAPI() {

		return managerContextPath + "/server/freemarker/debug-port"; //$NON-NLS-1$
	}

	private String getIsAliveAPI() {

		return managerContextPath + "/status"; //$NON-NLS-1$
	}

	private String getJSONOutput(JSONObject json)
		throws JSONException {

		return json.getString("output"); //$NON-NLS-1$
	}

	private Object getJSONResponse(String response) {

		Object retval = null;

		try {
			retval = JSONObject.fromObject(response);
		}
		catch (JSONException e) {
			try {
				retval = JSONArray.fromObject(response);
			}
			catch (JSONException e1) {
			}
		}

		return retval;
	}

	protected Object httpJSONAPI(Object... args)
		throws APIException {

		if (!(args[0] instanceof HttpRequestBase)) {
			throw new IllegalArgumentException("First argument must be a HttpRequestBase."); //$NON-NLS-1$
		}

		Object retval = null;
		String api = null;
		Object[] params = new Object[0];

		final HttpRequestBase request = (HttpRequestBase) args[0];

		if (args[1] instanceof String) {
			api = args[1].toString();
		}
		else if (args[1] instanceof Object[]) {
			params = (Object[]) args[1];
			api = params[0].toString();
		}
		else {
			throw new IllegalArgumentException("2nd argument must be either String or Object[]"); //$NON-NLS-1$
		}

		try {
			final URIBuilder builder = new URIBuilder();
			builder.setScheme("http"); //$NON-NLS-1$
			builder.setHost(getHost());
			builder.setPort(getHttpPort());
			builder.setPath(api);

			if (params.length >= 2) {
				for (int i = 0; i < params.length; i += 2) {
					String name = null;
					String value = "";

					if (params[i] != null) {
						name = params[i].toString();
					}

					if (params[i + 1] != null) {
						value = params[i + 1].toString();
					}

					builder.setParameter(name, value);
				}
			}

			request.setURI(builder.build());

			String response = getHttpResponse(request);

			if (response != null && response.length() > 0) {
				Object jsonResponse = getJSONResponse(response);

				if (jsonResponse == null) {
					throw new APIException(api, "Unable to get response: " + response); //$NON-NLS-1$
				}
				else {
					retval = jsonResponse;
				}
			}
		}
		catch (APIException e) {
			throw e;
		}
		catch (Exception e) {
			throw new APIException(api, e);
		}
		finally {
			try {
				request.releaseConnection();
			}
			finally {
				// no need to log error
			}
		}

		return retval;
	}

	protected String getHttpResponse(HttpUriRequest request)
		throws Exception {

		HttpResponse response = getHttpClient().execute(request);
		int statusCode = response.getStatusLine().getStatusCode();

		if (statusCode == HttpStatus.SC_OK) {
			HttpEntity entity = response.getEntity();

			String body = CoreUtil.readStreamToString(entity.getContent());

			EntityUtils.consume(entity);

			return body;
		}
		else {
			return response.getStatusLine().getReasonPhrase();
		}
	}

	private HttpClient getHttpClient() {

		if (this.httpClient == null) {
			DefaultHttpClient newDefaultHttpClient = null;

			if (getUsername() != null || getPassword() != null) {

				SchemeRegistry schemeRegistry = new SchemeRegistry();
				schemeRegistry.register(new Scheme("http", getHttpPort(), PlainSocketFactory.getSocketFactory())); //$NON-NLS-1$

				PoolingClientConnectionManager cm = new PoolingClientConnectionManager(schemeRegistry);
				cm.setMaxTotal(200);
				cm.setDefaultMaxPerRoute(20);

				DefaultHttpClient newHttpClient = new DefaultHttpClient(cm);
				HttpHost proxy = new HttpHost(getHost(), getHttpPort());

				newHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);

				newDefaultHttpClient = newHttpClient;

				newDefaultHttpClient.getCredentialsProvider().setCredentials(
					new AuthScope(getHost(), getHttpPort()), new UsernamePasswordCredentials(getUsername(), getPassword()));

				this.httpClient = newDefaultHttpClient;
			}
		}

		return this.httpClient;
	}

	private int getHttpPort() {

		return Integer.parseInt(getPort());
	}

	public String getHost() {

		return host;
	}

	public String getPort() {

		return port;
	}

	public String getUsername() {

		return username;
	}

	public String getPassword() {

		return password;
	}

}
