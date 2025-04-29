package com.example.Proyecto.services;

import com.example.Proyecto.entities.ClienteEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
public class ClienteServiceTest {

    @Autowired
    private TestEntityManager entityManager;


    @Autowired  // Spring creará el servicio automáticamente
    private ClienteService clienteService;



    // registrarCliente()
    @Test
    void registrarCliente_GuardaClienteCorrectamente() {
        // Given
        ClienteEntity cliente = new ClienteEntity(null, "Juan", "juan@test.com", 0);

        // When
        ClienteEntity resultado = clienteService.registrarCliente(cliente);

        // Then
        assertThat(resultado.getId()).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Juan");
    }

    @Test
    void registrarCliente_RechazaEmailDuplicado() {
        // Given
        ClienteEntity cliente1 = new ClienteEntity(null, "Ana", "ana@test.com", 0);
        entityManager.persist(cliente1);

        ClienteEntity cliente2 = new ClienteEntity(null, "Ana2", "ana@test.com", 0);

        // When / Then
        assertThatThrownBy(() -> clienteService.registrarCliente(cliente2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Email ya registrado");
    }

    @Test
    void registrarCliente_ValidaCamposObligatorios() {
        // Given
        ClienteEntity clienteSinNombre = new ClienteEntity(null, "", "test@test.com", 0);
        ClienteEntity clienteSinEmail = new ClienteEntity(null, "Luis", "", 0);

        // When / Then
        assertThatThrownBy(() -> clienteService.registrarCliente(clienteSinNombre))
                .isInstanceOf(ResponseStatusException.class);

        assertThatThrownBy(() -> clienteService.registrarCliente(clienteSinEmail))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void registrarCliente_InicializaReservasEnCero() {
        // Given
        ClienteEntity cliente = new ClienteEntity(null, "Maria", "maria@test.com", 99);

        // When
        ClienteEntity resultado = clienteService.registrarCliente(cliente);

        // Then
        assertThat(resultado.getTotal_reservas()).isZero();
    }

    @Test
    void registrarCliente_ConvierteEmailAMinusculas() {
        // Given
        ClienteEntity cliente = new ClienteEntity(null, "Carlos", "CARLOS@TEST.COM", 0);

        // When
        ClienteEntity resultado = clienteService.registrarCliente(cliente);

        // Then
        assertThat(resultado.getEmail()).isEqualTo("carlos@test.com");
    }


    // obtenerTodosLosClientes()
    @Test
    void obtenerTodosLosClientes_RetornaListaVaciaSiNoHayClientes() {
        // When
        List<ClienteEntity> clientes = clienteService.obtenerTodosLosClientes();

        // Then
        assertThat(clientes).isEmpty();
    }

    @Test
    void obtenerTodosLosClientes_RetornaClientesOrdenadosPorNombre() {
        // Given
        entityManager.persist(new ClienteEntity(null, "Carlos", "carlos@test.com", 0));
        entityManager.persist(new ClienteEntity(null, "Ana", "ana@test.com", 0));

        // When
        List<ClienteEntity> clientes = clienteService.obtenerTodosLosClientes();

        // Then
        assertThat(clientes).extracting(ClienteEntity::getNombre)
                .containsExactly("Ana", "Carlos");
    }


    // buscarPorId()
    @Test
    void buscarPorId_RetornaClienteExistente() {
        // Given
        ClienteEntity cliente = entityManager.persist(new ClienteEntity(null, "Test", "test@test.com", 0));

        // When
        Optional<ClienteEntity> resultado = clienteService.buscarPorId(cliente.getId());

        // Then
        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Test");
    }

    @Test
    void buscarPorId_RetornaVacioSiIdNoExiste() {
        // When
        Optional<ClienteEntity> resultado = clienteService.buscarPorId(999L);

        // Then
        assertThat(resultado).isEmpty();
    }
}