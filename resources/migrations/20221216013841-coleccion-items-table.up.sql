CREATE TABLE IF NOT EXISTS coleccion_items(
    id UUID PRIMARY KEY,
    coleccion UUID REFERENCES colecciones(id) ON DELETE CASCADE,
    referencia UUID REFERENCES referencias(id) ON DELETE CASCADE
);