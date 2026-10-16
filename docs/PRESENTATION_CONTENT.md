# Presentation Slide Deck: AI Powered Fraud Detection System

This guide outlines a professional 12-slide presentation structure designed for  project defense, live evaluation.

---

### Slide 1: Title Slide
- **Title**: AI Powered Fraud Detection System
- **Subtitle**: Enterprise Desktop Risk Intelligence Platform
- **Technology Stack**: Java 17+ • Java Swing • MySQL 8+ • JDBC • Multithreading
- **Course**: Advanced Object-Oriented Software Engineering & Database Systems
- **Student Name / Team**: Shiva & Project Contributors
- **Speaker Notes**:
  > "Good morning, respected professors and evaluators. Today, I am proud to present our project: 'AI Powered Fraud Detection System'. This is an enterprise-grade desktop application engineered in Java and MySQL that actively addresses the rising threat of digital financial fraud using explainable, local AI-inspired risk heuristics."

---

### Slide 2: Problem Statement
- **The Financial Fraud Crisis**:
  - Exponential surge in online transactions, P2P payments, and digital wallets.
  - Phishing scams, account takeovers, social engineering, and payment velocity attacks.
- **The Latency & Privacy Gap**:
  - Cloud-based AI APIs (e.g., OpenAI) introduce third-party privacy risks, subscription costs, and network latency.
  - Traditional rule engines lack explainability and multidimensional feature correlation.
- **Project Goal**:
  - Build a responsive, standalone desktop system capable of analyzing financial telemetry locally, generating explainable risk scores (0–100), and maintaining immutable audit trails.

---

### Slide 3: Proposed Solution & Core Features
- **Hybrid AI-Inspired Risk Engine**:
  - Dynamic multi-detector ensemble analyzing money spikes, text coercion, URL phishing, and device anomalies.
- **Asynchronous Desktop GUI (Java Swing)**:
  - Non-blocking multithreaded architecture with animated progress feedback and instant visual triage.
- **Relational Integrity (MySQL + JDBC)**:
  - 100% parameterized queries, atomic multi-table rollback transactions, and live KPI dashboards.
- **Zero Irreversible Harm**:
  - Decision-support architecture providing human compliance officers with transparent reasons and recommendations without destructive automatic account locking.

---

### Slide 4: System Architecture & Layered Design
- **Architectural Pattern**: N-Tier Clean Separation of Concerns
  - **Model Layer**: Immutable value objects, encapsulated entities (`Transaction`, `FraudAnalysis`).
  - **Data Access Layer (DAO)**: Generic `DatabaseOperations<T>` interface, JDBC `PreparedStatement` queries.
  - **Service Layer**: Business workflows, thread-pool task management, statistics synchronization.
  - **Detection Layer**: Polymorphic ensemble of specialized `FraudDetector` implementations.
  - **Presentation Layer (GUI)**: Modern Swing interface with custom Java2D graphics.

```
┌─────────────────────────────────────────────────────────────┐
│                      Java Swing GUI                         │
│  (Login, Dashboard, Analysis, History, Audit Logs, About)   │
└──────────────────────────────┬──────────────────────────────┘
                               │ Dispatches Asynchronously
┌──────────────────────────────▼──────────────────────────────┐
│                    FraudAnalysisService                     │
│    (ExecutorService Thread Pool & AuditLogger Daemon)       │
└──────────────┬──────────────────────────────┬───────────────┘
               │                              │
┌──────────────▼─────────────┐ ┌──────────────▼───────────────┐
│    FraudDetectionEngine    │ │        DAO & JDBC Layer       │
│  (Polymorphic Detectors)   │ │ (PreparedStatement, Trans.)  │
└────────────────────────────┘ └──────────────┬───────────────┘
                                              │
                               ┌──────────────▼───────────────┐
                               │       MySQL Database         │
                               └──────────────────────────────┘
```

---

### Slide 5: AI Fraud Detection Methodology
- **Multi-Vector Ensemble Scoring**:
  1. **Transaction Heuristics (35% weight)**: Absolute amounts, sudden baseline deviation multipliers, high-risk payment modalities (Crypto, International wire), burst velocity.
  2. **Text Social Engineering (25% weight)**: NLP pattern matching against urgency coercion, OTP/credential harvesting, and prize lures.
  3. **URL Phishing Detector (20% weight)**: Unencrypted HTTP, direct IP hostnames, URL shorteners, spoofed domain tokens.
  4. **Device & Location Anomalies (20% weight)**: Unrecognized hardware, geographical deviation, and compound anomaly multipliers.
- **Ensemble Threat Fusion**:
  - Combines weighted averages with peak vector threat pooling to prevent critical single-vector attacks (like OTP phishing) from being diluted.
- **Tiers**: LOW (0–29), MEDIUM (30–59), HIGH (60–79), CRITICAL (80–100).

---

