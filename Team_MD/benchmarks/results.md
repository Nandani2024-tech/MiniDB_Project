# MiniDB Benchmarks

## 1. Table Scan vs Index Scan Latency

| Table Size (Rows) | SeqScan Latency (ns) | IndexScan Latency (ns) | Speedup |
|-------------------|----------------------|------------------------|---------|
| 100 | 275,500 | 29,340 | 9.39x |
| 1000 | 914,080 | 32,860 | 27.82x |
| 10000 | 6,708,290 | 30,220 | 221.98x |

## 2. MVCC Read Throughput & Non-Blocking Readers

Ran 10 concurrent readers alongside 1 writer (which slept for 200ms).

- **Total Table Scans Completed**: 201
- **Throughput**: 397.23 scans/sec
- **Max Reader Latency**: 38 ms

*(Since max reader latency is well under 200ms, readers were **not** blocked by the active writer).*

## 3. WAL / Recovery Overhead

| Mode | Time per Commit (ms) | Time per Commit (ns) |
|------|----------------------|----------------------|
| `fsync()` Enabled (Safe) | 0.7016 | 701,584 |
| `fsync()` Disabled (Fast) | 0.0025 | 2,509 |

*(Synchronous disk writes add significant latency, demonstrating the durability vs performance trade-off).*

