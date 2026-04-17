package org.example.galleryproject.controller.dto;

import java.util.List;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminRequestDtoValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void imageRequestRequiresTitle() {
        Set<ConstraintViolation<ImageRequestDto>> violations = validator.validate(
                new ImageRequestDto(" ", "Description")
        );

        assertFalse(violations.isEmpty());
    }

    @Test
    void imageRequestRejectsTooLongTitle() {
        String tooLongTitle = "a".repeat(121);
        Set<ConstraintViolation<ImageRequestDto>> violations = validator.validate(
                new ImageRequestDto(tooLongTitle, "Description")
        );

        assertFalse(violations.isEmpty());
    }

    @Test
    void imageVisibilityRequiresHiddenField() {
        Set<ConstraintViolation<ImageVisibilityRequestDto>> violations = validator.validate(
                new ImageVisibilityRequestDto(null)
        );

        assertFalse(violations.isEmpty());
    }

    @Test
    void imageTagsRejectsEmptyCollection() {
        Set<ConstraintViolation<ImageTagsRequestDto>> violations = validator.validate(
                new ImageTagsRequestDto(List.of())
        );

        assertFalse(violations.isEmpty());
    }

    @Test
    void imageTagsRejectsBlankTag() {
        Set<ConstraintViolation<ImageTagsRequestDto>> violations = validator.validate(
                new ImageTagsRequestDto(List.of("nature", " "))
        );

        assertFalse(violations.isEmpty());
    }

    @Test
    void validAdminPayloadsPassValidation() {
        Set<ConstraintViolation<ImageRequestDto>> imageViolations = validator.validate(
                new ImageRequestDto("Sunset", "Golden hour")
        );
        Set<ConstraintViolation<ImageVisibilityRequestDto>> visibilityViolations = validator.validate(
                new ImageVisibilityRequestDto(Boolean.TRUE)
        );
        Set<ConstraintViolation<ImageTagsRequestDto>> tagViolations = validator.validate(
                new ImageTagsRequestDto(List.of("nature", "outdoor"))
        );

        assertTrue(imageViolations.isEmpty());
        assertTrue(visibilityViolations.isEmpty());
        assertTrue(tagViolations.isEmpty());
    }
}
