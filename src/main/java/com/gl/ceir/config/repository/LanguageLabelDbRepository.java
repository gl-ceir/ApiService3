/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.gl.ceir.config.model.LanguageLabelDb;
import org.springframework.data.jpa.repository.Query;

public interface LanguageLabelDbRepository extends JpaRepository<LanguageLabelDb, Long>, JpaSpecificationExecutor<LanguageLabelDb> {

    @Query(value = "select JSON_OBJECTAGG(label, english_name) as labelDetails from language_label_db where feature_name = :featureName", nativeQuery = true)
    public String getEnglishNameAndLabelFromFeatureName(String featureName);

    @Query(value = "select JSON_OBJECTAGG(label, khmer_name  ) as labelDetails from language_label_db where feature_name = :featureName", nativeQuery = true)
    public String getKhmerNameAndLabelFromFeatureName(String featureName);

}

/* 6:04    working labelDetails
public interface LanguageLabelDbRepository extends JpaRepository<LanguageLabelDb, Long>, JpaSpecificationExecutor<LanguageLabelDb> {

    @Query(value = "select  label ,english_name , khmer_name  from language_label_db where feature_name = ?1 ", nativeQuery = true)
    public List<LanguageLabelDb> getEnglishNameAndLabelFromFeatureName(String featureName);
    // make it String
}



 */
