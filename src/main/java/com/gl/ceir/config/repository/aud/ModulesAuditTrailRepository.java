package com.gl.ceir.config.repository.aud;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.gl.ceir.config.model.aud.ModulesAuditTrail;
import org.springframework.stereotype.Repository;
@Repository
public interface ModulesAuditTrailRepository extends JpaRepository<ModulesAuditTrail, Long>, JpaSpecificationExecutor<ModulesAuditTrail> {

    public ModulesAuditTrail getById(long id);

    //   public ModulesAuditTrail getBysaveId(ModulesAuditTrail modulesAuditTrail);


}
