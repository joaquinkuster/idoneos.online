package com.app.idoneos.service.claseenvivo;

import com.app.idoneos.service.CrudService;
import java.util.List;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.ClaseEnVivo;
import com.app.idoneos.model.Cohorte;
import com.app.idoneos.model.Docente;
import com.app.idoneos.service.streaming.StreamingService.DatosDeConexion;
import java.util.Optional;

/** Servicio de la clase en vivo. */
public interface ClaseEnVivoService extends CrudService<ClaseEnVivo> {

    /**
     * Datos que el docente carga en una clase en vivo (alta o modificación).
     *
     * @param idCohorte cohorte a la que se dicta la clase, solo en el alta
     * @param idTema tema que se dicta, solo en el alta
     * @param titulo título de la clase
     * @param fechaHoraInicio fecha y hora de inicio de la transmisión ({@code yyyy-MM-ddTHH:mm})
     * @param fechaHoraCierre fecha y hora de cierre de la transmisión ({@code yyyy-MM-ddTHH:mm}), de la que sale la duración
     */
    record SolicitudClaseEnVivo(Integer idCohorte, Integer idTema, String titulo, String fechaHoraInicio,
            String fechaHoraCierre) {
    }

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /**
     * Busca los registros vigentes de claseEnVivo asociados a cohorte.
     *
     * @param cohorte el registro de Cohorte asociado
     * @return una lista de ClaseEnVivo asociados al Cohorte indicado
     */
    List<ClaseEnVivo> buscarPorCohorte(Cohorte cohorte);

    /**
     * Busca las clases en vivo de un curso (incluidas las dadas de baja) aplicando filtros opcionales.
     *
     * @param idCurso identificador del curso (opcional)
     * @param baja {@code true} solo dadas de baja, {@code false} solo vigentes, {@code null} todas (opcional)
     * @param soloDeDocente si se indica, solo las de los cursos en los que participa ese docente
     * @return las clases en vivo que cumplen los criterios, las vigentes primero y por fecha
     */
    List<ClaseEnVivo> buscarConFiltros(Integer idCurso, Boolean baja, Docente soloDeDocente);

    /**
     * Busca la clase en vivo vigente que identifica un camino del servidor de medios.
     *
     * @param camino el camino, por ejemplo {@code clase-12}
     * @return la clase, o vacío si el camino no es de una clase vigente
     */
    Optional<ClaseEnVivo> buscarPorCamino(String camino);

    /**
     * Obtiene los minutos que se espera la reconexión del docente antes de cerrar sola una clase sin señal (parámetro del sistema).
     *
     * @return los minutos
     */
    int minutosDeEsperaSinSenal();

    /**
     * Obtiene los minutos de anticipación con que se puede iniciar una clase (parámetro del sistema).
     *
     * @return los minutos
     */
    int minutosDeAnticipacion();

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /**
     * Programa una clase en vivo para una cohorte activa.
     *
     * @param docente el docente que la programa, debe participar en el curso como titular o ayudante
     * @param datos los datos de la clase
     * @return la clase programada
     * @throws IllegalArgumentException si la cohorte no está en inscripción próxima, abierta, pendiente de contenido o en dictado,
     *         o los datos no son válidos
     */
    ClaseEnVivo registrar(Docente docente, SolicitudClaseEnVivo datos);

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /**
     * Modifica el título y la fecha y hora de inicio y de cierre de una clase programada.
     *
     * @param idClaseEnVivo identificador de la clase
     * @param docente el docente que la modifica, debe ser quien la programó
     * @param datos los datos de la clase
     * @return la clase actualizada
     * @throws IllegalArgumentException si no está activa ni programada o los datos no son válidos
     */
    ClaseEnVivo modificar(Integer idClaseEnVivo, Docente docente, SolicitudClaseEnVivo datos);

    /**
     * Cancela una clase programada que todavía no se transmitió.
     *
     * @param idClaseEnVivo identificador de la clase
     * @param docente el docente que la cancela, debe ser quien la programó
     * @throws IllegalArgumentException si no está activa ni programada
     */
    void cancelar(Integer idClaseEnVivo, Docente docente);

    /**
     * Cancela varias clases programadas, todas o ninguna.
     *
     * @param ids identificadores de las clases
     * @param docente el docente que las cancela, debe ser quien las programó
     * @throws IllegalArgumentException si no se seleccionó ninguna o alguna no se puede cancelar
     */
    void cancelarVarias(List<Integer> ids, Docente docente);

