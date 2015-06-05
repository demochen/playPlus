
package snails.common.jdbc.transaction;

import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.JPA;
@SuppressWarnings("unused")
public class JPATransactionManager {

    private static final Logger log = LoggerFactory.getLogger(JPATransactionManager.class);

    public static final String TAG = "JPATransactionManager";

    public static final void clearEntities() {
        if (!JPA.isInsideTransaction()) {
//            log.warn("Not Inside Transaction, No Need to clear....");
            return;
        }
        // JPA.isEnabled()
//        log.info("Clear Entities");
        EntityManager mananger = JPA.em();
        mananger.flush();
        mananger.clear();
    }
}
