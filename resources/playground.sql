INSERT INTO usuarios (id, correo, nombre, cuenta, clave) 
VALUES(gen_random_uuid(), 'javierparolle@gmail.com', 'Javier Parolle', 'parolle', DIGEST('chupatelamandarina', 'sha384'))
RETURNING id;

INSERT INTO autores (id, apellidos, nombres)
VALUES (gen_random_uuid(), 'Koselleck', 'Reinhart')
RETURNING id;

SELECT CONCAT(a.apellidos,' ', a.nombres)
FROM autores a
INNER JOIN publicaciones p ON p.autor = a.id
INNER JOIN referencias r ON r.id = p.referencia
WHERE r.id = '8b1d8dad56174d8abbe3afe2d13512ab';

INSERT INTO referencias (id, tipo_publicacion, titulo, editorial, ciudad, ano, volumen, nombre_revista, nombre_libro)
VALUES (gen_random_uuid(), 'Libro', 'Lágrimas de mármol', 'La Cantera', 'Madrid', 
        2022, null, null, null)
RETURNING id;

select * from publicaciones;

INSERT INTO publicaciones (id, referencia, autor)
VALUES (gen_random_uuid(), '8b1d8dad56174d8abbe3afe2d13512ab', 'ebea037716ba4467bc50-4767d5e6034f')
RETURNING id; 

-- 8b1d8dad-5617-4d8a-bbe3-afe2d13512ab
-- ebea0377-16ba-4467-bc50-4767d5e6034f

SELECT * FROM usuarios;

INSERT INTO comentarios (id, referencia, comentario, paginas, palabras_clave, usuario)
VALUES(gen_random_uuid(), '8b1d8dad56174d8abbe3afe2d13512ab', 'Este es un comentario muy tautológico', '34-39', 'chinos, alemanes, japoneses',
      '22e4d9dd2da9421aadb94b65cd8f2529')
RETURNING id;    

SELECT c.comentario, c.palabras_clave, c.paginas
FROM comentarios c
INNER JOIN referencias r ON r.id = c.referencia
WHERE r.id = '8b1d8dad56174d8abbe3afe2d13512ab';

INSERT INTO citas (id, referencia, cita, paginas, usuario)
VALUES (gen_random_uuid(), '8b1d8dad56174d8abbe3afe2d13512ab', 
'Las citas que no dicen nada. ¡Qué pena!', '90-120', '22e4d9dd2da9421aadb94b65cd8f2529')
RETURNING id;

SELECT c.cita, c.paginas
FROM citas c
WHERE c.referencia = '8b1d8dad56174d8abbe3afe2d13512ab';

INSERT INTO colecciones (id, nombre_coll)
VALUES (gen_random_uuid(), 'La más grande')
RETURNING id;

INSERT INTO coleccion_items (id, coleccion, referencia)
VALUES (gen_random_uuid(), '4140573d41fe4ab2abbfef7e8cabff47', '8b1d8dad56174d8abbe3afe2d13512ab')
RETURNING id;

SELECT CONCAT(a.apellidos, ' ', a.nombres), r.tipo_publicacion, r.titulo, r.editorial, r.ciudad, r.ano, r.volumen, r.nombre_revista, 
              r.nombre_libro
FROM referencias r
INNER JOIN publicaciones p ON p.referencia = r.id
INNER JOIN coleccion_items ci ON r.id = ci.referencia
INNER JOIN colecciones c ON c.id = ci.coleccion
LEFT JOIN autores a ON a.id = p.autor
WHERE c.nombre_coll = 'La más grande';

INSERT INTO bibliotecas (id, usuario, nombre_biblioteca)
VALUES (gen_random_uuid(), '22e4d9dd2da9421aadb94b65cd8f2529', 'Muchos libros')
RETURNING id;

INSERT INTO biblioteca_items (id, biblioteca, coleccion)
VALUES (gen_random_uuid(), '733a697d04864cbbbce792e10dd05fd0', '4140573d41fe4ab2abbfef7e8cabff47')
RETURNING id;

SELECT * FROM bibliotecas;

SELECT c.nombre_coll
FROM colecciones c
INNER JOIN biblioteca_items bi ON c.id = bi.coleccion
INNER JOIN bibliotecas b ON bi.biblioteca = b.id
WHERE b.usuario = '22e4d9dd2da9421aadb94b65cd8f2529' AND b.id = '733a697d04864cbbbce792e10dd05fd0';

SELECT r.tipo_publicacion, r.titulo, r.editorial, r.ciudad, r.ano, r.volumen, r.nombre_libro, r.nombre_revista,
       p.autor
FROM referencias r 
INNER JOIN publicaciones p ON p.referencia = r.id
ORDER BY p.autor;