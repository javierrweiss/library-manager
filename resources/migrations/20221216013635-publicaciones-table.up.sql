CREATE TABLE IF NOT EXISTS publicaciones (
    id UUID PRIMARY KEY,
    referencia UUID REFERENCES referencias(id) ON DELETE CASCADE,
    autor UUID REFERENCES autores(id) 
);