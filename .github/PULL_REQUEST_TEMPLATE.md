## What

<!-- One-sentence description of the change -->

## Why

<!-- What problem does this solve? Reference issues if applicable -->

## How

<!-- Summary of the implementation approach. Key decisions and trade-offs. -->

## Testing

<!-- How did you verify this works? List specific test cases or manual verification steps -->

---

## Checklist

- [ ] I have run `./gradlew compileJava` locally and it passes
- [ ] I have run `./gradlew test` locally and all tests pass
- [ ] I have run `./gradlew build -x test` locally and it passes
- [ ] I have verified no secrets/keys/tokens are hardcoded
- [ ] My commits follow conventional commit format (feat:, fix:, refactor:, etc.)
- [ ] I have updated AGENTS.md if I discovered new patterns or gotchas
- [ ] For Mixin changes: I used the most precise injection type possible
- [ ] For Mixin changes: I used `cardboard$` prefix for all new methods
- [ ] I have not used `@Overwrite` for new code (existing @Overwrite is being refactored)

## Type of Change

- [ ] Bug fix (non-breaking change)
- [ ] New feature (non-breaking change)
- [ ] Breaking change
- [ ] Mixin refactor (@Overwrite to @Inject/@ModifyArg/@Redirect)
- [ ] Documentation update
- [ ] CI/CD or build change
