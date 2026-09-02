# 🚀 Guía de Integración API REST — Frontend (Virtual Paddock)

Este documento es la especificación técnica para la integración del Frontend (Angular/React/Vue) con el Backend Spring Boot de **Virtual Paddock**.

---

## 📌 1. Información General del Servidor

- **URL Base:** `http://localhost:8080`
- **Prefix de Endpoints:** `/api`
- **Formato de Comunicación:** JSON (`Content-Type: application/json`)
- **Documentación Interactiva (Swagger UI):** `http://localhost:8080/swagger-ui.html`
- **OpenAPI Schema (JSON):** `http://localhost:8080/api-docs`
- **Autenticación (Estado Actual):** *Deshabilitada temporalmente* (Fase 1). Todos los endpoints son de acceso público sin token Bearer por ahora.

---

## 📦 2. Estructura Estándar de Respuestas (Wrappers)

Todas las respuestas exitosas de la API están estandarizadas con la siguiente envoltura:

### 2.1. Respuesta Genérica (`ApiResponse<T>`)
```typescript
interface ApiResponse<T> {
  success: boolean;  // true en operaciones exitosas, false en error
  message: string;   // Mensaje descriptivo (ej: "Liga creada exitosamente")
  data: T;           // Payload principal (Objeto, Lista o PageResponse)
}
```

### 2.2. Respuesta Paginada (`PageResponse<T>`)
Para consultas de listados con paginación, el campo `data` contiene una estructura `PageResponse<T>`:

```typescript
interface PageResponse<T> {
  content: T[];         // Arreglo de elementos devueltos
  pageNumber: number;   // Página actual (0-indexed)
  pageSize: number;     // Tamaño de elementos por página (ej: 10)
  totalElements: number;// Cantidad total de registros en BD
  totalPages: number;   // Páginas totales disponibles
  last: boolean;        // true si es la última página
}
```

**Parámetros Query Estándar de Paginación:**
- `page`: Número de página deseada (Default: `0`)
- `size`: Cantidad de elementos por página (Default: `10`)

*Ejemplo de URL:* `GET /api/drivers?page=0&size=10`

### 2.3. Estructura de Errores (`ErrorResponse`)
Cuando el servidor retorna códigos `400 Bad Request`, `404 Not Found` o `500 Internal Server Error`:

```typescript
interface ErrorResponse {
  status: number;                     // Código HTTP (400, 404, 500)
  message: string;                    // Descripción general del error
  errors?: Array<{ [key: string]: string }>; // Lista opcional de errores de validación de campos
}
```

---

## 🔠 3. Enumeraciones (Enums) del Sistema

El frontend debe enviar y recibir los valores exactos en formato `String` (en mayúsculas):

```typescript
export enum Role {
  SUPERADMIN = 'SUPERADMIN',
  LEAGUE_ADMIN = 'LEAGUE_ADMIN'
}

export enum EventStatus {
  SCHED = 'SCHED',       // Programado
  LIVE = 'LIVE',         // En Vivo / En Curso
  FINISHED = 'FINISHED'  // Finalizado
}

export enum DriverStatus {
  ACTIVE = 'ACTIVE',     // Activo
  WARNING = 'WARNING',   // Advertencia
  SANCTIONED = 'SANCTIONED' // Sancionado
}

export enum SanctionType {
  TRACK_LIMITS = 'TRACK_LIMITS', // Límites de Pista
  COLISION = 'COLISION',         // Colisión
  CONDUCTA = 'CONDUCTA',         // Conducta Antideportiva
  NO_SHOW = 'NO_SHOW'            // No Presentado
}

export enum SanctionSeverity {
  LEVE = 'LEVE',
  MODERADO = 'MODERADO',
  GRAVE = 'GRAVE'
}

export enum SanctionStatus {
  PENDIENTE = 'PENDIENTE',
  REVISION = 'REVISION',
  APLICADO = 'APLICADO'
}

export enum RaceType {
  NORMAL = 'NORMAL', // Carrera principal / estándar
  SPRINT = 'SPRINT'  // Carrera corta / sprint
}
```

---

## 🛠️ 4. Catálogo de Endpoints y DTOs

### 4.1. Ligas (`/api/leagues`)

