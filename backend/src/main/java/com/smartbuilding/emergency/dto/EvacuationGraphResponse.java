package com.smartbuilding.emergency.dto;

import java.util.List;

public record EvacuationGraphResponse(
        String buildingId,
        List<EvacuationNodeDto> nodes,
        List<EvacuationConnectionDto> connections
) {}
