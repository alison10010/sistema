package gov.br.acreprev.atendimento.service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

@Service
public class SequenciaChamadaService {

    private final String[] sequenciaTipos = {"P", "N"};

    // índice por serviço (id do serviço -> índice atual da sequência)
    private final ConcurrentHashMap<Long, AtomicInteger> mapaIndices = new ConcurrentHashMap<>();

    private AtomicInteger getIndiceAtomic(Long servicoId) {
        return mapaIndices.computeIfAbsent(servicoId, id -> new AtomicInteger(0));
    }

    public int getIndiceAtual(Long servicoId) {
        return getIndiceAtomic(servicoId).get();
    }

    public void atualizarIndice(Long servicoId, int novoIndice) {
        int tamanho = sequenciaTipos.length;
        getIndiceAtomic(servicoId).set(novoIndice % tamanho);
    }

    public String[] getSequenciaTipos() {
        return sequenciaTipos;
    }
}