| Método | Endpoint | Descripción | Body Request | Respuesta `data` |
|--------|----------|-------------|--------------|------------------|
| `POST` | `/api/leagues` | Crear nueva liga | `LeagueRequest` | `LeagueResponse` |
| `GET` | `/api/leagues` | Listar ligas paginadas | - | `PageResponse<LeagueBasicResponse>` |
| `GET` | `/api/leagues/{id}` | Obtener detalle de liga | - | `LeagueResponse` |
| `PUT` | `/api/leagues/{id}` | Actualizar liga | `LeagueUpdate` | `LeagueResponse` |
| `DELETE` | `/api/leagues/{id}` | Eliminar liga | - | `null` |

#### Schemas DTO:
```typescript
interface LeagueRequest {
  name: string;        // Requerido, máx 100 caracteres
  slugUrl: string;     // Requerido, máx 100 caracteres, único
  tagline?: string;    // Opcional, máx 255 caracteres
  accentColor?: string;// Opcional, Hex (ej: "#FF5733")
  surfaceTheme?: string;// Opcional
}

interface LeagueUpdate {
  name?: string;
  slugUrl?: string;
  tagline?: string;
  accentColor?: string;
  surfaceTheme?: string;
}

interface LeagueResponse {
  id: number;
  name: string;
  slugUrl: string;
  tagline?: string;
  accentColor?: string;
  surfaceTheme?: string;
  userId?: number;
}

interface LeagueBasicResponse {
  id: number;
  name: string;
}
```

---

### 4.2. Campeonatos (`/api/championships`)

| Método | Endpoint | Descripción | Body Request | Respuesta `data` |
|--------|----------|-------------|--------------|------------------|
| `POST` | `/api/championships` | Crear campeonato | `ChampionshipRequest` | `ChampionshipResponse` |
| `GET` | `/api/championships` | Listar todos | - | `PageResponse<ChampionshipBasicResponse>` |
| `GET` | `/api/championships/league/{leagueId}` | Listar por liga | - | `PageResponse<ChampionshipBasicResponse>` |
| `GET` | `/api/championships/{id}` | Obtener detalle | - | `ChampionshipResponse` |
| `PUT` | `/api/championships/{id}` | Actualizar | `ChampionshipUpdate` | `ChampionshipResponse` |
| `DELETE` | `/api/championships/{id}` | Eliminar | - | `null` |

#### Schemas DTO:
```typescript
interface ChampionshipRequest {
  gameName: string;           // Requerido, máx 100 (ej: "iRacing", "Assetto Corsa", "rFactor")
  category?: string;          // Opcional, máx 100 (ej: "GT3", "Fórmula 1", "TN Clase 3")
  pointsSystem?: string;      // Opcional, escala personalizada (ej: "25,18,15,12,10,8,6,4,2,1")
  sprintPointsSystem?: string;// Opcional, escala para sprint (ej: "8,7,6,5,4,3,2,1")
  pointsSystemId?: number;    // Opcional, ID de plantilla de sistema de puntos personalizada
  sprintPointsSystemId?: number; // Opcional, ID de plantilla de sistema de puntos sprint
  fastestLapPoints?: number;  // Opcional, puntos por vuelta rápida (default: 1)
  polePoints?: number;        // Opcional, puntos por pole position (default: 0)
  leagueId: number;           // Requerido
}

interface ChampionshipUpdate {
  gameName?: string;
  category?: string;
  pointsSystem?: string;
  sprintPointsSystem?: string;
  pointsSystemId?: number;
  sprintPointsSystemId?: number;
  fastestLapPoints?: number;
  polePoints?: number;
}

interface ChampionshipResponse {
  id: number;
  gameName: string;
  category?: string;
  pointsSystem?: string;
  sprintPointsSystem?: string;
  pointsSystemId?: number;
  pointsSystemName?: string;
  sprintPointsSystemId?: number;
  sprintPointsSystemName?: string;
  fastestLapPoints: number;
  polePoints: number;
  leagueId: number;
  leagueName?: string;
}

interface ChampionshipBasicResponse {
  id: number;
  gameName: string;
}
```

---

### 4.3. Temporadas (`/api/seasons`)

