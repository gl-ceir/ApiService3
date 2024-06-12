package com.gl.ceir.config.repository.app;

import com.gl.ceir.config.model.app.FeatureList;
import com.gl.ceir.config.model.app.FeatureMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeatureListRepository extends JpaRepository<FeatureList, Long> {
    //JpaSpecificationExecutor<FeatureMenu>     , CrudRepository<FeatureMenu, Long>

    @Override
    List<FeatureList> findAll();

    List<FeatureList> findByFeatureMenuId(Long l);
}
