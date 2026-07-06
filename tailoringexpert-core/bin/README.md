# tailoringexpert-core

Business core of tailoringexpert platform.
Interfaces to be implemented by any tenant are marked as `@TenantInterface`.

## Implementation restrictions
`tailoringexpert-core` component must not depend on runtime on any library except
* lombok
* mapstruct
* log4j2
