package gob.regionancash.node.dto;

import gob.regionancash.node.model.NodeRevision;
import lombok.Data;

@Data
public class NodeDTO {

    private Integer id;
    private Integer vid;

    private int created;
    private int changed;

    private String url;

    private NodeRevision revision;
}