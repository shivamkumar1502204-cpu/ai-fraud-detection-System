# University Evaluation Rubric Mapping (Total: 30 Marks)
## Project: AI Powered Fraud Detection System

This document provides an explicit, line-by-line verification mapping between the university evaluation marking rubric and the actual codebase implementation.

---

### Rubric Breakdown Summary Table

| Rubric Dimension | Marks | Primary Classes / Interfaces | Core Functionality |
| :--- | :---: | :--- | :--- |
| **1. OOP Implementation** | **10** | `FraudDetector`, `FraudDetectionStrategy`, `TransactionFraudDetector`, `TextFraudDetector`, `URLFraudDetector`, `AnomalyFraudDetector`, `FraudDetectionEngine`, `Validatable`, `User`, `Transaction`, Custom Exceptions | Inheritance, Polymorphism, Interfaces, Abstract Base Classes, Custom Exception Hierarchy, Encapsulation |
| **2. Collections & Generics** | **6** | `DatabaseOperations<T>`, `List<FraudIndicator>`, `List<FraudAnalysis>`, `Set<String>`, `Map<String, Integer>`, `BlockingQueue<AuditLog>`, `EnumMap<RiskLevel, Integer>` | Generic DAO interface, Type-safe collections, HashSet dictionaries, HashMap frequency metrics, BlockingQueue daemon logging |
| **3. Multithreading & Synchronization** | **4** | `FraudAnalysisTask`, `DatabaseSaveTask`, `AnalysisStatistics`, `AuditLogger`, `FraudAnalysisService` | `ExecutorService` thread pool, asynchronous Swing worker execution, `synchronized` state monitor, daemon thread queue consumer |
| **4. Database Operations Classes** | **7** | `DatabaseOperations<T>`, `UserDAO`, `TransactionDAO`, `FraudAnalysisDAO`, `FraudIndicatorDAO`, `AuditLogDAO` | Full CRUD operations, specialized analytical queries (`getRiskStatistics()`, `findRecentTransactions()`, `searchTransactions()`) |
| **5. Database Connectivity (JDBC)** | **3** | `DatabaseConnection`, `config.properties`, environment overrides | Singleton connection provider, dynamic configuration loader, connection diagnostics & health checks |
| **6. Implement JDBC for Connectivity** | **3** | All DAOs (`TransactionDAO`, `FraudAnalysisDAO`, etc.) | 100% `PreparedStatement` parameterization (zero SQL injection), multi-table transaction management (`commit`/`rollback`), `try-with-resources` |

---

### Detailed Rubric Verification

#### 1. OOP Implementation (10 Marks)

##### A. Encapsulation
- **Evidence**: All model classes (`User`, `Transaction`, `FraudAnalysis`, `FraudIndicator`, `AuditLog`) declare fields strictly as `private`.
- **Implementation**: Access is mediated via validated getters and setters. Collections returned from entities are defended via `Collections.unmodifiableList(...)` or defensive copies to preserve state encapsulation.
- **File references**:
  - `com.frauddetection.model.Transaction`
  - `com.frauddetection.model.User`
  - `com.frauddetection.model.FraudAnalysis`

##### B. Inheritance
- **Evidence**: `abstract class FraudDetector` serves as the foundational parent class defining shared state (`strategyName`, `weight`) and clamping utility methods.
- **Subclasses**:
  1. `TransactionFraudDetector extends FraudDetector`: Evaluates monetary magnitude, baseline deviation spikes, payment types, and velocity.
  2. `TextFraudDetector extends FraudDetector`: Evaluates social engineering, urgency coercions, and credential phishing.
  3. `URLFraudDetector extends FraudDetector`: Evaluates protocol security, IP hosts, URL shorteners, and spoofing tokens.
  4. `AnomalyFraudDetector extends FraudDetector`: Evaluates unrecognized hardware, geographic mismatches, and hostile environments.
- **File reference**:
  - `com.frauddetection.detection.FraudDetector`

