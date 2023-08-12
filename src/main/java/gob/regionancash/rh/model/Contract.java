package gob.regionancash.rh.model;

import org.isobit.directory.model.Province;
import org.isobit.directory.model.Dependency;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import org.isobit.directory.model.Company;
import org.isobit.directory.model.People;
import lombok.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false,onlyExplicitlyIncluded = true)
@Entity
@Table(name = "contract")
public class Contract implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @EqualsAndHashCode.Include()
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Column(name = "people_id")
    private int peopleId;
    @Column(name = "people_id", insertable = false, updatable = false)
    private long peopleIdLong;
    @Column(name = "employee_id")
    private Integer employeeId;
    @JoinColumn(name = "company_id", referencedColumnName = "id_dir", insertable = false, updatable = false)
    @ManyToOne(optional = true)
    private Company company;
    @Column(name = "company_id")
    private Integer companyId;
    @Column(name = "dependency_id")
    private Integer dependencyId;
    @JoinColumn(name = "dependency_id", referencedColumnName = "id_dep", insertable = false, updatable = false)
    @ManyToOne(optional = true)
    private Dependency dependency;
    @JoinColumn(name = "people_id", referencedColumnName = "id_dir", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private People people;
    @JoinColumn(name = "position_id", referencedColumnName = "id", insertable = false, updatable = false)
    @ManyToOne(optional = true)
    private Position position;
    @Basic(optional = false)
    @Column(name = "remunerative_level_id")
    private Integer remunerativeLevelId;
    @Transient
    private String remunerativeLevelName;
    @Column(name = "position_id")
    private Integer positionId;
    @Column(name = "fecha_ini")
    @Temporal(TemporalType.DATE)
    private Date fechaIni;
    @Basic(optional = true)
    @Column(name = "fecha_fin")
    @Temporal(TemporalType.DATE)
    private Date fechaFin;
    @Transient
    private Province province;
    @Basic(optional = false)
    @NotNull
    @Column(name = "user_id")
    private int userId;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_reg")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaReg;
    @Basic(optional = true)
    private String document;
    @Basic(optional = true)
    private Boolean charge;
    private Boolean canceled=false;
    @Basic(optional = true)
    private Boolean active = Boolean.TRUE;
    @Basic(optional = false)
    @NotNull
    private boolean status = Boolean.TRUE;
    @Size(max = 6)
    @Column(name = "province_id")
    private String jurisdictionId;

}
