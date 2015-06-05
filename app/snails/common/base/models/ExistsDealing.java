package snails.common.base.models;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity(name = ExistsDealing.TABLE_NAME)
public class ExistsDealing {

    public static final String TABLE_NAME = "dealing";
    
    @Id
    @GeneratedValue
    public long Id;
    
    public String nick;

    public String  dealing;
    
    public long modifedTime;
    

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    

    public long getId() {
        return Id;
    }

    public void setId(long id) {
        Id = id;
    }

    public long getModifedTime() {
        return modifedTime;
    }

    public void setModifedTime(long modifedTime) {
        this.modifedTime = modifedTime;
    }

    public String getDealing() {
        return dealing;
    }

    public void setDealing(String dealing) {
        this.dealing = dealing;
    }

}
