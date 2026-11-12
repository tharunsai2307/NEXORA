package com.smartbuilding.analytics.service;

import com.smartbuilding.analytics.dto.PostMortemRequest;
import com.smartbuilding.analytics.dto.PostMortemResponse;

import java.util.List;

public interface PostMortemService {

    PostMortemResponse getPostMortemByIncident(String incidentId);

    PostMortemResponse createOrUpdatePostMortem(String incidentId, PostMortemRequest request, String userId, String userName);

    PostMortemResponse finalizePostMortem(String incidentId, String reviewerName);

    List<PostMortemResponse> listPostMortems(String status);
}

