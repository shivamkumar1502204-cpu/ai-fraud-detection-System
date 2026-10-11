# Viva Voce & Oral Examination Preparation Guide
## AI Powered Fraud Detection System

This document provides 27 comprehensive, high-scoring answers to questions frequently asked by university examiners, professors, and technical evaluators.

---

### Category 1: Architecture & Technology Stack

#### Q1. Why did you choose Java Swing instead of a web framework like React or Spring Boot?
**Answer**:
"Java Swing is a native desktop GUI toolkit built directly into standard Java. Desktop applications are widely used in enterprise banking, compliance rooms, and SOC (Security Operations Centers) where operators require low-latency desktop access, local peripheral interaction, and zero reliance on browser sandboxes or external web servers. Furthermore, building in pure Swing directly tests fundamental OOP GUI principles: event dispatching, component hierarchy, threading model, and custom 2D painting without abstracting them behind web frameworks."

#### Q2. Why did you use JDBC instead of an ORM like Hibernate or JPA?
**Answer**:
"Direct JDBC (Java Database Connectivity) provides explicit control over SQL queries, statement compilation, transaction boundaries, and connection management. In academic evaluation and financial compliance systems, understanding explicit JDBC lifecycle steps—such as obtaining a `Connection`, preparing a `PreparedStatement`, executing a batch, handling `ResultSet` iteration, and calling explicit `commit()` or `rollback()`—is essential. It demonstrates core database engineering without relying on automated magic."

#### Q3. How is the project structured to ensure Separation of Concerns?
**Answer**:
"The project follows a clean N-Tier layered architecture:
- **`model`**: Domain entities representing data contracts (`Transaction`, `User`, `FraudAnalysis`).
- **`dao`**: Data Access Objects encapsulating SQL and JDBC interactions.
- **`service`**: Business logic, workflow coordination, and async thread management.
- **`detection`**: Autonomous fraud scoring algorithms.
- **`gui`**: Swing presentation components.
This decoupling ensures that changing the database or swapping the AI detection algorithm never requires rewriting the user interface."

---

### Category 2: Object-Oriented Programming (OOP)

#### Q4. What is Polymorphism, and where is it specifically implemented in this project?
**Answer**:
"Polymorphism allows objects of different concrete classes to be treated through a uniform parent interface. In our project, `FraudDetectionEngine` maintains a list declared as:
```java
List<FraudDetector> detectors;
```
During runtime analysis, the engine iterates polymorphically over this list:
```java
for (FraudDetector detector : detectors) {
    double risk = detector.calculateRisk(transaction);
    List<FraudIndicator> ind = detector.extractIndicators(transaction);
}
```
Although each detector (`TransactionFraudDetector`, `TextFraudDetector`, `URLFraudDetector`, `AnomalyFraudDetector`) executes entirely different domain logic, the calling engine interacts strictly through the parent contract."

#### Q5. Where is Inheritance implemented, and why is an abstract class used?
**Answer**:
"Inheritance is implemented with the abstract base class `FraudDetector`:
```java
public abstract class FraudDetector implements FraudDetectionStrategy
```
The four concrete subclasses extend `FraudDetector`. An abstract class was chosen rather than a pure interface because all detectors share common state (`strategyName`, `weight`) and shared helper routines (`clampScore()`, `createIndicator()`). This eliminates code duplication across subclasses while enforcing an abstract template for `calculateRisk()` and `extractIndicators()`."

#### Q6. What is the difference between an Interface and an Abstract Class in this project?
**Answer**:
"- **Interface (`FraudDetectionStrategy`, `DatabaseOperations<T>`, `Validatable`)**: Defines an abstract contract of capability with no state. For example, `DatabaseOperations<T>` defines what CRUD operations a persistence class must provide.
- **Abstract Class (`FraudDetector`)**: Provides partial implementation and shared state (`protected final double weight`) that subclasses inherit."

