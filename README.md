# Aurum Casino

Casino web de demostración con **créditos ficticios** y 10 juegos:

- **Mesas:** Blackjack, Ruleta europea, Baccarat, Video póker
- **Máquinas:** Tragamonedas, Keno
- **Instantáneos:** Crash, Minas, Plinko, Dados

| Capa | Tecnología |
|------|------------|
| Frontend | React 18 + TypeScript + Vite (CSS Modules, sin librerías de UI) |
| Backend | Java 17 + Spring Boot 3.3 (Web, Data JPA, Validation) |
| Base de datos | SQLite (archivo `aurum.db`, se crea solo) |
| Comunicación | API REST JSON, errores RFC 7807 (`ProblemDetail` + `code`) |

---

## Cómo ejecutarlo (Windows)

Requisitos: **JDK 17+** y **Node 18+**. Maven no hace falta (viene el wrapper `mvnw`).

**1. Backend** (puerto 8080)

```powershell
cd backend
.\mvnw.cmd test             # corre los tests (motores + flujo completo de la API)
.\mvnw.cmd spring-boot:run
```

**2. Frontend** (puerto 5173)

```powershell
cd frontend
npm install
npm run dev
```

Abrí http://localhost:5173, creá una cuenta y arrancás con 10.000 créditos.
En desarrollo, Vite reenvía `/api` al backend, así que no hay que configurar CORS.

### Configuración

Todo vive en `backend/src/main/resources/application.yml` (prefijo `aurum`):
saldo inicial, umbral y monto de recarga, límites de apuesta, duración de sesión y orígenes CORS.
Variables de entorno: `PORT`, `AURUM_DB_PATH`, `AURUM_CORS_ORIGINS`.
En el frontend, `VITE_API_URL` (ver `.env.example`) si el backend corre en otro origen.

---

## Arquitectura

### Backend — paquetes por funcionalidad

```
com.aurum.casino
├── shared/        config tipada, errores de dominio, manejador global, health
├── auth/          registro, login, sesiones (token opaco, se guarda solo su SHA-256)
├── player/        jugador + billetera (entidad rica: debit/credit validan invariantes)
└── game/
    ├── core/        BettingService (jugadas de un paso), GameEngine, RandomSource, historial
    ├── session/     SessionService (jugadas de varios pasos), SessionState, GameSession, codec JSON
    ├── cards/       Card, Rank, Suit, Shoe (compartido por blackjack, póker y baccarat)
    ├── slots/ roulette/ dice/ baccarat/ keno/ plinko/      → un solo paso
    └── blackjack/ videopoker/ mines/ crash/                → con estado entre peticiones
```

Hay dos flujos según el tipo de juego, y los motores no conocen ninguno de los dos:

- **Un paso** (`BettingService.play`, una transacción): validar límite → debitar → ejecutar motor → acreditar → registrar.
- **Varios pasos** (`SessionService`): `start` debita y guarda el estado del motor como JSON → cada acción
  (`act`) lo carga, lo transforma y debita cualquier aumento (doblar, dividir) → al terminar, acredita y registra.
  Un jugador tiene como máximo una mano abierta por juego; si cierra el navegador, la retoma al volver.
  La vista que recibe el cliente oculta lo secreto (mazo, carta tapada, minas, punto de crash) hasta el final.

### Patrones y principios aplicados

| Dónde | Qué |
|-------|-----|
| `GameEngine<O>` | **Strategy**: cada juego es una función pura del azar; no conoce saldo, BD ni HTTP |
| `BettingService` | **Template Method** del ciclo de apuesta; agregar un juego no la modifica (**OCP**) |
| `RandomSource` | **DIP**: producción usa `SecureRandom`, los tests usan semillas fijas |
| `RouletteBetType` | **Strategy vía enum**: cada tipo define su pago y su regla de acierto |
| `SessionState<V>` | **State + Memento**: el motor se serializa entre peticiones y expone una vista pública |
| `SessionService` | **Template Method** para juegos de varios pasos (abrir → actuar → liquidar) |
| `PokerHandEvaluator`, `BaccaratCoup` | Funciones puras de reglas, testeables sin Spring |
| `Shoe.stacked(...)` | Mazos con orden fijo para tests deterministas de cada regla |
| `Player` | **Modelo de dominio rico**: el saldo nunca queda negativo, la regla vive en la entidad |
| `DomainException` + `ErrorCode` | El dominio no conoce HTTP; `GlobalExceptionHandler` traduce a estados |
| `@CurrentPlayer` | Resolver de argumentos: los controladores reciben el id autenticado sin tocar la request |
| `AurumProperties` | Configuración tipada e inmutable (records) |
| Repositorios Spring Data | **Repository**; proyección `PlayerStats` con `select new` |

### Frontend

```
src/
├── api/          http.ts (fetch + ApiError + token), casinoApi.ts (fachada), types.ts (contratos)
├── context/      SessionContext (sesión y saldo), ToastContext (notificaciones)
├── hooks/        useGameRound (ciclo de jugada), useAnimatedNumber, useSound
├── lib/          sound.ts (Web Audio sintetizado), roulette.ts, format.ts
├── components/   layout/ (shell, barra, nav, historial) · ui/ (Button, StakeControl, WinBanner, GameFrame)
└── features/     auth/ · slots/ · roulette/ · dice/
```

