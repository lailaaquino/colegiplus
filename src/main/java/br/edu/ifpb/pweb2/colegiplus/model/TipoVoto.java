package br.edu.ifpb.pweb2.colegiplus.model;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public enum TipoVoto {
    COM_RELATOR(1),
    DIVERGENTE(2); 
    private final int codigo;
}