##### C. Polymorphism
- **Evidence**: In `FraudDetectionEngine`, detectors are stored using parent reference collections:
  ```java
  private final List<FraudDetector> detectors;
  ```
- **Polymorphic Execution**: The engine loops over `detectors` and invokes the overridden methods:
  ```java
  for (FraudDetector detector : detectors) {
      double rawScore = detector.calculateRisk(transaction);
      List<FraudIndicator> indicators = detector.extractIndicators(transaction);
  }
  ```
- Any new detection strategy can be plugged into the engine at runtime without modifying client code (Open-Closed Principle).

##### D. Interfaces
- **`FraudDetectionStrategy`**: Defines the algorithmic contract for risk scoring and explainable indicator extraction.
- **`DatabaseOperations<T>`**: Defines the generic data persistence contract for the DAO layer.
- **`Validatable`**: Defines self-validation enforcement across domain models (`Transaction.validate()`, `User.validate()`).
- **File references**:
  - `com.frauddetection.detection.FraudDetectionStrategy`
  - `com.frauddetection.dao.DatabaseOperations`
  - `com.frauddetection.util.Validatable`

##### E. Custom Exception Handling Hierarchy
- Structured exception hierarchy extending checked `FraudSystemException`:
  - `FraudSystemException` (Root base exception)
    - `InvalidInputException`: Thrown on validation constraints (negative amounts, blank fields).
    - `InvalidTransactionException`: Thrown on malformed transaction payloads.
    - `DatabaseOperationException`: Wraps JDBC SQL errors with SQLState and ErrorCode.
    - `FraudAnalysisException`: Thrown on scoring or ensemble pipeline failures.
- Zero swallowed exceptions; all exceptions are logged and converted to user-friendly Swing feedback dialogs.

---

#### 2. Collections & Generics (6 Marks)

##### A. Generics
- **Generic Interface**:
  ```java
  public interface DatabaseOperations<T> {
      T save(T entity) throws DatabaseOperationException;
      boolean update(T entity) throws DatabaseOperationException;
      boolean delete(int id) throws DatabaseOperationException;
      Optional<T> findById(int id) throws DatabaseOperationException;
      List<T> findAll() throws DatabaseOperationException;
  }
  ```
- **Generic DAO Implementations**:
  - `UserDAO implements DatabaseOperations<User>`
  - `TransactionDAO implements DatabaseOperations<Transaction>`
  - `FraudAnalysisDAO implements DatabaseOperations<FraudAnalysis>`
  - `FraudIndicatorDAO implements DatabaseOperations<FraudIndicator>`
  - `AuditLogDAO implements DatabaseOperations<AuditLog>`
- **Generic Utility Methods**: `ValidationUtil.<T> requireNonNull(T object, String fieldName)`.

##### B. Java Collections Framework
- **`List<T>` / `ArrayList<T>`**:
  - `List<FraudDetector>` in `FraudDetectionEngine`
  - `List<FraudIndicator>` in `FraudAnalysis`
  - `List<String>` in `FraudAnalysis.reasons`
  - `List<TransactionHistoryItem>` in `TransactionService`
- **`Set<T>` / `HashSet<T>`**:
  - `Set<String> URGENCY_KEYWORDS` in `TextFraudDetector`
  - `Set<String> CREDENTIAL_KEYWORDS` in `TextFraudDetector`
  - `Set<String> URL_SHORTENERS` in `URLFraudDetector`
  - `Set<String> SUSPICIOUS_DOMAIN_TOKENS` in `URLFraudDetector`
- **`Map<K, V>` / `HashMap<K, V>` / `EnumMap<K, V>`**:
  - `Map<String, Integer>` in `TextFraudDetector.countCategoryHits()`
  - `Map<String, Double>` in `FraudDetectionEngine.strategyWeights`
  - `Map<String, Object>` in `AnalysisStatistics.getSnapshot()` and `FraudAnalysisDAO.getRiskStatistics()`
  - `EnumMap<RiskLevel, Integer>` in `RiskDistributionChart`