| Método | Endpoint | Descripción | Body Request | Respuesta `data` |
|--------|----------|-------------|--------------|------------------|
| `POST` | `/api/seasons` | Crear temporada | `SeasonRequest` | `SeasonResponse` |
| `GET` | `/api/seasons` | Listar todas | - | `PageResponse<SeasonBasicResponse>` |
| `GET` | `/api/seasons/championship/{championshipId}` | Listar por campeonato | - | `PageResponse<SeasonBasicResponse>` |
| `GET` | `/api/seasons/{id}` | Obtener detalle | - | `SeasonResponse` |
| `PUT` | `/api/seasons/{id}` | Actualizar | `SeasonUpdate` | `SeasonResponse` |
| `DELETE` | `/api/seasons/{id}` | Eliminar | - | `null` |

#### Schemas DTO:
```typescript
interface SeasonRequest {
  seasonName: string;    // Requerido, máx 100 (ej: "Season 2026 - Q1")
  championshipId: number;// Requerido
}

interface SeasonUpdate {
  seasonName?: string;
}

interface SeasonResponse {
  id: number;
  seasonName: string;
  championshipId: number;
}

interface SeasonBasicResponse {
  id: number;
  seasonName: string;
}
```

---

### 4.4. Eventos de Carrera / Rondas (`/api/race-events`)

| Método | Endpoint | Descripción | Body Request | Respuesta `data` |
|--------|----------|-------------|--------------|------------------|
| `POST` | `/api/race-events` | Crear evento/ronda | `RaceEventRequest` | `RaceEventResponse` |
| `GET` | `/api/race-events` | Listar todos | - | `PageResponse<RaceEventBasicResponse>` |
| `GET` | `/api/race-events/season/{seasonId}` | Listar por temporada | - | `PageResponse<RaceEventBasicResponse>` |
| `GET` | `/api/race-events/{id}` | Obtener detalle | - | `RaceEventResponse` |
| `PUT` | `/api/race-events/{id}` | Actualizar | `RaceEventUpdate` | `RaceEventResponse` |
| `DELETE` | `/api/race-events/{id}` | Eliminar | - | `null` |

#### Schemas DTO:
```typescript
interface RaceEventRequest {
  roundNumber: string; // Requerido, máx 10 (ej: "Ronda 1", "R01")
  circuitName: string; // Requerido, máx 150 (ej: "Spa-Francorchamps")
  date?: string;       // ISO Date YYYY-MM-DD (ej: "2026-09-15")
  status: EventStatus; // SCHED | LIVE | FINISHED
  raceType?: RaceType; // NORMAL | SPRINT (default: NORMAL)
  seasonId: number;    // Requerido
}

interface RaceEventUpdate {
  roundNumber?: string;
  circuitName?: string;
  date?: string;
  status?: EventStatus;
  raceType?: RaceType;
}

interface RaceEventResponse {
  id: number;
  roundNumber: string;
  circuitName: string;
  date?: string;
  status: EventStatus;
  raceType: RaceType;  // NORMAL | SPRINT
  seasonId: number;
}

interface RaceEventBasicResponse {
  id: number;
  roundNumber: string;
  circuitName: string;
  raceType: RaceType;
}
```

---

### 4.5. Equipos (`/api/teams`)

| Método | Endpoint | Descripción | Body Request | Respuesta `data` |
|--------|----------|-------------|--------------|------------------|
| `POST` | `/api/teams` | Crear equipo | `TeamRequest` | `TeamResponse` |
| `GET` | `/api/teams` | Listar todos | - | `PageResponse<TeamBasicResponse>` |
| `GET` | `/api/teams/league/{leagueId}` | Listar por liga | - | `PageResponse<TeamBasicResponse>` |
| `GET` | `/api/teams/{id}` | Obtener detalle | - | `TeamResponse` |
| `PUT` | `/api/teams/{id}` | Actualizar | `TeamUpdate` | `TeamResponse` |
| `DELETE` | `/api/teams/{id}` | Eliminar | - | `null` |

#### Schemas DTO:
```typescript
interface TeamRequest {
  name: string;      // Requerido, máx 100
  carModel?: string; // Opcional, máx 100 (ej: "Porsche 911 GT3 R")
  colorHex?: string; // Opcional, máx 7 (ej: "#FF0000")
  leagueId: number;  // Requerido
}

interface TeamUpdate {
  name?: string;
  carModel?: string;
  colorHex?: string;
}

interface TeamResponse {
  id: number;
  name: string;
  carModel?: string;
  colorHex?: string;
  leagueId: number;
}

interface TeamBasicResponse {
  id: number;
  name: string;
}
```

