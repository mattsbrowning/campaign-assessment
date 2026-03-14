package com.example.campaignassessment.controller;

import com.example.campaignassessment.model.Assessment;
import com.example.campaignassessment.model.CampaignRequest;
import com.example.campaignassessment.repository.AssessmentRepository;
import com.example.campaignassessment.service.FeasibilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Assessments", description = "Submit campaign configurations and retrieve feasibility assessments")
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

    @Operation(
        summary = "Create a feasibility assessment",
        description = "Accepts a campaign configuration and returns an assessment containing "
            + "estimated reach, a recommended sample size, and any advisory warnings. "
            + "The created resource is returned with HATEOAS links."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Assessment created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request — one or more fields failed validation"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key"),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded")
    })
    @PostMapping
    public ResponseEntity<EntityModel<Assessment>> create(@Valid @RequestBody CampaignRequest request) {
        Assessment assessment = feasibilityService.assess(request);
        assessmentRepository.save(assessment);
        EntityModel<Assessment> model = assembler.toModel(assessment);
        return ResponseEntity
            .created(model.getRequiredLink(IanaLinkRelations.SELF).toUri())
            .body(model);
    }

    @Operation(
        summary = "Retrieve an assessment by ID",
        description = "Returns a previously created assessment identified by its UUID."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Assessment found"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key"),
        @ApiResponse(responseCode = "404", description = "No assessment found with the given ID")
    })
    @GetMapping("/{id}")
    public EntityModel<Assessment> getById(
            @Parameter(description = "UUID of the assessment to retrieve")
            @PathVariable String id) {
        Assessment assessment = assessmentRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No assessment found with id: " + id));
        return assembler.toModel(assessment);
    }

    @Operation(
        summary = "List all assessments",
        description = "Returns all assessments created during the current session. "
            + "Note: the in-memory store does not persist between restarts."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List returned successfully (may be empty)"),
        @ApiResponse(responseCode = "401", description = "Missing or invalid API key")
    })
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
