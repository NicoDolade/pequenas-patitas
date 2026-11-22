# Pequeñas Patitas para Android

Aplicaciones Móviles y Cloud Computing · TP Integrador 2 · Grupo 21

Nicolás Doladé y Zoe Bellusci

Desde la app se pueden consultar y registrar mascotas en adopción. Las altas quedan en el mismo catálogo que muestra el sitio web.

- [Sitio web](https://pequenas-patitas-production.up.railway.app/)
- [API del TP1](https://pequenas-patitas-production.up.railway.app/api/mascotas.php)

## Cómo correrla

1. Abrí la carpeta `android` en Android Studio.
2. Usá JDK 17 o 21 para Gradle e instalá el SDK de Android 36.
3. Elegí un emulador o conectá un teléfono con Android 7 (API 24) o superior, y ejecutá `app`.

En Windows también podés compilar con `.\gradlew.bat assembleDebug`. La APK queda en `app/build/outputs/apk/debug/app-debug.apk`. Probamos la app en Android 14.

## Pantallas y API

| Pantalla | Uso | Pedido |
|---|---|---|
| Listado | Muestra el catálogo. Desde acá se abre el alta o el detalle. | `GET /api/mascotas.php` |
| Alta | Pide nombre, contacto y, si se conoce, ciudad. Al guardar, vuelve al listado y lo actualiza. | `POST /api/mascotas.php` |
| Detalle | Al tocar una ficha, recibe su ID por un extra del `Intent` y muestra sus datos. | `GET /api/mascotas.php?id={id}` |

La URL base está en [`ApiCliente.java`](app/src/main/java/ar/edu/upso/pequenaspatitas/api/ApiCliente.java). Los métodos de la API están en [`MascotasApi.java`](app/src/main/java/ar/edu/upso/pequenaspatitas/api/MascotasApi.java).

## Decisiones de diseño

Usamos Retrofit y Gson para hacer los pedidos fuera del hilo de la interfaz y convertir las respuestas JSON a objetos Java. El `ViewModel` guarda el estado de una solicitud al girar la pantalla. Durante un alta, el botón Guardar se deshabilita hasta recibir la respuesta.

La app valida nombre y contacto antes de enviar. Si la API responde con un error, muestra su mensaje. Si se corta la conexión durante un alta, pide revisar el listado antes de volver a guardar.

## Alcance actual

La app permite consultar y dar de alta. Para editar o retirar una ficha se usa la web.
