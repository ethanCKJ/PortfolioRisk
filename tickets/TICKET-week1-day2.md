# Week 1, Day 2 — Containerise market-data-service and add Postgres to Docker Compose

## Context

Day 1 left us with a service that runs on the host JVM. That works for a few minutes of development but doesn't survive contact with reality — every team I'll eventually work on runs everything in containers, so the project should look the same way from day two. This ticket gets `market-data-service` into a Docker image, brings up Postgres alongside it via Docker Compose, and proves the two come up together with one command.

We're also introducing Postgres now, even though no service code touches it yet. The service-code wiring (JPA, Flyway, `Position` entity) happens on Day 4. Adding the container today means the compose file is one continuous evolution rather than a sudden expansion mid-week, and it forces me to think about service-to-service networking before the database becomes load-bearing.

## Acceptance Criteria

- [ ] A `Dockerfile` exists at `market-data-service/Dockerfile`.
- [ ] The Dockerfile is **multi-stage**: a build stage based on `maven:3.9-eclipse-temurin-21` and a runtime stage based on `eclipse-temurin:21-jre`. The runtime image must not contain Maven or the JDK.
- [ ] The build stage uses Docker layer caching for dependencies: parent and module `pom.xml` files are copied and dependencies resolved (`mvn dependency:go-offline -B`) **before** any source code is copied in.
- [ ] The runtime stage uses an `ENTRYPOINT` in **exec form** (JSON array),.
- [ ] The runtime stage declares `EXPOSE 8080`.
- [ ] A `.dockerignore` file exists at the repo root excluding at minimum `target/`, `.idea/`, `*.iml`, `.git/`, and `.DS_Store`.
- [ ] A `docker-compose.yml` file exists at the repo root with two services: `market-data-service` and `postgres`. No `version:` key at the top (Compose v2+ does not require it).
- [ ] `market-data-service` in compose uses `build.context: .` and `build.dockerfile: market-data-service/Dockerfile` — **not** a pre-built image reference.
- [ ] `market-data-service` maps host port `8080` to container port `8080`.
- [ ] `market-data-service` declares `depends_on: [postgres]` (startup ordering only; full readiness handling comes later).
- [ ] `postgres` uses image `postgres:16` exactly — not `postgres:latest`, not `postgres:16-alpine`.
- [ ] `postgres` is configured with environment variables `POSTGRES_DB=portfoliorisk`, `POSTGRES_USER=portfoliorisk`, `POSTGRES_PASSWORD=portfoliorisk`.
- [ ] `postgres` maps host port `5432` to container port `5432`.
- [ ] No named volumes declared on `postgres` — data loss on `docker compose down` is expected at this stage. Persistent volumes arrive on Day 4 when there's a schema worth keeping.
- [ ] No `container_name:` keys set on either service. Compose generates predictable names; explicit naming is a footgun for running multiple instances later.
- [ ] Running `docker compose up --build` from the repo root brings both containers to a running state with no errors in either service's logs.
- [ ] Running `curl localhost:8080/health` against the running stack returns HTTP 200 with body exactly `{"status":"UP","service":"market-data-service"}` — identical to Day 1's host-JVM behaviour.
- [ ] Running `docker compose exec postgres psql -U portfoliorisk -d portfoliorisk -c '\dt'` returns `Did not find any relations.` (the database exists, the user can connect, no tables yet).
- [ ] The built runtime image is **smaller than 350 MB**. Verify with `docker images | grep market-data-service`. A correct multi-stage build with a JRE base should land around 260–300 MB; 600+ MB means the build stage is leaking into runtime.
- [ ] A commit with message `chore: containerise market-data-service and add Postgres to compose` is pushed to `main`.

## Implementation Notes

- **Build context is the repo root, not the module directory.** The Dockerfile sits inside `market-data-service/` but it is invoked with the repo root as build context. The Dockerfile must `COPY pom.xml .` (parent) **and** `COPY market-data-service/pom.xml market-data-service/pom.xml` **and** `COPY market-data-service/src market-data-service/src`. If I find myself copying things with paths relative to the module directory, the build context is wrong.

- **Layer caching is non-negotiable.** The naive approach (`COPY . .` then `mvn package`) re-downloads the entire Maven dependency tree every time any source file changes. The fix: copy the two POMs first, run `mvn -f market-data-service/pom.xml dependency:go-offline -B`, then copy `src/`, then run `mvn package -pl market-data-service -am -DskipTests -B`. Order matters — Docker invalidates every layer downstream of the first changed one.