    /**
     * Cancela las clases programadas que no se iniciaron dentro de la tolerancia de espera (parámetro del sistema).
     *
     * @return la cantidad de clases canceladas
     */
    int cancelarNoIniciadas();

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /**
     * Da de baja una clase en cualquier estado, junto con su grabación si la tiene. Si se está transmitiendo, corta la señal.
     *
     * @param idClaseEnVivo identificador de la clase
     * @throws IllegalArgumentException si no está activa
     */
    void darDeBaja(Integer idClaseEnVivo);

    /**
     * Da de baja varias clases, todas o ninguna.
     *
     * @param ids identificadores de las clases
     * @throws IllegalArgumentException si no se seleccionó ninguna o alguna no está activa
     */
    void darDeBajaVarias(List<Integer> ids);

    // ----------------------------------------------------------------------
    // Transmisión
    // ----------------------------------------------------------------------

    /**
     * Inicia la transmisión de una clase programada: genera los datos de conexión y la pasa al estado En vivo.
     *
     * @param idClaseEnVivo identificador de la clase
     * @param docente el docente que la inicia, debe ser quien la programó
     * @return los datos de conexión que el docente carga en OBS
     * @throws IllegalArgumentException si no está activa ni programada o todavía no es el horario
     */
    DatosDeConexion iniciar(Integer idClaseEnVivo, Docente docente);

    /**
     * Genera una clave de transmisión nueva para una clase en vivo, y corta la señal que usaba la anterior.
     *
     * @param idClaseEnVivo identificador de la clase
     * @param docente el docente que transmite, debe ser quien la programó
     * @return los datos de conexión con la clave nueva
     * @throws IllegalArgumentException si la clase no está en vivo
     */
    DatosDeConexion regenerarClave(Integer idClaseEnVivo, Docente docente);

    /**
     * Finaliza la transmisión: corta la señal, pasa la clase a Finalizada y publica su grabación en el tema de la clase.
     *
     * @param idClaseEnVivo identificador de la clase
     * @param docente el docente que la finaliza, debe ser quien la programó
     * @return la clase finalizada, que tiene grabación si el servidor la generó
     * @throws IllegalArgumentException si la clase no está en vivo o su tema no está activo
     */
    ClaseEnVivo finalizar(Integer idClaseEnVivo, Docente docente);

    /**
     * Busca la clase que el docente está transmitiendo.
     *
     * @param idClaseEnVivo identificador de la clase
     * @param docente el docente, debe ser quien la programó
     * @return la clase en vivo
     * @throws IllegalArgumentException si no está activa ni en vivo, o es de otro docente
     */
    ClaseEnVivo buscarEnVivoDelDocente(Integer idClaseEnVivo, Docente docente);

    /**
     * Verifica que un alumno pueda ingresar a la transmisión de una clase.
     *
     * @param idClaseEnVivo identificador de la clase
     * @param alumno el alumno
     * @return la clase en vivo
     * @throws IllegalArgumentException si no tiene una inscripción habilitada en la cohorte de la clase o la clase no está en vivo
     */
    ClaseEnVivo autorizarIngreso(Integer idClaseEnVivo, Alumno alumno);

    /**
     * Publica la grabación de una clase finalizada como recurso de su tema. Se ejecuta en segundo plano al finalizar la clase,
     * y se puede volver a ejecutar si la clase todavía no tiene grabación.
     *
     * @param idClaseEnVivo identificador de la clase
     * @return la clase, con su grabación si el servidor la generó
     */
    ClaseEnVivo publicarGrabacion(Integer idClaseEnVivo);

    /** Papel con el que un usuario participa en la sala de una clase en vivo. */
    enum RolEnSala {
        DOCENTE, ALUMNO
    }

    /**
     * Obtiene el papel con el que un usuario puede estar en la sala de una clase: el docente que la programó o un alumno
     * con inscripción habilitada en su cohorte.
     *
     * @param clase la clase
     * @param usuario el usuario
     * @return el papel, o vacío si no puede participar
     */
    Optional<RolEnSala> rolEnSala(ClaseEnVivo clase, com.app.idoneos.model.Usuario usuario);

    /**
     * Cierra las clases en vivo cuya señal se cortó hace más del tiempo que se espera la reconexión (parámetro del sistema).
     * Si llegó a grabarse algo la clase queda Finalizada con su grabación, y si nunca llegó señal queda Cancelada.
     *
     * @return la cantidad de clases cerradas
     */
    int cerrarTransmisionesSinSenal();

    /**
     * Elimina las grabaciones que pasaron su fecha de vencimiento.
     *
     * @return la cantidad de grabaciones eliminadas
     */
    int vencerGrabaciones();
}
