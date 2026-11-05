package com.app.idoneos.repositorio;

import java.util.List;
import java.util.Optional;
import com.app.idoneos.modelo.Bibliografia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para acceder y gestionar la entidad {@link Bibliografia} en la base de datos.
 */
@Repository
public interface BibliografiaRepositorio extends JpaRepository<Bibliografia, Integer> {

    /**
     * Indica si otra bibliografía vigente (de un recurso distinto al indicado) tiene el mismo ISBN, sin distinguir
     * guiones, espacios ni mayúsculas.
     *
     * @param isbnNormalizado el ISBN sin guiones ni espacios y en mayúsculas.
     * @param idRecurso       el recurso que se excluye (el que se está modificando; -1 en un alta).
     */
    @org.springframework.data.jpa.repository.Query("""
            select count(b) > 0 from Bibliografia b
            where b.recurso.baja = false and b.recurso.idRecurso <> :idRecurso
              and upper(replace(replace(b.isbn, '-', ''), ' ', '')) = :isbnNormalizado
            """)
    boolean existeIsbn(@org.springframework.data.repository.query.Param("isbnNormalizado") String isbnNormalizado,
            @org.springframework.data.repository.query.Param("idRecurso") int idRecurso);
}
