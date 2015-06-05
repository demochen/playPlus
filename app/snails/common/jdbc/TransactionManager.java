package snails.common.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import snails.common.exception.JDBCException;
import snails.common.jdbc.config.Configs;
import snails.common.jdbc.transaction.DBBuilder;
import snails.common.jdbc.transaction.DBBuilder.DataSrc;
import snails.common.jdbc.transaction.JDBCBuilder;

public class TransactionManager {
    private final static org.slf4j.Logger log = LoggerFactory.getLogger(TransactionManager.class);
    private static final ThreadLocal<Integer> TX_STATUS = new ThreadLocal<Integer>();
    public static final int TX_NO_TX = 0; // 0
    public static final int TX_STARTED = 1 << 0; // 1
    public static final int TX_ROLLBACKED = 1 << 1; // 2
    public static final int TX_COMMITED = 1 << 2; // 4
    public static final int TX_AUTO = 1 << 3; // 8
    private static Map<DataSrc, ThreadLocal<Connection>> connections = new HashMap<DataSrc, ThreadLocal<Connection>>();

    static { 
        connections.put(DataSrc.BASIC, new ThreadLocal<Connection>());
    }

    public static void beginTransaction() {
        beginTransaction(false, false);
    }

    /**
     * 不要随意使用,否则以后的所有操作将自动提交
     */
    public static void beginAutoed() {
        setAuto();
    }

    public static void beginReadOnly() {
        if (Configs.JDBC.showTransaction) {
            log.warn("BeginReadOnly,ThreadId  {} ", Thread.currentThread().getId());
        }

        beginTransaction(false, true);
    }

    public static Connection getConnection(DataSrc src) {
        if (isAutoed()) {
            beginTransaction(true, false);
        }

        Connection conn = connections.get(src).get();

        if (conn == null) {
            log.debug("!!!!!!没有开启事务，将开启自动提交事务");
            setAuto();
            beginTransaction(true, false);
        } else {
            return conn;
        }

        return connections.get(src).get();
    }

    /**
     * 
     * @param autoCommit
     *            每次调用数据后会自动关闭，如果一次业务需要多次掉数据库，建议不要自动
     * @param readOnly
     *            只读 ，更新数据库会报异常
     */
    public static void beginTransaction(boolean autoCommit, boolean readOnly) {
        if (Configs.JDBC.showTransaction) {
            log.warn("BeginTransaction, ThreadId={} ", Thread.currentThread().getId());
        }

        if (isStarted()) {
            log.error("试图重新开启事务,请注意确认状态!重新开启事务意味着放弃之前的所有操作! ,ThreadId {} ", Thread.currentThread().getId());
        }

        try {
            if (!isStarted()) {
                for (Iterator<Entry<DataSrc, ThreadLocal<Connection>>> i = connections.entrySet().iterator(); i
                        .hasNext();) {
                    Entry<DataSrc, ThreadLocal<Connection>> entry = i.next();
                    Connection conn = DBBuilder.getConn(entry.getKey());
                    conn.setAutoCommit(autoCommit);
                    conn.setReadOnly(readOnly);
                    entry.getValue().set(conn);
                }

                if (autoCommit) {
                    setAuto();
                } else {
                    setStarted();
                }
            } else {
                log.error("未建立新数据库连接,将使用原数据库连接,ThreadId {} ", Thread.currentThread().getId());
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JDBCException(e);
        }
    }

    public static void rollBack() {
        if (!isStarted()) {
            log.error("当前的事务是自动提交--[直接return]-----;ThreadId={}", Thread.currentThread().getId());

            return;
        }

        log.error("RollBack ThreadId={} ", TX_STATUS.get(), Thread.currentThread().getId());

        try {
            for (Iterator<Entry<DataSrc, ThreadLocal<Connection>>> i = connections.entrySet().iterator(); i.hasNext();) {
                Entry<DataSrc, ThreadLocal<Connection>> entry = i.next();
                Connection conn = entry.getValue().get();
                conn.rollback();
                conn.close();
            }

            setRollBacked();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JDBCException(e);
        }
    }

    public static void commit() {
        if (Configs.JDBC.showTransaction) {
            log.warn("Commit ThreadId={} ", Thread.currentThread().getId());
        }

        if (!isStarted()) {
            log.error("未开启事务,----[直接return]-----;ThreadId={}", Thread.currentThread().getId());

            return;
        }

        try {
            for (Iterator<Entry<DataSrc, ThreadLocal<Connection>>> i = connections.entrySet().iterator(); i.hasNext();) {
                Entry<DataSrc, ThreadLocal<Connection>> entry = i.next();
                Connection conn = entry.getValue().get();
                conn.commit();
                conn.setAutoCommit(true);
                conn.close();
            }

            setCommited();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new JDBCException(e);
        }
    }

    private static void setRollBacked() {
        TX_STATUS.set(TX_ROLLBACKED);
    }

    private static void setCommited() {
        TX_STATUS.set(TX_COMMITED);
    }

    private static void setStarted() {
        TX_STATUS.set(TX_STARTED);
    }

    private static void setAuto() {
        TX_STATUS.set(TX_AUTO);
    }

    public static boolean isNoStarted() {
        return getStatus() == TX_NO_TX;
    }

    public static boolean isRollBacked() {
        return getStatus() == TX_ROLLBACKED;
    }

    public static boolean isStarted() {
        return getStatus() == TX_STARTED;
    }

    public static boolean isCommited() {
        return getStatus() == TX_COMMITED;
    }

    public static boolean isAutoed() {
        return getStatus() == TX_AUTO;
    }

    public static int getStatus() {
        if (TX_STATUS.get() == null) {
            TX_STATUS.set(TX_NO_TX);
        }

        return TX_STATUS.get();
    }

    /**
     * <pre>
     * 线程外单独开启一次,自动提交,一次性操作,保证不影响主事务
     * @author RIN 2013-7-18
     * @return
     * </pre>
     */
    public static long outThreadOperation(DataSrc src, String sql, Object... params) {
        Connection oldConn = null;
        Connection newConn = DBBuilder.getConn(src);

        try {
            newConn.setAutoCommit(false);
            newConn.setReadOnly(false);
        } catch (SQLException e1) {
            log.error(e1.getMessage(), e1);
        }

        oldConn = connections.get(src).get();
        connections.get(src).set(newConn);

        long key = 0;

        try {
            // 如果是插入操作会生成主键，如果是更新会返回更新结果
            key = JDBCBuilder.insert(false, src, sql, params);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            try {
                if (!newConn.isClosed()) {
                    try {
                        newConn.commit();
                        newConn.close();
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                log.debug(e.getMessage(), e);
            }
        }

        connections.get(src).set(oldConn);

        return key;
    }

    public static void close() {
        if (Configs.JDBC.showTransaction) {
            log.warn("close connection");
        }

        for (Iterator<Entry<DataSrc, ThreadLocal<Connection>>> i = connections.entrySet().iterator(); i.hasNext();) {
            Entry<DataSrc, ThreadLocal<Connection>> entry = i.next();
            Connection conn = entry.getValue().get();

            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                    throw new JDBCException(e);
                }
            }
        }
    }
}
