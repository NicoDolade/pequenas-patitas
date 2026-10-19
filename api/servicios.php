<?php
// api/servicios.php — Módulo desacoplado para consumo de servicios cloud externos.
// La API (mascotas.php) delega la comunicación con proveedores externos a estas funciones.

require_once "config.php";

// ----- Cliente HTTP genérico basado en cURL ("el fetch de PHP") -----
function pedirJSON($url, $metodo = "GET", $cuerpo = null, $cabeceras = []) {
    $curl = curl_init($url);
    curl_setopt($curl, CURLOPT_RETURNTRANSFER, true); // Retornar respuesta como string
    curl_setopt($curl, CURLOPT_TIMEOUT, 5);           // Timeout de 5 segundos
    curl_setopt($curl, CURLOPT_CUSTOMREQUEST, $metodo);

    // Compatibilidad Windows / XAMPP para certificados SSL nativos del sistema
    if (defined("CURLSSLOPT_NATIVE_CA")) {
        curl_setopt($curl, CURLOPT_SSL_OPTIONS, CURLSSLOPT_NATIVE_CA);
    }

    if ($cuerpo !== null) {
        curl_setopt($curl, CURLOPT_POSTFIELDS, json_encode($cuerpo));
        $cabeceras[] = "Content-Type: application/json";
    }

    curl_setopt($curl, CURLOPT_HTTPHEADER, $cabeceras);

    $respuesta = curl_exec($curl);
    $estado = curl_getinfo($curl, CURLINFO_HTTP_CODE);

    if ($respuesta === false) {
        return [0, null];
    }

    return [$estado, json_decode($respuesta, true)];
}

// ----- Servicio 1: Geocodificación (Open-Meteo Geocoding API, sin clave) -----
// Convierte el nombre de una ciudad o localidad en coordenadas (latitud y longitud)
function geocodificar($ciudad) {
    $url = "https://geocoding-api.open-meteo.com/v1/search?" . http_build_query([
        "name"     => $ciudad,
        "count"    => 1,
        "language" => "es",
        "format"   => "json",
    ]);

    [$estado, $datos] = pedirJSON($url);

    if ($estado !== 200) {
        return ["estado" => "sin_servicio"]; // Servicio inaccesible o error de red
    }

    if (empty($datos["results"])) {
        return ["estado" => "no_encontrada"]; // Consulta exitosa pero ciudad inexistente
    }

    $lugar = $datos["results"][0];
    return [
        "estado"   => "ok",
        "latitud"  => $lugar["latitude"],
        "longitud" => $lugar["longitude"],
    ];
}

// ----- Servicio 2: Aviso por Email (Resend API, con clave de API) -----
// Notifica el ingreso de una nueva mascota al catálogo de adopción.
// Cuenta con modo simulación si no se configuró una API key real.
function enviarAviso($mascota) {
    $texto = "¡Nuevo perrito disponible para adopción! Se registró a '" . $mascota["nombre"] 
           . "' en " . ($mascota["ciudad"] ? $mascota["ciudad"] : "ubicación a confirmar") 
           . ". Contacto para coordinar visita: " . $mascota["contacto"] . ".";

    // Modo simulación: si la clave está vacía, se registra en log local
    if (RESEND_API_KEY === "") {
        file_put_contents(__DIR__ . "/avisos.log",
            date("Y-m-d H:i:s") . " [SIMULACIÓN] " . $texto . PHP_EOL, FILE_APPEND);
        return true;
    }

    [$estado, $datos] = pedirJSON(
        "https://api.resend.com/emails",
        "POST",
        [
            "from"    => "Pequeñas Patitas <onboarding@resend.dev>",
            "to"      => [EMAIL_AVISOS],
            "subject" => "Nueva mascota en adopción: " . $mascota["nombre"],
            "html"    => "<p>" . htmlspecialchars($texto) . "</p>",
        ],
        ["Authorization: Bearer " . RESEND_API_KEY]
    );

    if ($estado !== 200) {
        error_log("Fallo al enviar email de aviso (" . $estado . "): " . json_encode($datos));
        return false;
    }

    return true;
}
