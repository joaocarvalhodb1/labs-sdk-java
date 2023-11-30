package br.com.labs.labssdkjava.exceptionhandler;

import java.io.Serial;

public abstract class EntityNotFound extends BusinessException {
        @Serial
        private static final long serialVersionUID = 1L;

        public EntityNotFound(String message) {
            super(message);
        }

    }