---

### 4.6. Pilotos (`/api/drivers`)

| Método | Endpoint | Descripción | Body Request | Respuesta `data` |
|--------|----------|-------------|--------------|------------------|
| `POST` | `/api/drivers` | Crear piloto | `DriverRequest` | `DriverResponse` |
| `GET` | `/api/drivers` | Listar todos los pilotos | - | `PageResponse<DriverBasicResponse>` |
| `GET` | `/api/drivers/team/{teamId}` | Listar por equipo | - | `PageResponse<DriverBasicResponse>` |
| `GET` | `/api/drivers/{id}` | Obtener detalle | - | `DriverResponse` |
| `PUT` | `/api/drivers/{id}` | Actualizar | `DriverUpdate` | `DriverResponse` |
| `DELETE` | `/api/drivers/{id}` | Eliminar | - | `null` |

#### Schemas DTO:
```typescript
interface DriverRequest {
  name: string;         // Requerido, máx 100
  gamertag?: string;    // Opcional, máx 100 (Steam ID, PSN, Xbox, etc.)
  nationality?: string; // Opcional, máx 50
  carNumber?: string;   // 🔢 Dorsal oficial (ej: "44", "1", "99")
  carModel?: string;    // 🏎️ Auto asignado (ej: "Porsche 992 GT3 R")
  status: DriverStatus; // ACTIVE | WARNING | SANCTIONED
  teamId: number;       // Requerido
}

interface DriverUpdate {
  name?: string;
  gamertag?: string;
  nationality?: string;
  carNumber?: string;
  carModel?: string;
  status?: DriverStatus;
  teamId?: number;      // Permite reasignar de equipo
}

interface DriverResponse {
  id: number;
  name: string;
  gamertag?: string;
  nationality?: string;
  carNumber?: string;
  carModel?: string;
  status: DriverStatus;
  teamId: number;
  teamName: string;     // Nombre del equipo inyectado para facilidad del UI
}

interface DriverBasicResponse {
  id: number;
  name: string;
  gamertag?: string;
  carNumber?: string;
}
```

---

### 4.7. Resultados de Carrera & Carga de Tiempos (`/api/results`)

| Método | Endpoint | Descripción | Body Request | Respuesta `data` |
|--------|----------|-------------|--------------|------------------|
| `POST` | `/api/results/race-event/{raceEventId}/batch` | **Guardar y calcular carrera completa (Batch)** | `RaceResultBulkRequest` | `List<RaceResultResponse>` |
| `POST` | `/api/results/race-event/{raceEventId}/preview` | **Previsualizar cálculo en memoria (Dry-Run)** | `RaceResultBulkRequest` | `List<RaceResultResponse>` |
| `POST` | `/api/results/race-event/{raceEventId}/preview-import` | **Previsualizar archivo de simulador sin guardar** | `MultipartFile file` (.json, .csv, .xml) | `List<RaceResultResponse>` |
| `POST` | `/api/results/race-event/{raceEventId}/import` | **Importar y guardar archivo de simulador directamente** | `MultipartFile file` (.json, .csv, .xml) | `List<RaceResultResponse>` |
| `POST` | `/api/results/race-event/{raceEventId}/recalculate` | **Recalcular carrera completa bajo demanda** | - | `List<RaceResultResponse>` |
| `POST` | `/api/results` | Crear resultado individual manual | `RaceResultRequest` | `RaceResultResponse` |
| `GET` | `/api/results/race-event/{raceEventId}` | Listar resultados por evento | - | `PageResponse<RaceResultBasicResponse>` |
| `GET` | `/api/results/driver/{driverId}` | Listar por piloto | - | `PageResponse<RaceResultBasicResponse>` |
| `GET` | `/api/results/{id}` | Obtener detalle | - | `RaceResultResponse` |
| `PUT` | `/api/results/{id}` | Actualizar | `RaceResultUpdate` | `RaceResultResponse` |
| `DELETE` | `/api/results/{id}` | Eliminar | - | `null` |

