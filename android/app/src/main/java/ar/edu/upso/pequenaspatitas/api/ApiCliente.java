package ar.edu.upso.pequenaspatitas.api;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiCliente {
    private static final String URL_BASE = "https://pequenas-patitas-production.up.railway.app/";
    private static final MascotasApi MASCOTAS = new Retrofit.Builder()
            .baseUrl(URL_BASE)
            .client(new OkHttpClient.Builder().retryOnConnectionFailure(false).build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MascotasApi.class);

    private ApiCliente() {
    }

    public static MascotasApi mascotas() {
        return MASCOTAS;
    }
}
