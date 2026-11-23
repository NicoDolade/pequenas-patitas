package ar.edu.upso.pequenaspatitas.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import ar.edu.upso.pequenaspatitas.R;
import ar.edu.upso.pequenaspatitas.modelo.Mascota;

public final class DetalleActivity extends AppCompatActivity {
    public static final String EXTRA_MASCOTA_ID = "ar.edu.upso.pequenaspatitas.MASCOTA_ID";
    private TextView nombre;
    private TextView contacto;
    private TextView ciudad;
    private TextView estado;
    private ProgressBar progreso;
    private Button reintentar;

    @Override
    protected void onCreate(Bundle estadoGuardado) {
        super.onCreate(estadoGuardado);
        if (!Sesion.requerir(this)) {
            return;
        }
        setContentView(R.layout.activity_detalle);
        Pantalla.respetarBarras(this);

        nombre = findViewById(R.id.detalle_nombre);
        contacto = findViewById(R.id.detalle_contacto);
        ciudad = findViewById(R.id.detalle_ciudad);
        estado = findViewById(R.id.estado);
        progreso = findViewById(R.id.progreso);
        reintentar = findViewById(R.id.reintentar);
        findViewById(R.id.volver).setOnClickListener(v -> finish());

        int id = getIntent().getIntExtra(EXTRA_MASCOTA_ID, 0);
        if (id <= 0) {
            estado.setText(R.string.error_id);
            estado.setVisibility(View.VISIBLE);
            return;
        }
        MascotasViewModel modelo = new ViewModelProvider(this).get(MascotasViewModel.class);
        reintentar.setOnClickListener(v -> modelo.cargarDetalle(id));
        modelo.detalle().observe(this, this::mostrar);
        if (modelo.detalle().getValue() == null) {
            modelo.cargarDetalle(id);
        }
    }

    private void mostrar(EstadoSolicitud<Mascota> solicitud) {
        progreso.setVisibility(solicitud.cargando ? View.VISIBLE : View.GONE);
        String mensaje = solicitud.mensaje(this);
        estado.setText(mensaje);
        estado.setVisibility(mensaje.isEmpty() ? View.GONE : View.VISIBLE);
        reintentar.setVisibility(mensaje.isEmpty() ? View.GONE : View.VISIBLE);
        Mascota mascota = solicitud.datos;
        if (mascota != null) {
            nombre.setText(mascota.getNombre());
            contacto.setText(getString(R.string.detalle_contacto, mascota.getContacto()));
            String localidad = mascota.getCiudad() == null
                    ? getString(R.string.sin_ciudad) : mascota.getCiudad();
            ciudad.setText(getString(R.string.detalle_ciudad, localidad));
            nombre.setVisibility(View.VISIBLE);
            contacto.setVisibility(View.VISIBLE);
            ciudad.setVisibility(View.VISIBLE);
        }
    }
}
