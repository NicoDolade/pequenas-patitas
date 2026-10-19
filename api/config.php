<?php
// api/config.php — Lee variables de entorno del servidor con getenv().
// En local (XAMPP) la variable no existe y toma el valor por defecto.
// En producción (Railway / nube), los valores reales se configuran en el panel.

function entorno($nombre, $porDefecto = "") {
    $valor = getenv($nombre);
    return $valor !== false ? $valor : $porDefecto;
}

// Configuración de la Base de Datos
define("DB_HOST", entorno("DB_HOST", "localhost"));
define("DB_PORT", entorno("DB_PORT", "3306"));
define("DB_NAME", entorno("DB_NAME", "pequenas_patitas"));
define("DB_USER", entorno("DB_USER", "root"));
define("DB_PASS", entorno("DB_PASS", ""));

// Claves de servicios cloud externos
define("RESEND_API_KEY", entorno("RESEND_API_KEY", "")); // vacía = modo simulación (guarda en avisos.log)
define("EMAIL_AVISOS", entorno("EMAIL_AVISOS", "avisos@pequenaspatitas.com"));

// Entorno de ejecución: "local" o "produccion"
define("ENTORNO", entorno("ENTORNO", "local"));

date_default_timezone_set("America/Argentina/Buenos_Aires");
