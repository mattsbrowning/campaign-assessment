package com.example.campaignassessment.controller;

import com.example.campaignassessment.model.Assessment;
import com.example.campaignassessment.model.CampaignRequest;
import com.example.campaignassessment.repository.AssessmentRepository;
import com.example.campaignassessment.service.FeasibilityService;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/assessments")
public class AssessmentController {

    private final FeasibilityService feasibilityService;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentModelAssembler assembler;

    public AssessmentController(FeasibilityService feasibilityService,
                                AssessmentRepository assessmentRepository,
                                AssessmentModelAssembler assembler) {
        this.feasibilityService = feasibilityService;
        this.assessmentRepository = assessmentRepository;
        this.assembler = assembler;
    }

    @PostMapping
    public ResponseEntity<EntityModel<Assessment>> create(@Valid @RequestBody CampaignRequest request) {
        Assessment assessment = feasibilityService.assess(request);
        assessmentRepository.save(assessment);
        EntityModel<Assessment> model = assembler.toModel(assessment);
        return ResponseEntity
            .created(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
            .body(model);
    }

    @GetMapping("/{id}")
    public EntityModel<Assessment> getById(@PathVariable String id) {
        Assessment assessment = assessmentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No assessment found with id: " + id));
        return assembler.toModel(assessment);
    }

    @GetMapping
    public CollectionModel<EntityModel<Assessment>> getAll() {
        List<EntityModel<Assessment>> assessments = assessmentRepository.findAll()
            .stream()
            .map(assembler::toModel)
            .toList();
        return CollectionModel.of(assessments,
            linkTo(methodOn(AssessmentController.class).getAll()).withSelfRel());
    }
}
