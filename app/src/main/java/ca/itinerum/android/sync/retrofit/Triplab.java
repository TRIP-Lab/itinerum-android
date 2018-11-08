package ca.itinerum.android.sync.retrofit;

import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;

import ca.itinerum.android.BuildConfig;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class Triplab {

	/**
	 * All API data requests are made through this URL
	 */
	public static final String sBaseURL = BuildConfig.BUILD_TYPE.equals("alpha") ? "https://api.testing.itinerum.ca" : "https://api.itinerum.ca";

	/***
	 * Static assets are accessible through the dashboard URL
	 */
	public static final String sDashboardBaseURL = BuildConfig.DASHBOARD_URL;

	private final TriplabAPI mAPI;

	public Triplab() {

		if (sBaseURL.equals("put upload server url here!")) throw new NotImplementedException("You must implement an upload URL to a server in settings.gradle");
		if (sDashboardBaseURL.equals("put dashboard server url here!")) throw new NotImplementedException("You must implement a dashboard URL to a server in settings.gradle");

		HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
		logging.setLevel(HttpLoggingInterceptor.Level.BODY);

		OkHttpClient client = new OkHttpClient.Builder()
				.addInterceptor(new Interceptor() {
					@Override
					public Response intercept(Interceptor.Chain chain) throws IOException {
						HttpUrl url = chain.request().url().newBuilder().build();
						Request request = chain.request().newBuilder().url(url).build();
						return chain.proceed(request);
					}
				})
				.addInterceptor(logging)
				.build();

		Retrofit restAdapter = new Retrofit.Builder()
				.baseUrl(sBaseURL + "/mobile/v1/")
				.client(client)
				.addConverterFactory(GsonConverterFactory.create())
				.build();

		mAPI = restAdapter.create(TriplabAPI.class);
	}

	public TriplabAPI getApi() {
		return mAPI;
	}

	public interface TriplabAPI {

		@POST("create")
		Call<CreateResponse> createUser(@Body User user);

		@POST("update")
		Call<UpdateResponse> updateReport(@Body Update update);

	}

}
