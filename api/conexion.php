<?php
require_once "config.php";

// En producción, los errores de PHP van al log.
if (ENTORNO === "produccion") {
    ini_set("display_errors", "0");
    ini_set("log_errors", "1");
}

try {
    $conexion = new PDO(
        "mysql:host=" . DB_HOST . ";port=" . DB_PORT . ";dbname=" . DB_NAME . ";charset=utf8mb4",
        DB_USER,
        DB_PASS,
        [PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION]
    );
} catch (PDOException $e) {
    error_log("Conexión fallida a MySQL: " . $e->getMessage());
    http_response_code(500);
    header("Content-Type: application/json; charset=utf-8");
    echo json_encode(["error" => "No se pudo conectar a la base de datos"]);
    exit();
}
