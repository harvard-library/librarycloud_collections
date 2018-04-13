package edu.harvard.lib.librarycloud.collections.dao;

public class PageParams {

    private int page;
    private int size;

    public PageParams(int page, int size) {
        this.page = page;
        this.size = size;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public void incrementUp() {
        page = page + 1;
    }
}
