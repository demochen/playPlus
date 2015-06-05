package snails.common.jdbc.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import snails.common.exception.CheckYourCodeException;

public class DBBuilder {
    private static final Logger log = LoggerFactory.getLogger(DBBuilder.class);
    public static final String TAG = "DBBuilder";

    static {
        log.info("Init jdbc pool");
        DBBuilder.initPool();
    }

    private static BasicDataSource baseDataSrc;
    private static BasicDataSource vmdbDataSrc;
    private static BasicDataSource propDataSrc;
    private static BasicDataSource concurrentDataSrc;

    public static void main(String[] args) {
    }

    public static Connection getConn() {
        return getConn(DataSrc.BASIC);
    }

    public static synchronized Connection getConn(DataSrc src) {
        try {
            switch (src) {
            case BASIC:
                return baseDataSrc.getConnection();

            case VMDB:
                return vmdbDataSrc.getConnection();

            case PROP:
                return propDataSrc.getConnection();

            case CONCURRENT:
                return concurrentDataSrc.getConnection();

            default:
                return baseDataSrc.getConnection();
            }
        } catch (SQLException e) {
            throw new CheckYourCodeException(e);
        }
    }

    public static synchronized void initPool() {
        Properties prop = Play.configuration;

        baseDataSrc = new BasicDataSource();
        vmdbDataSrc = new BasicDataSource();
        propDataSrc = new BasicDataSource();
        concurrentDataSrc = new BasicDataSource();

        int maxMainConnSize = Integer.parseInt(prop.getProperty("db.pool.maxSize", "1024"));
        int maxSlaveConnSize = 64;

        String defaultDriver = prop.getProperty("db.driver");
        String defaultUrl = prop.getProperty("db.url");
        String defaultUsername = prop.getProperty("db.user");
        String defaultPassword = prop.getProperty("db.pass");

        baseDataSrc.setDriverClassName(defaultDriver);
        baseDataSrc.setUrl(defaultUrl);
        baseDataSrc.setUsername(defaultUsername);
        baseDataSrc.setPassword(defaultPassword);
        baseDataSrc.setMaxActive(maxMainConnSize);

        vmdbDataSrc.setDriverClassName(prop.getProperty("vmdb.db.driver", defaultDriver));
        vmdbDataSrc.setUrl(prop.getProperty("vmdb.db.url", defaultUrl));
        vmdbDataSrc.setUsername(prop.getProperty("vmdb.db.user", defaultUsername));
        vmdbDataSrc.setPassword(prop.getProperty("vmdb.db.pass", defaultPassword));
        vmdbDataSrc.setMaxActive(maxSlaveConnSize);

        // itemDataSrc.setDriverClassName(prop.getProperty("item.db.driver",
        // defaultDriver));
        // itemDataSrc.setUrl(prop.getProperty("item.db.url", defaultUrl));
        // itemDataSrc.setUsername(prop.getProperty("item.db.user",
        // defaultUsername));
        // itemDataSrc.setPassword(prop.getProperty("item.db.pass",
        // defaultPassword));
        // itemDataSrc.setMaxActive(maxSlaveConnSize);

        // acookieDataSrc.setDriverClassName(prop.getProperty("acookie.db.driver",
        // defaultDriver));
        // acookieDataSrc.setUrl(prop.getProperty("acookie.db.url",
        // defaultUrl));
        // acookieDataSrc.setUsername(prop.getProperty("acookie.db.user",
        // defaultUsername));
        // acookieDataSrc.setPassword(prop.getProperty("acookie.db.pass",
        // defaultPassword));
        // acookieDataSrc.setMaxActive(maxSlaveConnSize);

        propDataSrc.setDriverClassName(prop.getProperty("prop.db.driver", defaultDriver));
        propDataSrc.setUrl(prop.getProperty("prop.db.url", defaultUrl));
        propDataSrc.setUsername(prop.getProperty("prop.db.user", defaultUsername));
        propDataSrc.setPassword(prop.getProperty("prop.db.pass", defaultPassword));
        propDataSrc.setMaxActive(maxSlaveConnSize);

        concurrentDataSrc.setDriverClassName(prop.getProperty("concurrent.db.driver", defaultDriver));
        concurrentDataSrc.setUrl(prop.getProperty("concurrent.db.url", defaultUrl));
        concurrentDataSrc.setUsername(prop.getProperty("concurrent.db.user", defaultUsername));
        concurrentDataSrc.setPassword(prop.getProperty("concurrent.db.pass", defaultPassword));
        concurrentDataSrc.setMaxActive(maxSlaveConnSize);
    }

    public enum DataSrc {
        BASIC, QUOTA, ITEM, ACOOKIE, PROP, CONCURRENT, VMDB;
    }
}
