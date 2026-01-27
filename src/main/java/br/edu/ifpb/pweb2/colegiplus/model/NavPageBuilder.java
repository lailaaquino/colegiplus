package br.edu.ifpb.pweb2.colegiplus.model;


public class NavPageBuilder {

    private NavPage paginator;

    public static NavPage newNavPage(int currentPage, long totalItens, int totalPages, int pageSize) {
        NavPageBuilder builder = new NavPageBuilder();
        builder.start();
        builder.setCurrentPage(currentPage);
        builder.setTotalItens(totalItens);
        builder.setTotalPages(totalPages);
        builder.setPageSize(pageSize);
        return builder.finish();
    }

    private NavPageBuilder() {
        this.start();
    }

    public NavPageBuilder start() {
        this.paginator = new NavPage(1, 0, 0, 10);
        return this;
    }

    public NavPageBuilder setCurrentPage(int currentPage) {
        this.paginator = new NavPage(currentPage, paginator.getTotalItens(), paginator.getTotalPages(), paginator.getPageSize());
        return this;
    }

    public NavPageBuilder setTotalItens(long totalItens) {
        this.paginator = new NavPage(paginator.getCurrentPage(), totalItens, paginator.getTotalPages(), paginator.getPageSize());
        return this;
    }

    public NavPageBuilder setTotalPages(int totalPages) {
        this.paginator = new NavPage(paginator.getCurrentPage(), paginator.getTotalItens(), totalPages, paginator.getPageSize());
        return this;
    }

    public NavPageBuilder setPageSize(int pageSize) {
        this.paginator = new NavPage(paginator.getCurrentPage(), paginator.getTotalItens(), paginator.getTotalPages(), pageSize);
        return this;
    }

    public NavPage finish() {
        return this.paginator;
    }
}
