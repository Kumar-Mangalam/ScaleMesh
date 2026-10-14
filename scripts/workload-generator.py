#!/usr/bin/env python3
"""
ScaleMesh Workload Generator
Simulates realistic fleet coordination and demand analysis traffic against the ScaleMesh API Gateway.
Specifically triggers Kubernetes Horizontal Pod Autoscaler (HPA) dynamics through controlled CPU burst.
"""

import argparse
import json
import random
import sys
import time
import urllib.request
import urllib.error
from concurrent.futures import ThreadPoolExecutor, as_completed

def send_request(url, payload):
    data = json.dumps(payload).encode('utf-8')
    req = urllib.request.Request(
        url,
        data=data,
        headers={'Content-Type': 'application/json', 'User-Agent': 'ScaleMesh-LoadGenerator/1.0'}
    )
    start_time = time.perf_counter()
    try:
        with urllib.request.urlopen(req, timeout=15) as response:
            latency_ms = (time.perf_counter() - start_time) * 1000.0
            status_code = response.getcode()
            body = json.loads(response.read().decode('utf-8'))
            return True, status_code, latency_ms, body
    except urllib.error.HTTPError as e:
        latency_ms = (time.perf_counter() - start_time) * 1000.0
        return False, e.code, latency_ms, str(e)
    except Exception as e:
        latency_ms = (time.perf_counter() - start_time) * 1000.0
        return False, 0, latency_ms, str(e)

def run_phase(name, gateway_url, num_requests, concurrency, grid_size, cpu_burn_ms, intensity):
    print(f"\n=======================================================")
    print(f"[*] Starting Phase: {name}")
    print(f"[*] Target: {gateway_url}")
    print(f"[*] Requests: {num_requests} | Concurrency: {concurrency}")
    print(f"[*] Matrix Grid: {grid_size}x{grid_size} | CPU Burn: {cpu_burn_ms}ms | Intensity: {intensity}")
    print(f"=======================================================")

    demand_url = f"{gateway_url}/api/demand/calculate"
    success_count = 0
    failure_count = 0
    latencies = []

    regions = ["downtown-metro", "airport-hub", "tech-corridor", "harbor-district"]

    def worker_task(req_id):
        payload = {
            "regionId": random.choice(regions),
            "gridSize": grid_size,
            "iterationCount": 15000 * intensity,
            "cpuBurnMs": cpu_burn_ms,
            "intensityLevel": intensity
        }
        return send_request(demand_url, payload)

    phase_start = time.perf_counter()
    with ThreadPoolExecutor(max_workers=concurrency) as executor:
        futures = [executor.submit(worker_task, i) for i in range(num_requests)]
        for idx, future in enumerate(as_completed(futures), 1):
            success, code, lat, body = future.result()
            latencies.append(lat)
            if success:
                success_count += 1
            else:
                failure_count += 1
            
            if idx % max(1, num_requests // 10) == 0 or idx == num_requests:
                print(f"  [{name}] Progress: {idx}/{num_requests} requests (Success: {success_count}, Fail: {failure_count})")

    phase_duration = time.perf_counter() - phase_start
    avg_lat = sum(latencies) / len(latencies) if latencies else 0.0
    p95_lat = sorted(latencies)[int(len(latencies) * 0.95)] if latencies else 0.0
    throughput = len(latencies) / phase_duration if phase_duration > 0 else 0.0

    print(f"\n[+] Completed Phase: {name}")
    print(f"    Duration:    {phase_duration:.2f}s")
    print(f"    Throughput:  {throughput:.2f} req/s")
    print(f"    Success:     {success_count}/{num_requests} ({success_count/num_requests*100:.1f}%)")
    print(f"    Avg Latency: {avg_lat:.2f}ms")
    print(f"    p95 Latency: {p95_lat:.2f}ms")

    return {
        "phase": name,
        "requests": num_requests,
        "concurrency": concurrency,
        "duration_sec": round(phase_duration, 2),
        "throughput_rps": round(throughput, 2),
        "avg_latency_ms": round(avg_lat, 2),
        "p95_latency_ms": round(p95_lat, 2),
        "success_rate_pct": round(success_count / num_requests * 100, 1)
    }

def main():
    parser = argparse.ArgumentParser(description="ScaleMesh Workload & HPA Stress Generator")
    parser.add_argument("--gateway", default="http://localhost:8081", help="API Gateway URL")
    parser.add_argument("--baseline-requests", type=int, default=20, help="Requests for baseline phase")
    parser.add_argument("--surge-requests", type=int, default=50, help="Requests for surge phase")
    parser.add_argument("--surge-concurrency", type=int, default=10, help="Concurrency during surge")
    args = parser.parse_args()

    results = []

    # 1. Baseline Load (Normal steady-state traffic)
    r1 = run_phase(
        name="1_BASELINE_LOAD",
        gateway_url=args.gateway,
        num_requests=args.baseline_requests,
        concurrency=2,
        grid_size=20,
        cpu_burn_ms=10,
        intensity=1
    )
    results.append(r1)

    print("\n[+] Pausing 5 seconds before surge ramp...")
    time.sleep(5)

    # 2. Stepped Surge Load (High concurrency + intense CPU convolution)
    r2 = run_phase(
        name="2_STEPPED_SURGE_LOAD",
        gateway_url=args.gateway,
        num_requests=args.surge_requests,
        concurrency=args.surge_concurrency,
        grid_size=60,
        cpu_burn_ms=150,
        intensity=4
    )
    results.append(r2)

    # Summary
    print("\n=======================================================")
    print("[*] Workload Execution Summary:")
    print(json.dumps(results, indent=2))
    print("=======================================================\n")

if __name__ == "__main__":
    main()
