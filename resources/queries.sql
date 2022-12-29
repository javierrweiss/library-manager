-- place your sql queries here
-- see https://www.hugsql.org/ for documentation

-- Dialecto SQL: postgresql/CockroachDB

-- Usuarios

-- :name crear-usuario! :<!
-- :doc Crea un usuario
INSERT INTO usuarios (id, correo, nombre, cuenta, clave) 
VALUES(gen_random_uuid(), :usuarios/correo, :usuarios/nombre, :usuarios/cuenta, DIGEST(:usuarios/clave, 'sha384'))
RETURNING id;


-- Autores

-- :name crear-autor! :<!
-- :doc Crea un autor de una publicacion
INSERT INTO autores (id, apellidos, nombres)
VALUES (gen_random_uuid(), :autores/apellidos, :autores/nombres)
RETURNING id;

-- :name obtener-autores-por-referencia :? :n
-- :doc Obtiene los autores de una referencia bibliográfica
SELECT CONCAT(a.apellidos,' ', a.nombres)
FROM autores a
INNER JOIN publicaciones p ON p.autor = a.id
INNER JOIN referencias r ON r.id = p.referencia
WHERE r.id = :referencias/id 


-- Referencias

-- :name crear-referencia! :<!
-- :doc Crea una referencia bibliográfica
INSERT INTO referencias (id, tipo_publicacion, titulo, editorial, ciudad, ano, volumen, nombre_revista, nombre_libro)
VALUES (gen_random_uuid(), :referencia/tipo_publicacion, :referencia/titulo, :referencia/editorial, :referencia/ciudad, 
        :referencia/ano, :referencia/volumen, :referencia/nombre_revista, :referencia/nombre_libro)
RETURNING id;


-- Publicaciones

-- :name crear-publicacion! :<!
-- :doc Crea una publicación
INSERT INTO publicaciones (id, referencia, autor)
VALUES (gen_random_uuid(), :publicaciones/referencia, :publicaciones/autor)
RETURNING id; 


-- Comentarios

-- :name crear-comentario! :<!
-- :doc Crea un comentario realizado por el usuario
INSERT INTO comentarios (id, referencia, comentario, paginas, palabras_clave, usuario)
VALUES(gen_random_uuid(), :comentarios/referencia, :comentarios/comentario, :comentarios/paginas, :comentarios/palabras_clave,
      :comentarios/usuario)
RETURNING id;     

-- :name obtener-comentarios-por-referencia-id
-- :doc Obtiene los comentarios hechos de una referencia bibliográfica
SELECT c.comentario, c.palabras_clave, c.paginas
FROM comentarios c
INNER JOIN referencias r ON r.id = c.referencia
WHERE r.id = :comentarios/referencia


-- Citas

-- :name crear-cita! :<!
-- :doc Crea una cita de una referencia bibliográfica
INSERT INTO citas (id, referencia, cita, paginas, usuario)
VALUES (gen_random_uuid(), :citas/referencia, :citas/cita, :citas/paginas, :citas/usuario)
RETURNING id;

-- :name obtener-citas-por-referencia-id :? :n
-- :doc Recupera las citas de una referencia bibliográfica
SELECT c.cita, c.paginas
FROM citas c
WHERE c.referencia = :citas/referencia


-- Colecciones

-- :name crear-coleccion! :<!
-- :doc Crea una coleccion para el usuario elegido
INSERT INTO colecciones (id, nombre_coll)
VALUES (gen_random_uuid(), :colecciones/nombre_coll)
RETURNING id;


-- Coleccion-items

-- :name crear-item-coleccion! :<!
-- :doc Crea un item dentro de una coleccion
INSERT INTO coleccion_items (id, coleccion, referencia)
VALUES (gen_random_uuid(), :coleccion_items/coleccion, :coleccion_items/referencia)
RETURNING id;

-- :name obtener-referencias-por-coleccion :? :n
-- :doc Devuelve las referencias que pertencen a una misma coleccion
SELECT CONCAT(a.apellidos, ' ', a.nombres), r.tipo_publicacion, r.titulo, r.editorial, r.ciudad, r.ano, r.volumen, r.nombre_revista, 
              r.nombre_libro
FROM referencias r
INNER JOIN publicaciones p ON p.referencia = r.id
INNER JOIN coleccion_items ci ON r.id = ci.referencia
INNER JOIN colecciones c ON c.id = ci.coleccion
LEFT JOIN autores a ON a.id = p.autor
WHERE c.nombre_coll = :colecciones/nombre_coll


-- Bibliotecas

-- :name crear-biblioteca! :<!
-- :doc Crea una biblioteca, es decir, una coleccion de colecciones de referencias bibliográficas
INSERT INTO bibliotecas (id, usuario, nombre_biblioteca)
VALUES (gen_random_uuid(), :bibliotecas/usuario, :bibliotecas/nombre_biblioteca)
RETURNING id;


-- Biblioteca-items

-- :name crear-item-biblioteca! :<!
-- :doc Crea un item para una biblioteca i.e. introduce una colección en una biblioteca
INSERT INTO biblioteca_items (id, biblioteca, coleccion)
VALUES (gen_random_uuid(), :biblioteca_items/biblioteca, :biblioteca_items/coleccion)
RETURNING id;

-- :name obtener-colecciones-por-usuario :? :n
-- :doc Retorna los nombres de las colecciones que pertenecen a un usuario. Recibe como argumento el id de la biblioteca y el
-- id de usuario
SELECT c.nombre_coll
FROM colecciones c
INNER JOIN biblioteca_items bi ON c.id = bi.coleccion
INNER JOIN bibliotecas b ON bi.biblioteca = b.id
WHERE b.usuario = :bibliotecas/usuario AND b.id = :bibliotecas/id


-- Utilitarios

-- :name actualizar-registro! :! :n
/* :doc Actualiza N campos de la tabla que se le pase como parámetro. Ejemplo de uso: 
   (clj-expr-generic-update db {:table "test"
                                :updates {:name "X"}
                                :id 3})
*/
/* :require [clojure.string :as string]
            [hugsql.parameters :refer [identifier-param-quote]] */
UPDATE :i:table SET
/*~
(string/join ","
  (for [[field _] (:updates params)]
    (str (identifier-param-quote (name field) options)
      " = :v:updates." (name field))))
~*/
WHERE id = :id

-- :name borrar-por-id! :! :n
-- :doc Borra N campos de la tabla que se le pase como parámetro. Recibe un hashmap como argumento con las llaves :table y :id
/* :require [clojure.string :as string]
            [hugsql.parameters :refer [identifier-param-quote]] */
DELETE FROM :i:table 
WHERE id = :id            

-- :name obtener-por-id :? :1
-- :doc Obtiene todos los campos de una  tabla que coincidan con el id. Debe recibir en un hashmap :table e :id
SELECT *
FROM :i:table 
WHERE id = :id

-- :name obtener-todo :? :*
-- :doc Trae todos los registros de la tabla pasada como parámetro
SELECT *
FROM :i:table

-- :name contar-registros :? :1
-- :doc Cuenta cuántos registros tiene una tabla
SELECT COUNT(*)
FROM :i:table