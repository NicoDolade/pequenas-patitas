-- Base de datos: pequenas_patitas
-- Trabajo Práctico Integrador N.º 1 - Grupo 21
-- Integrantes: Nicolás Doladé y Zoe Bellusci

CREATE TABLE IF NOT EXISTS mascotas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    contacto VARCHAR(150) NOT NULL,
    ciudad VARCHAR(100) DEFAULT NULL,
    latitud DECIMAL(10, 8) DEFAULT NULL,
    longitud DECIMAL(11, 8) DEFAULT NULL,
    creado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Datos iniciales de prueba para visualizar en el mapa y catálogo
INSERT INTO mascotas (nombre, contacto, ciudad, latitud, longitud) VALUES
('Milo', 'adopciones.tresarroyos@gmail.com', 'Tres Arroyos', -38.37583000, -60.27972000),
('Luna', 'zoe.refugio@gmail.com', 'Bahia Blanca', -38.71959000, -62.27243000),
('Rocky', 'rescates.tandil@gmail.com', 'Tandil', -37.32167000, -59.13316000),
('Bella', 'contacto@patitas.org', 'Necochea', -38.54726000, -58.73675000);
