## Refactoring notes

### Approach

The initial implementation had most of the business logic inside the controller.  
I refactored the code to separate responsibilities and make the business rules easier to test and maintain.

Main changes:

- Moved order processing logic from the controller to `OrderProcessingService`.
- Replaced the product type `if/else` block with a Strategy-based approach using `ProductProcessor`.
- Added dedicated processors for each product type:
    - `NormalProductProcessor`
    - `SeasonalProductProcessor`
    - `ExpirableProductProcessor`
- Added a `Clock` bean to make date-based rules deterministic and easier to test.
- Centralized product type constants in `ProductTypes`.
- Added `@Transactional` to order processing.
- Replaced direct `.get()` usage with an explicit exception when an order is not found.
- Added unit tests for product processors and order processing.
- Improved the integration test to verify stock updates and notifications.

### Tests

From the `api` directory:

```bash
./mvnw test
./mvnw integration-test
./mvnw verify