package es.edu.liga.controller;

import es.edu.liga.domain.model.Equipo;
import es.edu.liga.service.FileStorageService;
import es.edu.liga.service.LigaService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/equipos")
public class LigaController {

    private final LigaService ligaService;
    private final FileStorageService fileService;

    public LigaController(LigaService ligaService, FileStorageService fileService) {
        this.ligaService = ligaService;
        this.fileService = fileService;
    }

    @GetMapping
    public List<Equipo> getAll() {
        return ligaService.listarTodos();
    }

    @GetMapping("/{id}")
    public Equipo getById(@PathVariable Long id) {
        return ligaService.obtenerPorId(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Equipo create(@RequestBody Equipo equipo) throws IOException {
        return ligaService.crear(equipo);
    }

    @PutMapping("/{id}")
    public Equipo update(@PathVariable Long id, @RequestBody Equipo equipo) throws IOException {
        return ligaService.actualizar(id, equipo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) throws IOException {
        ligaService.eliminar(id);
    }

    @PostMapping(value = "/{id}/escudo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Equipo uploadEscudo(@PathVariable Long id, @RequestParam("file") MultipartFile file) throws IOException {
        return ligaService.subirEscudo(id, file);
    }

    @GetMapping("/{id}/escudo")
    public ResponseEntity<Resource> downloadEscudo(@PathVariable Long id) throws IOException {
        Equipo eq = ligaService.obtenerPorId(id);
        Resource img = fileService.cargarEscudo(eq.escudoFileName());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(img);
    }

    @GetMapping("/export/csv")
    public ResponseEntity<Resource> downloadCsv() throws IOException {
        Path csv = ligaService.generarCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clasificacion.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(new UrlResource(csv.toUri()));
    }
}
