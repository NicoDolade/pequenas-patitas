// frontend/app.js — Controlador de la SPA de Pequeñas Patitas
// Consume la API REST con fetch (async/await), maneja el estado de la UI y los marcadores de Leaflet.

// Ruta relativa: funciona de manera idéntica tanto en XAMPP como desplegada en la nube
const URL_API = "../api/mascotas.php";

// ----- Inicialización del Mapa con Leaflet -----
const mapa = L.map("mapa").setView([-38.4, -63.6], 5); // Centrado en la región pampeana / centro de Argentina

L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
  maxZoom: 19,
  attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> colaboradores'
}).addTo(mapa);

const capaMarcadores = L.featureGroup().addTo(mapa);
let marcadores = {}; // Diccionario: id_mascota -> objeto L.marker

// Función para mostrar mensajes de estado con estilos
function mostrarMensaje(texto, tipo = "info") {
  const el = document.querySelector("#mensaje");
  if (!texto) {
    el.textContent = "";
    el.className = "mensaje";
    return;
  }
  el.textContent = texto;
  el.className = `mensaje activo ${tipo}`;
}

// Agrega un marcador en el mapa para una mascota con coordenadas válidas
function agregarMarcador(mascota) {
  if (mascota.latitud === null || mascota.longitud === null) return;

  const lat = Number(mascota.latitud);
  const lon = Number(mascota.longitud);

  const contenidoPopup = `
    <div style="font-size:0.95rem; line-height: 1.4;">
      <h3 style="margin:0 0 6px 0; color:#2563eb;">🐾 ${escapeHTML(mascota.nombre)}</h3>
      <p style="margin:3px 0;"><b>Hogar en:</b> ${escapeHTML(mascota.ciudad ?? "Ubicación a convenir")}</p>
      <p style="margin:3px 0;"><b>Contacto:</b> ${escapeHTML(mascota.contacto)}</p>
      <p style="margin:6px 0 0 0; color:#16a34a; font-weight:600;">¡Escribí para coordinar una visita!</p>
    </div>
  `;

  const marcador = L.marker([lat, lon])
    .bindPopup(contenidoPopup)
    .addTo(capaMarcadores);

  marcadores[mascota.id] = marcador;
}

// Función auxiliar para prevenir inyecciones HTML en el popup
function escapeHTML(str) {
  if (!str) return "";
  return String(str)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}

// ----- Operación: Listar Mascotas (GET) -----
async function listarMascotas() {
  try {
    const respuesta = await fetch(URL_API);
    if (!respuesta.ok) {
      throw new Error(`Error del servidor (${respuesta.status})`);
    }
    const mascotas = await respuesta.json();
    renderizarMascotas(mascotas);
  } catch (error) {
    mostrarMensaje(`No se pudo cargar el catálogo: ${error.message}`, "error");
  }
}

function renderizarMascotas(mascotas) {
  const cuerpoTabla = document.querySelector("#cuerpoTabla");
  cuerpoTabla.innerHTML = "";
  capaMarcadores.clearLayers();
  marcadores = {};

  if (mascotas.length === 0) {
    cuerpoTabla.innerHTML = `<tr><td colspan="5" style="text-align:center; color:#64748b; padding:18px;">No hay mascotas registradas actualmente.</td></tr>`;
    return;
  }

  mascotas.forEach(function (mascota) {
    const fila = document.createElement("tr");
    fila.dataset.id = mascota.id;

    fila.innerHTML = `
      <td><strong>#${mascota.id}</strong></td>
      <td class="nombre-celda">${escapeHTML(mascota.nombre)}</td>
      <td class="contacto-celda">${escapeHTML(mascota.contacto)}</td>
      <td class="ciudad-celda">${mascota.ciudad ? escapeHTML(mascota.ciudad) : '<span style="color:#94a3b8;">—</span>'}</td>
      <td>
        <div class="acciones-celda">
          <button class="btn-editar">Editar</button>
          <button class="btn-borrar">Adoptado / Borrar</button>
        </div>
      </td>
    `;

    cuerpoTabla.appendChild(fila);
    agregarMarcador(mascota);
  });

  // Encuadrar el mapa para enfocar todos los hogares registrados
  if (capaMarcadores.getLayers().length > 0) {
    mapa.fitBounds(capaMarcadores.getBounds(), { padding: [50, 50], maxZoom: 9 });
  }
}

