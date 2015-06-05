package snails.common.jdbc.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.concurrent.Callable;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import snails.common.jdbc.TransactionManager;
import snails.common.jdbc.exception.JDBCException;
import snails.common.jdbc.transaction.DBBuilder.DataSrc;
 
public class JDBCBuilder {
    private static final Logger log = LoggerFactory.getLogger(JDBCBuilder.class);
    public static final String TAG = "JDBCUtil";

    public static long update(boolean debug, String query, Object... args) {
        return update(debug, DataSrc.BASIC, query, args);
    }

    public static long update(boolean debug, DataSrc src, String query, Object... args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rset = null;

        try {
            conn = TransactionManager.getConnection(src);
            ps = conn.prepareStatement(query);

            setArgs(ps, args);

            int res = ps.executeUpdate();

            if (res <= 0) {
                return 0L;
            } else {
                return (long) res;
            }
        } catch (SQLException e) {
            log.error(query + e.getMessage(), e);
            throw new JDBCException(e);
        } finally {
            closeAll(rset, ps, conn);
        }
    }

    public static long insert(boolean debug, String query, Object... args) {
        return insert(debug, DataSrc.BASIC, query, args);
    }

    public static long insert(boolean debug, DataSrc src, String query, Object... args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rset = null;

        try {
            conn = TransactionManager.getConnection(src);
            ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            setArgs(ps, args);

            int res = ps.executeUpdate();

            if (res <= 0) {
                return 0L;
            }

            rset = ps.getGeneratedKeys();

            if (rset.next()) {
                return rset.getLong(1);
            } else {
                return (long) res;
            }
        } catch (SQLException e) {
            log.error(query + e.getMessage(), e);
            throw new JDBCException(e);
        } finally {
            closeAll(rset, ps, conn);
        }
    }

    public static final void closeAll(ResultSet rs, PreparedStatement ps, Connection conn) {
        closeQuitely(rs);
        closeQuitely(ps);

        if (TransactionManager.isAutoed()) {
            TransactionManager.close();
        }
    }

    public static final void closeQuitely(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JDBCException(e);
        }
    }

    public static final void closeQuitely(Connection conn) {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JDBCException(e);
        }
    }

    public static final void closeQuitely(PreparedStatement ps) {
        try {
            if (ps != null) {
                ps.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JDBCException(e);
        }
    }

    private static void setArgs(PreparedStatement ps, Object... args) throws SQLException {
        if (ArrayUtils.isEmpty(args)) {
            return;
        }

        for (int i = 0; i < args.length; i++) {
            Object obj = args[i];

            if (obj == null) {
                ps.setString(i + 1, null);
            } else if (obj instanceof Integer) {
                ps.setInt(i + 1, ((Integer) obj).intValue());
            } else if (obj instanceof Long) {
                ps.setLong(i + 1, ((Long) obj).longValue());
            } else if (obj instanceof Float) {
                ps.setFloat(i + 1, ((Float) obj).floatValue());
            } else if (obj instanceof String) {
                ps.setString(i + 1, obj.toString());
            } else if (obj instanceof Boolean) {
                ps.setBoolean(i + 1, ((Boolean) obj).booleanValue());
            } else if (obj instanceof Double) {
                ps.setDouble(i + 1, ((Double) obj).doubleValue());
            } else if (obj instanceof Date) {
                ps.setTimestamp(i + 1, new java.sql.Timestamp(((Date) obj).getTime()));
            }
        }
    }

    public static abstract class JDBCExecutor<T> implements Callable<T> {
        String query;
        Object[] params;
        boolean debug = false;
        protected DataSrc src = DataSrc.BASIC;

        public JDBCExecutor(String query, Object... params) {
            this.query = query;
            this.params = params;
        }

        public JDBCExecutor(DataSrc src, String query, Object... params) {
            this.query = query;
            this.params = params;
            this.src = src;
        }

        public JDBCExecutor(boolean debug, String query, Object... params) {
            this.debug = debug;
            this.query = query;
            this.params = params;
        }

        public T call() {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            T t = null;

            try {
                conn = TransactionManager.getConnection(src);
                ps = conn.prepareStatement(query);
                if (debug) {
                    log.info("[Build Query :]" + query.toString());
                }
                setArgs(ps, params);
                rs = ps.executeQuery();
                t = doWithResultSet(rs);
            } catch (SQLException e) {
                log.error(query + e.getMessage(), e);
                throw new JDBCException(e);
            } finally {
                closeAll(rs, ps, conn);
            }

            return t;
        }

        public abstract T doWithResultSet(ResultSet rs) throws SQLException;
    }

    public static long singleLongQuery(String query, Object... args) {
        return singleLongQuery(false, query, args);
    }

    public static long singleLongQuery(boolean debug, String query, Object... args) {
        return singleLongQuery(debug, DataSrc.BASIC, query, args);

    }

    public static long singleLongQuery(boolean debug, DataSrc src, String query, Object... args) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = DBBuilder.getConn(src);
            ps = conn.prepareStatement(query);
            setArgs(ps, args);

            if (debug) {
                log.info("[Build Query :]" + ps.toString());
            }
            rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            log.warn("Error SQL :" + ps, e);
        } finally {
            closeAll(rs, ps, conn);
        }
        return 0L;
    }
}
