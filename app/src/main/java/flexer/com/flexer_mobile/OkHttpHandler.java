package flexer.com.flexer_mobile;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

import android.os.AsyncTask;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class OkHttpHandler extends AsyncTask<String, Void, String> {

	OkHttpClient client = new OkHttpClient();
	String userName, passWord;

	public OkHttpHandler(String userName, String passWord) {
		// TODO Auto-generated constructor stub
		this.userName = userName;
		this.passWord = passWord;
	}

	@Override
	protected String doInBackground(String... params) {

		client.setAuthenticator(new Authenticator() {
			@Override
			public Request authenticate(Proxy proxy, Response response) {
				String credential = Credentials.basic(userName, passWord);
				return response.request().newBuilder()
						.header("Authorization", credential).build();
			}

			@Override
			public Request authenticateProxy(Proxy proxy, Response response) {
				return null; // Null indicates no attempt to authenticate.
			}
		});

	/*	Request request = new Request.Builder().url(params[0]).build();
*/
		Response response;

		try {
			switch (params[1]){
				case "GET":
					response = methodGet(params[0]);
					break;
				case "PUT":
					response = methodPut(params[0],params[2]);
					break;
				default:
					response = methodGet(params[0]);
					break;
			}


			if (!response.isSuccessful()) {
				throw new IOException("Unexpected code " + response);
			}
			return response.body().string();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "failed to request";
		}
	}

	private Response methodGet(String url) throws IOException {
		Request request = new Request.Builder().url(url).build();
		return client.newCall(request).execute();
	}

	private Response methodPut(String url, String res) throws IOException {
		MediaType JSON = MediaType.parse("application/json; charset=utf-8");
		RequestBody body = RequestBody.create(JSON, res);
		Request request = new Request.Builder()
				.url(url)
				.put(body)
				.build();
		return client.newCall(request).execute();
	}
}