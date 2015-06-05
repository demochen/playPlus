package snails.common.jdbc.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;
import play.jobs.Job;

public abstract class DelayedTransactionSecurity<T> {

	private static final Logger log = LoggerFactory
			.getLogger(DelayedTransactionSecurity.class);

	public static final String TAG = "DelayedTransactionSecurity";

	public abstract T operateOnDB();

	protected boolean _newTxStarted = false;

	public void execute(final long ts) {
		new Job<T>() {
			@Override
			public T doJobWithResult() {
				log.info("[Now, Delayed Time]" + ts);
				try {
					Thread.sleep(ts);
				} catch (InterruptedException e) {
					log.warn(e.getMessage());
				}
				log.info("[Delayed Time Over]");
				return DelayedTransactionSecurity.this.execute(false, false);
			}
		}.now();
	}

	public T execute(boolean readOnly, boolean fallback) {
		if (!JPA.isInsideTransaction()) {
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
