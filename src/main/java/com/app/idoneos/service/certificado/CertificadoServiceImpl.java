package com.app.idoneos.service.certificado;

import com.lowagie.text.Font;

import com.app.idoneos.model.Alumno;
import com.app.idoneos.model.Certificado;
import com.app.idoneos.model.Docente;
import com.app.idoneos.model.Inscripcion;
import com.app.idoneos.repository.CertificadoRepository;
import com.app.idoneos.service.CrudService;
import com.app.idoneos.util.MensajeErrorUtil;
import com.app.idoneos.util.PdfUtil;
import com.app.idoneos.util.RespuestaFormularioUtil;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfShading;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Implementación de CertificadoService. */
@Service
public class CertificadoServiceImpl implements CertificadoService {

    /** Longitud máxima del nombre (la de la columna). */
    private static final int LONGITUD_NOMBRE = 100;

    private static final Logger REGISTRO = LoggerFactory.getLogger(CertificadoServiceImpl.class);

    @Autowired
    private CertificadoRepository certificadoRepository;

    // ----------------------------------------------------------------------
    // Lecturas
    // ----------------------------------------------------------------------

    /** Busca el certificado por su identificador. */
    @Override
    public Optional<Certificado> buscarPorId(Integer id) {
        return certificadoRepository.findById(id);
    }

    /** Obtiene todos los registros. */
    @Override
    public List<Certificado> obtenerTodo() {
        return certificadoRepository.findByAnuladoFalse();
    }

    /** Verifica si existe un registro con el identificador dado. */
    @Override
    public boolean existePorId(Integer id) {
        return certificadoRepository.existsById(id);
    }

    /** Busca el certificado por su atributo único 'numero'. */
    @Override
    public Optional<Certificado> buscarPorNumero(String numero) {
        return certificadoRepository.findByNumero(numero);
    }

    @Override
    public List<Certificado> buscarVigentesDelAlumno(Alumno alumno) {
        return certificadoRepository.findByInscripcionAlumnoAndAnuladoFalseOrderByFechaEmisionDesc(alumno);
    }

    @Override
    public Certificado buscarVisible(Integer idCertificado, Alumno solicitante) {
        return certificadoRepository.findById(idCertificado == null ? -1 : idCertificado)
                .filter(c -> !c.getAnulado())
                .filter(c -> solicitante == null
                        || c.getInscripcion().getAlumno().getIdAlumno() == solicitante.getIdAlumno())
                .orElseThrow(() -> new IllegalArgumentException("Error! El certificado no se encuentra disponible."));
    }

