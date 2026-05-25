# Stream-Server

A Java-based HTTP video streaming server that handles video upload, background transcoding via FFmpeg, and HLS (HTTP Live Streaming) delivery with adaptive bitrate streaming across three quality levels — 360p, 480p, and 720p.

---

## How It Works

When a video is uploaded, the server saves it and immediately kicks off three background transcoding jobs — one per resolution. A DB poller picks up pending jobs every 2 seconds and feeds them to a thread pool. Each worker calls FFmpeg to produce HLS segments. Once all three resolutions are done, a master playlist is generated and the video is ready to stream with adaptive bitrate.

```
Upload → Save → Create transcode jobs → Poll → FFmpeg × 3 → Master playlist → Stream
```

---

## Tech Stack

- **Java 21** (Eclipse Temurin JDK)
- **Gradle** — build tool
- **Java built-in HttpServer** (`com.sun.net.httpserver`) — no frameworks
- **Hibernate ORM 6.4** + Jakarta Persistence 3.0
- **MySQL 9.7**
- **FFmpeg** — video transcoding
- **GSON 2.10.1** — JSON
- **Docker + Docker Compose**

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/hello-test` | Health check |
| `POST` | `/video/upload` | Upload a video (binary body) |
| `GET` | `/video/{id}` | Get master HLS playlist |
| `GET` | `/streams/{id}/{quality}` | Get quality playlist (360/480/720) |
| `GET` | `/streams/watch/{id}/{quality}/{segment}` | Stream a `.ts` segment |

All endpoints support CORS. Segment streaming supports HTTP 206 Partial Content (Range requests).

---

## Transcoding Flow

1. Client POSTs a video binary to `/video/upload`
2. Server saves it to `storage/videos/{uuid}.mp4`
3. A `VideoEntity` is created with status `PROCESSING`
4. Three `TranscodeJobEntity` records are created (one per resolution), status `PENDING`
5. `JobDbPoller` queries the DB every 2 seconds for `PENDING` jobs and adds them to an `ArrayBlockingQueue` (capacity 500)
6. Three worker threads consume jobs, mark each `PROCESSING`, and invoke FFmpeg:
   - **720p** — 1280×720, 1400 kbps
   - **480p** — 854×480, 1200 kbps
   - **360p** — 640×360, 800 kbps
7. Each resolution outputs a `playlist.m3u8` + 10-second `.ts` segments
8. Once all three jobs complete, `master.m3u8` is generated
9. `VideoEntity` status updated to `READY_FULL`

---

## HLS Output Layout

```
{videoId}/
├── master.m3u8
├── 720p/
│   ├── playlist.m3u8
│   └── 000.ts, 001.ts, ...
├── 480p/
│   ├── playlist.m3u8
│   └── 000.ts, 001.ts, ...
└── 360p/
    ├── playlist.m3u8
    └── 000.ts, 001.ts, ...
```

---

## FFmpeg Command

```bash
ffmpeg -i storage/videos/{videoId}.mp4 \
  -map 0:v -map 0:a \
  -vf scale={WIDTH}:{HEIGHT} \
  -c:v libx264 \
  -hls_time 10 \
  -hls_playlist_type vod \
  -hls_flags independent_segments \
  -hls_base_url {BASE_URL}/streams/watch/{videoId}/{quality}/ \
  -hls_segment_filename storage/stream/{videoId}/{quality}/%03d.ts \
  storage/stream/{videoId}/{quality}/playlist.m3u8
