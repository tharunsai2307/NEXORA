package com.smartbuilding.emergency.repository;

import com.smartbuilding.emergency.model.EvacuationRoute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvacuationRouteRepository extends JpaRepository<EvacuationRoute, String> {
    List<EvacuationRoute> findByBuildingId(String buildingId);
    List<EvacuationRoute> findBySessionId(String sessionId);
    Optional<EvacuationRoute> findFirstByOriginRoomIdOrderByCalculatedAtDesc(String originRoomId);
    void deleteByBuildingId(String buildingId);
    void deleteBySessionId(String sessionId);
}
