-- Executar uma vez em instalações existentes para permitir ativar/desativar gestores.
ALTER TABLE APP_USER ADD (
    ACCOUNT_ENABLED NUMBER(1,0) DEFAULT 1 NOT NULL
);

ALTER TABLE APP_USER ADD CONSTRAINT CK_APP_USER_ENABLED
    CHECK (ACCOUNT_ENABLED IN (0, 1));
