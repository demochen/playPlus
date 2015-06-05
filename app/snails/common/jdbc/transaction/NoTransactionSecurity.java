package snails.common.jdbc.transaction;

import play.db.jpa.JPA;
import play.db.jpa.JPAPlugin;

public abstract class NoTransactionSecurity<T> {
	public abstract T noOpOnDB();

	protected boolean _TxFinished = false;

	public T execute() {
		return execute(false, false);
	}

	public T execute(boolean readOnly, boolean fallback) {
		if (JPA.isInsideTransaction()) {
			JPAPlugin.closeTx(fallback);
			_TxFinished = true;
		}
		T t = noOpOnDB();
		if (_TxFinished) {
			JPAPlugin.startTx(readOnly);
		}
		return t;
	}

	public static NoTransactionSecurity<Void> EMPYT_NO_TRANSACTION_SECURITY = new NoTransactionSecurity<Void>() {
		@Override
		public Void noOpOnDB() {
			return null;
		}
	};
}
