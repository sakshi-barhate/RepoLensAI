import ReactMarkdown from "react-markdown";
import { useState } from "react";
import "./App.css";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function App() {
  const [currentView, setCurrentView] = useState("home");
  const [repositoryUrl, setRepositoryUrl] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [analysis, setAnalysis] = useState(null);

  // Pull Request state
  const [prOwner, setPrOwner] = useState("");
  const [prRepository, setPrRepository] = useState("");
  const [prNumber, setPrNumber] = useState("");
  const [prLoading, setPrLoading] = useState(false);
  const [prError, setPrError] = useState("");
  const [prAnalysis, setPrAnalysis] = useState(null);

  // --------------------------------------------------
  // Repository Analysis
  // --------------------------------------------------

  const handleAnalyze = async () => {
    if (!repositoryUrl.trim()) {
      setError("Please enter a GitHub repository URL.");
      return;
    }

    setLoading(true);
    setCurrentView("repository");
    setError("");
    setAnalysis(null);

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/repository/analyze`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            repositoryUrl: repositoryUrl.trim(),
          }),
        }
      );

      if (!response.ok) {
        const errorText = await response.text();

        console.error("BACKEND ERROR STATUS:", response.status);
        console.error("BACKEND ERROR RESPONSE:", errorText);

        const backendError = new Error(
          `Backend returned ${response.status}: ${errorText}`
        );

        backendError.status = response.status;
        throw backendError;
      }

      const data = await response.json();

      console.log("Analysis Result:", data);

      setAnalysis(data);
    } catch (error) {
      console.error("Analysis error:", error);

      if (error.status === 404) {
        setError(
          "Repository not found. Please check the GitHub URL and make sure the repository is public."
        );
      } else if (error.status === 400) {
        setError(
          "Invalid repository URL. Please enter a valid public GitHub repository URL."
        );
      } else if (error.status === 500) {
        setError(
          "Repository analysis failed on the server. Please try again."
        );
      } else if (!error.status) {
        setError(
          "Unable to connect to the backend. Please make sure the RepoLensAI backend is running."
        );
      } else {
        setError(
          "Unable to analyze repository. Please check the repository URL and try again."
        );
      }
    } finally {
      setLoading(false);
    }
  };

  // --------------------------------------------------
  // Pull Request Analysis
  // --------------------------------------------------

  const handleAnalyzePullRequest = async () => {
    if (
      !prOwner.trim() ||
      !prRepository.trim() ||
      !prNumber.trim()
    ) {
      setPrError(
        "Please enter the repository owner, repository name and pull request number."
      );
      return;
    }

    setPrLoading(true);
    setPrError("");
    setPrAnalysis(null);

    try {
      const response = await fetch(
        `${API_BASE_URL}/api/pull-request/${encodeURIComponent(
          prOwner.trim()
        )}/${encodeURIComponent(
          prRepository.trim()
        )}/${encodeURIComponent(
          prNumber.trim()
        )}/analyze`
      );

      if (!response.ok) {
        const errorText = await response.text();

        console.error(
          "PR ANALYSIS ERROR STATUS:",
          response.status
        );

        console.error(
          "PR ANALYSIS ERROR RESPONSE:",
          errorText
        );

        const backendError = new Error(
          `Backend returned ${response.status}: ${errorText}`
        );

        backendError.status = response.status;

        throw backendError;
      }

      const data = await response.json();

      console.log(
        "Pull Request Analysis Result:",
        data
      );

      setPrAnalysis(data);
    } catch (error) {
      console.error(
        "Pull request analysis error:",
        error
      );

      if (error.status === 404) {
        setPrError(
          "Pull request not found. Check the repository owner, repository name and pull request number."
        );
      } else if (error.status === 403) {
        setPrError(
          "GitHub API access was denied or rate limited. Please try again later."
        );
      } else if (!error.status) {
        setPrError(
          "Unable to connect to the backend. Please make sure the RepoLensAI backend is running."
        );
      } else {
        setPrError(
          "Unable to analyze the pull request. Please check the details and try again."
        );
      }
    } finally {
      setPrLoading(false);
    }
  };

  // --------------------------------------------------
  // Reset
  // --------------------------------------------------

  const handleNewAnalysis = () => {
    setCurrentView("home");
    setAnalysis(null);
    setRepositoryUrl("");
    setError("");

    setPrOwner("");
    setPrRepository("");
    setPrNumber("");
    setPrError("");
    setPrAnalysis(null);

    window.scrollTo({
      top: 0,
      behavior: "smooth",
    });
  };

  // --------------------------------------------------
  // Navigation
  // --------------------------------------------------

  const handleDashboardClick = (event) => {
    event.preventDefault();
    setCurrentView("home");
    window.history.replaceState(null, "", "#dashboard");
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const handleFeaturesClick = (event) => {
    event.preventDefault();
    setCurrentView("home");
    window.history.replaceState(null, "", "#features");
    window.setTimeout(() => {
      document.getElementById("features")?.scrollIntoView({
        behavior: "smooth",
        block: "start",
      });
    }, 0);
  };

  const openView = (view) => {
    setCurrentView(view);
    window.history.replaceState(null, "", `#${view}`);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  // --------------------------------------------------
  // Dashboard Calculations
  // --------------------------------------------------

  const isSpringProject =
    analysis?.framework
      ?.toLowerCase()
      .includes("spring");

  const totalComposition =
    (analysis?.codeMetrics?.classes ?? 0) +
    (analysis?.codeMetrics?.interfaces ?? 0) +
    (analysis?.codeMetrics?.packages ?? 0);

  const classPercentage =
    totalComposition === 0
      ? 0
      : ((analysis?.codeMetrics?.classes ?? 0) /
          totalComposition) *
        100;

  const interfacePercentage =
    totalComposition === 0
      ? 0
      : ((analysis?.codeMetrics?.interfaces ?? 0) /
          totalComposition) *
        100;

  const packagePercentage =
    totalComposition === 0
      ? 0
      : ((analysis?.codeMetrics?.packages ?? 0) /
          totalComposition) *
        100;

  const complexitySection =
    isSpringProject ? "08" : "07";

  const duplicationSection =
    isSpringProject ? "09" : "08";

  const documentationSection =
    isSpringProject ? "10" : "09";

  const licenseSection =
    isSpringProject ? "11" : "10";

  const githubSection =
    isSpringProject ? "12" : "11";

  const commitSection =
    isSpringProject ? "13" : "12";

  const restSection =
    isSpringProject ? "14" : "13";

  return (
    <div className="app">

      {/* ==================================================
          NAVBAR
      ================================================== */}

      <nav className="navbar">

        <div className="logo">
          RepoLensAI
        </div>

        <div className="nav-links">

          <a
            href="#dashboard"
            onClick={handleDashboardClick}
          >
            Dashboard
          </a>

          <a href="#features" onClick={handleFeaturesClick}>
            Features
          </a>

        </div>

      </nav>

      {/* ==================================================
          LANDING PAGE
      ================================================== */}

      {(!analysis || currentView === "home" || currentView === "pr" || currentView === "features") && (
        <main>

          {currentView === "home" && (
            <>
              <section className="hero" id="dashboard">
                <div className="hero-content">
                  <p className="eyebrow">REPOLENSAI WORKSPACE</p>
                  <h1><span>RepoLensAI</span><span>Dashboard</span></h1>
                  <p className="hero-description">
                    Choose a workspace to analyze a GitHub repository or review a pull request.
                  </p>
                </div>
              </section>
              <section
                className="features dashboard-choice"
                style={{
                  width: "100%",
                  maxWidth: "1100px",
                  minHeight: "calc(100vh - 100px)",
                  margin: "0 auto",
                  padding: "24px",
                  boxSizing: "border-box",
                  display: "grid",
                  gridTemplateColumns: "repeat(2, minmax(0, 1fr))",
                  justifyContent: "center",
                  alignContent: "center",
                  alignItems: "stretch",
                  gap: "30px",
                }}
              >
                <div
                  className="feature-card"
                  style={{ display: "flex", flexDirection: "column" }}
                >
                  <div className="feature-number">01</div>
                  <h3>Repository Analysis</h3>
                  <p>Analyze repository structure, code quality, dependencies, security and health.</p>
                  <button
                    type="button"
                    className="new-analysis-button"
                    style={{ marginTop: "28px", whiteSpace: "nowrap" }}
                    onClick={() => openView("repository")}
                  >
                    Analyze Repository →
                  </button>
                </div>
                <div
                  className="feature-card"
                  style={{ display: "flex", flexDirection: "column" }}
                >
                  <div className="feature-number">02</div>
                  <h3>Pull Request Review</h3>
                  <p>Review a GitHub pull request for changes, security and code quality.</p>
                  <button
                    type="button"
                    className="new-analysis-button"
                    style={{ marginTop: "28px", whiteSpace: "nowrap" }}
                    onClick={() => openView("pr")}
                  >
                    Review Pull Request →
                  </button>
                </div>
              </section>
            </>
          )}

          {/* HERO */}

          {currentView === "repository" && (
          <section className="hero">

            <div className="hero-content">

              <p className="eyebrow">
                AI-POWERED REPOSITORY ANALYSIS
              </p>

              <h1>
                <span>
                  Understand Your
                </span>

                <span>
                  GitHub Repository
                </span>
              </h1>

              <p className="hero-description">
                Analyze your repository's structure,
                languages, frameworks, dependencies,
                security, complexity, code quality
                and overall health from one place.
              </p>

              <div
                className="analyze-box"
                id="analyze"
              >

                <input
                  type="text"
                  placeholder="https://github.com/username/repository"
                  value={repositoryUrl}
                  onChange={(e) =>
                    setRepositoryUrl(
                      e.target.value
                    )
                  }
                  disabled={loading}
                  onKeyDown={(e) => {
                    if (e.key === "Enter") {
                      handleAnalyze();
                    }
                  }}
                />

                <button
                  type="button"
                  onClick={handleAnalyze}
                  disabled={loading}
                >
                  {loading
                    ? "Analyzing..."
                    : "Analyze Repository"}
                </button>

              </div>

              {error && (
                <p className="error-message">
                  {error}
                </p>
              )}

            </div>

          </section>
          )}

          {/* ==================================================
              PULL REQUEST REVIEW
          ================================================== */}

          {currentView === "pr" && (<section
            className="pr-review-panel"
            id="pull-request-review"
          >

            <div className="pr-review-heading">

              <p className="eyebrow">
                AI-POWERED PULL REQUEST REVIEW
              </p>

              <h2>
                Review a GitHub Pull Request
              </h2>

              <p>
                Analyze a GitHub pull request for
                security issues, code-quality issues,
                changes and an AI-generated review.
              </p>

            </div>

            <div
              className="pr-review-form"
              style={{
                display: "flex",
                flexDirection: "column",
                alignItems: "stretch",
                gap: "12px",
              }}
            >

              <input
                type="text"
                placeholder="Repository owner"
                value={prOwner}
                onChange={(e) =>
                  setPrOwner(e.target.value)
                }
                disabled={prLoading}
              />

              <input
                type="text"
                placeholder="Repository name"
                value={prRepository}
                onChange={(e) =>
                  setPrRepository(
                    e.target.value
                  )
                }
                disabled={prLoading}
              />

              <input
                type="number"
                min="1"
                placeholder="Pull request number"
                value={prNumber}
                onChange={(e) =>
                  setPrNumber(e.target.value)
                }
                disabled={prLoading}
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    handleAnalyzePullRequest();
                  }
                }}
              />

              <button
                type="button"
                onClick={
                  handleAnalyzePullRequest
                }
                disabled={prLoading}
              >
                {prLoading
                  ? "Reviewing..."
                  : "Review Pull Request"}
              </button>

            </div>

            {prError && (
              <p className="error-message">
                {prError}
              </p>
            )}

            {/* ==================================================
                PR RESULTS
            ================================================== */}

            {prAnalysis && (
              <div className="pr-review-results">

                {/* PR HEADER */}

                <div className="pr-review-result-header">

                  <div>

                    <span className="stat-label">
                      PULL REQUEST #
                      {prAnalysis.pullRequestNumber}
                    </span>

                    <h3>
                      {prAnalysis.title ||
                        "Untitled Pull Request"}
                    </h3>

                    <p>
                      {prAnalysis.author ||
                        "Unknown"}

                      {" · "}

                      {prAnalysis.state ||
                        "Unknown"}
                    </p>

                  </div>

                  <div className="pr-status-badge">
                    {prAnalysis.state ||
                      "Unknown"}
                  </div>

                </div>


                {/* PR METRICS */}

                <div className="pr-metrics">

                  <div className="metric-card">
                    <span>
                      Changed Files
                    </span>

                    <strong>
                      {prAnalysis.changedFiles ??
                        0}
                    </strong>
                  </div>

                  <div className="metric-card">
                    <span>
                      Additions
                    </span>

                    <strong>
                      +{prAnalysis.additions ??
                        0}
                    </strong>
                  </div>

                  <div className="metric-card">
                    <span>
                      Deletions
                    </span>

                    <strong>
                      -{prAnalysis.deletions ??
                        0}
                    </strong>
                  </div>

                  <div className="metric-card">
                    <span>
                      Total Changes
                    </span>

                    <strong>
                      {prAnalysis.totalChanges ??
                        0}
                    </strong>
                  </div>

                </div>


                {/* AI SUMMARY */}

                <div className="pr-review-summary">

                  <div className="pr-card-heading">

                    <span className="pr-card-number">
                      01
                    </span>

                    <div>
                      <span className="pr-card-label">
                        AI REVIEW
                      </span>

                      <h4>
                        Summary
                      </h4>
                    </div>

                  </div>

                  <div className="pr-review-content">
                    <ReactMarkdown>
                      {prAnalysis.aiReview?.summary ||
                        "No AI summary available."}
                    </ReactMarkdown>
                  </div>

                </div>


                {/* AI REVIEW GRID */}

                <div className="pr-review-grid">

                  {/* SECURITY */}

                  <div className="pr-review-card">

                    <div className="pr-card-heading">

                      <span className="pr-card-number">
                        02
                      </span>

                      <div>
                        <span className="pr-card-label">
                          SECURITY
                        </span>

                        <h4>
                          Security
                        </h4>
                      </div>

                    </div>

                    <div className="pr-review-content">
                      <ReactMarkdown>
                        {prAnalysis.aiReview?.security ||
                          "No security review available."}
                      </ReactMarkdown>
                    </div>

                  </div>


                  {/* CODE QUALITY */}

                  <div className="pr-review-card">

                    <div className="pr-card-heading">

                      <span className="pr-card-number">
                        03
                      </span>

                      <div>
                        <span className="pr-card-label">
                          CODE QUALITY
                        </span>

                        <h4>
                          Code Quality
                        </h4>
                      </div>

                    </div>

                    <div className="pr-review-content">
                      <ReactMarkdown>
                        {prAnalysis.aiReview?.codeQuality ||
                          "No code-quality review available."}
                      </ReactMarkdown>
                    </div>

                  </div>


                  {/* RECOMMENDATIONS */}

                  <div className="pr-review-card">

                    <div className="pr-card-heading">

                      <span className="pr-card-number">
                        04
                      </span>

                      <div>
                        <span className="pr-card-label">
                          RECOMMENDATIONS
                        </span>

                        <h4>
                          Recommendations
                        </h4>
                      </div>

                    </div>

                    <div className="pr-review-content">
                      <ReactMarkdown>
                        {prAnalysis.aiReview?.recommendations ||
                          "No recommendations available."}
                      </ReactMarkdown>
                    </div>

                  </div>


                  {/* OVERALL REVIEW */}

                  <div className="pr-review-card">

                    <div className="pr-card-heading">

                      <span className="pr-card-number">
                        05
                      </span>

                      <div>
                        <span className="pr-card-label">
                          OVERALL REVIEW
                        </span>

                        <h4>
                          Overall Review
                        </h4>
                      </div>

                    </div>

                    <div className="pr-review-content">
                      <ReactMarkdown>
                        {prAnalysis.aiReview?.overallReview ||
                          "No overall review available."}
                      </ReactMarkdown>
                    </div>

                  </div>

                </div>


                {/* DETECTED ISSUES */}

                <div className="pr-detected-issues">

                  {/* SECURITY ISSUES */}

                  <div className="pr-issue-panel">

                    <div className="pr-issue-header">

                      <span className="pr-issue-indicator security" />

                      <div>

                        <h4>
                          Detected Security Issues
                        </h4>

                        <span>
                          Static analysis findings
                        </span>

                      </div>

                    </div>

                    {prAnalysis.securityIssues?.length ? (

                      <ul>
                        {prAnalysis.securityIssues.map(
                          (issue, index) => (
                            <li key={index}>
                              {issue}
                            </li>
                          )
                        )}
                      </ul>

                    ) : (

                      <p className="no-issues">
                        No security issues detected.
                      </p>

                    )}

                  </div>


                  {/* QUALITY ISSUES */}

                  <div className="pr-issue-panel">

                    <div className="pr-issue-header">

                      <span className="pr-issue-indicator quality" />

                      <div>

                        <h4>
                          Detected Code Quality Issues
                        </h4>

                        <span>
                          Static analysis findings
                        </span>

                      </div>

                    </div>

                    {prAnalysis.qualityIssues?.length ? (

                      <ul>
                        {prAnalysis.qualityIssues.map(
                          (issue, index) => (
                            <li key={index}>
                              {issue}
                            </li>
                          )
                        )}
                      </ul>

                    ) : (

                      <p className="no-issues">
                        No code-quality issues detected.
                      </p>

                    )}

                  </div>

                </div>

              </div>
            )}

          </section>)}


          {/* ==================================================
              FEATURES
          ================================================== */}

          {(currentView === "home" || currentView === "features") && (<section
            className="features"
            id="features"
          >

            <div className="feature-card">

              <div className="feature-number">
                01
              </div>

              <h3>
                Repository Analysis
              </h3>

              <p>
                Understand project structure,
                languages, frameworks,
                dependencies and repository
                statistics.
              </p>

            </div>

            <div className="feature-card">

              <div className="feature-number">
                02
              </div>

              <h3>
                Code Quality
              </h3>

              <p>
                Analyze complexity, duplication,
                maintainability, code smells
                and technical debt.
              </p>

            </div>

            <div className="feature-card">

              <div className="feature-number">
                03
              </div>

              <h3>
                Security Insights
              </h3>

              <p>
                Detect potential secrets,
                hardcoded credentials and
                other repository security risks.
              </p>

            </div>

          </section>)}

        </main>
      )}


      {/* ==================================================
          DASHBOARD
      ================================================== */}

      {analysis && currentView === "repository" && (
        <main
          className="dashboard"
          id="dashboard"
        >

          {/* DASHBOARD HEADER */}

          <section className="dashboard-header">

            <div>

              <p className="eyebrow">
                ANALYSIS COMPLETE
              </p>

              <h1>
                {analysis.repositoryName}
              </h1>

              <p className="repository-url">
                {repositoryUrl}
              </p>

            </div>

            <button
              className="new-analysis-button"
              onClick={handleNewAnalysis}
            >
              Analyze Another
            </button>

          </section>


          {/* OVERVIEW */}

          <section className="overview-grid">

            <div className="stat-card">

              <span className="stat-label">
                LANGUAGE
              </span>

              <strong>
                {analysis.language ||
                  "N/A"}
              </strong>

            </div>

            <div className="stat-card">

              <span className="stat-label">
                BUILD TOOL
              </span>

              <strong>
                {analysis.buildTool ||
                  "N/A"}
              </strong>

            </div>

            <div className="stat-card">

              <span className="stat-label">
                FRAMEWORK
              </span>

              <strong>
                {analysis.framework ||
                  "N/A"}
              </strong>

            </div>

            <div className="stat-card health-card">

              <span className="stat-label">
                HEALTH SCORE
              </span>

              <strong>
                {analysis.healthScore
                  ?.score ?? 0}/100
              </strong>

              <span className="health-grade">
                Grade{" "}
                {analysis.healthScore
                  ?.grade || "N/A"}
              </span>

            </div>

          </section>


          {/* AI SUMMARY */}

          <section className="dashboard-section">

            <div className="section-heading">

              <span>
                01
              </span>

              <h2>
                AI Repository Summary
              </h2>

            </div>

            <div className="summary-card">

              <div className="summary-label">
                AI ANALYSIS
              </div>

              <p>
                {analysis.aiSummary ||
                  "No AI summary available."}
              </p>

            </div>

          </section>


          {/* REPOSITORY STATISTICS */}

          <section className="dashboard-section">

            <div className="section-heading">

              <span>
                02
              </span>

              <h2>
                Repository Statistics
              </h2>

            </div>

            <div className="metric-grid">

              <div className="metric-card">

                <span>
                  Total Files
                </span>

                <strong>
                  {analysis.statistics
                    ?.totalFiles ?? 0}
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  Directories
                </span>

                <strong>
                  {analysis.statistics
                    ?.totalDirectories ?? 0}
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  Classes
                </span>

                <strong>
                  {analysis.codeMetrics
                    ?.classes ?? 0}
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  Interfaces
                </span>

                <strong>
                  {analysis.codeMetrics
                    ?.interfaces ?? 0}
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  Packages
                </span>

                <strong>
                  {analysis.codeMetrics
                    ?.packages ?? 0}
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  Source Files
                </span>

                <strong>
                  {analysis.complexity
                    ?.sourceFiles ?? 0}
                </strong>

              </div>

            </div>


            {/* COMPOSITION */}

            <div className="statistics-composition">

              <div className="composition-header">

                <h3>
                  Source Code Structure
                </h3>

                <span>
                  Classes, interfaces and packages
                </span>

              </div>

              <div className="composition-bar">

                <div
                  className="composition-segment classes"
                  style={{
                    width: `${classPercentage}%`,
                  }}
                />

                <div
                  className="composition-segment interfaces"
                  style={{
                    width: `${interfacePercentage}%`,
                  }}
                />

                <div
                  className="composition-segment packages"
                  style={{
                    width: `${packagePercentage}%`,
                  }}
                />

              </div>

              <div className="composition-legend">

                <div>

                  <span className="legend-dot classes" />

                  <span>
                    Classes
                  </span>

                  <strong>
                    {analysis.codeMetrics
                      ?.classes ?? 0}
                  </strong>

                </div>

                <div>

                  <span className="legend-dot interfaces" />

                  <span>
                    Interfaces
                  </span>

                  <strong>
                    {analysis.codeMetrics
                      ?.interfaces ?? 0}
                  </strong>

                </div>

                <div>

                  <span className="legend-dot packages" />

                  <span>
                    Packages
                  </span>

                  <strong>
                    {analysis.codeMetrics
                      ?.packages ?? 0}
                  </strong>

                </div>

              </div>

            </div>

          </section>


          {/* REPOSITORY SIZE */}

          <section className="dashboard-section">

            <div className="section-heading">

              <span>
                03
              </span>

              <h2>
                Repository Size
              </h2>

            </div>

            <div className="metric-grid">

              <div className="metric-card">

                <span>
                  Total Size
                </span>

                <strong>
                  {analysis.repositorySize
                    ?.totalSizeMB ?? 0}{" "}
                  MB
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  Source Code
                </span>

                <strong>
                  {analysis.repositorySize
                    ?.sourceCodeMB ?? 0}{" "}
                  MB
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  Resources
                </span>

                <strong>
                  {analysis.repositorySize
                    ?.resourcesMB ?? 0}{" "}
                  MB
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  Test Files
                </span>

                <strong>
                  {analysis.repositorySize
                    ?.testFilesMB ?? 0}{" "}
                  MB
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  Average File Size
                </span>

                <strong>
                  {analysis.repositorySize
                    ?.averageFileKB ?? 0}{" "}
                  KB
                </strong>

              </div>

            </div>

            <div className="file-type-breakdown">

              <h3>
                File Type Breakdown
              </h3>

              {(() => {

                const fileTypes =
                  Object.entries(
                    analysis.repositorySize
                      ?.fileTypeSizes ?? {}
                  ).sort(
                    ([, a], [, b]) =>
                      b - a
                  );

                const maxSize =
                  fileTypes[0]?.[1] ?? 1;

                return fileTypes.map(
                  ([type, size]) => (

                    <div
                      className="file-type-row"
                      key={type}
                    >

                      <div className="file-type-info">

                        <span>
                          {type}
                        </span>

                        <strong>
                          {size} MB
                        </strong>

                      </div>

                      <div className="file-type-bar">

                        <div
                          className="file-type-bar-fill"
                          style={{
                            width: `${
                              (size / maxSize) *
                              100
                            }%`,
                          }}
                        />

                      </div>

                    </div>

                  )
                );

              })()}

            </div>

          </section>


          {/* CODE QUALITY + SECURITY */}

          <section className="two-column">

            <div className="dashboard-card">

              <div className="card-title">

                <span>
                  04
                </span>

                <h2>
                  Code Quality
                </h2>

              </div>

              <div className="quality-list">

                <div>

                  <span>
                    Maintainability
                  </span>

                  <strong>
                    {analysis.codeQuality
                      ?.maintainability ||
                      "N/A"}
                  </strong>

                </div>

                <div>

                  <span>
                    Rating
                  </span>

                  <strong>
                    {analysis.codeQuality
                      ?.rating ||
                      "N/A"}
                  </strong>

                </div>

                <div>

                  <span>
                    Code Smells
                  </span>

                  <strong>
                    {analysis.codeQuality
                      ?.codeSmells ?? 0}
                  </strong>

                </div>

                <div>

                  <span>
                    Technical Debt
                  </span>

                  <strong>
                    {analysis.codeQuality
                      ?.technicalDebt ||
                      "N/A"}
                  </strong>

                </div>

              </div>

            </div>


            <div className="dashboard-card">

              <div className="card-title">

                <span>
                  05
                </span>

                <h2>
                  Security
                </h2>

              </div>

              <div className="security-summary">

                <div className="security-score">

                  <span>
                    Risk Level
                  </span>

                  <strong>
                    {analysis.security
                      ?.risk ||
                      "Unknown"}
                  </strong>

                </div>

                <div className="security-score">

                  <span>
                    Issues Found
                  </span>

                  <strong>
                    {analysis.security
                      ?.issuesFound ?? 0}
                  </strong>

                </div>

              </div>

              <div className="issue-list">

                {analysis.security
                  ?.issues?.length > 0 ? (

                  analysis.security.issues.map(
                    (issue, index) => (

                      <div
                        className="issue-item"
                        key={index}
                      >
                        {issue}
                      </div>

                    )
                  )

                ) : (

                  <div className="issue-item">
                    No security issues detected.
                  </div>

                )}

              </div>

            </div>

          </section>


          {/* DEPENDENCIES */}

          <section className="dashboard-section">

            <div className="section-heading">

              <span>
                06
              </span>

              <h2>
                Dependencies
              </h2>

            </div>

            <div className="tag-container">

              {analysis.dependencies
                ?.length > 0 ? (

                analysis.dependencies.map(
                  (dependency, index) => (

                    <span
                      className="tag"
                      key={index}
                    >
                      {dependency}
                    </span>

                  )
                )

              ) : (

                <span className="tag">
                  No dependencies detected
                </span>

              )}

            </div>

          </section>


          {/* SPRING COMPONENTS */}

          {isSpringProject && (

            <section className="dashboard-section">

              <div className="section-heading">

                <span>
                  07
                </span>

                <h2>
                  Spring Components
                </h2>

              </div>

              <div className="metric-grid">

                <div className="metric-card">

                  <span>
                    Controllers
                  </span>

                  <strong>
                    {analysis.springComponents
                      ?.controllers ?? 0}
                  </strong>

                </div>

                <div className="metric-card">

                  <span>
                    Services
                  </span>

                  <strong>
                    {analysis.springComponents
                      ?.services ?? 0}
                  </strong>

                </div>

                <div className="metric-card">

                  <span>
                    Entities
                  </span>

                  <strong>
                    {analysis.springComponents
                      ?.entities ?? 0}
                  </strong>

                </div>

                <div className="metric-card">

                  <span>
                    Components
                  </span>

                  <strong>
                    {analysis.springComponents
                      ?.components ?? 0}
                  </strong>

                </div>

                <div className="metric-card">

                  <span>
                    Configurations
                  </span>

                  <strong>
                    {analysis.springComponents
                      ?.configurations ?? 0}
                  </strong>

                </div>

              </div>

            </section>

          )}


          {/* COMPLEXITY + DUPLICATION */}

          <section className="two-column">

            <div className="dashboard-card">

              <div className="card-title">

                <span>
                  {complexitySection}
                </span>

                <h2>
                  Complexity
                </h2>

              </div>

              <div className="quality-list">

                <div>

                  <span>
                    Total Lines
                  </span>

                  <strong>
                    {analysis.complexity
                      ?.linesOfCode ?? 0}
                  </strong>

                </div>

                <div>

                  <span>
                    Average Lines per File
                  </span>

                  <strong>
                    {analysis.complexity
                      ?.averageLinesPerFile ??
                      0}
                  </strong>

                </div>

                <div>

                  <span>
                    Largest File
                  </span>

                  <strong>
                    {analysis.complexity
                      ?.largestFile ||
                      "N/A"}
                  </strong>

                </div>

                <div>

                  <span>
                    Complexity
                  </span>

                  <strong>
                    {analysis.complexity
                      ?.complexity ||
                      "N/A"}
                  </strong>

                </div>

              </div>

            </div>


            <div className="dashboard-card">

              <div className="card-title">

                <span>
                  {duplicationSection}
                </span>

                <h2>
                  Duplication
                </h2>

              </div>

              <div className="quality-list">

                <div>

                  <span>
                    Duplicate Files
                  </span>

                  <strong>
                    {analysis.duplication
                      ?.duplicateFiles ??
                      0}
                  </strong>

                </div>

                <div>

                  <span>
                    Risk
                  </span>

                  <strong>
                    {analysis.duplication
                      ?.risk ||
                      "Low"}
                  </strong>

                </div>

              </div>

            </div>

          </section>


          {/* DOCUMENTATION + LICENSE */}

          <section className="two-column">

            <div className="dashboard-card">

              <div className="card-title">

                <span>
                  {documentationSection}
                </span>

                <h2>
                  Documentation
                </h2>

              </div>

              <div className="quality-list">

                <div>

                  <span>
                    Documentation Score
                  </span>

                  <strong>
                    {analysis.documentation
                      ?.documentationScore ||
                      "N/A"}
                  </strong>

                </div>

                <div>

                  <span>
                    JavaDoc Comments
                  </span>

                  <strong>
                    {analysis.documentation
                      ?.javaDocComments ??
                      0}
                  </strong>

                </div>

                <div>

                  <span>
                    Markdown Files
                  </span>

                  <strong>
                    {analysis.documentation
                      ?.markdownFiles ??
                      0}
                  </strong>

                </div>

                <div>

                  <span>
                    README
                  </span>

                  <strong>
                    {analysis.documentation
                      ?.readmeExists
                      ? "Available"
                      : "Missing"}
                  </strong>

                </div>

              </div>

            </div>


            <div className="dashboard-card">

              <div className="card-title">

                <span>
                  {licenseSection}
                </span>

                <h2>
                  License
                </h2>

              </div>

              <div className="license-box">

                <strong>
                  {analysis.licenseInfo
                    ?.type ||
                    "Unknown"}
                </strong>

                <span>
                  {analysis.licenseInfo
                    ?.exists
                    ? "License detected"
                    : "No license detected"}
                </span>

              </div>

            </div>

          </section>


          {/* GITHUB INFORMATION */}

          <section className="dashboard-section">

            <div className="section-heading">

              <span>
                {githubSection}
              </span>

              <h2>
                GitHub Information
              </h2>

            </div>

            <div className="metric-grid">

              <div className="metric-card">

                <span>
                  Stars
                </span>

                <strong>
                  {analysis.github
                    ?.stars ?? 0}
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  Forks
                </span>

                <strong>
                  {analysis.github
                    ?.forks ?? 0}
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  Watchers
                </span>

                <strong>
                  {analysis.github
                    ?.watchers ?? 0}
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  Open Issues
                </span>

                <strong>
                  {analysis.github
                    ?.openIssues ?? 0}
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  Branches
                </span>

                <strong>
                  {analysis.branchInfo
                    ?.totalBranches ??
                    0}
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  Contributors
                </span>

                <strong>
                  {analysis.contributors
                    ?.totalContributors ??
                    0}
                </strong>

              </div>

            </div>

          </section>


          {/* LATEST COMMIT */}

          <section className="dashboard-section">

            <div className="section-heading">

              <span>
                {commitSection}
              </span>

              <h2>
                Latest Commit
              </h2>

            </div>

            <div className="commit-card">

              <div className="commit-message">

                <span>
                  MESSAGE
                </span>

                <p>
                  {analysis.commitInfo
                    ?.latestCommitMessage ||
                    "No commit information available."}
                </p>

              </div>

              <div className="commit-details">

                <div>

                  <span>
                    AUTHOR
                  </span>

                  <strong>
                    {analysis.commitInfo
                      ?.latestCommitAuthor ||
                      "Unknown"}
                  </strong>

                </div>

                <div>

                  <span>
                    DATE
                  </span>

                  <strong>

                    {analysis.commitInfo
                      ?.latestCommitDate
                      ? new Date(
                          analysis.commitInfo
                            .latestCommitDate
                        ).toLocaleString(
                          "en-IN",
                          {
                            dateStyle:
                              "long",
                            timeStyle:
                              "short",
                            timeZone:
                              "Asia/Kolkata",
                          }
                        )
                      : "Unknown"}

                  </strong>

                </div>

                <div>

                  <span>
                    SHA
                  </span>

                  <strong className="sha">
                    {analysis.commitInfo
                      ?.latestCommitSha ||
                      "N/A"}
                  </strong>

                </div>

              </div>

            </div>

          </section>


          {/* REST API */}

          <section className="dashboard-section">

            <div className="section-heading">

              <span>
                {restSection}
              </span>

              <h2>
                REST API Analysis
              </h2>

            </div>

            <div className="metric-grid">

              <div className="metric-card">

                <span>
                  GET
                </span>

                <strong>
                  {analysis.restApi
                    ?.getEndpoints ??
                    0}
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  POST
                </span>

                <strong>
                  {analysis.restApi
                    ?.postEndpoints ??
                    0}
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  PUT
                </span>

                <strong>
                  {analysis.restApi
                    ?.putEndpoints ??
                    0}
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  DELETE
                </span>

                <strong>
                  {analysis.restApi
                    ?.deleteEndpoints ??
                    0}
                </strong>

              </div>

              <div className="metric-card">

                <span>
                  PATCH
                </span>

                <strong>
                  {analysis.restApi
                    ?.patchEndpoints ??
                    0}
                </strong>

              </div>

            </div>

            <div className="endpoint-list">

              <h3>
                Detected Endpoints
              </h3>

              {analysis.restApi
                ?.endpoints?.length > 0 ? (

                analysis.restApi.endpoints.map(
                  (endpoint, index) => (

                    <div
                      className="endpoint-item"
                      key={index}
                    >

                      <span className="endpoint-method">
                        {endpoint.method}
                      </span>

                      <span className="endpoint-path">
                        {endpoint.path}
                      </span>

                    </div>

                  )
                )

              ) : (

                <div className="endpoint-item">
                  No REST endpoints detected.
                </div>

              )}

            </div>

          </section>


          {/* FOOTER */}

          <footer className="dashboard-footer">

            <span>
              RepoLensAI
            </span>

            <span>
              Repository analysis completed successfully.
            </span>

          </footer>

        </main>
      )}

    </div>
  );
}

export default App;
