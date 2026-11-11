package com.smartbuilding.emergency.repository;

import com.smartbuilding.emergency.model.EvacuationRoomStatus;
import com.smartbuilding.emergency.model.RoomEvacuationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvacuationRoomStatusRepository extends JpaRepository<EvacuationRoomStatus, String> {
    List<EvacuationRoomStatus> findBySessionId(String sessionId);
    Optional<EvacuationRoomStatus> findBySessionIdAndRoomId(String sessionId, String roomId);
    long countBySessionIdAndStatus(String sessionId, RoomEvacuationState status);
}