#### Schemas DTO:
```typescript
interface RaceResultBulkRequest {
  results: RaceResultBulkItemRequest[];
}

interface RaceResultBulkItemRequest {
  driverId: number;           // Requerido: ID del piloto
  driverName?: string;        // Opcional: Nombre para visualización
  carNumber?: string;         // Opcional: Dorsal (#)
  category?: string;          // Opcional: Categoría en pista (ej: "GT3", "LMP2", "Hypercar")
  finishOrder?: number;       // Opcional: Orden de llegada en pista si no se envía tiempo
  totalTime?: string;         // Opcional: Tiempo total en pista (ej: "45:10.500")
  bestLapTime?: string;       // ⏱️ Tiempo de la mejor vuelta individual (ej: "1:42.345")
  lapsCompleted?: number;     // 🔄 Cantidad de vueltas completadas
  penaltiesSeconds?: number;  // Opcional: Segundos de penalización a sumar (default: 0)
  fastestLap?: boolean;       // Opcional: ¿Marcó la vuelta rápida? (default: false)
  polePosition?: boolean;     // Opcional: ¿Hizo la pole position? (default: false)
  status?: string;            // Opcional: "FINISHED" | "DNF" | "DQ" | "DNS" (default: "FINISHED")
}

interface RaceResultRequest {
  position?: number;
  overallPosition?: number;
  category?: string;
  totalTime?: string;
  finalTime?: string;
  bestLapTime?: string;
  gap?: string;
  fastestLap: boolean;        // Requerido
  points?: number;
  penaltiesSeconds?: number;
  lapsCompleted?: number;
  status?: string;
  raceEventId: number;        // Requerido
  driverId: number;           // Requerido
}

interface RaceResultUpdate {
  position?: number;
  overallPosition?: number;
  category?: string;
  totalTime?: string;
  finalTime?: string;
  bestLapTime?: string;
  gap?: string;
  fastestLap?: boolean;
  points?: number;
  penaltiesSeconds?: number;
  lapsCompleted?: number;
  status?: string;
}

interface RaceResultResponse {
  id: number;
  position: number;           // 🏁 Posición final en su categoría (recalculada)
  overallPosition: number;    // 🌐 Posición general en pista (recalculada)
  category: string;           // Categoría del piloto
  totalTime?: string;         // Tiempo bruto en pista
  finalTime?: string;         // ⏱️ Tiempo final corregido (con penalizaciones sumadas)
  bestLapTime?: string;       // ⏱️ Mejor vuelta del piloto
  lapsCompleted?: number;     // 🔄 Cantidad de vueltas completadas
  gap?: string;               // Diferencia respecto al líder ("LEADER", "+5.123", "+1 LAP", "DNF", "DQ")
  fastestLap: boolean;
  points: number;             // 🏆 Puntos ganados (asignados automáticamente, 0 si DQ)
  penaltiesSeconds?: number;
  status: string;             // 🚦 "FINISHED" | "DNF" | "DQ" | "DNS"
  raceEventId: number;
  driverId: number;
  driverName: string;
}

interface RaceResultBasicResponse {
  id: number;
  position: number;
  overallPosition?: number;
  category?: string;
  driverName: string;
  bestLapTime?: string;
  lapsCompleted?: number;
  points?: number;
  status?: string;
}
```

---

### 4.8. Sanciones de Comisarios (`/api/sanctions`)

> [!NOTE]
> Al crear o actualizar una sanción con `penaltySeconds > 0`, el backend **actualiza automáticamente los segundos del piloto y recalcula las posiciones y puntos de esa carrera**.

| Método | Endpoint | Descripción | Body Request | Respuesta `data` |
|--------|----------|-------------|--------------|------------------|
| `POST` | `/api/sanctions` | Crear sanción | `SanctionRequest` | `SanctionResponse` |
| `GET` | `/api/sanctions` | Listar todas | - | `PageResponse<SanctionBasicResponse>` |
| `GET` | `/api/sanctions/race-event/{raceEventId}` | Listar por evento | - | `PageResponse<SanctionBasicResponse>` |
| `GET` | `/api/sanctions/driver/{driverId}` | Listar por piloto | - | `PageResponse<SanctionBasicResponse>` |
| `GET` | `/api/sanctions/{id}` | Obtener detalle | - | `SanctionResponse` |
| `PUT` | `/api/sanctions/{id}` | Actualizar | `SanctionUpdate` | `SanctionResponse` |
| `DELETE` | `/api/sanctions/{id}` | Eliminar | - | `null` |

