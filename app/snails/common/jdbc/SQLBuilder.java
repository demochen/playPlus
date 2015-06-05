package snails.common.jdbc;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import snails.common.base.dto.PagingDto;
import snails.common.util.lang.EmptyUtil;

public class SQLBuilder {
    private StringBuilder sb = null;

    private SQLBuilder() {
        sb = new StringBuilder();
    }

    public SQLBuilder appendWithoutEscape(String sql) {
        sb.append(sql);

        return this;
    }

    /**
     * 实例化SQLBUILDER
     * 
     * @return SQLBUILDER
     */
    public static SQLBuilder newBuilder() {
        return new SQLBuilder();
    }

    /**
     * 拼接SQL、转义
     * 
     * @param sql
     * @return
     */
    public SQLBuilder appendSQL(String sql) {
        sb.append(StringEscapeUtils.escapeSql(sql));

        return this;
    }

    public SQLBuilder appendLike(String sql) {
        sb.append("'%" + StringEscapeUtils.escapeSql(sql) + "%'");

        return this;
    }

    public SQLBuilder appendLikeIfNotNull(String sql, Object o) {
        if (EmptyUtil.isNotEmpty(o + "")) {
            appendSQL("" + sql);
            sb.append("'%" + StringEscapeUtils.escapeSql(o + "") + "%'");
        }

        return this;
    }

    public SQLBuilder appendParam(Object obj) {
        if (obj instanceof Number) {
            sb.append(obj);
        } else {
            sb.append("'").append(StringEscapeUtils.escapeSql(obj + "")).append("'");
        }

        return this;
    }

    /**
     * <pre>
     * LIMIT
     * @param begin
     * @param size
     * @return
     * @author RIN
     * </pre>
     */
    public SQLBuilder appendLimit(Long begin, Integer size) {
        sb.append(" limit ").append(begin).append(",").append(size);

        return this;
    }

    public SQLBuilder appendLimit(PagingDto page) {
        if (page == null)
            return this;
        appendLimit(page.getBegin(), page.getPageSize());

        return this;
    }

    /**
     * 如果对象不是空的就拼接进去
     * 
     * @param sql
     * @param o
     * @return
     */
    public SQLBuilder appendIfNotNull(String sql, Object o) {
        if (EmptyUtil.isNotEmpty(o + "")) {
            appendSQL("" + sql);
            appendParam(o);
        }

        return this;
    }
    public SQLBuilder appendSort(String sort) {
        if(StringUtils.isEmpty(sort)){
            sort = " null ";
        }
        appendSQL(" order by " + sort);

        return this;
    }
    
    public SQLBuilder orderBy(String orderBy, String sord) {
        if (StringUtils.isEmpty(orderBy)) {
            orderBy = " NULL ";
        }
        appendSQL(" order by " + orderBy + " ");
        if (StringUtils.isNotEmpty(sord)) {
            appendWithoutEscape(sord);
        }
        return this;
    }

    @Override
    public String toString() {
        return sb.toString();
    }
}
