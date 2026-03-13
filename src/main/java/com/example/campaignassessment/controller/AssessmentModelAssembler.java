package com.example.campaignassessment.controller;

import com.example.campaignassessment.model.Assessment;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class AssessmentModelAssembler
        implements RepresentationModelAssembler<Assessment, EntityModel<Assessment>> {

    @Override
    public EntityModel<Assessment> toModel(Assessment assessment) {
        return EntityModel.of(assessment,
            linkTo(methodOn(AssessmentController.class).getById(assessment.id())).withSelfRel(),
            linkTo(methodOn(AssessmentController.class).getAll()).withRel("assessments")
        );
    }
}
