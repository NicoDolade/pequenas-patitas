package ar.edu.upso.pequenaspatitas.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import ar.edu.upso.pequenaspatitas.R;
import ar.edu.upso.pequenaspatitas.modelo.Mascota;
import ar.edu.upso.pequenaspatitas.modelo.NuevaMascota;

public final class AltaActivity extends AppCompatActivity {
    private EditText nombre;
    private EditText contacto;
    private EditText ciudad;
    private Button guardar;
    private Button volver;
    private ProgressBar progreso;
    private TextView mensaje;
    private MascotasViewModel modelo;
    private final OnBackPressedCallback altaPendiente = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            mensaje.setText(R.string.alta_en_progreso);
            mensaje.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle estadoGuardado) {
        super.onCreate(estadoGuardado);
        setContentView(R.layout.activity_alta);
        Pantalla.respetarBarras(this);

        nombre = findViewById(R.id.nombre);
        contacto = findViewById(R.id.contacto);
        ciudad = findViewById(R.id.ciudad);
        guardar = findViewById(R.id.guardar);
        volver = findViewById(R.id.volver);
        progreso = findViewById(R.id.progreso);
        mensaje = findViewById(R.id.estado);
        modelo = new ViewModelProvider(this).get(MascotasViewModel.class);

        getOnBackPressedDispatcher().addCallback(this, altaPendiente);
        volver.setOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());
        guardar.setOnClickListener(v -> enviar());
        modelo.alta().observe(this, this::mostrar);
    }

    private void enviar() {
        String nombreIngresado = nombre.getText().toString().trim();
        String contactoIngresado = contacto.getText().toString().trim();
        String ciudadIngresada = ciudad.getText().toString().trim();
        if (nombreIngresado.isEmpty()) {
            nombre.setError(getString(R.string.nombre_requerido));
            nombre.requestFocus();
            return;
        }
        if (contactoIngresado.isEmpty()) {
            contacto.setError(getString(R.string.contacto_requerido));
            contacto.requestFocus();
            return;
        }
        modelo.crear(new NuevaMascota(nombreIngresado, contactoIngresado, ciudadIngresada));
    }

    private void mostrar(EstadoSolicitud<Mascota> solicitud) {
        if (solicitud.datos != null) {
            setResult(RESULT_OK);
            finish();
            return;
        }
        altaPendiente.setEnabled(solicitud.cargando);
        guardar.setEnabled(!solicitud.cargando);
        volver.setEnabled(!solicitud.cargando);
        nombre.setEnabled(!solicitud.cargando);
        contacto.setEnabled(!solicitud.cargando);
        ciudad.setEnabled(!solicitud.cargando);
        progreso.setVisibility(solicitud.cargando ? View.VISIBLE : View.GONE);
        String texto = solicitud.cargando ? getString(R.string.alta_en_progreso)
                : solicitud.mensaje(this);
        mensaje.setText(texto);
        mensaje.setVisibility(texto.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