```

---

## Database Schema

**`videos`**
| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT PK | |
| `videoId` | VARCHAR | UUID of uploaded video |
| `status` | ENUM | `PROCESSING` \| `READY_PARTIAL` \| `READY_FULL` |
| `createdAt` | TIMESTAMP | |
| `deletedAt` | TIMESTAMP | Soft delete |

**`transcode_jobs`**
| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT PK | |
| `videoId` | VARCHAR | |
| `resolution` | ENUM | `720P` \| `480P` \| `360P` |
| `status` | ENUM | `PENDING` \| `PROCESSING` \| `COMPLETE` \| `FAILED` |
| `createdAt` | TIMESTAMP | |
| `updatedAt` | TIMESTAMP | |
| `deletedAt` | TIMESTAMP | Soft delete |

Schema is auto-managed by Hibernate (`hbm2ddl.auto=update`).

---

## Configuration

Copy `.env.example` to `.env` and fill in your values:

```env
MYSQL_ROOT_PASSWORD=password
MYSQL_DATABASE=streamserver
MYSQL_USER=user
MYSQL_PASSWORD=password
DB_PORT=3306
APP_PORT=4221
BASE_URL=http://localhost:4221
```

> `BASE_URL` is embedded into generated HLS playlist URLs — set it to wherever the server is reachable.

---

## Running

### With Docker (recommended)

```bash
docker-compose up --build
```

Starts two services:
- `stream-server` on port 4221 (with FFmpeg installed)
- `db` — MySQL 9.7 with a health check and persistent volume

The app waits for MySQL to be healthy before starting.

### Locally

**Requirements:** Java 21, Gradle, MySQL, FFmpeg on PATH

```bash
# Start MySQL and create the DB from your .env
# Set environment variables, then:

./gradlew run

# Or build a fat JAR:
./gradlew jar
java -jar build/libs/stream-server.jar
```

---

## Startup Sequence

1. `DbConnection.init()` — initialize JPA EntityManagerFactory
2. `TranscodeWorkflow.resetStuckJobs()` — reset any `PROCESSING` jobs to `PENDING` (crash recovery)
3. `TranscodeWorkflow.startWorkers()` — start 3 worker threads + DB poller
4. `Server.startServer()` — begin accepting HTTP on port 4221
5. Shutdown hooks registered for graceful cleanup on JVM exit

---

## Project Structure

```
src/main/java/
├── Main.java
├── Http/
│   ├── Server.java
│   ├── Routes/Routes.java
│   ├── services/VideoService/VideoService.java
│   └── handler/
│       ├── Hello/HelloHandler.java
│       └── Video/
│           ├── VideoHandle.java
│           ├── StreamHandle.java
│           └── StreamSegmentHandle.java
├── packages/
│   ├── Http/
│   │   ├── RequestRouter.java
│   │   ├── ProcessRequest.java
│   │   ├── ResponseWriter.java
│   │   └── AddResponsePartialHeader.java
│   └── files/video/
│       ├── VideoStoreManager.java
│       └── TranscodeVideo.java
├── database/
│   ├── DbConnection.java
│   ├── entities/
│   │   ├── VideoEntity.java
│   │   └── TranscodeJobEntity.java
│   └── Repositories/
│       ├── VideoRepository.java
│       └── TranscodeJobRepository.java
├── workflows/
│   ├── TranscodeWorkflow.java
│   └── JobDbPoller.java
├── enums/
│   ├── HttpMethod.java
│   ├── HttpStatus.java
│   ├── VideoStatus.java
│   ├── TranscodeJobStatus.java
│   └── VideoResolution.java
└── constants/
    ├── Paths.java
    ├── Jobs.java
    └── VideoScale.java
```

---

## Design Decisions

- **No framework** — uses Java's built-in `HttpServer` to keep dependencies minimal
- **DB-backed job queue** — transcoding is fully decoupled from upload; if the server crashes mid-transcode, stuck jobs reset on restart
- **Thread pool** — HTTP thread pool sized at `availableProcessors() × 2`; 3 dedicated transcode worker threads
- **Atomic master playlist** — `master.m3u8` is written via file rename only after all three resolution jobs complete, so clients never see a partial playlist
- **HTTP 206 Partial Content** — segment streaming supports range requests for smooth playback in browsers and media players