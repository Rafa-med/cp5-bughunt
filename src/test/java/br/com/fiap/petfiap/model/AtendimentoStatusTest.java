package br.com.fiap.petfiap.model;

import br.com.fiap.petfiap.exception.StatusInvalidoException;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

// Testes unitarios das transicoes de status do model: sem banco, sem Spring (Aula 15).
public class AtendimentoStatusTest {

    private Banho banhoDoRex() {
        return new Banho(1, "Rex", "PEQUENO", "Ana", LocalDateTime.of(2026, 10, 1, 10, 0));
    }

    @Test
    public void deveRecusarCancelamentoQuandoAtendimentoJaConcluido() {
        // Arrange: atendimento ja realizado
        Banho banho = banhoDoRex();
        banho.concluir();

        // Act + Assert: nao da para cancelar o que ja foi feito
        assertThrows(StatusInvalidoException.class, banho::cancelar);
        assertEquals("CONCLUIDO", banho.getStatus());
    }

    @Test
    public void deveRecusarConclusaoQuandoAtendimentoCancelado() {
        // Arrange
        Banho banho = banhoDoRex();
        banho.cancelar();

        // Act + Assert: atendimento cancelado nao pode ser concluido
        assertThrows(StatusInvalidoException.class, banho::concluir);
        assertEquals("CANCELADO", banho.getStatus());
    }
}
