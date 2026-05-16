# Week 1, Day 1 — Initialise multi-module Maven project and market-data-service skeleton

## Context

This is the foundation ticket for PortfolioRisk. We need a single GitHub repository that will eventually host multiple Spring Boot services — `market-data-service`, `risk-calc-service`, and potentially others. Starting with a Maven multi-module layout from day one avoids the pain of migrating a flat single-module project later, which is the kind of "we'll fix it when it matters" call that costs real teams weeks.

The first module is `market-data-service` — the entry point for all external price data. This ticket covers everything from repo creation through to a running `/health` endpoint. By the end of the day there should be a green build and a service you can curl.

## Acceptance Criteria

- [ ] Public GitHub repository exists on my account named `PortfolioRisk`.
- [ ] Repo contains a parent `pom.xml` at the root with `<packaging>pom</packaging>` and a `<modules>` block listing `market-data-service`.
- [ ] Parent `pom.xml` declares Java 21 in both `<maven.compiler.source>` and `<maven.compiler.target>`.
- [ ] Parent `pom.xml` imports the Spring Boot 3.3.x BOM in `<dependencyManagement>` (`spring-boot-dependencies`, scope `import`, type `pom`).
- [ ] `market-data-service/pom.xml` inherits from the parent (`<parent>` block) and declares dependencies `spring-boot-starter-web` and `spring-boot-starter-actuator` with **no `<version>` tags** — versions come from the BOM.
- [ ] `market-data-service/src/main/java/com/portfoliorisk/marketdata/MarketDataServiceApplication.java` exists with a standard Spring Boot main class (`@SpringBootApplication`).
- [ ] `market-data-service/src/main/java/com/portfoliorisk/marketdata/controller/HealthController.java` exists and exposes a hand-written `GET /health` endpoint. This is **not** the Actuator built-in — it's a `@RestController` I control.
- [ ] Running `curl localhost:8080/health` against the running service returns HTTP 200 with body exactly `{"status":"UP","service":"market-data-service"}`.
- [ ] `market-data-service/src/main/resources/application.yml` exists (even if minimal — at least sets `spring.application.name: market-data-service`).
- [ ] Running `mvn clean package` from the repo root completes with `BUILD SUCCESS` and produces `market-data-service/target/market-data-service-*.jar`.
- [ ] `.gitignore` excludes `target/`, `.idea/`, `*.iml`, and `.DS_Store`.
- [ ] An initial commit with message `chore: scaffold multi-module Maven project` is pushed to `main`.

## Implementation Notes

- **No archetype for the parent.** IntelliJ's `New → Project → Maven` flow lets you create a plain project with no archetype. The parent should have no `src/` directory at all — just the `pom.xml`. Archetypes will generate a `src/` you have to delete.
- **The BOM is the point of multi-module.** The whole reason for declaring Spring Boot dependencies in the parent's `<dependencyManagement>` is so child modules don't need to repeat versions. If you find yourself writing `<version>` inside a child module's dependency, stop — that's the signal something's wrong with the parent.
- **Hand-rolled `/health`, not Actuator's.** Yes, `spring-boot-starter-actuator` ships with `/actuator/health` for free. You're writing your own anyway because (a) it gives you a controlled response shape rather than Actuator's nested JSON, and (b) writing a `@RestController` is the most basic Spring Boot exercise and it would be silly to skip it. You'll wire up Actuator's health indicators properly in a later ticket — for now they're just along for the ride.
- **Package naming.** Use `com.portfoliorisk.marketdata` as the base package for this module. Future modules will be `com.portfoliorisk.riskcalc`, etc.
- **Don't add Spring Initializr's `HELP.md` or `mvnw` wrapper.** Keep the repo clean. We assume Maven is installed locally; the wrapper is noise at this stage.

## Dependencies

None — this is the first ticket.

## Estimate

2 hours.

## Definition of Done

Someone cloning the repo on a clean machine with Java 21 and Maven installed can run `mvn clean package` from the root, then `java -jar market-data-service/target/market-data-service-*.jar`, then `curl localhost:8080/health` and see `{"status":"UP","service":"market-data-service"}`.

## Out of Scope

- Docker, Docker Compose, Postgres — that's Day 2.
- GitHub Actions CI — that's Day 3.
- Any database, entity, or repository code — that's Day 4.
- Any Kafka producer or consumer — that's Week 2.
- Actuator's built-in health indicators wired to anything real — later.
- The `risk-calc-service` module — Week 2 extracts that.
- Tests of any kind — Day 1 is scaffolding only. Day 4 onwards introduces tests.

## Status

Submitted for review.