package snails.common.base.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import play.db.jpa.Model;

@JsonIgnoreProperties(value = { "persistent" })
public class ShardModel extends Model {

    public static long genUserIdHashKey(Long userId) {
        return userId % 16;
    }

    public static String genUserIdShardQuery(String query, Long userId) {
        String key = String.valueOf(genUserIdHashKey(userId));
        return genShardQuery(query, key);
    }

    public static String genShardQuery(String query, String key) {
        query = query.replaceAll("%s", "~~");
        query = query.replaceAll("%", "##");
        query = query.replaceAll("~~", "%s");

        String formQuery = String.format(query, key);

        return formQuery.replaceAll("##", "%");
    }
}
