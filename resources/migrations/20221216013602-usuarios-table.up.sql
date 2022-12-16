CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY,
    correo VARCHAR(120),
    nombre VARCHAR(60),
    cuenta VARCHAR(40),
    clave BYTES
);