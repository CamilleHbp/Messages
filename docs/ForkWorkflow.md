# Fork workflow

Create each feature branch from the upstream base, using the `codex/` prefix. Keep each branch independently reviewable and push it to the personal fork for upstream pull requests. Merge feature branches into the fork's `main` so the personal build includes them together. Keep personal signing, release versions, and download publication configuration on `main` rather than in upstream feature PRs.

Keep `upstream` pointing at `an1ndra/Messages` and `origin` at `CamilleHbp/Messages`. Fetch upstream updates, integrate them into `main`, and rebase unpublished feature work as needed. Do not force-push published feature branches without authorization. The scripts submodule follows matching branch names in `CamilleHbp/Messages-scripts`; publish its commits before updating app pointers.

Every UI feature includes JVM tests, a rerunnable emulator regression, and before/after captures using synthetic messages. Build and verify both APK variants after final Android input changes. Publish qualifying combined releases to the shared download site. Do not install on a physical phone unless requested.
