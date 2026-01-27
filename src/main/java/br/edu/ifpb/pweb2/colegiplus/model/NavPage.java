package br.edu.ifpb.pweb2.colegiplus.model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NavPage {
    private int currentPage;
    private long totalItens;
    private int totalPages;
    private int pageSize;
}