#### Q7. How does Encapsulation protect data integrity in your entities?
**Answer**:
"All instance variables in our model classes are marked `private`. External classes cannot mutate state directly. Access is mediated through validated getters and setters. Additionally, methods like `getReasons()` and `getIndicators()` in `FraudAnalysis` return `Collections.unmodifiableList(...)` or defensive copies, preventing external code from corrupting internal collections."

#### Q8. How is Exception Handling architected in this application?
**Answer**:
"We built a domain-specific checked exception hierarchy rooted at `FraudSystemException`:
- `InvalidInputException`: Thrown on validation failures (e.g., negative money, blank required fields).
- `InvalidTransactionException`: Thrown on malformed transaction payloads.
- `DatabaseOperationException`: Wraps low-level JDBC `SQLException` instances, retaining SQLState and ErrorCode.
- `FraudAnalysisException`: Thrown when scoring algorithms encounter mathematical or pipeline faults.
We never swallow exceptions with empty catch blocks; every error is logged via `AuditLogger` and converted to user-friendly Swing alert dialogs."

---

### Category 3: Collections & Generics

#### Q9. Why did you use Generics in the DAO layer?
**Answer**:
"We created a generic interface:
```java
public interface DatabaseOperations<T> {
    T save(T entity) throws DatabaseOperationException;
    boolean update(T entity) throws DatabaseOperationException;
    boolean delete(int id) throws DatabaseOperationException;
    Optional<T> findById(int id) throws DatabaseOperationException;
    List<T> findAll() throws DatabaseOperationException;
}
```
Generics provide compile-time type safety, eliminate dangerous runtime casts, and enforce a consistent persistence contract across `UserDAO`, `TransactionDAO`, `FraudAnalysisDAO`, `FraudIndicatorDAO`, and `AuditLogDAO`."

#### Q10. What Java Collection types did you use and why?
**Answer**:
"We used multiple specialized collections tailored to specific access patterns:
1. **`ArrayList<T>`**: Fast indexed access and dynamic resizing for lists of indicators, transactions, and reasons.
2. **`HashSet<String>`**: O(1) constant-time membership testing for high-risk token dictionaries (`URGENCY_KEYWORDS`, `CREDENTIAL_KEYWORDS`, `URL_SHORTENERS`).
3. **`HashMap<String, Integer>`**: Key-value mapping for counting keyword frequency occurrences and strategy weights.
4. **`EnumMap<RiskLevel, Integer>`**: High-efficiency array-backed map for risk tier distribution counting.
5. **`LinkedBlockingQueue<AuditLog>`**: Thread-safe FIFO queue for the asynchronous audit logging producer-consumer pipeline."

---

### Category 4: Multithreading & Synchronization

#### Q11. Why is Multithreading necessary in a Java Swing application?
**Answer**:
"Java Swing is single-threaded; all user input, button clicks, and screen repainting are executed by the single Event Dispatch Thread (EDT). If a long-running computation—such as heuristic risk analysis or a network database query—is executed directly on the EDT, the entire user interface freezes, buttons stop responding, and Windows displays 'Not Responding'. By executing analysis and database persistence in background threads using `ExecutorService`, the GUI remains responsive, displays smooth progress animations, and lets the operator continue interacting with the system."

#### Q12. How do you safely update Swing components from a background thread?
**Answer**:
"Swing components are not thread-safe and must only be modified on the Event Dispatch Thread. Whenever a background task completes or reports progress, we dispatch the update using `SwingUtilities.invokeLater(() -> { ... })`. This queues the UI update on the EDT message pump."

#### Q13. Where is Synchronization used, and what problem does it prevent?
**Answer**:
"Synchronization is used in `AnalysisStatistics`:
```java
public synchronized void recordAnalysis(int score, RiskLevel level, Decision decision) {
    totalAnalyses++;
    totalRiskScoreSum += score;
    ...
}
public synchronized Map<String, Object> getSnapshot() { ... }
```
Because multiple background threads can process transactions concurrently, concurrent read-modify-write operations on shared counters (`totalAnalyses++`) would suffer from race conditions and memory consistency errors. Marking these methods `synchronized` ensures mutual exclusion, atomic updates, and memory visibility across threads."

