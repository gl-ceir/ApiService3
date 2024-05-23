package com.gl.ceir.config.repository.app;

import com.gl.ceir.config.model.app.GdceData;
import com.gl.ceir.config.model.app.GdceRegisterImeiReq;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GdceDataRepository extends JpaRepository<GdceData, Long> {

    public GdceData save(GdceData gdceData);

}