### Slide 6: Object-Oriented Programming (OOP) Implementation
- **Inheritance**:
  - `abstract class FraudDetector` extended by `TransactionFraudDetector`, `TextFraudDetector`, `URLFraudDetector`, and `AnomalyFraudDetector`.
- **Interfaces**:
  - `FraudDetectionStrategy`: Contract for detection algorithms.
  - `DatabaseOperations<T>`: Contract for persistence layer.
  - `Validatable`: Enforces domain self-validation.
- **Polymorphism**:
  - `List<FraudDetector> detectors` evaluated through common interface calls.
- **Exception Handling**:
  - Hierarchical checked exceptions (`FraudSystemException`, `InvalidInputException`, `DatabaseOperationException`) with graceful GUI alerts.

---

### Slide 7: Collections & Generics Framework
- **Generics**:
  - Generic DAO interface `DatabaseOperations<T>` eliminates code duplication and ensures compile-time type safety.
  - Generic method `ValidationUtil.requireNonNull(T, String)`.
- **Collections Employed**:
  - `ArrayList<FraudIndicator>`: Fine-grained dynamic indicators.
  - `HashSet<String>`: Constant-time O(1) keyword and domain lookup.
  - `HashMap<String, Integer>`: Keyword hit frequency counters.
  - `EnumMap<RiskLevel, Integer>`: High-performance distribution mapping.
  - `LinkedBlockingQueue<AuditLog>`: Thread-safe producer-consumer audit pipeline.

---

### Slide 8: Multithreading & Synchronization
- **GUI Responsiveness Guarantee**:
  - Analysis and database operations run on background threads using an `ExecutorService` thread pool.
  - UI updates are marshaled back to the Swing Event Dispatch Thread (EDT) using `SwingUtilities.invokeLater(...)`.
- **Thread Synchronization**:
  - `AnalysisStatistics` uses `synchronized` methods to prevent race conditions during concurrent risk score recording.
- **Background Daemon Worker**:
  - `AuditLogger` runs a dedicated background daemon worker thread consuming from a thread-safe `BlockingQueue`.

---

### Slide 9: Database Architecture & JDBC
- **Database Engine**: MySQL 8+ (`fraud_detection_db`)
- **Key Tables**: `users`, `transactions`, `fraud_analysis`, `fraud_indicators`, `audit_logs`.
- **Relational Integrity**: Foreign key constraints with `ON DELETE CASCADE`.
- **Security & Quality**:
  - 100% `PreparedStatement` usage (immune to SQL injection).
  - Multi-table atomic transactions using `conn.setAutoCommit(false)`, `conn.commit()`, and `conn.rollback()`.
  - Passwords stored using SHA-256 with per-user cryptographically random salts.

---

### Slide 10: User Interface Design (Java Swing)
- **Design Philosophy**: Modern, high-contrast Slate/Navy enterprise aesthetic.
- **Key Screens**:
  - **Login / Register**: Secure authentication with role-based access (ADMIN, USER) and live MySQL diagnostic badge.
  - **Dashboard**: Real-time KPI metric cards, dynamic Java2D risk distribution chart.
  - **Analysis Studio**: Interactive form with 1-click test presets, live progress bar, and explainable evidence badges.
  - **Transaction History**: Searchable, filterable JTable with custom color-coded renderers and deletion workflows.
  - **Inspection Modal**: Deep audit of individual transaction telemetry and indicators.
  - **Audit Logs**: Immutable compliance trace.

---

### Slide 11: Automated Test Results & Rubric Verification
- **Automated Verification Suite (`SystemVerificationTest`)**:
  - Test 1 (Low-Value Normal): Score 0, Tier: LOW, Decision: SAFE (Passed)
  - Test 2 (High Amount + New Device): Score 42, Tier: MEDIUM, Decision: SUSPICIOUS (Passed)
  - Test 3 (Phishing URL + Urgent Text + Foreign Geo): Score 74, Tier: HIGH, Decision: FRAUDULENT (Passed)
  - Test 4 (Credential/OTP Harvesting): Score 78, Tier: HIGH, Decision: FRAUDULENT (Passed)
  - Test 5 (Negative Amount Input): Caught `InvalidInputException` (Passed)
  - Test 6 (MySQL Disconnection): Caught `DatabaseOperationException` gracefully (Passed)
  - Test 7 (50 Concurrent Analyses): 100% Thread-safe without deadlocks or corrupted counters (Passed)

---

### Slide 12: Summary, Future Enhancements 
- **Summary**:
  - Complete, functional, 30-mark rubric compliant Java GUI application.
  - Zero mock data; full integration of Swing, MySQL, JDBC, Multithreading, and OOP.
- **Future Enhancements**:
  - Integrate ONNX Java Runtime for pre-trained Random Forest/XGBoost ML models.
  - Multi-factor SMS/Email OTP verification dispatch for SUSPICIOUS transactions.
  - Distributed Kafka event streaming for multi-bank consortium fraud intelligence.

