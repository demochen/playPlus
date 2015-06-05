package snails.common.jdbc;

import java.io.File;
import java.util.Properties;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.SQLExec;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.Play;
import snails.common.jdbc.config.Configs;

public class DBUtil {

    private static final Logger log = LoggerFactory.getLogger(DBUtil.class);

    public static final String TAG = "DBUtil";

    public static void loadSqlFile(String string) {
        loadSqlFile(new File(Configs.sqlDir, string));
    } 

    public static void loadSqlFile(File file) { 
        Properties prop = Play.configuration;
 
        String defaultUrl = prop.getProperty("db.url");
        String driver = prop.getProperty("db.driver");
        String name = prop.getProperty("db.user"); 
        String pswd = prop.getProperty("db.pass");
        SQLExec sqlExec = new SQLExec(); 
        sqlExec.setDriver(driver);
        sqlExec.setUserid(name);
        sqlExec.setPassword(pswd);
        sqlExec.setUrl(defaultUrl);

        // 要执行的脚本
        sqlExec.setSrc(file);
        // 有出错的语句该如何处理
        sqlExec.setOnerror((SQLExec.OnError) (EnumeratedAttribute.getInstance(SQLExec.OnError.class, "abort")));
        sqlExec.setPrint(true); // 设置是否输出
        // 输出到文件 sql.out 中；不设置该属性，默认输出到控制台
        // sqlExec.setOutput(log.);
        sqlExec.setProject(new Project()); // 要指定这个属性，不然会出错
        sqlExec.execute();
        
        log.error("-----------------");
    }

}
