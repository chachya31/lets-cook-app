# Development Rules

## Backend Development Rules

### Tech Stack
- Language: Java 17
- Framework: Spring Boot 3.x
- Build Tool: Gradle (Groovy DSL)
- Testing: JUnit 5, Mockito
- Database: DynamoDB (via AWS SDK 2.x)

### Architecture
- Pattern: Clean Architecture
- Layers:
  - `presentation`: Controllers, DTOs
  - `application`: Use Cases
  - `domain`: Entities, Repository Interfaces
  - `infrastructure`: Repository Impls, Config
- Strict dependency rule: Inner layers (domain) must not depend on outer layers.

### Coding Standards
- Lombok: Use `@Builder`, `@Getter`, `@AllArgsConstructor` only. Avoid `@Data`.
- Tests: Unit tests are required for domain logic and use cases.

---

## Frontend Development Rules

### Tech Stack
- Language: TypeScript
- Framework: React 18+
- Build Tool: Vite
- State Management: Zustand
- Routing: React Router v7
- Styling: Tailwind CSS
- UI Library: shadcn/ui
- HTTP Client: Axios

### Architecture
- Pattern: Feature-based structure
- Directory structure:
  ```
  src/
    ├── features/
    │   ├── auth/ (components, api, stores)
    │   └── misc/ (LandingPage, etc.)
    ├── components/ (shared UI: Button, Layout)
    ├── lib/ (axios.ts, etc.)
    └── routes/ (index.tsx)
  ```
- Store location: `src/features/**/stores/`
- Feature isolation: Each feature should be self-contained

### Coding Standards
- React Router: Use `createBrowserRouter` (React Router v7 style)
- Zustand: Keep stores minimal and feature-specific
- Axios: Configure with `baseURL: /api`
- TypeScript: Strict mode enabled, avoid `any` type
- Components: Functional components with TypeScript interfaces for props

### Code Style
- **Quotes**: Use single quotes (`'`) for strings. Do not use double quotes.
- **Semicolons**: Do not use semicolons at the end of statements.
- **Consistency**: When adding `shadcn/ui` components, convert to match this style.

### UI/UX Guidelines
- **UI Library**: Use `shadcn/ui` as the standard component library. Do not create custom buttons or input forms; always use shadcn components (`@/components/ui/*`).
- **Responsive Design**: Strictly follow **Mobile First** approach. All layouts must prioritize mobile screens (320px~) and scale up for desktop using md/lg breakpoints.
- **Theme**:
  - Base: Zinc (Neutral)
  - Primary Color: Emerald Green (healthy and positive impression)
  - Use Tailwind classes; avoid hardcoded colors (hex values) whenever possible.
