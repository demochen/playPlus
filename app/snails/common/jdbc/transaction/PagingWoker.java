package snails.common.jdbc.transaction;

public abstract class PagingWoker {
    private int totalNum;
    private int pageSize;

    public PagingWoker(int totalNum, int pageSize) {
        super();
        this.totalNum     = totalNum;
        this.pageSize     = pageSize;
    }

    public void call() throws Exception {
        for (int i = 0; i < totalNum; i += pageSize) {
            if ((i + pageSize) > totalNum) {
                doWork(i, totalNum);
            } else {
                doWork(i, i + pageSize);
            }
        }
    }

    public abstract void doWork(int count, int pageSize);
}
