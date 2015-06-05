package snails.common.jdbc.config;

import java.io.File;

import play.Play;

public class Configs {
    public static class JDBC {
        public static final boolean showSQL = new Boolean(Play.configuration.getProperty("jdbc.showSQL", "false"));
        public static final boolean showTransaction = new Boolean(Play.configuration.getProperty(
                "jdbc.showTransaction", "false"));
        public static final boolean showResult = new Boolean(Play.configuration.getProperty("jdbc.showResult", "false"));
    }
    public static File configDir = new File(Play.applicationPath, "conf");

    public static File sqlDir = new File(configDir, "sql");
    public static final String JOB_PAKAGES=Play.configuration.getProperty("job.pkgs", "");
}
