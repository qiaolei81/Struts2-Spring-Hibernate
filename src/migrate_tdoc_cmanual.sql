-- Migration: strip HTML anchor tags from TDOC.CMANUAL
-- Reason: t1 fix changed cmanual from storing full HTML (<a href='upload/foo.pdf'>foo.pdf</a>)
--         to storing plain filenames only (foo.pdf).
--         Existing rows written before the fix must be updated.
--
-- Database: Oracle
-- Table:    TDOC
-- Column:   CMANUAL
--
-- IMPORTANT: Run this migration ONCE per environment (dev / staging / prod)
--            BEFORE deploying the t1 code change.
--            Verify row count before and after to confirm all rows were updated.
--
-- NOTE: Oracle POSIX ERE does not support lazy quantifiers (.*?).
--       This script uses [^']+ and [^<]* instead, which are equivalent
--       for the known data format: <a href='upload/FILENAME'>FILENAME</a>

-- Step 1: preview affected rows (run this first, do NOT commit)
SELECT cid, cmanual,
       REGEXP_REPLACE(cmanual, '<a href=''upload/([^'']+)''>[^<]*</a>', '\1') AS cmanual_fixed
FROM tdoc
WHERE cmanual LIKE '<a href=%';

-- Step 2: apply the migration
UPDATE tdoc
SET cmanual = REGEXP_REPLACE(cmanual, '<a href=''upload/([^'']+)''>[^<]*</a>', '\1')
WHERE cmanual LIKE '<a href=%';

-- Step 3: verify no HTML remains
SELECT COUNT(*) AS remaining_html_rows
FROM tdoc
WHERE cmanual LIKE '<a href=%';
-- Expected result: 0

COMMIT;
