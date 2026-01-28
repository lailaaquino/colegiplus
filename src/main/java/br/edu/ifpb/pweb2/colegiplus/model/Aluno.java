package br.edu.ifpb.pweb2.colegiplus.model;

import java.io.Serializable;
import java.util.List;

import br.edu.ifpb.pweb2.colegiplus.validator.MatriculaValida;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Aluno implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String nome;
    private String telefone;

    @NotBlank(message = "A matrícula não pode estar vazia!")
    @MatriculaValida
    @Column(unique = true, nullable = false)
    private String matricula;

    @Column(unique = true, nullable = false)
    private String login;

    @OneToMany(mappedBy = "interessado")
    private List<Processo> processos;

    @OneToOne
    @JoinColumn(name = "username")
    private User user;

    @NotBlank(message = "A senha é obrigatória")
    private String senha;

}