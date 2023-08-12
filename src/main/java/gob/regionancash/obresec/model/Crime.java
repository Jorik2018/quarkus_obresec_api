package gob.regionancash.obresec.model;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false,onlyExplicitlyIncluded = true)
@Entity
@Table(name = "crime")
public class Crime  extends PanacheEntityBase {

    private static final long serialVersionUID = 1L;
    @Id
    @EqualsAndHashCode.Include()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer id;
    
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha")
    @Temporal(TemporalType.DATE)
    private Date fecha;
    @Column(name = "crime_type_id")
    private Integer crimeTypeId;
    @JoinColumn(name = "crime_type_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private CrimeType crimeType;
    @Basic(optional = false)
    @NotNull
    @Size(min = 2, max = 6)
    @Column(name = "district_id")
    private String districtId;
    private Double lat;
    private Double lon;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_reg")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaReg;
    @Basic(optional = false)
    @NotNull
    @Column(name = "usuario_id")
    private int uid;
    @Transient
    private Object ext;
    private String description;
    @Transient
    private String dependencyName;
    @Basic
    @Column(name = "victim_sex")
    private Character victimSex = null;
    @Basic
    @Column(name = "victim_age")
    private Short victimAge = null;
    @Basic
    @Column(name = "victim_country")
    private Integer victimCountry = null;
    @Basic
    @Column(name = "criminal_sex")
    private Character criminalSex = null;
    @Basic
    @Column(name = "criminal_age")
    private Short criminalAge = null;
    @Basic
    @Column(name = "criminal_country")
    private Integer criminalCountry = null;
    @Basic
    @Column(name = "dependency_id")
    private Integer dependencyId = null;
    @Basic
    @Column(name = "directory_id")
    private Integer directoryId = null;
    
    
    @Basic
    private boolean canceled;
    
    private String observation;

    private String address;

}
