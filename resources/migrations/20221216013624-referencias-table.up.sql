CREATE TABLE IF NOT EXISTS referencias (
    id UUID PRIMARY KEY,
    tipo_publicacion VARCHAR,
    titulo VARCHAR,
    editorial VARCHAR(40),
    ciudad VARCHAR(40),
    ano VARCHAR(4),
    volumen VARCHAR(6),
    nombre_revista VARCHAR,
    nombre_libro VARCHAR
);