- **`useGameRound`** replica el Template Method en el cliente: reserva la apuesta, pide el resultado,
  ejecuta la animación propia del juego y recién entonces liquida el saldo (el número no "spoilea" el resultado).
- **Registro de juegos** en `App.tsx`: una mesa nueva es una entrada en `GAMES`.
- El servidor es la **única fuente de verdad**: el cliente solo anima lo que el backend decidió.

### Efectos visuales

Rodillos con desenfoque de movimiento y frenado escalonado, líneas de pago que se dibujan,
marquesina de luces, ruleta SVG con plato y bola en sentidos opuestos, fichas con denominación por color,
dados con contador desacelerado, lluvia de monedas escalada al premio (×5 «Gran premio», ×20 «Mega premio»),
contador de saldo animado, giros automáticos, «Repetir apuesta» en la ruleta y sonidos generados por código
(sin archivos de audio). Respeta `prefers-reduced-motion`.

---

## Matemática de los juegos

| Juego | Retorno teórico (RTP) | Notas |
|-------|----------------------|-------|
| Tragamonedas | ≈ 94,8 % | 3×3, 5 líneas, símbolos ponderados; aciertos en ~22 % de los giros |
| Ruleta europea | 97,3 % | Un cero; pleno 35:1, docena/columna 2:1, sencillas 1:1 |
| Dados | 99 % | Multiplicador = 99 / probabilidad elegida (2 % a 98 %) |
| Blackjack | ≈ 99,4 % | 6 mazos, S17, 3:2, doblar con 2 cartas, dividir una vez |
| Video póker | ≈ 99,5 % | Jacks or Better 9/6 con estrategia óptima |
| Baccarat | 98,9 % / 98,8 % / 85,6 % | Banca (5 % de comisión) / Jugador / Empate 8:1 |
| Keno | 93–96 % | Tabla calibrada por cantidad de números con la distribución hipergeométrica |
| Crash | 99 % | P(crash ≥ m) = 0,99 / m; el servidor decide con su propio reloj |
| Minas | 99 % | Multiplicador = 0,99 / probabilidad de sobrevivir |
| Plinko | ≈ 99 % | 12 filas, 3 perfiles de riesgo (hasta ×170) |

Los tests verifican el RTP de cada juego (cálculo exacto o simulaciones de cientos de miles de jugadas)
y cada regla con mazos de orden fijo: blackjack natural, crupier con blackjack, doblar, dividir, tabla de tercera carta del baccarat, todas las manos de póker, etc.

---

## API

Todas las rutas salvo `auth/register`, `auth/login` y `health` requieren `Authorization: Bearer <token>`.

| Método | Ruta | Cuerpo |
|--------|------|--------|
| POST | `/api/auth/register` · `/api/auth/login` | `{ username, password }` |
| POST | `/api/auth/logout` | — |
| GET | `/api/me` | — |
| POST | `/api/wallet/refill` | — (solo con saldo < 100) |
| GET | `/api/rounds?limit=20` · `/api/rounds/stats` | — |
| POST | `/api/games/slots/spin` | `{ stake }` |
| POST | `/api/games/roulette/spin` | `{ bets: [{ type, value?, amount }] }` |
| POST | `/api/games/dice/roll` | `{ stake, target, direction: "UNDER" \| "OVER" }` |
| POST | `/api/games/baccarat/deal` | `{ player, banker, tie }` |
| POST | `/api/games/keno/play` | `{ stake, picks: [1..80] }` (1 a 10 números) |
| POST | `/api/games/plinko/drop` | `{ stake, risk: "LOW" \| "MEDIUM" \| "HIGH" }` |
| POST | `/api/games/blackjack/deal` · `/action` | `{ stake }` · `{ action: "HIT" \| "STAND" \| "DOUBLE" \| "SPLIT" }` |
| POST | `/api/games/video-poker/deal` · `/draw` | `{ stake }` · `{ held: [0..4] }` |
| POST | `/api/games/mines/start` · `/reveal` · `/cashout` | `{ stake, mines }` · `{ tile }` · — |
| POST | `/api/games/crash/start` · `/tick` · `/cashout` | `{ stake, autoCashout? }` · — · — |
| GET | `/api/games/{blackjack,video-poker,mines,crash}/active` | Mano abierta (200) o ninguna (204) |

---

## Cómo agregar un juego

1. Backend: crear `game/<juego>/` y sumar el valor a `GameType`.
   - De un paso: motor que devuelve un `GameOutcome` y controlador que llama a `bettingService.play(...)`.
   - De varios pasos: clase que implementa `SessionState<V>` (con constructor vacío privado) y controlador
     que usa `sessions.start(...)`, `sessions.act(...)` y `sessions.active(...)`.
2. Frontend: tipos en `api/types.ts`, endpoints en `casinoApi.ts`, componente en `features/<juego>/`
   con `useGameRound` (un paso) o `useGameSession` (varios pasos) dentro de `GameFrame`,
   y una entrada en `games/registry.ts`. La navegación y el historial se actualizan solos.

## Próximos pasos sugeridos

- Seguro (insurance) y re-división en blackjack.
- Crash multijugador en tiempo real con WebSocket.
- Spring Security + JWT si se expone públicamente; rate limiting en login.
- Migraciones con Flyway en lugar de `ddl-auto: update`.
- PostgreSQL para producción (solo cambia datasource y dialecto).
