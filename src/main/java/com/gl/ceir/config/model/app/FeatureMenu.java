package com.gl.ceir.config.model.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
public class FeatureMenu {
    private static final long serialVersionUID = 1L;
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String logo, name;

    @OneToMany(mappedBy = "featureMenuId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<FeatureList> featureList;


    public List<FeatureList> getFeatureList() {
        return featureList;
    }

    public void setFeatureList(List<FeatureList> featureList) {
        this.featureList = featureList;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "FeatureMenu {" +
                ", id=" + id +
                ", logo='" + logo + '\'' +
                ", name='" + name + '\'' +
                ", featureList=" + featureList +
                '}';
    }


}
