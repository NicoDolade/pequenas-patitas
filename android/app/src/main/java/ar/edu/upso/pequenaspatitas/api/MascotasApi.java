package ar.edu.upso.pequenaspatitas.api;

import ar.edu.upso.pequenaspatitas.modelo.Mascota;
import ar.edu.upso.pequenaspatitas.modelo.NuevaMascota;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MascotasApi {
    String MASCOTAS = "api/mascotas.php";

    @GET(MASCOTAS)
    Call<List<Mascota>> listar();

    @GET(MASCOTAS)
    Call<Mascota> detalle(@Query("id") int id);

    @POST(MASCOTAS)
    Call<Mascota> crear(@Body NuevaMascota mascota);
}
