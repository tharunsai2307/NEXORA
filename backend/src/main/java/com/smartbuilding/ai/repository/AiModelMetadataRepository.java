package com.smartbuilding.ai.repository;

import com.smartbuilding.ai.model.AiModelMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AiModelMetadataRepository extends JpaRepository<AiModelMetadata, String> {

    Optional<AiModelMetadata> findByModelName(String modelName);

    Optional<AiModelMetadata> findByModelType(String modelType);
}
