package com.smartbuilding.emergency.repository;

import com.smartbuilding.emergency.model.EvacuationNode;
import com.smartbuilding.emergency.model.EvacuationNodeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvacuationNodeRepository extends JpaRepository<EvacuationNode, String> {
    List<EvacuationNode> findByBuildingId(String buildingId);
    List<EvacuationNode> findByBuildingIdAndFloorId(String buildingId, String floorId);
    List<EvacuationNode> findByBuildingIdAndNodeType(String buildingId, EvacuationNodeType nodeType);
    Optional<EvacuationNode> findByBuildingIdAndRoomId(String buildingId, String roomId);
    Optional<EvacuationNode> findByBuildingIdAndNodeCode(String buildingId, String nodeCode);
}
