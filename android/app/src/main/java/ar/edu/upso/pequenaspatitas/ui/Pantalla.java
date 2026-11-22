package ar.edu.upso.pequenaspatitas.ui;

import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import ar.edu.upso.pequenaspatitas.R;

final class Pantalla {
    private Pantalla() {
    }

    static void respetarBarras(AppCompatActivity actividad) {
        EdgeToEdge.enable(actividad);
        View raiz = actividad.findViewById(R.id.contenido);
        int izquierda = raiz.getPaddingLeft();
        int arriba = raiz.getPaddingTop();
        int derecha = raiz.getPaddingRight();
        int abajo = raiz.getPaddingBottom();
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (vista, insets) -> {
            int tipos = WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
                    | WindowInsetsCompat.Type.ime();
            Insets bordes = insets.getInsets(tipos);
            vista.setPadding(izquierda + bordes.left, arriba + bordes.top,
                    derecha + bordes.right, abajo + bordes.bottom);
            return insets;
        });
    }
}