    @Override
    public byte[] generarPdf(Certificado certificado) {
        DateTimeFormatter formatoFecha = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.of("es", "AR"));
        Rectangle pagina = PageSize.A4.rotate();
        float ancho = pagina.getWidth();
        float alto = pagina.getHeight();
        float centro = ancho / 2;
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        Document documento = new Document(pagina, 0, 0, 0, 0);
        try {
            PdfWriter escritor = PdfWriter.getInstance(documento, salida);
            documento.open();
            PdfContentByte lienzo = escritor.getDirectContent();
            BaseFont negrita = PdfUtil.helvetica(true);
            BaseFont normal = PdfUtil.helvetica(false);
            BaseFont cinzel = PdfUtil.cinzel();

            // Banner superior con degradé de azul a dorado
            PdfShading degrade = PdfShading.simpleAxial(escritor, 0, 0, ancho, 0, PdfUtil.AZUL, PdfUtil.DORADO);
            lienzo.saveState();
            lienzo.moveTo(0, alto);
            lienzo.lineTo(ancho, alto);
            lienzo.lineTo(ancho, alto - 34);
            lienzo.lineTo(0, alto - 54);
            lienzo.closePath();
            lienzo.clip();
            lienzo.newPath();
            lienzo.paintShading(degrade);
            lienzo.restoreState();

            // Encabezado: logo, nombre y áreas a la izquierda, número del certificado a la derecha en una sola línea
            PdfUtil.dibujarEncabezado(lienzo, 48, alto - 118);
            String etiquetaNumero = "N.\u00ba de certificado: ";
            float anchoNumero = normal.getWidthPoint(etiquetaNumero, 10) + negrita.getWidthPoint(certificado.getNumero(), 11);
            lienzo.beginText();
            lienzo.setFontAndSize(normal, 10);
            lienzo.setColorFill(PdfUtil.GRIS);
            lienzo.setTextMatrix(ancho - 48 - anchoNumero, alto - 92);
            lienzo.showText(etiquetaNumero);
            lienzo.setFontAndSize(negrita, 11);
            lienzo.setColorFill(PdfUtil.AZUL);
            lienzo.showText(certificado.getNumero());
            lienzo.endText();

            // Cuerpo
            PdfUtil.centrado(lienzo, normal, 10, PdfUtil.GRIS, "ID\u00d3NEOS ONLINE S.A.S. OTORGA EL PRESENTE", centro, alto - 168, 2.2f);
            PdfUtil.centrado(lienzo, cinzel, 44, PdfUtil.AZUL, "CERTIFICADO", centro, alto - 222, 9f);

            String nombre = certificado.getNombreAlumno();
            String dni = "D.N.I.: ";
            String numeroDni = certificado.getDniAlumno();
            float anchoA = normal.getWidthPoint("A", 14);
            float anchoNombre = negrita.getWidthPoint(nombre, 24);
            float anchoDni = normal.getWidthPoint(dni, 12) + negrita.getWidthPoint(numeroDni, 12);
            float separacion = 22;
            float total = anchoA + separacion + anchoNombre + separacion + anchoDni;
            float x = centro - total / 2;
            float y = alto - 272;
            lienzo.beginText();
            lienzo.setFontAndSize(normal, 14);
            lienzo.setColorFill(PdfUtil.GRIS);
            lienzo.setTextMatrix(x, y);
            lienzo.showText("A");
            lienzo.setFontAndSize(negrita, 24);
            lienzo.setColorFill(PdfUtil.AZUL);
            lienzo.setTextMatrix(x + anchoA + separacion, y);
            lienzo.showText(nombre);
            lienzo.setFontAndSize(normal, 12);
            lienzo.setColorFill(PdfUtil.GRIS);
            lienzo.setTextMatrix(x + anchoA + separacion + anchoNombre + separacion, y);
            lienzo.showText(dni);
            lienzo.setFontAndSize(negrita, 12);
            lienzo.setColorFill(PdfUtil.AZUL);
            lienzo.showText(numeroDni);
            lienzo.endText();
            lienzo.setColorStroke(PdfUtil.DORADO);
            lienzo.setLineWidth(2f);
            lienzo.moveTo(x + anchoA + separacion - 14, y - 7);
            lienzo.lineTo(x + anchoA + separacion + anchoNombre + 14, y - 7);
            lienzo.stroke();

            // Texto del certificado, centrado y con partes en negrita
            Inscripcion inscripcion = certificado.getInscripcion();
            Font gris = new Font(normal, 13, Font.NORMAL, PdfUtil.GRIS);
            Font fuerte = new Font(negrita, 13, Font.NORMAL, PdfUtil.AZUL);
            Phrase frase = new Phrase();
            frase.add(new Chunk("Por haber completado y aprobado el curso ", gris));
            frase.add(new Chunk("\"" + inscripcion.getCurso().getNombre() + "\"", fuerte));
            frase.add(new Chunk(" (Programa " + inscripcion.getCohorte().getPrograma().getNombre() + "), dictado durante la cohorte ", gris));
            frase.add(new Chunk(inscripcion.getCohorte().getMesAnio(), fuerte));
            Integer minutos = inscripcion.getCurso().getCargaHorariaMinutos();
            if (minutos != null && minutos > 0) {
                frase.add(new Chunk(", con una carga horaria total de ", gris));
                frase.add(new Chunk(formatearHoras(minutos) + " c\u00e1tedra", fuerte));
            }
            frase.add(new Chunk(".", gris));
            ColumnText texto = new ColumnText(lienzo);
            texto.setSimpleColumn(frase, 150, alto - 395, ancho - 150, alto - 300, 21, Element.ALIGN_CENTER);
            texto.go();

            // Firma del docente titular
            Docente titular = inscripcion.getCurso().getDocenteTitular();
            if (titular != null) {
                String nombreDocente = titular.getUsuario().getNombreCompleto();
                PdfUtil.centrado(lienzo, cinzel, 20, PdfUtil.AZUL, nombreDocente, centro, 150, 1f);
                lienzo.setColorStroke(PdfUtil.AZUL);
                lienzo.setLineWidth(0.8f);
                lienzo.moveTo(centro - 120, 142);
                lienzo.lineTo(centro + 120, 142);
                lienzo.stroke();
                PdfUtil.centrado(lienzo, negrita, 11, PdfUtil.AZUL, nombreDocente, centro, 126, 0);
                String detalle = "Docente titular"
                        + (titular.getMatriculaCnv() != null && !titular.getMatriculaCnv().isBlank()
                                ? " \u2022 Matr\u00edcula CNV " + titular.getMatriculaCnv() : "");
                PdfUtil.centrado(lienzo, normal, 9, PdfUtil.GRIS, detalle, centro, 112, 0);
            }

            String emision = "Emitido el " + certificado.getFechaEmision().format(formatoFecha);
            lienzo.beginText();
            lienzo.setFontAndSize(normal, 9);
            lienzo.setColorFill(PdfUtil.GRIS);
            lienzo.setTextMatrix(ancho - 48 - normal.getWidthPoint(emision, 9), 48);
            lienzo.showText(emision);
            lienzo.endText();
            documento.close();
        } catch (DocumentException e) {
            throw new IllegalStateException("No se pudo generar el certificado en PDF", e);
        }
        return salida.toByteArray();
    }

    /** Escribe una cantidad de minutos como horas cátedra, por ejemplo 120 horas o 1,5 horas. */
    private static String formatearHoras(int minutos) {
        if (minutos % 60 == 0) {
            int horas = minutos / 60;
            return horas + (horas == 1 ? " hora" : " horas");
        }
        return String.format(Locale.of("es", "AR"), "%.1f horas", minutos / 60.0);
    }

    // ----------------------------------------------------------------------
    // Creaciones
    // ----------------------------------------------------------------------

    /** Guarda el certificado en la base de datos. */
    @Override
    public Certificado guardar(Certificado entidad) {
        return certificadoRepository.save(entidad);
    }

    /** Emite el certificado de finalización de una inscripción. */
    @Override
    @Transactional
    public Certificado emitir(Inscripcion inscripcion) {
        Certificado vigente = inscripcion.getCertificadoVigente();
        if (vigente != null) {
            return vigente;
        }
        LocalDateTime ahora = LocalDateTime.now();
        long secuencia = certificadoRepository.count() + 1;
        String numero = String.format("CERT-%d-%06d", ahora.getYear(), secuencia);
        while (certificadoRepository.findByNumero(numero).isPresent()) {
            secuencia++;
            numero = String.format("CERT-%d-%06d", ahora.getYear(), secuencia);
        }
        var usuario = inscripcion.getAlumno().getUsuario();
        Certificado certificado = new Certificado(inscripcion, numero, ahora,
                "Certificado de finalización del curso " + inscripcion.getCurso().getNombre(), usuario.getNombreCompleto(),
                usuario.getDni());
        certificado = certificadoRepository.save(certificado);
        inscripcion.getCertificados().add(certificado);
        // El correo real llega con MOD-NF-01: por ahora el envío se registra en el log y el certificado queda sin enviar
        REGISTRO.info("Certificado {} emitido para {} (envío por correo simulado)", numero, usuario.getCorreo());
        return certificado;
    }

    // ----------------------------------------------------------------------
    // Modificaciones
    // ----------------------------------------------------------------------

    /** Modifica un registro existente. */
    @Override
    public Certificado modificar(Certificado entidad) {
        return certificadoRepository.save(entidad);
    }

    @Override
    @Transactional
    public Certificado corregirDatos(Integer idCertificado, String nombre, String dni) {
        Certificado certificado = certificadoRepository.findById(idCertificado)
                .filter(c -> !c.estaAnulado() && !c.getInscripcion().esInactivo())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Error! El certificado no se encuentra vigente o su inscripción está dada de baja."));
        if (nombre == null || nombre.isBlank() || dni == null || dni.isBlank()) {
            throw new IllegalArgumentException("Error! El nombre y el DNI del alumno son obligatorios.");
        }
        String nombreLimpio = nombre.trim();
        String dniLimpio = dni.trim();
        if (nombreLimpio.length() > LONGITUD_NOMBRE) {
            throw RespuestaFormularioUtil.armarCampoInvalido("nombre", MensajeErrorUtil.armarDemasiadoLargo("El nombre", LONGITUD_NOMBRE, "caracteres"));
        }
        if (!dniLimpio.matches("\\d{7,8}")) {
            throw RespuestaFormularioUtil.armarCampoInvalido("dni", MensajeErrorUtil.armarFormatoInvalido("El DNI", "solo números, 7 u 8 dígitos"));
        }
        certificado.setNombreAlumno(nombreLimpio);
        certificado.setDniAlumno(dniLimpio);
        certificado.setUltimaModificacion(LocalDateTime.now());
        return certificadoRepository.save(certificado);
    }

    // ----------------------------------------------------------------------
    // Bajas
    // ----------------------------------------------------------------------

    /** Anula el certificado, en lugar de eliminarlo de la base de datos. */
    @Override
    public void borrar(Certificado entidad) {
        entidad.anular(); // Anulación en lugar de borrado
        certificadoRepository.save(entidad);
    }

    /** Anula los certificados vigentes de una inscripción. */
    @Override
    @Transactional
    public boolean anular(Inscripcion inscripcion) {
        List<Certificado> vigentes = certificadoRepository.findByInscripcionAndAnuladoFalse(inscripcion);
        for (Certificado certificado : vigentes) {
            certificado.anular();
            certificado.setUltimaModificacion(LocalDateTime.now());
            certificadoRepository.save(certificado);
        }
        return !vigentes.isEmpty();
    }
}
