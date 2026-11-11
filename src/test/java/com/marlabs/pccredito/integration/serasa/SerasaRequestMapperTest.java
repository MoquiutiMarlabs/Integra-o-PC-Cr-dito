package com.marlabs.pccredito.integration.serasa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.marlabs.pccredito.domain.CreditAnalysis;
import com.marlabs.pccredito.domain.CustomerRelationshipType;
import com.marlabs.pccredito.integration.serasa.dto.SerasaNovaPropostaRequest;

class SerasaRequestMapperTest {

    private final SerasaRequestMapper mapper =
            new SerasaRequestMapper();

    @Test
    void shouldMapNovoToSerasaNovo() {
        assertSubproduto2(
                CustomerRelationshipType.NOVO,
                "Novo"
        );
    }

    @Test
    void shouldMapReativacaoToSerasaCarteira() {
        assertSubproduto2(
                CustomerRelationshipType.REATIVACAO,
                "Carteira"
        );
    }

    @Test
    void shouldMapAumentoLimiteToSerasaCarteira() {
        assertSubproduto2(
                CustomerRelationshipType.AUMENTO_LIMITE,
                "Carteira"
        );
    }

    private void assertSubproduto2(
            CustomerRelationshipType tipoConsulta,
            String expectedSubproduto2) {

        CreditAnalysis analysis = new CreditAnalysis(
                "test-request-id",
                "23167514000131",
                tipoConsulta,
                10000L,
                new BigDecimal("87.5"),
                12,
                new BigDecimal("15000.75"),
                new BigDecimal("2500.30")
        );

        SerasaNovaPropostaRequest request =
                mapper.toNovaProposta(
                        analysis,
                        "12345678"
                );

        SerasaNovaPropostaRequest.ProdutoSolicitado produto =
                request.application().produtoSolicitado();

        assertEquals(
                expectedSubproduto2,
                produto.subproduto2()
        );

        assertEquals("EMPR", produto.produto());
        assertEquals("Industria", produto.subproduto1());
        assertEquals(
                10000L,
                produto.valorEmprestimoSolicitado()
        );

        assertEquals(
                "NovaProposta",
                request.application().idServico()
        );

        assertEquals(
                "12345678",
                request.application().fonte()
        );

        assertEquals(
                "23167514000131",
                request.applicant()
                        .applicants()
                        .get(0)
                        .cnpj()
        );
    }
}
