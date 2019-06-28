package com.jcspider.server.model;

import java.util.ArrayList;
import java.util.List;

public class SqlParam {

    private String  sql;
    private List<Object> params;

    public SqlParam() {
    }

    public SqlParam(String sql, List<Object> params) {
        this.sql = sql;
        this.params = params;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }

    public void addParam(Object param) {
        if (params == null) {
            this.params = new ArrayList<>();
        }
        this.params.add(param);
    }

    public boolean hasParams() {
        return this.params != null && !this.params.isEmpty();
    }

    public String appendSql(String afterSql) {
        return this.sql + afterSql;
    }

    public Object[] toSqlParaqms() {
        return this.hasParams() ? this.params.toArray() : new Object[]{};
    }
}
