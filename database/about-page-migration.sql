-- Executar uma vez no schema da aplicacao antes de arrancar a versao atualizada.
CREATE TABLE ABOUT_PAGE (
    ID                 NUMBER(19,0)      NOT NULL,
    PAGE_TITLE         VARCHAR2(100 CHAR) NOT NULL,
    INTRODUCTION       VARCHAR2(240 CHAR) NOT NULL,
    PAGE_DESCRIPTION   VARCHAR2(2000 CHAR) NOT NULL,
    ADDRESS            VARCHAR2(200 CHAR) NOT NULL,
    CONTACT            VARCHAR2(30 CHAR) NOT NULL,
    IMAGE_CONTENT_TYPE VARCHAR2(50 CHAR),
    CONSTRAINT PK_ABOUT_PAGE PRIMARY KEY (ID),
    CONSTRAINT CK_ABOUT_PAGE_SINGLETON CHECK (ID = 1)
);

CREATE TABLE ABOUT_PAGE_IMAGE (
    ID         NUMBER(19,0) NOT NULL,
    IMAGE_DATA BLOB         NOT NULL,
    CONSTRAINT PK_ABOUT_PAGE_IMAGE PRIMARY KEY (ID),
    CONSTRAINT FK_ABOUT_PAGE_IMAGE FOREIGN KEY (ID) REFERENCES ABOUT_PAGE (ID)
);

INSERT INTO ABOUT_PAGE (
    ID, PAGE_TITLE, INTRODUCTION, PAGE_DESCRIPTION, ADDRESS, CONTACT
) VALUES (
    1,
    'Uma estadia acolhedora à sua espera',
    'Conforto, hospitalidade e tranquilidade no coração de Maluana.',
    'Na Mabjaia Inn, acreditamos que cada viagem merece um lugar onde possa descansar e sentir-se em casa. A nossa equipa recebe cada hóspede com atenção e simplicidade, num ambiente pensado para tornar a sua estadia confortável e memorável.',
    'Maluana Estrada N1',
    '82-09-69-206'
);

COMMIT;
