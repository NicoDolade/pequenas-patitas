package ar.edu.upso.pequenaspatitas.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import ar.edu.upso.pequenaspatitas.R;
import ar.edu.upso.pequenaspatitas.modelo.Mascota;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public final class ListadoActivity extends AppCompatActivity {
    private MascotasViewModel modelo;
    private ArrayAdapter<Mascota> adaptador;
    private ProgressBar progreso;
    private TextView estado;
    private Button recargar;

    private final ActivityResultLauncher<Intent> abrirAlta = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() == RESULT_OK) {
                    Toast.makeText(this, R.string.alta_confirmada, Toast.LENGTH_SHORT).show();
                    modelo.listar();
                }
            });

    @Override
    protected void onCreate(Bundle estadoGuardado) {
        super.onCreate(estadoGuardado);
        if (!Sesion.requerir(this)) {
            return;
        }
        setContentView(R.layout.activity_listado);
        Pantalla.respetarBarras(this);

        FirebaseAuth auth = Sesion.autenticacion(this);
        TextView cuenta = findViewById(R.id.cuenta);
        cuenta.setText(auth.getCurrentUser().getEmail());
        findViewById(R.id.salir).setOnClickListener(v -> {
            auth.signOut();
            Sesion.volverAlIngreso(this);
        });

        modelo = new ViewModelProvider(this).get(MascotasViewModel.class);
        ListView lista = findViewById(R.id.mascotas);
        progreso = findViewById(R.id.progreso);
        estado = findViewById(R.id.estado);
        recargar = findViewById(R.id.recargar);

        adaptador = new ArrayAdapter<Mascota>(this, R.layout.fila_mascota, R.id.fila_nombre) {
            @Override
            public View getView(int posicion, View reciclada, ViewGroup padre) {
                View fila = super.getView(posicion, reciclada, padre);
                Mascota mascota = getItem(posicion);
                TextView nombre = fila.findViewById(R.id.fila_nombre);
                TextView ciudad = fila.findViewById(R.id.fila_ciudad);
                nombre.setText(mascota.getNombre());
                ciudad.setText(mascota.getCiudad() == null
                        ? getString(R.string.sin_ciudad) : mascota.getCiudad());
                return fila;
            }
        };
        lista.setAdapter(adaptador);
        lista.setOnItemClickListener((padre, vista, posicion, filaId) -> {
            Mascota elegida = adaptador.getItem(posicion);
            Intent detalle = new Intent(this, DetalleActivity.class);
            detalle.putExtra(DetalleActivity.EXTRA_MASCOTA_ID, elegida.getId());
            startActivity(detalle);
        });
        findViewById(R.id.nueva).setOnClickListener(v ->
                abrirAlta.launch(new Intent(this, AltaActivity.class)));
        recargar.setOnClickListener(v -> modelo.listar());

        modelo.listado().observe(this, this::mostrar);
        if (modelo.listado().getValue() == null) {
            modelo.listar();
        }
    }

    private void mostrar(EstadoSolicitud<List<Mascota>> solicitud) {
        progreso.setVisibility(solicitud.cargando ? View.VISIBLE : View.GONE);
        recargar.setEnabled(!solicitud.cargando);
        String mensaje = solicitud.mensaje(this);
        if (solicitud.datos != null) {
            adaptador.clear();
            adaptador.addAll(solicitud.datos);
            if (solicitud.datos.isEmpty()) {
                mensaje = getString(R.string.lista_vacia);
            }
        }
        estado.setText(mensaje);
        estado.setVisibility(mensaje.isEmpty() ? View.GONE : View.VISIBLE);
    }
}