#### Schemas DTO:
```typescript
interface SanctionRequest {
  type: SanctionType;         // TIME_PENALTY | DRIVE_THROUGH | STOP_AND_GO | GRID_PENALTY | DISQUALIFICATION | WARNING
  severity: SanctionSeverity; // LOW | MEDIUM | HIGH | CRITICAL
  status: SanctionStatus;     // PENDIENTE | EN_REVISION | APLICADO | APELADO | ANULADO
  detail?: string;            // Explicación de los comisarios
  penaltySeconds?: number;    // ⏱️ Segundos a sumar al tiempo final (ej: 5, 10)
  pointsDeduction?: number;   // 🔻 Puntos directos a restar del campeonato
  raceEventId: number;
  driverId: number;
}

interface SanctionResponse {
  id: number;
  type: SanctionType;
  severity: SanctionSeverity;
  status: SanctionStatus;
  detail?: string;
  penaltySeconds?: number;
  pointsDeduction?: number;
  raceEventId: number;
  driverId: number;
  driverName: string;
}
```

---

### 4.9. 🏆 Clasificación General / Standings (`/api/seasons/{seasonId}/standings`)

Endpoints para renderizar las tablas de posiciones acumuladas del campeonato con desempates oficiales automáticos.

| Método | Endpoint | Descripción | Query Params | Respuesta `data` |
|--------|----------|-------------|--------------|------------------|
| `GET` | `/api/seasons/{seasonId}/standings/drivers` | Tabla de posiciones de Pilotos | `?category=GT3` (opcional) | `List<DriverStandingResponse>` |
| `GET` | `/api/seasons/{seasonId}/standings/teams` | Tabla de posiciones de Equipos/Constructores | `?category=GT3` (opcional) | `List<TeamStandingResponse>` |

#### Schemas DTO:
```typescript
interface DriverStandingResponse {
  position: number;        // 🥇 1, 2, 3...
  driverId: number;
  driverName: string;
  gamertag?: string;
  carNumber?: string;      // Dorsal (#)
  carModel?: string;       // Modelo de auto
  category: string;        // "GT3", "LMP2", etc.
  teamId?: number;
  teamName: string;
  points: number;          // 🏆 Puntos netos acumulados (sumatoria menos sanciones)
  wins: number;            // 🥇 Victorias (P1)
  podiums: number;         // 🥈 Podios (P1 a P3)
  fastestLaps: number;     // 🟣 Vueltas rápidas
  polePositions: number;   // 🚀 Poles
  racesCount: number;      // 🏁 Carreras disputadas
}

interface TeamStandingResponse {
  position: number;        // 🥇 1, 2, 3...
  teamId: number;
  teamName: string;
  carModel?: string;
  category: string;
  points: number;          // 🏆 Puntos acumulados combinados del equipo
  wins: number;            // Victorias combinadas
  podiums: number;         // Podios combinados
  racesCount: number;      // Carreras disputadas
}
```

---

### 4.10. Sistemas de Puntos Personalizados (`/api/points-systems`)

Permite a los administradores crear plantillas de sistemas de puntos independientes y personalizadas (ej. solo Top 5, Top 10, o hasta Top 30+), asignando los puntos deseados por posición (`P1 = 25`, `P2 = 18`, etc.) y configurando bonos por vuelta rápida y pole position.

| Método | Endpoint | Descripción | Body Request | Respuesta `data` |
|--------|----------|-------------|--------------|------------------|
| `POST` | `/api/points-systems` | Crear sistema de puntos | `PointsSystemRequest` | `PointsSystemResponse` |
| `GET` | `/api/points-systems/{id}` | Obtener detalle con todas sus posiciones | - | `PointsSystemResponse` |
| `GET` | `/api/points-systems/league/{leagueId}` | Listar sistemas de puntos de una liga | - | `PointsSystemResponse[]` |
| `GET` | `/api/points-systems/league/{leagueId}/paged` | Listar sistemas de puntos paginados | - | `PageResponse<PointsSystemBasicResponse>` |
| `PUT` | `/api/points-systems/{id}` | Actualizar reglas y puntos | `PointsSystemUpdate` | `PointsSystemResponse` |
| `DELETE` | `/api/points-systems/{id}` | Eliminar sistema de puntos | - | `null` |

