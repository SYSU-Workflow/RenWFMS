package org.sysu.renNameService.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.sysu.renNameService.entity.RenDomainEntity;

/**
 * Created by Skye on 2018/12/7.
 */

public interface RenDomainEntityRepository extends JpaRepository<RenDomainEntity, String> {
}
