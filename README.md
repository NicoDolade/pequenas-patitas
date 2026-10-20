# 🐾 Pequeñas Patitas — Red Solidaria de Adopción de Mascotas

Trabajo Práctico Integrador N.º 1 — **Aplicaciones Móviles y Cloud Computing**  
Tecnicatura Universitaria en Programación (TUP) — UPSO Sede Tres Arroyos  
Docentes: Eduardo Mónaco · Heber Alonso  

---

## 1. Presentación

- **Nombre de la Aplicación:** Pequeñas Patitas
- **Tema del Proyecto:** Plataforma web fullstack orientada a la publicación, consulta y geolocalización de mascotas (perros y gatos) en adopción responsable y hogares de tránsito. Permite a los interesados explorar el catálogo de animales y visualizar en un mapa interactivo la ubicación de cada refugio u hogar de tránsito para coordinar visitas.
- **Grupo:** N.º 21
- **Integrantes:**
  - Nicolás Doladé
  - Zoe Bellusci
- **URL Pública de la Aplicación (Frontend):** `http://pequenaspatitas.infinityfreeapp.com/`
- **URL Pública de la API REST:** `http://pequenaspatitas.infinityfreeapp.com/api/mascotas.php`

---

## 2. Cómo correr la aplicación en local (con XAMPP)

Para ejecutar el proyecto en un entorno local de desarrollo con Apache y MySQL:

1. **Instalación de archivos:**
   - Clonar o descargar este repositorio dentro de la carpeta pública de XAMPP:  
     `C:\xampp\htdocs\pequenas-patitas\`
2. **Creación de la Base de Datos:**
   - Iniciar los módulos **Apache** y **MySQL** desde el panel de control de XAMPP.
   - Ingresar a phpMyAdmin desde el navegador (`http://localhost/phpmyadmin/`).
   - Crear una nueva base de datos llamada `pequenas_patitas` con cotejamiento `utf8mb4_unicode_ci`.
   - Ir a la pestaña **Importar** (o pestaña **SQL**) y ejecutar el script ubicado en:  
     `sql/base.sql`
3. **Configuración local:**
   - El archivo `api/config.php` ya viene preconfigurado con valores por defecto para XAMPP:
     - Host: `localhost`
     - Puerto: `3306`
     - Base de datos: `pequenas_patitas`
     - Usuario: `root`
     - Contraseña: `""` (vacía)
   - El servicio de email funcionará automáticamente en **modo simulación**, registrando los avisos en el archivo `api/avisos.log`.
4. **Acceso a la aplicación:**
   - Abrir en el navegador: `http://localhost/pequenas-patitas/` (redirige automáticamente a `frontend/index.html`).

---

## 3. Endpoints de la API REST (`api/mascotas.php`)

La API responde exclusivamente con el encabezado `Content-Type: application/json; charset=utf-8` y utiliza verbos HTTP estándar.

| Método | Ruta | Parámetros / Cuerpo recibido | Respuesta devuelta | Códigos de estado HTTP |
|---|---|---|---|---|
| **GET** | `/api/mascotas.php` | Ninguno | Array en formato JSON con todas las mascotas registradas | `200 OK` |
| **GET** | `/api/mascotas.php?id={id}` | Parámetro URL `id` (entero) | Objeto JSON con los datos de la mascota solicitada | `200 OK`<br>`400 Bad Request` (id no numérico)<br>`404 Not Found` (no existe) |
| **POST** | `/api/mascotas.php` | Objeto JSON:<br>`{ "nombre": string, "contacto": string, "ciudad": string }` | Objeto JSON de la mascota creada, con su `id` asignado y las coordenadas calculadas (`latitud`, `longitud`) | `201 Created`<br>`400 Bad Request` (faltan campos obligatorios)<br>`422 Unprocessable Entity` (ciudad no encontrada)<br>`500 Internal Server Error` (falla de BD) |
| **PUT** | `/api/mascotas.php?id={id}` | Parámetro URL `id` y cuerpo JSON con campos a modificar:<br>`{ "nombre": string, "contacto": string, "ciudad": string }` | Objeto JSON actualizado | `200 OK`<br>`400 Bad Request` (datos inválidos)<br>`404 Not Found` (recurso inexistente)<br>`422 Unprocessable Entity` (nueva ciudad inválida) |
| **DELETE** | `/api/mascotas.php?id={id}` | Parámetro URL `id` (entero) | Sin contenido (cuerpo vacío) | `204 No Content`<br>`400 Bad Request`<br>`404 Not Found` |
| **OPTIONS** | `/api/mascotas.php` | Ninguno (solicitud preflight de CORS) | Encabezados de autorización de métodos | `200 OK` |

