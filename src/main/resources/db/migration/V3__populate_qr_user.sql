-- V3: Placeholder/migration notes for populating qr_numbers.user_id
-- WARNING: automatic mapping from legacy QR records to users requires a project-specific rule.
-- If you have a mapping (for example: qr_numbers.numero -> some table that links to users), implement the update below.

-- Example (NO-OP): verify DB connectivity. Replace with real UPDATE when mapping rule exists.
SELECT 1;

-- Example mapping (commented):
-- UPDATE qr_numbers q
-- SET user_id = u.id
-- FROM app_users u
-- WHERE /* your join condition here */;

-- If unsure, leave this migration as a manual step and perform data migration with a controlled script.
