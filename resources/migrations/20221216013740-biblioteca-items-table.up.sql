CREATE TABLE IF NOT EXISTS biblioteca_items(
    id UUID PRIMARY KEY, 
    biblioteca UUID REFERENCES bibliotecas(id) ON DELETE CASCADE,
    coleccion UUID REFERENCES colecciones(id) ON DELETE CASCADE
);