---

## 4. Variables de Entorno

Toda la configuración sensible o dependiente del servidor se administra mediante **variables de entorno** leídas con la función nativa `getenv()`, evitando almacenar credenciales en el código fuente:

| Variable | Descripción y Propósito |
|---|---|
| `DB_HOST` | Nombre de host o dirección del servidor MySQL en la nube (ej. asignado por Railway). |
| `DB_PORT` | Puerto de conexión a MySQL (ej. `3306` o el puerto TCP dinámico provisto). |
| `DB_NAME` | Nombre de la base de datos asignada para la aplicación. |
| `DB_USER` | Usuario con permisos de lectura/escritura en la base de datos. |
| `DB_PASS` | Contraseña segura del usuario de la base de datos. |
| `RESEND_API_KEY` | Clave secreta de la API de Resend (`re_...`) para el envío de correos electrónicos. Si no se provee, la app opera en **modo simulación**. |
| `EMAIL_AVISOS` | Dirección de correo electrónico verificada en Resend a donde se despachan las notificaciones de nuevos ingresos. |
| `ENTORNO` | Define el contexto de ejecución (`local` o `produccion`). En `produccion`, desactiva `display_errors` para proteger la información interna del servidor. |

> **Nota de Seguridad:** Ningún valor confidencial o token real se encuentra versionado en el repositorio ni embebido en el código frontend de JavaScript.

---

## 5. Integración con Servicios Cloud Externos

El sistema integra **tres servicios en la nube**, desacoplados en el módulo `api/servicios.php` y en el frontend:

### 1. Geocodificación: Open-Meteo Geocoding API
- **Quién lo llama y por qué:** Lo llama el **backend (PHP)** mediante cURL. Se eligió llamarlo desde el servidor para centralizar la validación antes del `INSERT` en la base de datos y evitar exponer peticiones innecesarias desde el cliente.
- **Datos enviados:** Únicamente el nombre de la ciudad ingresada por el usuario (sin información sensible o identificatoria).
- **Manejo de fallas y degradación elegante:** Si el servicio externo no encuentra la localidad ingresada, la API responde con código `422 Unprocessable Entity` para que el usuario corrija la escritura. Si el servicio de Open-Meteo se encuentra caído o inaccesible (timeout / sin red), la aplicación **degrada elegantemente**: la mascota se registra y se persiste en la base de datos con coordenadas en `null`, garantizando que la funcionalidad principal nunca se interrumpa.

### 2. Mapa Interactivo: Leaflet + OpenStreetMap
- **Quién lo llama y por qué:** Lo llama el **navegador (frontend JavaScript)**. Leaflet se carga desde la red de distribución de contenidos (CDN) de unpkg y solicita las teselas cartográficas directamente a los servidores de OpenStreetMap.
- **Datos enviados:** Coordenadas de latitud y longitud numéricas para posicionar los marcadores geográficos.
- **Manejo de fallas:** Si el cliente pierde conectividad con el servicio de mapas, la tabla con el catálogo de mascotas continúa disponible e interactiva, permitiendo seguir consultando, dando de alta o editando datos.

### 3. Notificaciones por Correo: Resend API
- **Quién lo llama y por qué:** Lo llama el **backend (PHP)** mediante una petición HTTP POST autenticada con cabecera `Authorization: Bearer`. Se ejecuta en el backend para resguardar la API Key privada.
- **Datos enviados:** Nombre del animal, ciudad del refugio y correo de contacto para coordinar la visita.
- **Manejo de fallas:** Si la clave no está configurada o el servicio de email rechaza el envío, la función atrapa el error y la mascota queda guardada normalmente en la base de datos (respondiendo `201 Created`). Además, si `RESEND_API_KEY` está vacía, el aviso se guarda en `api/avisos.log`.

