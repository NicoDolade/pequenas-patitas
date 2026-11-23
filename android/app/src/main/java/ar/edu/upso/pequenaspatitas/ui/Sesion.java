package ar.edu.upso.pequenaspatitas.ui;

import android.content.Context;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

final class Sesion {
    private Sesion() {
    }

    static FirebaseAuth autenticacion(Context contexto) {
        return FirebaseApp.getApps(contexto).isEmpty() ? null : FirebaseAuth.getInstance();
    }

    static boolean requerir(AppCompatActivity actividad) {
        FirebaseAuth auth = autenticacion(actividad);
        if (auth != null && auth.getCurrentUser() != null) {
            return true;
        }
        volverAlIngreso(actividad);
        return false;
    }

    static void volverAlIngreso(AppCompatActivity actividad) {
        Intent ingreso = new Intent(actividad, IngresoActivity.class);
        ingreso.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        actividad.startActivity(ingreso);
        actividad.finish();
    }
}
