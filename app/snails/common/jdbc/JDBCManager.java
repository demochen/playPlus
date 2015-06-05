package snails.common.jdbc ;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;

import play.Play;
import play.utils.Java;
import play.vfs.VirtualFile;
import snails.common.base.models.BasicGenericModel;
import snails.common.exception.CheckYourCodeException;
import snails.common.jdbc.annotation.PartitionKey;
import snails.common.jdbc.config.Configs;
import snails.common.jdbc.exception.JDBCException;
import snails.common.jdbc.transaction.DBBuilder.DataSrc;
import snails.common.jdbc.transaction.JDBCBuilder;
import snails.common.jdbc.transaction.JDBCBuilder.JDBCExecutor;
import snails.common.jdbc.utils.JdbcUtil;

/**
 * 数据库链接
 */
public class JDBCManager {
    /**
     * 日志
     */
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(JDBCManager.class);

    /**
     * 按字母排序 A-Z_a-z
     */
    private static Comparator<Field> sortFieldByName = new Comparator<Field>() {
        @Override
        public int compare(Field o1, Field o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

    /**
     * 不允许实例化
     */
    private JDBCManager() {
    }

    /**
     * 打印SQL
     * 
     * @param dataSrc
     *            数据源
     * @param query
     *            SQL语句
     * @param params
     *            参数
     */
    private static void showSQL(DataSrc dataSrc, String query, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append("执行SQL:").append(query).append("数据源:").append(dataSrc).append("参数：");

        for (int i = 0; i < params.length; i++) {
            if ((params[i] != null) && (params[i].toString().length() > 300)) {
                continue;
            }

            sb.append(("【")).append(i).append(" = ").append(params[i]).append("】");
        }

        log.warn(sb.toString());
    }

    private static final VirtualFile SQL_FOLDER = VirtualFile.open(new File(Play.applicationPath, "/app/sql"));

    public static <T> List<T> getResultListBySQLFile(DataSrc dataSrc, final Class<T> objClazz, String _sqlFilePath,
            Map _params) {
        return JDBCManager.getResultList(dataSrc, objClazz, JdbcUtil.renderResult(_sqlFilePath, _params, SQL_FOLDER));
    }

    public static <T> T getSingleResultBySQLFile(DataSrc dataSrc, final Class<T> objClazz, String _sqlFilePath,
            Map _params) {
        return JDBCManager.getSingleResult(dataSrc, objClazz, JdbcUtil.renderResult(_sqlFilePath, _params, SQL_FOLDER));
    }

    /**
     * 删除对象<code>（该对象必须是含有@Id字段）</code>
     * 
     * @param dataSrc
     *            数据源
     * @param obj
     *            对象
     * @return 删除结果 直接返回数据库所影响的记录数
     */
    public static long delete(DataSrc dataSrc, Object obj) {
        String table = getTableName(obj.getClass());
        Class objClazz = obj.getClass();
        List<Object> params = new ArrayList<Object>();

        Set<Field> fields = getFields(objClazz);
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(table).append(" WHERE ");

        for (Field field : fields) {
            if (field.getAnnotation(Id.class) != null) {
                sql.append(getColumnName(field)).append(" = ?  AND ");
                params.add(JdbcUtil.invokeGet(obj, field));
            }
        }

        sql.delete(sql.length() - 4, sql.length());

        if (Configs.JDBC.showSQL) {
            showSQL(dataSrc, sql.toString(), params.toArray());
        }

        return JDBCBuilder.update(false, dataSrc, sql.toString(), params.toArray());
    }

    /**
     * 主键判断
     * 
     * @param field
     *            字段
     * @return true:主键 false:不是主键
     */
    private static boolean isId(Field field) {
        return field.getAnnotation(Id.class) != null;
    }

    /**
     * 批量删除
     * 
     * @param dataSrc
     *            数据源
     * @param c
     *            对象集合
     * @return 数据库影响的记录数
     */
    public static long deleteBatch(DataSrc dataSrc, Collection<? extends Object> c) {
        if (CollectionUtils.isEmpty(c)) {
            return 0;
        }

        Class objClazz = c.iterator().next().getClass();
        String table = getTableName(objClazz);
        StringBuilder sql = new StringBuilder();
        sql.append("DELETE FROM ").append(table).append(" where ");

        List<Object> params = new ArrayList<Object>();

        for (Object obj : c) {
            sql.append("(");

            for (Field field : getFields(objClazz)) {
                if (isId(field)) {
                    Object value = JdbcUtil.invokeGet(obj, field);
                    sql.append(field.getName()).append(" =");
                    sql.append(" ? and ");
                    params.add(value);
                }
            }

            sql.delete(sql.length() - 4, sql.length());
            sql.append(") or ");
        }

        sql.delete(sql.length() - 4, sql.length());

        if (Configs.JDBC.showSQL) {
            showSQL(dataSrc, sql.toString(), params.toArray());
        }

        return JDBCBuilder.update(false, dataSrc, sql.toString(), params.toArray());
    }

    /**
     * 根据主键查找对象
     * 
     * @param dataSrc
     *            数据源
     * @param objClazz
     *            对象类型
     * @param params
     *            参数
     * @return 对象
     */
    public static <T> T findById(DataSrc dataSrc, final Class<T> objClazz, Object... params) {
        StringBuilder fieldStr = new StringBuilder();

        for (Field field : getFields(objClazz)) {
            if (field.getAnnotation(Id.class) != null) {
                fieldStr.append(field.getName()).append(",");
            }
        }

        fieldStr.delete(fieldStr.length() - 1, fieldStr.length());

        return findByFields(dataSrc, objClazz, fieldStr.toString(), params);
    }

    /**
     * 根据字段查找对象
     * 
     * @param dataSrc
     *            数据源
     * @param objClazz
     *            对象类型
     * @param fields
     *            字段名,逗号隔开
     * @param params
     *            参数
     * @return 对象
     */
    public static <T> T findByFields(DataSrc dataSrc, final Class<T> objClazz, String fields, Object... params) {
        String table = null;

        if (objClazz == null) {
            throw new JDBCException("entity could not be null ");
        }

        Entity entity = objClazz.getAnnotation(Entity.class);

        if (entity != null) {
            table = entity.name();
        } else {
            table = objClazz.getSimpleName();
        }

        StringBuilder sql = new StringBuilder();
        sql.append("select * from ").append(table).append(" where ");

        String[] idArray = fields.split(",");

        for (String id : idArray) {
            for (Field field : getFields(objClazz)) {
                if (field.getName().equals(id)) {
                    sql.append(getColumnName(field)).append(" = ? and ");
                }
            }
        }

        sql.delete(sql.length() - 4, sql.length());

        return getSingleResult(dataSrc, objClazz, sql.toString(), params);
    }

    /**
     * 获得数据库表中的列名
     * 
     * @param field
     *            字段
     * @return 数据库表中的列名
     */
    public static String getColumnName(Field field) {
        return getColumnName(field, true);
    }

    public static String getColumnName(Field field, boolean add) {
        Column col = field.getAnnotation(Column.class);

        if ((col != null) && StringUtils.isNotEmpty(col.name())) {
            if (add) {
                return "`" + col.name() + "`";
            } else {
                return col.name();
            }
        }
        if (add) {
            return "`" + field.getName() + "`";
        } else {
            return field.getName();
        }

    }

    /**
     * 获得全部字段包括继承的，取得的字段按照字母排序A-Z-a-z
     * 
     * @param clazz
     *            对象类型
     * @return 所有字段不包括被static或<code>@Transient</code>或Transient修饰的字段
     */
    private static Set<Field> getFields(final Class clazz) {
        Set<Field> fieldSet = new HashSet<Field>();
        Java.findAllFields(clazz, fieldSet);

        for (Iterator<Field> i = fieldSet.iterator(); i.hasNext();) {
            if (skipField(i.next())) {
                i.remove();
            }
        }

        List<Field> fieldList = new ArrayList<Field>();
        fieldList.addAll(fieldSet);

        Collections.sort(fieldList, sortFieldByName);
        fieldSet.clear();
        fieldSet.addAll(fieldList);
        return fieldSet;
    }

    private static Set<Field> getFields(final Class clazz, final String[] needFields) {
        Set<Field> fieldSet = getFields(clazz);

        // NEW LIST
        List<String> needFieldsList = new ArrayList<String>();

        for (String fieldName : needFields) {
            needFieldsList.add(StringUtils.trim(fieldName));
        }
        for (Iterator<Field> fieldI = fieldSet.iterator(); fieldI.hasNext();) {
            Field check = fieldI.next();
            if (needFieldsList.contains(check.getName()) == false) {
                fieldI.remove();
            }
        }
        return fieldSet;
    }

    /**
     * 获得集合
     * 
     * @param dataSrc数据源
     * @param objClazz对象类型
     * @param query查询语句
     * @param params参数
     * @return 对象集合
     */
    public static <T> List<T> getResultList(DataSrc dataSrc, final Class<T> objClazz, String query, Object... params) {
        if (Configs.JDBC.showSQL) {
            showSQL(dataSrc, query.toString(), params);
        }

        if ((objClazz == Long.class) || (objClazz == String.class) || (objClazz == Integer.class)
                || (objClazz == Double.class) || (objClazz == Float.class) || (objClazz == Boolean.class)) {
            return new JDBCExecutor<List<T>>(dataSrc, query, params) {
                @Override
                public List<T> doWithResultSet(ResultSet rs) throws SQLException {
                    List<T> list = new ArrayList<T>();

                    while (rs.next()) {
                        Object obj = null;
                        Object objGet = rs.getObject(1);

                        if (objGet != null) {
                            try {
                                if (Long.class.equals(objClazz)) {
                                    obj = new Long(objGet.toString());
                                } else if (String.class.equals(objClazz)) {
                                    obj = new String(objGet.toString());
                                } else if (Integer.class.equals(objClazz)) {
                                    obj = new Integer(objGet.toString());
                                } else if (Double.class.equals(objClazz)) {
                                    obj = new Double(objGet.toString());
                                } else if (Float.class.equals(objClazz)) {
                                    obj = new Float(objGet.toString());
                                } else if (Boolean.class.equals(objClazz)) {
                                    String booleanString = objGet.toString();

                                    if (booleanString.equals("1") || booleanString.equalsIgnoreCase("true")) {
                                        obj = new Boolean(true);
                                    } else {
                                        obj = new Boolean(false);
                                    }
                                }
                            } catch (Exception e) {
                                log.error(e.getMessage(), e);
                                throw new JDBCException(e);
                            }

                            list.add((T) obj);
                        }
                    }

                    if (Configs.JDBC.showResult) {
                        for (int i = 0; i < list.size(); i++) {
                            String reslt = list.get(i).toString();

                            if (reslt.length() > 300) {
                                reslt = reslt.substring(0, 300);
                            }

                            log.warn("查询结果第 {} 个= {} ", i, reslt);
                        }
                    }

                    return list;
                }
            }.call();
        }

        return new JDBCExecutor<List<T>>(dataSrc, query, params) {
            @Override
            public List<T> doWithResultSet(ResultSet rs) throws SQLException {
                List<T> list = new ArrayList<T>();
                Set<Field> fs = getFields(objClazz);

                while (rs.next()) {
                    Object obj = null;

                    try {
                        obj = objClazz.newInstance();

                        for (Field field : fs) {
                            try {
                                Object columnValue = rs.getObject(getColumnName(field, false));

                                if (columnValue != null) {
                                    setValue(obj, objClazz, field, columnValue);
                                }
                            } catch (SQLException e) {
                                continue;
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        throw new JDBCException(e);
                    }
                    list.add((T) obj);
                }

                if (Configs.JDBC.showResult) {
                    for (int i = 0; i < list.size(); i++) {
                        String reslt = list.get(i).toString();

                        if (reslt.length() > 300) {
                            reslt = reslt.substring(0, 300);
                        }

                        log.warn("查询结果第 {} 个= {} ", i, reslt);
                    }
                }

                return list;
            }
        }.call();
    }

    /**
     * 获得单个对象
     * 
     * @param dataSrc
     *            数据源
     * @param objClazz
     *            对象类型
     * @param query
     *            查询语句
     * @param params
     *            参数
     * @return 对象
     */
    public static <T> T getSingleResult(DataSrc dataSrc, final Class<T> objClazz, final String query, Object... params) {
        if (Configs.JDBC.showSQL) {
            showSQL(dataSrc, query.toString(), params);
        }

        if ((objClazz == Long.class) || (objClazz == String.class) || (objClazz == Integer.class)
                || (objClazz == Double.class) || (objClazz == Float.class) || (objClazz == Boolean.class)) {
            return new JDBCExecutor<T>(dataSrc, query, params) {
                @Override
                public T doWithResultSet(ResultSet rs) throws SQLException {
                    Object obj = null;

                    if (!rs.next()) {
                        if ((objClazz == Integer.class)
                                && ((query.toUpperCase().indexOf("COUNT") > -1) || (query.toUpperCase().indexOf("SUM") > -1))) {
                            if (Configs.JDBC.showResult) {
                                log.warn("查询结果 0 ");
                            }

                            return (T) new Integer("0");
                        } else if ((objClazz == Long.class)
                                && ((query.toUpperCase().indexOf("COUNT") > -1) || (query.toUpperCase().indexOf("SUM") > -1))) {
                            if (Configs.JDBC.showResult) {
                                log.warn("查询结果 0 ");
                            }

                            return (T) new Long("0");
                        } else {
                            if (Configs.JDBC.showResult) {
                                log.warn("查询结果 NULL ");
                            }

                            return null;
                        }
                    }
                    if (rs.getObject(1) == null) {
                        if (Configs.JDBC.showResult) {
                            log.warn("查询结果 NULL ");
                        }
                        return null;
                    }
                    try {
                        if (Long.class.equals(objClazz)) {
                            obj = new Long(rs.getObject(1).toString());
                        } else if (String.class.equals(objClazz)) {
                            obj = new String(rs.getObject(1).toString());
                        } else if (Integer.class.equals(objClazz)) {
                            obj = new Integer(rs.getObject(1).toString());
                        } else if (Double.class.equals(objClazz)) {
                            obj = new Double(rs.getObject(1).toString());
                        } else if (Float.class.equals(objClazz)) {
                            obj = new Float(rs.getObject(1).toString());
                        } else if (Boolean.class.equals(objClazz)) {
                            String booleanString = rs.getObject(1).toString();

                            if (booleanString.equals("1") || booleanString.equalsIgnoreCase("true")) {
                                obj = new Boolean(true);
                            } else {
                                obj = new Boolean(false);
                            }
                        }
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        throw new JDBCException(e);
                    }

                    if (Configs.JDBC.showResult) {
                        if (log.isInfoEnabled()) {
                            String reslt = obj.toString();

                            if (reslt.length() > 200) {
                                reslt = reslt.substring(0, 200);
                            }

                            log.warn("查询结果 {} ", reslt);
                        }
                    }

                    return (T) obj;
                }
            }.call();
        }

        return new JDBCExecutor<T>(dataSrc, query, params) {
            @Override
            public T doWithResultSet(ResultSet rs) throws SQLException {
                Set<Field> fs = getFields(objClazz);

                if (!rs.next()) {
                    if (Configs.JDBC.showResult) {
                        log.warn("查询结果 NULL ");
                    }

                    return null;
                }

                Object obj = null;

                try {
                    obj = objClazz.newInstance();

                    for (Field field : fs) {
                        try {
                            Object columnValue = rs.getObject(getColumnName(field, false));

                            if (columnValue != null) {
                                setValue(obj, objClazz, field, columnValue);
                            }
                        } catch (SQLException e) {
                            continue;
                        }
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new JDBCException(e);
                }

                if (Configs.JDBC.showResult) {
                    String reslt = obj.toString();

                    if (reslt.length() > 200) {
                        reslt = reslt.substring(0, 200);
                    }

                    log.warn("查询结果 {} ", reslt);
                }

                return (T) obj;
            }
        }.call();
    }

    /**
     * 获得数据库表名
     * 
     * @param clazz
     *            对象类
     * @return 表名
     */
    private static String getTableName(Class<? extends Object> clazz) {
        String table = null;

        Entity entity = clazz.getAnnotation(Entity.class);

        if (entity != null) {
            table = entity.name();
        } else {
            table = clazz.getSimpleName();
        }

        return table;
    }

    /**
     * 插入一个对象,返回自增长后的值
     * 
     * @param dataSrc
     *            数据源
     * @param obj
     *            对象
     * @return 影响数据库的结果数
     */
    public static long insert(DataSrc dataSrc, Object obj) {
        String table = getTableName(obj.getClass());

        if (obj instanceof BasicGenericModel) {
            ((BasicGenericModel) obj).setAddTs(new Date());
        }

        StringBuilder sqlField = new StringBuilder();
        StringBuilder sqlValue = new StringBuilder();
        Class objClazz = obj.getClass();
        List<Object> params = new ArrayList<Object>();
        sqlField.append("INSERT INTO ").append(table).append("(");
        sqlValue.append(" VALUES (");

        for (Field field : getFields(objClazz)) {
            Object value = JdbcUtil.invokeGet(obj, field);

            if (value != null) {
                sqlField.append(getColumnName(field)).append(",");
                sqlValue.append("?,");
                params.add(value);
            }
        }

        sqlField.deleteCharAt(sqlField.length() - 1);
        sqlField.append(")");
        sqlValue.deleteCharAt(sqlValue.length() - 1);
        sqlValue.append(")");

        if (Configs.JDBC.showSQL) {
            showSQL(dataSrc, sqlField.toString() + sqlValue.toString(), params.toArray());
        }

        return JDBCBuilder.insert(false, dataSrc, sqlField.toString() + sqlValue.toString(), params.toArray());
    }

    /**
     * 是否是自增长字段判断
     * 
     * @param field
     *            字段
     * @return true 自增长 false 不是自增长
     */
    private static boolean isAutoIntrement(Field field) {
        return field.getAnnotation(GeneratedValue.class) != null;
    }

    /**
     * 批量插入
     * 
     * @param dataSrc
     *            数据源
     * @param c
     *            对象集合
     * @return 影响数据库结果数
     */
    public static long insertBatch(DataSrc dataSrc, Collection<? extends Object> c) {
        if (CollectionUtils.isEmpty(c)) {
            return 0;
        }

        Class objClazz = c.iterator().next().getClass();
        String table = getTableName(objClazz);
        StringBuilder sqlField = new StringBuilder();
        sqlField.append("INSERT INTO ").append(table).append("(");

        for (Field field : getFields(objClazz)) {
            if (!isAutoIntrement(field)) {
                sqlField.append(getColumnName(field)).append(",");
            }
        }

        sqlField.deleteCharAt(sqlField.length() - 1);
        sqlField.append(")");

        StringBuilder sqlValue = new StringBuilder();
        sqlValue.append(" VALUES ");

        List<Object> params = new ArrayList<Object>();

        for (Object obj : c) {
            if (obj instanceof BasicGenericModel) {
                ((BasicGenericModel) obj).setAddTs(new Date());
                ((BasicGenericModel) obj).setUpdateTs(new Date());
            }

            sqlValue.append("(");

            for (Field field : getFields(objClazz)) {
                if (!isAutoIntrement(field)) {
                    Object value = JdbcUtil.invokeGet(obj, field);
                    sqlValue.append("?,");
                    params.add(value);
                }
            }

            sqlValue.deleteCharAt(sqlValue.length() - 1);
            sqlValue.append("),");
        }

        sqlValue.deleteCharAt(sqlValue.length() - 1);

        if (Configs.JDBC.showSQL) {
            showSQL(dataSrc, sqlField.toString() + sqlValue.toString(), params.toArray());
        }

        return JDBCBuilder.insert(false, dataSrc, sqlField.toString() + sqlValue.toString(), params.toArray());
    }

    /**
     * 插入或更新（会造成addTs不正确的问题）
     * 
     * @param dataSrc
     *            数据源
     * @param obj
     *            对象
     * @return 影响数据库的行数
     */
    public static long insertOrUpdate(DataSrc dataSrc, Object obj) {

        String table = getTableName(obj.getClass());

        if (obj instanceof BasicGenericModel) {
            ((BasicGenericModel) obj).setAddTs(new Date());
            ((BasicGenericModel) obj).setUpdateTs(new Date());
        }

        StringBuilder sqlField = new StringBuilder();
        StringBuilder sqlValue = new StringBuilder();
        Class objClazz = obj.getClass();
        List<Object> params = new ArrayList<Object>();
        sqlField.append("INSERT INTO ").append(table).append("(");
        sqlValue.append(" VALUES (");

        for (Field field : getFields(objClazz)) {
            Object value = JdbcUtil.invokeGet(obj, field);

            if (value != null) {
                sqlField.append(getColumnName(field)).append(",");
                sqlValue.append("?,");
                params.add(value);
            }
        }

        sqlField.deleteCharAt(sqlField.length() - 1);
        sqlField.append(")");
        sqlValue.deleteCharAt(sqlValue.length() - 1);
        sqlValue.append(")");

        StringBuilder sqlOnDuplicateKey = new StringBuilder();
        sqlOnDuplicateKey.append(" ON DUPLICATE KEY  UPDATE ");

        for (Field field : getFields(objClazz)) {
            if (isAutoIntrement(field)) {
                continue;
            }
            if (obj instanceof BasicGenericModel) {
                if (field.getName().equals("addTs")) {
                    continue;
                }
            }
            sqlOnDuplicateKey.append(getColumnName(field)).append("= ?,");
            params.add(JdbcUtil.invokeGet(obj, field));
        }

        sqlOnDuplicateKey.deleteCharAt(sqlOnDuplicateKey.length() - 1);
        if (Configs.JDBC.showSQL) {
            showSQL(dataSrc, sqlField.toString() + sqlValue.toString() + sqlOnDuplicateKey.toString(), params.toArray());
        }

        return JDBCBuilder.insert(false, dataSrc,
                sqlField.toString() + sqlValue.toString() + sqlOnDuplicateKey.toString(), params.toArray());
    }

    /**
     * 插入或更新（会造成addTs不正确的问题）
     * 
     * @param dataSrc
     *            数据源
     * @param obj
     *            对象
     * @return 影响数据库的行数
     */
    public static long insertOrUpdateNotNull(DataSrc dataSrc, Object obj) {

        String table = getTableName(obj.getClass());

        if (obj instanceof BasicGenericModel) {
            ((BasicGenericModel) obj).setAddTs(new Date());
            ((BasicGenericModel) obj).setUpdateTs(new Date());
        }

        StringBuilder sqlField = new StringBuilder();
        StringBuilder sqlValue = new StringBuilder();
        Class objClazz = obj.getClass();
        List<Object> params = new ArrayList<Object>();
        sqlField.append("INSERT INTO ").append(table).append("(");
        sqlValue.append(" VALUES (");

        for (Field field : getFields(objClazz)) {
            Object value = JdbcUtil.invokeGet(obj, field);

            if (value != null) {
                sqlField.append(getColumnName(field)).append(",");
                sqlValue.append("?,");
                params.add(value);
            }
        }

        sqlField.deleteCharAt(sqlField.length() - 1);
        sqlField.append(")");
        sqlValue.deleteCharAt(sqlValue.length() - 1);
        sqlValue.append(")");

        StringBuilder sqlOnDuplicateKey = new StringBuilder();
        sqlOnDuplicateKey.append(" ON DUPLICATE KEY  UPDATE ");

        for (Field field : getFields(objClazz)) {
            if (isAutoIntrement(field)) {
                continue;
            }
            if (obj instanceof BasicGenericModel) {
                if (field.getName().equals("addTs")) {
                    continue;
                }
            }
            Object value = JdbcUtil.invokeGet(obj, field);
            if (value == null)
                continue;
            sqlOnDuplicateKey.append(getColumnName(field)).append("= ?,");
            params.add(value);
        }

        sqlOnDuplicateKey.deleteCharAt(sqlOnDuplicateKey.length() - 1);
        if (Configs.JDBC.showSQL) {
            showSQL(dataSrc, sqlField.toString() + sqlValue.toString() + sqlOnDuplicateKey.toString(), params.toArray());
        }

        return JDBCBuilder.insert(false, dataSrc,
                sqlField.toString() + sqlValue.toString() + sqlOnDuplicateKey.toString(), params.toArray());
    }

    public static long updateIncludeFields(DataSrc dataSrc, Object obj, String fields) {

        if (StringUtils.isEmpty(fields)) {
            throw new CheckYourCodeException("需要更新的字段为空");
        }
        String[] fieldArray = StringUtils.trim(fields).split(",");

        if (ArrayUtils.isEmpty(fieldArray)) {
            throw new CheckYourCodeException("需要更新的字段为空");
        }

        Class objClazz = obj.getClass();

        // //指定的字段
        Set<Field> fileds = getFields(objClazz, fieldArray);

        String table = getTableName(obj.getClass());

        if (obj instanceof BasicGenericModel) {
            ((BasicGenericModel) obj).setUpdateTs(new Date());
        }

        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(table).append(" SET ");

        List<Object> params = new ArrayList<Object>();

        for (Field field : fileds) {
            sql.append(getColumnName(field)).append("= ?,");
            params.add(JdbcUtil.invokeGet(obj, field));
        }

        sql.deleteCharAt(sql.length() - 1);
        sql.append(" WHERE ");

        // 所有字段
        for (Field field : getFields(objClazz)) {
            if (field.getAnnotation(Id.class) != null) {
                sql.append(getColumnName(field)).append(" = ? AND ");
                params.add(JdbcUtil.invokeGet(obj, field));
            }
        }

        sql.delete(sql.length() - 4, sql.length());

        if (Configs.JDBC.showSQL) {
            showSQL(dataSrc, sql.toString(), params.toArray());
        }

        return JDBCBuilder.update(false, dataSrc, sql.toString(), params.toArray());
    }

    public static long insertOrUpdateIncludeFields(DataSrc dataSrc, Object obj, String fields) {

        if (StringUtils.isEmpty(fields)) {
            throw new CheckYourCodeException("需要更新的字段为空");
        }
        String[] fieldArray = StringUtils.trim(fields).split(",");

        if (ArrayUtils.isEmpty(fieldArray)) {
            throw new CheckYourCodeException("需要更新的字段为空");
        }
        Class objClazz = obj.getClass();
        // 指定的字段
        Set<Field> indexFileds = getFields(objClazz, fieldArray);
        String table = getTableName(obj.getClass());

        if (obj instanceof BasicGenericModel) {
            ((BasicGenericModel) obj).setAddTs(new Date());
            ((BasicGenericModel) obj).setUpdateTs(new Date());
        }

        StringBuilder sqlField = new StringBuilder();
        StringBuilder sqlValue = new StringBuilder();

        List<Object> params = new ArrayList<Object>();
        sqlField.append("INSERT INTO ").append(table).append("(");
        sqlValue.append(" VALUES (");

        // 所有字段
        for (Field field : getFields(objClazz)) {
            Object value = JdbcUtil.invokeGet(obj, field);

            if (value != null) {
                sqlField.append(getColumnName(field)).append(",");
                sqlValue.append("?,");
                params.add(value);
            }
        }

        sqlField.deleteCharAt(sqlField.length() - 1);
        sqlField.append(")");
        sqlValue.deleteCharAt(sqlValue.length() - 1);
        sqlValue.append(")");

        StringBuilder sqlOnDuplicateKey = new StringBuilder();
        sqlOnDuplicateKey.append(" ON DUPLICATE KEY  UPDATE ");

        for (Field field : indexFileds) {
            if (isAutoIntrement(field)) {
                continue;
            }
            if (obj instanceof BasicGenericModel) {
                if (field.getName().equals("addTs")) {
                    continue;
                }
            }
            sqlOnDuplicateKey.append(getColumnName(field)).append("= ?,");
            params.add(JdbcUtil.invokeGet(obj, field));
        }

        sqlOnDuplicateKey.deleteCharAt(sqlOnDuplicateKey.length() - 1);
        if (Configs.JDBC.showSQL) {
            showSQL(dataSrc, sqlField.toString() + sqlValue.toString() + sqlOnDuplicateKey.toString(), params.toArray());
        }

        return JDBCBuilder.insert(false, dataSrc,
                sqlField.toString() + sqlValue.toString() + sqlOnDuplicateKey.toString(), params.toArray());
    }

    /**
     * 工具字段插入或更新
     * 
     * @param dataSrc
     *            数据源
     * @param obj
     *            对象
     * @param fields
     *            字段逗号隔开
     * @param params
     *            参数
     * @return 影响数据库的结果数
     */
    public static long insertOrUpdate(DataSrc dataSrc, Object obj, String fields, Object... params) {
        if (null == findByFields(dataSrc, obj.getClass(), fields, params)) {
            return insert(dataSrc, obj);
        } else {
            return update(dataSrc, obj);
        }
    }

    /**
     * 给对象的某个成员设置（set）
     * 
     * @param o
     *            对象
     * @param filedName
     *            字段名
     * @param value
     *            值
     */
    private static void setValue(Object o, Class objClazz, Field field, Object value) {
        if (value == null) {
            return;
        }

        Class fieldClazz = field.getType();

        Object fieldValue = null;

        if (String.class.equals(fieldClazz)) {
            fieldValue = value.toString();
        } else if (Integer.class.equals(fieldClazz) || int.class.equals(fieldClazz)) {
            fieldValue = new Integer(value.toString());
        } else if (Long.class.equals(fieldClazz) || long.class.equals(fieldClazz)) {
            fieldValue = new Long(value.toString());
        } else if (java.util.Date.class.equals(fieldClazz)) {
            fieldValue = new Date(((java.sql.Timestamp) value).getTime());
        } else if (Float.class.equals(fieldClazz) || float.class.equals(fieldClazz)) {
            fieldValue = new Float(value.toString());
        } else if (Double.class.equals(fieldClazz) || double.class.equals(fieldClazz)) {
            fieldValue = new Double(value.toString());
        } else if (BigDecimal.class.equals(fieldClazz)) {
            fieldValue = new BigDecimal(value.toString());
        } else if (Boolean.class.equals(fieldClazz) || boolean.class.equals(fieldClazz)) {
            if (value.toString().equals("1") || value.toString().equalsIgnoreCase("true")) {
                value = "true";
            }

            fieldValue = new Boolean(value.toString());
        } else {
            log.error("this filed of value will not be seted because of unprocessed type  " + fieldClazz
                    + " and value = " + value + " and valueClass = " + value.getClass());

            return;
        }

        JdbcUtil.invokeSet(o, field, fieldValue);
    }

    /**
     * 忽略字段（被static或<code>@Transient</code>或Transient所修饰的字段）
     * 
     * @param field
     *            字段
     * @return true 忽略
     */
    private static boolean skipField(Field field) {
        int modifiers = field.getModifiers();

        return (field.getAnnotation(Transient.class) != null) || Modifier.isStatic(modifiers)
                || Modifier.isTransient(modifiers);
    }

    /**
     * 更新一个对象
     * 
     * @param dataSrc
     *            数据源
     * @param obj
     *            对象
     * @return 影响数据库的结果数
     */
    public static long updatePartitionObject(DataSrc dataSrc, Object obj) {
        String table = getTableName(obj.getClass());

        if (obj instanceof BasicGenericModel) {
            ((BasicGenericModel) obj).setUpdateTs(new Date());
        }

        Class objClazz = obj.getClass();
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(table).append(" SET ");

        List<Object> params = new ArrayList<Object>();

        for (Field field : getFields(objClazz)) {
            sql.append(getColumnName(field)).append("= ?,");
            params.add(JdbcUtil.invokeGet(obj, field));
        }

        sql.deleteCharAt(sql.length() - 1);
        sql.append(" WHERE ");

        for (Field field : getFields(objClazz)) {
            if (field.getAnnotation(Id.class) != null) {
                sql.append(getColumnName(field)).append(" = ? AND ");
                params.add(JdbcUtil.invokeGet(obj, field));
            }
            if (field.getAnnotation(PartitionKey.class) != null) {
                sql.append(getColumnName(field)).append(" = ? AND ");
                params.add(JdbcUtil.invokeGet(obj, field));
            }
        }

        sql.delete(sql.length() - 4, sql.length());

        if (Configs.JDBC.showSQL) {
            showSQL(dataSrc, sql.toString(), params.toArray());
        }

        return JDBCBuilder.update(false, dataSrc, sql.toString(), params.toArray());
    }

    /**
     * 更新一个对象
     * 
     * @param dataSrc
     *            数据源
     * @param obj
     *            对象
     * @return 影响数据库的结果数
     */
    public static long update(DataSrc dataSrc, Object obj) {
        String table = getTableName(obj.getClass());

        if (obj instanceof BasicGenericModel) {
            ((BasicGenericModel) obj).setUpdateTs(new Date());
        }

        Class objClazz = obj.getClass();
        StringBuilder sql = new StringBuilder();
        sql.append("UPDATE ").append(table).append(" SET ");

        List<Object> params = new ArrayList<Object>();

        for (Field field : getFields(objClazz)) {
            sql.append(getColumnName(field)).append("= ?,");
            params.add(JdbcUtil.invokeGet(obj, field));
        }

        sql.deleteCharAt(sql.length() - 1);
        sql.append(" WHERE ");

        for (Field field : getFields(objClazz)) {
            if (field.getAnnotation(Id.class) != null) {
                sql.append(getColumnName(field)).append(" = ? AND ");
                params.add(JdbcUtil.invokeGet(obj, field));
            }
        }

        sql.delete(sql.length() - 4, sql.length());

        if (Configs.JDBC.showSQL) {
            showSQL(dataSrc, sql.toString(), params.toArray());
        }

        return JDBCBuilder.update(false, dataSrc, sql.toString(), params.toArray());
    }

    /**
     * 批量更新
     * 
     * @param dataSrc
     *            数据源
     * @param c
     *            对象集合
     * @return 影响数据库的结果数
     */
    public static long updateBatch(DataSrc dataSrc, Collection<? extends Object> c) {
        long count = 0;

        for (Object object : c) {
            if (object == null) {
                continue;
            }

            count += update(dataSrc, object);
        }

        return count;
    }

    /**
     * 如果是插入操作会返回自增长的值，否则返回更新结果
     * 
     * @param dataSrc
     *            数据源
     * @param sql
     *            SQL语句
     * @param params
     *            参数
     * @return 影响数据库的结果数
     */
    public static long updateDbBySQL(DataSrc dataSrc, String sql, Object... params) {
        if (Configs.JDBC.showSQL) {
            showSQL(dataSrc, sql, params);
        }

        return JDBCBuilder.insert(false, dataSrc, sql, params);
    }
}
