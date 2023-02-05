CREATE TABLE IF NOT EXISTS citas (
    id UUID PRIMARY KEY,
    referencia UUID REFERENCES referencias(id),
    cita VARCHAR,
    paginas VARCHAR(10),
    usuario UUID REFERENCES usuarios(id)
);