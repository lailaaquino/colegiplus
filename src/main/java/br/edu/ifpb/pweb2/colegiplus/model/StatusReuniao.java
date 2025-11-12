package br.edu.ifpb.pweb2.colegiplus.model;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum StatusReuniao {
    ENCERRADA(1), 
    PROGRAMADA(2); 
    private final int codigo;
}