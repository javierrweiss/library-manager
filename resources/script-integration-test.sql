CREATE DATABASE referencias_bibliograficas;

CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    correo VARCHAR(120),
    nombre VARCHAR(60),
    cuenta VARCHAR(40),
    clave VARCHAR(40)
);

CREATE TABLE autores (
    id UUID PRIMARY KEY,
    apellidos VARCHAR(80),
    nombres VARCHAR(80)
);

CREATE TABLE referencias (
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

