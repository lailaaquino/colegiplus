package br.edu.ifpb.pweb2.colegiplus.model;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Processo implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @NotBlank
    private String numero;

    @Temporal(TemporalType.DATE)
    private Date dataRecepcao;

    @Temporal(TemporalType.DATE)
    private Date dataDistribuicao;

    @Enumerated(EnumType.STRING)
    private TipoDecisao decisaoRelator;

    @Column(name = "parecer_arquivo", columnDefinition = "bytea")
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] parecer;

    @Temporal(TemporalType.DATE)
    private Date dataParecer;

    @Enumerated(EnumType.STRING)
    private TipoDecisao resultadoColegiado;

    @Enumerated(EnumType.STRING)
    private StatusProcesso status;

    @ManyToOne
    @JoinColumn(name = "assunto_id")
    private Assunto assunto;

    @ManyToOne
    @JoinColumn(name = "aluno_interessado_id")
    private Aluno interessado;

    @ManyToOne
    @JoinColumn(name = "professor_relator_id")
    private Professor relator;

    @Column(length = 1000)
    private String textoRequerimento;

    @Column(name="requerimento_nome")
    private String requerimentoNome;

    @Column(name="requerimento_content_type")
    private String requerimentoContentType;

    @Column(name = "requerimento_pdf", columnDefinition = "bytea")
    @JdbcTypeCode(SqlTypes.VARBINARY)
    private byte[] requerimentoPdf;
}
