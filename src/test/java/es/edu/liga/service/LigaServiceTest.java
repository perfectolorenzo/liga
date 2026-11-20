package es.edu.liga.service;

import es.edu.liga.domain.exception.EquipoNotFoundException;
import es.edu.liga.domain.model.Equipo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LigaServiceTest {

    @Mock
    private FileStorageService fileStorageService;

    private LigaService ligaService;

    @BeforeEach
    void setUp() {
        ligaService = new LigaService(fileStorageService);
    }

    @Test
    void crearEquipo_Valido_GuardaYRegistraLog() throws IOException {
        Equipo input = new Equipo(null, "Athletic Club", "Bilbao", 60, null);

        Equipo resultado = ligaService.crear(input);

        assertNotNull(resultado.id());
        assertEquals("Athletic Club", resultado.nombre());
        verify(fileStorageService, times(1)).registrarLog(contains("Athletic Club"));
    }

    @Test
    void crearEquipo_SinNombre_LanzaExcepcion() {
        Equipo input = new Equipo(null, "  ", "Sevilla", 30, null);

        assertThrows(IllegalArgumentException.class, () -> ligaService.crear(input));
        verifyNoInteractions(fileStorageService);
    }

    @Test
    void obtenerPorId_Inexistente_LanzaNotFound() {
        assertThrows(EquipoNotFoundException.class, () -> ligaService.obtenerPorId(999L));
    }

    @Test
    void actualizarEquipo_Existente_ModificaDatos() throws IOException {
        Equipo cambios = new Equipo(null, "RC Celta", "Vigo", 50, null);

        Equipo actualizado = ligaService.actualizar(1L, cambios);

        assertEquals("RC Celta", actualizado.nombre());
        assertEquals(50, actualizado.puntos());
        verify(fileStorageService).registrarLog("ACTUALIZADO: ID 1");
    }

    @Test
    void eliminarEquipo_Existente_LoBorraDeMemoria() throws IOException {
        ligaService.eliminar(1L);

        assertThrows(EquipoNotFoundException.class, () -> ligaService.obtenerPorId(1L));
        verify(fileStorageService).registrarLog(contains("ELIMINADO"));
    }

    @Test
    void subirEscudo_ActualizaNombreFichero() throws IOException {
        MockMultipartFile mockImg = new MockMultipartFile("file", "celta.png", "image/png", "bytes".getBytes());
        when(fileStorageService.guardarEscudo(1L, mockImg)).thenReturn("escudo_1_celta.png");

        Equipo resultado = ligaService.subirEscudo(1L, mockImg);

        assertEquals("escudo_1_celta.png", resultado.escudoFileName());
        verify(fileStorageService).guardarEscudo(1L, mockImg);
    }
}
