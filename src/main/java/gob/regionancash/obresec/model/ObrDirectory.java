package gob.regionancash.obresec.model;

import java.io.Serializable;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.isobit.directory.model.Company;
import org.isobit.directory.model.People;

import gob.regionancash.rh.model.Contract;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false,onlyExplicitlyIncluded = true)
@Entity
@Table(name = "obr_directorio")
@XmlRootElement
public class ObrDirectory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @EqualsAndHashCode.Include()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    @Size(max = 100)
    @Column(name = "company")
    private String companyName;
    @Column(name = "company_id")
    private Long companyId;
    @Transient
    private Company company;
    @Transient
    private Contract contract;
    @Size(max = 100)
    private String position;
    @JoinColumn(name = "people_id", referencedColumnName = "id_dir")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private People people;
    @Column(name = "position_id")
    private Integer contractId;
    // @Pattern(regexp="^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$", message="Invalid phone/fax format, should be as xxx-xxx-xxxx")//if the field contains phone or fax number consider using this annotation to enforce field validation
    @Size(max = 100)
    private String phone;
    @Size(max = 100)
    private String address;

}
