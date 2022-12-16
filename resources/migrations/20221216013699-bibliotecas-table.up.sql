CREATE TABLE IF NOT EXISTS bibliotecas (
    id UUID PRIMARY KEY,
    nombre_biblioteca VARCHAR(60),
    usuario UUID REFERENCES usuarios(id) ON DELETE CASCADE 
);