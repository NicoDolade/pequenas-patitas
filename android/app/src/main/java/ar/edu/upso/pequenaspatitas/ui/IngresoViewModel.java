package ar.edu.upso.pequenaspatitas.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import ar.edu.upso.pequenaspatitas.R;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public final class IngresoViewModel extends AndroidViewModel {
    private final FirebaseAuth auth;
    private final MutableLiveData<EstadoSolicitud<Boolean>> estado = new MutableLiveData<>();
    private String claveEnMemoria = "";

    public IngresoViewModel(@NonNull Application aplicacion) {
        super(aplicacion);
        auth = Sesion.autenticacion(aplicacion);
    }

    boolean configurado() {
        return auth != null;
    }

    boolean haySesion() {
        return auth != null && auth.getCurrentUser() != null;
    }

    LiveData<EstadoSolicitud<Boolean>> estado() {
        return estado;
    }

    String claveEnMemoria() {
        return claveEnMemoria;
    }

    void conservarClave(String clave) {
        claveEnMemoria = clave;
    }

    void ingresar(String correo, String clave) {
        EstadoSolicitud<Boolean> solicitud = estado.getValue();
        if (auth == null || (solicitud != null && solicitud.cargando)) {
            return;
        }

        estado.setValue(EstadoSolicitud.cargando());
        auth.signInWithEmailAndPassword(correo, clave).addOnCompleteListener(tarea -> {
            if (tarea.isSuccessful()) {
                claveEnMemoria = "";
                estado.setValue(EstadoSolicitud.completada(true));
            } else {
                estado.setValue(EstadoSolicitud.error(mensajeDeError(tarea.getException()), null));
            }
        });
    }

    private static int mensajeDeError(Exception error) {
        if (error instanceof FirebaseNetworkException) {
            return R.string.ingreso_sin_red;
        }
        if (error instanceof FirebaseTooManyRequestsException) {
            return R.string.ingreso_muchos_intentos;
        }
        if (error instanceof FirebaseAuthInvalidCredentialsException
                || error instanceof FirebaseAuthInvalidUserException) {
            return R.string.ingreso_credenciales_invalidas;
        }
        return R.string.ingreso_error;
    }
}