#### Q14. How does your asynchronous Audit Logger work?
**Answer**:
"`AuditLogger` implements the Producer-Consumer pattern using a `LinkedBlockingQueue<AuditLog>`. When an event occurs, `log()` offers the record to the queue without waiting. A dedicated background daemon thread (`AuditLogger-WorkerThread`) calls `queue.take()`, which blocks until an entry is available, and writes it to MySQL via `AuditLogDAO`. This decouples database I/O latency from the primary transaction processing path."

---

### Category 5: Database & JDBC

#### Q15. What is the difference between Statement and PreparedStatement?
**Answer**:
"- **`Statement`**: SQL query is passed as a raw string and compiled on every execution. If user input is concatenated into the SQL string, attackers can inject arbitrary SQL commands (SQL Injection).
- **`PreparedStatement`**: Pre-compiles the SQL template on the database server. User parameters are bound using typed placeholders (`?`), treating input strictly as literal data rather than executable code. It prevents SQL injection and provides performance benefits through query plan caching."

#### Q16. Did you use any string concatenation in your SQL queries?
**Answer**:
"No. 100% of SQL queries across all DAO classes (`UserDAO`, `TransactionDAO`, `FraudAnalysisDAO`, `FraudIndicatorDAO`, `AuditLogDAO`) use parameterized `PreparedStatement` with typed methods like `setString()`, `setInt()`, `setDouble()`, and `setTimestamp()`. User input is never concatenated directly into SQL strings."

#### Q17. How do you manage database transactions across multiple tables?
**Answer**:
"In `DatabaseSaveTask`, saving a transaction requires writing to `transactions`, `fraud_analysis`, and `fraud_indicators`. We manage this atomically using manual commit mode:
```java
conn.setAutoCommit(false);
try {
    transactionDAO.save(tx, conn);
    analysisDAO.save(analysis, conn);
    indicatorDAO.saveBatch(analysis.getId(), indicators, conn);
    conn.commit(); // Atomic commit
} catch (SQLException ex) {
    conn.rollback(); // Rollback on any failure
    throw ex;
} finally {
    conn.setAutoCommit(true);
}
```
If any insert fails, all changes are rolled back, preserving database consistency."

#### Q18. What happens if MySQL is not running or credentials are wrong?
**Answer**:
"`DatabaseConnection.testConnection()` diagnoses the connection state on startup without crashing the application. If MySQL is unreachable, the system catches `DatabaseOperationException` gracefully, displays a clear status warning badge ('● MySQL Offline') on the login screen, and alerts the user to start MySQL and run `schema.sql`."

#### Q19. How are user passwords stored in the database?
**Answer**:
"Passwords are never stored in plain text. We use standard Java cryptographic libraries (`MessageDigest` with `SHA-256`) combined with a cryptographically secure 16-byte random salt generated via `SecureRandom`. Each user has a unique salt stored in the `users` table. During authentication, the candidate password is concatenated with the user's stored salt and hashed before comparison using constant-time `MessageDigest.isEqual(...)`."

---

### Category 6: AI Fraud Detection Logic

#### Q20. Why do you describe the engine as a 'Hybrid AI-Inspired Risk Engine'?
**Answer**:
"To maintain academic honesty. The application does not use external generative AI APIs like ChatGPT or black-box third-party services. Instead, it implements a local, explainable AI-inspired risk heuristic architecture utilizing feature extraction, weighted multi-factor scoring, baseline deviation ratios, anomaly detection, and ensemble fusion. It behaves dynamically and is designed so a trained ONNX machine learning model could be dropped into the `FraudDetector` hierarchy without rewriting the GUI or database layers."

