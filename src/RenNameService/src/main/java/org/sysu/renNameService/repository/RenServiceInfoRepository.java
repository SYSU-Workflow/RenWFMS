package org.sysu.renNameService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.sysu.renCommon.entity.RenServiceInfo;

import java.util.List;

/**
 * Created by Skye on 2019/1/2.
 */
public interface RenServiceInfoRepository extends JpaRepository<RenServiceInfo, String> {

    @Query(value = "select location from ren_serviceinfo where locate('WFMSComponent_Engine_', interpreter_id) > 0", nativeQuery = true)
    List<String> findAllBOEngineLocation();

    @Query(value = "select location from ren_serviceinfo where locate('WFMSComponent_RS_', interpreter_id) > 0", nativeQuery = true)
    String findRSLocation();

}
