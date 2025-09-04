package io.metersphere.whitebox.repository;

import io.metersphere.whitebox.entity.BusinessRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BusinessRuleRepository extends JpaRepository<BusinessRule, Long> {
    List<BusinessRule> findByServiceName(String serviceName);
    List<BusinessRule> findByIsCoreRuleTrue();
}