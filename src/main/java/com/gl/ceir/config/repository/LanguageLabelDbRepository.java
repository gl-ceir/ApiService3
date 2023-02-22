/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.gl.ceir.config.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.gl.ceir.config.model.LanguageLabelDb;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import com.gl.ceir.config.model.EnglishLabel;

public interface LanguageLabelDbRepository extends JpaRepository<LanguageLabelDb, Long>, JpaSpecificationExecutor<LanguageLabelDb> {

    @Query(value = "select  label ,english_name  from language_label_db where feature_name = ?1 ", nativeQuery = true)
    public List<EnglishLabel> getEnglishNameAndLabelFromFeatureName(String featureName);
    // make it String

}

/* 6:04    working
public interface LanguageLabelDbRepository extends JpaRepository<LanguageLabelDb, Long>, JpaSpecificationExecutor<LanguageLabelDb> {

    @Query(value = "select  label ,english_name , khmer_name  from language_label_db where feature_name = ?1 ", nativeQuery = true)
    public List<LanguageLabelDb> getEnglishNameAndLabelFromFeatureName(String featureName);
    // make it String
}



 */
