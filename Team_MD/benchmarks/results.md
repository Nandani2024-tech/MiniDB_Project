# MiniDB Benchmarks

## 1. Table Scan vs Index Scan Latency

| Table Size (Rows) | SeqScan Latency (ns) | IndexScan Latency (ns) | Speedup |
|-------------------|----------------------|------------------------|---------|
| 100 | 255,970 | 15,760 | 16.24x |
| 1000 | 705,110 | 26,850 | 26.26x |
| 10000 | 4,774,160 | 22,280 | 214.28x |

## 2. MVCC Read Throughput & Non-Blocking Readers

Ran 10 concurrent readers alongside 1 writer (which slept for 200ms).

- **Total Table Scans Completed**: 180
- **Throughput**: 351.56 scans/sec
- **Max Reader Latency**: 40 ms

*(Since max reader latency is well under 200ms, readers were **not** blocked by the active writer).*

## 3. WAL / Recovery Overhead

| Mode | Time per Commit (ms) | Time per Commit (ns) |
|------|----------------------|----------------------|
| `fsync()` Enabled (Safe) | 0.5837 | 583,674 |
| `fsync()` Disabled (Fast) | 0.0020 | 1,989 |

*(Synchronous disk writes add significant latency, demonstrating the durability vs performance trade-off).*