---

## 6. Despliegue en la Nube

- **Proveedor Cloud en Producción:** **InfinityFree** (siguiendo la contingencia oficial prevista por la cátedra en la Semana 10 debido al incidente global de Railway del 20/10/2026 reportado en status.railway.com).
- **Compatibilidad PaaS:** Incluye `Dockerfile` listo para aprovisionamiento directo en **Railway** o Render.
- **Modelo de Servicio (NIST):** **PaaS (Platform as a Service)**.
  - *Justificación:* Permite desplegar aplicaciones web completas abstrayendo la administración del sistema operativo subyacente y las actualizaciones de parches de seguridad. Proporciona aprovisionamiento automatizado mediante contenedores Docker, gestión nativa de certificados SSL (HTTPS obligatorio), base de datos MySQL administrada y configuración directa de variables de entorno desde el panel de control.
- **Pasos para volver a desplegar:**
  1. Conectar el repositorio de GitHub al proyecto en Railway.
  2. Railway detecta automáticamente el archivo `Dockerfile` (configurado con PHP 8.2, Apache y la extensión `pdo_mysql`).
  3. Aprovisionar un servicio MySQL dentro del mismo proyecto de Railway.
  4. Vincular las variables de entorno en el servicio web:
     - `DB_HOST = ${{MySQL.MYSQLHOST}}`
     - `DB_PORT = ${{MySQL.MYSQLPORT}}`
     - `DB_NAME = ${{MySQL.MYSQLDATABASE}}`
     - `DB_USER = ${{MySQL.MYSQLUSER}}`
     - `DB_PASS = ${{MySQL.MYSQLPASSWORD}}`
     - `ENTORNO = produccion`
     - `RESEND_API_KEY = (clave privada)`
     - `EMAIL_AVISOS = (correo destino)`
  5. Ejecutar el script `sql/base.sql` en la base de datos de Railway desde el cliente MySQL o consola web.
  6. Generar el dominio público con HTTPS en la sección *Networking*.

---

## 7. Limitaciones Conocidas

1. **Planes gratuitos con suspensión (Cold Starts):** Si la aplicación permanece inactiva por un período prolongado en un plan PaaS gratuito, la primera petición puede demorar entre 15 y 30 segundos mientras el contenedor se reactiva.
2. **Persistencia de archivos efímeros en contenedor:** En la plataforma de despliegue basada en contenedores, el sistema de archivos del servidor es efímero; por tanto, los registros de simulación en `avisos.log` se reinician si el contenedor se reconstruye. Los datos del catálogo no sufren pérdidas ya que residen en el servicio gestionado de MySQL.
3. **Imágenes fotográficas:** En esta versión, las mascotas se identifican mediante sus nombres, datos de contacto y localización. La carga y almacenamiento de fotografías en un bucket S3 queda planificada para futuras iteraciones del proyecto.

---

## 8. Aportes de los Integrantes (Evidencia de Participación)

- **Nicolás Doladé:**
  - Configuración del entorno base, `Dockerfile` y scripts de inicialización SQL (`sql/base.sql`).
  - Implementación de la arquitectura de conexión segura PDO y lectura de entorno (`api/config.php` y `api/conexion.php`).
  - Desarrollo de la interfaz de usuario en el frontend (`frontend/index.html` y `frontend/estilos.css`) y ensamblado de la capa cartográfica interactiva de Leaflet (`frontend/app.js`).
- **Zoe Bellusci:**
  - Desarrollo del módulo de integración con servicios externos en el backend (`api/servicios.php`): cliente HTTP cURL, integración con Open-Meteo y pasarela de emails de Resend.
  - Implementación de los controladores y validaciones REST en `api/mascotas.php` (respuestas JSON, códigos 200, 201, 400, 422 y degradación elegante).
  - Configuración del enrutamiento inicial (`index.php`) y elaboración de la documentación técnica de endpoints y arquitectura en el `README.md`.
