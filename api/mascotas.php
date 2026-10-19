<?php
// api/mascotas.php — API REST para gestión de mascotas en adopción (Pequeñas Patitas)
// Cumple con verbos HTTP estándar, códigos de estado REST y degradación elegante.

require_once "conexion.php";
require_once "servicios.php";

// Orígenes permitidos para peticiones cruzadas (CORS) en desarrollo
$origen = $_SERVER["HTTP_ORIGIN"] ?? "";
$origenesPermitidos = [
    "http://127.0.0.1:5500",
    "http://localhost:5500",
    "http://localhost"
];

if (in_array($origen, $origenesPermitidos, true)) {
    header("Access-Control-Allow-Origin: $origen");
    header("Vary: Origin");
}

header("Content-Type: application/json; charset=utf-8");
header("Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS");
header("Access-Control-Allow-Headers: Content-Type");

// Petición Preflight (CORS)
if ($_SERVER["REQUEST_METHOD"] === "OPTIONS") {
    http_response_code(200);
    exit();
}

$metodo = $_SERVER["REQUEST_METHOD"];

// ============================================================================
// POST: Registrar nueva mascota en adopción
// ============================================================================
if ($metodo === "POST") {
    $cuerpo = json_decode(file_get_contents("php://input"), true);

    // Validación 400: Campos obligatorios
    if (empty(trim($cuerpo["nombre"] ?? "")) || empty(trim($cuerpo["contacto"] ?? ""))) {
        http_response_code(400);
        echo json_encode(["error" => "Los campos 'nombre' y 'contacto' son obligatorios"]);
        exit();
    }

    $ciudad = trim($cuerpo["ciudad"] ?? "");
    $latitud = null;
    $longitud = null;

    // Si se especificó una ciudad, se geocodifica con Open-Meteo
    if ($ciudad !== "") {
        $lugar = geocodificar($ciudad);

        // Validación 422: Ciudad inexistente o no reconocida por el servicio
        if ($lugar["estado"] === "no_encontrada") {
            http_response_code(422);
            echo json_encode(["error" => "No se encontró la ciudad \"$ciudad\""], JSON_UNESCAPED_UNICODE);
            exit();
        }

        if ($lugar["estado"] === "ok") {
            $latitud = $lugar["latitud"];
            $longitud = $lugar["longitud"];
        }
        // "sin_servicio": Degradación elegante: se continúa el alta sin coordenadas
    }

    // Consulta preparada para inserción segura contra SQL Injection
    $consulta = $conexion->prepare(
        "INSERT INTO mascotas (nombre, contacto, ciudad, latitud, longitud)
         VALUES (:nombre, :contacto, :ciudad, :latitud, :longitud)"
    );
    $consulta->execute([
        ":nombre"   => trim($cuerpo["nombre"]),
        ":contacto" => trim($cuerpo["contacto"]),
        ":ciudad"   => $ciudad !== "" ? $ciudad : null,
        ":latitud"  => $latitud,
        ":longitud" => $longitud,
    ]);

    $nuevaMascota = [
        "id"       => (int) $conexion->lastInsertId(),
        "nombre"   => trim($cuerpo["nombre"]),
        "contacto" => trim($cuerpo["contacto"]),
        "ciudad"   => $ciudad !== "" ? $ciudad : null,
        "latitud"  => $latitud,
        "longitud" => $longitud,
    ];

    // Envío de aviso por email (si falla, la mascota ya fue persistida con éxito)
    enviarAviso($nuevaMascota);

    http_response_code(201);
    echo json_encode($nuevaMascota, JSON_UNESCAPED_UNICODE);
    exit();
}

