# #42 Runner loses draft on reload without registration

- 2026-07-20T09:00:00Z: opened by octo-dev · state=OPEN · labels=[bug, runner] · assignees=[maintainer-a] · milestone=v0.9 · https://github.com/acme/app/issues/42

Without registration, a browser reload drops in-progress data. Expected: save-and-resume.

## Thread
- 2026-07-20T10:00:00Z — maintainer-a: Repro confirmed; localStorage draft is only written after registration.
- 2026-07-21T08:00:00Z — octo-dev: Proposed fix: write the draft on first field change, not on registration.
