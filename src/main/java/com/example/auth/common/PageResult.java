package com.example.auth.common;

import java.util.List;

public class PageResult<T> {
    private long total;
    private List<T> list;
    private int page;
    private int size;

    public PageResult() {}

    public PageResult(long total, List<T> list, int page, int size) {
        this.total = total;
        this.list = list;
        this.page = page;
        this.size = size;
    }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
