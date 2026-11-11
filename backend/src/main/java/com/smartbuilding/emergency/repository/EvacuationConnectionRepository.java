package com.smartbuilding.emergency.repository;

import com.smartbuilding.emergency.model.EvacuationConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EvacuationConnectionRepository extends JpaRepository<EvacuationConnection, String> {
    List<EvacuationConnection> findByBuildingId(String buildingId);

    @Query("SELECT c FROM EvacuationConnection c WHERE c.building.id = :buildingId AND (c.fromNode.id = :nodeId OR (c.toNode.id = :nodeId AND c.bidirectional = true))")
    List<EvacuationConnection> findConnectedEdges(@Param("buildingId") String buildingId, @Param("nodeId") String nodeId);
}
