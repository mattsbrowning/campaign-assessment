package com.example.campaignassessment.repository;

import com.example.campaignassessment.model.Assessment;

import java.util.List;
import java.util.Optional;

public interface AssessmentRepository {
    Assessment save(Assessment assessment);
    Optional<Assessment> findById(String id);
    List<Assessment> findAll();
}
