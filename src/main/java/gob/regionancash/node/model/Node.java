package gob.regionancash.node.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.*;
import org.isobit.util.OptionMap;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dru_node")
public class Node extends PanacheEntityBase {

    @Id
    @EqualsAndHashCode.Include()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "nid")
    private Integer id;

    private Integer vid;

    @Transient
    private NodeRevision revision;

    private static OptionMap ESTADO_MAP = new OptionMap();

    private static final long serialVersionUID = 1L;

    static {
        ESTADO_MAP.put(Integer.valueOf(0), "BORRADOR");
        ESTADO_MAP.put(Integer.valueOf(1), "PUBLICADO");
    }

    public OptionMap getESTADO_MAP() {
        return ESTADO_MAP;
    }

    @Transient
    private NodeRevision nodeRevision;

    @Basic(optional = false)
    @Column(name = "type")
    private String type;

    @Basic(optional = false)
    @Column(name = "language")
    private String language;

    @Basic(optional = false)
    @Column(name = "title")
    private String title;

    @Basic(optional = false)
    @Column(name = "uid")
    private int uid;

    @Transient
    private User user;

    @Basic(optional = false)
    @Column(name = "status")
    private int status;

    @Basic(optional = false)
    @Column(name = "created")
    private int created;

    @Basic(optional = false)
    @Column(name = "changed")
    private int changed;

    @Basic(optional = false)
    @Column(name = "_comment")
    private int comment;

    @Basic(optional = false)
    @Column(name = "promote")
    private int promote;

    @Basic(optional = false)
    @Column(name = "moderate")
    private int moderate;

    @Basic(optional = false)
    @Column(name = "sticky")
    private int sticky;

    @Basic(optional = false)
    @Column(name = "tnid")
    private long tnid;

    @Basic(optional = false)
    @Column(name = "translate")
    private int translate;

    @Transient
    private Object ext;
}
