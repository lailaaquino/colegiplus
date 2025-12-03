package br.edu.ifpb.pweb2.colegiplus.model;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(exclude = {"processos", "colegiadosMembro", "colegiadosCoordenados"})
public class Professor implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank private String nome;
    private String fone;

    @Column(unique = true, nullable = false)
    private String matricula;

    @Column(unique = true, nullable = false)
    private String login;

    @NotBlank
    private String senha;

    private boolean coordenador;

    @OneToMany(mappedBy = "relator")
    private List<Processo> processos;
    
    @ManyToMany(mappedBy = "membros") 
    private List<Colegiado> colegiadosMembro;

    @OneToMany(mappedBy = "coordenador")
    private List<Colegiado> colegiadosCoordenados;
    
}