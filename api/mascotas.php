<?php
// Orígenes del frontend en desarrollo local.
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

$metodo = $_SERVER["REQUEST_METHOD"];
$metodosPermitidos = ["GET", "POST", "PUT", "DELETE", "OPTIONS"];
$encabezadoMetodos = implode(", ", $metodosPermitidos);

header("Content-Type: application/json; charset=utf-8");
header("Access-Control-Allow-Methods: $encabezadoMetodos");
header("Access-Control-Allow-Headers: Content-Type");

if ($metodo === "OPTIONS") {
    http_response_code(200);
    exit();
}

if (!in_array($metodo, $metodosPermitidos, true)) {
    header("Allow: $encabezadoMetodos");
    http_response_code(405);
    echo json_encode(["error" => "Método no permitido"], JSON_UNESCAPED_UNICODE);
    exit();
}

require_once "conexion.php";
require_once "servicios.php";

function leerDatosMascota() {
    $datos = json_decode(file_get_contents("php://input"));
    if ($datos instanceof stdClass) {
        foreach (["nombre", "contacto", "ciudad"] as $campo) {
            if (isset($datos->$campo) && !is_string($datos->$campo)) {
                http_response_code(400);
                echo json_encode(["error" => "El campo '$campo' debe ser texto"]);
                exit();
            }
        }
        return (array) $datos;
    }

    http_response_code(400);
    echo json_encode(["error" => "El cuerpo debe ser un objeto JSON"]);
    exit();
}

if ($metodo === "POST") {
    $cuerpo = leerDatosMascota();
    $nombre = trim($cuerpo["nombre"] ?? "");
    $contacto = trim($cuerpo["contacto"] ?? "");

    if ($nombre === "" || $contacto === "") {
        http_response_code(400);
        echo json_encode(["error" => "Los campos 'nombre' y 'contacto' son obligatorios"]);
        exit();
    }

    $ciudad = trim($cuerpo["ciudad"] ?? "");
    $latitud = null;
    $longitud = null;

    if ($ciudad !== "") {
        $lugar = geocodificar($ciudad);

        if ($lugar["estado"] === "no_encontrada") {
            http_response_code(422);
            echo json_encode(["error" => "No se encontró la ciudad \"$ciudad\""], JSON_UNESCAPED_UNICODE);
            exit();
        }

        if ($lugar["estado"] === "ok") {
            $latitud = $lugar["latitud"];
            $longitud = $lugar["longitud"];
        }
        // Si Open-Meteo no responde, dejamos las coordenadas vacías.
    }

    $consulta = $conexion->prepare(
        "INSERT INTO mascotas (nombre, contacto, ciudad, latitud, longitud)
         VALUES (:nombre, :contacto, :ciudad, :latitud, :longitud)"
    );
    $consulta->execute([
        ":nombre"   => $nombre,
        ":contacto" => $contacto,
        ":ciudad"   => $ciudad !== "" ? $ciudad : null,
        ":latitud"  => $latitud,
        ":longitud" => $longitud,
    ]);

    $nuevaMascota = [
        "id"       => (int) $conexion->lastInsertId(),
        "nombre"   => $nombre,
        "contacto" => $contacto,
        "ciudad"   => $ciudad !== "" ? $ciudad : null,
        "latitud"  => $latitud,
        "longitud" => $longitud,
    ];

    // La ficha queda guardada aunque falle el aviso.
    enviarAviso($nuevaMascota);

    http_response_code(201);
    echo json_encode($nuevaMascota, JSON_UNESCAPED_UNICODE);
    exit();
}

if ($metodo === "PUT") {
    if (!isset($_GET["id"]) || !is_numeric($_GET["id"])) {
        http_response_code(400);
        echo json_encode(["error" => "Falta el parámetro id o es inválido"]);
        exit();
    }

    $id = (int) $_GET["id"];

    $consultaExiste = $conexion->prepare("SELECT * FROM mascotas WHERE id = :id");
    $consultaExiste->execute([":id" => $id]);
    $mascotaActual = $consultaExiste->fetch(PDO::FETCH_ASSOC);

    if (!$mascotaActual) {
        http_response_code(404);
        echo json_encode(["error" => "No existe una mascota con el ID especificado"]);
        exit();
    }

    $cuerpo = leerDatosMascota();

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

$consulta = $conexion->query("SELECT * FROM mascotas ORDER BY id DESC");
$mascotas = $consulta->fetchAll(PDO::FETCH_ASSOC);
echo json_encode($mascotas, JSON_UNESCAPED_UNICODE);
