package com.example.campaignassessment.repository;

import com.example.campaignassessment.model.Assessment;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryAssessmentRepository implements AssessmentRepository {

    private final ConcurrentHashMap<String, Assessment> store = new ConcurrentHashMap<>();

    @Override
    public Assessment save(Assessment assessment) {
        store.put(assessment.id(), assessment);
        return assessment;
    }

    @Override
    public Optional<Assessment> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Assessment> findAll() {
        return List.copyOf(store.values());
    }
}
