package ar.edu.upso.pequenaspatitas.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import ar.edu.upso.pequenaspatitas.R;
import ar.edu.upso.pequenaspatitas.api.ApiCliente;
import ar.edu.upso.pequenaspatitas.modelo.Mascota;
import ar.edu.upso.pequenaspatitas.modelo.NuevaMascota;
import com.google.gson.JsonParseException;
import java.io.IOException;
import java.util.List;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class MascotasViewModel extends ViewModel {
    private final MutableLiveData<EstadoSolicitud<List<Mascota>>> listado = new MutableLiveData<>();
    private final MutableLiveData<EstadoSolicitud<Mascota>> detalle = new MutableLiveData<>();
    private final MutableLiveData<EstadoSolicitud<Mascota>> alta = new MutableLiveData<>();
    private Call<?> pendiente;

    LiveData<EstadoSolicitud<List<Mascota>>> listado() {
        return listado;
    }

    LiveData<EstadoSolicitud<Mascota>> detalle() {
        return detalle;
    }

    LiveData<EstadoSolicitud<Mascota>> alta() {
        return alta;
    }

    void listar() {
        if (pendiente == null) {
            ejecutar(ApiCliente.mascotas().listar(), listado, false);
        }
    }

    void cargarDetalle(int id) {
        if (pendiente == null) {
            ejecutar(ApiCliente.mascotas().detalle(id), detalle, false);
        }
    }

    void crear(NuevaMascota mascota) {
        if (pendiente == null && (alta.getValue() == null || alta.getValue().datos == null)) {
            ejecutar(ApiCliente.mascotas().crear(mascota), alta, true);
        }
    }

    private <T> void ejecutar(Call<T> llamada, MutableLiveData<EstadoSolicitud<T>> estado,
                              boolean esAlta) {
        pendiente = llamada;
        estado.setValue(EstadoSolicitud.cargando());
        llamada.enqueue(new Callback<T>() {
            @Override
            public void onResponse(Call<T> call, Response<T> respuesta) {
                pendiente = null;
                if (respuesta.isSuccessful() && respuesta.body() != null) {
                    estado.setValue(EstadoSolicitud.completada(respuesta.body()));
                } else if (respuesta.isSuccessful()) {
                    estado.setValue(EstadoSolicitud.error(R.string.error_respuesta, null));
                } else {
                    estado.setValue(errorHttp(respuesta));
                }
            }

            @Override
            public void onFailure(Call<T> call, Throwable error) {
                pendiente = null;
                if (call.isCanceled()) {
                    return;
                }
                if (error instanceof JsonParseException) {
                    estado.setValue(EstadoSolicitud.error(R.string.error_respuesta, null));
                } else {
                    int mensaje = esAlta ? R.string.error_red_alta : R.string.error_red;
                    estado.setValue(EstadoSolicitud.error(mensaje, null));
                }
            }
        });
    }

    private static <T> EstadoSolicitud<T> errorHttp(Response<T> respuesta) {
        try (ResponseBody cuerpo = respuesta.errorBody()) {
            if (cuerpo != null) {
                String mensaje = new JSONObject(cuerpo.string()).optString("error").trim();
                if (!mensaje.isEmpty()) {
                    return EstadoSolicitud.error(R.string.error_mensaje_api, mensaje);
                }
            }
        } catch (IOException | JSONException ignorado) {
            // Si el servidor no envía un mensaje JSON, mostramos el código HTTP.
        }
        return EstadoSolicitud.error(R.string.error_http, respuesta.code());
    }

    @Override
    protected void onCleared() {
        if (pendiente != null) {
            pendiente.cancel();
        }
    }
}