- **`Queue<T>` / `BlockingQueue<T>`**:
  - `BlockingQueue<AuditLog>` in `AuditLogger` for asynchronous queue consumption.

---

#### 3. Multithreading & Synchronization (4 Marks)

##### A. Asynchronous Concurrency Without Freezing GUI
- **Problem Solved**: Long-running risk scoring and database I/O freeze the Swing Event Dispatch Thread (EDT) if run synchronously.
- **Solution**:
  - `ExecutorService threadPool = Executors.newFixedThreadPool(4)` in `FraudAnalysisService`.
  - Concurrency Tasks:
    - `FraudAnalysisTask implements Callable<FraudAnalysis>`: Runs AI evaluation in background thread and emits progress callbacks.
    - `DatabaseSaveTask implements Callable<FraudAnalysis>`: Runs transactional multi-table persistence in background thread.
    - `HistoryLoadingTask` / background threads in `TransactionHistoryPanel` and `StatisticsPanel`.
  - Swing Safety: UI updates and result bindings are dispatched safely back to the EDT via `SwingUtilities.invokeLater(...)`.

##### B. Thread Synchronization & Shared State Protection
- **`AnalysisStatistics`**: Shared singleton maintaining real-time system metrics across concurrent worker threads.
  - `synchronized void recordAnalysis(...)`
  - `synchronized Map<String, Object> getSnapshot()`
  - `synchronized void syncFromDatabase(...)`
- **`AuditLogger`**:
  - Thread-safe non-blocking submission to a `LinkedBlockingQueue<AuditLog>`.
  - Dedicated background daemon worker thread (`AuditLogger-WorkerThread`) drains the queue and persists logs asynchronously.

---

#### 4. Classes for Database Operations (7 Marks)

- Complete separation of concerns:
  - **Model**: Pure state and validation contracts.
  - **DAO**: Direct JDBC queries, mapping `ResultSet` to domain entities.
  - **Service**: Business logic, workflow orchestration, audit logging, transactions.
  - **Detection**: Independent heuristic risk calculation algorithms.
  - **GUI**: Interactive Swing interface.
- Full CRUD Support:
  - Create: `save(T)` across all DAOs.
  - Read: `findById(int)`, `findAll()`, `findByUsername()`, `findByTransactionId()`.
  - Update: `update(T)` across DAOs.
  - Delete: `delete(int)` in `TransactionDAO` (cascades to analysis and indicators).
- Analytical aggregation methods:
  - `FraudAnalysisDAO.getRiskStatistics()`: Calculates SQL `COUNT(*)`, `AVG(risk_score)`, and condition-based tier distribution.

---

#### 5 & 6. Database Connectivity (JDBC) (6 Marks)

##### A. JDBC Architecture & Connection Management
- `DatabaseConnection` singleton manages connection acquisition.
- Hierarchical configuration: Environment variables -> `config.properties` -> fallback defaults.
- Diagnostic `testConnection()` method verifies connectivity and reports informative error messages if MySQL is offline.

##### B. Defensive Database Engineering & Transactions
- **Zero SQL Injection**: 100% of queries use `PreparedStatement` with typed parameter binding (`ps.setString()`, `ps.setDouble()`, etc.). Not a single raw SQL string concatenation with user input exists.
- **Multi-Table Atomic Transactions**:
  - `DatabaseSaveTask` explicitly invokes `conn.setAutoCommit(false)`.
  - Inserts `Transaction`, retrieves generated primary key (`Statement.RETURN_GENERATED_KEYS`), inserts `FraudAnalysis`, batch-inserts `FraudIndicator` records using `ps.addBatch()` and `ps.executeBatch()`.
  - Executes `conn.commit()` on success, or `conn.rollback()` inside `catch (SQLException ex)` on failure.
- **Resource Management**: All statements, connections, and result sets use `try-with-resources` ensuring zero memory or connection pool leakage.