// ============================================================================
// PUT: Modificar datos de una mascota existente
// ============================================================================
if ($metodo === "PUT") {
    if (!isset($_GET["id"]) || !is_numeric($_GET["id"])) {
        http_response_code(400);
        echo json_encode(["error" => "Falta el parámetro id o es inválido"]);
        exit();
    }

    $id = (int) $_GET["id"];

    // Verificar si el recurso existe
    $consultaExiste = $conexion->prepare("SELECT * FROM mascotas WHERE id = :id");
    $consultaExiste->execute([":id" => $id]);
    $mascotaActual = $consultaExiste->fetch(PDO::FETCH_ASSOC);

    if (!$mascotaActual) {
        http_response_code(404);
        echo json_encode(["error" => "No existe una mascota con el ID especificado"]);
        exit();
    }

    $cuerpo = json_decode(file_get_contents("php://input"), true);

    $nuevoNombre = trim($cuerpo["nombre"] ?? $mascotaActual["nombre"]);
    $nuevoContacto = trim($cuerpo["contacto"] ?? $mascotaActual["contacto"]);
    $nuevaCiudad = isset($cuerpo["ciudad"]) ? trim($cuerpo["ciudad"]) : $mascotaActual["ciudad"];

    if ($nuevoNombre === "" || $nuevoContacto === "") {
        http_response_code(400);
        echo json_encode(["error" => "Nombre y contacto no pueden quedar vacíos"]);
        exit();
    }

    $latitud = $mascotaActual["latitud"];
    $longitud = $mascotaActual["longitud"];

    // Si la ciudad cambió y no está vacía, se actualizan coordenadas
    if ($nuevaCiudad !== $mascotaActual["ciudad"]) {
        if ($nuevaCiudad !== "") {
            $lugar = geocodificar($nuevaCiudad);
            if ($lugar["estado"] === "no_encontrada") {
                http_response_code(422);
                echo json_encode(["error" => "No se encontró la ciudad \"$nuevaCiudad\""], JSON_UNESCAPED_UNICODE);
                exit();
            }
            if ($lugar["estado"] === "ok") {
                $latitud = $lugar["latitud"];
                $longitud = $lugar["longitud"];
            }
        } else {
            $nuevaCiudad = null;
            $latitud = null;
            $longitud = null;
        }
    }

    $consultaUpdate = $conexion->prepare(
        "UPDATE mascotas 
         SET nombre = :nombre, contacto = :contacto, ciudad = :ciudad, latitud = :latitud, longitud = :longitud 
         WHERE id = :id"
    );
    $consultaUpdate->execute([
        ":nombre"   => $nuevoNombre,
        ":contacto" => $nuevoContacto,
        ":ciudad"   => $nuevaCiudad,
        ":latitud"  => $latitud,
        ":longitud" => $longitud,
        ":id"       => $id,
    ]);

    http_response_code(200);
    echo json_encode([
        "id"       => $id,
        "nombre"   => $nuevoNombre,
        "contacto" => $nuevoContacto,
        "ciudad"   => $nuevaCiudad,
        "latitud"  => $latitud,
        "longitud" => $longitud,
    ], JSON_UNESCAPED_UNICODE);
    exit();
}

// ============================================================================
// DELETE: Borrar mascota (cuando fue adoptada)
// ============================================================================
if ($metodo === "DELETE") {
    if (!isset($_GET["id"]) || !is_numeric($_GET["id"])) {
        http_response_code(400);
        echo json_encode(["error" => "Falta el parámetro id o es inválido"]);
        exit();
    }

    $id = (int) $_GET["id"];

    $consulta = $conexion->prepare("DELETE FROM mascotas WHERE id = :id");
    $consulta->execute([":id" => $id]);

    if ($consulta->rowCount() === 0) {
        http_response_code(404);
        echo json_encode(["error" => "No existe una mascota con el ID especificado"]);
        exit();
    }

    http_response_code(204);
    exit();
}

// ============================================================================
// GET: Obtener una mascota por ID
// ============================================================================
if (isset($_GET["id"])) {
    if (!is_numeric($_GET["id"])) {
        http_response_code(400);
        echo json_encode(["error" => "El ID debe ser numérico"]);
        exit();
    }

    $consulta = $conexion->prepare("SELECT * FROM mascotas WHERE id = :id");
    $consulta->execute([":id" => (int)$_GET["id"]]);
    $mascota = $consulta->fetch(PDO::FETCH_ASSOC);

    if (!$mascota) {
        http_response_code(404);
        echo json_encode(["error" => "No existe una mascota con ese id"]);
        exit();
    }

    echo json_encode($mascota, JSON_UNESCAPED_UNICODE);
    exit();
}

// ============================================================================
// GET por defecto: Listado completo de mascotas en adopción
// ============================================================================
$consulta = $conexion->query("SELECT * FROM mascotas ORDER BY id DESC");
$mascotas = $consulta->fetchAll(PDO::FETCH_ASSOC);
echo json_encode($mascotas, JSON_UNESCAPED_UNICODE);
