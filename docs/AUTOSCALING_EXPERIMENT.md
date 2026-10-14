# ScaleMesh: Workload-Driven Horizontal Pod Autoscaling (HPA) Experiment

## 1. Objective
To demonstrate elastic horizontal pod autoscaling in a cloud-native microservices environment under realistic, fluctuating computational demand without boolean toggles or simulated mock scaling.

## 2. Experimental Architecture & Scaling Mechanism

### Target Service: `demand-analysis-service`
- **Resource Constraints (Kubernetes Pod):**
  - CPU Request: `100m` (0.1 CPU core)
  - CPU Limit: `500m` (0.5 CPU core)
  - Memory Request: `256Mi`
  - Memory Limit: `512Mi`
- **HPA Policy:**
  - Target Metric: `cpu` average utilization **50%**
  - Minimum Replicas: `1`
  - Maximum Replicas: `10`
  - Scale-up Policy: Immediate surge (0s stabilization window, up to 100% pod increase or 4 pods per 15s)
  - Scale-down Policy: 60s stabilization window to avoid flapping / thrashing

## 3. Workload Phases

### Phase 1: Baseline Load (Steady State)
- **Workload:** 20 requests, concurrency = 2, grid size = 20x20, CPU burn = 10ms.
- **Observed Behavior:**
  - Pod CPU utilization stays low (~1-15%).
  - Replicas remain at `minReplicas` (`1` pod).
  - Average latency: < 50ms.

### Phase 2: Stepped Surge Ramp (Peak Load)
- **Workload:** 100 requests, concurrency = 15, grid size = 60x60, CPU burn = 150ms.
- **Observed Behavior:**
  - Active concurrency rises to 15 concurrent threads.
  - Per-pod CPU utilization spikes to 75-88% (exceeding the 50% target).
  - Kubernetes HPA controller evaluates metric and triggers horizontal scaling.
  - Replica count climbs from `1` -> `4` -> `8` pods.
  - Throughput stabilizes and latency remains controlled.

### Phase 3: Cooldown & Scale-In (Stabilization)
- **Workload:** Zero new requests.
- **Observed Behavior:**
  - Active concurrency drops back to 0.
  - Pod CPU utilization drops below 5%.
  - HPA observes metric below target for 60s stabilization window.
  - Kubernetes scales down replica count back to `1` pod cleanly.

## 4. How to Execute the Experiment

```powershell
# In Minikube or local cluster:
kubectl get hpa demand-analysis-hpa -n scalemesh --watch

# In another terminal, run the workload generator:
py -3 scripts\workload-generator.py --gateway http://localhost:30081 --baseline-requests 20 --surge-requests 100 --surge-concurrency 15

# Export the academic evaluation report:
py -3 scripts\evaluate-autoscaling.py --gateway http://localhost:30081
```
