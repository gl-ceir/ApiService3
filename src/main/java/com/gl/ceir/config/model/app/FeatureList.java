package com.gl.ceir.config.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;


@Entity
@AllArgsConstructor
@NoArgsConstructor
public class FeatureList {
    private static final long serialVersionUID = 1L;
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    private Integer featureMenuId;


    private String  link, name;

    //  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL, optional = false)
    //   @JoinColumn(name = "feature_list_id", nullable = false)

    //  @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, optional = false)

//    @JsonIgnore
//    @ManyToOne
//    @JoinColumn(name = "featureMenuId")
//    private FeatureMenu featureMenu;

    public Integer getFeatureMenuId() {
        return featureMenuId;
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public String toString() {
        return " {" +
                ", id=" + id +
                ", link='" + link + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
