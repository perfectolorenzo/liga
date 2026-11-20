package es.edu.liga.service;

import es.edu.liga.domain.exception.EquipoNotFoundException;
import es.edu.liga.domain.model.Equipo;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class LigaService {

    private final Map<Long, Equipo> store = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);
    private final FileStorageService fileService;

    public LigaService(FileStorageService fileService) {
        this.fileService = fileService;
        // Datos semilla
        store.put(1L, new Equipo(idGen.getAndIncrement(), "Celta de Vigo", "Vigo", 45, null));
        store.put(2L, new Equipo(idGen.getAndIncrement(), "Real Madrid", "Madrid", 78, null));
    }

    public List<Equipo> listarTodos() {
        return store.values().stream()
                .sorted(Comparator.comparingInt(Equipo::puntos).reversed())
                .toList();
    }

    public Equipo obtenerPorId(Long id) {
        Equipo eq = store.get(id);
        if (eq == null) throw new EquipoNotFoundException(id);
        return eq;
    }

    public Equipo crear(Equipo dto) throws IOException {
        if (dto.nombre() == null || dto.nombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del equipo es obligatorio");
        }
        Long id = idGen.getAndIncrement();
        Equipo nuevo = new Equipo(id, dto.nombre(), dto.ciudad(), dto.puntos(), null);
        store.put(id, nuevo);
        fileService.registrarLog("CREADO: " + nuevo.nombre());
        return nuevo;
    }

    public Equipo actualizar(Long id, Equipo dto) throws IOException {
        Equipo actual = obtenerPorId(id);
        Equipo actualizado = new Equipo(id, dto.nombre(), dto.ciudad(), dto.puntos(), actual.escudoFileName());
        store.put(id, actualizado);
        fileService.registrarLog("ACTUALIZADO: ID " + id);
        return actualizado;
    }

    public void eliminar(Long id) throws IOException {
        Equipo eliminado = obtenerPorId(id);
        store.remove(id);
        fileService.registrarLog("ELIMINADO: " + eliminado.nombre());
    }

    public Equipo subirEscudo(Long id, MultipartFile file) throws IOException {
        Equipo actual = obtenerPorId(id);
        String fileName = fileService.guardarEscudo(id, file);
        Equipo conEscudo = new Equipo(id, actual.nombre(), actual.ciudad(), actual.puntos(), fileName);
        store.put(id, conEscudo);
        fileService.registrarLog("ESCUDO SUBIDO: ID " + id);
        return conEscudo;
    }

    public Path generarCsv() throws IOException {
        return fileService.exportarCsv(listarTodos());
    }
}
