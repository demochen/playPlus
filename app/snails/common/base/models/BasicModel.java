package snails.common.base.models;

import java.io.Serializable;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * 基类，继承自BasicGenericModel，扩展ID
 * 
 */
@MappedSuperclass
@JsonIgnoreProperties(value = { "persistent", "entityId" })
public class BasicModel extends BasicGenericModel implements Serializable {
	/**
	 * 主键自增长
	 */
	@Id
	@GeneratedValue
	public Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