#### Q21. How is the final fraud score calculated?
**Answer**:
"1. Each detector calculates an independent sub-score (0–100) and extracts explainable indicators.
2. The engine computes a weighted average across all registered detectors based on their configured weights (Transaction Heuristics: 35%, Text Social Engineering: 25%, URL Phishing: 20%, Device Anomalies: 20%).
3. **Ensemble Threat Fusion**: If a critical single-vector threat (such as OTP harvesting or a 20x baseline money spike) is detected with a raw threat score $\ge 70$, peak pooling blends the peak threat with the ensemble average to prevent the severe attack from being diluted by other nominal features."

#### Q22. How are the Risk Levels and Decisions mapped?
**Answer**:
"- **Risk Score 0–29**: `LOW` Risk $\rightarrow$ `SAFE` (Approved for standard processing)
- **Risk Score 30–59**: `MEDIUM` Risk $\rightarrow$ `SUSPICIOUS` (Flagged for Step-Up MFA / Secondary review)
- **Risk Score 60–79**: `HIGH` Risk $\rightarrow$ `SUSPICIOUS` or `FRAUDULENT` (Hold transaction, contact cardholder)
- **Risk Score 80–100**: `CRITICAL` Risk $\rightarrow$ `FRAUDULENT` (Do not proceed, lock session, investigate)"

---

### Category 7: Verification & Testing

#### Q23. How did you test the system?
**Answer**:
"We built an automated test runner (`SystemVerificationTest`) that tests all 7 rubric verification scenarios:
1. Normal low-value transaction $\rightarrow$ Verifies score $\le 29$ (`LOW` / `SAFE`).
2. High amount + new device $\rightarrow$ Verifies `MEDIUM` or `HIGH` risk.
3. Suspicious URL + urgent message + foreign location $\rightarrow$ Verifies `HIGH` or `CRITICAL` risk.
4. Credential/OTP harvesting text $\rightarrow$ Verifies `HIGH` or `CRITICAL` risk.
5. Negative amount input $\rightarrow$ Verifies `InvalidInputException` is thrown.
6. Database disconnection $\rightarrow$ Verifies graceful exception handling.
7. 50 concurrent analyses on 8 worker threads $\rightarrow$ Verifies thread safety and synchronized counters."

#### Q24. Can an operator delete or alter audit logs?
**Answer**:
"No. In `AuditLogDAO`, the `update()` and `delete()` methods throw `DatabaseOperationException('Audit logs are immutable')`. This conforms to financial regulatory standards (e.g., PCI-DSS, SOX) which mandate write-only audit trails."

#### Q25. What is the role of the `Validatable` interface?
**Answer**:
"`Validatable` enforces self-validation on domain models:
```java
public interface Validatable {
    void validate() throws InvalidInputException;
}
```
`Transaction` and `User` implement this interface. When a transaction is submitted, calling `transaction.validate()` validates positive amounts, non-empty fields, valid URL syntax, and length constraints before any analysis or database persistence begins."

#### Q26. How can this application be extended in production?
**Answer**:
"1. **Machine Learning Model Integration**: Drop in an ONNX runtime or PMML model (e.g., XGBoost, Isolation Forest) into a new `MLFraudDetector extends FraudDetector`.
2. **Real-Time Notification Pipeline**: Add Webhook or SMS/Email notifications when a `CRITICAL` transaction is flagged.
3. **Connection Pooling**: Incorporate HikariCP for enterprise connection pooling under high concurrent loads."

#### Q27. What happens when a user deletes a transaction in the history table?
**Answer**:
"Because foreign keys in `schema.sql` are defined with `ON DELETE CASCADE`, deleting a transaction row in the `transactions` table automatically removes its child records in `fraud_analysis` and `fraud_indicators`. Furthermore, the deletion action is logged to the `audit_logs` table, recording who performed the deletion and which transaction was removed."
