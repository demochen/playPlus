
package snails.common.jdbc.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
@SuppressWarnings("unused")
public abstract class TransactionSecurity<T> {

    private static final Logger log = LoggerFactory.getLogger(TransactionSecurity.class);

    public static final String TAG = "TransactionSecurity";

    public abstract T operateOnDB();

    protected boolean _newTxStarted = false;

    public T execute() {
        return execute(false, false);
    }

    public T execute(boolean readOnly, boolean fallback) {
        if (!JPA.isInsideTransaction()) {
//            log.info("start new transaction");
            JPAPlugin.startTx(readOnly);
            _newTxStarted = true;
        }
        T t = operateOnDB();
        if (_newTxStarted) {
            JPAPlugin.closeTx(fallback);
        }
        return t;
    }
}
