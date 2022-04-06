package com.meow.community.entity;

public class Page {

    //当前页码
    private int current = 1;

    //每页显示帖子的最大数量
    private int limit = 10;

    //数据（帖子）总数
    private int rows;

    //查询路径（用于复用分页链接）
    private String path;

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        if(current >= 1){
            this.current = current;
        }
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit >=1 && limit <= 100){
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows >= 0){
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    //获取每页的起始帖子序号
    public int getOffset(){
        return (current - 1) * limit;
    }
    //获取所有的页数
    public int getTotalPages(){
        int totalPages = rows / limit;
        return rows % limit == 0 ? totalPages : totalPages + 1;
    }

    //并非将所有的页码都显示出来，只显示包含当前页码一定范围内的页码就可以，例如[current-2, current+2]
    //
    public int getLeftPage(){
        int leftPage = current - 2;
        return leftPage <= 0 ? 1 : leftPage;
    }
    //
    public int getRightPage(){
        int rightPage = current + 2;
        int totalPages = getTotalPages();
        return rightPage > totalPages ? totalPages : rightPage;
    }
}
