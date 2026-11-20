package es.edu.liga.service;

import es.edu.liga.domain.gmodel.Equipo;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileStorageService {

    private final Path baseDir;

    public FileStorageService() throws IOException {
        this.baseDir = Paths.get(System.getProperty("user.home"), "laliga-data");
        Files.createDirectories(baseDir.resolve("escudos"));
    }

    // 1. TXT: Log de operaciones
    public void registrarLog(String accion) throws IOException {
        Path logFile = baseDir.resolve("auditoria.txt");
        String linea = LocalDateTime.now() + " - " + accion + System.lineSeparator();
        Files.writeString(logFile, linea, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    // 2. CSV: Exportar tabla
    public Path exportarCsv(List<Equipo> equipos) throws IOException {
        Path csvFile = baseDir.resolve("clasificacion.csv");
        String header = "ID,Nombre,Ciudad,Puntos\n";
        String body = equipos.stream()
                .map(e -> e.id() + "," + e.nombre() + "," + e.ciudad() + "," + e.puntos())
                .collect(Collectors.joining("\n"));
        Files.writeString(csvFile, header + body, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        return csvFile;
    }

    // 3. IMAGEN: Guardar y recuperar escudo
    public String guardarEscudo(Long id, MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IllegalArgumentException("El archivo de imagen está vacío");
        String fileName = "escudo_" + id + "_" + file.getOriginalFilename();
        Path destino = baseDir.resolve("escudos").resolve(fileName);
        Files.copy(file.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    public Resource cargarEscudo(String fileName) throws IOException {
        Path archivo = baseDir.resolve("escudos").resolve(fileName).normalize();
        Resource resource = new UrlResource(archivo.toUri());
        if (!resource.exists()) throw new IOException("Imagen no encontrada: " + fileName);
        return resource;
    }
}
