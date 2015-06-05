package snails.common.base.models;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity(name = JobLock.TABLE_NAME)
public class JobLock extends BasicGenericModel {

    public static final String TABLE_NAME = "job_lock";

    @Id
    private String jobId;
    private String machineName;
    private Integer bitStatus;
    private Long lastLockTs;

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Integer getBitStatus() {
        return bitStatus;
    }

    public void setBitStatus(Integer bitStatus) {
        this.bitStatus = bitStatus;
    }

    public Long getLastLockTs() {
        return lastLockTs;
    }

    public void setLastLockTs(Long lastLockTs) {
        this.lastLockTs = lastLockTs;
    }
}
