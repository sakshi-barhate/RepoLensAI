# RepoLensAI

RepoLensAI analyzes public GitHub repositories and reviews pull requests. It combines repository metrics, security and code-quality checks, and AI-generated summaries in one web application.

## Features

### Repository analysis

- Detects languages, build tools, and frameworks
- Summarizes repository structure, size, dependencies, and code metrics
- Reports code quality, security findings, complexity, duplication, and documentation
- Displays license, GitHub metadata, latest commit, Spring components, and detected endpoints
- Calculates an overall repository health score

### Pull request review

- Analyzes a public GitHub pull request
- Summarizes changed files, additions, and deletions
- Provides AI-generated security, code-quality, and overall reviews
- Lists detected security and code-quality issues

## Technology

- **Frontend:** React, Vite, and React Markdown
- **Backend:** Java and Spring Boot
- **Database:** PostgreSQL
- **AI review:** Google Gemini
- **Repository data:** GitHub API

## Requirements

- Java version supported by `backend/pom.xml`
- Node.js and npm
- A PostgreSQL database
- A Google Gemini API key

## Run locally

### 1. Configure and start the backend

The backend reads the database password and Gemini key from environment variables. In PowerShell, set them in the terminal you’ll use to start the backend:

```powershell
$env:DB_PASSWORD = "your-database-password"
$env:GEMINI_API_KEY = "your-gemini-api-key"
```

The database URL and username are configured in `backend/src/main/resources/application.yml`. You can override them with `SPRING_DATASOURCE_URL` and `SPRING_DATASOURCE_USERNAME` if needed.

Start the backend:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The backend runs on `http://localhost:8080` by default.

### 2. Configure and start the frontend

In a second terminal:

```powershell
cd frontend
```

Create a local `frontend/.env.local` file with:

```env
VITE_API_BASE_URL=http://localhost:8080
```

Then install dependencies and start the development server:

```powershell
npm install
npm run dev
```

Open the local URL printed by Vite, usually `http://localhost:5173`.

Keep `.env.local` and all real credentials out of Git. For hosted deployments, set `DB_PASSWORD`, `GEMINI_API_KEY`, and `VITE_API_BASE_URL` in the hosting providers’ environment settings.

## API endpoints

### Analyze a repository

`POST /api/repository/analyze`

Request body:

```json
{
  "repositoryUrl": "https://github.com/owner/repository"
}
```

### Review a pull request

`GET /api/pull-request/{owner}/{repository}/{number}/analyze`

Example:

```text
/api/pull-request/spring-projects/spring-petclinic/2676/analyze
```

## Project structure

```text
RepoLensAI/
├── backend/    # Spring Boot API and analysis services
├── frontend/   # React and Vite application
├── docs/
└── screenshots/
```

## Security

Use your own API credentials and database settings. Store secrets in environment variables or your hosting provider’s secret settings; never commit real credentials to the repository.