package gob.regionancash.content.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

@Entity
@Table(name = "dru_blocks")
public class Block extends PanacheEntityBase {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "bid")
    private Integer bid;

    @Basic(optional = false)
    @Column(name = "module")
    private String module;

    @Basic(optional = false)
    @Column(name = "delta")
    private String delta;

    @Basic(optional = false)
    @Column(name = "theme")
    private String theme;

    @Basic(optional = false)
    @Column(name = "status")
    private short status;

    @Basic(optional = false)
    @Column(name = "weight")
    private short weight;

    @Basic(optional = false)
    @Column(name = "region")
    private String region;

    @Basic(optional = false)
    @Column(name = "custom")
    private short custom;

    @Basic(optional = false)
    @Column(name = "throttle")
    private short throttle;

    @Basic(optional = false)
    @Column(name = "visibility")
    private short visibility;

    @Basic(optional = false)
    @Column(name = "pages")
    private String pages;

    @Basic(optional = true)
    @Column(name = "title")
    private String title = "";

    @Basic(optional = false)
    @Column(name = "cache")
    private short cache;

    @Transient
    private Object ext;

}