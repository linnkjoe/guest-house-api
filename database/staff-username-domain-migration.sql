-- Normalize existing ADMIN/GESTOR usernames to the required @mbj.inn suffix.
-- The collision check prevents an update from changing which account owns a login.
DECLARE
    collision_count NUMBER;
BEGIN
    SELECT COUNT(*)
      INTO collision_count
      FROM app_user staff_user
      JOIN app_user existing_user
        ON LOWER(existing_user.username) = LOWER(
            CASE
                WHEN INSTR(staff_user.username, '@') > 0
                    THEN SUBSTR(staff_user.username, 1, INSTR(staff_user.username, '@') - 1) || '@mbj.inn'
                ELSE staff_user.username || '@mbj.inn'
            END
        )
     WHERE staff_user.user_role IN ('ADMIN', 'GESTOR')
       AND existing_user.user_id <> staff_user.user_id;

    IF collision_count > 0 THEN
        RAISE_APPLICATION_ERROR(-20001, 'Username collision: resolve duplicate team usernames before running this migration.');
    END IF;
END;
/

UPDATE app_user
   SET username = LOWER(
       CASE
           WHEN INSTR(username, '@') > 0
               THEN SUBSTR(username, 1, INSTR(username, '@') - 1) || '@mbj.inn'
           ELSE username || '@mbj.inn'
       END
   )
 WHERE user_role IN ('ADMIN', 'GESTOR')
   AND LOWER(username) NOT LIKE '%@mbj.inn';

UPDATE app_user
   SET username = LOWER(username)
 WHERE user_role IN ('ADMIN', 'GESTOR')
   AND username <> LOWER(username);

COMMIT;
