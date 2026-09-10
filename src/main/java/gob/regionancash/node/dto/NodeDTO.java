package gob.regionancash.node.dto;

import gob.regionancash.node.model.Node;
import gob.regionancash.node.model.NodeRevision;
import org.isobit.app.model.User;
import lombok.Data;

@Data
public class NodeDTO {

    private Integer id;
    private Integer vid;

    private int created;
    private int changed;

    private String url;

    private NodeRevision revision;

    private String type;

    private String language;

    private String title;

    private int uid;

    private User user;

    private int status;

    private int comment;

    private int promote;

    private int moderate;

    private int sticky;

    private long tnid;

    private int translate;

    public NodeDTO(Node node) {
        this.id = node.getId();
        this.vid = node.getVid();

        this.type = node.getType();
        this.language = node.getLanguage();
        this.title = node.getTitle();

        this.uid = node.getUid();
        this.user = node.getUser();

        // Drupal guarda timestamps Unix en segundos.
        // El front los recibe en milisegundos.
        this.created = node.getCreated() * 1000;
        this.changed = node.getChanged() * 1000;

        this.status = node.getStatus();
        this.comment = node.getComment();
        this.promote = node.getPromote();
        this.moderate = node.getModerate();
        this.sticky = node.getSticky();

        this.tnid = node.getTnid();
        this.translate = node.getTranslate();

        this.url = node.getUrl();
        this.revision = node.getRevision();
    }
}