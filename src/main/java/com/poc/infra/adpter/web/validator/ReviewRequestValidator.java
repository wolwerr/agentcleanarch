package com.poc.infra.adpter.web.validator;

import com.poc.infra.exception.InvalidRequestException;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReviewRequestValidator {

    public void validate(String path) {
        if (path == null || path.isBlank()) {
            throw new InvalidRequestException("O parâmetro 'path' é obrigatório.");
        }

        if (path.length() > 1024) {
            throw new InvalidRequestException("O parâmetro 'path' é muito longo.");
        }
    }
}
