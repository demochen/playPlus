package snails.common.jdbc;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import snails.common.jdbc.transaction.DBBuilder.DataSrc;

public class JDBCBase { 
    
    private JDBCBase() {
    }
    
    public static <T> List<T> getResultListBySQLFile(final Class<T> objClazz, String _sqlFilePath) {
        return JDBCManager.getResultListBySQLFile(DataSrc.BASIC, objClazz, _sqlFilePath,null);
    }

    public static <T> List<T> getResultListBySQLFile(final Class<T> objClazz, String _sqlFilePath, Map _params) {
        return JDBCManager.getResultListBySQLFile(DataSrc.BASIC, objClazz, _sqlFilePath, _params);
    }

    public static <T> List<T> getResultList(final Class<T> objClazz, String query, Object... params) {
        return JDBCManager.getResultList(DataSrc.BASIC, objClazz, query, params);
    }

    public static <T> T getSingleResult(final Class<T> objClazz, String query, Object... params) {
        return JDBCManager.getSingleResult(DataSrc.BASIC, objClazz, query, params);
    }

    public static long update(Object obj) {
        return JDBCManager.update(DataSrc.BASIC, obj);
    }

    public static long updateIncludeFields(Object obj,String fields) {
        return JDBCManager.updateIncludeFields(DataSrc.BASIC, obj,fields);
    }
    
    public static long delete(Object obj) {
        return JDBCManager.delete(DataSrc.BASIC, obj);
    }

    public static long insert(Object obj) {
        return JDBCManager.insert(DataSrc.BASIC, obj);
    }

    public static long insertOrUpdate(Object obj) {
        return JDBCManager.insertOrUpdate(DataSrc.BASIC, obj);
    }
    
    public static long insertOrUpdateIncludeFields(Object obj,String fields) {
        return JDBCManager.insertOrUpdateIncludeFields(DataSrc.BASIC, obj,fields);
    }

    public static long insertOrUpdate(Object obj, String fields, Object... params) {
        return JDBCManager.insertOrUpdate(DataSrc.BASIC, obj, fields, params);
    }

    public static <T> T findById(final Class<T> objClazz, Object... params) {
        return JDBCManager.findById(DataSrc.BASIC, objClazz, params);
    }

    public static <T> T findByFields(final Class<T> objClazz, String fields, Object... params) {
        return JDBCManager.findByFields(DataSrc.BASIC, objClazz, fields, params);
    }

    public static long updateDbBySQL(String sql, Object... params) {
        return JDBCManager.updateDbBySQL(DataSrc.BASIC, sql, params);
    }

    public static long insertBySQL(String sql, Object... params) {
        return JDBCManager.updateDbBySQL(DataSrc.BASIC, sql, params);
    }

    public static long insertBatch(Collection<? extends Object> c) {
        return JDBCManager.insertBatch(DataSrc.BASIC, c);
    }

    public static long deleteBatch(Collection<? extends Object> c) {
        return JDBCManager.deleteBatch(DataSrc.BASIC, c);
    }

    public static long updateBatch(Collection<? extends Object> c) {
        return JDBCManager.updateBatch(DataSrc.BASIC, c);
    }

}