// ----- Operación: Alta de Mascota (POST con Servicios Cloud) -----
document.querySelector("#formAlta").addEventListener("submit", async function (evento) {
  evento.preventDefault();
  mostrarMensaje("Guardando y geocodificando ubicación en la nube…", "info");

  const btn = document.querySelector("#btnGuardar");
  btn.disabled = true;

  const nuevaMascota = {
    nombre: document.querySelector("#campoNombre").value.trim(),
    contacto: document.querySelector("#campoContacto").value.trim(),
    ciudad: document.querySelector("#campoCiudad").value.trim(),
  };

  try {
    const respuesta = await fetch(URL_API, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(nuevaMascota),
    });

    if (!respuesta.ok) {
      const errorJson = await respuesta.json();
      mostrarMensaje(`Error ${respuesta.status}: ${errorJson.error || "No se pudo procesar la solicitud"}`, "error");
      btn.disabled = false;
      return;
    }

    const mascotaCreada = await respuesta.json();
    mostrarMensaje(`¡${mascotaCreada.nombre} fue publicado/a con éxito en el catálogo!`, "exito");
    document.querySelector("#formAlta").reset();
    await listarMascotas();

    setTimeout(() => {
      mostrarMensaje("");
    }, 5000);
  } catch (err) {
    mostrarMensaje(`Fallo de conexión: ${err.message}`, "error");
  } finally {
    btn.disabled = false;
  }
});

// ----- Operaciones: Baja (DELETE) y Modificación (PUT) -----
document.querySelector("#cuerpoTabla").addEventListener("click", async function (evento) {
  const fila = evento.target.closest("tr");
  if (!fila) return;
  const id = fila.dataset.id;
  const nombreActual = fila.querySelector(".nombre-celda").textContent;
  const contactoActual = fila.querySelector(".contacto-celda").textContent;
  const ciudadActual = fila.querySelector(".ciudad-celda").textContent.replace("—", "").trim();

  // Caso 1: Borrar (Adoptado)
  if (evento.target.classList.contains("btn-borrar")) {
    const confirma = confirm(`¿Confirmás que '${nombreActual}' ya fue adoptado/a o deseás retirar la publicación?`);
    if (!confirma) return;

    try {
      const respuesta = await fetch(`${URL_API}?id=${id}`, { method: "DELETE" });
      if (!respuesta.ok) {
        const error = await respuesta.json();
        mostrarMensaje(`Error al borrar: ${error.error}`, "error");
        return;
      }

      fila.remove();
      if (marcadores[id]) {
        capaMarcadores.removeLayer(marcadores[id]);
        delete marcadores[id];
      }
      mostrarMensaje(`'${nombreActual}' fue retirado del catálogo. ¡Ojalá tenga un gran hogar!`, "info");
    } catch (err) {
      mostrarMensaje(`Error al intentar borrar: ${err.message}`, "error");
    }
  }

  // Caso 2: Editar datos
  if (evento.target.classList.contains("btn-editar")) {
    const nuevoNombre = prompt("Nombre de la mascota:", nombreActual);
    if (nuevoNombre === null) return;

    const nuevoContacto = prompt("Email / Teléfono de contacto:", contactoActual);
    if (nuevoContacto === null) return;

    const nuevaCiudad = prompt("Ciudad del hogar de tránsito (dejar en blanco para sin cambios):", ciudadActual);
    if (nuevaCiudad === null) return;

    mostrarMensaje("Actualizando datos...", "info");

    try {
      const respuesta = await fetch(`${URL_API}?id=${id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          nombre: nuevoNombre,
          contacto: nuevoContacto,
          ciudad: nuevaCiudad,
        }),
      });

      if (!respuesta.ok) {
        const error = await respuesta.json();
        mostrarMensaje(`Error ${respuesta.status}: ${error.error}`, "error");
        return;
      }

      mostrarMensaje("Datos actualizados correctamente.", "exito");
      await listarMascotas();
    } catch (err) {
      mostrarMensaje(`Fallo al actualizar: ${err.message}`, "error");
    }
  }
});

// Carga inicial
listarMascotas();