#### Schemas DTO:
```typescript
interface PointRuleDto {
  position: number;        // Requerido, ej: 1 para P1, 2 para P2... 30 para P30
  points: number;          // Requerido, puntos otorgados (ej: 25, 18, 15, 0...)
}

interface PointsSystemRequest {
  name: string;            // Requerido (ej: "Sistema FIA F1 2026", "Top 5 Personalizado", "IndyCar 33")
  description?: string;    // Opcional
  fastestLapPoints?: number;// Opcional, default: 1
  polePoints?: number;     // Opcional, default: 0
  leagueId: number;        // Requerido
  rules: PointRuleDto[];   // Requerido, arreglo con cada posición y sus puntos
}

interface PointsSystemUpdate {
  name?: string;
  description?: string;
  fastestLapPoints?: number;
  polePoints?: number;
  rules?: PointRuleDto[];  // Reemplaza la lista completa de reglas si se provee
}

interface PointsSystemResponse {
  id: number;
  name: string;
  description?: string;
  fastestLapPoints: number;
  polePoints: number;
  leagueId: number;
  leagueName?: string;
  rules: PointRuleDto[];   // Ordenado ascendentemente por position (1, 2, 3...)
}

interface PointsSystemBasicResponse {
  id: number;
  name: string;
  description?: string;
  fastestLapPoints: number;
  polePoints: number;
  rulesCount: number;      // Cantidad de posiciones configuradas (ej: 10, 30)
}
```

#### Ejemplo de Payload para Crear un Sistema de Puntos Personalizado:
```json
POST /api/points-systems
{
  "name": "Sistema Top 5 Personalizado",
  "description": "Solo puntúan los primeros 5 pilotos",
  "fastestLapPoints": 1,
  "polePoints": 1,
  "leagueId": 1,
  "rules": [
    { "position": 1, "points": 10 },
    { "position": 2, "points": 7 },
    { "position": 3, "points": 5 },
    { "position": 4, "points": 3 },
    { "position": 5, "points": 1 }
  ]
}
```

---

## 💻 5. Ejemplo de Integración en Angular (HttpClient / Services)

### Ejemplo de Servicio en Angular (`driver.service.ts`):

```typescript
import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class DriverService {
  private apiUrl = 'http://localhost:8080/api/drivers';

  constructor(private http: HttpClient) {}

  // Listar pilotos paginados
  getDrivers(page: number = 0, size: number = 10): Observable<ApiResponse<PageResponse<DriverBasicResponse>>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<ApiResponse<PageResponse<DriverBasicResponse>>>(this.apiUrl, { params });
  }

  // Obtener detalle de un piloto por ID
  getDriverById(id: number): Observable<ApiResponse<DriverResponse>> {
    return this.http.get<ApiResponse<DriverResponse>>(`${this.apiUrl}/${id}`);
  }

  // Crear nuevo piloto
  createDriver(request: DriverRequest): Observable<ApiResponse<DriverResponse>> {
    return this.http.post<ApiResponse<DriverResponse>>(this.apiUrl, request);
  }

  // Actualizar piloto
  updateDriver(id: number, update: DriverUpdate): Observable<ApiResponse<DriverResponse>> {
    return this.http.put<ApiResponse<DriverResponse>>(`${this.apiUrl}/${id}`, update);
  }

  // Eliminar piloto
  deleteDriver(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/${id}`);
  }
}
```

---

## ⚠️ 6. Manejo de Errores e HTTP Status Codes

- **`200 OK`**: Petición procesada correctamente.
- **`201 Created`**: Recurso creado exitosamente (POST).
- **`400 Bad Request`**: Datos de entrada inválidos. El cuerpo del error contendrá el arreglo `errors` con el campo y mensaje específico de validación.
- **`404 Not Found`**: El ID especificado no existe en la base de datos (ej: `Liga no encontrada con ID: 5`).
- **`500 Internal Server Error`**: Error no controlado en el servidor.
