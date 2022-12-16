CREATE TABLE IF NOT EXISTS comentarios (
    id UUID PRIMARY KEY,
    referencia UUID REFERENCES referencias(id) ON DELETE CASCADE,
    comentario VARCHAR,
    paginas VARCHAR(10),
    palabras_clave VARCHAR,
    usuario UUID REFERENCES usuarios(id) ON DELETE CASCADE
);