- **`-B` (batch mode) on every Maven invocation.** Interactive progress output clutters CI logs and serves no purpose in non-TTY environments. Always `mvn -B`.

- **`-DskipTests` in the Dockerfile build.** Tests aren't introduced until Day 4, but the flag goes in now so I don't forget when they arrive. Test execution belongs in CI (Day 3), not in image builds.

- **Maven's build image, not a vanilla JDK image.** `eclipse-temurin:21-jdk` has no Maven. `maven:3.9-eclipse-temurin-21` does, and is the standard build image for Spring Boot 3 / Java 21 projects. The runtime image is `eclipse-temurin:21-jre` because the running JVM doesn't need a compiler.

- **Exec form `ENTRYPOINT`, not shell form.** Shell form (`ENTRYPOINT java -jar app.jar`) wraps the JVM in a shell that doesn't propagate signals correctly — `docker stop` sends `SIGTERM` to the shell, not the JVM, and the JVM gets killed by `SIGKILL` after the grace period. Exec form makes the JVM PID 1 and lets it shut down gracefully. This matters more under Kubernetes (Week 5), but the habit starts now.

- **No `:latest` tags anywhere.** `postgres:16` pins the major version, which is the minimum acceptable bar. `postgres:latest` silently upgrades to whatever's current next time someone pulls fresh — the canonical "works on my machine" trap.

- **`depends_on` is startup ordering only.** It guarantees Postgres starts before the service, **not** that Postgres is ready to accept connections. Today this is fine because the service doesn't talk to Postgres yet. From Day 4 onwards I'll need either `depends_on: postgres: condition: service_healthy` (requires a `healthcheck:` block on Postgres) or application-level connection retry. Flag it then, not now.

- **Hostname is `postgres`, not `localhost`.** From inside the `market-data-service` container, the database is reachable at `postgres:5432` — the compose service name acts as DNS. From the host machine (running `psql` directly), it's `localhost:5432` thanks to the port mapping. Not relevant to Day 2 code, but I'll trip on it Day 4 if I don't internalise it now.

- **No `application-docker.yml` or `SPRING_PROFILES_ACTIVE`.** The service doesn't yet need to behave differently in Docker versus on the host — there's no datasource configuration to vary, no external service to connect to. Profile-based config arrives on Day 4 with the JDBC URL.

- **`.dockerignore` matters more than people think.** Without it, the entire `target/` directory (potentially hundreds of MB) gets shipped to the Docker daemon as build context on every build, even though the Dockerfile never references it. This makes the build perceptibly slower for no reason.

## Dependencies

Week 1, Day 1 — the multi-module Maven project, the `MarketDataServiceApplication` main class, the `HealthController`, and the `/health` endpoint must already exist and produce a working JAR via `mvn clean package` on the host.

## Estimate

2 hours.

## Definition of Done

Someone cloning the repo on a clean machine with Docker installed (and **no** Java or Maven installed locally) can run `docker compose up --build` from the repo root, wait for the build to complete, then `curl localhost:8080/health` and see `{"status":"UP","service":"market-data-service"}`. Running `docker compose down` cleans up without leftover containers. A second `docker compose up --build` after changing one Java source file completes the image build in under 30 seconds — proving the dependency layer was cached.

## Out of Scope

- Wiring Postgres into the Spring application (datasource, JDBC URL, JPA, connection pool tuning) — Day 4.
- Flyway migrations or any schema definition — Day 4.
- The `Position` entity, `PositionRepository`, or any database access code — Day 4.
- GitHub Actions CI — Day 3. The Docker build must work locally on Day 2; CI catches regressions starting Day 3.
- A `healthcheck:` block on either service in compose — added when there is a real readiness condition for downstream consumers to wait on, not before.
- Kubernetes manifests, Helm charts, ingress, probes — Week 5.
- Production hardening: non-root container user, distroless or Alpine base, secrets management, image signing, SBOM generation — out of scope for the entire pre-joining plan. Worth a follow-up ticket post-joining BlackRock.
- A persistent named volume for Postgres data — Day 4 when there's data worth persisting.
- The `risk-calc-service` module — Week 2 introduces it. Day 2 stays focused on getting one service containerised cleanly.
- Tests of any kind, including container smoke tests via Testcontainers — Day 4 onwards. Day 2 remains scaffolding.
- An ADR for choosing Docker. Docker is the default tooling for this project, not an architecturally significant decision worth recording. ADRs document choices where the alternative was credible; "should we use containers in 2026" is not one of those.

## Status

Submitted for review.