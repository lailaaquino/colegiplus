package br.edu.ifpb.pweb2.colegiplus.model;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StatusReuniao {
    ENCERRADA(1), 
    PROGRAMADA(2), 
    EM_JULGAMENTO(3);
    private final int codigo;
}