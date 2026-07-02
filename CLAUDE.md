# Code style

- Prefer static imports in general: for constants/factory methods used unqualified elsewhere (enum values, `static` factories like `Clue.clueOf`), add `import static ...;` and drop the qualifier, rather than writing `Type.MEMBER` everywhere.
- `Cell` enum values (`FILLED`, `EMPTY`, `NO_VALUE`): use `import static ru.megadevelopers.nanogram.model.Cell.*;` and reference unqualified. Keep the plain `Cell` type import alongside it for method signatures/fields. Needed even in the `model` package itself - being in the same package as `Cell` does not give unqualified access to its enum constants.
