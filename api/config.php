<?php
// Los valores por defecto son para XAMPP. Railway los toma del entorno.
function entorno($nombre, $porDefecto = "") {
    $valor = getenv($nombre);
    return $valor !== false ? $valor : $porDefecto;
}

define("DB_HOST", entorno("DB_HOST", "localhost"));
define("DB_PORT", entorno("DB_PORT", "3306"));
define("DB_NAME", entorno("DB_NAME", "pequenas_patitas"));
define("DB_USER", entorno("DB_USER", "root"));
define("DB_PASS", entorno("DB_PASS", ""));

define("RESEND_API_KEY", entorno("RESEND_API_KEY", "")); // vacía = modo simulación (guarda en avisos.log)
define("EMAIL_AVISOS", entorno("EMAIL_AVISOS", "avisos@pequenaspatitas.com"));

define("ENTORNO", entorno("ENTORNO", "local"));

date_default_timezone_set("America/Argentina/Buenos_Aires");
