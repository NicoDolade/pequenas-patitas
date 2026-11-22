# Pequeñas Patitas

Catálogo de mascotas en adopción. La web permite publicar, editar y retirar fichas, y muestra en un mapa las localidades de los hogares de tránsito. La app Android consulta el mismo catálogo y permite registrar mascotas.

Grupo 21 · Nicolás Doladé y Zoe Bellusci · Aplicaciones Móviles y Cloud Computing, UPSO

- [Sitio web](https://pequenas-patitas-production.up.railway.app/)
- [API de mascotas](https://pequenas-patitas-production.up.railway.app/api/mascotas.php)
- [Proyecto Android](android/README.md)

## Levantar la web con XAMPP

1. Cloná el repositorio en `C:\xampp\htdocs\pequenas-patitas` y prendé Apache y MySQL.
2. En phpMyAdmin, creá la base `pequenas_patitas` con cotejamiento `utf8mb4_unicode_ci` e importá `sql/base.sql`.
3. Abrí `http://localhost/pequenas-patitas/`. La configuración local de `api/config.php` usa el puerto `3306`, el usuario `root` y contraseña vacía.

## API

| Método | Ruta | Resultado |
|---|---|---|
| GET | `/api/mascotas.php` | Lista de mascotas. Responde `200`. |
| GET | `/api/mascotas.php?id={id}` | Detalle. Responde `200`, `400` si el id es inválido o `404` si no existe. |
| POST | `/api/mascotas.php` | Crea una mascota y devuelve sus datos con `201`. |
| PUT | `/api/mascotas.php?id={id}` | Actualiza los campos enviados. Responde `200` o `404` si la mascota no existe. |
| DELETE | `/api/mascotas.php?id={id}` | Retira la mascota. Responde `204`, `400` o `404`. |
| OPTIONS | `/api/mascotas.php` | Devuelve los encabezados CORS con `200`. |

El alta requiere `nombre` y `contacto` como texto. `ciudad` es opcional. En una edición, enviar una ciudad vacía quita la ubicación. La API devuelve `400` con datos inválidos, `422` cuando Open-Meteo no encuentra la ciudad y `405` con el encabezado `Allow` si el método no está admitido. Los errores incluyen el campo JSON `error`.

## Servicios externos

- **Open-Meteo.** PHP envía el nombre de la ciudad y guarda las coordenadas que recibe. Si el servicio está caído durante un alta, guarda la ficha sin coordenadas.
- **Leaflet y OpenStreetMap.** El navegador muestra los marcadores con las coordenadas guardadas. La tabla del catálogo también se puede usar sin el mapa.
- **Resend.** El backend prepara un aviso con nombre, ciudad y contacto al registrar una mascota. Con `RESEND_API_KEY` y `EMAIL_AVISOS` configuradas, intenta enviarlo por correo. Sin clave, escribe el aviso en `api/avisos.log`, que se puede perder al reconstruir el contenedor.

## Despliegue en Railway

Railway es el PaaS que usamos para desplegar la web. El servicio ejecuta PHP y Apache con el `Dockerfile`. MySQL está en el mismo proyecto y recibe conexiones por la red privada.

| Variable del servicio web | Valor |
|---|---|
| `DB_HOST` | `${{MySQL.MYSQLHOST}}` |
| `DB_PORT` | `${{MySQL.MYSQLPORT}}` |
| `DB_NAME` | `${{MySQL.MYSQLDATABASE}}` |
| `DB_USER` | `${{MySQL.MYSQLUSER}}` |
| `DB_PASS` | `${{MySQL.MYSQLPASSWORD}}` |
| `ENTORNO` | `produccion` |
| `PORT` | `80` |

Para reinstalar con datos existentes, importá el respaldo en MySQL. `sql/base.sql` sirve para iniciar una base de prueba. El dominio HTTPS apunta al puerto 80 del servicio web.

## Aportes al TP1

- **Nicolás Doladé.** Configuración y conexión de MySQL, `Dockerfile`, script SQL, interfaz web y mapa.
- **Zoe Bellusci.** CRUD PHP, integración con Open-Meteo y Resend, entrada a la web y documentación de la API.
