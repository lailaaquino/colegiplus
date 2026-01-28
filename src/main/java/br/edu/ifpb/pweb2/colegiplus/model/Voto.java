package br.edu.ifpb.pweb2.colegiplus.model;
import java.io.Serializable;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(
    uniqueConstraints = @UniqueConstraint(
        name = "uk_voto_professor_reuniao_processo",
        columnNames = {"professor_id", "reuniao_id", "processo_id"}
    )
)
public class Voto implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TipoDecisao decisao;

    @Column(length = 1000)
    private String justificativa;

    @Column(nullable = false)
    private Boolean ausente = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private TipoVoto tipoVoto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "professor_id")
    private Professor professor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "processo_id")
    private Processo processo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "reuniao_id")
    private Reuniao reuniao;
}
