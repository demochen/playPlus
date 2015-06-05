package snails.common.base.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.GenericModel;

@JsonIgnoreProperties(value = { "persistent" })
public class ShardGenericModel extends GenericModel {

    @SuppressWarnings("unused")
    private final static Logger log = LoggerFactory.getLogger(ShardGenericModel.class);

    public static long genUserIdHashKey(Long userId) {
        return userId % 16;
    }

    public static String genUserIdShardQuery(String query, Long userId) {
        String key = String.valueOf(genUserIdHashKey(userId));
        String shardSql = genShardQuery(query, key);
//        log.warn("shardSql:" + shardSql);
        return shardSql;
    }

    public static String genShardQuery(String query, String key) {
        query = query.replaceAll("%s", "~~");
        query = query.replaceAll("%", "##");
        query = query.replaceAll("~~", "%s");

        String formQuery = String.format(query, key);
        return formQuery.replaceAll("##", "%");
    }
}
