package com.gl.ceir.config.repository.app;

import com.gl.ceir.config.model.app.FeatureMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureMenuRepository extends JpaRepository<FeatureMenu, Long> {
    @Override
    public List<FeatureMenu> findAll();

    // SELECT DISTINCT e FROM Entity eJOIN e.relatedEntity rWHERE r.someField = :value
    @Query(value = "SELECT   distinct  f.*   FROM feature_menu f JOIN feature_submenu fs WHERE f.id=fs.feature_menu_id and  f.status=1 and f.language=:language and fs.status=1  ", nativeQuery = true)
    public List<FeatureMenu> getByQuery(String language);

    public List<FeatureMenu> getByLanguage(String language);

    //@Query(value = "SELECT * FROM feature_menu f INNER JOIN feature_submenu fs on f.id=fs.feature_menu_id WHERE f.status=:label and f.language=:language and fs.status=:status  ", nativeQuery = true)
    public List<FeatureMenu> getByLanguageAndFeatureSubmenusStatus(String language, int status);

}
