package br.edu.ifpb.pweb2.colegiplus.model;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
@ToString(exclude = {"reunioes"}) // Exclui a coleção do toString()
public class Colegiado implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;

    @ManyToOne
    @JoinColumn(name = "coordenador_id")
    private Professor coordenador;

    @ManyToMany
    @JoinTable(
        name = "colegiado_membros",
        joinColumns = @JoinColumn(name = "colegiado_id"),
        inverseJoinColumns = @JoinColumn(name = "professor_id")
    )
    private List<Professor> membros;

    @OneToMany(mappedBy = "colegiado")
    private List<Reuniao> reunioes;
}
