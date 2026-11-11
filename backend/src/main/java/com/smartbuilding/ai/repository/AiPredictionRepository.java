package com.smartbuilding.ai.repository;

import com.smartbuilding.ai.model.AiPrediction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiPredictionRepository extends JpaRepository<AiPrediction, String> {

    List<AiPrediction> findByRoomIdOrderByPredictionTimestampDesc(String roomId);

    Optional<AiPrediction> findFirstByRoomIdOrderByPredictionTimestampDesc(String roomId);

    Page<AiPrediction> findByRoomFloorBuildingIdOrderByPredictionTimestampDesc(String buildingId, Pageable pageable);

    @Query("SELECT p FROM AiPrediction p WHERE p.room.id = :roomId ORDER BY p.predictionTimestamp DESC")
    Page<AiPrediction> findPagedByRoomId(@Param("roomId") String roomId, Pageable pageable);
}

