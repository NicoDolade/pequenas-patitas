package ar.edu.upso.pequenaspatitas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import ar.edu.upso.pequenaspatitas.R;

public final class IngresoActivity extends AppCompatActivity {
    private IngresoViewModel modelo;
    private EditText correo;
    private EditText clave;
    private Button ingresar;
    private ProgressBar progreso;
    private TextView estado;

    @Override
    protected void onCreate(Bundle estadoGuardado) {
        super.onCreate(estadoGuardado);
        setContentView(R.layout.activity_ingreso);
        Pantalla.respetarBarras(this);

        modelo = new ViewModelProvider(this).get(IngresoViewModel.class);
        correo = findViewById(R.id.correo);
        clave = findViewById(R.id.clave);
        ingresar = findViewById(R.id.ingresar);
        progreso = findViewById(R.id.progreso);
        estado = findViewById(R.id.estado);

        if (!modelo.configurado()) {
            ingresar.setEnabled(false);
            estado.setText(R.string.firebase_falta_configuracion);
            estado.setVisibility(View.VISIBLE);
            return;
        }

        // La clave sobrevive al giro en memoria, sin guardarse en el Bundle.
        clave.setText(modelo.claveEnMemoria());
        clave.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence texto, int inicio, int cantidad, int despues) {
            }

            @Override
            public void onTextChanged(CharSequence texto, int inicio, int antes, int cantidad) {
                modelo.conservarClave(texto.toString());
            }

            @Override
            public void afterTextChanged(Editable texto) {
            }
        });
        ingresar.setOnClickListener(v -> enviar());
        modelo.estado().observe(this, this::mostrar);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (modelo.haySesion()) {
            mostrarListado();
        }
    }

    private void enviar() {
        String direccion = correo.getText().toString().trim();
        if (!Patterns.EMAIL_ADDRESS.matcher(direccion).matches()) {
            correo.setError(getString(R.string.ingreso_correo_invalido));
            correo.requestFocus();
            return;
        }
        String contrasena = clave.getText().toString();
        if (contrasena.isEmpty()) {
            clave.setError(getString(R.string.ingreso_clave_requerida));
            clave.requestFocus();
            return;
        }
        modelo.ingresar(direccion, contrasena);
    }

    private void mostrar(EstadoSolicitud<Boolean> solicitud) {
        if (Boolean.TRUE.equals(solicitud.datos)) {
            mostrarListado();
            return;
        }
        ingresar.setEnabled(!solicitud.cargando);
        correo.setEnabled(!solicitud.cargando);
        clave.setEnabled(!solicitud.cargando);
        progreso.setVisibility(solicitud.cargando ? View.VISIBLE : View.GONE);
        String mensaje = solicitud.mensaje(this);
        estado.setText(mensaje);
        estado.setVisibility(mensaje.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void mostrarListado() {
        if (!isFinishing()) {
            startActivity(new Intent(this, ListadoActivity.class));
            finish();
        }
    }
}
