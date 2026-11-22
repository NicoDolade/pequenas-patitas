package ar.edu.upso.pequenaspatitas.ui;

import android.content.Context;
import androidx.annotation.StringRes;

final class EstadoSolicitud<T> {
    final boolean cargando;
    final T datos;
    private final int recursoMensaje;
    private final Object detalle;

    private EstadoSolicitud(boolean cargando, T datos, int recursoMensaje, Object detalle) {
        this.cargando = cargando;
        this.datos = datos;
        this.recursoMensaje = recursoMensaje;
        this.detalle = detalle;
    }

    static <T> EstadoSolicitud<T> cargando() {
        return new EstadoSolicitud<>(true, null, 0, null);
    }

    static <T> EstadoSolicitud<T> completada(T datos) {
        return new EstadoSolicitud<>(false, datos, 0, null);
    }

    static <T> EstadoSolicitud<T> error(@StringRes int recursoMensaje, Object detalle) {
        return new EstadoSolicitud<>(false, null, recursoMensaje, detalle);
    }

    String mensaje(Context contexto) {
        if (recursoMensaje == 0) {
            return "";
        }
        return detalle == null
                ? contexto.getString(recursoMensaje)
                : contexto.getString(recursoMensaje, detalle);
    }
}
