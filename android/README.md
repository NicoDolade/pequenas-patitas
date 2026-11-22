# Pequeñas Patitas para Android

TP Integrador 2 de Aplicaciones Móviles y Cloud Computing. Grupo 21, Nicolás Doladé y Zoe Bellusci.

Abrí esta carpeta en Android Studio, sincronizá Gradle con JDK 17 o 21 y ejecutá la app en un emulador o teléfono con Android 7 (API 24) o superior. Para compilar desde la terminal, ejecutá `gradlew.bat assembleDebug` en Windows o `./gradlew assembleDebug` en macOS y Linux. El proyecto requiere el SDK de Android 36.

La app usa la [API pública del TP1](https://pequenas-patitas-production.up.railway.app/api/mascotas.php) y comparte los datos con el [sitio web](https://pequenas-patitas-production.up.railway.app/). La URL base está en `ApiCliente.java`.

| Pantalla | Flujo | Operación |
|---|---|---|
| Listado | Muestra las mascotas registradas y abre el alta o el detalle. | `GET /api/mascotas.php` |
| Alta | Valida nombre y contacto, guarda y devuelve un resultado para refrescar el listado. | `POST /api/mascotas.php` |
| Detalle | Recibe el ID en un extra del Intent y muestra la información de la mascota. | `GET /api/mascotas.php?id={id}` |

Retrofit ejecuta los pedidos de forma asíncrona. Los ViewModels conservan el estado de carga al girar el teléfono y el formulario mantiene el texto ingresado. El botón Guardar se deshabilita mientras el envío está pendiente.
